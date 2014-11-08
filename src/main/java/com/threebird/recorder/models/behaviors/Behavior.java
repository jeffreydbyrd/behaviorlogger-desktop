package com.threebird.recorder.models.behaviors;

public abstract class Behavior
{
  public final String key;
  public final String description;

  Behavior( String key, String description )
  {
    this.key = key;
    this.description = description;
  }

  abstract boolean isContinuous();
}
