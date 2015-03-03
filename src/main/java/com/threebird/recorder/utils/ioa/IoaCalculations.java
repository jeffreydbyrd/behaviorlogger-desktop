package com.threebird.recorder.utils.ioa;

import java.io.File;
import java.util.Collections;

import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

public class IoaCalculations
{

  private static int getNumIntervals( Multiset< Integer > times )
  {
    if (times == null) {
      return 0;
    }
    return Collections.max( times ) + 1;
  }

  static File exactAgreement( KeyToTime data1, KeyToTime data2 )
  {
    SetView< Character > common = Sets.union( data1.keySet(), data2.keySet() );
    for (Character c : common) {
      System.out.println( "============- " + c + " -============" );
      int numIntervals1 = getNumIntervals( data1.get( c ) );
      int numIntervals2 = getNumIntervals( data2.get( c ) );
      int numIntervals = numIntervals1 > numIntervals2 ? numIntervals1 : numIntervals2;
      int[] intervals1 = new int[numIntervals];
      int[] intervals2 = new int[numIntervals];
      for (Integer i = 0; i < numIntervals; i++) {
        intervals1[i] += data1.get( c ) != null ? data1.get( c ).count( i ) : 0;
        intervals2[i] += data2.get( c ) != null ? data2.get( c ).count( i ) : 0;
        System.out.print( i + ", " );
      }
      System.out.println();
      for (int i : intervals1) {
        System.out.print( i + ", " );
      }
      System.out.println();
      for (int i : intervals2) {
        System.out.print( i + ", " );
      }
      System.out.println();
    }
    return null;
  }
}
