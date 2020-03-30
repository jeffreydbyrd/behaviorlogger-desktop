package com.threebird.recorder.utils.ioa.version1_1;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.threebird.recorder.models.schemas.KeyBehaviorMapping;
import com.threebird.recorder.models.schemas.SchemaVersion;
import com.threebird.recorder.persistence.GsonUtils;
import com.threebird.recorder.persistence.recordings.RecordingRawJson1_1.ContinuousEvent;
import com.threebird.recorder.persistence.recordings.RecordingRawJson1_1.DiscreteEvent;
import com.threebird.recorder.persistence.recordings.RecordingRawJson1_1.SessionBean1_1;
import com.threebird.recorder.utils.ioa.KeyToInterval;

public class IoaUtilsTest
{
  static SessionBean1_1 standard = new SessionBean1_1();
  static {
    standard.duration = 6000;
    standard.discreteEvents = Lists.newArrayList();
    standard.continuousEvents = Lists.newArrayList();

    standard.discreteEvents.add( new DiscreteEvent("event-one", "d", 0 ) );
    standard.discreteEvents.add( new DiscreteEvent( "event-two", "d", 1000 ) );
    standard.discreteEvents.add( new DiscreteEvent( "event-three", "d", 1100 ) );
    standard.discreteEvents.add( new DiscreteEvent( "event-four", "d", 3000 ) );
    standard.discreteEvents.add( new DiscreteEvent( "event-five", "d", 3100 ) );

    standard.continuousEvents.add( new ContinuousEvent( "event-six", "c", 0, 1100 ) );
    standard.continuousEvents.add( new ContinuousEvent( "event-seven", "c", 3100, 3900 ) );

    standard.schema = new SchemaVersion();
    standard.schema.behaviors = Lists.newArrayList();
    standard.schema.behaviors.add( new KeyBehaviorMapping( "d", 'd', "discrete", false, false ) );
    standard.schema.behaviors.add( new KeyBehaviorMapping( "c", 'c', "continuous", true, false ) );
  }

  static SessionBean1_1 empty = new SessionBean1_1();
  static {
    empty.duration = 6000;
    empty.discreteEvents = Lists.newArrayList();
    empty.continuousEvents = Lists.newArrayList();

    empty.schema = new SchemaVersion();
    empty.schema.behaviors = Lists.newArrayList();
    empty.schema.behaviors.add( new KeyBehaviorMapping( "d", 'd', "discrete", false, false ) );
    empty.schema.behaviors.add( new KeyBehaviorMapping( "c", 'c', "continuous", true, false ) );
  }

  static SessionBean1_1 multi = new SessionBean1_1();
  static {
    multi.duration = 10000;
    multi.discreteEvents = Lists.newArrayList();
    multi.continuousEvents = Lists.newArrayList();

    multi.discreteEvents.add( new DiscreteEvent( "event-one", "a", 0 ) );
    multi.discreteEvents.add( new DiscreteEvent( "event-two", "a", 1000 ) );
    multi.discreteEvents.add( new DiscreteEvent( "event-three", "a", 1100 ) );
    multi.discreteEvents.add( new DiscreteEvent( "event-four", "a", 3000 ) );
    multi.discreteEvents.add( new DiscreteEvent( "event-five", "a", 3100 ) );
    multi.discreteEvents.add( new DiscreteEvent( "event-six", "a", 7000 ) );
    multi.discreteEvents.add( new DiscreteEvent( "event-seven", "a", 8000 ) );
    multi.discreteEvents.add( new DiscreteEvent( "event-eight", "b", 2000 ) );
    multi.discreteEvents.add( new DiscreteEvent( "event-nine", "b", 2100 ) );
    multi.discreteEvents.add( new DiscreteEvent( "event-ten", "b", 4000 ) );
    multi.discreteEvents.add( new DiscreteEvent( "event-eleven", "b", 9000 ) );
    multi.continuousEvents.add( new ContinuousEvent( "event-twelve", "c", 3000, 6100 ) );
    multi.continuousEvents.add( new ContinuousEvent( "event-thirteen", "d", 5000, 8100 ) );

    multi.schema = new SchemaVersion();
    multi.schema.behaviors = Lists.newArrayList();
    multi.schema.behaviors.add( new KeyBehaviorMapping( "a", 'a', "apple", false, false ) );
    multi.schema.behaviors.add( new KeyBehaviorMapping( "b", 'b', "banana", false, false ) );
    multi.schema.behaviors.add( new KeyBehaviorMapping( "c", 'c', "cucumber", true, false ) );
    multi.schema.behaviors.add( new KeyBehaviorMapping( "d", 'd', "date", true, false ) );
  }

  static SessionBean1_1 zero_len = new SessionBean1_1();
  static {
    zero_len.duration = 0;
    zero_len.discreteEvents = Lists.newArrayList();
    zero_len.continuousEvents = Lists.newArrayList();
    zero_len.schema = new SchemaVersion();
    zero_len.schema.behaviors = Lists.newArrayList();
    zero_len.schema.behaviors.add( new KeyBehaviorMapping( "d", 'd', "discrete", false, false ) );
  }

  @Test public void deserialize_standard() throws Exception
  {
    URL url = IoaUtilsTest.class.getResource( "test-0.json" );
    File f = new File( url.toURI() );
    SessionBean1_1 bean = GsonUtils.get( f, new SessionBean1_1() );

    assertEquals( standard.duration, bean.duration );
    assertEquals( standard.discreteEvents, bean.discreteEvents );
    assertEquals( standard.continuousEvents, bean.continuousEvents );
  }

  @Test public void deserialize_empty() throws Exception
  {
    URL url = IoaUtilsTest.class.getResource( "test-empty.json" );
    File f = new File( url.toURI() );
    SessionBean1_1 bean = GsonUtils.get( f, new SessionBean1_1() );

    assertEquals( empty.duration, bean.duration );
    assertEquals( empty.discreteEvents, bean.discreteEvents );
    assertEquals( empty.continuousEvents, bean.continuousEvents );
  }

  @Test public void deserialize_multi() throws Exception
  {
    URL url = IoaUtilsTest.class.getResource( "test-1.json" );
    File f = new File( url.toURI() );
    SessionBean1_1 bean = GsonUtils.get( f, new SessionBean1_1() );

    assertEquals( multi.duration, bean.duration );
    assertEquals( multi.discreteEvents, bean.discreteEvents );
    assertEquals( multi.continuousEvents, bean.continuousEvents );
  }

  @Test public void partition_standard_blockSize_1()
  {
    int blockSize = 1;

    HashMap< String, ArrayList< Integer > > mapD = Maps.newHashMap();
    HashMap< String, ArrayList< Integer > > mapC = Maps.newHashMap();
    IoaUtils1_1.populateDiscrete( standard, mapD );
    IoaUtils1_1.populateContinuous( standard, mapC );

    KeyToInterval actualDiscrete =
        IoaUtils1_1.partition( mapD, standard.duration, blockSize );
    KeyToInterval actualContinuous =
        IoaUtils1_1.partition( mapC, standard.duration, blockSize );

    HashMap< String, Multiset< Integer > > expectedDMap = Maps.newHashMap();
    HashMap< String, Multiset< Integer > > expectedCMap = Maps.newHashMap();

    HashMultiset< Integer > expectedDTimes = HashMultiset.create();
    expectedDMap.put( "d", expectedDTimes );
    expectedDTimes.add( 0 );
    expectedDTimes.add( 1 );
    expectedDTimes.add( 1 );
    expectedDTimes.add( 3 );
    expectedDTimes.add( 3 );

    HashMultiset< Integer > expectedCTimes = HashMultiset.create();
    expectedCMap.put( "c", expectedCTimes );
    expectedCTimes.add( 0 );
    expectedCTimes.add( 1 );
    expectedCTimes.add( 3 );

    KeyToInterval expected1 = new KeyToInterval( expectedDMap, 6, blockSize );
    KeyToInterval expected2 = new KeyToInterval( expectedCMap, 6, blockSize );

    assertEquals( expected1, actualDiscrete );
    assertEquals( expected2, actualContinuous );
  }

  @Test public void partition_standard_blockSize_2()
  {
    int blockSize = 2;

    HashMap< String, ArrayList< Integer > > mapD = Maps.newHashMap();
    HashMap< String, ArrayList< Integer > > mapC = Maps.newHashMap();
    IoaUtils1_1.populateDiscrete( standard, mapD );
    IoaUtils1_1.populateContinuous( standard, mapC );

    KeyToInterval actualDiscrete =
        IoaUtils1_1.partition( mapD, standard.duration, blockSize );
    KeyToInterval actualContinuous =
        IoaUtils1_1.partition( mapC, standard.duration, blockSize );

    HashMap< String, Multiset< Integer > > expectedDMap = Maps.newHashMap();
    HashMap< String, Multiset< Integer > > expectedCMap = Maps.newHashMap();

    // 0,1,2,3,4,5
    // 0,0,1,1,2,2

    HashMultiset< Integer > expectedDTimes = HashMultiset.create();
    expectedDMap.put( "d", expectedDTimes );
    expectedDTimes.add( 0 );
    expectedDTimes.add( 0 );
    expectedDTimes.add( 0 );
    expectedDTimes.add( 1 );
    expectedDTimes.add( 1 );

    HashMultiset< Integer > expectedCTimes = HashMultiset.create();
    expectedCMap.put( "c", expectedCTimes );
    expectedCTimes.add( 0 );
    expectedCTimes.add( 0 );
    expectedCTimes.add( 1 );

    KeyToInterval expected1 = new KeyToInterval( expectedDMap, 3, blockSize );
    KeyToInterval expected2 = new KeyToInterval( expectedCMap, 3, blockSize );

    assertEquals( expected1, actualDiscrete );
    assertEquals( expected2, actualContinuous );
  }

  @Test public void partition_standard_blockSize_4()
  {
    int blockSize = 4;

    HashMap< String, ArrayList< Integer > > mapD = Maps.newHashMap();
    HashMap< String, ArrayList< Integer > > mapC = Maps.newHashMap();
    IoaUtils1_1.populateDiscrete( standard, mapD );
    IoaUtils1_1.populateContinuous( standard, mapC );

    KeyToInterval actualDiscrete =
        IoaUtils1_1.partition( mapD, standard.duration, blockSize );
    KeyToInterval actualContinuous =
        IoaUtils1_1.partition( mapC, standard.duration, blockSize );

    HashMap< String, Multiset< Integer > > expectedDMap = Maps.newHashMap();
    HashMap< String, Multiset< Integer > > expectedCMap = Maps.newHashMap();

    // 0,1,2,3,4,5
    // 0,0,0,0,1,1

    HashMultiset< Integer > expectedDTimes = HashMultiset.create();
    expectedDMap.put( "d", expectedDTimes );
    expectedDTimes.add( 0 );
    expectedDTimes.add( 0 );
    expectedDTimes.add( 0 );
    expectedDTimes.add( 0 );
    expectedDTimes.add( 0 );

    HashMultiset< Integer > expectedCTimes = HashMultiset.create();
    expectedCMap.put( "c", expectedCTimes );
    expectedCTimes.add( 0 );
    expectedCTimes.add( 0 );
    expectedCTimes.add( 0 );

    KeyToInterval expected1 = new KeyToInterval( expectedDMap, 2, blockSize );
    KeyToInterval expected2 = new KeyToInterval( expectedCMap, 2, blockSize );

    assertEquals( expected1, actualDiscrete );
    assertEquals( expected2, actualContinuous );
  }

  @Test public void partition_multi_blockSize_3()
  {
    int blockSize = 3;

    HashMap< String, ArrayList< Integer > > mapD = Maps.newHashMap();
    HashMap< String, ArrayList< Integer > > mapC = Maps.newHashMap();
    IoaUtils1_1.populateDiscrete( multi, mapD );
    IoaUtils1_1.populateContinuous( multi, mapC );

    KeyToInterval actualDiscrete =
        IoaUtils1_1.partition( mapD, multi.duration, blockSize );
    KeyToInterval actualContinuous =
        IoaUtils1_1.partition( mapC, multi.duration, blockSize );

    HashMap< String, Multiset< Integer > > expectedDMap = Maps.newHashMap();
    HashMap< String, Multiset< Integer > > expectedCMap = Maps.newHashMap();

    // 0,1,2,3,4,5,6,7,8,9
    // 0,0,0,1,1,1,2,2,2,3

    HashMultiset< Integer > expectedATimes = HashMultiset.create();
    HashMultiset< Integer > expectedBTimes = HashMultiset.create();
    expectedDMap.put( "a", expectedATimes );
    expectedDMap.put( "b", expectedBTimes );
    expectedATimes.addAll( Lists.newArrayList( 0, 0, 0, 1, 1, 2, 2 ) );
    expectedBTimes.addAll( Lists.newArrayList( 0, 0, 1, 3 ) );

    HashMultiset< Integer > expectedCTimes = HashMultiset.create();
    HashMultiset< Integer > expectedDTimes = HashMultiset.create();
    expectedCMap.put( "c", expectedCTimes );
    expectedCMap.put( "d", expectedDTimes );
    expectedCTimes.addAll( Lists.newArrayList( 1, 1, 1, 2 ) );
    expectedDTimes.addAll( Lists.newArrayList( 1, 2, 2, 2 ) );

    KeyToInterval expected1 = new KeyToInterval( expectedDMap, 4, blockSize );
    KeyToInterval expected2 = new KeyToInterval( expectedCMap, 4, blockSize );

    assertEquals( expected1, actualDiscrete );
    assertEquals( expected2, actualContinuous );
  }

  @Test public void partition_zero_length_blockSize_1()
  {
    int blockSize = 1;

    HashMap< String, ArrayList< Integer > > mapD = Maps.newHashMap();
    HashMap< String, ArrayList< Integer > > mapC = Maps.newHashMap();
    IoaUtils1_1.populateDiscrete( zero_len, mapD );
    IoaUtils1_1.populateContinuous( zero_len, mapC );

    KeyToInterval actualDiscrete =
        IoaUtils1_1.partition( mapD, zero_len.duration, blockSize );
    KeyToInterval actualContinuous =
        IoaUtils1_1.partition( mapC, zero_len.duration, blockSize );

    HashMap< String, Multiset< Integer > > expectedDMap = Maps.newHashMap();
    HashMap< String, Multiset< Integer > > expectedCMap = Maps.newHashMap();
    KeyToInterval expected1 = new KeyToInterval( expectedDMap, 0, blockSize );
    KeyToInterval expected2 = new KeyToInterval( expectedCMap, 0, blockSize );

    assertEquals( expected1, actualDiscrete );
    assertEquals( expected2, actualContinuous );
  }

}
