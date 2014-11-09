package com.threebird.recorder.models.behaviors;

/**
 * An actual behavior that a researcher observed during a recording session
 */
public abstract class Behavior
{
  public final Character key;
  public final String description;

  Behavior( Character key, String description )
  {
    this.key = key;
    this.description = description;
  }

  abstract boolean isContinuous();
}
