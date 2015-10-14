package com.threebird.recorder.utils.ioa;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;
import java.util.HashMap;

import org.junit.Test;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.threebird.recorder.persistence.GsonUtils;
import com.threebird.recorder.persistence.RecordingRawJson.SessionBean;

public class IoaUtilsTest
{
  static SessionBean standard = new SessionBean();
  static {
    standard.totalTimeMillis = 6000;
    standard.discretes = Maps.newHashMap();
    standard.continuous = Maps.newHashMap();
    standard.discretes.put( 'd', Lists.newArrayList( 0, 1, 1, 3, 3 ) );
    standard.continuous.put( 'c', Lists.newArrayList( 0, 1, 3 ) );
  }

  static SessionBean empty = new SessionBean();
  static {
    empty.totalTimeMillis = 6000;
    empty.discretes = Maps.newHashMap();
    empty.continuous = Maps.newHashMap();
  }

  static SessionBean multi = new SessionBean();
  static {
    multi.totalTimeMillis = 10000;
    multi.discretes = Maps.newHashMap();
    multi.continuous = Maps.newHashMap();
    multi.discretes.put( 'a', Lists.newArrayList( 0, 1, 1, 3, 3, 7, 8 ) );
    multi.discretes.put( 'b', Lists.newArrayList( 2, 2, 4, 9 ) );
    multi.continuous.put( 'c', Lists.newArrayList( 3, 4, 5, 6 ) );
    multi.continuous.put( 'd', Lists.newArrayList( 5, 6, 7, 8 ) );
  }

  @Test public void deserialize_standard() throws Exception
  {
    URL url = IoaUtilsTest.class.getResource( "test-0.raw" );
    File f = new File( url.toURI() );
    SessionBean bean = GsonUtils.get( f, new SessionBean() );

    assertEquals( standard.totalTimeMillis, bean.totalTimeMillis );
    assertEquals( standard.discretes, bean.discretes );
    assertEquals( standard.continuous, bean.continuous );
  }

  @Test public void deserialize_empty() throws Exception
  {
    URL url = IoaUtilsTest.class.getResource( "test-empty.raw" );
    File f = new File( url.toURI() );
    SessionBean bean = GsonUtils.get( f, new SessionBean() );

    assertEquals( empty.totalTimeMillis, bean.totalTimeMillis );
    assertEquals( empty.discretes, bean.discretes );
    assertEquals( empty.continuous, bean.continuous );
  }
  
  @Test public void deserialize_multi() throws Exception
  {
    URL url = IoaUtilsTest.class.getResource( "test-1.raw" );
    File f = new File( url.toURI() );
    SessionBean bean = GsonUtils.get( f, new SessionBean() );
    
    assertEquals( multi.totalTimeMillis, bean.totalTimeMillis );
    assertEquals( multi.discretes, bean.discretes );
    assertEquals( multi.continuous, bean.continuous );
  }

  @Test public void partition_standard_blockSize_1()
  {
    int blockSize = 1;
    KeyToInterval actualDiscrete =
        IoaUtils.partition( standard.discretes, standard.totalTimeMillis, blockSize );
    KeyToInterval actualContinuous =
        IoaUtils.partition( standard.continuous, standard.totalTimeMillis, blockSize );

    HashMap< Character, Multiset< Integer >> expectedDMap = Maps.newHashMap();
    HashMap< Character, Multiset< Integer >> expectedCMap = Maps.newHashMap();

    HashMultiset< Integer > expectedDTimes = HashMultiset.create();
    expectedDMap.put( 'd', expectedDTimes );
    expectedDTimes.add( 0 );
    expectedDTimes.add( 1 );
    expectedDTimes.add( 1 );
    expectedDTimes.add( 3 );
    expectedDTimes.add( 3 );

    HashMultiset< Integer > expectedCTimes = HashMultiset.create();
    expectedCMap.put( 'c', expectedCTimes );
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
    KeyToInterval actualDiscrete =
        IoaUtils.partition( standard.discretes, standard.totalTimeMillis, blockSize );
    KeyToInterval actualContinuous =
        IoaUtils.partition( standard.continuous, standard.totalTimeMillis, blockSize );

    HashMap< Character, Multiset< Integer >> expectedDMap = Maps.newHashMap();
    HashMap< Character, Multiset< Integer >> expectedCMap = Maps.newHashMap();

    // 0,1,2,3,4,5
    // 0,0,1,1,2,2

    HashMultiset< Integer > expectedDTimes = HashMultiset.create();
    expectedDMap.put( 'd', expectedDTimes );
    expectedDTimes.add( 0 );
    expectedDTimes.add( 0 );
    expectedDTimes.add( 0 );
    expectedDTimes.add( 1 );
    expectedDTimes.add( 1 );

    HashMultiset< Integer > expectedCTimes = HashMultiset.create();
    expectedCMap.put( 'c', expectedCTimes );
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
    KeyToInterval actualDiscrete =
        IoaUtils.partition( standard.discretes, standard.totalTimeMillis, blockSize );
    KeyToInterval actualContinuous =
        IoaUtils.partition( standard.continuous, standard.totalTimeMillis, blockSize );

    HashMap< Character, Multiset< Integer >> expectedDMap = Maps.newHashMap();
    HashMap< Character, Multiset< Integer >> expectedCMap = Maps.newHashMap();

    // 0,1,2,3,4,5
    // 0,0,0,0,1,1

    HashMultiset< Integer > expectedDTimes = HashMultiset.create();
    expectedDMap.put( 'd', expectedDTimes );
    expectedDTimes.add( 0 );
    expectedDTimes.add( 0 );
    expectedDTimes.add( 0 );
    expectedDTimes.add( 0 );
    expectedDTimes.add( 0 );

    HashMultiset< Integer > expectedCTimes = HashMultiset.create();
    expectedCMap.put( 'c', expectedCTimes );
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
    KeyToInterval actualDiscrete =
        IoaUtils.partition( multi.discretes, multi.totalTimeMillis, blockSize );
    KeyToInterval actualContinuous =
        IoaUtils.partition( multi.continuous, multi.totalTimeMillis, blockSize );

    HashMap< Character, Multiset< Integer >> expectedDMap = Maps.newHashMap();
    HashMap< Character, Multiset< Integer >> expectedCMap = Maps.newHashMap();

    // 0,1,2,3,4,5,6,7,8,9
    // 0,0,0,1,1,1,2,2,2,3
    
    HashMultiset< Integer > expectedATimes = HashMultiset.create();
    HashMultiset< Integer > expectedBTimes = HashMultiset.create();
    expectedDMap.put( 'a', expectedATimes );
    expectedDMap.put( 'b', expectedBTimes );
    expectedATimes.addAll( Lists.newArrayList( 0, 0, 0, 1, 1, 2, 2 ) );
    expectedBTimes.addAll( Lists.newArrayList( 0, 0, 1, 3 ) );

    HashMultiset< Integer > expectedCTimes = HashMultiset.create();
    HashMultiset< Integer > expectedDTimes = HashMultiset.create();
    expectedCMap.put( 'c', expectedCTimes );
    expectedCMap.put( 'd', expectedDTimes );
    expectedCTimes.addAll( Lists.newArrayList( 1, 1, 1, 2 ) );
    expectedDTimes.addAll( Lists.newArrayList( 1, 2, 2, 2 ) );

    KeyToInterval expected1 = new KeyToInterval( expectedDMap, 4 );
    KeyToInterval expected2 = new KeyToInterval( expectedCMap, 4 );

    assertEquals( expected1, actualDiscrete );
    assertEquals( expected2, actualContinuous );
  }
}
