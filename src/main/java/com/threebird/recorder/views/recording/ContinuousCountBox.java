package com.threebird.recorder.views.recording;

import com.threebird.recorder.models.behaviors.ContinuousBehavior;
import com.threebird.recorder.models.schemas.KeyBehaviorMapping;

/**
 * A {@link BehaviorCountBox} used for keeping track of a
 * {@link ContinuousBehavior}. Whenever you toggle the counter on, it turns
 * green. When you toggle if off, the green is removed
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

  @Override public void setCount( Integer millis )
  {
    int seconds = millis / 1000;
    if (seconds < 0) {
      seconds = 0;
    }

    this.countLbl.setText( "" + seconds );
  }
}
