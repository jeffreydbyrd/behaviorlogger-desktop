package com.threebird.recorder.models;

import java.io.File;
import java.util.HashMap;
import java.util.Optional;

import com.google.common.collect.Maps;

/**
 * Contains all session configurations, such as session duration and
 * key-behavior mappings
 */
public class Schema
{
  public Integer id;
  public String client;
  public String project;
  public HashMap< MappableChar, KeyBehaviorMapping > mappings = Maps.newHashMap();
  public File sessionDirectory = new File( System.getProperty( "user.home" ) );
  public int duration = 0;
  public boolean pause = false;
  public boolean color = false;
  public boolean sound = false;

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
