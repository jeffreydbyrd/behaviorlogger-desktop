package com.threebird.recorder.models.sessions;

import javafx.animation.Timeline;
import javafx.beans.property.SimpleIntegerProperty;

public class ContinuousCounter
{
  public final Timeline timer;
  public final SimpleIntegerProperty count;

  public ContinuousCounter( Timeline timer, SimpleIntegerProperty count )
  {
    this.timer = timer;
    this.count = count;
  }
}
