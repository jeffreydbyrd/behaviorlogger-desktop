package com.threebird.recorder.utils.ioa;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class IoaCalculationsTest
{
  @Test public void partialAgreement__blocksize1()
  {
    int blockSize = 1;
    List< BehaviorLogRow > input1 =
        Lists.newArrayList( new BehaviorLogRow( "d", "" ),
                            new BehaviorLogRow( "dd", "" ) );
    List< BehaviorLogRow > input2 =
        Lists.newArrayList( new BehaviorLogRow( "", "" ),
                            new BehaviorLogRow( "d", "" ) );

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

  @Test public void partialAgreement__blocksize2()
  {
    int blockSize = 2;
    List< BehaviorLogRow > input1 =
        Lists.newArrayList( new BehaviorLogRow( "d", "" ),
                            new BehaviorLogRow( "dd", "" ) );
    List< BehaviorLogRow > input2 =
        Lists.newArrayList( new BehaviorLogRow( "", "" ),
                            new BehaviorLogRow( "d", "" ) );

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

  @Test public void partialAgreement__blocksizeBig()
  {
    int blockSize = 5;
    List< BehaviorLogRow > input1 =
        Lists.newArrayList( new BehaviorLogRow( "d", "" ),
                            new BehaviorLogRow( "dd", "" ) );
    List< BehaviorLogRow > input2 =
        Lists.newArrayList( new BehaviorLogRow( "", "" ),
                            new BehaviorLogRow( "d", "" ) );

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
    List< BehaviorLogRow > input1 =
        Lists.newArrayList( new BehaviorLogRow( "d", "" ),
                            new BehaviorLogRow( "dd", "c" ) );
    List< BehaviorLogRow > input2 =
        Lists.newArrayList( new BehaviorLogRow( "", "c" ),
                            new BehaviorLogRow( "d", "c" ) );

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
    List< BehaviorLogRow > input1 =
        Lists.newArrayList( new BehaviorLogRow( "d", "" ),
                            new BehaviorLogRow( "dd", "" ) );
    List< BehaviorLogRow > input2 =
        Lists.newArrayList( new BehaviorLogRow( "", "" ),
                            new BehaviorLogRow( "d", "" ),
                            new BehaviorLogRow( "d", "" ),
                            new BehaviorLogRow( "", "" ) );

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
    List< BehaviorLogRow > input1 =
        Lists.newArrayList( new BehaviorLogRow( "d", "" ),
                            new BehaviorLogRow( "dd", "" ) );
    List< BehaviorLogRow > input2 =
        Lists.newArrayList( new BehaviorLogRow( "", "" ),
                            new BehaviorLogRow( "d", "" ),
                            new BehaviorLogRow( "d", "" ),
                            new BehaviorLogRow( "", "" ) );

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
    List< BehaviorLogRow > input1 = Lists.newArrayList();
    List< BehaviorLogRow > input2 = Lists.newArrayList();

    KeyToInterval data1 = IoaUtils.mapKeysToInterval( input1, blockSize );
    KeyToInterval data2 = IoaUtils.mapKeysToInterval( input2, blockSize );

    Map< Character, IntervalCalculations > actual = IoaCalculations.partialAgreement( data1, data2 );
    Map< Character, IntervalCalculations > expected = Maps.newHashMap();

    Assert.assertEquals( expected, actual );
  }

  @Test public void exactAgreement__blocksize1__half_agree()
  {
    int blockSize = 1;
    List< BehaviorLogRow > input1 =
        Lists.newArrayList( new BehaviorLogRow( "d", "" ),
                            new BehaviorLogRow( "dd", "" ) );
    List< BehaviorLogRow > input2 =
        Lists.newArrayList( new BehaviorLogRow( "d", "" ),
                            new BehaviorLogRow( "d", "" ) );

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
    List< BehaviorLogRow > input1 =
        Lists.newArrayList( new BehaviorLogRow( "d", "" ),
                            new BehaviorLogRow( "dd", "" ) );
    List< BehaviorLogRow > input2 =
        Lists.newArrayList( new BehaviorLogRow( "d", "" ),
                            new BehaviorLogRow( "d", "" ) );

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
    List< BehaviorLogRow > input1 =
        Lists.newArrayList( new BehaviorLogRow( "d", "" ),
                            new BehaviorLogRow( "dd", "" ) );
    List< BehaviorLogRow > input2 =
        Lists.newArrayList( new BehaviorLogRow( "d", "" ),
                            new BehaviorLogRow( "d", "" ) );

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
    List< BehaviorLogRow > input1 =
        Lists.newArrayList( new BehaviorLogRow( "d", "" ),
                            new BehaviorLogRow( "dd", "c" ) );
    List< BehaviorLogRow > input2 =
        Lists.newArrayList( new BehaviorLogRow( "", "c" ),
                            new BehaviorLogRow( "d", "c" ) );

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
    List< BehaviorLogRow > input1 =
        Lists.newArrayList( new BehaviorLogRow( "d", "" ),
                            new BehaviorLogRow( "dd", "" ) );
    List< BehaviorLogRow > input2 =
        Lists.newArrayList( new BehaviorLogRow( "d", "" ),
                            new BehaviorLogRow( "d", "" ),
                            new BehaviorLogRow( "d", "" ),
                            new BehaviorLogRow( "", "" ) );

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
    List< BehaviorLogRow > input1 =
        Lists.newArrayList( new BehaviorLogRow( "d", "" ),
                            new BehaviorLogRow( "dd", "" ) );
    List< BehaviorLogRow > input2 =
        Lists.newArrayList( new BehaviorLogRow( "", "" ),
                            new BehaviorLogRow( "d", "" ),
                            new BehaviorLogRow( "d", "" ),
                            new BehaviorLogRow( "", "" ) );

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
    List< BehaviorLogRow > input1 = Lists.newArrayList();
    List< BehaviorLogRow > input2 = Lists.newArrayList();

    KeyToInterval data1 = IoaUtils.mapKeysToInterval( input1, blockSize );
    KeyToInterval data2 = IoaUtils.mapKeysToInterval( input2, blockSize );

    Map< Character, IntervalCalculations > actual = IoaCalculations.exactAgreement( data1, data2 );
    Map< Character, IntervalCalculations > expected = Maps.newHashMap();

    Assert.assertEquals( expected, actual );
  }

  @Test public void windowAgreement_blockSize0()
  {
    int blockSize = 0;
    List< BehaviorLogRow > input1 =
        Lists.newArrayList( new BehaviorLogRow( "d", "" ),
                            new BehaviorLogRow( "dd", "" ) );
    List< BehaviorLogRow > input2 =
        Lists.newArrayList( new BehaviorLogRow( "d", "" ),
                            new BehaviorLogRow( "d", "" ) );

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
    List< BehaviorLogRow > input1 =
        Lists.newArrayList( new BehaviorLogRow( "d", "" ),
                            new BehaviorLogRow( "dd", "" ),
                            new BehaviorLogRow( "", "" ),
                            new BehaviorLogRow( "dd", "" ) );
    List< BehaviorLogRow > input2 =
        Lists.newArrayList( new BehaviorLogRow( "d", "" ),
                            new BehaviorLogRow( "d", "" ),
                            new BehaviorLogRow( "dd", "" ) );

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
    List< BehaviorLogRow > input1 =
        Lists.newArrayList( new BehaviorLogRow( "dddddd", "" ) );
    List< BehaviorLogRow > input2 =
        Lists.newArrayList( new BehaviorLogRow( "d", "" ),
                            new BehaviorLogRow( "d", "" ),
                            new BehaviorLogRow( "dd", "" ),
                            new BehaviorLogRow( "", "" ),
                            new BehaviorLogRow( "d", "" ),
                            new BehaviorLogRow( "d", "" ) );

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
    List< BehaviorLogRow > input1 = Lists.newArrayList();
    List< BehaviorLogRow > input2 = Lists.newArrayList();

    KeyToInterval data1 = IoaUtils.mapKeysToInterval( input1, 1 );
    KeyToInterval data2 = IoaUtils.mapKeysToInterval( input2, 1 );

    Map< Character, Double > actual = IoaCalculations.windowAgreement( data1, data2, blockSize );
    Map< Character, Double > expected = Maps.newHashMap();

    Assert.assertEquals( expected, actual );
  }
}
