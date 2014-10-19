package com.threebird.recorder.models;

public class KeyBehaviorMapping
{
  public Character key;
  public String behavior;

  public KeyBehaviorMapping( Character key, String behavior )
  {
    this.key = key;
    this.behavior = behavior;
  }

  public KeyBehaviorMapping( String key, String behavior )
  {
    this( key.charAt( 0 ), behavior );
  }
}
