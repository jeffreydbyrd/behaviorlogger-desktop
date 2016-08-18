package com.threebird.recorder.utils.ioa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.threebird.recorder.persistence.RecordingRawJson.SessionBean;
import com.threebird.recorder.persistence.StartEndTimes;

public class IoaCalculationsTest
{
  static SessionBean input1 = new SessionBean();
  static {
    input1.totalTimeMillis = 1700;
    input1.discreteEvents = Maps.newHashMap();
    input1.continuousEvents = Maps.newHashMap();
    input1.discreteEvents.put( "d", Lists.newArrayList( 0, 1100, 1700 ) ); // 0,1,1
    input1.continuousEvents.put( "c", Lists.newArrayList( new StartEndTimes( 0, 1000 ) ) ); // 0,1
  }

  static SessionBean input2 = new SessionBean();
  static {
    input2.totalTimeMillis = 2700;
    input2.discreteEvents = Maps.newHashMap();
    input2.continuousEvents = Maps.newHashMap();
    input2.discreteEvents.put( "d", Lists.newArrayList( 0, 1100 ) ); // 0,1
    input2.continuousEvents.put( "c", Lists.newArrayList( new StartEndTimes( 0, 2000 ) ) ); // 0,1,2
  }

  static SessionBean empty = new SessionBean();
  static {
    empty.totalTimeMillis = 2700;
    empty.discreteEvents = Maps.newHashMap();
    empty.continuousEvents = Maps.newHashMap();
  }

  @Test public void partialAgreement_blocksize_1()
  {
    int blockSize = 1;

    HashMap< String, ArrayList< Integer > > stream1 = IoaUtils.createIoaMap( input1 );
    HashMap< String, ArrayList< Integer > > stream2 = IoaUtils.createIoaMap( input2 );
    KeyToInterval data1 = IoaUtils.partition( stream1, input1.totalTimeMillis, blockSize );
    KeyToInterval data2 = IoaUtils.partition( stream2, input2.totalTimeMillis, blockSize );

    Map< String, IntervalCalculations > actual = IoaCalculations.partialAgreement( data1, data2 );
    Map< String, IntervalCalculations > expected = Maps.newHashMap();

    // . . . . . . . . . . 0, 1, 2
    int[] intervals1_d = { 1, 2, 0 };
    int[] intervals2_d = { 1, 1, 0 };
    double[] result_d = { 1, 0.5, 1 };
    // . . . . . . . . . . 0, 1, 2
    int[] intervals1_c = { 1, 1, 0 };
    int[] intervals2_c = { 1, 1, 1 };
    double[] result_c = { 1, 1, 0 };

    expected.put( "d", new IntervalCalculations( "d", intervals1_d, intervals2_d, result_d ) );
    expected.put( "c", new IntervalCalculations( "c", intervals1_c, intervals2_c, result_c ) );

    Assert.assertEquals( expected, actual );
  }

  @Test public void partialAgreement_blocksize_2()
  {
    int blockSize = 2;

    HashMap< String, ArrayList< Integer > > stream1 = IoaUtils.createIoaMap( input1 );
    HashMap< String, ArrayList< Integer > > stream2 = IoaUtils.createIoaMap( input2 );
    KeyToInterval data1 = IoaUtils.partition( stream1, input1.totalTimeMillis, blockSize );
    KeyToInterval data2 = IoaUtils.partition( stream2, input2.totalTimeMillis, blockSize );

    Map< String, IntervalCalculations > actual = IoaCalculations.partialAgreement( data1, data2 );
    Map< String, IntervalCalculations > expected = Maps.newHashMap();

    // . . . . . . . . . . 0, 1, 2
    // . . . . . . . . . . 0, 0, 1
    int[] intervals1_d = { 3, 0 };
    int[] intervals2_d = { 2, 0 };
    double[] result_d = { 2.0 / 3.0, 1 };

    int[] intervals1_c = { 2, 0 };
    int[] intervals2_c = { 2, 1 };
    double[] result_c = { 1, 0 };

    expected.put( "d", new IntervalCalculations( "d", intervals1_d, intervals2_d, result_d ) );
    expected.put( "c", new IntervalCalculations( "c", intervals1_c, intervals2_c, result_c ) );

    Assert.assertEquals( expected, actual );
  }

  @Test public void partialAgreement_blocksize_3()
  {
    int blockSize = 3;

    HashMap< String, ArrayList< Integer > > stream1 = IoaUtils.createIoaMap( input1 );
    HashMap< String, ArrayList< Integer > > stream2 = IoaUtils.createIoaMap( input2 );
    KeyToInterval data1 = IoaUtils.partition( stream1, input1.totalTimeMillis, blockSize );
    KeyToInterval data2 = IoaUtils.partition( stream2, input2.totalTimeMillis, blockSize );

    Map< String, IntervalCalculations > actual = IoaCalculations.partialAgreement( data1, data2 );
    Map< String, IntervalCalculations > expected = Maps.newHashMap();

    // . . . . . . . . . . 0, 1, 2
    // . . . . . . . . . . 0, 0, 0
    int[] intervals1_d = { 3 };
    int[] intervals2_d = { 2 };
    double[] result_d = { 2.0 / 3.0 };

    int[] intervals1_c = { 2 };
    int[] intervals2_c = { 3 };
    double[] result_c = { 2.0 / 3.0 };

    expected.put( "d", new IntervalCalculations( "d", intervals1_d, intervals2_d, result_d ) );
    expected.put( "c", new IntervalCalculations( "c", intervals1_c, intervals2_c, result_c ) );

    Assert.assertEquals( expected, actual );
  }

  @Test public void partialAgreement_empty()
  {
    int blockSize = 2;

    HashMap< String, ArrayList< Integer > > stream1 = IoaUtils.createIoaMap( input1 );
    HashMap< String, ArrayList< Integer > > stream2 = IoaUtils.createIoaMap( empty );
    KeyToInterval data1 = IoaUtils.partition( stream1, input1.totalTimeMillis, blockSize );
    KeyToInterval data2 = IoaUtils.partition( stream2, empty.totalTimeMillis, blockSize );

    Map< String, IntervalCalculations > actual = IoaCalculations.partialAgreement( data1, data2 );
    Map< String, IntervalCalculations > expected = Maps.newHashMap();

    // . . . . . . . . . . 0, 1, 2
    // . . . . . . . . . . 0, 0, 1
    int[] intervals1_d = { 3, 0 };
    int[] intervals2_d = { 0, 0 };
    double[] result_d = { 0, 1 };

    int[] intervals1_c = { 2, 0 };
    int[] intervals2_c = { 0, 0 };
    double[] result_c = { 0, 1 };

    expected.put( "d", new IntervalCalculations( "d", intervals1_d, intervals2_d, result_d ) );
    expected.put( "c", new IntervalCalculations( "c", intervals1_c, intervals2_c, result_c ) );

    Assert.assertEquals( expected, actual );
  }

  @Test public void exactAgreement_blocksize_1()
  {
    int blockSize = 1;

    HashMap< String, ArrayList< Integer > > stream1 = IoaUtils.createIoaMap( input1 );
    HashMap< String, ArrayList< Integer > > stream2 = IoaUtils.createIoaMap( input2 );
    KeyToInterval data1 = IoaUtils.partition( stream1, input1.totalTimeMillis, blockSize );
    KeyToInterval data2 = IoaUtils.partition( stream2, input2.totalTimeMillis, blockSize );

    Map< String, IntervalCalculations > actual = IoaCalculations.exactAgreement( data1, data2 );
    Map< String, IntervalCalculations > expected = Maps.newHashMap();

    // . . . . . . . . . . 0, 1, 2
    // . . . . . . . . . . 0, 1, 2
    int[] intervals1_d = { 1, 2, 0 };
    int[] intervals2_d = { 1, 1, 0 };
    double[] result_d = { 1, 0, 1 };
    // . . . . . . . . . . 0, 1, 2
    int[] intervals1_c = { 1, 1, 0 };
    int[] intervals2_c = { 1, 1, 1 };
    double[] result_c = { 1, 1, 0 };

    expected.put( "d", new IntervalCalculations( "d", intervals1_d, intervals2_d, result_d ) );
    expected.put( "c", new IntervalCalculations( "c", intervals1_c, intervals2_c, result_c ) );

    Assert.assertEquals( expected, actual );
  }

  @Test public void exactAgreement_blocksize_2()
  {
    int blockSize = 2;

    HashMap< String, ArrayList< Integer > > stream1 = IoaUtils.createIoaMap( input1 );
    HashMap< String, ArrayList< Integer > > stream2 = IoaUtils.createIoaMap( input2 );
    KeyToInterval data1 = IoaUtils.partition( stream1, input1.totalTimeMillis, blockSize );
    KeyToInterval data2 = IoaUtils.partition( stream2, input2.totalTimeMillis, blockSize );

    Map< String, IntervalCalculations > actual = IoaCalculations.exactAgreement( data1, data2 );
    Map< String, IntervalCalculations > expected = Maps.newHashMap();

    // . . . . . . . . . . 0, 1, 2
    // . . . . . . . . . . 0, 0, 1
    int[] intervals1_d = { 3, 0 };
    int[] intervals2_d = { 2, 0 };
    double[] result_d = { 0, 1 };
    // . . . . . . . . . . 0, 1, 2
    int[] intervals1_c = { 2, 0 };
    int[] intervals2_c = { 2, 1 };
    double[] result_c = { 1, 0 };

    expected.put( "d", new IntervalCalculations( "d", intervals1_d, intervals2_d, result_d ) );
    expected.put( "c", new IntervalCalculations( "c", intervals1_c, intervals2_c, result_c ) );

    Assert.assertEquals( expected, actual );
  }

  @Test public void exactAgreement_empty()
  {
    int blockSize = 2;

    HashMap< String, ArrayList< Integer > > stream1 = IoaUtils.createIoaMap( input1 );
    HashMap< String, ArrayList< Integer > > stream2 = IoaUtils.createIoaMap( empty );
    KeyToInterval data1 = IoaUtils.partition( stream1, input1.totalTimeMillis, blockSize );
    KeyToInterval data2 = IoaUtils.partition( stream2, empty.totalTimeMillis, blockSize );

    Map< String, IntervalCalculations > actual = IoaCalculations.exactAgreement( data1, data2 );
    Map< String, IntervalCalculations > expected = Maps.newHashMap();

    // . . . . . . . . . . 0, 1, 2
    // . . . . . . . . . . 0, 0, 1
    int[] intervals1_d = { 3, 0 };
    int[] intervals2_d = { 0, 0 };
    double[] result_d = { 0, 1 };
    // . . . . . . . . . . 0, 1, 2
    int[] intervals1_c = { 2, 0 };
    int[] intervals2_c = { 0, 0 };
    double[] result_c = { 0, 1 };

    expected.put( "d", new IntervalCalculations( "d", intervals1_d, intervals2_d, result_d ) );
    expected.put( "c", new IntervalCalculations( "c", intervals1_c, intervals2_c, result_c ) );

    Assert.assertEquals( expected, actual );
  }

  @Test public void timeWindow_Agreement_blocksize_1()
  {
    int thresh = 1;

    HashMap< String, ArrayList< Integer > > mapD1 = Maps.newHashMap();
    HashMap< String, ArrayList< Integer > > mapD2 = Maps.newHashMap();
    HashMap< String, ArrayList< Integer > > mapC1 = Maps.newHashMap();
    HashMap< String, ArrayList< Integer > > mapC2 = Maps.newHashMap();

    IoaUtils.populateDiscrete( input1, mapD1 );
    IoaUtils.populateDiscrete( input2, mapD2 );
    IoaUtils.populateContinuous( input1, mapC1 );
    IoaUtils.populateContinuous( input2, mapC2 );

    KeyToInterval discrete1 = IoaUtils.partition( mapD1, input1.totalTimeMillis, 1 );
    KeyToInterval discrete2 = IoaUtils.partition( mapD2, input2.totalTimeMillis, 1 );
    KeyToInterval continuous1 = IoaUtils.partition( mapC1, input1.totalTimeMillis, 1 );
    KeyToInterval continuous2 = IoaUtils.partition( mapC2, input2.totalTimeMillis, 1 );

    Map< String, TimeWindowCalculations > actualDiscrete =
        IoaCalculations.windowAgreementDiscrete( discrete1, discrete2, thresh );
    Map< String, Double > actualContinuous =
        IoaCalculations.windowAgreementContinuous( continuous1, continuous2 );

    Map< String, TimeWindowCalculations > expectedDiscrete = Maps.newHashMap();
    expectedDiscrete.put( "d", new TimeWindowCalculations( 2.0 / 3.0, 1 ) );

    Map< String, Double > expectedContinuous = Maps.newHashMap();
    expectedContinuous.put( "c", 2.0 / 3.0 );

    Assert.assertEquals( expectedDiscrete, actualDiscrete );
    Assert.assertEquals( expectedContinuous, actualContinuous );
  }

  @Test public void timeWindow_nonempty_empty()
  {
    int thresh = 1;

    HashMap< String, ArrayList< Integer > > mapD1 = Maps.newHashMap();
    HashMap< String, ArrayList< Integer > > mapD2 = Maps.newHashMap();
    HashMap< String, ArrayList< Integer > > mapC1 = Maps.newHashMap();
    HashMap< String, ArrayList< Integer > > mapC2 = Maps.newHashMap();

    IoaUtils.populateDiscrete( input1, mapD1 );
    IoaUtils.populateDiscrete( empty, mapD2 );
    IoaUtils.populateContinuous( input1, mapC1 );
    IoaUtils.populateContinuous( empty, mapC2 );

    KeyToInterval discrete1 = IoaUtils.partition( mapD1, input1.totalTimeMillis, 1 );
    KeyToInterval discrete2 = IoaUtils.partition( mapD2, empty.totalTimeMillis, 1 );
    KeyToInterval continuous1 = IoaUtils.partition( mapC1, input1.totalTimeMillis, 1 );
    KeyToInterval continuous2 = IoaUtils.partition( mapC2, empty.totalTimeMillis, 1 );

    Map< String, TimeWindowCalculations > actualDiscrete =
        IoaCalculations.windowAgreementDiscrete( discrete1, discrete2, thresh );
    Map< String, Double > actualContinuous =
        IoaCalculations.windowAgreementContinuous( continuous1, continuous2 );

    Map< String, TimeWindowCalculations > expectedDiscrete = Maps.newHashMap();
    expectedDiscrete.put( "d", new TimeWindowCalculations( 0, 0 ) );

    Map< String, Double > expectedContinuous = Maps.newHashMap();
    expectedContinuous.put( "c", 0D );

    Assert.assertEquals( expectedDiscrete, actualDiscrete );
    Assert.assertEquals( expectedContinuous, actualContinuous );
  }
}
