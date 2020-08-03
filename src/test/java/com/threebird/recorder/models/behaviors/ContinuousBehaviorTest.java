package com.threebird.recorder.models.behaviors;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.threebird.recorder.models.MappableChar;

public class ContinuousBehaviorTest {
    @Test
    public void intervals() {
	ContinuousBehavior cb = new ContinuousBehavior("uuid", MappableChar.C, "continuous", 1100, 1000);
	List<Integer> intervals = cb.intervals(500);
	Assert.assertTrue(intervals.equals(Lists.newArrayList(2, 3, 4)));
    }
}
