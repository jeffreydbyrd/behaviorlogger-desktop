package com.behaviorlogger.models.schemas;

import java.util.List;
import java.util.Set;

import com.behaviorlogger.models.MappableChar;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class SchemaVersion
{
  public String uuid;
  public String versionUuid;
  public Integer versionNumber;
  public Set< String > parentVersionSet;
  public String client;
  public String project;
  public Integer duration; // in milliseconds
  public Boolean pause;
  public Boolean color;
  public Boolean sound;
  public Boolean archived;
  public List< KeyBehaviorMapping > behaviors = Lists.newArrayList();

  public ImmutableMap< MappableChar, KeyBehaviorMapping > behaviorsMap()
  {
    return Maps.uniqueIndex( behaviors, b -> b.key );
  }

  @Override public String toString()
  {
    return "SchemaVersion [uuid=" + uuid + ", versionUuid=" + versionUuid + ", versionNumber=" + versionNumber
        + ", parentVersionSet=" + parentVersionSet + ", client=" + client + ", project=" + project + ", duration="
        + duration + ", pause=" + pause + ", color=" + color + ", sound=" + sound + ", archived=" + archived
        + ", behaviors=" + behaviors + "]";
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((archived == null) ? 0 : archived.hashCode());
    result = prime * result + ((behaviors == null) ? 0 : behaviors.hashCode());
    result = prime * result + ((client == null) ? 0 : client.hashCode());
    result = prime * result + ((color == null) ? 0 : color.hashCode());
    result = prime * result + ((duration == null) ? 0 : duration.hashCode());
    result = prime * result + ((parentVersionSet == null) ? 0 : parentVersionSet.hashCode());
    result = prime * result + ((pause == null) ? 0 : pause.hashCode());
    result = prime * result + ((project == null) ? 0 : project.hashCode());
    result = prime * result + ((sound == null) ? 0 : sound.hashCode());
    result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
    result = prime * result + ((versionNumber == null) ? 0 : versionNumber.hashCode());
    result = prime * result + ((versionUuid == null) ? 0 : versionUuid.hashCode());
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
    SchemaVersion other = (SchemaVersion) obj;
    if (archived == null) {
      if (other.archived != null)
        return false;
    } else if (!archived.equals( other.archived ))
      return false;
    if (behaviors == null) {
      if (other.behaviors != null)
        return false;
    } else if (!behaviors.equals( other.behaviors ))
      return false;
    if (client == null) {
      if (other.client != null)
        return false;
    } else if (!client.equals( other.client ))
      return false;
    if (color == null) {
      if (other.color != null)
        return false;
    } else if (!color.equals( other.color ))
      return false;
    if (duration == null) {
      if (other.duration != null)
        return false;
    } else if (!duration.equals( other.duration ))
      return false;
    if (parentVersionSet == null) {
      if (other.parentVersionSet != null)
        return false;
    } else if (!parentVersionSet.equals( other.parentVersionSet ))
      return false;
    if (pause == null) {
      if (other.pause != null)
        return false;
    } else if (!pause.equals( other.pause ))
      return false;
    if (project == null) {
      if (other.project != null)
        return false;
    } else if (!project.equals( other.project ))
      return false;
    if (sound == null) {
      if (other.sound != null)
        return false;
    } else if (!sound.equals( other.sound ))
      return false;
    if (uuid == null) {
      if (other.uuid != null)
        return false;
    } else if (!uuid.equals( other.uuid ))
      return false;
    if (versionNumber == null) {
      if (other.versionNumber != null)
        return false;
    } else if (!versionNumber.equals( other.versionNumber ))
      return false;
    if (versionUuid == null) {
      if (other.versionUuid != null)
        return false;
    } else if (!versionUuid.equals( other.versionUuid ))
      return false;
    return true;
  }
}