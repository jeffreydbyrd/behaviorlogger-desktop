package com.threebird.recorder.models.behaviors;

import com.threebird.recorder.models.MappableChar;
import com.threebird.recorder.utils.BehaviorLoggerUtil;

public class DiscreteBehavior extends BehaviorEvent
{
  public DiscreteBehavior( String uuid, MappableChar key, String description, Integer time )
  {
    super( uuid, key, description, time );
  }

  @Override public boolean isContinuous()
  {
    return false;
  }

  @Override public String timeDisplay()
  {
    return BehaviorLoggerUtil.millisToTimestampNoSpaces( startTime );
  }

  @Override public String type()
  {
    return "discrete";
  }
}
