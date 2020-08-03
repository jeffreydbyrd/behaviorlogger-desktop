package com.threebird.recorder.utils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.threebird.recorder.models.behaviors.BehaviorEvent;
import com.threebird.recorder.models.schemas.KeyBehaviorMapping;

public class ConditionalProbability {

    private static List<Integer> RANGES = Lists.newArrayList(5, 10, 15, 20);

    public static Map<Integer, Float> binary(KeyBehaviorMapping target, KeyBehaviorMapping consequence,
	    List<BehaviorEvent> events) {
	List<Integer> targetIntervals = events.stream() //
		.filter((e) -> e.key == target.key) //
		.map((e) -> e.startTime / 1000) //
		.collect(Collectors.toList());
	List<Integer> consequentIntervals = events.stream() //
		.filter((e) -> e.key == consequence.key) //
		.map((e) -> e.startTime / 1000) //
		.collect(Collectors.toList());

	Map<Integer, Float> results = Maps.newHashMap();
	float numOccurrencesOfConsequenceFollowingTarget;
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

    public static Map<Integer, Float> proportion(KeyBehaviorMapping target, KeyBehaviorMapping consequence,
	    List<BehaviorEvent> events) {
	List<Integer> targetSeconds = events.stream() //
		.filter((e) -> e.key == target.key) //
		.flatMap((e) -> e.intervals(1000).stream()) //
		.collect(Collectors.toList());
	List<Integer> consequentSeconds = events.stream() //
		.filter((e) -> e.key == consequence.key) //
		.flatMap((e) -> e.intervals(1000).stream()) //
		.collect(Collectors.toList());

	Map<Integer, Float> results = Maps.newHashMap();
	float numOccurrencesOfConsequenceFollowingTarget;
	for (Integer range : RANGES) {
	    numOccurrencesOfConsequenceFollowingTarget = 0f;
	    for (Integer targetSecond : targetSeconds) {
		for (Integer consequentSecond : consequentSeconds) {
		    int end = targetSecond + range;
		    if (consequentSecond > targetSecond && consequentSecond < end) {
			numOccurrencesOfConsequenceFollowingTarget++;
		    }
		}
	    }
	    float totalSeconds = range * targetSeconds.size();
	    float p = numOccurrencesOfConsequenceFollowingTarget / totalSeconds;
	    results.put(range, p);
	}
	return results;
    }
}
