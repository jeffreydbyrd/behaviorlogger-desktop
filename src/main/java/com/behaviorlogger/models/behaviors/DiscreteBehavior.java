package com.behaviorlogger.models.behaviors;

import java.util.List;

import com.behaviorlogger.models.MappableChar;
import com.behaviorlogger.utils.BehaviorLoggerUtil;
import com.google.common.collect.Lists;

public class DiscreteBehavior extends BehaviorEvent {
    public DiscreteBehavior(String uuid, MappableChar key, String description, Integer time) {
	super(uuid, key, description, time);
    }

    @Override
    public boolean isContinuous() {
	return false;
    }

    @Override
    public String timeDisplay() {
	return BehaviorLoggerUtil.millisToTimestampNoSpaces(startTime);
    }

    @Override
    public String type() {
	return "discrete";
    }

    @Override
    public List<Integer> intervals(int sizeMillis) {
	return Lists.newArrayList(this.startTime / sizeMillis);
    }
    
    @Override
    public int endTime() {
        return this.startTime;
    }
}
