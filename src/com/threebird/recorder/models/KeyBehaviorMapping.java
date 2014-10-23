package com.threebird.recorder.models;

public class KeyBehaviorMapping
{
  public final Character key;
  public final String behavior;
  public final boolean isContinuous;

  public KeyBehaviorMapping( Character key,
                             String behavior,
                             boolean isDurational )
  {
    this.key = key;
    this.behavior = behavior;
    this.isContinuous = isDurational;
  }

  public KeyBehaviorMapping( String key, String behavior, boolean isDurational )
  {
    this( key.charAt( 0 ), behavior, isDurational );
  }

  @Override public String toString()
  {
    return "KeyBehaviorMapping [key=" + key + ", behavior=" + behavior
        + ", isContinuous=" + isContinuous + "]";
  }
}
