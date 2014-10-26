package com.threebird.recorder.models;

import java.util.HashMap;
import java.util.Optional;

/**
 * Represents a set of key-behavior mappings
 */
public class Schema
{
  public Integer id;
  public String name;
  public HashMap< Character, KeyBehaviorMapping > mappings;
  public int duration;

  public Schema( String name,
                 HashMap< Character, KeyBehaviorMapping > mappings,
                 int duration )
  {
    this.name = name;
    this.mappings = mappings;
    this.duration = duration;
  }

  public Schema( String name )
  {
    this( name, new HashMap< Character, KeyBehaviorMapping >(), 0 );
  }

  /**
   * Returns the KeyBehaviorMapping that corresponds to 'key', wrapped in an
   * {@link Optional} if found. If the key is not found, it returns an empty
   * Optional
   */
  public Optional< KeyBehaviorMapping > getMapping( Character key )
  {
    return Optional.ofNullable( mappings.get( key ) );
  }
}
