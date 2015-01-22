package com.threebird.recorder.models.sessions;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

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
import com.threebird.recorder.models.preferences.PreferencesManager;
import com.threebird.recorder.models.schemas.KeyBehaviorMapping;
import com.threebird.recorder.models.schemas.SchemasManager;

public class RecordingManager
{
  public final Timeline timer;
  public final SimpleBooleanProperty playingProperty = new SimpleBooleanProperty( false );
  public final SimpleIntegerProperty counter = new SimpleIntegerProperty( 0 );
  public final ObservableList< DiscreteBehavior > discrete = FXCollections.observableArrayList();
  public final ObservableList< ContinuousBehavior > continuous = FXCollections.observableArrayList();
  public final ObservableMap< MappableChar, KeyBehaviorMapping > unknowns = FXCollections.observableHashMap();

  private final File file;

  public RecordingManager()
  {
    timer = new Timeline();
    timer.setCycleCount( Animation.INDEFINITE );
    System.out.println( Thread.currentThread().getName() );
    KeyFrame kf = new KeyFrame( Duration.seconds( 1 ), evt -> {
      counter.set( counter.get() + 1 );
    } );
    timer.getKeyFrames().add( kf );

    String directory = SchemasManager.getSelected().sessionDirectory.getPath();
    String path = String.format( "%s/%s", directory, getFilename() );
    this.file = new File( path );
  }

  public static String getFilename()
  {
    List< String > components =
        PreferencesManager.getFilenameComponents()
                          .stream()
                          .filter( comp -> comp.enabled )
                          .map( comp -> comp.getComponent() )
                          .collect( Collectors.toList() );

    String join = String.join( "-", components );
    return String.format( "%s.xls", join );
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
