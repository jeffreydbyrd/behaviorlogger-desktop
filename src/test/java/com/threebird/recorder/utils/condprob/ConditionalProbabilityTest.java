package com.threebird.recorder.utils.condprob;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.threebird.recorder.models.MappableChar;
import com.threebird.recorder.models.behaviors.BehaviorEvent;
import com.threebird.recorder.models.behaviors.ContinuousBehavior;
import com.threebird.recorder.models.behaviors.DiscreteBehavior;
import com.threebird.recorder.models.schemas.KeyBehaviorMapping;
import com.threebird.recorder.persistence.recordings.RecordingRawJson1_1.ContinuousEvent;
import com.threebird.recorder.persistence.recordings.RecordingRawJson1_1.DiscreteEvent;
import com.threebird.recorder.utils.ConditionalProbability;
import com.threebird.recorder.utils.ConditionalProbability.Results;
import com.threebird.recorder.utils.ConditionalProbability.TooManyBackgroundEventsException;

public class ConditionalProbabilityTest {

    // Filtering Event Streams

    @Test
    public void getTargets() {
	List<DiscreteEvent> discreteEvents = new ArrayList<>();
	KeyBehaviorMapping target = new KeyBehaviorMapping("b1", MappableChar.B, "", false, false);
	discreteEvents.add(new DiscreteEvent("b1", 0));
	discreteEvents.add(new DiscreteEvent("b2", 0));
	discreteEvents.add(new DiscreteEvent("b1", 1));
	List<BehaviorEvent> targetEvents = ConditionalProbability.getTargetEvents(target, discreteEvents);
	List<BehaviorEvent> expected = new ArrayList<>();
	expected.add(new DiscreteBehavior("b1", MappableChar.B, "", 0));
	expected.add(new DiscreteBehavior("b1", MappableChar.B, "", 1));
	Assert.assertTrue(targetEvents.size() == 2);
	Assert.assertTrue(targetEvents.get(0).uuid.equals("b1"));
	Assert.assertTrue(targetEvents.get(0).startTime == 0);
	Assert.assertTrue(targetEvents.get(1).uuid.equals("b1"));
	Assert.assertTrue(targetEvents.get(1).startTime == 1);
    }

    @Test
    public void getConsequenceEvents() {
	List<DiscreteEvent> discreteEvents = new ArrayList<>();
	discreteEvents.add(new DiscreteEvent("b1", 0));
	discreteEvents.add(new DiscreteEvent("b2", 0));
	discreteEvents.add(new DiscreteEvent("b1", 1));
	List<ContinuousEvent> continuousEvents = new ArrayList<>();
	continuousEvents.add(new ContinuousEvent("b3", 0, 1));
	continuousEvents.add(new ContinuousEvent("b4", 0, 1));
	continuousEvents.add(new ContinuousEvent("b3", 1, 2));

	KeyBehaviorMapping consequence = new KeyBehaviorMapping("b1", MappableChar.B, "", false, false);
	List<BehaviorEvent> targetEvents = ConditionalProbability.getConsequenceEvents(consequence, discreteEvents,
		continuousEvents);

	List<BehaviorEvent> expected = new ArrayList<>();
	expected.add(new DiscreteBehavior("b1", MappableChar.B, "", 0));
	expected.add(new DiscreteBehavior("b1", MappableChar.B, "", 1));
	Assert.assertTrue(targetEvents.size() == 2);
	Assert.assertTrue(targetEvents.get(0).uuid.equals("b1"));
	Assert.assertTrue(targetEvents.get(0).startTime == 0);
	Assert.assertTrue(targetEvents.get(1).uuid.equals("b1"));
	Assert.assertTrue(targetEvents.get(1).startTime == 1);

	consequence = new KeyBehaviorMapping("b3", MappableChar.C, "", true, false);
	targetEvents = ConditionalProbability.getConsequenceEvents(consequence, discreteEvents, continuousEvents);

	expected = new ArrayList<>();
	expected.add(new ContinuousBehavior("b3", MappableChar.C, "", 0, 1));
	expected.add(new ContinuousBehavior("b3", MappableChar.C, "", 1, 1));
	Assert.assertTrue(targetEvents.size() == 2);
	Assert.assertTrue(targetEvents.get(0).uuid.equals("b3"));
	Assert.assertTrue(targetEvents.get(0).startTime == 0);
	Assert.assertTrue(targetEvents.get(1).uuid.equals("b3"));
	Assert.assertTrue(targetEvents.get(1).startTime == 1);
    }

    // Generating Background Events

    @Test
    public void createNoneEOBackgroundEventsWithIter() {
	KeyBehaviorMapping target = new KeyBehaviorMapping("b1", MappableChar.B, "", false, false);
	int numEvents = 3;
	Iterator<Integer> iter = Lists.newArrayList(0, 0, 1, 1, 2, 2, 3, 3).iterator();
	List<BehaviorEvent> consequenceEvents = Lists.newArrayList();
	consequenceEvents.add(new ContinuousBehavior("c1", MappableChar.C, "", 0, 0));
	List<DiscreteBehavior> events = ConditionalProbability.randomBackgroundEventsWithIter(iter, target,
		consequenceEvents, numEvents);
	List<Integer> expectedStartTimes = Lists.newArrayList(1, 2, 3);
	List<Integer> actualStartTimes = Lists.transform(events, (e) -> e.startTime);
	Assert.assertEquals(expectedStartTimes, actualStartTimes);
	Assert.assertTrue(events.stream().allMatch((e) -> e.uuid.equals(target.uuid)));
    }

    @Test
    public void createNonEOBackgroundEventsTrueRandom() throws TooManyBackgroundEventsException {
	KeyBehaviorMapping target = new KeyBehaviorMapping("b1", MappableChar.B, "", false, false);
	int numEvents = 9;
	int duration = 9;
	List<BehaviorEvent> consequenceEvents = Lists.newArrayList();
	consequenceEvents.add(new ContinuousBehavior("c1", MappableChar.C, "", 0, 0));
	List<DiscreteBehavior> events = ConditionalProbability.randomBackgroundEvents(target, consequenceEvents,
		duration, numEvents);
	Assert.assertEquals(9, events.size());
	Assert.assertEquals(9, Sets.newHashSet(events).size());
	Assert.assertTrue(events.stream().noneMatch((e) -> e.startTime == 0));
    }

    @Test
    public void createNonEOBackgroundEventsRejectsImpossibleState() throws TooManyBackgroundEventsException {
	KeyBehaviorMapping target = new KeyBehaviorMapping("b1", MappableChar.B, "", false, false);
	int numEvents = 3;
	int duration = 3;
	List<BehaviorEvent> consequenceEvents = Lists.newArrayList();
	consequenceEvents.add(new ContinuousBehavior("c1", MappableChar.C, "", 0, 1));

	ThrowingRunnable r = () -> ConditionalProbability.randomBackgroundEvents(target, consequenceEvents, duration,
		numEvents);
	Assert.assertThrows(ConditionalProbability.TooManyBackgroundEventsException.class, r);

	int numEvents2 = 5;
	int duration2 = 3;
	List<BehaviorEvent> consequenceEvents2 = Lists.newArrayList();
	r = () -> ConditionalProbability.randomBackgroundEvents(target, consequenceEvents2, duration2,
		numEvents2);
	Assert.assertThrows(ConditionalProbability.TooManyBackgroundEventsException.class, r);
    }

    // Calculating Conditional Probabilities

    @Test
    public void binaryNonEO() {
	List<BehaviorEvent> targetEvents = Lists.newArrayList();
	targetEvents.add(new DiscreteBehavior("target", MappableChar.T, "target", 4000));
	targetEvents.add(new DiscreteBehavior("target", MappableChar.T, "target", 10000));

	List<BehaviorEvent> consequentEvents = Lists.newArrayList();
	consequentEvents.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 3000, 2000));
	consequentEvents.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 17000, 1000));

	Results result = ConditionalProbability.binaryNonEO(targetEvents, consequentEvents,
		ConditionalProbability.RANGE_5);
	Results expected = new Results(1.0 / 2, 2, 2);
	Assert.assertEquals(expected, result);

	expected = new Results(1.0, 2, 2);
	result = ConditionalProbability.binaryNonEO(targetEvents, consequentEvents, ConditionalProbability.RANGE_10);
	Assert.assertEquals(expected, result);
    }

    @Test
    public void binaryEO() throws Exception {
	List<BehaviorEvent> targetEvents = Lists.newArrayList();
	targetEvents.add(new DiscreteBehavior("target", MappableChar.T, "target", 4000));
	targetEvents.add(new DiscreteBehavior("target", MappableChar.T, "target", 10000));
	List<BehaviorEvent> consequentEvents = Lists.newArrayList();
	consequentEvents.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 3000, 2000));
	consequentEvents.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 17000, 1000));

	Results result = ConditionalProbability.binaryEO(targetEvents, consequentEvents,
		ConditionalProbability.RANGE_5);
	Results expected = new Results(0.0 / 2, 1, 2);
	Assert.assertEquals(expected, result);

	result = ConditionalProbability.binaryEO(targetEvents, consequentEvents, ConditionalProbability.RANGE_10);
	expected = new Results(1.0 / 1, 1, 2);
	Assert.assertEquals(expected, result);
    }

    @Test
    public void proportionNonEO() throws Exception {
	List<BehaviorEvent> targetEvents = Lists.newArrayList();
	targetEvents.add(new DiscreteBehavior("target", MappableChar.T, "target", 4000));
	targetEvents.add(new DiscreteBehavior("target", MappableChar.T, "target", 10000));

	List<BehaviorEvent> consequentEvents = Lists.newArrayList();
	consequentEvents.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 3000, 2000));
	consequentEvents.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 17000, 1000));

	Results result = ConditionalProbability.proportionNonEO(targetEvents, consequentEvents,
		ConditionalProbability.RANGE_5);
	Results expected = new Results(1.0 / 10, 2, 2);
	Assert.assertEquals(expected, result);

	result = ConditionalProbability.proportionNonEO(targetEvents, consequentEvents,
		ConditionalProbability.RANGE_10);
	expected = new Results(2.0 / 20, 2, 2);
	Assert.assertEquals(expected, result);

	result = ConditionalProbability.proportionNonEO(targetEvents, consequentEvents,
		ConditionalProbability.RANGE_20);
	expected = new Results(3.0 / 40, 2, 2);
	Assert.assertEquals(expected, result);
    }

    @Test
    public void proportionEO() throws Exception {
	List<BehaviorEvent> targetEvents = Lists.newArrayList();
	targetEvents.add(new DiscreteBehavior("target", MappableChar.T, "target", 4000));
	targetEvents.add(new DiscreteBehavior("target", MappableChar.T, "target", 10000));

	List<BehaviorEvent> consequentEvents = Lists.newArrayList();
	consequentEvents.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 3000, 2000));
	consequentEvents.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 17000, 1000));

	Results result = ConditionalProbability.proportionEO(targetEvents, consequentEvents,
		ConditionalProbability.RANGE_5);
	Results expected = new Results(0.0 / 5, 1, 2);
	Assert.assertEquals(expected, result);

	result = ConditionalProbability.proportionEO(targetEvents, consequentEvents, ConditionalProbability.RANGE_10);
	expected = new Results(1.0 / 10, 1, 2);
	Assert.assertEquals(expected, result);

	result = ConditionalProbability.proportionEO(targetEvents, consequentEvents, ConditionalProbability.RANGE_20);
	expected = new Results(1.0 / 20, 1, 2);
	Assert.assertEquals(expected, result);
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
	Results result = ConditionalProbability.binaryNonEO(targetEvents, consequentEvents,
		ConditionalProbability.RANGE_5);
	Results expected = new Results(10.0 / 10, 10, 10);
	Assert.assertEquals(expected, result);
	result = ConditionalProbability.proportionNonEO(targetEvents, consequentEvents, ConditionalProbability.RANGE_5);
	expected = new Results(19.0 / 50, 10, 10);
	Assert.assertEquals(expected, result);

	// EO
	result = ConditionalProbability.binaryEO(targetEvents, consequentEvents, ConditionalProbability.RANGE_5);
	expected = new Results(9.0 / 9, 9, 10);
	Assert.assertEquals(expected, result);
	result = ConditionalProbability.proportionEO(targetEvents, consequentEvents, ConditionalProbability.RANGE_5);
	expected = new Results(17.0 / 45, 9, 10);
	Assert.assertEquals(expected, result);
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
	Results result = ConditionalProbability.binaryNonEO(targetEvents, consequentEvents,
		ConditionalProbability.RANGE_5);
	Results expected = new Results(1.0 / 10, 10, 10);
	Assert.assertEquals(expected, result);
	result = ConditionalProbability.proportionNonEO(targetEvents, consequentEvents, ConditionalProbability.RANGE_5);
	expected = new Results(4.0 / 50, 10, 10);
	Assert.assertEquals(expected, result);
	result = ConditionalProbability.proportionNonEO(targetEvents, consequentEvents,
		ConditionalProbability.RANGE_10);
	expected = new Results(6.0 / 100, 10, 10);
	Assert.assertEquals(expected, result);

	// EO
	result = ConditionalProbability.binaryEO(targetEvents, consequentEvents, ConditionalProbability.RANGE_5);
	expected = new Results(0.0 / 9, 9, 10);
	Assert.assertEquals(expected, result);
	result = ConditionalProbability.proportionEO(targetEvents, consequentEvents, ConditionalProbability.RANGE_5);
	expected = new Results(0.0 / 45, 9, 10);
	Assert.assertEquals(expected, result);
	result = ConditionalProbability.proportionEO(targetEvents, consequentEvents, ConditionalProbability.RANGE_10);
	expected = new Results(2.0 / 90, 9, 10);
	Assert.assertEquals(expected, result);
    }

    @Test
    public void emptyTargets() {
	List<BehaviorEvent> targetEvents = Lists.newArrayList();
	List<BehaviorEvent> consequentEvents = Lists.newArrayList();
	consequentEvents.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 3000, 2000));
	consequentEvents.add(new ContinuousBehavior("consequence", MappableChar.C, "consequence", 17000, 1000));

	Results result = ConditionalProbability.binaryNonEO(targetEvents, consequentEvents,
		ConditionalProbability.RANGE_5);
	Results expected = new Results(-1, 0, 0);
	Assert.assertEquals(expected, result);

	result = ConditionalProbability.binaryEO(targetEvents, consequentEvents, ConditionalProbability.RANGE_5);
	expected = new Results(-1, 0, 0);
	Assert.assertEquals(expected, result);

	result = ConditionalProbability.proportionNonEO(targetEvents, consequentEvents, ConditionalProbability.RANGE_5);
	expected = new Results(-1, 0, 0);
	Assert.assertEquals(expected, result);

	result = ConditionalProbability.proportionEO(targetEvents, consequentEvents, ConditionalProbability.RANGE_5);
	expected = new Results(-1, 0, 0);
	Assert.assertEquals(expected, result);
    }

    @Test
    public void emptyConsequences() {
	List<BehaviorEvent> targetEvents = Lists.newArrayList();
	targetEvents.add(new DiscreteBehavior("target", MappableChar.T, "target", 4000));
	targetEvents.add(new DiscreteBehavior("target", MappableChar.T, "target", 10000));

	List<BehaviorEvent> consequentEvents = Lists.newArrayList();

	Results result = ConditionalProbability.binaryNonEO(targetEvents, consequentEvents,
		ConditionalProbability.RANGE_5);
	Results expected = new Results(0, 2, 2);
	Assert.assertEquals(expected, result);

	result = ConditionalProbability.binaryEO(targetEvents, consequentEvents, ConditionalProbability.RANGE_5);
	expected = new Results(0, 2, 2);
	Assert.assertEquals(expected, result);

	result = ConditionalProbability.proportionNonEO(targetEvents, consequentEvents, ConditionalProbability.RANGE_5);
	expected = new Results(0, 2, 2);
	Assert.assertEquals(expected, result);

	result = ConditionalProbability.proportionEO(targetEvents, consequentEvents, ConditionalProbability.RANGE_5);
	expected = new Results(0, 2, 2);
	Assert.assertEquals(expected, result);
    }

}
