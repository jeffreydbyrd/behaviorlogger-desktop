package com.threebird.recorder.utils.condprob;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.threebird.recorder.models.MappableChar;
import com.threebird.recorder.models.behaviors.BehaviorEvent;
import com.threebird.recorder.models.behaviors.ContinuousBehavior;
import com.threebird.recorder.models.behaviors.DiscreteBehavior;
import com.threebird.recorder.utils.ConditionalProbability;

public class ConditionalProbabilityTest {
    @Test
    public void binaryNonEO() {
	List<BehaviorEvent> targetEvents = Lists.newArrayList();
	targetEvents.add(new DiscreteBehavior("target", MappableChar.T, "target", 4000));
	targetEvents.add(new DiscreteBehavior("target", MappableChar.T, "target", 10000));

	List<BehaviorEvent> consequentEvents = Lists.newArrayList();
	consequentEvents.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 3000, 2000));
	consequentEvents.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 17000, 1000));

	Double result = ConditionalProbability.binaryNonEO(targetEvents, consequentEvents,
		ConditionalProbability.RANGE_5);
	Assert.assertEquals(1f / 2, result, 0.0001);
	result = ConditionalProbability.binaryNonEO(targetEvents, consequentEvents, ConditionalProbability.RANGE_10);
	Assert.assertEquals(1f, result, 0.0001);
    }

    @Test
    public void binaryEO() throws Exception {
	List<BehaviorEvent> targetEvents = Lists.newArrayList();
	targetEvents.add(new DiscreteBehavior("target", MappableChar.T, "target", 4000));
	targetEvents.add(new DiscreteBehavior("target", MappableChar.T, "target", 10000));
	List<BehaviorEvent> consequentEvents = Lists.newArrayList();
	consequentEvents.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 3000, 2000));
	consequentEvents.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 17000, 1000));

	Double result = ConditionalProbability.binaryEO(targetEvents, consequentEvents, ConditionalProbability.RANGE_5);
	Assert.assertEquals(0f, result, 0.0001);
	result = ConditionalProbability.binaryEO(targetEvents, consequentEvents, ConditionalProbability.RANGE_10);
	Assert.assertEquals(1f / 1, result, 0.0001);
    }

    @Test
    public void proportionNonEO() throws Exception {
	List<BehaviorEvent> targetEvents = Lists.newArrayList();
	targetEvents.add(new DiscreteBehavior("target", MappableChar.T, "target", 4000));
	targetEvents.add(new DiscreteBehavior("target", MappableChar.T, "target", 10000));

	List<BehaviorEvent> consequentEvents = Lists.newArrayList();
	consequentEvents.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 3000, 2000));
	consequentEvents.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 17000, 1000));

	Double result = ConditionalProbability.proportionNonEO(targetEvents, consequentEvents,
		ConditionalProbability.RANGE_5);
	Assert.assertEquals(1f / 10, result, 0.0001);
	result = ConditionalProbability.proportionNonEO(targetEvents, consequentEvents,
		ConditionalProbability.RANGE_10);
	Assert.assertEquals(2f / 20, result, 0.0001);
	result = ConditionalProbability.proportionNonEO(targetEvents, consequentEvents,
		ConditionalProbability.RANGE_20);
	Assert.assertEquals(3f / 40, result, 0.0001);
    }

    @Test
    public void proportionEO() throws Exception {
	List<BehaviorEvent> targetEvents = Lists.newArrayList();
	targetEvents.add(new DiscreteBehavior("target", MappableChar.T, "target", 4000));
	targetEvents.add(new DiscreteBehavior("target", MappableChar.T, "target", 10000));

	List<BehaviorEvent> consequentEvents = Lists.newArrayList();
	consequentEvents.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 3000, 2000));
	consequentEvents.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 17000, 1000));

	Double result = ConditionalProbability.proportionEO(targetEvents, consequentEvents,
		ConditionalProbability.RANGE_5);
	Assert.assertEquals(0f / 5, result, 0.0001);
	result = ConditionalProbability.proportionEO(targetEvents, consequentEvents, ConditionalProbability.RANGE_10);
	Assert.assertEquals(1f / 10, result, 0.0001);
	result = ConditionalProbability.proportionEO(targetEvents, consequentEvents, ConditionalProbability.RANGE_20);
	Assert.assertEquals(1f / 20, result, 0.0001);
    }

    @Test
    public void stPeterExample_Attention() {
	List<BehaviorEvent> targetEvents = Lists.newArrayList();
	List<BehaviorEvent> consequentEvents = Lists.newArrayList();
	targetEvents.add(new DiscreteBehavior("target", MappableChar.T, "target", 10000)); // 1000 - 1000
	targetEvents.add(new DiscreteBehavior("target", MappableChar.T, "target", 24000)); // 3000 - 4000
	targetEvents.add(new DiscreteBehavior("target", MappableChar.T, "target", 25000)); // 3000 - 7000
	targetEvents.add(new DiscreteBehavior("target", MappableChar.T, "target", 29000)); // 1000 - 8000
	targetEvents.add(new DiscreteBehavior("target", MappableChar.T, "target", 30000)); // 2000 - 10000
	targetEvents.add(new DiscreteBehavior("target", MappableChar.T, "target", 53000)); // 2000 - 12000
	targetEvents.add(new DiscreteBehavior("target", MappableChar.T, "target", 66000)); // 2000 - 14000
	targetEvents.add(new DiscreteBehavior("target", MappableChar.T, "target", 68000)); // 2000 - 16000
	targetEvents.add(new DiscreteBehavior("target", MappableChar.T, "target", 71000)); // 1000 - 17000
	targetEvents.add(new DiscreteBehavior("target", MappableChar.T, "target", 81000)); // 2000 - 19000
	consequentEvents.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 11000, 1000));
	consequentEvents.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 17000, 1000));
	consequentEvents.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 21000, 1000));
	consequentEvents.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 25001, 3000));
	consequentEvents.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 32000, 1000));
	consequentEvents.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 34000, 5000));
	consequentEvents.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 40000, 1000));
	consequentEvents.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 42000, 1000));
	consequentEvents.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 45000, 2000));
	consequentEvents.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 50000, 2000));
	consequentEvents.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 54000, 1000));
	consequentEvents.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 57000, 3000));
	consequentEvents.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 64000, 3000));
	consequentEvents.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 69000, 1000));
	consequentEvents.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 72000, 1000));
	consequentEvents.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 81001, 2000));

	// non-EO
	Double result1 = ConditionalProbability.binaryNonEO(targetEvents, consequentEvents,
		ConditionalProbability.RANGE_5);
	Assert.assertEquals(10f / 10, result1, 0.0001);
	Double result2 = ConditionalProbability.proportionNonEO(targetEvents, consequentEvents,
		ConditionalProbability.RANGE_5);
	Assert.assertEquals(19f / 50f, result2, 0.0001);

	// EO
	result1 = ConditionalProbability.binaryEO(targetEvents, consequentEvents, ConditionalProbability.RANGE_5);
	Assert.assertEquals(9f / 9, result1, 0.0001);
	result1 = ConditionalProbability.proportionEO(targetEvents, consequentEvents, ConditionalProbability.RANGE_5);
	Assert.assertEquals(17f / 45, result1, 0.0001);
    }

    @Test
    public void stPeterExample_Tangible() {
	List<BehaviorEvent> targetEvents = Lists.newArrayList();
	List<BehaviorEvent> consequentEvents = Lists.newArrayList();
	targetEvents.add(new DiscreteBehavior("target", MappableChar.T, "target", 10000));
	targetEvents.add(new DiscreteBehavior("target", MappableChar.T, "target", 24000));
	targetEvents.add(new DiscreteBehavior("target", MappableChar.T, "target", 25000));
	targetEvents.add(new DiscreteBehavior("target", MappableChar.T, "target", 29000));
	targetEvents.add(new DiscreteBehavior("target", MappableChar.T, "target", 30000));
	targetEvents.add(new DiscreteBehavior("target", MappableChar.T, "target", 53000));
	targetEvents.add(new DiscreteBehavior("target", MappableChar.T, "target", 66000));
	targetEvents.add(new DiscreteBehavior("target", MappableChar.T, "target", 71000));
	targetEvents.add(new DiscreteBehavior("target", MappableChar.T, "target", 68000));
	targetEvents.add(new DiscreteBehavior("target", MappableChar.T, "target", 81000));
	consequentEvents.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 7000, 7000));
	consequentEvents.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 89000, 7000));

	// non-EO
	Double result1 = ConditionalProbability.binaryNonEO(targetEvents, consequentEvents,
		ConditionalProbability.RANGE_5);
	Assert.assertEquals(1f / 10, result1, 0.0001);
	Double result2 = ConditionalProbability.proportionNonEO(targetEvents, consequentEvents,
		ConditionalProbability.RANGE_5);
	Assert.assertEquals(4f / 50, result2, 0.0001);
	Double result3 = ConditionalProbability.proportionNonEO(targetEvents, consequentEvents,
		ConditionalProbability.RANGE_10);
	Assert.assertEquals(6f / 100, result3, 0.0001);

	// EO
	result1 = ConditionalProbability.binaryEO(targetEvents, consequentEvents, ConditionalProbability.RANGE_5);
	Assert.assertEquals(0f / 9, result1, 0.0001);
	result2 = ConditionalProbability.proportionEO(targetEvents, consequentEvents, ConditionalProbability.RANGE_5);
	Assert.assertEquals(0f / 45, result2, 0.0001);
	result3 = ConditionalProbability.proportionEO(targetEvents, consequentEvents, ConditionalProbability.RANGE_10);
	Assert.assertEquals(2f / 90, result3, 0.0001);
    }

    @Test
    public void emptyTargets() {
	List<BehaviorEvent> targetEvents = Lists.newArrayList();
	List<BehaviorEvent> consequentEvents = Lists.newArrayList();
	consequentEvents.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 3000, 2000));
	consequentEvents.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 17000, 1000));

	Double result = ConditionalProbability.binaryNonEO(targetEvents, consequentEvents,
		ConditionalProbability.RANGE_5);
	Assert.assertEquals(-1, result, 0.0001);

	result = ConditionalProbability.binaryEO(targetEvents, consequentEvents, ConditionalProbability.RANGE_5);
	Assert.assertEquals(-1, result, 0.0001);

	result = ConditionalProbability.proportionNonEO(targetEvents, consequentEvents, ConditionalProbability.RANGE_5);
	Assert.assertEquals(-1, result, 0.0001);

	result = ConditionalProbability.proportionEO(targetEvents, consequentEvents, ConditionalProbability.RANGE_5);
	Assert.assertEquals(-1, result, 0.0001);
    }

    @Test
    public void emptyConsequences() {
	List<BehaviorEvent> targetEvents = Lists.newArrayList();
	targetEvents.add(new DiscreteBehavior("target", MappableChar.T, "target", 4000));
	targetEvents.add(new DiscreteBehavior("target", MappableChar.T, "target", 10000));

	List<BehaviorEvent> consequentEvents = Lists.newArrayList();

	Double result = ConditionalProbability.binaryNonEO(targetEvents, consequentEvents,
		ConditionalProbability.RANGE_5);
	Assert.assertEquals(0, result, 0.0001);

	result = ConditionalProbability.binaryEO(targetEvents, consequentEvents, ConditionalProbability.RANGE_5);
	Assert.assertEquals(0, result, 0.0001);

	result = ConditionalProbability.proportionNonEO(targetEvents, consequentEvents, ConditionalProbability.RANGE_5);
	Assert.assertEquals(0, result, 0.0001);

	result = ConditionalProbability.proportionEO(targetEvents, consequentEvents, ConditionalProbability.RANGE_5);
	Assert.assertEquals(0, result, 0.0001);
    }

}
