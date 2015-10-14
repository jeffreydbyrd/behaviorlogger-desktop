package com.threebird.recorder.utils.ioa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.threebird.recorder.persistence.RecordingRawJson.SessionBean;

public class IoaCalculationsTest
{
  @Test public void partialAgreement_blocksize_1()
  {
    int blockSize = 1;

    // 0,1,2,3,4,5,6,7,8,9
    // 0,1,2,3,4,5,6,7,8,9

    SessionBean input1 = new SessionBean();
    input1.totalTimeMillis = 2000;
    input1.discretes = Maps.newHashMap();
    input1.continuous = Maps.newHashMap();
    input1.discretes.put( 'd', Lists.newArrayList( 0, 1, 1 ) );
    input1.continuous.put( 'c', Lists.newArrayList( 0, 1 ) );

    SessionBean input2 = new SessionBean();
    input2.totalTimeMillis = 3000;
    input2.discretes = Maps.newHashMap();
    input2.continuous = Maps.newHashMap();
    input2.discretes.put( 'd', Lists.newArrayList( 0, 1 ) );
    input2.continuous.put( 'c', Lists.newArrayList( 0, 1, 2 ) );

    HashMap< Character, ArrayList< Integer >> stream1 = Maps.newHashMap( input1.discretes );
    stream1.putAll( input1.continuous );
    HashMap< Character, ArrayList< Integer >> stream2 = Maps.newHashMap( input2.discretes );
    stream2.putAll( input2.continuous );

    KeyToInterval data1 = IoaUtils.partition( stream1, input1.totalTimeMillis, blockSize );
    KeyToInterval data2 = IoaUtils.partition( stream2, input2.totalTimeMillis, blockSize );

    Map< Character, IntervalCalculations > actual = IoaCalculations.partialAgreement( data1, data2 );
    Map< Character, IntervalCalculations > expected = Maps.newHashMap();

    // . . . . . . . . . . 0, 1, 2
    int[] intervals1_d = { 1, 2, 0 };
    int[] intervals2_d = { 1, 1, 0 };
    double[] result_d = { 1, 0.5, 1 };
    // . . . . . . . . . . 0, 1, 2
    int[] intervals1_c = { 1, 1, 0 };
    int[] intervals2_c = { 1, 1, 1 };
    double[] result_c = { 1, 1, 0 };

    expected.put( 'd', new IntervalCalculations( 'd', intervals1_d, intervals2_d, result_d ) );
    expected.put( 'c', new IntervalCalculations( 'c', intervals1_c, intervals2_c, result_c ) );

    Assert.assertEquals( expected, actual );
  }
  //
  // @Test public void partialAgreement_blocksize2()
  // {
  // int blockSize = 2;
  // List< BehaviorLogRow > input1 =
  // Lists.newArrayList( new BehaviorLogRow( "d", "" ),
  // new BehaviorLogRow( "dd", "" ) );
  // List< BehaviorLogRow > input2 =
  // Lists.newArrayList( new BehaviorLogRow( "", "" ),
  // new BehaviorLogRow( "d", "" ) );
  //
  // KeyToInterval data1 = IoaUtils.mapRowsToInterval( input1, blockSize );
  // KeyToInterval data2 = IoaUtils.mapRowsToInterval( input2, blockSize );
  //
  // Map< Character, IntervalCalculations > actual = IoaCalculations.partialAgreement( data1, data2 );
  // Map< Character, IntervalCalculations > expected = Maps.newHashMap();
  //
  // int[] intervals1 = { 3 };
  // int[] intervals2 = { 1 };
  // double[] result = { 1.0 / 3.0 };
  //
  // expected.put( 'd', new IntervalCalculations( 'd', intervals1, intervals2, result ) );
  //
  // Assert.assertEquals( expected, actual );
  // }
  //
  // @Test public void partialAgreement_blocksizeBig()
  // {
  // int blockSize = 5;
  // List< BehaviorLogRow > input1 =
  // Lists.newArrayList( new BehaviorLogRow( "d", "" ),
  // new BehaviorLogRow( "dd", "" ) );
  // List< BehaviorLogRow > input2 =
  // Lists.newArrayList( new BehaviorLogRow( "", "" ),
  // new BehaviorLogRow( "d", "" ) );
  //
  // KeyToInterval data1 = IoaUtils.mapRowsToInterval( input1, blockSize );
  // KeyToInterval data2 = IoaUtils.mapRowsToInterval( input2, blockSize );
  //
  // Map< Character, IntervalCalculations > actual = IoaCalculations.partialAgreement( data1, data2 );
  // Map< Character, IntervalCalculations > expected = Maps.newHashMap();
  //
  // int[] intervals1 = { 3 };
  // int[] intervals2 = { 1 };
  // double[] result = { 1.0 / 3.0 };
  //
  // expected.put( 'd', new IntervalCalculations( 'd', intervals1, intervals2, result ) );
  //
  // Assert.assertEquals( expected, actual );
  // }
  //
  // @Test public void partialAgreement_multipleChars()
  // {
  // int blockSize = 1;
  // List< BehaviorLogRow > input1 =
  // Lists.newArrayList( new BehaviorLogRow( "d", "" ),
  // new BehaviorLogRow( "dd", "c" ) );
  // List< BehaviorLogRow > input2 =
  // Lists.newArrayList( new BehaviorLogRow( "", "c" ),
  // new BehaviorLogRow( "d", "c" ) );
  //
  // KeyToInterval data1 = IoaUtils.mapRowsToInterval( input1, blockSize );
  // KeyToInterval data2 = IoaUtils.mapRowsToInterval( input2, blockSize );
  //
  // Map< Character, IntervalCalculations > actual = IoaCalculations.partialAgreement( data1, data2 );
  // Map< Character, IntervalCalculations > expected = Maps.newHashMap();
  //
  // int[] dintervals1 = { 1, 2 };
  // int[] dintervals2 = { 0, 1 };
  // double[] dresult = { 0, .5 };
  // expected.put( 'd', new IntervalCalculations( 'd', dintervals1, dintervals2, dresult ) );
  //
  // int[] cintervals1 = { 0, 1 };
  // int[] cintervals2 = { 1, 1 };
  // double[] cresult = { 0, 1 };
  // expected.put( 'c', new IntervalCalculations( 'c', cintervals1, cintervals2, cresult ) );
  //
  // Assert.assertEquals( expected, actual );
  // }
  //
  // @Test public void partialAgreement_blockSize1_uneven()
  // {
  // int blockSize = 1;
  // List< BehaviorLogRow > input1 =
  // Lists.newArrayList( new BehaviorLogRow( "d", "" ),
  // new BehaviorLogRow( "dd", "" ) );
  // List< BehaviorLogRow > input2 =
  // Lists.newArrayList( new BehaviorLogRow( "", "" ),
  // new BehaviorLogRow( "d", "" ),
  // new BehaviorLogRow( "d", "" ),
  // new BehaviorLogRow( "", "" ) );
  //
  // KeyToInterval data1 = IoaUtils.mapRowsToInterval( input1, blockSize );
  // KeyToInterval data2 = IoaUtils.mapRowsToInterval( input2, blockSize );
  //
  // Map< Character, IntervalCalculations > actual = IoaCalculations.partialAgreement( data1, data2 );
  // Map< Character, IntervalCalculations > expected = Maps.newHashMap();
  //
  // int[] dintervals1 = { 1, 2, 0, 0 };
  // int[] dintervals2 = { 0, 1, 1, 0 };
  // double[] dresult = { 0, .5, 0, 1 };
  // expected.put( 'd', new IntervalCalculations( 'd', dintervals1, dintervals2, dresult ) );
  //
  // Assert.assertEquals( expected, actual );
  // }
  //
  // @Test public void partialAgreement_imperfectPartitions()
  // {
  // int blockSize = 3;
  // List< BehaviorLogRow > input1 =
  // Lists.newArrayList( new BehaviorLogRow( "d", "" ),
  // new BehaviorLogRow( "dd", "" ) );
  // List< BehaviorLogRow > input2 =
  // Lists.newArrayList( new BehaviorLogRow( "", "" ),
  // new BehaviorLogRow( "d", "" ),
  // new BehaviorLogRow( "d", "" ),
  // new BehaviorLogRow( "", "" ) );
  //
  // KeyToInterval data1 = IoaUtils.mapRowsToInterval( input1, blockSize );
  // KeyToInterval data2 = IoaUtils.mapRowsToInterval( input2, blockSize );
  //
  // Map< Character, IntervalCalculations > actual = IoaCalculations.partialAgreement( data1, data2 );
  // Map< Character, IntervalCalculations > expected = Maps.newHashMap();
  //
  // int[] dintervals1 = { 3, 0 };
  // int[] dintervals2 = { 2, 0 };
  // double[] dresult = { 2.0 / 3.0, 1 };
  // expected.put( 'd', new IntervalCalculations( 'd', dintervals1, dintervals2, dresult ) );
  //
  // Assert.assertEquals( expected, actual );
  // }
  //
  // @Test public void partialAgreement_empty()
  // {
  // int blockSize = 1;
  // List< BehaviorLogRow > input1 = Lists.newArrayList();
  // List< BehaviorLogRow > input2 = Lists.newArrayList();
  //
  // KeyToInterval data1 = IoaUtils.mapRowsToInterval( input1, blockSize );
  // KeyToInterval data2 = IoaUtils.mapRowsToInterval( input2, blockSize );
  //
  // Map< Character, IntervalCalculations > actual = IoaCalculations.partialAgreement( data1, data2 );
  // Map< Character, IntervalCalculations > expected = Maps.newHashMap();
  //
  // Assert.assertEquals( expected, actual );
  // }
  //
  // @Test public void exactAgreement_blocksize1_half_agree()
  // {
  // int blockSize = 1;
  // List< BehaviorLogRow > input1 =
  // Lists.newArrayList( new BehaviorLogRow( "d", "" ),
  // new BehaviorLogRow( "dd", "" ) );
  // List< BehaviorLogRow > input2 =
  // Lists.newArrayList( new BehaviorLogRow( "d", "" ),
  // new BehaviorLogRow( "d", "" ) );
  //
  // KeyToInterval data1 = IoaUtils.mapRowsToInterval( input1, blockSize );
  // KeyToInterval data2 = IoaUtils.mapRowsToInterval( input2, blockSize );
  //
  // Map< Character, IntervalCalculations > actual = IoaCalculations.exactAgreement( data1, data2 );
  // Map< Character, IntervalCalculations > expected = Maps.newHashMap();
  //
  // int[] intervals1 = { 1, 2 };
  // int[] intervals2 = { 1, 1 };
  // double[] result = { 1, 0 };
  //
  // expected.put( 'd', new IntervalCalculations( 'd', intervals1, intervals2, result ) );
  //
  // Assert.assertEquals( expected, actual );
  // }
  //
  // @Test public void exactAgreement_blocksize2__half_agree()
  // {
  // int blockSize = 2;
  // List< BehaviorLogRow > input1 =
  // Lists.newArrayList( new BehaviorLogRow( "d", "" ),
  // new BehaviorLogRow( "dd", "" ) );
  // List< BehaviorLogRow > input2 =
  // Lists.newArrayList( new BehaviorLogRow( "d", "" ),
  // new BehaviorLogRow( "d", "" ) );
  //
  // KeyToInterval data1 = IoaUtils.mapRowsToInterval( input1, blockSize );
  // KeyToInterval data2 = IoaUtils.mapRowsToInterval( input2, blockSize );
  //
  // Map< Character, IntervalCalculations > actual = IoaCalculations.exactAgreement( data1, data2 );
  // Map< Character, IntervalCalculations > expected = Maps.newHashMap();
  //
  // int[] intervals1 = { 3 };
  // int[] intervals2 = { 2 };
  // double[] result = { 0 };
  //
  // expected.put( 'd', new IntervalCalculations( 'd', intervals1, intervals2, result ) );
  //
  // Assert.assertEquals( expected, actual );
  // }
  //
  // @Test public void exactAgreement_blocksizeBig__half_agree()
  // {
  // int blockSize = 5;
  // List< BehaviorLogRow > input1 =
  // Lists.newArrayList( new BehaviorLogRow( "d", "" ),
  // new BehaviorLogRow( "dd", "" ) );
  // List< BehaviorLogRow > input2 =
  // Lists.newArrayList( new BehaviorLogRow( "d", "" ),
  // new BehaviorLogRow( "d", "" ) );
  //
  // KeyToInterval data1 = IoaUtils.mapRowsToInterval( input1, blockSize );
  // KeyToInterval data2 = IoaUtils.mapRowsToInterval( input2, blockSize );
  //
  // Map< Character, IntervalCalculations > actual = IoaCalculations.exactAgreement( data1, data2 );
  // Map< Character, IntervalCalculations > expected = Maps.newHashMap();
  //
  // int[] intervals1 = { 3 };
  // int[] intervals2 = { 2 };
  // double[] result = { 0 };
  //
  // expected.put( 'd', new IntervalCalculations( 'd', intervals1, intervals2, result ) );
  //
  // Assert.assertEquals( expected, actual );
  // }
  //
  // @Test public void exactAgreement_multipleChars()
  // {
  // int blockSize = 1;
  // List< BehaviorLogRow > input1 =
  // Lists.newArrayList( new BehaviorLogRow( "d", "" ),
  // new BehaviorLogRow( "dd", "c" ) );
  // List< BehaviorLogRow > input2 =
  // Lists.newArrayList( new BehaviorLogRow( "", "c" ),
  // new BehaviorLogRow( "d", "c" ) );
  //
  // KeyToInterval data1 = IoaUtils.mapRowsToInterval( input1, blockSize );
  // KeyToInterval data2 = IoaUtils.mapRowsToInterval( input2, blockSize );
  //
  // Map< Character, IntervalCalculations > actual = IoaCalculations.exactAgreement( data1, data2 );
  // Map< Character, IntervalCalculations > expected = Maps.newHashMap();
  //
  // int[] dintervals1 = { 1, 2 };
  // int[] dintervals2 = { 0, 1 };
  // double[] dresult = { 0, 0 };
  // expected.put( 'd', new IntervalCalculations( 'd', dintervals1, dintervals2, dresult ) );
  //
  // int[] cintervals1 = { 0, 1 };
  // int[] cintervals2 = { 1, 1 };
  // double[] cresult = { 0, 1 };
  // expected.put( 'c', new IntervalCalculations( 'c', cintervals1, cintervals2, cresult ) );
  //
  // Assert.assertEquals( expected, actual );
  // }
  //
  // @Test public void exactAgreement_blockSize1_uneven()
  // {
  // int blockSize = 1;
  // List< BehaviorLogRow > input1 =
  // Lists.newArrayList( new BehaviorLogRow( "d", "" ),
  // new BehaviorLogRow( "dd", "" ) );
  // List< BehaviorLogRow > input2 =
  // Lists.newArrayList( new BehaviorLogRow( "d", "" ),
  // new BehaviorLogRow( "d", "" ),
  // new BehaviorLogRow( "d", "" ),
  // new BehaviorLogRow( "", "" ) );
  //
  // KeyToInterval data1 = IoaUtils.mapRowsToInterval( input1, blockSize );
  // KeyToInterval data2 = IoaUtils.mapRowsToInterval( input2, blockSize );
  //
  // Map< Character, IntervalCalculations > actual = IoaCalculations.exactAgreement( data1, data2 );
  // Map< Character, IntervalCalculations > expected = Maps.newHashMap();
  //
  // int[] dintervals1 = { 1, 2, 0, 0 };
  // int[] dintervals2 = { 1, 1, 1, 0 };
  // double[] dresult = { 1, 0, 0, 1 };
  // expected.put( 'd', new IntervalCalculations( 'd', dintervals1, dintervals2, dresult ) );
  //
  // Assert.assertEquals( expected, actual );
  // }
  //
  // @Test public void exactAgreement_imperfectPartitions()
  // {
  // int blockSize = 3;
  // List< BehaviorLogRow > input1 =
  // Lists.newArrayList( new BehaviorLogRow( "d", "" ),
  // new BehaviorLogRow( "dd", "" ) );
  // List< BehaviorLogRow > input2 =
  // Lists.newArrayList( new BehaviorLogRow( "", "" ),
  // new BehaviorLogRow( "d", "" ),
  // new BehaviorLogRow( "d", "" ),
  // new BehaviorLogRow( "", "" ) );
  //
  // KeyToInterval data1 = IoaUtils.mapRowsToInterval( input1, blockSize );
  // KeyToInterval data2 = IoaUtils.mapRowsToInterval( input2, blockSize );
  //
  // Map< Character, IntervalCalculations > actual = IoaCalculations.exactAgreement( data1, data2 );
  // Map< Character, IntervalCalculations > expected = Maps.newHashMap();
  //
  // int[] dintervals1 = { 3, 0 };
  // int[] dintervals2 = { 2, 0 };
  // double[] dresult = { 0, 1 };
  // expected.put( 'd', new IntervalCalculations( 'd', dintervals1, dintervals2, dresult ) );
  //
  // Assert.assertEquals( expected, actual );
  // }
  //
  // @Test public void exactAgreement_empty()
  // {
  // int blockSize = 1;
  // List< BehaviorLogRow > input1 = Lists.newArrayList();
  // List< BehaviorLogRow > input2 = Lists.newArrayList();
  //
  // KeyToInterval data1 = IoaUtils.mapRowsToInterval( input1, blockSize );
  // KeyToInterval data2 = IoaUtils.mapRowsToInterval( input2, blockSize );
  //
  // Map< Character, IntervalCalculations > actual = IoaCalculations.exactAgreement( data1, data2 );
  // Map< Character, IntervalCalculations > expected = Maps.newHashMap();
  //
  // Assert.assertEquals( expected, actual );
  // }
  //
  // @Test public void windowAgreementDiscrete_blockSize0()
  // {
  // int blockSize = 0;
  // List< BehaviorLogRow > input1 =
  // Lists.newArrayList( new BehaviorLogRow( "d", "" ),
  // new BehaviorLogRow( "dd", "" ) );
  // List< BehaviorLogRow > input2 =
  // Lists.newArrayList( new BehaviorLogRow( "d", "" ),
  // new BehaviorLogRow( "d", "" ) );
  //
  // KeyToInterval data1 = IoaUtils.mapRowsToInterval( input1, 1 );
  // KeyToInterval data2 = IoaUtils.mapRowsToInterval( input2, 1 );
  //
  // Map< Character, TimeWindowCalculations > actual = IoaCalculations.windowAgreementDiscrete( data1, data2, blockSize
  // );
  // Map< Character, TimeWindowCalculations > expected = Maps.newHashMap();
  //
  // double result1 = 2.0 / 3.0;
  // double result2 = 2.0 / 2.0;
  // TimeWindowCalculations expectedD = new TimeWindowCalculations( result1, result2 );
  //
  // expected.put( 'd', expectedD );
  //
  // Assert.assertEquals( expected, actual );
  // }
  //
  // @Test public void windowAgreementDiscrete_blockSize1()
  // {
  // int blockSize = 1;
  // List< BehaviorLogRow > input1 =
  // Lists.newArrayList( new BehaviorLogRow( "d", "" ),
  // new BehaviorLogRow( "dd", "" ),
  // new BehaviorLogRow( "", "" ),
  // new BehaviorLogRow( "dd", "" ) );
  // List< BehaviorLogRow > input2 =
  // Lists.newArrayList( new BehaviorLogRow( "d", "" ),
  // new BehaviorLogRow( "d", "" ),
  // new BehaviorLogRow( "dd", "" ) );
  //
  // KeyToInterval data1 = IoaUtils.mapRowsToInterval( input1, 1 );
  // KeyToInterval data2 = IoaUtils.mapRowsToInterval( input2, 1 );
  //
  // Map< Character, TimeWindowCalculations > actual = IoaCalculations.windowAgreementDiscrete( data1, data2, blockSize
  // );
  // Map< Character, TimeWindowCalculations > expected = Maps.newHashMap();
  //
  // double result1 = 4.0 / 5.0;
  // double result2 = 4.0 / 4.0;
  // TimeWindowCalculations expectedD = new TimeWindowCalculations( result1, result2 );
  //
  // expected.put( 'd', expectedD );
  //
  // Assert.assertEquals( expected, actual );
  // }
  //
  // @Test public void windowAgreementDiscrete_blockSizeBig()
  // {
  // int blockSize = 5;
  // List< BehaviorLogRow > input1 =
  // Lists.newArrayList( new BehaviorLogRow( "dddddd", "" ) );
  // List< BehaviorLogRow > input2 =
  // Lists.newArrayList( new BehaviorLogRow( "d", "" ),
  // new BehaviorLogRow( "d", "" ),
  // new BehaviorLogRow( "dd", "" ),
  // new BehaviorLogRow( "", "" ),
  // new BehaviorLogRow( "d", "" ),
  // new BehaviorLogRow( "d", "" ) );
  //
  // KeyToInterval data1 = IoaUtils.mapRowsToInterval( input1, 1 );
  // KeyToInterval data2 = IoaUtils.mapRowsToInterval( input2, 1 );
  //
  // Map< Character, TimeWindowCalculations > actual = IoaCalculations.windowAgreementDiscrete( data1, data2, blockSize
  // );
  // Map< Character, TimeWindowCalculations > expected = Maps.newHashMap();
  //
  // double result1 = 1;
  // double result2 = 1;
  // TimeWindowCalculations expectedD = new TimeWindowCalculations( result1, result2 );
  //
  // expected.put( 'd', expectedD );
  //
  // Assert.assertEquals( expected, actual );
  // }
  //
  // @Test public void windowAgreementDiscrete_empty()
  // {
  // int blockSize = 0;
  // List< BehaviorLogRow > input1 = Lists.newArrayList();
  // List< BehaviorLogRow > input2 = Lists.newArrayList();
  //
  // KeyToInterval data1 = IoaUtils.mapRowsToInterval( input1, 1 );
  // KeyToInterval data2 = IoaUtils.mapRowsToInterval( input2, 1 );
  //
  // Map< Character, TimeWindowCalculations > actual = IoaCalculations.windowAgreementDiscrete( data1, data2, blockSize
  // );
  // Map< Character, TimeWindowCalculations > expected = Maps.newHashMap();
  //
  // Assert.assertEquals( expected, actual );
  // }
  //
  // @Test public void windowAgreementContinuous()
  // {
  // List< BehaviorLogRow > input1 =
  // Lists.newArrayList( new BehaviorLogRow( "", "c" ),
  // new BehaviorLogRow( "", "ct" ),
  // new BehaviorLogRow( "", "ct" ),
  // new BehaviorLogRow( "", "" ),
  // new BehaviorLogRow( "", "" ),
  // new BehaviorLogRow( "", "t" ) );
  //
  // List< BehaviorLogRow > input2 =
  // Lists.newArrayList( new BehaviorLogRow( "", "c" ),
  // new BehaviorLogRow( "", "ct" ),
  // new BehaviorLogRow( "", "ct" ),
  // new BehaviorLogRow( "", "t" ),
  // new BehaviorLogRow( "", "c" ) );
  //
  // KeyToInterval data1 = IoaUtils.mapRowsToInterval( input1, 1 );
  // KeyToInterval data2 = IoaUtils.mapRowsToInterval( input2, 1 );
  //
  // Map< Character, Double > actual = IoaCalculations.windowAgreementContinuous( data1, data2 );
  // Map< Character, Double > expected = Maps.newHashMap();
  // expected.put( 'c', 3.0 / 4.0 );
  // expected.put( 't', 2.0 / 4.0 );
  //
  // Assert.assertEquals( expected, actual );
  // }
}
