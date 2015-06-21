package com.threebird.recorder.views.recording;

import javafx.animation.Animation.Status;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import com.threebird.recorder.models.schemas.KeyBehaviorMapping;

/**
 * When toggled, the background flashes green for 100 milliseconds
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

  @Override public void setCount( Integer count )
  {
    countLbl.setText( count.toString() );
  }
}
