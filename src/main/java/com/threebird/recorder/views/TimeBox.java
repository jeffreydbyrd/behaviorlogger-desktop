package com.threebird.recorder.views;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

public class TimeBox extends HBox
{
  private int totalSeconds;

  private int hours;
  private int minutes;
  private int seconds;

  private Label hrsText;
  private Label minText;
  private Label secText;

  public TimeBox( int totalSeconds )
  {
    super();
    this.setSpacing( 3 );
    redraw( totalSeconds );
  }

  private void redraw( int totalSeconds )
  {
    this.totalSeconds = totalSeconds;

    int remaining = totalSeconds % (60 * 60);
    hours = totalSeconds / (60 * 60);
    minutes = remaining / 60;
    seconds = remaining % 60;

    hrsText = new Label( hours > 9 ? hours + "" : "0" + hours );
    minText = new Label( minutes > 9 ? minutes + "" : "0" + minutes );
    secText = new Label( seconds > 9 ? seconds + "" : "0" + seconds );

    Text separator1 = new Text( ":" );
    Text separator2 = new Text( ":" );

    this.getChildren().clear();
    this.getChildren().addAll( hrsText, separator1, minText, separator2, secText );
  }

  public int hours()
  {
    return hours;
  }

  public int minutes()
  {
    return minutes;
  }

  public int seconds()
  {
    return seconds;
  }

  public void setTime( int totalSeconds )
  {
    redraw( totalSeconds );
  }

  public void plusSeconds( int seconds )
  {
    redraw( totalSeconds + seconds );
  }

  public void minusSeconds( int seconds )
  {
    redraw( totalSeconds - seconds );
  }
}
