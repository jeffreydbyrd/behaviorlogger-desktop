package com.threebird.recorder.models.behaviors;

import com.threebird.recorder.models.MappableChar;

public class DiscreteBehavior extends Behavior
{
  public final Integer time;

  public DiscreteBehavior( MappableChar key, String description, Integer time )
  {
    super( key, description );
    this.time = time;
  }

  @Override public boolean isContinuous()
  {
    return false;
  }
}
