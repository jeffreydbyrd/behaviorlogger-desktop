package com.threebird.recorder.utils.ioa;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.codec.Charsets;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.threebird.recorder.models.MappableChar;
import com.threebird.recorder.models.behaviors.ContinuousBehavior;
import com.threebird.recorder.models.behaviors.DiscreteBehavior;
import com.threebird.recorder.utils.EventRecorderUtil;

public class IoaUtils
{

  private static class BehaviorCounts
  {
    /**
     * keys: DiscreteBehaviors
     * 
     * vals: all seconds in which said behavior occurred and how many times it
     * occurred
     */
    public Map< DiscreteBehavior, Multiset< Integer >> discrete = Maps.newHashMap();

    /**
     * keys: ContinuousBehaviors
     * 
     * vals: all seconds in which said behavior occurred
     */
    public Map< ContinuousBehavior, Set< Integer >> continuous = Maps.newHashMap();
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

  private static BehaviorCounts getCounts( File f ) throws IOException
  {
    BehaviorCounts c = new BehaviorCounts();
    Iterable< CSVRecord > recs = CSVFormat.DEFAULT.parse( Files.newReader( f, Charsets.UTF_8 ) );

    for (CSVRecord rec : recs) {
      boolean isDiscrete = rec.get( 1 ).equals( "discrete" );
      if (isDiscrete) {
        DiscreteBehavior db = recordToDiscrete( rec );
        if (!c.discrete.containsKey( db )) {
          c.discrete.put( db, HashMultiset.create() );
        }
        c.discrete.get( db ).add( db.startTime );
      } else {
        ContinuousBehavior cb = recordToContinuous( rec );
        if (!c.continuous.containsKey( cb )) {
          c.continuous.put( cb, Sets.newHashSet() );
        }

        Set< Integer > intervals = c.continuous.get( cb );
        intervals.add( cb.startTime );

        for (int t = cb.startTime + 1; t <= cb.startTime + cb.getDuration(); t++) {
          intervals.add( t );
        }
      }
    }

    return c;
  }

  public static File compare( File f1, File f2 ) throws IOException
  {
    return null;
  }
}
