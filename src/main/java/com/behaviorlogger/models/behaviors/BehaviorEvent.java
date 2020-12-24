package com.behaviorlogger.models.behaviors;

import java.util.Comparator;
import java.util.List;

import com.behaviorlogger.models.MappableChar;

/**
 * An actual behavioral event that a researcher observed during a recording
 * session
 */
public abstract class BehaviorEvent {
    public static final Comparator<BehaviorEvent> comparator = //
	    (BehaviorEvent o1, BehaviorEvent o2) -> {
		long diff = o1.startTime - o2.startTime;
		if (diff > 0) {
		    return 1;
		}
		if (diff < 0) {
		    return -1;
		}
		return 0;
	    };

    public final long startTime;
    public final String uuid;
    public final MappableChar key;
    public final String name;

    /**
     * @param key
     * @param description
     * @param startTime   - start-time in millis
     */
    BehaviorEvent(String uuid, MappableChar key, String description, long startTime) {
	this.uuid = uuid;
	this.key = key;
	this.name = description;
	this.startTime = startTime;
    }

    public abstract boolean isContinuous();

    public abstract String timeDisplay();

    public abstract String type();

    /**
     * Given an interval size, this returns the intervals that this behavior spans
     */
    public abstract List<Integer> intervals(int sizeMillis);

    public abstract long endTime();
}
