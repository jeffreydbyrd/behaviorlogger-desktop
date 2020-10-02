package com.behaviorlogger.models.behaviors;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.behaviorlogger.models.MappableChar;
import com.behaviorlogger.models.behaviors.ContinuousBehavior;
import com.google.common.collect.Lists;

public class ContinuousBehaviorTest {
    @Test
    public void intervalsHalfSecond() {
	ContinuousBehavior cb = new ContinuousBehavior("uuid", MappableChar.C, "continuous", 1100, 1000);
	List<Integer> intervals = cb.intervals(500);
	Assert.assertTrue(intervals.equals(Lists.newArrayList(2, 3, 4)));
    }

    @Test
    public void intervalsOneMillisecond() {
	ContinuousBehavior cb = new ContinuousBehavior("uuid", MappableChar.C, "continuous", 1100, 10);
	List<Integer> intervals = cb.intervals(1);
	Assert.assertTrue(
		intervals.equals(Lists.newArrayList(1100, 1101, 1102, 1103, 1104, 1105, 1106, 1107, 1108, 1109, 1110)));
    }

    @Test
    public void endTime() {
	ContinuousBehavior cb = new ContinuousBehavior("uuid", MappableChar.C, "continuous", 1100, 10);
	Assert.assertEquals(1110, cb.endTime());
    }
}
