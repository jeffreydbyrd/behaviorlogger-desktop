package com.threebird.recorder.models.preferences;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import com.google.common.base.Preconditions;
import com.threebird.recorder.persistence.Preferences;

public class PreferencesManager
{
  private static SimpleStringProperty sessionDirectoryProperty;
  private static SimpleIntegerProperty durationProperty;
  private static SimpleBooleanProperty colorOnEndProperty;
  private static SimpleBooleanProperty pauseOnEndProperty;
  private static SimpleBooleanProperty soundOnEndProperty;

  public static synchronized SimpleStringProperty sessionDirectoryProperty()
  {
    if (sessionDirectoryProperty == null) {
      sessionDirectoryProperty = new SimpleStringProperty( Preferences.getSessionDirectory() );
      sessionDirectoryProperty.addListener( ( obsrvr, oldV, newV ) -> {
        Preferences.saveSessionDirectory( newV );
      } );
    }
    return sessionDirectoryProperty;
  }

  public static synchronized SimpleIntegerProperty durationProperty()
  {
    if (durationProperty == null) {
      durationProperty = new SimpleIntegerProperty( Preferences.getDuration() );
      durationProperty.addListener( ( obsrvr, oldV, newV ) -> {
        Preferences.saveDuration( newV.intValue() );
      } );
    }
    return durationProperty;
  }

  public static synchronized SimpleBooleanProperty colorOnEndProperty()
  {
    if (colorOnEndProperty == null) {
      colorOnEndProperty = new SimpleBooleanProperty( Preferences.getColorOnEnd() );
      colorOnEndProperty.addListener( ( obsvr, oldV, newV ) -> {
        Preferences.saveColorOnEnd( newV );
      } );
    }
    return colorOnEndProperty;
  }

  public static synchronized SimpleBooleanProperty pauseOnEndProperty()
  {
    if (pauseOnEndProperty == null) {
      pauseOnEndProperty = new SimpleBooleanProperty( Preferences.getPauseOnEnd() );
      pauseOnEndProperty.addListener( ( obsrvr, old, newV ) -> {
        Preferences.savePauseOnEnd( newV );
      } );
    }
    return pauseOnEndProperty;
  }

  public static synchronized SimpleBooleanProperty soundOnEndProperty()
  {
    if (soundOnEndProperty == null) {
      soundOnEndProperty = new SimpleBooleanProperty( Preferences.getSoundOnEnd() );
      pauseOnEndProperty.addListener( ( obsrvr, old, newV ) -> {
        Preferences.saveSoundOnEnd( newV );
      } );
    }
    return soundOnEndProperty;
  }

  public static void saveSessionDirectory( String dir )
  {
    Preconditions.checkNotNull( dir );
    sessionDirectoryProperty().set( dir );
  }

  public static String getSessionDirectory()
  {
    return sessionDirectoryProperty().get();
  }

  public static void saveDuration( Integer duration )
  {
    Preconditions.checkNotNull( duration );
    durationProperty().set( duration );
  }

  public static Integer getDuration()
  {
    return durationProperty().get();
  }

  public static void saveColorOnEnd( boolean colorOnEnd )
  {
    colorOnEndProperty().set( colorOnEnd );
  }

  public static boolean getColorOnEnd()
  {
    return colorOnEndProperty().get();
  }

  public static void savePauseOnEnd( boolean pauseOnEnd )
  {
    pauseOnEndProperty().set( pauseOnEnd );
  }

  public static boolean getPauseOnEnd()
  {
    return pauseOnEndProperty().get();
  }

  public static void saveSoundOnEnd( boolean soundOnEnd )
  {
    soundOnEndProperty().set( soundOnEnd );
  }

  public static boolean getSoundOnEnd()
  {
    return soundOnEndProperty().get();
  }
}
