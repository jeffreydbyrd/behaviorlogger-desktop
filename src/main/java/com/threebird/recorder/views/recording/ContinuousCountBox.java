package com.threebird.recorder.views.recording;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import com.threebird.recorder.models.KeyBehaviorMapping;

public class ContinuousCountBox extends BehaviorCountBox
{
  private Timeline timer;
  private boolean toggled = false;

  public ContinuousCountBox( KeyBehaviorMapping kbm )
  {
    super( kbm );
    timer = new Timeline();
    timer.setCycleCount( Animation.INDEFINITE );
    KeyFrame kf = new KeyFrame( Duration.seconds( 1 ), evt -> incrementCount() );
    timer.getKeyFrames().add( kf );
  }

  public int getLatestTime()
  {
    return 0;
  }

  @Override public boolean toggle()
  {
    toggled = !toggled;
    if (toggled) {
      timer.play();
    } else {
      timer.pause();
    }

    return toggled;
  }
}
