package com.threebird.recorder.views.recording;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.util.Duration;

import com.threebird.recorder.models.KeyBehaviorMapping;

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

  public int getLastStart()
  {
    return start;
  }

  @Override public boolean toggle()
  {
    toggled = !toggled;
    if (toggled) {
      start = count;
    }

    return toggled;
  }
}
