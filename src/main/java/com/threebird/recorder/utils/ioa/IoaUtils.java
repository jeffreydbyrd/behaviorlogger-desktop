package com.threebird.recorder.utils.ioa;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.apache.commons.codec.Charsets;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.io.Files;
import com.threebird.recorder.models.MappableChar;
import com.threebird.recorder.models.behaviors.Behavior;
import com.threebird.recorder.models.behaviors.ContinuousBehavior;
import com.threebird.recorder.models.behaviors.DiscreteBehavior;
import com.threebird.recorder.utils.EventRecorderUtil;

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

  private static KeyToTime mapKeysToTime( List< Behavior > bs ) throws IOException
  {
    KeyToTime counts = new KeyToTime();

    bs.forEach( b -> {
      char ch = b.key.c;
      if (!counts.containsKey( ch )) {
        counts.put( ch, HashMultiset.create() );
      }

      if (!b.isContinuous()) {
        counts.get( ch ).add( b.startTime );
      } else {
        ContinuousBehavior cb = (ContinuousBehavior) b;
        Multiset< Integer > intervals = counts.get( ch );
        if (!intervals.contains( b.startTime )) {
          intervals.add( b.startTime );
        }

        for (int t = b.startTime; t <= b.startTime + cb.getDuration(); t++) {
          if (!intervals.contains( t )) {
            intervals.add( t );
          }
        }
      }
    } );

    return counts;
  }

  public static List< Behavior > toBehaviors( File f ) throws IOException
  {
    Iterator< CSVRecord > recsItor = CSVFormat.DEFAULT.parse( Files.newReader( f, Charsets.UTF_8 ) ).iterator();
    recsItor.next(); // skip header
    Iterator< Behavior > behItor = Iterators.transform( recsItor, IoaUtils::recordToBehavior );
    return Lists.newArrayList( behItor );
  }

  public static File compare( File f1, File f2, IoaMethod method, int threshold ) throws IOException
  {
    KeyToTime data1 = mapKeysToTime( toBehaviors( f1 ) );
    KeyToTime data2 = mapKeysToTime( toBehaviors( f2 ) );

    if (method == IoaMethod.Exact_Agreement) {
      IoaCalculations.exactAgreement( data1, data2, threshold );
    } else if (method == IoaMethod.Partial_Agreement) {

    } else if (method == IoaMethod.Time_Window) {

    }

    return null;
  }
}
