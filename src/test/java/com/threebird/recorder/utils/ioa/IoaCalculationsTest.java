package com.threebird.recorder.utils.ioa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.threebird.recorder.persistence.RecordingRawJson.SessionBean;

public class IoaCalculationsTest
{
  static SessionBean input1 = new SessionBean();
  static {
    input1.totalTimeMillis = 1700;
    input1.discretes = Maps.newHashMap();
    input1.continuous = Maps.newHashMap();
    input1.discretes.put( 'd', Lists.newArrayList( 0, 1, 1 ) );
    input1.continuous.put( 'c', Lists.newArrayList( 0, 1 ) );
  }

  static SessionBean input2 = new SessionBean();
  static {
    input2.totalTimeMillis = 2700;
    input2.discretes = Maps.newHashMap();
    input2.continuous = Maps.newHashMap();
    input2.discretes.put( 'd', Lists.newArrayList( 0, 1 ) );
    input2.continuous.put( 'c', Lists.newArrayList( 0, 1, 2 ) );
  }

  @Test public void partialAgreement_blocksize_1()
  {
    int blockSize = 1;

    // 0,1,2,3,4,5,6,7,8,9
    // 0,1,2,3,4,5,6,7,8,9

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

  @Test public void partialAgreement_blocksize_2()
  {
    int blockSize = 2;

    HashMap< Character, ArrayList< Integer >> stream1 = Maps.newHashMap( input1.discretes );
    stream1.putAll( input1.continuous );
    HashMap< Character, ArrayList< Integer >> stream2 = Maps.newHashMap( input2.discretes );
    stream2.putAll( input2.continuous );

    KeyToInterval data1 = IoaUtils.partition( stream1, input1.totalTimeMillis, blockSize );
    KeyToInterval data2 = IoaUtils.partition( stream2, input2.totalTimeMillis, blockSize );

    Map< Character, IntervalCalculations > actual = IoaCalculations.partialAgreement( data1, data2 );
    Map< Character, IntervalCalculations > expected = Maps.newHashMap();

    // . . . . . . . . . . 0, 1, 2
    // . . . . . . . . . . 0, 0, 1
    int[] intervals1_d = { 3, 0 };
    int[] intervals2_d = { 2, 0 };
    double[] result_d = { 2.0 / 3.0, 1 };

    int[] intervals1_c = { 2, 0 };
    int[] intervals2_c = { 2, 1 };
    double[] result_c = { 1, 0 };

    expected.put( 'd', new IntervalCalculations( 'd', intervals1_d, intervals2_d, result_d ) );
    expected.put( 'c', new IntervalCalculations( 'c', intervals1_c, intervals2_c, result_c ) );

    Assert.assertEquals( expected, actual );
  }

  @Test public void partialAgreement_blocksize_3()
  {
    int blockSize = 2;

    HashMap< Character, ArrayList< Integer >> stream1 = Maps.newHashMap( input1.discretes );
    stream1.putAll( input1.continuous );
    HashMap< Character, ArrayList< Integer >> stream2 = Maps.newHashMap( input2.discretes );
    stream2.putAll( input2.continuous );

    KeyToInterval data1 = IoaUtils.partition( stream1, input1.totalTimeMillis, blockSize );
    KeyToInterval data2 = IoaUtils.partition( stream2, input2.totalTimeMillis, blockSize );

    Map< Character, IntervalCalculations > actual = IoaCalculations.partialAgreement( data1, data2 );
    Map< Character, IntervalCalculations > expected = Maps.newHashMap();

    // . . . . . . . . . . 0, 1, 2
    // . . . . . . . . . . 0, 0, 1
    int[] intervals1_d = { 3, 0 };
    int[] intervals2_d = { 2, 0 };
    double[] result_d = { 2.0 / 3.0, 1 };

    int[] intervals1_c = { 2, 0 };
    int[] intervals2_c = { 2, 1 };
    double[] result_c = { 1, 0 };

    expected.put( 'd', new IntervalCalculations( 'd', intervals1_d, intervals2_d, result_d ) );
    expected.put( 'c', new IntervalCalculations( 'c', intervals1_c, intervals2_c, result_c ) );

    Assert.assertEquals( expected, actual );
  }

  @Test public void exactAgreement_blocksize_1()
  {
    int blockSize = 1;

    HashMap< Character, ArrayList< Integer >> stream1 = Maps.newHashMap( input1.discretes );
    stream1.putAll( input1.continuous );
    HashMap< Character, ArrayList< Integer >> stream2 = Maps.newHashMap( input2.discretes );
    stream2.putAll( input2.continuous );

    KeyToInterval data1 = IoaUtils.partition( stream1, input1.totalTimeMillis, blockSize );
    KeyToInterval data2 = IoaUtils.partition( stream2, input2.totalTimeMillis, blockSize );

    Map< Character, IntervalCalculations > actual = IoaCalculations.exactAgreement( data1, data2 );
    Map< Character, IntervalCalculations > expected = Maps.newHashMap();

    // . . . . . . . . . . 0, 1, 2
    // . . . . . . . . . . 0, 1, 2
    int[] intervals1_d = { 1, 2, 0 };
    int[] intervals2_d = { 1, 1, 0 };
    double[] result_d = { 1, 0, 1 };
    // . . . . . . . . . . 0, 1, 2
    int[] intervals1_c = { 1, 1, 0 };
    int[] intervals2_c = { 1, 1, 1 };
    double[] result_c = { 1, 1, 0 };

    expected.put( 'd', new IntervalCalculations( 'd', intervals1_d, intervals2_d, result_d ) );
    expected.put( 'c', new IntervalCalculations( 'c', intervals1_c, intervals2_c, result_c ) );

    Assert.assertEquals( expected, actual );
  }

  @Test public void timeWindow_Agreement_blocksize_1()
  {
    int thresh = 1;

    KeyToInterval discrete1 = IoaUtils.partition( input1.discretes, input1.totalTimeMillis, 1 );
    KeyToInterval discrete2 = IoaUtils.partition( input2.discretes, input2.totalTimeMillis, 1 );
    KeyToInterval continuous1 = IoaUtils.partition( input1.continuous, input1.totalTimeMillis, 1 );
    KeyToInterval continuous2 = IoaUtils.partition( input2.continuous, input2.totalTimeMillis, 1 );

    Map< Character, TimeWindowCalculations > actualDiscrete =
        IoaCalculations.windowAgreementDiscrete( discrete1, discrete2, thresh );
    Map< Character, Double > actualContinuous =
        IoaCalculations.windowAgreementContinuous( continuous1, continuous2 );

    Map< Character, TimeWindowCalculations > expectedDiscrete = Maps.newHashMap();
    expectedDiscrete.put( 'd', new TimeWindowCalculations( 2.0 / 3.0, 1 ) );

    Map< Character, Double > expectedContinuous = Maps.newHashMap();
    expectedContinuous.put( 'c', 2.0 / 3.0 );

    Assert.assertEquals( expectedDiscrete, actualDiscrete );
    Assert.assertEquals( expectedContinuous, actualContinuous );
  }

  @Test public void timeWindow_discrete_blocksize_2()
  {
    SessionBean input1 = new SessionBean();
    input1.totalTimeMillis = 10000;
    input1.discretes = Maps.newHashMap();
    input1.continuous = Maps.newHashMap();
    input1.discretes.put( 'a', Lists.newArrayList( 0, 1, 1, 5, 8, 8 ) );
    input1.discretes.put( 'b', Lists.newArrayList( 4, 9, 9 ) );

    SessionBean input2 = new SessionBean();
    input2.totalTimeMillis = 11000;
    input2.discretes = Maps.newHashMap();
    input2.continuous = Maps.newHashMap();
    input2.discretes.put( 'a', Lists.newArrayList( 0, 1, 4, 7, 8 ) );
    input2.discretes.put( 'b', Lists.newArrayList( 1, 7, 10 ) );

    int thresh = 2;

    HashMap< Character, ArrayList< Integer >> stream1 = Maps.newHashMap( input1.discretes );
    stream1.putAll( input1.continuous );
    HashMap< Character, ArrayList< Integer >> stream2 = Maps.newHashMap( input2.discretes );
    stream2.putAll( input2.continuous );

    KeyToInterval discrete1 = IoaUtils.partition( input1.discretes, input1.totalTimeMillis, 1 );
    KeyToInterval discrete2 = IoaUtils.partition( input2.discretes, input2.totalTimeMillis, 1 );

    Map< Character, TimeWindowCalculations > actualDiscrete =
        IoaCalculations.windowAgreementDiscrete( discrete1, discrete2, thresh );

    Map< Character, TimeWindowCalculations > expectedDiscrete = Maps.newHashMap();
    expectedDiscrete.put( 'a', new TimeWindowCalculations( 5.0 / 6.0, 1 ) );
    expectedDiscrete.put( 'b', new TimeWindowCalculations( 2.0 / 3.0, 2.0 / 3.0 ) );

    Assert.assertEquals( expectedDiscrete, actualDiscrete );
  }
}
