package com.threebird.recorder.utils;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.threebird.recorder.models.behaviors.BehaviorEvent;

public class ConditionalProbability {

    public static Integer RANGE_5 = 5000;
    public static Integer RANGE_10 = 10000;
    public static Integer RANGE_15 = 15000;
    public static Integer RANGE_20 = 20000;

    public static class Results {
	public static Comparator<Results> compare = //
		(r1, r2) -> (int) (Math.floor(r1.avg * 1000) - Math.floor(r2.avg * 1000));

	public final Double binaryEO;
	public final Double binaryNonEO;
	public final Double proportionEO;
	public final Double proportionNonEO;
	public final Double avg;

	public Results(Double binaryEO, Double binaryNonEO, Double proportionEO, Double proportionNonEO) {
	    super();

	    this.binaryEO = binaryEO;
	    this.binaryNonEO = binaryNonEO;
	    this.proportionEO = proportionEO;
	    this.proportionNonEO = proportionNonEO;
	    this.avg = (binaryEO + binaryNonEO + proportionEO + proportionNonEO) / 4;
	}
    }

    public static Results results(List<BehaviorEvent> targetEvents, //
	    List<BehaviorEvent> consequentEvents, //
	    int range) {
	return new Results(//
		binaryEO(targetEvents, consequentEvents, range), //
		binaryNonEO(targetEvents, consequentEvents, range), //
		proportionEO(targetEvents, consequentEvents, range), //
		proportionNonEO(targetEvents, consequentEvents, range));
    }

    public static Double binaryNonEO( //
	    List<BehaviorEvent> targetEvents, //
	    List<BehaviorEvent> consequentEvents, //
	    int range) {
	double numTargets = targetEvents.size();
	double numPotentiallyReinforcedTargets = 0f;
	for (BehaviorEvent te : targetEvents) {
	    boolean hasConsequentEvents = hasConsequentEvents(consequentEvents, te, range);
	    boolean hasOverlappingEvents = hasOverlappingEvents(consequentEvents, te);
	    if (hasConsequentEvents || hasOverlappingEvents) {
		numPotentiallyReinforcedTargets++;
	    }
	}
	if (numTargets == 0) {
	    return -1.0;
	}
	return numPotentiallyReinforcedTargets / numTargets;
    }

    private static boolean hasOverlappingEvents(List<BehaviorEvent> candidates, BehaviorEvent target) {
	for (BehaviorEvent ce : candidates) {
	    if (ce.startTime < target.startTime && ce.endTime() > target.startTime) {
		return true;
	    }
	}
	return false;
    }

    private static boolean hasConsequentEvents(List<BehaviorEvent> candidates, BehaviorEvent target, int range) {
	int end = target.startTime + range;
	for (BehaviorEvent ce : candidates) {
	    if (ce.startTime > target.startTime && ce.startTime < end) {
		return true;
	    }
	}
	return false;
    }

    public static Double binaryEO( //
	    List<BehaviorEvent> targetEvents, //
	    List<BehaviorEvent> consequentEvents, //
	    int range) {
	int numTargets = 0;
	double numPotentiallyReinforcedTargets = 0.0;
	for (BehaviorEvent te : targetEvents) {
	    boolean hasOverlappingEvents = hasOverlappingEvents(consequentEvents, te);
	    if (hasOverlappingEvents) {
		continue;
	    }
	    numTargets++;
	    boolean hasConsequentEvents = hasConsequentEvents(consequentEvents, te, range);
	    if (hasConsequentEvents) {
		numPotentiallyReinforcedTargets++;
	    }
	}
	if (numTargets == 0) {
	    return -1.0;
	}
	return numPotentiallyReinforcedTargets / numTargets;
    }

    public static Double proportionNonEO(//
	    List<BehaviorEvent> targetEvents, //
	    List<BehaviorEvent> consequentEvents, //
	    int range) {
	double totalDurationOfReinforcingConsequences = 0.0;
	for (BehaviorEvent te : targetEvents) {
	    int rangeEnd = te.startTime + range;
	    List<BehaviorEvent> matchingEvents = findConsequentEvents(consequentEvents, te, range);
	    matchingEvents.addAll(findOverlappingEvents(consequentEvents, te));
	    for (BehaviorEvent ce : matchingEvents) {
		int consequentEnd = ce.endTime();
		if (consequentEnd > rangeEnd) {
		    consequentEnd = rangeEnd;
		}
		int startTime = Math.max(ce.startTime, te.startTime);
		int duration = consequentEnd - startTime;
		totalDurationOfReinforcingConsequences += duration;
	    }
	}
	int totalMillis = range * targetEvents.size();
	if (totalMillis == 0) {
	    return -1.0;
	}
	return totalDurationOfReinforcingConsequences / totalMillis;
    }

    private static List<BehaviorEvent> findOverlappingEvents(List<BehaviorEvent> candidates, BehaviorEvent target) {
	return candidates.stream() //
		.filter((ce) -> ce.startTime < target.startTime && ce.endTime() > target.startTime) //
		.collect(Collectors.toList());
    }

    private static List<BehaviorEvent> findConsequentEvents(List<BehaviorEvent> candidates, BehaviorEvent target,
	    int range) {
	int end = target.startTime + range;
	return candidates.stream() //
		.filter((ce) -> ce.startTime > target.startTime && ce.startTime < end) //
		.collect(Collectors.toList());
    }

    public static Double proportionEO(//
	    List<BehaviorEvent> targetEvents, //
	    List<BehaviorEvent> consequentEvents, //
	    int range) {
	int numTargets = 0;
	double totalDurationOfReinforcingConsequences = 0.0;
	for (BehaviorEvent te : targetEvents) {
	    boolean hasOverlappingEvents = hasOverlappingEvents(consequentEvents, te);
	    if (hasOverlappingEvents) {
		continue;
	    }
	    numTargets++;
	    int rangeEnd = te.startTime + range;
	    List<BehaviorEvent> matchingEvents = findConsequentEvents(consequentEvents, te, range);
	    for (BehaviorEvent ce : matchingEvents) {
		int consequentEnd = ce.endTime();
		if (consequentEnd > rangeEnd) {
		    consequentEnd = rangeEnd;
		}
		int startTime = Math.max(ce.startTime, te.startTime);
		int duration = consequentEnd - startTime;
		totalDurationOfReinforcingConsequences += duration;
	    }
	}
	int totalMillis = range * numTargets;
	if (totalMillis == 0) {
	    return -1.0;
	}
	return totalDurationOfReinforcingConsequences / totalMillis;
    }

}
