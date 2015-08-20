package com.threebird.recorder.views.ioa;

import java.util.Map;
import java.util.Map.Entry;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import com.threebird.recorder.utils.ioa.TimeWindowCalculations;

public class IoaTimeWindowSummary extends VBox
{
  public IoaTimeWindowSummary( Map< Character, TimeWindowCalculations > ioaDiscrete,
                               Map< Character, Double > ioaContinuous )
  {
    Text txt = new Text( "IOA Discrete Time Window Summary" );
    txt.setFont( Font.font( "Verdana", 12 ) );
    this.getChildren().add( txt );

    for (Entry< Character, TimeWindowCalculations > entry : ioaDiscrete.entrySet()) {
      Character key = entry.getKey();
      TimeWindowCalculations calc = entry.getValue();

      Label left = new Label( "" + key );
      left.setMinWidth( 25 );
      left.setAlignment( Pos.TOP_RIGHT );

      Label mid = new Label( " - " + calc.result1 );
      Label right = new Label( ", " + calc.result1 );

      HBox row = new HBox( left, mid, right );
      row.setSpacing( 5 );

      this.getChildren().add( row );
    }
  }
}
