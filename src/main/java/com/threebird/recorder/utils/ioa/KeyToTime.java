package com.threebird.recorder.utils.ioa;

import java.util.HashMap;

import com.google.common.collect.Multiset;

/**
 * A simple typedef for a Map that maps each key to all the intervals that they
 * occurred in. It also contains a reference to the total number of intervals
 */
public class KeyToTime extends HashMap< Character, Multiset< Integer >>
{
  private static final long serialVersionUID = 1091663297033821383L;

  public int totalIntervals;
}