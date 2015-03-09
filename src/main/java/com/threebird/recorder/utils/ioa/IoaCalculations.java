package com.threebird.recorder.utils.ioa;

import java.util.Arrays;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

public class IoaCalculations
{
  private static double exactComparison( int x, int y )
  {
    return x == y ? 1.0 : 0.0;
  }

  private static double partialComparison( int x, int y )
  {
    if (x == y) {
      return 1.0;
    }
    if (x == 0 || y == 0) {
      return 0.0;
    }
    double _x = (double) x;
    double _y = (double) y;
    return (x > y ? _y / _x : _x / _y);
  }

  private static int getNumIntervals( Multiset< Integer > times )
  {
    return times.stream()
                .collect( Collectors.maxBy( Integer::compare ) )
                .orElse( 0 );
  }

  private static int getNumIntervals( KeyToTime data1, KeyToTime data2 )
  {
    Integer numIntervals1 = data1.values().stream()
                                 .map( IoaCalculations::getNumIntervals )
                                 .collect( Collectors.maxBy( Integer::compare ) )
                                 .orElse( 0 );

    Integer numIntervals2 = data2.values().stream()
                                 .map( IoaCalculations::getNumIntervals )
                                 .collect( Collectors.maxBy( Integer::compare ) )
                                 .orElse( 0 );

    int numIntervals = Math.max( numIntervals1, numIntervals2 );
    return numIntervals;
  }

  public static class IntervalCalculations
  {
    public final Character c;
    public final int[] intervals1;
    public final int[] intervals2;
    public final double[] result;
    public final double avg;

    public IntervalCalculations( Character c, int[] intervals1, int[] intervals2, double[] result )
    {
      this.c = c;
      this.intervals1 = intervals1;
      this.intervals2 = intervals2;
      this.result = result;
      this.avg = Arrays.stream( result ).average().orElse( 0 );
    }
  }

  private static Map< Character, IntervalCalculations >
    getIntervals( KeyToTime data1,
                  KeyToTime data2,
                  BiFunction< Integer, Integer, Double > compare )
  {
    SetView< Character > common = Sets.union( data1.keySet(), data2.keySet() );
    Map< Character, IntervalCalculations > map = Maps.newHashMap();

    int numIntervals = getNumIntervals( data1, data2 );

    for (Character c : common) {
      int[] intervals1 = new int[numIntervals];
      int[] intervals2 = new int[numIntervals];
      double[] result = new double[numIntervals];

      for (Integer i = 0; i < numIntervals; i++) {
        intervals1[i] += data1.get( c ) != null ? data1.get( c ).count( i ) : 0;
        intervals2[i] += data2.get( c ) != null ? data2.get( c ).count( i ) : 0;
      }

      for (int i = 0; i < numIntervals; i++) {
        int x = intervals1[i];
        int y = intervals2[i];
        result[i] = compare.apply( x, y );
      }

      map.put( c, new IntervalCalculations( c, intervals1, intervals2, result ) );
    }

    return map;
  }

  static Map< Character, IntervalCalculations > exactAgreement( KeyToTime data1, KeyToTime data2 )
  {
    return getIntervals( data1, data2, IoaCalculations::exactComparison );
  }

  static Map< Character, IntervalCalculations > partialAgreement( KeyToTime data1, KeyToTime data2 )
  {
    return getIntervals( data1, data2, IoaCalculations::partialComparison );
  }
}
