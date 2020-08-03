package com.threebird.recorder.models.behaviors;

import java.util.Comparator;
import java.util.List;

import com.threebird.recorder.models.MappableChar;

/**
 * An actual behavioral event that a researcher observed during a recording
 * session
 */
public abstract class BehaviorEvent {
    public static final Comparator<BehaviorEvent> comparator = (BehaviorEvent o1, BehaviorEvent o2) -> o1.startTime
	    - o2.startTime;

    public final int startTime;
    public final String uuid;
    public final MappableChar key;
    public final String name;

    /**
     * @param key
     * @param description
     * @param startTime   - start-time in millis
     */
    BehaviorEvent(String uuid, MappableChar key, String description, int startTime) {
	this.uuid = uuid;
	this.key = key;
	this.name = description;
	this.startTime = startTime;
    }

    public abstract boolean isContinuous();

    public abstract String timeDisplay();

    public abstract String type();

    public abstract List<Integer> intervals(int sizeMillis);
}
