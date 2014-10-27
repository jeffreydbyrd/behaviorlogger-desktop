package com.threebird.recorder.models;

import java.util.HashMap;
import java.util.Optional;

import com.google.common.collect.Maps;

/**
 * Represents a set of key-behavior mappings
 */
public class Schema
{
  public Integer id;
  public String name;
  public HashMap< Character, KeyBehaviorMapping > mappings = Maps.newHashMap();
  public int duration = 0;

  /**
   * Returns the KeyBehaviorMapping that corresponds to 'key', wrapped in an
   * {@link Optional} if found. If the key is not found, it returns an empty
   * Optional
   */
  public Optional< KeyBehaviorMapping > getMapping( Character key )
  {
    return Optional.ofNullable( mappings.get( key ) );
  }

  public void addMapping( KeyBehaviorMapping mapping )
  {
    mappings.put( mapping.key, mapping );
  }
}
