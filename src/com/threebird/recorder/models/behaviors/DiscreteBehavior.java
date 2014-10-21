package com.threebird.recorder.models.behaviors;

public class DiscreteBehavior implements Behavior
{
  public final String description;
  public final Integer time;

  public DiscreteBehavior( String description, Integer time )
  {
    this.description = description;
    this.time = time;
  }

  @Override public boolean isDurational()
  {
    return true;
  }
}
