package com.threebird.recorder.utils.ioa;

import java.util.HashMap;

import com.google.common.collect.Multiset;

/**
 * A simple typedef for a Map that maps each key to all the intervals that they
 * occurred in. It also contains a reference to the total number of intervals
 */
public class KeyToInterval
{
  public final HashMap< Character, Multiset< Integer >> charToIntervals;
  public final int totalIntervals;

  public KeyToInterval( HashMap< Character, Multiset< Integer >> charToIntervals, int totalIntervals )
  {
    this.charToIntervals = charToIntervals;
    this.totalIntervals = totalIntervals;
  }

  @Override public String toString()
  {
    StringBuilder sb = new StringBuilder();
    charToIntervals.forEach( ( ch, intervals ) -> {
      sb.append( "  " + ch + "->" + intervals.toString() + "\n" );
    } );
    return "KeyToInterval [\n charToIntervals=\n" + sb.toString() + ", totalIntervals=" + totalIntervals + "\n]";
  }
}