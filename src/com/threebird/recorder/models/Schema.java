package com.threebird.recorder.models;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Represents a set of key-behavior mappings
 */
public class Schema
{
  public String name;
  public ArrayList< KeyBehaviorMapping > mappings;

  public Schema( String name, ArrayList< KeyBehaviorMapping > mappings )
  {
    this.name = name;
    this.mappings = mappings;
  }

  public Schema( String name )
  {
    this( name, new ArrayList< KeyBehaviorMapping >() );
  }

  /**
   * Returns the KeyBehaviorMapping that corresponds to 'key', wrapped in an
   * {@link Optional} if found. If the key is not found, it returns an empty
   * Optional
   */
  public Optional< KeyBehaviorMapping > getMapping( Character key )
  {
    for (KeyBehaviorMapping mapping : mappings) {
      if (mapping.key.equals( key )) {
        return Optional.of( mapping );
      }
    }
    return Optional.empty();
  }
}
