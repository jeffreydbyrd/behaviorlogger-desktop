package com.threebird.recorder.utils.ioa;

import java.io.File;
import java.util.List;

import com.google.common.collect.Iterables;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

public class IoaCalculations
{
  static File exactAgreement( KeyToTime data1, KeyToTime data2, int threshold )
  {
    SetView< Character > common = Sets.intersection( data1.keySet(), data2.keySet() );
    for (Character c : common) {
      List< Integer > times1 = Ordering.natural().sortedCopy( data1.get( c ) );
      List< Integer > times2 = Ordering.natural().sortedCopy( data2.get( c ) );
      int numIntervals1 = times1.get( times1.size() - 1 ) / threshold;
      int numIntervals2 = times2.get( times2.size() - 1 ) / threshold;
      int numIntervals = numIntervals1 > numIntervals2 ? numIntervals1 : numIntervals2;

      int[] intervals = new int[numIntervals];
      for (int i = 0; i < numIntervals; i++) {

      }
    }
    return null;
  }
}
