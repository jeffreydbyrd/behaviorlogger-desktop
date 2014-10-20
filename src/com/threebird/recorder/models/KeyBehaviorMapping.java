package com.threebird.recorder.models;

public class KeyBehaviorMapping
{
  public final Character key;
  public final String behavior;
  public final boolean isDurational;

  public KeyBehaviorMapping( Character key,
                             String behavior,
                             boolean isDurational )
  {
    this.key = key;
    this.behavior = behavior;
    this.isDurational = isDurational;
  }

  public KeyBehaviorMapping( String key, String behavior, boolean isDurational )
  {
    this( key.charAt( 0 ), behavior, isDurational );
  }

  @Override public String toString()
  {
    return "KeyBehaviorMapping [key=" + key + ", behavior=" + behavior
        + ", isDurational=" + isDurational + "]";
  }
}
