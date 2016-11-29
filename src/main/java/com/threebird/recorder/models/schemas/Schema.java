package com.threebird.recorder.models.schemas;

import java.util.HashMap;
import java.util.Map.Entry;
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
  public HashMap< MappableChar, KeyBehaviorMapping > behaviors = Maps.newHashMap();
  public Integer duration; // in milliseconds
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
    return Optional.ofNullable( behaviors.get( key ) );
  }

  public void addMapping( KeyBehaviorMapping mapping )
  {
    behaviors.put( mapping.key, mapping );
  }

  @Override public String toString()
  {
    return "Schema [uuid=" + uuid + ", version=" + version + ", client=" + client + ", project=" + project
        + ", duration=" + duration + ", pause=" + pause + ", color=" + color
        + ", sound=" + sound + ", archived=" + archived + "]";
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

  private static boolean isDifferent( HashMap< MappableChar, KeyBehaviorMapping > m1,
                                      HashMap< MappableChar, KeyBehaviorMapping > m2 )
  {
    if (!m1.equals( m2 )) {
      return true;
    }

    for (Entry< MappableChar, KeyBehaviorMapping > e : m1.entrySet()) {
      KeyBehaviorMapping kbm1 = e.getValue();
      KeyBehaviorMapping kbm2 = m2.get( e.getKey() );

      if (KeyBehaviorMapping.isDifferent( kbm1, kbm2 )) {
        return true;
      }
    }

    return false;
  }

  public static boolean isDifferent( Schema s1, Schema s2 )
  {
    if (!s1.uuid.equals( s2.uuid )) {
      return true;
    }
    if (!s1.version.equals( s2.version )) {
      return true;
    }
    if (!s1.client.equals( s2.client )) {
      return true;
    }
    if (!s1.project.equals( s2.project )) {
      return true;
    }
    if (!s1.duration.equals( s2.duration )) {
      return true;
    }
    if (!s1.pause.equals( s2.pause )) {
      return true;
    }
    if (!s1.color.equals( s2.color )) {
      return true;
    }
    if (!s1.sound.equals( s2.sound )) {
      return true;
    }
    if (!s1.archived.equals( s2.archived )) {
      return true;
    }

    if (isDifferent( s1.behaviors, s2.behaviors )) {
      return true;
    }

    return false;
  }
}
