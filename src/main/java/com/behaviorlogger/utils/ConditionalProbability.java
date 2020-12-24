package com.behaviorlogger.utils;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import com.behaviorlogger.models.MappableChar;
import com.behaviorlogger.models.behaviors.BehaviorEvent;
import com.behaviorlogger.models.behaviors.ContinuousBehavior;
import com.behaviorlogger.models.behaviors.DiscreteBehavior;
import com.behaviorlogger.models.schemas.KeyBehaviorMapping;
import com.behaviorlogger.persistence.recordings.RecordingRawJson1_1.ContinuousEvent;
import com.behaviorlogger.persistence.recordings.RecordingRawJson1_1.DiscreteEvent;
import com.google.common.collect.Lists;

public class ConditionalProbability {

    public static class TooManyBackgroundEventsException extends Exception {
	private static final long serialVersionUID = 1L;
    }

    public static int WINDOW_5 = 5000;
    public static int WINDOW_10 = 10000;
    public static int WINDOW_15 = 15000;
    public static int WINDOW_20 = 20000;

    public static class Results {
	public double probability;
	public long sampled;
	public long total;

	public Results(double probability, long sampled, long total) {
	    this.probability = probability;
	    this.sampled = sampled;
	    this.total = total;
	}

	@Override
	public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    long temp;
	    temp = Double.doubleToLongBits(probability);
	    result = prime * result + (int) (temp ^ (temp >>> 32));
	    result = prime * result + (int) (sampled ^ (sampled >>> 32));
	    result = prime * result + (int) (total ^ (total >>> 32));
	    return result;
	}

	@Override
	public boolean equals(Object obj) {
	    if (this == obj)
		return true;
	    if (obj == null)
		return false;
	    if (getClass() != obj.getClass())
		return false;
	    Results other = (Results) obj;
	    if (Double.doubleToLongBits(probability) != Double.doubleToLongBits(other.probability))
		return false;
	    if (sampled != other.sampled)
		return false;
	    if (total != other.total)
		return false;
	    return true;
	}

	@Override
	public String toString() {
	    return "Results [probability=" + probability + ", sampled=" + sampled + ", total=" + total + "]";
	}
    }

    public static class AllResults {
	public static Comparator<AllResults> compare = //
		(r1, r2) -> (int) (Math.floor(r1.avg * 1000) - Math.floor(r2.avg * 1000));

	public final Results binaryEO;
	public final Results binaryNonEO;
	public final Results proportionEO;
	public final Results proportionNonEO;
	public final Double avg;

	public AllResults(Results binaryEO, Results binaryNonEO, Results proportionEO, Results proportionNonEO) {
	    this.binaryEO = binaryEO;
	    this.binaryNonEO = binaryNonEO;
	    this.proportionEO = proportionEO;
	    this.proportionNonEO = proportionNonEO;
	    this.avg = (binaryEO.probability + binaryNonEO.probability + proportionEO.probability
		    + proportionNonEO.probability) / 4;
	}

	@Override
	public String toString() {
	    return "AllResults [binaryEO=" + binaryEO + ", binaryNonEO=" + binaryNonEO + ", proportionEO="
		    + proportionEO + ", proportionNonEO=" + proportionNonEO + ", avg=" + avg + "]";
	}
    }

    public static AllResults all(List<BehaviorEvent> targetEvents, //
	    List<BehaviorEvent> consequentEvents, //
	    int windowMillis) {
	return new AllResults(//
		binaryEO(targetEvents, consequentEvents, windowMillis), //
		binaryNonEO(targetEvents, consequentEvents, windowMillis), //
		proportionEO(targetEvents, consequentEvents, windowMillis), //
		proportionNonEO(targetEvents, consequentEvents, windowMillis));
    }

    public static Results binaryNonEO( //
	    List<BehaviorEvent> targetEvents, //
	    List<BehaviorEvent> consequentEvents, //
	    int windowMillis) {
	int numTargets = targetEvents.size();
	double numPotentiallyReinforcedTargets = 0f;
	for (BehaviorEvent te : targetEvents) {
	    boolean hasConsequentEvents = hasConsequentEvents(consequentEvents, te, windowMillis);
	    boolean hasOverlappingEvents = hasOverlappingEvents(consequentEvents, te.startTime);
	    if (hasConsequentEvents || hasOverlappingEvents) {
		numPotentiallyReinforcedTargets++;
	    }
	}
	if (numTargets == 0) {
	    return new Results(-1.0, numTargets, numTargets);
	}
	return new Results(numPotentiallyReinforcedTargets / numTargets, numTargets, numTargets);
    }

    private static boolean hasOverlappingEvents(List<BehaviorEvent> candidates, long targetTime) {
	for (BehaviorEvent ce : candidates) {
	    if (ce.startTime <= targetTime && ce.endTime() >= targetTime) {
		return true;
	    }
	}
	return false;
    }

    private static boolean hasConsequentEvents(List<BehaviorEvent> candidates, BehaviorEvent target,
	    long windowMillis) {
	long end = target.startTime + windowMillis;
	for (BehaviorEvent ce : candidates) {
	    if (ce.startTime > target.startTime && ce.startTime < end) {
		return true;
	    }
	}
	return false;
    }

    public static Results binaryEO( //
	    List<BehaviorEvent> targetEvents, //
	    List<BehaviorEvent> consequentEvents, //
	    int windowMillis) {
	int numTargets = 0;
	double numPotentiallyReinforcedTargets = 0.0;
	for (BehaviorEvent te : targetEvents) {
	    boolean hasOverlappingEvents = hasOverlappingEvents(consequentEvents, te.startTime);
	    if (hasOverlappingEvents) {
		continue;
	    }
	    numTargets++;
	    boolean hasConsequentEvents = hasConsequentEvents(consequentEvents, te, windowMillis);
	    if (hasConsequentEvents) {
		numPotentiallyReinforcedTargets++;
	    }
	}
	if (numTargets == 0) {
	    return new Results(-1.0, numTargets, targetEvents.size());
	}
	return new Results(numPotentiallyReinforcedTargets / numTargets, numTargets, targetEvents.size());
    }

    public static Results proportionNonEO(//
	    List<BehaviorEvent> targetEvents, //
	    List<BehaviorEvent> consequentEvents, //
	    int windowMillis) {
	double totalDurationOfReinforcingConsequences = 0.0;
	for (BehaviorEvent te : targetEvents) {
	    long windowEnd = te.startTime + windowMillis;
	    List<BehaviorEvent> matchingEvents = findConsequentEvents(consequentEvents, te, windowMillis);
	    matchingEvents.addAll(findOverlappingEvents(consequentEvents, te));
	    for (BehaviorEvent ce : matchingEvents) {
		long consequentEnd = ce.endTime();
		if (consequentEnd > windowEnd) {
		    consequentEnd = windowEnd;
		}
		long startTime = Math.max(ce.startTime, te.startTime);
		long duration = consequentEnd - startTime;
		totalDurationOfReinforcingConsequences += duration;
	    }
	}
	long totalMillis = windowMillis * (long) targetEvents.size();
	if (totalMillis == 0) {
	    return new Results(-1.0, targetEvents.size(), targetEvents.size());
	}
	return new Results(totalDurationOfReinforcingConsequences / totalMillis, targetEvents.size(),
		targetEvents.size());
    }

    private static List<BehaviorEvent> findOverlappingEvents(List<BehaviorEvent> candidates, BehaviorEvent target) {
	return candidates.stream() //
		.filter((ce) -> ce.startTime < target.startTime && ce.endTime() > target.startTime) //
		.collect(Collectors.toList());
    }

    private static List<BehaviorEvent> findConsequentEvents(List<BehaviorEvent> candidates, BehaviorEvent target,
	    long windowMillis) {
	double windowEnd = target.startTime + windowMillis;
	return candidates.stream() //
		.filter((ce) -> ce.startTime > target.startTime && ce.startTime < windowEnd) //
		.collect(Collectors.toList());
    }

    public static Results proportionEO( //
	    List<BehaviorEvent> targetEvents, //
	    List<BehaviorEvent> consequentEvents, //
	    int windowMillis) {
	long numTargets = 0;
	double totalDurationOfReinforcingConsequences = 0.0;
	for (BehaviorEvent te : targetEvents) {
	    boolean hasOverlappingEvents = hasOverlappingEvents(consequentEvents, te.startTime);
	    if (hasOverlappingEvents) {
		continue;
	    }
	    numTargets++;
	    long windowEnd = te.startTime + windowMillis;
	    List<BehaviorEvent> matchingConsequentEvents = findConsequentEvents(consequentEvents, te, windowMillis);
	    for (BehaviorEvent ce : matchingConsequentEvents) {
		long endTime = ce.endTime();
		if (endTime > windowEnd) {
		    endTime = windowEnd;
		}
		long duration = endTime - ce.startTime;
		totalDurationOfReinforcingConsequences += duration;
	    }
	}
	long totalMillis = windowMillis * numTargets;
	if (totalMillis == 0) {
	    return new Results(-1.0, numTargets, targetEvents.size());
	}
	double proportion = totalDurationOfReinforcingConsequences / totalMillis;
	return new Results(proportion, numTargets, targetEvents.size());
    }

    public static List<BehaviorEvent> getTargetEvents( //
	    KeyBehaviorMapping targetBehavior, //
	    List<DiscreteEvent> discreteEvents, //
	    List<ContinuousEvent> continuousEvents) {
	List<BehaviorEvent> targetEvents;
	if (targetBehavior.isContinuous) {
	    targetEvents = continuousEvents.stream() //
		    .filter((e) -> e.behaviorUuid.equals(targetBehavior.uuid)) //
		    .map((e) -> new ContinuousBehavior(targetBehavior.uuid, targetBehavior.key,
			    targetBehavior.description, e.startTime, (int) (e.endTime - e.startTime))) //
		    .collect(Collectors.toList());
	} else {
	    targetEvents = discreteEvents.stream() //
		    .filter((e) -> e.behaviorUuid.equals(targetBehavior.uuid)) //
		    .map((e) -> {
			return new DiscreteBehavior(targetBehavior.uuid, targetBehavior.key, targetBehavior.description,
				e.time);
		    }).collect(Collectors.toList());
	}
	return convertToDiscrete(targetEvents);
    }

    public static List<BehaviorEvent> getConsequenceEvents(KeyBehaviorMapping kbm, List<DiscreteEvent> discreteEvents,
	    List<ContinuousEvent> continuousEvents) {
	List<BehaviorEvent> consequenceEvents = Lists.newArrayList();
	for (DiscreteEvent evt : discreteEvents) {
	    if (evt.behaviorUuid.equals(kbm.uuid)) {
		BehaviorEvent discreteBehavior = new DiscreteBehavior(kbm.uuid, kbm.key, kbm.description, evt.time);
		consequenceEvents.add(discreteBehavior);
	    }
	}
	if (!consequenceEvents.isEmpty()) {
	    return consequenceEvents;
	}
	for (ContinuousEvent evt : continuousEvents) {
	    if (evt.behaviorUuid.equals(kbm.uuid)) {
		long duration = evt.endTime - evt.startTime;
		BehaviorEvent continuousBehavior = new ContinuousBehavior(kbm.uuid, kbm.key, kbm.description,
			evt.startTime, (int) duration);
		consequenceEvents.add(continuousBehavior);
	    }
	}
	return consequenceEvents;
    }

    public static List<BehaviorEvent> randomBackgroundEventsWithIter(Iterator<Long> randomInts, KeyBehaviorMapping kbm,
	    List<BehaviorEvent> consequenceEvents, long numEvents) {
	Set<Long> startTimes = new HashSet<>();
	for (int i = 0; i < numEvents; i++) {
	    Long next = randomInts.next();
	    while (startTimes.contains(next) || hasOverlappingEvents(consequenceEvents, next)) {
		next = randomInts.next();
	    }
	    startTimes.add(next);
	}
	List<BehaviorEvent> result = Lists.newArrayList();
	for (Long startTime : startTimes) {
	    DiscreteBehavior evt = new DiscreteBehavior(kbm.uuid, kbm.key, kbm.description, startTime);
	    result.add(evt);
	}
	return result;
    }

    public static List<BehaviorEvent> randomBackgroundEvents(KeyBehaviorMapping target,
	    List<BehaviorEvent> consequenceEvents, long duration, long numEvents)
	    throws TooManyBackgroundEventsException {

	long totalConsequencesMillis = 0;
	for (BehaviorEvent ce : consequenceEvents) {
	    totalConsequencesMillis += ce.endTime() - ce.startTime + 1;
	}
	long allowedMillis = (duration + 1) - totalConsequencesMillis;
	if (numEvents > allowedMillis) {
	    throw new TooManyBackgroundEventsException();
	}

	Iterator<Long> randomInts = new Iterator<Long>() {
	    @Override
	    public boolean hasNext() {
		return true;
	    }

	    @Override
	    public Long next() {
		return ThreadLocalRandom.current().nextLong(duration + 1);
	    }
	};
	return randomBackgroundEventsWithIter(randomInts, target, consequenceEvents, numEvents);
    }

    /**
     * @param target
     * @param consequenceEvents
     * @param duration
     * @return
     */
    public static List<BehaviorEvent> completeBackgroundEvents(KeyBehaviorMapping target,
	    List<BehaviorEvent> consequenceEvents, long duration) {
	long totalConsequencesMillis = 0;
	for (BehaviorEvent ce : consequenceEvents) {
	    totalConsequencesMillis += ce.endTime() - ce.startTime + 1;
	}
	long allowedMillis = (duration + 1) - totalConsequencesMillis;

	Iterator<Long> sequentialInts = new Iterator<Long>() {
	    private long c = 0;

	    @Override
	    public boolean hasNext() {
		return true;
	    }

	    @Override
	    public Long next() {
		return this.c++;
	    }
	};
	return randomBackgroundEventsWithIter(sequentialInts, target, consequenceEvents, allowedMillis);
    }

    public static List<BehaviorEvent> convertToDiscrete(List<BehaviorEvent> events) {
	List<BehaviorEvent> result = Lists.newArrayList();
	for (BehaviorEvent event : events) {
	    if (!event.isContinuous()) {
		result.add(event);
		continue;
	    }
	    for (long t = event.startTime; t <= event.endTime(); t += 1000) {
		result.add(new DiscreteBehavior( //
			event.uuid + "-" + t, //
			MappableChar.C, event.name, t));
	    }
	}
	return result;
    }
}
