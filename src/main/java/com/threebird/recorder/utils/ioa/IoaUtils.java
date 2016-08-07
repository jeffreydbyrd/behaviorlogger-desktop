package com.threebird.recorder.utils.ioa;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javafx.scene.layout.VBox;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.threebird.recorder.persistence.GsonUtils;
import com.threebird.recorder.persistence.RecordingRawJson.SessionBean;
import com.threebird.recorder.persistence.StartEndTimes;
import com.threebird.recorder.persistence.WriteIoaIntervals;
import com.threebird.recorder.persistence.WriteIoaTimeWindows;
import com.threebird.recorder.views.ioa.IoaTimeBlockSummary;
import com.threebird.recorder.views.ioa.IoaTimeWindowSummary;

public class IoaUtils
{
  static KeyToInterval partition( HashMap< String, ArrayList< Integer > > stream,
                                  int totalTimeMilles,
                                  int size )
  {
    HashMap< String, Multiset< Integer > > idToIntervals = Maps.newHashMap();

    stream.forEach( ( buuid, ints ) -> {
      HashMultiset< Integer > times = HashMultiset.create();
      idToIntervals.put( buuid, times );
      ints.forEach( t -> {
        times.add( t / size );
      } );
    } );

    int numIntervals = (int) Math.ceil( (totalTimeMilles / 1000.0) / size );
    return new KeyToInterval( idToIntervals, numIntervals );
  }

  private static VBox processTimeBlock( IoaMethod method,
                                        int blockSize,
                                        boolean appendToFile,
                                        File out,
                                        SessionBean stream1,
                                        SessionBean stream2 )
      throws Exception
  {
    int size = blockSize < 1 ? 1 : blockSize;
    HashMap< String, ArrayList< Integer > > map1 = createIoaMap( stream1 );
    HashMap< String, ArrayList< Integer > > map2 = createIoaMap( stream2 );

    KeyToInterval data1 = partition( map1, stream1.totalTimeMillis, size );
    KeyToInterval data2 = partition( map2, stream2.totalTimeMillis, size );

    Map< String, IntervalCalculations > intervals =
        method == IoaMethod.Exact_Agreement
            ? IoaCalculations.exactAgreement( data1, data2 )
            : IoaCalculations.partialAgreement( data1, data2 );
    WriteIoaIntervals.write( intervals, appendToFile, out );
    return new IoaTimeBlockSummary( intervals );
  }

  public static HashMap< String, ArrayList< Integer > > createIoaMap( SessionBean bean )
  {
    HashMap< String, ArrayList< Integer > > result = Maps.newHashMap();
    populateDiscrete( bean, result );
    populateContinuous( bean, result );
    return result;
  }

  /**
   * Mutates the map
   */
  public static void populateContinuous( SessionBean stream1, HashMap< String, ArrayList< Integer > > map1 )
  {
    for (Entry< String, ArrayList< StartEndTimes > > entry : stream1.continuousEvents.entrySet()) {
      String buuid = entry.getKey();
      if (map1.containsKey( buuid )) {
        map1.put( buuid, Lists.newArrayList() );
      }
      for (StartEndTimes startEndTimes : entry.getValue()) {
        int start = startEndTimes.start / 1000;
        int end = startEndTimes.end / 1000;
        for (int t = start; t <= end; t += 1) {
          map1.get( buuid ).add( t );
        }
      }
    }
  }

  /**
   * Mutates the map
   */
  public static void populateDiscrete( SessionBean stream1, HashMap< String, ArrayList< Integer > > map1 )
  {
    for (Entry< String, ArrayList< Integer > > entry : stream1.discreteEvents.entrySet()) {
      String buuid = entry.getKey();
      if (!map1.containsKey( buuid )) {
        map1.put( buuid, Lists.newArrayList() );
      }
      for (Integer t : entry.getValue()) {
        int seconds = t / 1000;
        map1.get( buuid ).add( seconds );
      }
    }
  }

  private static VBox processTimeWindow( String file1,
                                         String file2,
                                         boolean appendToFile,
                                         File out,
                                         int threshold,
                                         SessionBean stream1,
                                         SessionBean stream2 )
      throws Exception
  {
    HashMap< String, ArrayList< Integer > > discretes1 = Maps.newHashMap();
    HashMap< String, ArrayList< Integer > > discretes2 = Maps.newHashMap();
    populateDiscrete( stream1, discretes1 );
    populateDiscrete( stream2, discretes2 );
    KeyToInterval discrete1 = partition( discretes1, stream1.totalTimeMillis, 1 );
    KeyToInterval discrete2 = partition( discretes2, stream2.totalTimeMillis, 1 );

    HashMap< String, ArrayList< Integer > > continuous1 = Maps.newHashMap();
    HashMap< String, ArrayList< Integer > > continuous2 = Maps.newHashMap();
    populateContinuous( stream1, continuous1 );
    populateContinuous( stream2, continuous2 );
    KeyToInterval cont1 = partition( continuous1, stream1.totalTimeMillis, 1 );
    KeyToInterval cont2 = partition( continuous2, stream2.totalTimeMillis, 1 );

    Map< String, TimeWindowCalculations > ioaDiscrete =
        IoaCalculations.windowAgreementDiscrete( discrete1, discrete2, threshold );
    Map< String, Double > ioaContinuous =
        IoaCalculations.windowAgreementContinuous( cont1, cont2 );

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
                              File out )
      throws Exception
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
