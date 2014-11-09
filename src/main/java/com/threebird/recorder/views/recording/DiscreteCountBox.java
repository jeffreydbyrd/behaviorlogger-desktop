package com.threebird.recorder.views.recording;

import com.threebird.recorder.models.KeyBehaviorMapping;
import com.threebird.recorder.models.behaviors.DiscreteBehavior;

/**
 * A {@link BehaviorCountBox} that counts {@link DiscreteBehavior}s. When
 * toggled, it simply increments the counter by 1
 */
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
