package com.threebird.recorder.utils.ioa;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Maps;

public class IoaCalculationsTest
{
  @Test public void partialAgreement__blocksize1__half_agree()
  {
    int blockSize = 1;
    String[] input1 = new String[] { "d", "dd" };
    String[] input2 = new String[] { "", "d" };

    KeyToInterval data1 = IoaUtils.mapKeysToInterval( input1, blockSize );
    KeyToInterval data2 = IoaUtils.mapKeysToInterval( input2, blockSize );

    Map< Character, IntervalCalculations > actual = IoaCalculations.partialAgreement( data1, data2 );
    Map< Character, IntervalCalculations > expected = Maps.newHashMap();

    int[] intervals1 = { 1, 2 };
    int[] intervals2 = { 0, 1 };
    double[] result = { 0, 0.5 };

    expected.put( 'd', new IntervalCalculations( 'd', intervals1, intervals2, result ) );

    Assert.assertEquals( expected, actual );
  }

  @Test public void partialAgreement__blocksize2__half_agree()
  {
    int blockSize = 2;
    String[] input1 = new String[] { "d", "dd" };
    String[] input2 = new String[] { "", "d" };

    KeyToInterval data1 = IoaUtils.mapKeysToInterval( input1, blockSize );
    KeyToInterval data2 = IoaUtils.mapKeysToInterval( input2, blockSize );

    Map< Character, IntervalCalculations > actual = IoaCalculations.partialAgreement( data1, data2 );
    Map< Character, IntervalCalculations > expected = Maps.newHashMap();

    int[] intervals1 = { 3 };
    int[] intervals2 = { 1 };
    double[] result = { 1.0 / 3.0 };

    expected.put( 'd', new IntervalCalculations( 'd', intervals1, intervals2, result ) );

    Assert.assertEquals( expected, actual );
  }

  @Test public void partialAgreement__blocksizeBig__half_agree()
  {
    int blockSize = 5;
    String[] input1 = new String[] { "d", "dd" };
    String[] input2 = new String[] { "", "d" };

    KeyToInterval data1 = IoaUtils.mapKeysToInterval( input1, blockSize );
    KeyToInterval data2 = IoaUtils.mapKeysToInterval( input2, blockSize );

    Map< Character, IntervalCalculations > actual = IoaCalculations.partialAgreement( data1, data2 );
    Map< Character, IntervalCalculations > expected = Maps.newHashMap();

    int[] intervals1 = { 3 };
    int[] intervals2 = { 1 };
    double[] result = { 1.0 / 3.0 };

    expected.put( 'd', new IntervalCalculations( 'd', intervals1, intervals2, result ) );

    Assert.assertEquals( expected, actual );
  }

  @Test public void partialAgreement_multipleChars()
  {
    int blockSize = 1;
    String[] input1 = new String[] { "d", "cdd" };
    String[] input2 = new String[] { "c", "cd" };

    KeyToInterval data1 = IoaUtils.mapKeysToInterval( input1, blockSize );
    KeyToInterval data2 = IoaUtils.mapKeysToInterval( input2, blockSize );

    Map< Character, IntervalCalculations > actual = IoaCalculations.partialAgreement( data1, data2 );
    Map< Character, IntervalCalculations > expected = Maps.newHashMap();

    int[] dintervals1 = { 1, 2 };
    int[] dintervals2 = { 0, 1 };
    double[] dresult = { 0, .5 };
    expected.put( 'd', new IntervalCalculations( 'd', dintervals1, dintervals2, dresult ) );

    int[] cintervals1 = { 0, 1 };
    int[] cintervals2 = { 1, 1 };
    double[] cresult = { 0, 1 };
    expected.put( 'c', new IntervalCalculations( 'c', cintervals1, cintervals2, cresult ) );

    Assert.assertEquals( expected, actual );
  }

  @Test public void partialAgreement__blockSize1__uneven()
  {
    int blockSize = 1;
    String[] input1 = new String[] { "d", "dd" };
    String[] input2 = new String[] { "", "d", "d", "" };

    KeyToInterval data1 = IoaUtils.mapKeysToInterval( input1, blockSize );
    KeyToInterval data2 = IoaUtils.mapKeysToInterval( input2, blockSize );

    Map< Character, IntervalCalculations > actual = IoaCalculations.partialAgreement( data1, data2 );
    Map< Character, IntervalCalculations > expected = Maps.newHashMap();

    int[] dintervals1 = { 1, 2, 0, 0 };
    int[] dintervals2 = { 0, 1, 1, 0 };
    double[] dresult = { 0, .5, 0, 1 };
    expected.put( 'd', new IntervalCalculations( 'd', dintervals1, dintervals2, dresult ) );

    Assert.assertEquals( expected, actual );
  }

  @Test public void partialAgreement__imperfectPartitions()
  {
    int blockSize = 3;
    String[] input1 = new String[] { "d", "dd" };
    String[] input2 = new String[] { "", "d", "d", "" };

    KeyToInterval data1 = IoaUtils.mapKeysToInterval( input1, blockSize );
    KeyToInterval data2 = IoaUtils.mapKeysToInterval( input2, blockSize );

    Map< Character, IntervalCalculations > actual = IoaCalculations.partialAgreement( data1, data2 );
    Map< Character, IntervalCalculations > expected = Maps.newHashMap();

    int[] dintervals1 = { 3, 0 };
    int[] dintervals2 = { 2, 0 };
    double[] dresult = { 2.0 / 3.0, 1 };
    expected.put( 'd', new IntervalCalculations( 'd', dintervals1, dintervals2, dresult ) );

    Assert.assertEquals( expected, actual );
  }

  @Test public void partialAgreement_empty()
  {
    int blockSize = 1;
    String[] input1 = new String[] {};
    String[] input2 = new String[] {};

    KeyToInterval data1 = IoaUtils.mapKeysToInterval( input1, blockSize );
    KeyToInterval data2 = IoaUtils.mapKeysToInterval( input2, blockSize );

    Map< Character, IntervalCalculations > actual = IoaCalculations.partialAgreement( data1, data2 );
    Map< Character, IntervalCalculations > expected = Maps.newHashMap();

    Assert.assertEquals( expected, actual );
  }

  @Test public void exactAgreement__blocksize1__half_agree()
  {
    int blockSize = 1;
    String[] input1 = new String[] { "d", "dd" };
    String[] input2 = new String[] { "d", "d" };

    KeyToInterval data1 = IoaUtils.mapKeysToInterval( input1, blockSize );
    KeyToInterval data2 = IoaUtils.mapKeysToInterval( input2, blockSize );

    Map< Character, IntervalCalculations > actual = IoaCalculations.exactAgreement( data1, data2 );
    Map< Character, IntervalCalculations > expected = Maps.newHashMap();

    int[] intervals1 = { 1, 2 };
    int[] intervals2 = { 1, 1 };
    double[] result = { 1, 0 };

    expected.put( 'd', new IntervalCalculations( 'd', intervals1, intervals2, result ) );

    Assert.assertEquals( expected, actual );
  }

  @Test public void exactAgreement__blocksize2__half_agree()
  {
    int blockSize = 2;
    String[] input1 = new String[] { "d", "dd" };
    String[] input2 = new String[] { "d", "d" };

    KeyToInterval data1 = IoaUtils.mapKeysToInterval( input1, blockSize );
    KeyToInterval data2 = IoaUtils.mapKeysToInterval( input2, blockSize );

    Map< Character, IntervalCalculations > actual = IoaCalculations.exactAgreement( data1, data2 );
    Map< Character, IntervalCalculations > expected = Maps.newHashMap();

    int[] intervals1 = { 3 };
    int[] intervals2 = { 2 };
    double[] result = { 0 };

    expected.put( 'd', new IntervalCalculations( 'd', intervals1, intervals2, result ) );

    Assert.assertEquals( expected, actual );
  }

  @Test public void exactAgreement__blocksizeBig__half_agree()
  {
    int blockSize = 5;
    String[] input1 = new String[] { "d", "dd" };
    String[] input2 = new String[] { "d", "d" };

    KeyToInterval data1 = IoaUtils.mapKeysToInterval( input1, blockSize );
    KeyToInterval data2 = IoaUtils.mapKeysToInterval( input2, blockSize );

    Map< Character, IntervalCalculations > actual = IoaCalculations.exactAgreement( data1, data2 );
    Map< Character, IntervalCalculations > expected = Maps.newHashMap();

    int[] intervals1 = { 3 };
    int[] intervals2 = { 2 };
    double[] result = { 0 };

    expected.put( 'd', new IntervalCalculations( 'd', intervals1, intervals2, result ) );

    Assert.assertEquals( expected, actual );
  }

  @Test public void exactAgreement_multipleChars()
  {
    int blockSize = 1;
    String[] input1 = new String[] { "d", "cdd" };
    String[] input2 = new String[] { "c", "cd" };

    KeyToInterval data1 = IoaUtils.mapKeysToInterval( input1, blockSize );
    KeyToInterval data2 = IoaUtils.mapKeysToInterval( input2, blockSize );

    Map< Character, IntervalCalculations > actual = IoaCalculations.exactAgreement( data1, data2 );
    Map< Character, IntervalCalculations > expected = Maps.newHashMap();

    int[] dintervals1 = { 1, 2 };
    int[] dintervals2 = { 0, 1 };
    double[] dresult = { 0, 0 };
    expected.put( 'd', new IntervalCalculations( 'd', dintervals1, dintervals2, dresult ) );

    int[] cintervals1 = { 0, 1 };
    int[] cintervals2 = { 1, 1 };
    double[] cresult = { 0, 1 };
    expected.put( 'c', new IntervalCalculations( 'c', cintervals1, cintervals2, cresult ) );

    Assert.assertEquals( expected, actual );
  }

  @Test public void exactAgreement__blockSize1__uneven()
  {
    int blockSize = 1;
    String[] input1 = new String[] { "d", "dd" };
    String[] input2 = new String[] { "d", "d", "d", "" };

    KeyToInterval data1 = IoaUtils.mapKeysToInterval( input1, blockSize );
    KeyToInterval data2 = IoaUtils.mapKeysToInterval( input2, blockSize );

    Map< Character, IntervalCalculations > actual = IoaCalculations.exactAgreement( data1, data2 );
    Map< Character, IntervalCalculations > expected = Maps.newHashMap();

    int[] dintervals1 = { 1, 2, 0, 0 };
    int[] dintervals2 = { 1, 1, 1, 0 };
    double[] dresult = { 1, 0, 0, 1 };
    expected.put( 'd', new IntervalCalculations( 'd', dintervals1, dintervals2, dresult ) );

    Assert.assertEquals( expected, actual );
  }

  @Test public void exactAgreement__imperfectPartitions()
  {
    int blockSize = 3;
    String[] input1 = new String[] { "d", "dd" };
    String[] input2 = new String[] { "", "d", "d", "" };

    KeyToInterval data1 = IoaUtils.mapKeysToInterval( input1, blockSize );
    KeyToInterval data2 = IoaUtils.mapKeysToInterval( input2, blockSize );

    Map< Character, IntervalCalculations > actual = IoaCalculations.exactAgreement( data1, data2 );
    Map< Character, IntervalCalculations > expected = Maps.newHashMap();

    int[] dintervals1 = { 3, 0 };
    int[] dintervals2 = { 2, 0 };
    double[] dresult = { 0, 1 };
    expected.put( 'd', new IntervalCalculations( 'd', dintervals1, dintervals2, dresult ) );

    Assert.assertEquals( expected, actual );
  }

  @Test public void exactAgreement_empty()
  {
    int blockSize = 1;
    String[] input1 = new String[] {};
    String[] input2 = new String[] {};

    KeyToInterval data1 = IoaUtils.mapKeysToInterval( input1, blockSize );
    KeyToInterval data2 = IoaUtils.mapKeysToInterval( input2, blockSize );

    Map< Character, IntervalCalculations > actual = IoaCalculations.exactAgreement( data1, data2 );
    Map< Character, IntervalCalculations > expected = Maps.newHashMap();

    Assert.assertEquals( expected, actual );
  }

  @Test public void windowAgreement_blockSize0()
  {
    int blockSize = 0;
    String[] input1 = new String[] { "d", "dd" };
    String[] input2 = new String[] { "d", "d" };

    KeyToInterval data1 = IoaUtils.mapKeysToInterval( input1, 1 );
    KeyToInterval data2 = IoaUtils.mapKeysToInterval( input2, 1 );

    Map< Character, Double > actual = IoaCalculations.windowAgreement( data1, data2, blockSize );
    Map< Character, Double > expected = Maps.newHashMap();

    double result1 = 2.0 / 3.0;
    double result2 = 2.0 / 2.0;

    expected.put( 'd', (result1 + result2) / 2 );

    Assert.assertEquals( expected, actual );
  }

  @Test public void windowAgreement_blockSize1()
  {
    int blockSize = 1;
    String[] input1 = new String[] { "d", "dd", "", "dd" };
    String[] input2 = new String[] { "d", "d", "dd" };

    KeyToInterval data1 = IoaUtils.mapKeysToInterval( input1, 1 );
    KeyToInterval data2 = IoaUtils.mapKeysToInterval( input2, 1 );

    Map< Character, Double > actual = IoaCalculations.windowAgreement( data1, data2, blockSize );
    Map< Character, Double > expected = Maps.newHashMap();

    double result1 = 4.0 / 5.0;
    double result2 = 4.0 / 4.0;

    expected.put( 'd', (result1 + result2) / 2 );

    Assert.assertEquals( expected, actual );
  }

  @Test public void windowAgreement_blockSizeBig()
  {
    int blockSize = 5;
    String[] input1 = new String[] { "dddddd", };
    String[] input2 = new String[] { "d", "d", "dd", "", "d", "d" };

    KeyToInterval data1 = IoaUtils.mapKeysToInterval( input1, 1 );
    KeyToInterval data2 = IoaUtils.mapKeysToInterval( input2, 1 );

    Map< Character, Double > actual = IoaCalculations.windowAgreement( data1, data2, blockSize );
    Map< Character, Double > expected = Maps.newHashMap();

    expected.put( 'd', 1.0 );

    Assert.assertEquals( expected, actual );
  }

  @Test public void windowAgreement_empty()
  {
    int blockSize = 0;
    String[] input1 = new String[] {};
    String[] input2 = new String[] {};

    KeyToInterval data1 = IoaUtils.mapKeysToInterval( input1, 1 );
    KeyToInterval data2 = IoaUtils.mapKeysToInterval( input2, 1 );

    Map< Character, Double > actual = IoaCalculations.windowAgreement( data1, data2, blockSize );
    Map< Character, Double > expected = Maps.newHashMap();

    Assert.assertEquals( expected, actual );
  }
}
