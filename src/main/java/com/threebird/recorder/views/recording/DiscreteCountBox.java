package com.threebird.recorder.views.recording;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.Animation.Status;
import javafx.util.Duration;

import com.threebird.recorder.models.behaviors.DiscreteBehavior;
import com.threebird.recorder.models.schemas.KeyBehaviorMapping;

/**
 * A {@link BehaviorCountBox} that counts {@link DiscreteBehavior}s. When
 * toggled, it simply increments the counter by 1
 */
public class DiscreteCountBox extends BehaviorCountBox
{
  private Timeline timer;

  public DiscreteCountBox( KeyBehaviorMapping kbm )
  {
    super( kbm );
  }

  @Override public boolean toggle()
  {
    this.setStyle( TOGGLED_STYLE );

    DiscreteCountBox self = this;

    if (timer != null && timer.getStatus() == Status.RUNNING) {
      timer.stop();
    }

    timer = new Timeline();
    timer.setCycleCount( 1 );
    KeyFrame kf = new KeyFrame( Duration.millis( 100 ), evt -> {
      self.setStyle( "" );
      timer.stop();
    } );
    timer.getKeyFrames().add( kf );
    timer.play();

    return true;
  }
}
