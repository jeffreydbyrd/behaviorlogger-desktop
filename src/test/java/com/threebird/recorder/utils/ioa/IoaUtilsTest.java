package com.threebird.recorder.utils.ioa;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;

public class IoaUtilsTest
{
  List< BehaviorLogRow > standard =
      Lists.newArrayList( new BehaviorLogRow( "d", "c" ),
                          new BehaviorLogRow( "dd", "c" ),
                          new BehaviorLogRow( "", "" ),
                          new BehaviorLogRow( "dd", "c" ),
                          new BehaviorLogRow( "", "c" ),
                          new BehaviorLogRow( "", "" ) );

  @Test public void timesToKeys_standard() throws Exception
  {
    URL url = IoaUtilsTest.class.getResource( "test-0.csv" );
    File f = new File( url.toURI() );
    List< BehaviorLogRow > actual = IoaUtils.deserialize( f );
    List< BehaviorLogRow > expected = standard;

    assertEquals( expected, actual );
  }

  @Test public void timesToKeys_empty() throws Exception
  {
    URL url = IoaUtilsTest.class.getResource( "test-empty.csv" );
    File f = new File( url.toURI() );
    List< BehaviorLogRow > actual = IoaUtils.deserialize( f );
    List< BehaviorLogRow > expected = Lists.newArrayList();

    assertEquals( expected, actual );
  }

  @Test public void mapKeysToInterval_standard()
  {
    List< BehaviorLogRow > input = standard;
    int blockSize = 1;

    int expectedNumIntervals = 6;

    HashMultiset< Integer > expectedCTimes = HashMultiset.create();
    expectedCTimes.add( 0 );
    expectedCTimes.add( 1 );
    expectedCTimes.add( 3 );
    expectedCTimes.add( 4 );

    HashMultiset< Integer > expectedDTimes = HashMultiset.create();
    expectedDTimes.add( 0 );
    expectedDTimes.add( 1 );
    expectedDTimes.add( 1 );
    expectedDTimes.add( 3 );
    expectedDTimes.add( 3 );

    HashMap< Character, Multiset< Integer >> expectedCharToIntervals = Maps.newHashMap();
    expectedCharToIntervals.put( 'c', expectedCTimes );
    expectedCharToIntervals.put( 'd', expectedDTimes );

    KeyToInterval expected = new KeyToInterval( expectedCharToIntervals, expectedNumIntervals );
    KeyToInterval actual = IoaUtils.mapKeysToInterval( input, blockSize );

    assertEquals( expected, actual );
  }

  @Test public void mapKeysToInterval_blockSize_4()
  {
    List< BehaviorLogRow > input = standard;
    int blockSize = 4;

    int expectedNumIntervals = 2;

    HashMultiset< Integer > expectedCTimes = HashMultiset.create();
    expectedCTimes.add( 0 );
    expectedCTimes.add( 0 );
    expectedCTimes.add( 0 );
    expectedCTimes.add( 1 );

    HashMultiset< Integer > expectedDTimes = HashMultiset.create();
    expectedDTimes.add( 0 );
    expectedDTimes.add( 0 );
    expectedDTimes.add( 0 );
    expectedDTimes.add( 0 );
    expectedDTimes.add( 0 );

    HashMap< Character, Multiset< Integer >> expectedCharToIntervals = Maps.newHashMap();
    expectedCharToIntervals.put( 'c', expectedCTimes );
    expectedCharToIntervals.put( 'd', expectedDTimes );

    KeyToInterval expected = new KeyToInterval( expectedCharToIntervals, expectedNumIntervals );
    KeyToInterval actual = IoaUtils.mapKeysToInterval( input, blockSize );

    assertEquals( expected, actual );
  }

  @Test public void mapKeysToInterval_empty()
  {
    List< BehaviorLogRow > input = Lists.newArrayList();
    int blockSize = 1;
    int expectedNumIntervals = input.size();
    HashMap< Character, Multiset< Integer >> expectedCharToIntervals = Maps.newHashMap();
    KeyToInterval expected = new KeyToInterval( expectedCharToIntervals, expectedNumIntervals );
    KeyToInterval actual = IoaUtils.mapKeysToInterval( input, blockSize );

    assertEquals( expected, actual );
  }
}
