package com.threebird.recorder.models.behaviors;

public class DiscreteBehavior extends Behavior
{
  public final Integer time;

  public DiscreteBehavior( Character key, String description, Integer time )
  {
    super( key, description );
    this.time = time;
  }

  @Override public boolean isContinuous()
  {
    return false;
  }
}
