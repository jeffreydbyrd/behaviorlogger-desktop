package com.threebird.recorder.utils.ioa;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javafx.scene.layout.VBox;

import org.apache.commons.codec.Charsets;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.common.io.Files;
import com.threebird.recorder.persistence.WriteIoaIntervals;
import com.threebird.recorder.persistence.WriteIoaTimeWindows;
import com.threebird.recorder.views.ioa.IoaTimeBlockSummary;
import com.threebird.recorder.views.ioa.IoaTimeWindowSummary;

public class IoaUtils
{
  /**
   * @param timeTokeys
   *          - a list of rows, representing each second of the session in order
   * @param blockSize
   *          - the partition size of intervals
   * @return a map of keys to the times they occurred in
   */
  static KeyToInterval mapKeysToInterval( List< String > timeToKeys, int blockSize )
  {
    HashMap< Character, Multiset< Integer >> charToIntervals = Maps.newHashMap();

    for (int s = 0; s < timeToKeys.size(); s++) {
      String keys = timeToKeys.get( s );
      for (char ch : keys.toCharArray()) {
        if (!charToIntervals.containsKey( ch )) {
          charToIntervals.put( ch, HashMultiset.create() );
        }
        charToIntervals.get( ch ).add( s / blockSize );
      }
    }

    return new KeyToInterval( charToIntervals, (int) Math.ceil( (double) timeToKeys.size() / blockSize ) );
  }

  static KeyToInterval mapRowsToInterval( List< BehaviorLogRow > rows, int blockSize )
  {
    return mapKeysToInterval( Lists.transform( rows, r -> r.continuous + r.discrete ), blockSize );
  }

  static List< BehaviorLogRow > deserialize( File f ) throws IOException
  {
    try {
      BufferedReader reader;
      reader = Files.newReader( f, Charsets.UTF_8 );
      Iterator< CSVRecord > recsItor = CSVFormat.DEFAULT.parse( reader ).iterator();
      recsItor.next(); // skip header
      Iterator< BehaviorLogRow > keyItor =
          Iterators.transform( recsItor, rec -> new BehaviorLogRow( rec.get( 1 ), rec.get( 2 ) ) );
      ArrayList< BehaviorLogRow > rows = Lists.newArrayList( keyItor );
      reader.close();
      return rows;
    } catch (RuntimeException e) {
      String msg =
          String.format( "A problem occurred while processing %s. Please consult the manual for the file format IOA Calculator uses.",
                         f.getName() );
      throw new RuntimeException( msg, e );
    }
  }

  private static VBox processTimeBlock( IoaMethod method,
                                        int blockSize,
                                        boolean appendToFile,
                                        File out,
                                        List< BehaviorLogRow > rows1,
                                        List< BehaviorLogRow > rows2 ) throws Exception
  {
    int size = blockSize < 1 ? 1 : blockSize;

    KeyToInterval data1 = mapRowsToInterval( rows1, size );
    KeyToInterval data2 = mapRowsToInterval( rows2, size );
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
                                         List< BehaviorLogRow > rows1,
                                         List< BehaviorLogRow > rows2 ) throws Exception
  {
    KeyToInterval discrete1 = mapKeysToInterval( Lists.transform( rows1, r -> r.discrete ), 1 );
    KeyToInterval discrete2 = mapKeysToInterval( Lists.transform( rows2, r -> r.discrete ), 1 );
    KeyToInterval continuous1 = mapKeysToInterval( Lists.transform( rows1, r -> r.continuous ), 1 );
    KeyToInterval continuous2 = mapKeysToInterval( Lists.transform( rows2, r -> r.continuous ), 1 );

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
   *          the first CSV input file
   * @param f2
   *          the second CSV input file
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
    List< BehaviorLogRow > rows1 = deserialize( f1 );
    List< BehaviorLogRow > rows2 = deserialize( f2 );

    if (method != IoaMethod.Time_Window) {
      return processTimeBlock( method, blockSize, appendToFile, out, rows1, rows2 );
    } else {
      return processTimeWindow( f1.getName(), f2.getName(), appendToFile, out, blockSize, rows1, rows2 );
    }
  }
}
