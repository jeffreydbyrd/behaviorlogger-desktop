package com.threebird.recorder.models.preferences;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.threebird.recorder.persistence.GsonUtils;
import com.threebird.recorder.utils.Alerts;
import com.threebird.recorder.utils.resources.ResourceUtils;

public class PreferencesManager
{
  private static class GsonFilenameComp
  {
    String name;
    boolean enabled;

    public GsonFilenameComp( String name, boolean enabled )
    {
      this.name = name;
      this.enabled = enabled;
    }
  }

  private static class GsonBean
  {
    String directory = System.getProperty( "user.home" );
    int duration = 600;
    boolean colorOnEnd = true;
    boolean pauseOnEnd = false;
    boolean soundOnEnd = false;
    boolean checkVersion = true;
    String lastVersionCheck = "";

    List< GsonFilenameComp > filenameComponents =
        Lists.newArrayList( FilenameComponent.values() )
             .stream()
             .map( c -> new GsonFilenameComp( c.name(), c.enabled ) )
             .collect( Collectors.toList() );
  }

  private static SimpleStringProperty sessionDirectoryProperty;
  private static ObservableList< FilenameComponent > filenameComponents;
  private static SimpleIntegerProperty durationProperty;
  private static SimpleBooleanProperty colorOnEndProperty;
  private static SimpleBooleanProperty pauseOnEndProperty;
  private static SimpleBooleanProperty soundOnEndProperty;
  private static SimpleBooleanProperty checkVersionProperty;
  private static SimpleStringProperty lastVersionCheckProperty;

  private static File file = ResourceUtils.getPrefs();
  private static Supplier< GsonBean > defaultModel = Suppliers.memoize( ( ) -> {
    GsonBean bean = new GsonBean();
    try {
      return GsonUtils.get( file, bean );
    } catch (Exception e) {
      Alerts.warning( "Error Loading Preferences",
                      "There was a problem while loading your preferences. The application will use default values.",
                      e.getMessage() );
      return bean;
    }
  } );

  private static void persist()
  {
    GsonBean model = new GsonBean();
    model.directory = getSessionDirectory();
    model.duration = getDuration();
    model.colorOnEnd = getColorOnEnd();
    model.pauseOnEnd = getPauseOnEnd();
    model.soundOnEnd = getSoundOnEnd();
    model.checkVersion = getCheckVersion();
    model.lastVersionCheck = lastVersionCheckProperty.get();
    model.filenameComponents =
        filenameComponents().stream()
                            .map( c -> new GsonFilenameComp( c.name(), c.enabled ) )
                            .collect( Collectors.toList() );

    try {
      GsonUtils.save( file, model );
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static synchronized SimpleStringProperty sessionDirectoryProperty()
  {
    if (sessionDirectoryProperty == null) {
      sessionDirectoryProperty = new SimpleStringProperty( defaultModel.get().directory );
      sessionDirectoryProperty.addListener( ( obsrvr, oldV, newV ) -> persist() );
    }
    return sessionDirectoryProperty;
  }

  public static synchronized SimpleIntegerProperty durationProperty()
  {
    if (durationProperty == null) {
      durationProperty = new SimpleIntegerProperty( defaultModel.get().duration );
      durationProperty.addListener( ( obsrvr, oldV, newV ) -> persist() );
    }
    return durationProperty;
  }

  public static synchronized SimpleBooleanProperty colorOnEndProperty()
  {
    if (colorOnEndProperty == null) {
      colorOnEndProperty = new SimpleBooleanProperty( defaultModel.get().colorOnEnd );
      colorOnEndProperty.addListener( ( obsrvr, oldV, newV ) -> persist() );
    }
    return colorOnEndProperty;
  }

  public static synchronized SimpleBooleanProperty pauseOnEndProperty()
  {
    if (pauseOnEndProperty == null) {
      pauseOnEndProperty = new SimpleBooleanProperty( defaultModel.get().pauseOnEnd );
      pauseOnEndProperty.addListener( ( obsrvr, oldV, newV ) -> persist() );
    }
    return pauseOnEndProperty;
  }

  public static synchronized SimpleBooleanProperty soundOnEndProperty()
  {
    if (soundOnEndProperty == null) {
      soundOnEndProperty = new SimpleBooleanProperty( defaultModel.get().soundOnEnd );
      pauseOnEndProperty.addListener( ( obsrvr, oldV, newV ) -> persist() );
    }
    return soundOnEndProperty;
  }

  public static synchronized SimpleBooleanProperty checkVersionProperty()
  {
    if (checkVersionProperty == null) {
      checkVersionProperty = new SimpleBooleanProperty( defaultModel.get().checkVersion );
      checkVersionProperty.addListener( ( o, old, newV ) -> persist() );
    }
    return checkVersionProperty;
  }

  public static synchronized SimpleStringProperty lastVersionCheckProperty()
  {
    if (lastVersionCheckProperty == null) {
      lastVersionCheckProperty = new SimpleStringProperty( defaultModel.get().lastVersionCheck );
      lastVersionCheckProperty.addListener( ( o, old, newV ) -> persist() );
    }
    return lastVersionCheckProperty;
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

  public static void setCheckVersion( boolean checkVersion )
  {
    checkVersionProperty().set( checkVersion );
  }

  public static boolean getCheckVersion()
  {
    return checkVersionProperty().get();
  }

  public static ObservableList< FilenameComponent > filenameComponents()
  {
    if (filenameComponents == null) {
      filenameComponents = FXCollections.observableArrayList();
      List< GsonFilenameComp > beans = defaultModel.get().filenameComponents;
      for (GsonFilenameComp bean : beans) {
        FilenameComponent comp = FilenameComponent.valueOf( bean.name );
        comp.enabled = bean.enabled;
        comp.order = beans.indexOf( bean ) + 1;
        filenameComponents.add( comp );
      }

      HashSet< FilenameComponent > enums = Sets.newHashSet( FilenameComponent.values() );
      SetView< FilenameComponent > missed = Sets.difference( enums, Sets.newHashSet( filenameComponents ) );
      for (FilenameComponent comp : missed) {
        comp.order = filenameComponents.size() + 1;
        filenameComponents.add( comp );
      }
    }
    return filenameComponents;
  }

  public static void saveFilenameComponents( List< FilenameComponent > components )
  {
    filenameComponents.clear();
    filenameComponents.addAll( components );
    persist();
  }

}
