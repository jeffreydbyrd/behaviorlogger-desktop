package com.threebird.recorder.utils.condprob;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.threebird.recorder.models.MappableChar;
import com.threebird.recorder.models.behaviors.BehaviorEvent;
import com.threebird.recorder.models.behaviors.ContinuousBehavior;
import com.threebird.recorder.models.behaviors.DiscreteBehavior;
import com.threebird.recorder.models.schemas.KeyBehaviorMapping;
import com.threebird.recorder.utils.ConditionalProbability;

public class ConditionalProbabilityTest {
    @Test
    public void basic() {
	KeyBehaviorMapping target = new KeyBehaviorMapping("target", "t", "target", false, false);
	KeyBehaviorMapping consequence = new KeyBehaviorMapping("consequence", "c", "consequence", true, false);

	List<BehaviorEvent> events = Lists.newArrayList();
	events.add(new DiscreteBehavior("target", MappableChar.T, "target", 0));
	events.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 4000, 2000));
	events.add(new DiscreteBehavior("target", MappableChar.T, "target", 10000));
	events.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 17000, 1000));

	Map<Integer, Double> results = ConditionalProbability.binary(target, consequence, events, false);
	Assert.assertEquals(1f / 2, results.get(ConditionalProbability.RANGE_5), 0.0001);
	Assert.assertEquals(1f, results.get(ConditionalProbability.RANGE_10), 0.0001);
	Assert.assertEquals(1f, results.get(ConditionalProbability.RANGE_15), 0.0001);
	Assert.assertEquals(1f, results.get(ConditionalProbability.RANGE_20), 0.0001);

	results = ConditionalProbability.proportion(target, consequence, events, false);
	Assert.assertEquals(1f / 10, results.get(ConditionalProbability.RANGE_5), 0.00001);
	Assert.assertEquals(3f / 20, results.get(ConditionalProbability.RANGE_10), 0.00001);
	Assert.assertEquals(3f / 30, results.get(ConditionalProbability.RANGE_15), 0.00001);
	Assert.assertEquals(4f / 40, results.get(ConditionalProbability.RANGE_20), 0.00001);
    }

    @Test
    public void stPeterExample_Attention() {
	KeyBehaviorMapping target = new KeyBehaviorMapping(//
		"target", //
		"t", "disruptive behavior", //
		false, false);
	KeyBehaviorMapping consequence = new KeyBehaviorMapping(//
		"consequence", //
		"c", "attention", //
		true, false);

	List<BehaviorEvent> events = Lists.newArrayList();
	events.add(new DiscreteBehavior("target", MappableChar.T, "target", 10000));
	events.add(new DiscreteBehavior("target", MappableChar.T, "target", 24000));
	events.add(new DiscreteBehavior("target", MappableChar.T, "target", 25000));
	events.add(new DiscreteBehavior("target", MappableChar.T, "target", 29000));
	events.add(new DiscreteBehavior("target", MappableChar.T, "target", 30000));
	events.add(new DiscreteBehavior("target", MappableChar.T, "target", 53000));
	events.add(new DiscreteBehavior("target", MappableChar.T, "target", 66000));
	events.add(new DiscreteBehavior("target", MappableChar.T, "target", 68000));
	events.add(new DiscreteBehavior("target", MappableChar.T, "target", 81000));
	events.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 11000, 1000));
	events.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 17000, 999));
	events.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 21000, 1000));
	events.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 25001, 3000));
	events.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 32000, 999));
	events.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 34000, 5000));
	events.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 40000, 1000));
	events.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 42000, 1000));
	events.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 45000, 2000));
	events.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 50000, 2000));
	events.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 54000, 1000));
	events.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 57000, 3000));
	events.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 64000, 3000));
	events.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 69000, 1000));
	events.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 72000, 1000));
	events.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 81001, 2000));

	Map<Integer, Double> results = ConditionalProbability.binary(target, consequence, events, false);
	Assert.assertEquals(1f, results.get(ConditionalProbability.RANGE_5), 0.0001);

	results = ConditionalProbability.proportion(target, consequence, events, false);
	Assert.assertEquals(16.998f / 45f, results.get(ConditionalProbability.RANGE_5), 0.0001);
    }

    @Test
    public void stPeterExample_Tangible() {
	KeyBehaviorMapping target = new KeyBehaviorMapping(//
		"target", //
		"t", "disruptive behavior", //
		false, false);
	KeyBehaviorMapping consequence = new KeyBehaviorMapping(//
		"consequence", //
		"c", "attention", //
		true, false);

	List<BehaviorEvent> events = Lists.newArrayList();
	events.add(new DiscreteBehavior("target", MappableChar.T, "target", 10000));
	events.add(new DiscreteBehavior("target", MappableChar.T, "target", 24000));
	events.add(new DiscreteBehavior("target", MappableChar.T, "target", 25000));
	events.add(new DiscreteBehavior("target", MappableChar.T, "target", 29000));
	events.add(new DiscreteBehavior("target", MappableChar.T, "target", 30000));
	events.add(new DiscreteBehavior("target", MappableChar.T, "target", 53000));
	events.add(new DiscreteBehavior("target", MappableChar.T, "target", 66000));
	events.add(new DiscreteBehavior("target", MappableChar.T, "target", 68000));
	events.add(new DiscreteBehavior("target", MappableChar.T, "target", 81000));
	events.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 7000, 7000));
	events.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 89000, 7000));

	Map<Integer, Double> results = ConditionalProbability.binary(target, consequence, events, false);
	Assert.assertEquals(0f, results.get(ConditionalProbability.RANGE_5), 0.0001);
	Assert.assertEquals(1f / 9f, results.get(ConditionalProbability.RANGE_10), 0.0001);

	results = ConditionalProbability.proportion(target, consequence, events, false);
	Assert.assertEquals(0f, results.get(ConditionalProbability.RANGE_5), 0.0001);
	Assert.assertEquals(2f / 90f, results.get(ConditionalProbability.RANGE_10), 0.0001);
    }

    @Test
    public void establishingOperationsIgnoreOverlapping() {
	KeyBehaviorMapping target = new KeyBehaviorMapping("target", "t", "target", false, false);
	KeyBehaviorMapping consequence = new KeyBehaviorMapping("consequence", "c", "consequence", true, false);

	List<BehaviorEvent> events = Lists.newArrayList();
	events.add(new DiscreteBehavior("target", MappableChar.T, "target", 5000));
	events.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 4000, 2000));
	events.add(new DiscreteBehavior("target", MappableChar.T, "target", 10000));
	events.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 17000, 1000));

	Map<Integer, Double> results = ConditionalProbability.binary(target, consequence, events, true);
	Assert.assertEquals(0.0f, results.get(ConditionalProbability.RANGE_5), 0.0001);
	Assert.assertEquals(1f, results.get(ConditionalProbability.RANGE_10), 0.0001);
	Assert.assertEquals(1f, results.get(ConditionalProbability.RANGE_15), 0.0001);
	Assert.assertEquals(1f, results.get(ConditionalProbability.RANGE_20), 0.0001);

	results = ConditionalProbability.proportion(target, consequence, events, true);
	Assert.assertEquals(0.0f, results.get(ConditionalProbability.RANGE_5), 0.0001);
	Assert.assertEquals(1f / 10, results.get(ConditionalProbability.RANGE_10), 0.0001);
	Assert.assertEquals(1f / 15, results.get(ConditionalProbability.RANGE_15), 0.0001);
	Assert.assertEquals(1f / 20, results.get(ConditionalProbability.RANGE_20), 0.0001);
    }

}
