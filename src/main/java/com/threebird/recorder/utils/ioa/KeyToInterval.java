package com.threebird.recorder.utils.ioa;

import java.util.HashMap;

import com.google.common.collect.Multiset;

/**
 * Maps each key to all the intervals that they occurred in. It also contains a reference to the total number of
 * intervals
 */
public class KeyToInterval
{
  public final HashMap< String, Multiset< Integer > > keyToIntervals;
  public final int totalIntervals;

  public KeyToInterval( HashMap< String, Multiset< Integer > > idToIntervals, int totalIntervals )
  {
    this.keyToIntervals = idToIntervals;
    this.totalIntervals = totalIntervals;
  }

  @Override public String toString()
  {
    StringBuilder sb = new StringBuilder();
    keyToIntervals.forEach( ( ch, intervals ) -> {
      sb.append( "  " + ch + "->" + intervals.toString() + "\n" );
    } );
    return "KeyToInterval [\n charToIntervals=\n" + sb.toString() + ", totalIntervals=" + totalIntervals + "\n]";
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((keyToIntervals == null) ? 0 : keyToIntervals.hashCode());
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
    if (keyToIntervals == null) {
      if (other.keyToIntervals != null)
        return false;
    } else if (!keyToIntervals.equals( other.keyToIntervals ))
      return false;
    if (totalIntervals != other.totalIntervals)
      return false;
    return true;
  }
}