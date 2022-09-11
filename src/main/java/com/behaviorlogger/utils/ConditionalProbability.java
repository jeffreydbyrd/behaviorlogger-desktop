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

	public static Comparator<Results> compare = //
		(r1, r2) -> (int) (Math.floor(r1.probability * 1000) - Math.floor(r2.probability * 1000));

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

    /**
     * Perform Binary Non-EO calculation. The result is a proportion of number of
     * potentially reinforced targets over the total number of targets. Targets are
     * considered reinforced if a consequence occurred at any point during the
     * window following a target.
     */
    public static Results binaryNonEO(List<DiscreteBehavior> targetEvents, List<ContinuousBehavior> consequentEvents,
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

    /**
     * Returns true if any of the candidates' start and end times overlap with the
     * targetTime.
     */
    private static boolean hasOverlappingEvents(List<ContinuousBehavior> candidates, long targetTime) {
	for (BehaviorEvent ce : candidates) {
	    if (ce.startTime <= targetTime && ce.endTime() >= targetTime) {
		return true;
	    }
	}
	return false;
    }

    private static boolean hasConsequentEvents(List<ContinuousBehavior> candidates, BehaviorEvent target,
	    long windowMillis) {
	long end = target.startTime + windowMillis;
	for (BehaviorEvent ce : candidates) {
	    if (ce.startTime > target.startTime && ce.startTime < end) {
		return true;
	    }
	}
	return false;
    }

    /**
     * Perform Binary EO calculation. The result is a proportion of number of
     * potentially reinforced targets over the total number of targets that did not
     * occur while a consequence was active. Targets are considered reinforced if a
     * consequence occurred at any point during the window following a target and no
     * consequences were overlapping with the target.
     */
    public static Results binaryEO( //
	    List<DiscreteBehavior> targetEvents, //
	    List<ContinuousBehavior> consequentEvents, //
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

    /**
     * Same as binaryNonEO except the proportion is equal to the total duration of
     * reinforcing consequences over the total aggregate of time windows.
     */
    public static Results proportionNonEO(//
	    List<DiscreteBehavior> targetEvents, //
	    List<ContinuousBehavior> consequentEvents, //
	    int windowMillis) {
	double totalDurationOfReinforcingConsequences = 0.0;
	for (BehaviorEvent te : targetEvents) {
	    long windowEnd = te.startTime + windowMillis;
	    List<ContinuousBehavior> matchingEvents = findConsequentEvents(consequentEvents, te, windowMillis);
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

    private static <B extends BehaviorEvent> List<B> findOverlappingEvents(List<B> candidates, BehaviorEvent target) {
	List<B> overlappingEvents = candidates.stream() //
		.filter((ce) -> ce.startTime < target.startTime && ce.endTime() > target.startTime) //
		.collect(Collectors.toList());
	return overlappingEvents;
    }

    private static <B extends BehaviorEvent> List<B> findConsequentEvents(List<B> candidates, BehaviorEvent target,
	    long windowMillis) {
	double windowEnd = target.startTime + windowMillis;
	return candidates.stream() //
		.filter((ce) -> ce.startTime > target.startTime && ce.startTime < windowEnd) //
		.collect(Collectors.toList());
    }

    /**
     * Same as proportionEO except targets with overlapping consequences are
     * discounted.
     */
    public static Results proportionEO( //
	    List<DiscreteBehavior> targetEvents, //
	    List<ContinuousBehavior> consequentEvents, //
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
	    List<ContinuousBehavior> matchingConsequentEvents = findConsequentEvents(consequentEvents, te,
		    windowMillis);
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

    public static List<DiscreteBehavior> getTargetEvents( //
	    KeyBehaviorMapping targetBehavior, //
	    List<DiscreteEvent> discreteEvents, //
	    List<ContinuousEvent> continuousEvents) {
	if (targetBehavior.isContinuous) {
	    List<BehaviorEvent> targetEvents = continuousEvents.stream() //
		    .filter((e) -> e.behaviorUuid.equals(targetBehavior.uuid)) //
		    .map((e) -> new ContinuousBehavior(targetBehavior.uuid, targetBehavior.key,
			    targetBehavior.description, e.startTime, (int) (e.endTime - e.startTime))) //
		    .collect(Collectors.toList());
	    return convertToDiscrete(targetEvents);
	}
	return discreteEvents.stream() //
		.filter((e) -> e.behaviorUuid.equals(targetBehavior.uuid)) //
		.map((e) -> {
		    return new DiscreteBehavior(targetBehavior.uuid, targetBehavior.key, targetBehavior.description,
			    e.time);
		}).collect(Collectors.toList());
    }

    /**
     * Finds the events that match the {@link KeyBehaviorMapping}, converts them to
     * {@link ContinuousBehavior}.
     */
    public static List<ContinuousBehavior> getConsequenceEvents(KeyBehaviorMapping kbm,
	    List<DiscreteEvent> discreteEvents, List<ContinuousEvent> continuousEvents) {
	List<ContinuousBehavior> consequenceEvents = Lists.newArrayList();
	if (!kbm.isContinuous) {
	    for (DiscreteEvent evt : discreteEvents) {
		if (evt.behaviorUuid.equals(kbm.uuid)) {
		    ContinuousBehavior continuousBehavior = new ContinuousBehavior(kbm.uuid, kbm.key, kbm.description,
			    evt.time, 1000);
		    consequenceEvents.add(continuousBehavior);
		}
	    }
	    return consequenceEvents;
	}
	for (ContinuousEvent evt : continuousEvents) {
	    if (evt.behaviorUuid.equals(kbm.uuid)) {
		long duration = evt.endTime - evt.startTime;
		ContinuousBehavior continuousBehavior = new ContinuousBehavior(kbm.uuid, kbm.key, kbm.description,
			evt.startTime, (int) duration);
		consequenceEvents.add(continuousBehavior);
	    }
	}
	return consequenceEvents;
    }

    public static List<DiscreteBehavior> randomBackgroundEventsWithIter(Iterator<Long> randomInts,
	    KeyBehaviorMapping kbm, List<ContinuousBehavior> consequenceEvents, long numEvents) {
	Set<Long> startTimes = new HashSet<>();
	for (int i = 0; i < numEvents; i++) {
	    Long next = randomInts.next();
	    while (startTimes.contains(next) || hasOverlappingEvents(consequenceEvents, next)) {
		next = randomInts.next();
	    }
	    startTimes.add(next);
	}
	List<DiscreteBehavior> result = Lists.newArrayList();
	for (Long startTime : startTimes) {
	    DiscreteBehavior evt = new DiscreteBehavior(kbm.uuid, kbm.key, kbm.description, startTime);
	    result.add(evt);
	}
	return result;
    }

    /**
     * Generates a number of target events (equal to numEvents) at random times in
     * which avoidedConsequenceEvents are not active. If numEvents is higher than
     * the number of possible times, TooManyBackgroundEventsException is thrown.
     */
    public static List<DiscreteBehavior> randomBackgroundEvents(KeyBehaviorMapping target,
	    List<ContinuousBehavior> avoidedConsequenceEvents, long duration, long numEvents)
	    throws TooManyBackgroundEventsException {

	long totalConsequencesMillis = 0;
	for (BehaviorEvent ce : avoidedConsequenceEvents) {
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
	return randomBackgroundEventsWithIter(randomInts, target, avoidedConsequenceEvents, numEvents);
    }

    /**
     * Generates a target event for each millisecond in which the
     * avoidedConsequenceEvents are not active
     */
    public static List<DiscreteBehavior> completeBackgroundEvents(KeyBehaviorMapping target,
	    List<ContinuousBehavior> avoidedConsequenceEvents, long duration) {
	long totalConsequencesMillis = 0;
	for (BehaviorEvent ce : avoidedConsequenceEvents) {
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
	return randomBackgroundEventsWithIter(sequentialInts, target, avoidedConsequenceEvents, allowedMillis);
    }

    public static List<DiscreteBehavior> convertToDiscrete(List<BehaviorEvent> events) {
	List<DiscreteBehavior> result = Lists.newArrayList();
	for (BehaviorEvent event : events) {
	    if (!event.isContinuous()) {
		result.add((DiscreteBehavior) event);
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

//    public static List<ContinuousBehavior> convertToContinuous(List<BehaviorEvent> events) {
//	List<ContinuousBehavior> result = Lists.newArrayList();
//	for (BehaviorEvent event : events) {
//	    if (event.isContinuous()) {
//		result.add((ContinuousBehavior) event);
//		continue;
//	    }
//	    result.add(new ContinuousBehavior(event.uuid, event.key, event.name, event.startTime, 1000));
//	}
//	return result;
//    }
}
