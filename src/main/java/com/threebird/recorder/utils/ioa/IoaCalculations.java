package com.threebird.recorder.utils.ioa;

import java.util.Arrays;
import java.util.Map;
import java.util.function.BiFunction;

import com.google.common.collect.Maps;
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

    @Override public String toString()
    {
      return "IntervalCalculations [\n c=" + c + ",\n intervals1=" + Arrays.toString( intervals1 ) + ",\n intervals2="
          + Arrays.toString( intervals2 ) + ",\n result=" + Arrays.toString( result ) + ",\n avg=" + avg + "\n]";
    }
  }

  private static Map< Character, IntervalCalculations >
    getIntervals( KeyToInterval data1,
                  KeyToInterval data2,
                  BiFunction< Integer, Integer, Double > compare )
  {
    SetView< Character > common = Sets.union( data1.charToIntervals.keySet(), data2.charToIntervals.keySet() );
    Map< Character, IntervalCalculations > map = Maps.newHashMap();

    int numIntervals = Math.max( data1.totalIntervals, data2.totalIntervals );

    for (Character c : common) {
      int[] intervals1 = new int[numIntervals];
      int[] intervals2 = new int[numIntervals];
      double[] result = new double[numIntervals];

      for (Integer i = 0; i < numIntervals; i++) {
        intervals1[i] += data1.charToIntervals.get( c ) != null ? data1.charToIntervals.get( c ).count( i ) : 0;
        intervals2[i] += data2.charToIntervals.get( c ) != null ? data2.charToIntervals.get( c ).count( i ) : 0;
      }

      for (int i = 0; i < numIntervals; i++) {
        result[i] = compare.apply( intervals1[i], intervals2[i] );
      }

      map.put( c, new IntervalCalculations( c, intervals1, intervals2, result ) );
    }

    return map;
  }

  static Map< Character, IntervalCalculations > exactAgreement( KeyToInterval data1, KeyToInterval data2 )
  {
    return getIntervals( data1, data2, IoaCalculations::exactComparison );
  }

  static Map< Character, IntervalCalculations > partialAgreement( KeyToInterval data1, KeyToInterval data2 )
  {
    return getIntervals( data1, data2, IoaCalculations::partialComparison );
  }
}
