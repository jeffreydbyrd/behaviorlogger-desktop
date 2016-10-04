package com.threebird.recorder.persistence.recordings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.joda.time.DateTime;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.threebird.recorder.EventRecorder;
import com.threebird.recorder.models.MappableChar;
import com.threebird.recorder.models.behaviors.Behavior;
import com.threebird.recorder.models.behaviors.ContinuousBehavior;
import com.threebird.recorder.models.schemas.KeyBehaviorMapping;
import com.threebird.recorder.models.schemas.Schema;
import com.threebird.recorder.persistence.GsonUtils;
import com.threebird.recorder.persistence.recordings.Recordings.SaveDetails;

public class RecordingRawJson1_1
{
  public static class BehaviorBean1_1
  {
    public String uuid;
    public Character key;
    public String name;
    public boolean isContinuous;

    public BehaviorBean1_1( String uuid, Character key, String name, boolean isContinuous )
    {
      this.uuid = uuid;
      this.key = key;
      this.name = name;
      this.isContinuous = isContinuous;
    }
  }

  public static class SchemaBean1_1
  {
    public String uuid;
    public String client;
    public String project;
    public int version;
    public ArrayList< BehaviorBean1_1 > behaviors;
    public String sessionDirectory;
    public Integer duration; // in milliseconds
    public Boolean pause;
    public Boolean color;
    public Boolean sound;
  }

  public static class SessionBean1_1
  {
    String streamUuid;
    String blVersion;
    public SchemaBean1_1 schema;
    String schemaUuid;
    Integer schemaVersion;
    String observer;
    String therapist;
    String condition;
    String location;
    Integer sessionNumber;
    public Integer totalTimeMillis;
    String notes;
    DateTime startTime;
    DateTime stopTime;

    // maps behavior key to times (in millis) it occurred
    public HashMap< String, ArrayList< Integer > > discreteEvents;

    // maps behavior key to start and end-times in millis
    public HashMap< String, ArrayList< StartEndTimes > > continuousEvents;
  }

  public static void write( SaveDetails details ) throws Exception
  {
    if (!details.f.exists()) {
      details.f.getParentFile().mkdirs();
      details.f.createNewFile();
    }

    SessionBean1_1 bean = new SessionBean1_1();
    bean.schema = new SchemaBean1_1();
    bean.blVersion = EventRecorder.version;
    bean.discreteEvents = Maps.newHashMap();
    bean.continuousEvents = Maps.newHashMap();

    bean.streamUuid = details.uuid;

    copySchema( details.schema, bean.schema );

    bean.schemaUuid = details.schema.uuid;
    bean.schemaVersion = details.schema.version;
    bean.observer = details.observer;
    bean.therapist = details.therapist;
    bean.condition = details.condition;
    bean.location = details.location;
    bean.sessionNumber = details.sessionNumber;
    bean.totalTimeMillis = details.totalTimeMillis;
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

  private static void copySchema( Schema from, SchemaBean1_1 to )
  {
    to.uuid = from.uuid;
    to.client = from.client;
    to.project = from.project;
    to.version = from.version;
    to.behaviors = Lists.newArrayList();

    for (Entry< MappableChar, KeyBehaviorMapping > entry : from.mappings.entrySet()) {
      KeyBehaviorMapping b = entry.getValue();
      to.behaviors.add( new BehaviorBean1_1( b.uuid, b.key.c, b.behavior, b.isContinuous ) );
    }

    to.sessionDirectory = from.sessionDirectory.getAbsolutePath();
    to.duration = from.duration;
    to.pause = from.pause;
    to.color = from.color;
    to.sound = from.sound;
  }
}
