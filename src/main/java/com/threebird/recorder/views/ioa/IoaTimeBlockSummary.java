package com.threebird.recorder.views.ioa;

import java.util.Map;
import java.util.Map.Entry;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import com.threebird.recorder.utils.ioa.IntervalCalculations;

public class IoaTimeBlockSummary extends VBox
{
  public IoaTimeBlockSummary( Map< Character, IntervalCalculations > intervals )
  {
    Text txt = new Text( "IOA Time Block Summary" );
    txt.setFont( Font.font( "Verdana", 12 ) );
    this.getChildren().add( txt );

    for (Entry< Character, IntervalCalculations > entry : intervals.entrySet()) {
      Character key = entry.getKey();
      IntervalCalculations calc = entry.getValue();

      Text left = new Text( "" + key );
      Text right = new Text( "" + calc.avg );
      HBox row = new HBox( left, right );
      this.getChildren().add( row );
    }
  }
}
