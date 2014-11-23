package com.threebird.recorder.views.recording;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleBooleanProperty;
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
  private Timeline timer;
  private boolean toggled = false;
  private int start = 0;

  public ContinuousCountBox( KeyBehaviorMapping kbm, SimpleBooleanProperty playingProperty )
  {
    super( kbm );
    timer = new Timeline();
    timer.setCycleCount( Animation.INDEFINITE );
    KeyFrame kf = new KeyFrame( Duration.seconds( 1 ), evt -> incrementCount() );
    timer.getKeyFrames().add( kf );

    playingProperty.addListener( ( obs, oldV, playing ) -> {
      if (playing) {
        timer.play();
      } else {
        timer.pause();
      }
    } );
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
      timer.play();
      this.setStyle( TOGGLED_STYLE );
    } else {
      timer.pause();
      this.setStyle( "" );
    }

    return toggled;
  }
}
