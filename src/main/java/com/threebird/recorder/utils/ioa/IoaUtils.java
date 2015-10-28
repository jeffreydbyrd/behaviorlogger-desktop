package com.threebird.recorder.utils.ioa;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javafx.scene.layout.VBox;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.threebird.recorder.persistence.GsonUtils;
import com.threebird.recorder.persistence.RecordingRawJson.SessionBean;
import com.threebird.recorder.persistence.WriteIoaIntervals;
import com.threebird.recorder.persistence.WriteIoaTimeWindows;
import com.threebird.recorder.views.ioa.IoaTimeBlockSummary;
import com.threebird.recorder.views.ioa.IoaTimeWindowSummary;

public class IoaUtils
{
  static KeyToInterval partition( HashMap< Character, ArrayList< Integer >> stream,
                                  int totalTimeMilles,
                                  int size )
  {
    HashMap< Character, Multiset< Integer >> charToIntervals = Maps.newHashMap();

    stream.forEach( ( c, ints ) -> {
      HashMultiset< Integer > times = HashMultiset.create();
      charToIntervals.put( c, times );
      ints.forEach( t -> {
        times.add( t / size );
      } );
    } );

    int numIntervals = (int) Math.ceil( (totalTimeMilles / 1000.0) / size );
    return new KeyToInterval( charToIntervals, numIntervals );
  }

  private static VBox processTimeBlock( IoaMethod method,
                                        int blockSize,
                                        boolean appendToFile,
                                        File out,
                                        SessionBean stream1,
                                        SessionBean stream2 ) throws Exception
  {
    int size = blockSize < 1 ? 1 : blockSize;

    HashMap< Character, ArrayList< Integer >> map1 = Maps.newHashMap( stream1.discretes );
    HashMap< Character, ArrayList< Integer >> map2 = Maps.newHashMap( stream2.discretes );
    map1.putAll( stream1.continuous );
    map2.putAll( stream2.continuous );

    KeyToInterval data1 = partition( map1, stream1.totalTimeMillis, size );
    KeyToInterval data2 = partition( map2, stream2.totalTimeMillis, size );

    Map< Character, IntervalCalculations > intervals =
        method == IoaMethod.Exact_Agreement
            ? IoaCalculations.exactAgreement( data1, data2 )
            : IoaCalculations.partialAgreement( data1, data2 );
    WriteIoaIntervals.write( intervals, appendToFile, out );
    return new IoaTimeBlockSummary( intervals );
  }

  private static VBox processTimeWindow( String file1,
                                         String file2,
                                         boolean appendToFile,
                                         File out,
                                         int threshold,
                                         SessionBean stream1,
                                         SessionBean stream2 ) throws Exception
  {
    KeyToInterval discrete1 = partition( stream1.discretes, stream1.totalTimeMillis, 1 );
    KeyToInterval discrete2 = partition( stream2.discretes, stream2.totalTimeMillis, 1 );
    KeyToInterval continuous1 = partition( stream1.continuous, stream1.totalTimeMillis, 1 );
    KeyToInterval continuous2 = partition( stream2.continuous, stream2.totalTimeMillis, 1 );

    Map< Character, TimeWindowCalculations > ioaDiscrete =
        IoaCalculations.windowAgreementDiscrete( discrete1, discrete2, threshold );
    Map< Character, Double > ioaContinuous =
        IoaCalculations.windowAgreementContinuous( continuous1, continuous2 );

    WriteIoaTimeWindows.write( ioaDiscrete,
                               ioaContinuous,
                               file1,
                               file2,
                               appendToFile,
                               out );

    return new IoaTimeWindowSummary( ioaDiscrete, ioaContinuous );
  }

  /**
   * Calculates IOA and writes the output to 'out'
   * 
   * @param f1
   *          the first raw input file
   * @param f2
   *          the second raw input file
   * @param method
   *          the {@link IoaMethod} used
   * @param blockSize
   *          the blocksize of intervals used
   * @param out
   *          the output file
   * @return a JavaFX pane giving a summary of the output file
   * @throws IOException
   */
  public static VBox process( File f1,
                              File f2,
                              IoaMethod method,
                              int blockSize,
                              boolean appendToFile,
                              File out ) throws Exception
  {
    SessionBean stream1 = GsonUtils.get( f1, new SessionBean() );
    SessionBean stream2 = GsonUtils.get( f2, new SessionBean() );

    if (method != IoaMethod.Time_Window) {
      return processTimeBlock( method, blockSize, appendToFile, out, stream1, stream2 );
    } else {
      return processTimeWindow( f1.getName(), f2.getName(), appendToFile, out, blockSize, stream1, stream2 );
    }
  }
}
