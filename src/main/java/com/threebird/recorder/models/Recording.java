package com.threebird.recorder.models;

import java.util.List;

import com.google.common.collect.Lists;
import com.threebird.recorder.models.behaviors.Behavior;

public class Recording
{
  private List< Behavior > behaviors;

  Recording()
  {
    behaviors = Lists.newArrayList();
  }

  public void log( Behavior b )
  {
    behaviors.add( b );
  }
}
