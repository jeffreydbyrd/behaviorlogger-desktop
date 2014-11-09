package com.threebird.recorder.views.recording;

import com.threebird.recorder.models.KeyBehaviorMapping;

public class DiscreteCountBox extends BehaviorCountBox
{
  public DiscreteCountBox( KeyBehaviorMapping kbm )
  {
    super( kbm );
  }

  @Override public void toggle()
  {
    count += 1;
    countLbl.setText( count.toString() );
  }
}
