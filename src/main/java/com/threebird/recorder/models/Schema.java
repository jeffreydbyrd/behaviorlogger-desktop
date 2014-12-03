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

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
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
    Schema other = (Schema) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals( other.id ))
      return false;
    return true;
  }

}
