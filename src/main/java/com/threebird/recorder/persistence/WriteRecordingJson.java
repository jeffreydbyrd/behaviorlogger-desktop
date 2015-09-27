package com.threebird.recorder.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.threebird.recorder.models.MappableChar;
import com.threebird.recorder.models.behaviors.Behavior;
import com.threebird.recorder.models.behaviors.ContinuousBehavior;
import com.threebird.recorder.models.schemas.KeyBehaviorMapping;
import com.threebird.recorder.models.schemas.Schema;
import com.threebird.recorder.persistence.Recordings.SaveDetails;

public class WriteRecordingJson
{
  @SuppressWarnings("unused")
  private static class BehaviorBean
  {
    Character key;
    String name;
    boolean isContinuous;

    public BehaviorBean( Character key, String name, boolean isContinuous )
    {
      this.key = key;
      this.name = name;
      this.isContinuous = isContinuous;
    }
  }

  @SuppressWarnings("unused")
  private static class SchemaBean
  {
    public String uuid;
    public String client;
    public String project;
    public ArrayList< BehaviorBean > behaviors;
    public String sessionDirectory;
    public Integer duration; // in seconds
    public Boolean pause;
    public Boolean color;
    public Boolean sound;
  }

  @SuppressWarnings("unused")
  private static class SessionBean
  {
    String uuid;
    SchemaBean schema;
    String observer;
    String therapist;
    String condition;
    String location;
    Integer sessionNumber;
    Integer totalTimeMillis;
    String notes;

    // maps behavior name to times (in seconds) it occurred
    HashMap< Character, ArrayList< Integer >> times;
  }

  public static void write( SaveDetails details ) throws Exception
  {
    if (!details.f.exists()) {
      details.f.getParentFile().mkdirs();
      details.f.createNewFile();
    }

    SessionBean bean = new SessionBean();
    bean.schema = new SchemaBean();
    bean.times = Maps.newHashMap();

    bean.uuid = details.uuid;

    copySchema( details.schema, bean.schema );

    bean.observer = details.observer;
    bean.therapist = details.therapist;
    bean.condition = details.condition;
    bean.location = details.location;
    bean.sessionNumber = details.sessionNumber;
    bean.totalTimeMillis = details.totalTimeMillis;
    bean.notes = details.notes;

    for (Behavior b : details.behaviors) {
      Character c = new Character( b.key.c );
      
      if (!bean.times.containsKey( c )) {
        bean.times.put( c, Lists.newArrayList() );
      }

      if (b.isContinuous()) {
        Integer start = b.startTime / 1000; // start-time in seconds
        Integer end = start + (((ContinuousBehavior) b).getDuration() / 1000); // end-time in seconds

        for (int i = start; i <= end; i++) {
          if (!bean.times.get( c ).contains( i )) { // make sure there are no overlaps
            bean.times.get( c ).add( i );
          }
        }
      } else {
        bean.times.get( c ).add( b.startTime / 1000 );
      }
    }

    GsonUtils.save( details.f, bean );
  }

  private static void copySchema( Schema from, SchemaBean to )
  {
    to.uuid = from.uuid;
    to.client = from.client;
    to.project = from.project;
    to.behaviors = Lists.newArrayList();

    for (Entry< MappableChar, KeyBehaviorMapping > entry : from.mappings.entrySet()) {
      KeyBehaviorMapping b = entry.getValue();
      to.behaviors.add( new BehaviorBean( b.key.c, b.behavior, b.isContinuous ) );
    }

    to.sessionDirectory = from.sessionDirectory.getAbsolutePath();
    to.duration = from.duration;
    to.pause = from.pause;
    to.color = from.color;
    to.sound = from.sound;
  }
}
