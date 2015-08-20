package com.threebird.recorder.views.ioa;

import java.util.Map;
import java.util.Map.Entry;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
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

      Label left = new Label( "" + key );
      left.setMinWidth( 25 );
      left.setAlignment( Pos.TOP_RIGHT );

      Label right = new Label( " - " + calc.avg );

      HBox row = new HBox( left, right );
      row.setSpacing( 5 );

      this.getChildren().add( row );
    }
  }
}
