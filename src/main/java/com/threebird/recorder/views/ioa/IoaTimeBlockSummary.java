package com.threebird.recorder.views.ioa;

import java.util.Map;
import java.util.Map.Entry;

import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import com.threebird.recorder.utils.ioa.IntervalCalculations;

public class IoaTimeBlockSummary extends VBox
{
  private int colWidth = 35;
  private Font boldVerdana = Font.font( "Verdana", FontWeight.BOLD, 12 );

  public IoaTimeBlockSummary( Map< Character, IntervalCalculations > intervals )
  {
    this.setSpacing( 5 );

    // Setup table title
    Text txt = new Text( "IOA Time Block Summary" );
    txt.setFont( boldVerdana );
    this.getChildren().add( txt );

    // Headers
    Label keys = new Label( "Keys" );
    keys.setMinWidth( colWidth );
    keys.setAlignment( Pos.TOP_RIGHT );
    keys.setFont( boldVerdana );
    Label avgIoa = new Label( "Average IOA" );
    avgIoa.setFont( boldVerdana );
    HBox header = new HBox( keys, new Separator( Orientation.VERTICAL ), avgIoa );
    header.setSpacing( 5 );
    this.getChildren().add( header );

    // Key | Average summaries
    for (Entry< Character, IntervalCalculations > entry : intervals.entrySet()) {
      Character key = entry.getKey();
      IntervalCalculations calc = entry.getValue();

      Label left = new Label( "" + key );
      left.setMinWidth( colWidth );
      left.setAlignment( Pos.TOP_RIGHT );

      Separator sep = new Separator( Orientation.VERTICAL );

      String percent = String.format( "%.2f", calc.avg * 100 );
      Label right = new Label( "%" + percent );

      HBox row = new HBox( left, sep, right );
      row.setSpacing( 5 );

      this.getChildren().add( row );
    }
  }
}
