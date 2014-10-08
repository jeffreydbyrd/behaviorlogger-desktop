package com.threebird.recorder.models;

import java.util.HashMap;

/**
 * Represents a set of key-behavior mappings
 */
public class Schema
{
  public String name;
  public HashMap< Character, String > mappings;

  public Schema( String name, HashMap< Character, String > mappings )
  {
    this.name = name;
    this.mappings = mappings;
  }

  public Schema( String name )
  {
    this( name, new HashMap< Character, String >() );
  }
}
