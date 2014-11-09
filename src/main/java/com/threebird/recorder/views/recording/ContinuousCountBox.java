package com.threebird.recorder.views.recording;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.util.Duration;

import com.threebird.recorder.models.KeyBehaviorMapping;
import com.threebird.recorder.models.behaviors.ContinuousBehavior;

/**
 * A {@link BehaviorCountBox} used for keeping track of a
 * {@link ContinuousBehavior}. Whenever you toggle the counter on, it records
 * the time and begins counting up. When you toggle if off, it stops
 * incrementing
 */
public class ContinuousCountBox extends BehaviorCountBox
{
  private boolean toggled = false;
  private int start = 0;

  public ContinuousCountBox( KeyBehaviorMapping kbm, Timeline timer )
  {
    super( kbm );
    KeyFrame kf = new KeyFrame( Duration.seconds( 1 ), this::onTick );
    timer.getKeyFrames().add( kf );
  }

  private void onTick( ActionEvent evt )
  {
    if (toggled) {
      incrementCount();
    }
  }

  /**
   * Returns the time at which you last toggled on this counter
   */
  public int getLastStart()
  {
    return start;
  }

  @Override public boolean toggle()
  {
    toggled = !toggled;
    if (toggled) {
      start = count;
      this.setStyle( TOGGLED_STYLE );
    } else {
      this.setStyle( "" );
    }

    return toggled;
  }
}
