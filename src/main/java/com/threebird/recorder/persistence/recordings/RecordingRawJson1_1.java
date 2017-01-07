package com.threebird.recorder.persistence.recordings;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.joda.time.DateTime;

import com.google.common.collect.Lists;
import com.threebird.recorder.BehaviorLoggerApp;
import com.threebird.recorder.models.MappableChar;
import com.threebird.recorder.models.behaviors.Behavior;
import com.threebird.recorder.models.behaviors.ContinuousBehavior;
import com.threebird.recorder.models.schemas.KeyBehaviorMapping;
import com.threebird.recorder.models.schemas.SchemaVersion;
import com.threebird.recorder.persistence.GsonUtils;
import com.threebird.recorder.persistence.recordings.Recordings.SaveDetails;

public class RecordingRawJson1_1
{
  public static class BehaviorBean1_1
  {
    public String uuid;
    public Character key;
    public String description;
    public boolean isContinuous;
    public boolean archived;

    public BehaviorBean1_1()
    {}

    public BehaviorBean1_1( String uuid, Character key, String name, boolean isContinuous, boolean archived )
    {
      this.uuid = uuid;
      this.key = key;
      this.description = name;
      this.isContinuous = isContinuous;
      this.archived = archived;
    }

    @Override public String toString()
    {
      return "BehaviorBean1_1 [uuid=" + uuid + ", key=" + key + ", description=" + description + ", isContinuous="
          + isContinuous + ", archived=" + archived + " ]";
    }
  }

  public static class SchemaBean1_1
  {
    public String uuid;
    public String versionUuid;
    public Integer versionNumber;
    public Set< String > parentVersionSet;
    public String client;
    public String project;
    public Integer duration; // in milliseconds
    public Boolean pause;
    public Boolean color;
    public Boolean sound;
    public Boolean archived;
    public ArrayList< BehaviorBean1_1 > behaviors;

    @Override public String toString()
    {
      return "SchemaBean1_1 [uuid=" + uuid + ", client=" + client + ", project=" + project + ", versionUuid="
          + versionUuid + ", versionNumber=" + versionNumber
          + ", behaviors=" + behaviors + ", duration=" + duration
          + ", pause=" + pause + ", color=" + color + ", sound=" + sound + "]";
    }
  }

  public static class DiscreteEvent
  {
    public String uuid;
    public String behaviorUuid;
    public Integer time;

    public DiscreteEvent( String uuid, String behaviorUuid, Integer time )
    {
      this.uuid = uuid;
      this.behaviorUuid = behaviorUuid;
      this.time = time;
    }

    public DiscreteEvent( String behaviorUuid, Integer time )
    {
      this( UUID.randomUUID().toString(), behaviorUuid, time );
    }
    
    
    @Override public String toString()
    {
      return "(" + uuid + ", " + behaviorUuid + ", " + time + ")";
    }

    @Override public boolean equals( Object obj )
    {
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
      } else if (!behaviorUuid.equals( other.behaviorUuid ))
        return false;
      if (time == null) {
        if (other.time != null)
          return false;
      } else if (!time.equals( other.time ))
        return false;
      if (uuid == null) {
        if (other.uuid != null)
          return false;
      } else if (!uuid.equals( other.uuid ))
        return false;
      return true;
    }
  }

  public static class ContinuousEvent
  {
    public String uuid;
    public String behaviorUuid;
    public Integer startTime;
    public Integer endTime;

    public ContinuousEvent( String uuid, String behaviorUuid, Integer startTime, Integer endTime )
    {
      this.uuid = uuid;
      this.behaviorUuid = behaviorUuid;
      this.startTime = startTime;
      this.endTime = endTime;
    }

    public ContinuousEvent( String behaviorUuid, Integer startTime, Integer endTime )
    {
      this( UUID.randomUUID().toString(), behaviorUuid, startTime, endTime );
    }

    @Override public boolean equals( Object obj )
    {
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
      } else if (!behaviorUuid.equals( other.behaviorUuid ))
        return false;
      if (endTime == null) {
        if (other.endTime != null)
          return false;
      } else if (!endTime.equals( other.endTime ))
        return false;
      if (startTime == null) {
        if (other.startTime != null)
          return false;
      } else if (!startTime.equals( other.startTime ))
        return false;
      if (uuid == null) {
        if (other.uuid != null)
          return false;
      } else if (!uuid.equals( other.uuid ))
        return false;
      return true;
    }
  }

  public static class SessionBean1_1
  {
    String uuid;
    String blVersion;
    String versionUuid;
    Integer sessionNumber;
    public Integer duration; // in millis
    String observer;
    String therapist;
    String condition;
    String location;
    String notes;
    public DateTime startTime;
    public DateTime stopTime;
    public SchemaBean1_1 schema;

    // maps behavior key to times (in millis) it occurred
    public ArrayList< DiscreteEvent > discreteEvents;

    // maps behavior key to start and end-times in millis
    public ArrayList< ContinuousEvent > continuousEvents;
  }

  public static void write( SaveDetails details ) throws Exception
  {
    if (!details.f.exists()) {
      details.f.getParentFile().mkdirs();
      details.f.createNewFile();
    }

    SessionBean1_1 bean = new SessionBean1_1();
    bean.schema = new SchemaBean1_1();
    bean.blVersion = BehaviorLoggerApp.version;
    bean.discreteEvents = Lists.newArrayList();
    bean.continuousEvents = Lists.newArrayList();

    bean.uuid = details.uuid;

    copySchema( details.schema, bean.schema );

    bean.versionUuid = details.schema.versionUuid;
    bean.observer = details.observer;
    bean.therapist = details.therapist;
    bean.condition = details.condition;
    bean.location = details.location;
    bean.sessionNumber = details.sessionNumber;
    bean.duration = details.totalTimeMillis;
    bean.notes = details.notes;
    bean.startTime = details.startTime;
    bean.stopTime = details.stopTime;

    for (Behavior b : details.behaviors) {
      if (b.isContinuous()) {
        ContinuousEvent ce =
            new ContinuousEvent( b.uuid, b.startTime, b.startTime + (((ContinuousBehavior) b).getDuration()) );
        bean.continuousEvents.add( ce );
      } else {
        DiscreteEvent de = new DiscreteEvent( b.uuid, b.startTime );
        bean.discreteEvents.add( de );
      }
    }

    GsonUtils.save( details.f, bean );
  }

  private static void copySchema( SchemaVersion from, SchemaBean1_1 to )
  {
    to.uuid = from.uuid;
    to.versionUuid = from.versionUuid;
    to.versionNumber = from.versionNumber;
    to.parentVersionSet = from.parentVersionSet;
    to.client = from.client;
    to.project = from.project;
    to.duration = from.duration;
    to.pause = from.pause;
    to.color = from.color;
    to.sound = from.sound;
    to.archived = from.archived;
    to.behaviors = Lists.newArrayList();

    for (Entry< MappableChar, KeyBehaviorMapping > entry : from.behaviors.entrySet()) {
      KeyBehaviorMapping b = entry.getValue();
      to.behaviors.add( new BehaviorBean1_1( b.uuid, b.key.c, b.description, b.isContinuous, b.archived ) );
    }
  }
}
