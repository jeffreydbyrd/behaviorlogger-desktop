package com.threebird.recorder.utils.ioa;

import java.util.HashMap;

import com.google.common.collect.Multiset;

/**
 * A simple typedef for a Map that maps each key to all the seconds that they
 * occurred in.
 */
class KeyToTime extends HashMap< Character, Multiset< Integer >>
{
  private static final long serialVersionUID = 1091663297033821383L;
}