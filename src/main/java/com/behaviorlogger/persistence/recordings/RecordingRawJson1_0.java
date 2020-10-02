package com.behaviorlogger.persistence.recordings;

import java.util.ArrayList;
import java.util.HashMap;

import org.joda.time.DateTime;

import com.behaviorlogger.BehaviorLoggerApp;
import com.behaviorlogger.models.behaviors.BehaviorEvent;
import com.behaviorlogger.models.behaviors.ContinuousBehavior;
import com.behaviorlogger.models.schemas.KeyBehaviorMapping;
import com.behaviorlogger.models.schemas.SchemaVersion;
import com.behaviorlogger.persistence.GsonUtils;
import com.behaviorlogger.persistence.SessionDirectories;
import com.behaviorlogger.persistence.recordings.Recordings.SaveDetails;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class RecordingRawJson1_0
{
  public static class BehaviorBean1_0
  {
    public Character key;
    public String name;
    public boolean isContinuous;

    public BehaviorBean1_0( Character key, String name, boolean isContinuous )
    {
      this.key = key;
      this.name = name;
      this.isContinuous = isContinuous;
    }
  }

  public static class SchemaBean1_0
  {
    public String uuid;
    public String client;
    public String project;
    public ArrayList< BehaviorBean1_0 > behaviors;
    public String sessionDirectory;
    public Integer duration; // in seconds
    public Boolean pause;
    public Boolean color;
    public Boolean sound;
  }

  public static class SessionBean1_0
  {
    String uuid;
    String version;
    public SchemaBean1_0 schema;
    String observer;
    String therapist;
    String condition;
    String location;
    Integer sessionNumber;
    public Integer totalTimeMillis;
    String notes;
    public DateTime startTime;
    public DateTime stopTime;

    // maps behavior key to times (in seconds) it occurred
    public HashMap< Character, ArrayList< Integer > > discretes;
    public HashMap< Character, ArrayList< Integer > > continuous;
  }

  public static void write( SaveDetails details ) throws Exception
  {
    if (!details.f.exists()) {
      details.f.getParentFile().mkdirs();
      details.f.createNewFile();
    }

    SessionBean1_0 bean = new SessionBean1_0();
    bean.schema = new SchemaBean1_0();
    bean.version = BehaviorLoggerApp.version;
    bean.discretes = Maps.newHashMap();
    bean.continuous = Maps.newHashMap();

    bean.uuid = details.sessionUuid;

    copySchema( details.schema, bean.schema );

    bean.observer = details.observer;
    bean.therapist = details.therapist;
    bean.condition = details.condition;
    bean.location = details.location;
    bean.sessionNumber = details.sessionNumber;
    bean.totalTimeMillis = details.totalTimeMillis;
    bean.notes = details.notes;
    bean.startTime = new DateTime( details.startTime );
    bean.stopTime = new DateTime( details.stopTime );

    for (BehaviorEvent b : details.behaviors) {
      char c = b.key.c;

      if (b.isContinuous()) {
        if (!bean.continuous.containsKey( c )) {
          bean.continuous.put( c, Lists.newArrayList() );
        }

        Integer start = b.startTime / 1000; // start-time in seconds
        Integer end = start + (((ContinuousBehavior) b).getDuration() / 1000); // end-time in seconds

        for (int i = start; i <= end; i++) {
          if (!bean.continuous.get( c ).contains( i )) { // make sure there are no overlaps
            bean.continuous.get( c ).add( i );
          }
        }
      } else {
        if (!bean.discretes.containsKey( c )) {
          bean.discretes.put( c, Lists.newArrayList() );
        }
        bean.discretes.get( c ).add( b.startTime / 1000 );
      }
    }

    GsonUtils.save( details.f, bean );
  }

  private static void copySchema( SchemaVersion from, SchemaBean1_0 to )
  {
    to.uuid = from.uuid;
    to.client = from.client;
    to.project = from.project;
    to.behaviors = Lists.newArrayList();

    for (KeyBehaviorMapping b : from.behaviors) {
      to.behaviors.add( new BehaviorBean1_0( b.key.c, b.description, b.isContinuous ) );
    }

    to.sessionDirectory = SessionDirectories.getForSchemaIdOrDefault( from.uuid ).getAbsolutePath();
    to.duration = from.duration;
    to.pause = from.pause;
    to.color = from.color;
    to.sound = from.sound;
  }
}