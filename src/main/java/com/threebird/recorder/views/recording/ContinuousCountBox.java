package com.threebird.recorder.views.recording;

import com.threebird.recorder.models.behaviors.ContinuousBehavior;
import com.threebird.recorder.models.schemas.KeyBehaviorMapping;

/**
 * A {@link BehaviorCountBox} used for keeping track of a
 * {@link ContinuousBehavior}. Whenever you toggle the counter on, it records
 * the time and begins counting up. When you toggle if off, it stops
 * incrementing
 */
public class ContinuousCountBox extends BehaviorCountBox
{
  private boolean toggled = false;

  public ContinuousCountBox( KeyBehaviorMapping kbm )
  {
    super( kbm );
  }

  @Override public boolean toggle()
  {
    toggled = !toggled;
    if (toggled) {
      this.setStyle( TOGGLED_STYLE );
    } else {
      this.setStyle( "" );
    }

    return toggled;
  }
}
