package com.threebird.recorder.models;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.threebird.recorder.models.behaviors.Behavior;
import com.threebird.recorder.models.behaviors.ContinuousBehavior;
import com.threebird.recorder.models.behaviors.DiscreteBehavior;

/**
 * Contains a list of {@link DiscreteBehavior} and a list of
 * {@link ContinuousBehavior} and various helper methods.
 */
public class Recording
{
  private List< DiscreteBehavior > discrete;
  private List< ContinuousBehavior > continuous;

  public Recording()
  {
    discrete = Lists.newArrayList();
    continuous = Lists.newArrayList();
  }

  public void log( DiscreteBehavior db )
  {
    discrete.add( db );
  }

  public void log( ContinuousBehavior cb )
  {
    continuous.add( cb );
  }

  public List< DiscreteBehavior > getDiscreteBehaviors()
  {
    return Lists.newArrayList( discrete );
  }

  public List< ContinuousBehavior > getContinuousBehaviors()
  {
    return Lists.newArrayList( continuous );
  }

  public List< Behavior > getAllBehaviors()
  {
    ArrayList< Behavior > result = Lists.newArrayList( discrete );
    result.addAll( continuous );
    return result;
  }
}
