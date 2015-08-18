package com.threebird.recorder.views.ioa;

import java.util.Map;
import java.util.Map.Entry;

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

      Text left = new Text( "" + key );
      Text mid = new Text( "" + calc.result1 );
      Text right = new Text( "" + calc.result2 );
      HBox row = new HBox( left, mid, right );
      
      this.getChildren().add( row );
    }
  }
}
