package com.threebird.recorder.views.ioa;

import java.util.Map;
import java.util.Map.Entry;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import com.threebird.recorder.utils.ioa.TimeWindowCalculations;

public class IoaTimeWindowSummary extends VBox
{
  private int colWidth = 35;
  private Font boldVerdana = Font.font( "Verdana", FontWeight.BOLD, 12 );

  public IoaTimeWindowSummary( Map< String, TimeWindowCalculations > ioaDiscrete,
                               Map< String, Double > ioaContinuous )
  {
    this.setSpacing( 5 );

    // Setup table title
    Label discreteLabel = new Label( "Discrete Summary" );
    discreteLabel.setFont( boldVerdana );
    this.getChildren().add( discreteLabel );

    // Headers 1
    Label keys = new Label( "Keys" );
    keys.setMinWidth( colWidth );
    keys.setAlignment( Pos.TOP_RIGHT );
    keys.setFont( boldVerdana );
    Label obsrvr1 = new Label( "Observer 1" );
    obsrvr1.setFont( boldVerdana );
    obsrvr1.setMinWidth( 75 );
    Label obsrvr2 = new Label( "Observer 2" );
    obsrvr2.setFont( boldVerdana );
    obsrvr2.setMinWidth( 75 );
    HBox header = new HBox( keys, vertSep(), obsrvr1, vertSep(), obsrvr2 );
    header.setSpacing( 5 );
    this.getChildren().add( header );

    // Discrete Key | Average Summaries
    for (Entry< String, TimeWindowCalculations > entry : ioaDiscrete.entrySet()) {
      String key = entry.getKey();
      TimeWindowCalculations calc = entry.getValue();

      Label left = new Label( "" + key );
      left.setMinWidth( colWidth );
      left.setAlignment( Pos.TOP_RIGHT );

      Separator sep1 = vertSep();
      Separator sep2 = vertSep();

      String percent1 = String.format( "%.2f", calc.result1 * 100 );
      String percent2 = String.format( "%.2f", calc.result2 * 100 );
      Label mid = new Label( "%" + percent1 );
      mid.setMinWidth( 75 );
      Label right = new Label( "%" + percent2 );
      right.setMinWidth( 75 );

      HBox row = new HBox( left, sep1, mid, sep2, right );
      row.setSpacing( 5 );

      this.getChildren().add( row );
    }

    // Table 2 title:
    Label continuousLbl = new Label( "Continuous Summary" );
    continuousLbl.setFont( boldVerdana );
    continuousLbl.setPadding( new Insets( 5, 0, 0, 0 ) );
    this.getChildren().add( continuousLbl );

    // Headers 2
    keys = new Label( "Keys" );
    keys.setMinWidth( colWidth );
    keys.setAlignment( Pos.TOP_RIGHT );
    keys.setFont( boldVerdana );
    Label avgLbl = new Label( "Average IOA" );
    avgLbl.setFont( boldVerdana );
    header = new HBox( keys, vertSep(), avgLbl );
    header.setSpacing( 5 );
    this.getChildren().add( header );

    // Continuous Key
    for (Entry< String, Double > entry : ioaContinuous.entrySet()) {
      String key = entry.getKey();
      Double avg = entry.getValue();

      Label left = new Label( "" + key );
      left.setMinWidth( colWidth );
      left.setAlignment( Pos.TOP_RIGHT );

      Separator sep = vertSep();

      String percent1 = String.format( "%.2f", avg * 100 );
      Label right = new Label( "%" + percent1 );

      HBox row = new HBox( left, sep, right );
      row.setSpacing( 5 );

      this.getChildren().add( row );
    }
  }

  private Separator vertSep()
  {
    return new Separator( Orientation.VERTICAL );
  }
}
