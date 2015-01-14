package com.threebird.recorder.models.preferences;

import java.io.File;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.threebird.recorder.persistence.GsonUtils;

public class PreferencesManager
{
  private static class GsonBean
  {
    String directory = System.getProperty( "user.home" );
    int duration = 600;
    boolean colorOnEnd = true;
    boolean pauseOnEnd = false;
    boolean soundOnEnd = false;
  }

  private static SimpleStringProperty sessionDirectoryProperty;
  private static SimpleIntegerProperty durationProperty;
  private static SimpleBooleanProperty colorOnEndProperty;
  private static SimpleBooleanProperty pauseOnEndProperty;
  private static SimpleBooleanProperty soundOnEndProperty;

  private static File file = new File( "./resources/prefs.json" );
  private static Supplier< GsonBean > model = Suppliers.memoize( ( ) -> GsonUtils.get( file, new GsonBean() ) );

  private static void persist()
  {
    GsonBean model = new GsonBean();
    model.directory = getSessionDirectory();
    model.duration = getDuration();
    model.colorOnEnd = getColorOnEnd();
    model.pauseOnEnd = getPauseOnEnd();
    model.soundOnEnd = getSoundOnEnd();
    GsonUtils.save( file, model );
  }

  public static synchronized SimpleStringProperty sessionDirectoryProperty()
  {
    if (sessionDirectoryProperty == null) {
      sessionDirectoryProperty = new SimpleStringProperty( model.get().directory );
      sessionDirectoryProperty.addListener( ( obsrvr, oldV, newV ) -> persist() );
    }
    return sessionDirectoryProperty;
  }

  public static synchronized SimpleIntegerProperty durationProperty()
  {
    if (durationProperty == null) {
      durationProperty = new SimpleIntegerProperty( model.get().duration );
      durationProperty.addListener( ( obsrvr, oldV, newV ) -> persist() );
    }
    return durationProperty;
  }

  public static synchronized SimpleBooleanProperty colorOnEndProperty()
  {
    if (colorOnEndProperty == null) {
      colorOnEndProperty = new SimpleBooleanProperty( model.get().colorOnEnd );
      colorOnEndProperty.addListener( ( obsrvr, oldV, newV ) -> persist() );
    }
    return colorOnEndProperty;
  }

  public static synchronized SimpleBooleanProperty pauseOnEndProperty()
  {
    if (pauseOnEndProperty == null) {
      pauseOnEndProperty = new SimpleBooleanProperty( model.get().pauseOnEnd );
      pauseOnEndProperty.addListener( ( obsrvr, oldV, newV ) -> persist() );
    }
    return pauseOnEndProperty;
  }

  public static synchronized SimpleBooleanProperty soundOnEndProperty()
  {
    if (soundOnEndProperty == null) {
      soundOnEndProperty = new SimpleBooleanProperty( model.get().soundOnEnd );
      pauseOnEndProperty.addListener( ( obsrvr, oldV, newV ) -> persist() );
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
