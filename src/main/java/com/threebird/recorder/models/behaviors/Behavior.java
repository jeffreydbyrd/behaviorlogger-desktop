package com.threebird.recorder.models.behaviors;

import com.threebird.recorder.models.MappableChar;

/**
 * An actual behavior that a researcher observed during a recording session
 */
public abstract class Behavior
{
  public final MappableChar key;
  public final String description;

  Behavior( MappableChar key, String description )
  {
    this.key = key;
    this.description = description;
  }

  abstract boolean isContinuous();
}
