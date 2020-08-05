package com.threebird.recorder.utils;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.threebird.recorder.models.behaviors.BehaviorEvent;
import com.threebird.recorder.models.schemas.KeyBehaviorMapping;

public class ConditionalProbability {

    public static Integer RANGE_5 = 5000;
    public static Integer RANGE_10 = 10000;
    public static Integer RANGE_15 = 15000;
    public static Integer RANGE_20 = 20000;

    private static List<Integer> RANGES = Lists.newArrayList( //
	    ConditionalProbability.RANGE_5, //
	    ConditionalProbability.RANGE_10, //
	    ConditionalProbability.RANGE_15, //
	    ConditionalProbability.RANGE_20);

    public static Map<Integer, Double> binary(KeyBehaviorMapping target, KeyBehaviorMapping consequence,
	    List<BehaviorEvent> events, boolean establishingOperations) {
	return calculate( //
		target, //
		consequence, //
		events, //
		establishingOperations, //
		ConditionalProbability::calcBinary);
    }

    public static Map<Integer, Double> proportion(KeyBehaviorMapping target, KeyBehaviorMapping consequence,
	    List<BehaviorEvent> events, boolean establishingOperations) {
	return calculate( //
		target, //
		consequence, //
		events, //
		establishingOperations, //
		ConditionalProbability::calcProportion);
    }

    private static Map<Integer, Double> calculate( //
	    KeyBehaviorMapping target, //
	    KeyBehaviorMapping consequence, //
	    List<BehaviorEvent> events, //
	    boolean establishingOperations, //
	    BiFunction<List<BehaviorEvent>, List<BehaviorEvent>, Map<Integer, Double>> calc) {
	List<BehaviorEvent> consequentEvents = events.stream() //
		.filter((e) -> e.key == consequence.key) //
		.collect(Collectors.toList());
	List<BehaviorEvent> targetEvents = events.stream() //
		.filter((e) -> e.key == target.key) //
		.collect(Collectors.toList());

	if (establishingOperations) {
	    targetEvents = targetEvents.stream() //
		    .filter((e) -> !hasOverlappingEvents(consequentEvents, e)) //
		    .collect(Collectors.toList());
	}

	return calc.apply(consequentEvents, targetEvents);
    }

    private static Map<Integer, Double> calcBinary(List<BehaviorEvent> consequentEvents,
	    List<BehaviorEvent> targetEvents) {
	Map<Integer, Double> results = Maps.newHashMap();
	double numOccurrencesOfConsequenceFollowingTarget;
	for (Integer range : RANGES) {
	    numOccurrencesOfConsequenceFollowingTarget = 0f;
	    for (BehaviorEvent te : targetEvents) {
		if (hasConsequentEvents(consequentEvents, te, range)) {
		    numOccurrencesOfConsequenceFollowingTarget++;
		}
	    }
	    results.put(range, numOccurrencesOfConsequenceFollowingTarget / targetEvents.size());
	}
	return results;
    }

    private static Map<Integer, Double> calcProportion(List<BehaviorEvent> consequentEvents,
	    List<BehaviorEvent> targetEvents) {
	Map<Integer, Double> results = Maps.newHashMap();
	double totalDurationOfConsequenceFollowingTarget;
	for (Integer range : RANGES) {
	    totalDurationOfConsequenceFollowingTarget = 0f;
	    for (BehaviorEvent te : targetEvents) {
		int rangeEnd = te.startTime + range;
		List<BehaviorEvent> matchingEvents = findConsequentEvents(consequentEvents, te, range);
		for (BehaviorEvent ce : matchingEvents) {
		    int consequentEnd = ce.endTime();
		    if (consequentEnd > rangeEnd) {
			consequentEnd = rangeEnd;
		    }
		    int duration = consequentEnd - ce.startTime;
		    totalDurationOfConsequenceFollowingTarget += duration;
		}
	    }
	    int totalMillis = range * targetEvents.size();
	    double p = totalDurationOfConsequenceFollowingTarget / totalMillis;
	    results.put(range, p);
	}
	return results;
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

    private static List<BehaviorEvent> findConsequentEvents(List<BehaviorEvent> candidates, BehaviorEvent target,
	    int range) {
	int end = target.startTime + range;
	return candidates.stream() //
		.filter((ce) -> ce.startTime > target.startTime && ce.startTime < end) //
		.collect(Collectors.toList());
    }
}
