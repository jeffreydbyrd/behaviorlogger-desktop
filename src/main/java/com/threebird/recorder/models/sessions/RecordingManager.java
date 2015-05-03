package com.threebird.recorder.models.sessions;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.util.Duration;

import com.google.common.collect.Lists;
import com.threebird.recorder.models.MappableChar;
import com.threebird.recorder.models.behaviors.Behavior;
import com.threebird.recorder.models.behaviors.ContinuousBehavior;
import com.threebird.recorder.models.behaviors.DiscreteBehavior;
import com.threebird.recorder.models.preferences.PreferencesManager;
import com.threebird.recorder.models.schemas.KeyBehaviorMapping;
import com.threebird.recorder.models.schemas.SchemasManager;
import com.threebird.recorder.persistence.Recordings;

public class RecordingManager
{
  public final Timeline timer;
  public final SimpleBooleanProperty saveSuccessfulProperty = new SimpleBooleanProperty( true );
  public final SimpleBooleanProperty playingProperty = new SimpleBooleanProperty( false );
  public final SimpleIntegerProperty counter = new SimpleIntegerProperty( 0 );
  public final ObservableList< DiscreteBehavior > discrete = FXCollections.observableArrayList();
  public final ObservableList< ContinuousBehavior > continuous = FXCollections.observableArrayList();
  public final ObservableMap< MappableChar, KeyBehaviorMapping > unknowns = FXCollections.observableHashMap();

  public RecordingManager()
  {
    timer = new Timeline();
    timer.setCycleCount( Animation.INDEFINITE );
    KeyFrame kf = new KeyFrame( Duration.millis( 1 ), evt -> {
      counter.set( counter.get() + 1 );
    } );
    timer.getKeyFrames().add( kf );

    discrete.addListener( (ListChangeListener< DiscreteBehavior >) c -> persist() );
    continuous.addListener( (ListChangeListener< ContinuousBehavior >) c -> persist() );
    playingProperty.addListener( ( o, oldV, playing ) -> {
      if (!playing) {
        persist();
      }
    } );
  }

  private void persist()
  {
    String fullFileName = getFullFileName();
    List< Behavior > behaviors = allBehaviors();

    try {
      Recordings.saveCsv( new File( fullFileName + ".csv" ), behaviors, count() ).get();
      Recordings.saveXls( new File( fullFileName + ".xls" ), behaviors, count() ).get();
      saveSuccessfulProperty.set( true );
    } catch (Exception e) {
      saveSuccessfulProperty.set( false );
    }
  }

  private List< Behavior > allBehaviors()
  {
    ArrayList< Behavior > behaviors = Lists.newArrayList();
    behaviors.addAll( discrete );
    behaviors.addAll( continuous );
    Collections.sort( behaviors, Behavior.comparator );
    return behaviors;
  }

  public static String getFileName()
  {
    if (SchemasManager.getSelected() == null) {
      return null;
    }

    return PreferencesManager.filenameComponents().stream()
                             .filter( comp -> comp.enabled )
                             .map( comp -> comp.getComponent() )
                             .collect( Collectors.joining( "-" ) );
  }

  public static String getFullFileName()
  {
    if (SchemasManager.getSelected() == null) {
      return null;
    }
    String directory = SchemasManager.getSelected().sessionDirectory.getPath();
    String filename = getFileName();
    return String.format( "%s/%s", directory, filename );
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
