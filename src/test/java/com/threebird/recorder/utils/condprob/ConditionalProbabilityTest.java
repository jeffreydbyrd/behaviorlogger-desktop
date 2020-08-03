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
    public void binary() {
	KeyBehaviorMapping target = new KeyBehaviorMapping("target", "t", "target", false, false);
	KeyBehaviorMapping consequence = new KeyBehaviorMapping("consequence", "c", "consequence", true, false);

	List<BehaviorEvent> events = Lists.newArrayList();
	events.add(new DiscreteBehavior("target", MappableChar.T, "target", 0));
	events.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 4000, 6000));
	events.add(new DiscreteBehavior("target", MappableChar.T, "target", 10000));
	events.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 17000, 18000));

	Map<Integer, Float> results = ConditionalProbability.binary(target, consequence, events);
	Assert.assertEquals(0.5f, results.get(5), 0.00001);
	Assert.assertEquals(1f, results.get(10), 0.00001);
	Assert.assertEquals(1f, results.get(15), 0.00001);
	Assert.assertEquals(1f, results.get(20), 0.00001);
    }

    @Test
    public void proportion() {
	KeyBehaviorMapping target = new KeyBehaviorMapping("target", "t", "target", false, false);
	KeyBehaviorMapping consequence = new KeyBehaviorMapping("consequence", "c", "consequence", true, false);

	List<BehaviorEvent> events = Lists.newArrayList();
	events.add(new DiscreteBehavior("target", MappableChar.T, "target", 0));
	events.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 4000, 2000));
	events.add(new DiscreteBehavior("target", MappableChar.T, "target", 10000));
	events.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 17000, 1000));

	Map<Integer, Float> results = ConditionalProbability.proportion(target, consequence, events);

	Assert.assertEquals(0.1f, results.get(5), 0.00001);
	Assert.assertEquals(0.25f, results.get(10), 0.00001);
    }
}
