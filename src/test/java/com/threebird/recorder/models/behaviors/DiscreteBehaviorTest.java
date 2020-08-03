package com.threebird.recorder.models.behaviors;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.threebird.recorder.models.MappableChar;

public class DiscreteBehaviorTest {
    @Test
    public void intervals() {
	DiscreteBehavior db = new DiscreteBehavior("uuid", MappableChar.D, "discrete", 3300);
	List<Integer> intervals = db.intervals(1000);
	Assert.assertTrue(intervals.equals(Lists.newArrayList(3)));
    }
}
