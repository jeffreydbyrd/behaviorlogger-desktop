package com.threebird.recorder.models.schemas;

import com.threebird.recorder.models.MappableChar;


/**
 * Maps a key to a behavior. We use this in configuring a {@link Schema}
 */
public class KeyBehaviorMapping
{
  public final MappableChar key;
  public final String behavior;
  public final boolean isContinuous;

  public KeyBehaviorMapping( MappableChar key,
                             String behavior,
                             boolean isContinuous )
  {
    this.key = key;
    this.behavior = behavior;
    this.isContinuous = isContinuous;
  }

  public KeyBehaviorMapping( String key, String behavior, boolean isContinuous )
  {
    this( MappableChar.getForChar( key.charAt( 0 ) ).get(), behavior, isContinuous );
  }

  @Override public String toString()
  {
    return "KeyBehaviorMapping [key=" + key + ", behavior=" + behavior
        + ", isContinuous=" + isContinuous + "]";
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((behavior == null) ? 0 : behavior.hashCode());
    result = prime * result + (isContinuous ? 1231 : 1237);
    result = prime * result + ((key == null) ? 0 : key.hashCode());
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
    if (behavior == null) {
      if (other.behavior != null)
        return false;
    } else if (!behavior.equals( other.behavior ))
      return false;
    if (isContinuous != other.isContinuous)
      return false;
    if (key == null) {
      if (other.key != null)
        return false;
    } else if (!key.equals( other.key ))
      return false;
    return true;
  }

}
