package com.threebird.recorder.utils.ioa.version1_0;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.UUID;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.threebird.recorder.persistence.recordings.RecordingRawJson1_0.BehaviorBean1_0;
import com.threebird.recorder.persistence.recordings.RecordingRawJson1_0.SessionBean1_0;
import com.threebird.recorder.persistence.recordings.RecordingRawJson1_1.BehaviorBean1_1;
import com.threebird.recorder.persistence.recordings.RecordingRawJson1_1.ContinuousEvent;
import com.threebird.recorder.persistence.recordings.RecordingRawJson1_1.DiscreteEvent;
import com.threebird.recorder.persistence.recordings.RecordingRawJson1_1.SchemaBean1_1;
import com.threebird.recorder.persistence.recordings.RecordingRawJson1_1.SessionBean1_1;

public class ConvertTo1_1
{
  public static SessionBean1_1 convert( SessionBean1_0 bean0 )
  {
    // Convert the Schema first:
    SessionBean1_1 bean1 = new SessionBean1_1();
    bean1.schema = new SchemaBean1_1();
    bean1.schema.behaviors = Lists.newArrayList();

    // Copy over schema behaviors
    for (BehaviorBean1_0 behavior0 : bean0.schema.behaviors) {
      BehaviorBean1_1 behavior1 = new BehaviorBean1_1();
      behavior1.uuid = UUID.randomUUID().toString();
      behavior1.key = behavior0.key;
      bean1.schema.behaviors.add( behavior1 );
    }

    // Copy over random details
    bean1.duration = bean0.totalTimeMillis;
    bean1.startTime = bean0.startTime;
    bean1.stopTime = bean0.stopTime;

    // Index behaviors by character
    ImmutableMap< Character, BehaviorBean1_1 > behaviors = Maps.uniqueIndex( bean1.schema.behaviors, ( b ) -> b.key );

    // Copy over discrete events
    bean1.discreteEvents = Lists.newArrayList();
    for (Entry< Character, ArrayList< Integer > > entry : bean0.discretes.entrySet()) {
      Character key = entry.getKey();
      String behUuid = behaviors.get( key ).uuid;
      ArrayList< Integer > timesSeconds = entry.getValue();
      for (Integer t : timesSeconds) {
        bean1.discreteEvents.add( new DiscreteEvent( behUuid, t * 1000 ) );
      }
    }

    // Copy over continuous events
    bean1.continuousEvents = Lists.newArrayList();
    for (Entry< Character, ArrayList< Integer > > entry : bean0.continuous.entrySet()) {
      Character key = entry.getKey();
      String behUuid = behaviors.get( key ).uuid;
      ArrayList< Integer > timesSeconds = entry.getValue();

      for (Integer t : timesSeconds) {
        bean1.continuousEvents.add( new ContinuousEvent( behUuid, t * 1000, t * 1000 + 1 ) );
      }
    }

    return bean1;
  }
}
