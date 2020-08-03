package com.threebird.recorder.utils;

import java.util.List;
import java.util.Map;
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
	    List<BehaviorEvent> events) {
	List<Integer> targetIntervals = events.stream() //
		.filter((e) -> e.key == target.key) //
		.map((e) -> e.startTime) //
		.collect(Collectors.toList());
	List<Integer> consequentIntervals = events.stream() //
		.filter((e) -> e.key == consequence.key) //
		.map((e) -> e.startTime) //
		.collect(Collectors.toList());

	Map<Integer, Double> results = Maps.newHashMap();
	double numOccurrencesOfConsequenceFollowingTarget;
	for (Integer range : RANGES) {
	    numOccurrencesOfConsequenceFollowingTarget = 0f;
	    for (Integer targetInterval : targetIntervals) {
		for (Integer consequentInterval : consequentIntervals) {
		    int end = (targetInterval) + range;
		    if (consequentInterval > targetInterval && consequentInterval < end) {
			numOccurrencesOfConsequenceFollowingTarget++;
			break;
		    }
		}
	    }
	    results.put(range, numOccurrencesOfConsequenceFollowingTarget / targetIntervals.size());
	}
	return results;
    }

    public static Map<Integer, Double> proportion(KeyBehaviorMapping target, KeyBehaviorMapping consequence,
	    List<BehaviorEvent> events) {
	List<Integer> targets = events.stream() //
		.filter((e) -> e.key == target.key) //
		.flatMap((e) -> e.intervals(1).stream()) //
		.collect(Collectors.toList());
	List<Integer> consequences = events.stream() //
		.filter((e) -> e.key == consequence.key) //
		.flatMap((e) -> e.intervals(1).stream()) //
		.collect(Collectors.toList());

	Map<Integer, Double> results = Maps.newHashMap();
	float numOccurrencesOfConsequenceFollowingTarget;
	for (Integer range : RANGES) {
	    numOccurrencesOfConsequenceFollowingTarget = 0f;
	    for (Integer targetSecond : targets) {
		for (Integer consequentSecond : consequences) {
		    int end = targetSecond + range;
		    if (consequentSecond > targetSecond && consequentSecond < end) {
			numOccurrencesOfConsequenceFollowingTarget++;
		    }
		}
	    }
	    int totalMillis = range * targets.size();
	    double p = numOccurrencesOfConsequenceFollowingTarget / totalMillis;
	    results.put(range, p);
	}
	return results;
    }
}
