package com.threebird.recorder.models.behaviors;

import com.threebird.recorder.models.MappableChar;
import com.threebird.recorder.utils.EventRecorderUtil;

public class ContinuousBehavior extends Behavior
{
  private Integer duration;

  /**
   * @param key
   * @param description
   * @param start
   *          - start-time in millis
   * @param duration
   *          - duration of the behavior in millis
   */
  public ContinuousBehavior( MappableChar key, String description, Integer start, Integer duration )
  {
    super( key, description, start );
    this.duration = duration;
  }

  public ContinuousBehavior( MappableChar key, String description, Integer start )
  {
    this( key, description, start, 0 );
  }

  @Override public boolean isContinuous()
  {
    return true;
  }

  public void setDuration( int duration )
  {
    this.duration = duration;
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
    String start = EventRecorderUtil.millisToTimestampNoSpaces( startTime );
    String end = EventRecorderUtil.millisToTimestampNoSpaces( startTime + duration );
    return String.format( "%s-%s", start, end );
  }

  @Override public String type()
  {
    return "continuous";
  }
}
