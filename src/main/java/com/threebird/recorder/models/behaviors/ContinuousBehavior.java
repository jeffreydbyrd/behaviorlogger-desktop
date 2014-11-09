package com.threebird.recorder.models.behaviors;

public class ContinuousBehavior extends Behavior
{
  final public Integer start;
  private Integer duration;

  public ContinuousBehavior( Character key, String description, Integer start, Integer duration )
  {
    super( key, description );
    this.start = start;
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
}
