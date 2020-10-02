package com.behaviorlogger.models.sessions;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.behaviorlogger.models.MappableChar;
import com.behaviorlogger.models.behaviors.BehaviorEvent;
import com.behaviorlogger.models.behaviors.ContinuousBehavior;
import com.behaviorlogger.models.behaviors.DiscreteBehavior;
import com.behaviorlogger.models.preferences.PreferencesManager;
import com.behaviorlogger.models.schemas.KeyBehaviorMapping;
import com.behaviorlogger.models.schemas.SchemasManager;
import com.behaviorlogger.persistence.SessionDirectories;
import com.behaviorlogger.persistence.recordings.Recordings;
import com.google.common.collect.Lists;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.util.Duration;

public class RecordingManager
{
  public final Timeline timer;
  public final SimpleBooleanProperty saveSuccessfulProperty = new SimpleBooleanProperty();
  public final SimpleBooleanProperty playingProperty = new SimpleBooleanProperty( false );
  public final SimpleIntegerProperty counter = new SimpleIntegerProperty( 0 );
  public final SimpleStringProperty notes = new SimpleStringProperty();
  public final ObservableList< DiscreteBehavior > discrete = FXCollections.observableArrayList();
  public final ObservableList< ContinuousBehavior > continuous = FXCollections.observableArrayList();
  public final ObservableMap< MappableChar, KeyBehaviorMapping > unknowns = FXCollections.observableHashMap();

  public final ObservableMap< MappableChar, ContinuousBehavior > midContinuous =
      FXCollections.observableHashMap();
  public final ObservableMap< MappableChar, SimpleIntegerProperty > discreteCounts =
      FXCollections.observableHashMap();
  public final ObservableMap< MappableChar, ContinuousCounter > continuousCounts =
      FXCollections.observableHashMap();

  private final String streamUuid;
  private long startTime = 0;

  public RecordingManager()
  {
    streamUuid = UUID.randomUUID().toString();

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

    AtomicBoolean started = new AtomicBoolean( false );
    playingProperty.addListener( ( o, oldV, playing ) -> {
      if (playing && !started.get()) {
        this.startTime = System.currentTimeMillis();
        started.set( true );
      }
    } );

    notes.addListener( ( obs, old, newV ) -> {
      persist();
    } );

  }

  private void persist()
  {
    String fullFileName = getFullFileName();
    List< BehaviorEvent > behaviors = allBehaviors();
    String _notes = Optional.ofNullable( notes.get() ).orElse( "" );

    long stopTime = System.currentTimeMillis();

    CompletableFuture< Long > fCsv =
        Recordings.saveJson( new File( fullFileName + ".raw" ),
                             streamUuid,
                             behaviors,
                             count(),
                             _notes,
                             startTime,
                             stopTime );
    CompletableFuture< Long > fXls =
        Recordings.saveXls( new File( fullFileName + ".xls" ),
                            streamUuid,
                            behaviors,
                            count(),
                            _notes,
                            startTime,
                            stopTime );

    CompletableFuture.allOf( fCsv, fXls ).handleAsync( ( v, t ) -> {
      boolean saveSuccessful = t == null;
      Platform.runLater( () -> saveSuccessfulProperty.set( saveSuccessful ) );
      if (t != null) {
        t.printStackTrace();
      }
      return null;
    } );
  }

  private List< BehaviorEvent > allBehaviors()
  {
    ArrayList< BehaviorEvent > behaviors = Lists.newArrayList();
    behaviors.addAll( discrete );
    behaviors.addAll( continuous );
    Collections.sort( behaviors, BehaviorEvent.comparator );
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

    String uuid = SchemasManager.getSelected().uuid;
    String directory = SessionDirectories.getForSchemaIdOrDefault( uuid ).getPath();
    String filename = getFileName();
    return String.format( "%s%s%s", directory, File.separator, filename );
  }

  public void togglePlayingProperty()
  {
    playingProperty.set( !playingProperty.get() );
  }

  /**
   * @return the current count of the timer, in milliseconds
   */
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
