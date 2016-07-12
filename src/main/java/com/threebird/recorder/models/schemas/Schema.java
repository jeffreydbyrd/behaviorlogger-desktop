package com.threebird.recorder.models.schemas;

import java.io.File;
import java.util.HashMap;
import java.util.Optional;

import com.google.common.collect.Maps;
import com.threebird.recorder.models.MappableChar;

/**
 * Contains all session configurations, such as session duration and key-behavior mappings
 */
public class Schema
{
  public String uuid;
  public Integer version;
  public String client;
  public String project;
  public HashMap< MappableChar, KeyBehaviorMapping > mappings = Maps.newHashMap();
  public File sessionDirectory;
  public Integer duration; // in seconds
  public Boolean pause;
  public Boolean color;
  public Boolean sound;
  public Boolean archived;

  /**
   * Returns the KeyBehaviorMapping that corresponds to 'key', wrapped in an {@link Optional} if found. If the key is
   * not found, it returns an empty Optional
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
    result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
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
    if (uuid == null) {
      if (other.uuid != null)
        return false;
    } else if (!uuid.equals( other.uuid ))
      return false;
    return true;
  }

  @Override public String toString()
  {
    return "Schema [uuid=" + uuid + ", version=" + version + ", client=" + client + ", project=" + project
        + ", sessionDirectory=" + sessionDirectory + ", duration=" + duration + ", pause=" + pause + ", color=" + color
        + ", sound=" + sound + ", archived=" + archived + "]";
  }
}
