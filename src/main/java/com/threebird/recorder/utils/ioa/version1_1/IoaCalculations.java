package com.threebird.recorder.utils.ioa.version1_1;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.threebird.recorder.utils.ioa.IntervalCalculations;
import com.threebird.recorder.utils.ioa.KeyToInterval;
import com.threebird.recorder.utils.ioa.TimeWindowCalculations;

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

  private static Map< String, IntervalCalculations >
    getIntervals( KeyToInterval data1,
                  KeyToInterval data2,
                  BiFunction< Integer, Integer, Double > compare )
  {
    SetView< String > common = Sets.union( data1.keyToIntervals.keySet(), data2.keyToIntervals.keySet() );
    Map< String, IntervalCalculations > map = Maps.newHashMap();

    int numIntervals = Math.max( data1.totalIntervals, data2.totalIntervals );

    for (String key : common) {
      int[] intervals1 = new int[numIntervals];
      int[] intervals2 = new int[numIntervals];
      double[] result = new double[numIntervals];

      for (Integer i = 0; i < numIntervals; i++) {
        intervals1[i] += data1.keyToIntervals.get( key ) != null ? data1.keyToIntervals.get( key ).count( i ) : 0;
        intervals2[i] += data2.keyToIntervals.get( key ) != null ? data2.keyToIntervals.get( key ).count( i ) : 0;
      }

      for (int i = 0; i < numIntervals; i++) {
        result[i] = compare.apply( intervals1[i], intervals2[i] );
      }

      map.put( key, new IntervalCalculations( key, intervals1, intervals2, result ) );
    }

    return map;
  }

  static Map< String, IntervalCalculations > exactAgreement( KeyToInterval data1, KeyToInterval data2 )
  {
    return getIntervals( data1, data2, IoaCalculations::exactComparison );
  }

  static Map< String, IntervalCalculations > partialAgreement( KeyToInterval data1, KeyToInterval data2 )
  {
    return getIntervals( data1, data2, IoaCalculations::partialComparison );
  }

  static double windowAgreementDiscrete( Multiset< Integer > target, Multiset< Integer > comparison, int threshold )
  {
    if (target == null || target.isEmpty() || comparison == null || comparison.isEmpty()) {
      return 0;
    }

    List< Integer > _target = Lists.newArrayList( target );
    List< Integer > _comparison = Lists.newArrayList( comparison );
    int numMatched = 0;

    for (int k = 0; k < _target.size(); k++) {
      int sec = _target.get( k );
      int min = sec - threshold;
      int max = sec + threshold;

      Optional< Integer > optMatch = Iterables.tryFind( _comparison, i -> i >= min && i <= max );
      for (Integer match : optMatch.asSet()) {
        _comparison.remove( match );
        numMatched++;
      }
    }

    return ((double) numMatched) / _target.size();
  }

  static double windowAgreementContinuous( Multiset< Integer > seconds1, Multiset< Integer > seconds2 )
  {
    if (seconds1 == null || seconds1.isEmpty() || seconds2 == null || seconds2.isEmpty()) {
      return 0;
    }

    Set< Integer > set1 = seconds1.elementSet();
    Set< Integer > set2 = seconds2.elementSet();
    SetView< Integer > intersection = Sets.intersection( set1, set2 );
    SetView< Integer > union = Sets.union( set1, set2 );

    return ((double) intersection.size()) / union.size();
  }

  static Map< String, TimeWindowCalculations >
    windowAgreementDiscrete( KeyToInterval data1,
                             KeyToInterval data2,
                             int threshold )
  {
    SetView< String > common = Sets.union( data1.keyToIntervals.keySet(), data2.keyToIntervals.keySet() );
    Map< String, TimeWindowCalculations > result = Maps.newHashMap();

    for (String key : common) {
      Multiset< Integer > seconds1 = data1.keyToIntervals.get( key );
      Multiset< Integer > seconds2 = data2.keyToIntervals.get( key );

      double result1 = windowAgreementDiscrete( seconds1, seconds2, threshold );
      double result2 = windowAgreementDiscrete( seconds2, seconds1, threshold );
      TimeWindowCalculations calcs = new TimeWindowCalculations( result1, result2 );
      result.put( key, calcs );
    }

    return result;
  }

  static Map< String, Double > windowAgreementContinuous( KeyToInterval data1, KeyToInterval data2 )
  {
    SetView< String > common = Sets.union( data1.keyToIntervals.keySet(), data2.keyToIntervals.keySet() );
    Map< String, Double > result = Maps.newHashMap();

    for (String key : common) {
      Multiset< Integer > seconds1 = data1.keyToIntervals.get( key );
      Multiset< Integer > seconds2 = data2.keyToIntervals.get( key );

      result.put( key, windowAgreementContinuous( seconds1, seconds2 ) );
    }

    return result;
  }
}
