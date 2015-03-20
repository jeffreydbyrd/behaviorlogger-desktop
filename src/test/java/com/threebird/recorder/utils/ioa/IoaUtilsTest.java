package com.threebird.recorder.utils.ioa;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;

import org.junit.Test;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;

public class IoaUtilsTest
{
  private static String[] standard = new String[] { "cd", "dcd", "", "ddc", "c", "" };

  @Test public void timesToKeys_standard() throws Exception
  {
    URL url = IoaUtilsTest.class.getResource( "test-0.csv" );
    File f = new File( url.toURI() );
    String[] actual = IoaUtils.timesToKeys( f );
    String[] expected = standard;

    assertTrue( Arrays.equals( actual, expected ) );
  }

  @Test public void timesToKeys_empty() throws Exception
  {
    URL url = IoaUtilsTest.class.getResource( "test-empty.csv" );
    File f = new File( url.toURI() );
    String[] actual = IoaUtils.timesToKeys( f );
    String[] expected = new String[0];

    assertTrue( Arrays.equals( actual, expected ) );
  }

  @Test public void mapKeysToInterval_standard()
  {
    String[] input = standard;
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
    String[] input = standard;
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
    String[] input = new String[0];
    int blockSize = 1;
    int expectedNumIntervals = input.length;
    HashMap< Character, Multiset< Integer >> expectedCharToIntervals = Maps.newHashMap();
    KeyToInterval expected = new KeyToInterval( expectedCharToIntervals, expectedNumIntervals );
    KeyToInterval actual = IoaUtils.mapKeysToInterval( input, blockSize );

    assertEquals( expected, actual );
  }
}
