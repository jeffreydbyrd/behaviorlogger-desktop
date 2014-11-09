package com.threebird.recorder.views.recording;

import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import com.threebird.recorder.models.KeyBehaviorMapping;

public abstract class BehaviorCountBox extends HBox
{
  protected Integer count = 0;
  public final Label keyLabel;
  public final Label behaviorLbl;
  public final Label countLbl;

  public BehaviorCountBox( KeyBehaviorMapping kbm )
  {
    super();

    keyLabel = new Label( kbm.key.toString() );
    keyLabel.setAlignment( Pos.CENTER );
    keyLabel.setMinWidth( 30 );
    keyLabel.setMaxWidth( 30 );
    HBox.setHgrow( keyLabel, Priority.NEVER );

    behaviorLbl = new Label( kbm.behavior );
    behaviorLbl.setWrapText( true );
    HBox.setHgrow( behaviorLbl, Priority.ALWAYS );
    behaviorLbl.setMaxWidth( Double.MAX_VALUE );

    countLbl = new Label( count.toString() );
    countLbl.setAlignment( Pos.CENTER );
    countLbl.setMinWidth( 60 );
    countLbl.setMaxWidth( 60 );
    HBox.setHgrow( countLbl, Priority.NEVER );

    Separator s1 = new Separator( Orientation.VERTICAL );
    Separator s2 = new Separator( Orientation.VERTICAL );

    this.getChildren().addAll( keyLabel, s1, behaviorLbl, s2, countLbl );
    this.setSpacing( 5 );
  }

  public abstract void toggle();
}
