package com.behaviorlogger.persistence.recordings;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import com.behaviorlogger.BehaviorLoggerApp;
import com.behaviorlogger.models.MappableChar;
import com.behaviorlogger.models.behaviors.BehaviorEvent;
import com.behaviorlogger.models.behaviors.ContinuousBehavior;
import com.behaviorlogger.models.schemas.KeyBehaviorMapping;
import com.behaviorlogger.models.schemas.SchemaVersion;
import com.behaviorlogger.persistence.GsonUtils;
import com.behaviorlogger.persistence.recordings.Recordings.SaveDetails;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class RecordingRawJson1_1 {
    public static class DiscreteEvent {
	public String uuid;
	public String behaviorUuid;
	public long time;

	public DiscreteEvent(String uuid, String behaviorUuid, long time) {
	    this.uuid = uuid;
	    this.behaviorUuid = behaviorUuid;
	    this.time = time;
	}

	public DiscreteEvent(String behaviorUuid, long time) {
	    this(UUID.randomUUID().toString(), behaviorUuid, time);
	}

	@Override
	public String toString() {
	    return "(" + uuid + ", " + behaviorUuid + ", " + time + ")";
	}

	@Override
	public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((behaviorUuid == null) ? 0 : behaviorUuid.hashCode());
	    result = prime * result + (int) (time ^ (time >>> 32));
	    result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
	    return result;
	}

	@Override
	public boolean equals(Object obj) {
	    if (this == obj)
		return true;
	    if (obj == null)
		return false;
	    if (getClass() != obj.getClass())
		return false;
	    DiscreteEvent other = (DiscreteEvent) obj;
	    if (behaviorUuid == null) {
		if (other.behaviorUuid != null)
		    return false;
	    } else if (!behaviorUuid.equals(other.behaviorUuid))
		return false;
	    if (time != other.time)
		return false;
	    if (uuid == null) {
		if (other.uuid != null)
		    return false;
	    } else if (!uuid.equals(other.uuid))
		return false;
	    return true;
	}
    }

    public static class ContinuousEvent {
	public String uuid;
	public String behaviorUuid;
	public long startTime;
	public long endTime;

	public ContinuousEvent(String uuid, String behaviorUuid, long startTime, long endTime) {
	    this.uuid = uuid;
	    this.behaviorUuid = behaviorUuid;
	    this.startTime = startTime;
	    this.endTime = endTime;
	}

	public ContinuousEvent(String behaviorUuid, long startTime, long endTime) {
	    this(UUID.randomUUID().toString(), behaviorUuid, startTime, endTime);
	}

	@Override
	public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((behaviorUuid == null) ? 0 : behaviorUuid.hashCode());
	    result = prime * result + (int) (endTime ^ (endTime >>> 32));
	    result = prime * result + (int) (startTime ^ (startTime >>> 32));
	    result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
	    return result;
	}

	@Override
	public boolean equals(Object obj) {
	    if (this == obj)
		return true;
	    if (obj == null)
		return false;
	    if (getClass() != obj.getClass())
		return false;
	    ContinuousEvent other = (ContinuousEvent) obj;
	    if (behaviorUuid == null) {
		if (other.behaviorUuid != null)
		    return false;
	    } else if (!behaviorUuid.equals(other.behaviorUuid))
		return false;
	    if (endTime != other.endTime)
		return false;
	    if (startTime != other.startTime)
		return false;
	    if (uuid == null) {
		if (other.uuid != null)
		    return false;
	    } else if (!uuid.equals(other.uuid))
		return false;
	    return true;
	}
    }

    public static class SessionBean1_1 {
	String uuid;
	String blVersion;
	String versionUuid;
	int sessionNumber;
	public long duration; // in millis
	String notes;
	public long startTime; // in millis
	public SchemaVersion schema;
	Map<String, String> attributes;

	// maps behavior key to times (in millis) it occurred
	public ArrayList<DiscreteEvent> discreteEvents;

	// maps behavior key to start and end-times in millis
	public ArrayList<ContinuousEvent> continuousEvents;
    }

    public static void write(SaveDetails details) throws Exception {
	if (!details.f.exists()) {
	    details.f.getParentFile().mkdirs();
	    details.f.createNewFile();
	}

	SessionBean1_1 bean = new SessionBean1_1();
	bean.schema = details.schema;
	bean.blVersion = BehaviorLoggerApp.version;
	bean.discreteEvents = Lists.newArrayList();
	bean.continuousEvents = Lists.newArrayList();

	bean.uuid = details.sessionUuid;
	bean.versionUuid = details.schema.versionUuid;
	bean.sessionNumber = details.sessionNumber;
	bean.duration = details.totalTimeMillis;
	bean.notes = details.notes;
	bean.startTime = details.startTime;

	bean.attributes = Maps.newHashMap();
	bean.attributes.put("observer", Strings.emptyToNull(details.observer));
	bean.attributes.put("therapist", Strings.emptyToNull(details.therapist));
	bean.attributes.put("condition", Strings.emptyToNull(details.condition));
	bean.attributes.put("location", Strings.emptyToNull(details.location));

	ImmutableMap<MappableChar, KeyBehaviorMapping> behaviorsMap = bean.schema.behaviorsMap();
	for (BehaviorEvent b : details.behaviors) {
	    if (!behaviorsMap.containsKey(b.key)) {
		continue;
	    }
	    String behaviorUuid = behaviorsMap.get(b.key).uuid;
	    if (b.isContinuous()) {
		ContinuousEvent ce = new ContinuousEvent(behaviorUuid, b.startTime,
			b.startTime + (((ContinuousBehavior) b).getDuration()));
		bean.continuousEvents.add(ce);
	    } else {
		DiscreteEvent de = new DiscreteEvent(behaviorUuid, b.startTime);
		bean.discreteEvents.add(de);
	    }
	}

	GsonUtils.save(details.f, bean);
    }
}
