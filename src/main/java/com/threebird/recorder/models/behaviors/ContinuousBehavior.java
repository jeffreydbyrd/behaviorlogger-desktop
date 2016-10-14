package com.threebird.recorder.models.behaviors;

import com.threebird.recorder.models.MappableChar;
import com.threebird.recorder.utils.BehaviorLoggerUtil;

public class ContinuousBehavior extends Behavior
{
  private Integer duration;

  /**
   * @param uuid
   * @param key
   * @param description
   * @param start
   *          - start-time in millis
   * @param duration
   *          - duration of the behavior in millis
   */
  public ContinuousBehavior( String uuid, MappableChar key, String description, Integer start, Integer duration )
  {
    super( uuid, key, description, start );
    this.duration = duration;
  }

  @Override public boolean isContinuous()
  {
    return true;
  }

  /**
   * @return the duration of this behavior in milliseconds
   */
  public int getDuration()
  {
    return duration;
  }

  @Override public String timeDisplay()
  {
    String start = BehaviorLoggerUtil.millisToTimestampNoSpaces( startTime );
    String end = BehaviorLoggerUtil.millisToTimestampNoSpaces( startTime + duration );
    return String.format( "%s-%s", start, end );
  }

  @Override public String type()
  {
    return "continuous";
  }
}
