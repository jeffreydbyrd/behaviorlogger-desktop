package com.behaviorlogger.views.ioa;

import java.util.Map;
import java.util.Map.Entry;

import com.behaviorlogger.utils.ioa.IntervalCalculations;

import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class IoaTimeBlockSummary extends VBox
{
  private int colWidth = 35;
  private Font boldVerdana = Font.font( "Verdana", FontWeight.BOLD, 12 );

  public IoaTimeBlockSummary( Map< String, IntervalCalculations > intervals )
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
    for (Entry< String, IntervalCalculations > entry : intervals.entrySet()) {
      String buuid = entry.getKey();
      IntervalCalculations calc = entry.getValue();

      Label left = new Label( "" + buuid );
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
