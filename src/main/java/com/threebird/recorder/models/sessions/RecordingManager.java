package com.threebird.recorder.models.sessions;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.util.Duration;

import com.threebird.recorder.models.MappableChar;
import com.threebird.recorder.models.behaviors.ContinuousBehavior;
import com.threebird.recorder.models.behaviors.DiscreteBehavior;
import com.threebird.recorder.models.schemas.KeyBehaviorMapping;

public class RecordingManager
{
  public final SimpleBooleanProperty playingProperty = new SimpleBooleanProperty( false );
  public final Timeline timer;
  public final SimpleIntegerProperty counter = new SimpleIntegerProperty( 0 );
  public final ObservableList< DiscreteBehavior > discrete = FXCollections.observableArrayList();
  public final ObservableList< ContinuousBehavior > continuous = FXCollections.observableArrayList();
  public final ObservableMap< MappableChar, KeyBehaviorMapping > unknowns = FXCollections.observableHashMap();

  public RecordingManager()
  {
    timer = new Timeline();
    timer.setCycleCount( Animation.INDEFINITE );
    System.out.println( Thread.currentThread().getName() );
    KeyFrame kf = new KeyFrame( Duration.seconds( 1 ), evt -> {
      counter.set( counter.get() + 1 );
    } );
    timer.getKeyFrames().add( kf );
  }

  public void togglePlayingProperty()
  {
    playingProperty.set( !playingProperty.get() );
  }

  public int count()
  {
    return counter.get();
  }

  public void log( DiscreteBehavior db )
  {
    discrete.add( db );
  }

  public void log( ContinuousBehavior cb )
  {
    continuous.add( cb );
  }
}
