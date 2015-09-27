package com.threebird.recorder.models.behaviors;

import java.util.Comparator;

import com.threebird.recorder.models.MappableChar;

/**
 * An actual behavior that a researcher observed during a recording session
 */
public abstract class Behavior
{
  public static final Comparator< Behavior > comparator = ( Behavior o1, Behavior o2 ) -> o1.startTime - o2.startTime;

  public final int startTime;
  public final MappableChar key;
  public final String name;

  /**
   * @param key
   * @param description
   * @param startTime
   *          - start-time in millis
   */
  Behavior( MappableChar key, String description, int startTime )
  {
    this.key = key;
    this.name = description;
    this.startTime = startTime;
  }

  public abstract boolean isContinuous();

  public abstract String timeDisplay();

  public abstract String type();
}
