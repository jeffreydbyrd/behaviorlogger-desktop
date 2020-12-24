package com.behaviorlogger.models.behaviors;

import java.util.ArrayList;
import java.util.List;

import com.behaviorlogger.models.MappableChar;
import com.behaviorlogger.utils.BehaviorLoggerUtil;
import com.google.common.collect.Lists;

public class ContinuousBehavior extends BehaviorEvent {
    private int duration;

    /**
     * @param uuid
     * @param key
     * @param description
     * @param start       - start-time in millis
     * @param duration    - duration of the behavior in millis
     */
    public ContinuousBehavior(String uuid, MappableChar key, String description, long start, int duration) {
	super(uuid, key, description, start);
	this.duration = duration;
    }

    @Override
    public boolean isContinuous() {
	return true;
    }

    /**
     * @return the duration of this behavior in milliseconds
     */
    public int getDuration() {
	return duration;
    }

    @Override
    public String timeDisplay() {
	String start = BehaviorLoggerUtil.millisToTimestampNoSpaces(startTime);
	String end = BehaviorLoggerUtil.millisToTimestampNoSpaces(startTime + duration);
	return String.format("%s-%s", start, end);
    }

    @Override
    public String type() {
	return "continuous";
    }

    @Override
    public List<Integer> intervals(int sizeMillis) {
	ArrayList<Integer> result = Lists.newArrayList();
	long end = this.startTime + this.duration + sizeMillis;
	for (long n = this.startTime; n < end; n += sizeMillis) {
	    result.add((int) (n / sizeMillis));
	}
	return result;
    }

    @Override
    public long endTime() {
	return this.startTime + this.duration;
    }
}
