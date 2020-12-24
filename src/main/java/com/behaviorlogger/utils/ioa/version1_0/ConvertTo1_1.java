package com.behaviorlogger.utils.ioa.version1_0;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.UUID;

import com.behaviorlogger.models.schemas.KeyBehaviorMapping;
import com.behaviorlogger.models.schemas.SchemaVersion;
import com.behaviorlogger.persistence.recordings.RecordingRawJson1_0.BehaviorBean1_0;
import com.behaviorlogger.persistence.recordings.RecordingRawJson1_0.SessionBean1_0;
import com.behaviorlogger.persistence.recordings.RecordingRawJson1_1.ContinuousEvent;
import com.behaviorlogger.persistence.recordings.RecordingRawJson1_1.DiscreteEvent;
import com.behaviorlogger.persistence.recordings.RecordingRawJson1_1.SessionBean1_1;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ConvertTo1_1
{
  public static SessionBean1_1 convert( SessionBean1_0 bean0 )
  {
    // Convert the Schema first:
    SessionBean1_1 bean1 = new SessionBean1_1();
    bean1.schema = new SchemaVersion();
    bean1.schema.behaviors = Lists.newArrayList();

    // Copy over schema behaviors
    for (BehaviorBean1_0 behavior0 : bean0.schema.behaviors) {
      KeyBehaviorMapping keyBehaviorMapping = new KeyBehaviorMapping( UUID.randomUUID().toString(), behavior0.key.toString(), behavior0.name, behavior0.isContinuous, false );
      bean1.schema.behaviors.add( keyBehaviorMapping );
    }

    // Copy over times
    bean1.duration = bean0.totalTimeMillis;
    bean1.startTime = bean0.startTime.getMillis();

    // Index behaviors by character
    ImmutableMap< Character, KeyBehaviorMapping > behaviors =
        Maps.uniqueIndex( bean1.schema.behaviors, ( b ) -> b.key.c );

    // Copy over discrete events
    bean1.discreteEvents = Lists.newArrayList();
    for (Entry< Character, ArrayList< Long > > entry : bean0.discretes.entrySet()) {
      Character key = entry.getKey();
      String behUuid = behaviors.get( key ).uuid;
      ArrayList< Long > timesSeconds = entry.getValue();
      for (long t : timesSeconds) {
        bean1.discreteEvents.add( new DiscreteEvent( behUuid, t * 1000 ) );
      }
    }

    // Copy over continuous events
    bean1.continuousEvents = Lists.newArrayList();
    for (Entry< Character, ArrayList< Long > > entry : bean0.continuous.entrySet()) {
      Character key = entry.getKey();
      String behUuid = behaviors.get( key ).uuid;
      ArrayList< Long > timesSeconds = entry.getValue();

      for (long t : timesSeconds) {
        bean1.continuousEvents.add( new ContinuousEvent( behUuid, t * 1000, t * 1000 + 1 ) );
      }
    }

    return bean1;
  }
}
