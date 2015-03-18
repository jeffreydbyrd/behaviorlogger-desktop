package com.threebird.recorder.utils.ioa;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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

public class IoaUtils
{
  /**
   * @param timeTokeys
   *          - each index represents a second mapped to an array of characters
   * @param blockSize
   *          - the partition size of intervals
   * @return a map of keys to the times they occurred in
   */
  private static KeyToInterval mapKeysToInterval( String[] timeToKeys, int blockSize )
  {
    HashMap< Character, Multiset< Integer >> charToIntervals = Maps.newHashMap();

    for (int s = 0; s < timeToKeys.length; s++) {
      String keys = timeToKeys[s];
      for (char ch : keys.toCharArray()) {
        if (!charToIntervals.containsKey( ch )) {
          charToIntervals.put( ch, HashMultiset.create() );
        }
        charToIntervals.get( ch ).add( s / blockSize );
      }
    }

    return new KeyToInterval( charToIntervals, timeToKeys.length / blockSize );
  }

  private static String[] timesToKeys( File f ) throws IOException
  {
    Iterator< CSVRecord > recsItor = CSVFormat.DEFAULT.parse( Files.newReader( f, Charsets.UTF_8 ) ).iterator();
    recsItor.next(); // skip header
    Iterator< String > keyItor = Iterators.transform( recsItor, rec -> rec.get( 1 ) );
    ArrayList< String > keys = Lists.newArrayList( keyItor );
    return keys.toArray( new String[keys.size()] );
  }

  public static void process( File f1,
                              File f2,
                              IoaMethod method,
                              int blockSize,
                              File out ) throws IOException
  {
    if (method != IoaMethod.Time_Window) {
      KeyToInterval data1 = mapKeysToInterval( timesToKeys( f1 ), blockSize );
      KeyToInterval data2 = mapKeysToInterval( timesToKeys( f2 ), blockSize );
      Map< Character, IntervalCalculations > intervals =
          method == IoaMethod.Exact_Agreement
              ? IoaCalculations.exactAgreement( data1, data2 )
              : IoaCalculations.partialAgreement( data1, data2 );
      WriteIoaIntervals.write( intervals, out );
    } else {
      KeyToInterval data1 = mapKeysToInterval( timesToKeys( f1 ), 1 );
      KeyToInterval data2 = mapKeysToInterval( timesToKeys( f2 ), 1 );
      Map< Character, Double > windowAgreement = IoaCalculations.windowAgreement( data1, data2, blockSize );
    }
  }
}
