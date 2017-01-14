package com.threebird.recorder.models.schemas;

import com.threebird.recorder.models.MappableChar;

/**
 * Maps a key to a behavior. We use this in configuring a {@link SchemaVersion}
 */
public class KeyBehaviorMapping
{
  public String uuid;
  public MappableChar key;
  public String description;
  public boolean isContinuous;
  public boolean archived;

  public KeyBehaviorMapping( String uuid,
                             MappableChar key,
                             String behavior,
                             boolean isContinuous,
                             boolean archived )
  {
    this.uuid = uuid;
    this.key = key;
    this.description = behavior;
    this.isContinuous = isContinuous;
    this.archived = archived;
  }

  public KeyBehaviorMapping( String uuid,
                             String key,
                             String behavior,
                             boolean isContinuous,
                             boolean archived )
  {
    this( uuid,
          MappableChar.getForChar( key.charAt( 0 ) ).get(),
          behavior,
          isContinuous,
          archived );
  }
  
  public KeyBehaviorMapping( String uuid,
                             Character key,
                             String behavior,
                             boolean isContinuous,
                             boolean archived )
  {
    this( uuid,
          MappableChar.getForChar( key ).get(),
          behavior,
          isContinuous,
          archived );
  }

  @Override public String toString()
  {
    return "KeyBehaviorMapping [uuid=" + uuid + ", key=" + key + ", description=" + description + ", isContinuous="
        + isContinuous + ", archived=" + archived + "]";
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + (archived ? 1231 : 1237);
    result = prime * result + ((description == null) ? 0 : description.hashCode());
    result = prime * result + (isContinuous ? 1231 : 1237);
    result = prime * result + ((key == null) ? 0 : key.hashCode());
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
    KeyBehaviorMapping other = (KeyBehaviorMapping) obj;
    if (archived != other.archived)
      return false;
    if (description == null) {
      if (other.description != null)
        return false;
    } else if (!description.equals( other.description ))
      return false;
    if (isContinuous != other.isContinuous)
      return false;
    if (key != other.key)
      return false;
    if (uuid == null) {
      if (other.uuid != null)
        return false;
    } else if (!uuid.equals( other.uuid ))
      return false;
    return true;
  }
}
