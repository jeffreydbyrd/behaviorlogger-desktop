package com.threebird.recorder.views.recording;

import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import com.threebird.recorder.models.schemas.KeyBehaviorMapping;

/**
 * An HBox that displays a {@link KeyBehaviorMapping} and also keeps track of a
 * counter for that behavior.
 */
public abstract class BehaviorCountBox extends HBox
{
  protected static String TOGGLED_STYLE = "-fx-background-color:#7DFF86";
  
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

  /**
   * Increments 'count' and sets it to the countLbl
   */
  protected void incrementCount()
  {
    count += 1;
    countLbl.setText( count.toString() );
  }

  /**
   * Call this method when the user presses a key that maps to this behavior. If
   * this is a {@link ContinuousCountBox}, it returns true if toggled on or
   * false if toggled off. If this is a {@link DiscreteCountBox}, toggle() will
   * always return true.
   */
  public abstract boolean toggle();
}
