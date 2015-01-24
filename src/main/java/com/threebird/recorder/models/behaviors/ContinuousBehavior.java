package com.threebird.recorder.models.behaviors;

import com.threebird.recorder.models.MappableChar;
import com.threebird.recorder.utils.EventRecorderUtil;

public class ContinuousBehavior extends Behavior
{
  private Integer duration;

  public ContinuousBehavior( MappableChar key, String description, Integer start, Integer duration )
  {
    super( key, description, start );
    this.duration = duration;
  }

  @Override public boolean isContinuous()
  {
    return true;
  }

  public Integer getDuration()
  {
    return duration;
  }

  @Override public String timeDisplay()
  {
    String start = EventRecorderUtil.secondsToTimestamp( startTime );
    String end = EventRecorderUtil.secondsToTimestamp( startTime + duration );
    return String.format( "%s - %s", start, end );
  }
}
