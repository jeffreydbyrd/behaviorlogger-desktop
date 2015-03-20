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

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((charToIntervals == null) ? 0 : charToIntervals.hashCode());
    result = prime * result + totalIntervals;
    return result;
  }

  @Override public boolean equals( Object obj )
  {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    KeyToInterval other = (KeyToInterval) obj;
    if (charToIntervals == null) {
      if (other.charToIntervals != null)
        return false;
    } else if (!charToIntervals.equals( other.charToIntervals ))
      return false;
    if (totalIntervals != other.totalIntervals)
      return false;
    return true;
  }
}