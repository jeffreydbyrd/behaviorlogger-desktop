package com.behaviorlogger.models.behaviors;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.behaviorlogger.models.MappableChar;
import com.google.common.collect.Lists;

public class DiscreteBehaviorTest {
    @Test
    public void intervalsOneSecond() {
	DiscreteBehavior db = new DiscreteBehavior("uuid", MappableChar.D, "discrete", 3300);
	List<Integer> intervals = db.intervals(1000);
	Assert.assertTrue(intervals.equals(Lists.newArrayList(3)));
    }
    
    @Test
    public void intervalsOneMillisecond() {
	DiscreteBehavior db = new DiscreteBehavior("uuid", MappableChar.D, "discrete", 3300);
	List<Integer> intervals = db.intervals(1);
	Assert.assertTrue(intervals.equals(Lists.newArrayList(3300)));
    }
    
    @Test
    public void endTime() {
	DiscreteBehavior db = new DiscreteBehavior("uuid", MappableChar.D, "discrete", 3300);
	Assert.assertEquals(3300, db.endTime());
    }
}
