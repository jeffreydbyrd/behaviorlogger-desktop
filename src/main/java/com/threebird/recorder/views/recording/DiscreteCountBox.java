package com.threebird.recorder.views.recording;

import com.threebird.recorder.models.KeyBehaviorMapping;

public class DiscreteCountBox extends BehaviorCountBox
{
  public DiscreteCountBox( KeyBehaviorMapping kbm )
  {
    super( kbm );
  }

  @Override public boolean toggle()
  {
    incrementCount();
    return true;
  }
}
