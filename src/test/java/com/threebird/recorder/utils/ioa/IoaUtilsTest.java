package com.threebird.recorder.utils.ioa;

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
import com.threebird.recorder.persistence.GsonUtils;
import com.threebird.recorder.persistence.recordings.StartEndTimes;
import com.threebird.recorder.persistence.recordings.RecordingRawJson1_1.BehaviorBean1_1;
import com.threebird.recorder.persistence.recordings.RecordingRawJson1_1.SchemaBean1_1;
import com.threebird.recorder.persistence.recordings.RecordingRawJson1_1.SessionBean1_1;

public class IoaUtilsTest
{
  static SessionBean1_1 standard = new SessionBean1_1();
  static {
    standard.totalTimeMillis = 6000;
    standard.discreteEvents = Maps.newHashMap();
    standard.continuousEvents = Maps.newHashMap();
    standard.discreteEvents.put( "d", Lists.newArrayList( 0, 1000, 1100, 3000, 3100 ) );
    standard.continuousEvents.put( "c",
                                   Lists.newArrayList( new StartEndTimes( 0, 1100 ),
                                                       new StartEndTimes( 3100, 3900 ) ) );

    standard.schema = new SchemaBean1_1();
    standard.schema.behaviors = Lists.newArrayList();
    standard.schema.behaviors.add( new BehaviorBean1_1( "d", 'd', "discrete", false ) );
    standard.schema.behaviors.add( new BehaviorBean1_1( "c", 'c', "continuous", true ) );
  }

  static SessionBean1_1 empty = new SessionBean1_1();
  static {
    empty.totalTimeMillis = 6000;
    empty.discreteEvents = Maps.newHashMap();
    empty.continuousEvents = Maps.newHashMap();
    
    empty.schema = new SchemaBean1_1();
    empty.schema.behaviors = Lists.newArrayList();
    empty.schema.behaviors.add( new BehaviorBean1_1( "d", 'd', "discrete", false ) );
    empty.schema.behaviors.add( new BehaviorBean1_1( "c", 'c', "continuous", true ) );
  }

  static SessionBean1_1 multi = new SessionBean1_1();
  static {
    multi.totalTimeMillis = 10000;
    multi.discreteEvents = Maps.newHashMap();
    multi.continuousEvents = Maps.newHashMap();
    multi.discreteEvents.put( "a", Lists.newArrayList( 0, 1000, 1100, 3000, 3100, 7000, 8000 ) );
    multi.discreteEvents.put( "b", Lists.newArrayList( 2000, 2100, 4000, 9000 ) );
    multi.continuousEvents.put( "c", Lists.newArrayList( new StartEndTimes( 3000, 6100 ) ) );
    multi.continuousEvents.put( "d", Lists.newArrayList( new StartEndTimes( 5000, 8100 ) ) );
    
    multi.schema = new SchemaBean1_1();
    multi.schema.behaviors = Lists.newArrayList();
    multi.schema.behaviors.add( new BehaviorBean1_1( "a", 'a', "apple", false ) );
    multi.schema.behaviors.add( new BehaviorBean1_1( "b", 'b', "banana", false ) );
    multi.schema.behaviors.add( new BehaviorBean1_1( "c", 'c', "cucumber", true ) );
    multi.schema.behaviors.add( new BehaviorBean1_1( "d", 'd', "date", true ) );
  }
  
  static SessionBean1_1 zero_len = new SessionBean1_1();
  static {
    zero_len.totalTimeMillis = 0;
    zero_len.discreteEvents = Maps.newHashMap();
    zero_len.continuousEvents = Maps.newHashMap();
    zero_len.schema = new SchemaBean1_1();
    zero_len.schema.behaviors = Lists.newArrayList();
    zero_len.schema.behaviors.add( new BehaviorBean1_1( "d", 'd', "discrete", false ) );
  }

  @Test public void deserialize_standard() throws Exception
  {
    URL url = IoaUtilsTest.class.getResource( "test-0.raw" );
    File f = new File( url.toURI() );
    SessionBean1_1 bean = GsonUtils.get( f, new SessionBean1_1() );

    assertEquals( standard.totalTimeMillis, bean.totalTimeMillis );
    assertEquals( standard.discreteEvents, bean.discreteEvents );
    assertEquals( standard.continuousEvents, bean.continuousEvents );
  }

  @Test public void deserialize_empty() throws Exception
  {
    URL url = IoaUtilsTest.class.getResource( "test-empty.raw" );
    File f = new File( url.toURI() );
    SessionBean1_1 bean = GsonUtils.get( f, new SessionBean1_1() );

    assertEquals( empty.totalTimeMillis, bean.totalTimeMillis );
    assertEquals( empty.discreteEvents, bean.discreteEvents );
    assertEquals( empty.continuousEvents, bean.continuousEvents );
  }

  @Test public void deserialize_multi() throws Exception
  {
    URL url = IoaUtilsTest.class.getResource( "test-1.raw" );
    File f = new File( url.toURI() );
    SessionBean1_1 bean = GsonUtils.get( f, new SessionBean1_1() );

    assertEquals( multi.totalTimeMillis, bean.totalTimeMillis );
    assertEquals( multi.discreteEvents, bean.discreteEvents );
    assertEquals( multi.continuousEvents, bean.continuousEvents );
  }

  @Test public void partition_standard_blockSize_1()
  {
    int blockSize = 1;

    HashMap< String, ArrayList< Integer > > mapD = Maps.newHashMap();
    HashMap< String, ArrayList< Integer > > mapC = Maps.newHashMap();
    IoaUtils.populateDiscrete( standard, mapD );
    IoaUtils.populateContinuous( standard, mapC );

    KeyToInterval actualDiscrete =
        IoaUtils.partition( mapD, standard.totalTimeMillis, blockSize );
    KeyToInterval actualContinuous =
        IoaUtils.partition( mapC, standard.totalTimeMillis, blockSize );

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

    KeyToInterval expected1 = new KeyToInterval( expectedDMap, 6 );
    KeyToInterval expected2 = new KeyToInterval( expectedCMap, 6 );

    assertEquals( expected1, actualDiscrete );
    assertEquals( expected2, actualContinuous );
  }

  @Test public void partition_standard_blockSize_2()
  {
    int blockSize = 2;

    HashMap< String, ArrayList< Integer > > mapD = Maps.newHashMap();
    HashMap< String, ArrayList< Integer > > mapC = Maps.newHashMap();
    IoaUtils.populateDiscrete( standard, mapD );
    IoaUtils.populateContinuous( standard, mapC );

    KeyToInterval actualDiscrete =
        IoaUtils.partition( mapD, standard.totalTimeMillis, blockSize );
    KeyToInterval actualContinuous =
        IoaUtils.partition( mapC, standard.totalTimeMillis, blockSize );

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

    KeyToInterval expected1 = new KeyToInterval( expectedDMap, 3 );
    KeyToInterval expected2 = new KeyToInterval( expectedCMap, 3 );

    assertEquals( expected1, actualDiscrete );
    assertEquals( expected2, actualContinuous );
  }

  @Test public void partition_standard_blockSize_4()
  {
    int blockSize = 4;

    HashMap< String, ArrayList< Integer > > mapD = Maps.newHashMap();
    HashMap< String, ArrayList< Integer > > mapC = Maps.newHashMap();
    IoaUtils.populateDiscrete( standard, mapD );
    IoaUtils.populateContinuous( standard, mapC );

    KeyToInterval actualDiscrete =
        IoaUtils.partition( mapD, standard.totalTimeMillis, blockSize );
    KeyToInterval actualContinuous =
        IoaUtils.partition( mapC, standard.totalTimeMillis, blockSize );

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

    KeyToInterval expected1 = new KeyToInterval( expectedDMap, 2 );
    KeyToInterval expected2 = new KeyToInterval( expectedCMap, 2 );

    assertEquals( expected1, actualDiscrete );
    assertEquals( expected2, actualContinuous );
  }

  @Test public void partition_multi_blockSize_3()
  {
    int blockSize = 3;

    HashMap< String, ArrayList< Integer > > mapD = Maps.newHashMap();
    HashMap< String, ArrayList< Integer > > mapC = Maps.newHashMap();
    IoaUtils.populateDiscrete( multi, mapD );
    IoaUtils.populateContinuous( multi, mapC );

    KeyToInterval actualDiscrete =
        IoaUtils.partition( mapD, multi.totalTimeMillis, blockSize );
    KeyToInterval actualContinuous =
        IoaUtils.partition( mapC, multi.totalTimeMillis, blockSize );

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

    KeyToInterval expected1 = new KeyToInterval( expectedDMap, 4 );
    KeyToInterval expected2 = new KeyToInterval( expectedCMap, 4 );

    assertEquals( expected1, actualDiscrete );
    assertEquals( expected2, actualContinuous );
  }
  
  @Test public void partition_zero_length_blockSize_1()
  {
    int blockSize = 1;

    HashMap< String, ArrayList< Integer > > mapD = Maps.newHashMap();
    HashMap< String, ArrayList< Integer > > mapC = Maps.newHashMap();
    IoaUtils.populateDiscrete( zero_len, mapD );
    IoaUtils.populateContinuous( zero_len, mapC );

    KeyToInterval actualDiscrete =
        IoaUtils.partition( mapD, zero_len.totalTimeMillis, blockSize );
    KeyToInterval actualContinuous =
        IoaUtils.partition( mapC, zero_len.totalTimeMillis, blockSize );

    HashMap< String, Multiset< Integer > > expectedDMap = Maps.newHashMap();
    HashMap< String, Multiset< Integer > > expectedCMap = Maps.newHashMap();
    KeyToInterval expected1 = new KeyToInterval( expectedDMap, 0 );
    KeyToInterval expected2 = new KeyToInterval( expectedCMap, 0 );

    assertEquals( expected1, actualDiscrete );
    assertEquals( expected2, actualContinuous );
  }

}
