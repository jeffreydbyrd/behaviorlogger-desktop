package com.threebird.recorder.persistence.recordings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

import org.joda.time.DateTime;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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

  public static class SessionBean1_1
  {
    String blVersion;
    String uuid;
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
    public HashMap< String, ArrayList< Integer > > discreteEvents;

    // maps behavior key to start and end-times in millis
    public HashMap< String, ArrayList< StartEndTimes > > continuousEvents;

    @Override public String toString()
    {
      return "SessionBean1_1 [uuid=" + uuid + ", blVersion=" + blVersion + ", schema=" + schema
          + ", versionUuid=" + versionUuid + ", observer=" + observer
          + ", therapist=" + therapist + ", condition=" + condition + ", location=" + location + ", sessionNumber="
          + sessionNumber + ", notes=" + notes + ", startTime=" + startTime
          + ", stopTime=" + stopTime + ", discreteEvents=" + discreteEvents + ", continuousEvents=" + continuousEvents
          + "]";
    }
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
    bean.discreteEvents = Maps.newHashMap();
    bean.continuousEvents = Maps.newHashMap();

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
        if (!bean.continuousEvents.containsKey( b.uuid )) {
          bean.continuousEvents.put( b.uuid, Lists.newArrayList() );
        }

        Integer start = b.startTime;
        Integer end = start + (((ContinuousBehavior) b).getDuration());
        bean.continuousEvents.get( b.uuid ).add( new StartEndTimes( start, end ) );
      } else {
        if (!bean.discreteEvents.containsKey( b.uuid )) {
          bean.discreteEvents.put( b.uuid, Lists.newArrayList() );
        }
        bean.discreteEvents.get( b.uuid ).add( b.startTime );
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
