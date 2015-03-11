package com.threebird.recorder.utils.ioa;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.codec.Charsets;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.threebird.recorder.models.MappableChar;
import com.threebird.recorder.models.behaviors.Behavior;
import com.threebird.recorder.models.behaviors.ContinuousBehavior;
import com.threebird.recorder.models.behaviors.DiscreteBehavior;
import com.threebird.recorder.persistence.WriteIoaIntervals;
import com.threebird.recorder.utils.EventRecorderUtil;
import com.threebird.recorder.utils.ioa.IoaCalculations.IntervalCalculations;

public class IoaUtils
{

  private static Behavior recordToBehavior( CSVRecord rec )
  {
    return rec.get( 1 ).equals( "discrete" )
        ? recordToDiscrete( rec )
        : recordToContinuous( rec );
  }

  private static DiscreteBehavior recordToDiscrete( CSVRecord rec )
  {
    char ch = rec.get( 0 ).charAt( 0 );
    Optional< MappableChar > key = MappableChar.getForChar( ch );
    Preconditions.checkState( key.isPresent(), "Not a mappable character: " + ch );
    String behavior = rec.get( 3 );
    int time = EventRecorderUtil.getDuration( rec.get( 2 ) );

    return new DiscreteBehavior( key.get(), behavior, time );
  }

  private static ContinuousBehavior recordToContinuous( CSVRecord rec )
  {
    char ch = rec.get( 0 ).charAt( 0 );
    Optional< MappableChar > key = MappableChar.getForChar( ch );
    Preconditions.checkState( key.isPresent(), "Not a mappable character: " + ch );
    String behavior = rec.get( 3 );

    String[] split = rec.get( 2 ).split( " *- *" );
    int start = EventRecorderUtil.getDuration( split[0] );
    int end = EventRecorderUtil.getDuration( split[1] );

    return new ContinuousBehavior( key.get(), behavior, start, end - start );
  }

  /**
   * @param timeTokeys
   *          - each index represents a second mapped to an array of characters
   * @param blockSize
   *          - the partition size of intervals
   * @return a map of keys to the times they occurred in
   */
  private static KeyToTime mapKeysToTime( String[] timeToKeys, int blockSize )
  {
    KeyToTime counts = new KeyToTime();

    int second = 0;
    for (String keys : timeToKeys) {
      counts.totalIntervals = second / blockSize;
      for (char ch : keys.toCharArray()) {
        if (!counts.containsKey( ch )) {
          counts.put( ch, HashMultiset.create() );
        }
        counts.get( ch ).add( counts.totalIntervals );
      }
      second++;
    }

    return counts;
  }

  private static String[] timesToKeys( File f ) throws IOException
  {
    Iterator< CSVRecord > recsItor = CSVFormat.DEFAULT.parse( Files.newReader( f, Charsets.UTF_8 ) ).iterator();
    recsItor.next(); // skip header
    Iterator< String > keyItor = Iterators.transform( recsItor, rec -> rec.get( 1 ) );
    ArrayList< String > keys = Lists.newArrayList( keyItor );
    return keys.toArray( new String[keys.size()] );
  }

  public static List< Behavior > toBehaviors( File f ) throws IOException
  {
    Iterator< CSVRecord > recsItor = CSVFormat.DEFAULT.parse( Files.newReader( f, Charsets.UTF_8 ) ).iterator();
    recsItor.next(); // skip header
    Iterator< Behavior > behItor = Iterators.transform( recsItor, IoaUtils::recordToBehavior );
    return Lists.newArrayList( behItor );
  }

  public static void process( File f1,
                              File f2,
                              IoaMethod method,
                              int blockSize,
                              File out ) throws IOException
  {
    if (method != IoaMethod.Time_Window) {
      KeyToTime data1 = mapKeysToTime( timesToKeys( f1 ), blockSize );
      KeyToTime data2 = mapKeysToTime( timesToKeys( f2 ), blockSize );
      Map< Character, IntervalCalculations > intervals =
          method == IoaMethod.Exact_Agreement
              ? IoaCalculations.exactAgreement( data1, data2 )
              : IoaCalculations.partialAgreement( data1, data2 );
      WriteIoaIntervals.write( intervals, out );
    } else {
      // KeyToTime data1 = mapKeysToTime( toBehaviors( f1 ), 1 );
      // KeyToTime data2 = mapKeysToTime( toBehaviors( f2 ), 1 );
    }
  }
}
