package com.threebird.recorder.models.behaviors;

public class ContinuousBehavior extends Behavior
{
  final public Integer start;
  private Integer duration;

  public ContinuousBehavior( Character key, String description, Integer start )
  {
    super( key, description );
    this.start = start;
    this.duration = 1;
  }

  @Override public boolean isContinuous()
  {
    return true;
  }

  public Integer getDuration()
  {
    return duration;
  }

  public void incDuration( Integer seconds )
  {
    duration += seconds;
  }
}
