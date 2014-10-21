package com.threebird.recorder.models.behaviors;

public class DurationalBehavior implements Behavior
{
  final public String description;
  final public Integer start;
  final public Integer end;

  public DurationalBehavior( String description, Integer start, Integer end )
  {
    this.description = description;
    this.start = start;
    this.end = end;
  }

  @Override public boolean isDurational()
  {
    return true;
  }
}
