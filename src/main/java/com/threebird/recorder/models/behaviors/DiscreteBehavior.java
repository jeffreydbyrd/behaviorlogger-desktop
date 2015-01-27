package com.threebird.recorder.models.behaviors;

import com.threebird.recorder.models.MappableChar;
import com.threebird.recorder.utils.EventRecorderUtil;

public class DiscreteBehavior extends Behavior
{
  public final Integer time;

  public DiscreteBehavior( MappableChar key, String description, Integer time )
  {
    super( key, description, time );
    this.time = time;
  }

  @Override public boolean isContinuous()
  {
    return false;
  }

  @Override public String timeDisplay()
  {
    return EventRecorderUtil.millisToTimestamp( startTime );
  }
}
