package com.threebird.recorder.models;

import java.io.File;
import java.util.Optional;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.threebird.recorder.persistence.GsonUtils;
import com.threebird.recorder.utils.resources.ResourceUtils;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class BinManager
{

  private static class GsonBean
  {
    String file;
    int binsize = 1;
    boolean appendSelected;
    String appendFile;
  }

  private static SimpleStringProperty fileProperty;
  private static SimpleIntegerProperty binsizeProperty;
  private static SimpleBooleanProperty appendSelectedProperty;
  private static SimpleStringProperty appendFileProperty;

  private static File file = ResourceUtils.getBinDetails();

  private static Supplier< GsonBean > defaultModel = Suppliers.memoize( ( ) -> {
    GsonBean bean = new GsonBean();
    try {
      return GsonUtils.get( file, bean );
    } catch (Exception e) {
      return bean;
    }
  } );

  private static void persist()
  {
    GsonBean model = new GsonBean();
    model.file = fileProperty().get();
    model.binsize = binsizeProperty().get();
    model.appendSelected = appendSelectedProperty().get();
    model.appendFile = appendFileProperty().get();

    try {
      GsonUtils.save( file, model );
    } catch (Exception e) {
      // XXX: No err message...the user can still continue if this fails
      e.printStackTrace();
    }
  }

  public static SimpleStringProperty fileProperty()
  {
    if (fileProperty == null) {
      fileProperty = new SimpleStringProperty( defaultModel.get().file );
      fileProperty.addListener( ( o, old, newV ) -> persist() );
    }
    return fileProperty;
  }

  public static SimpleIntegerProperty binsizeProperty()
  {
    if (binsizeProperty == null) {
      binsizeProperty = new SimpleIntegerProperty( defaultModel.get().binsize );
      binsizeProperty.addListener( ( o, old, newV ) -> persist() );
    }
    return binsizeProperty;
  }

  public static SimpleBooleanProperty appendSelectedProperty()
  {
    if (appendSelectedProperty == null) {
      appendSelectedProperty = new SimpleBooleanProperty( defaultModel.get().appendSelected );
      appendSelectedProperty.addListener( ( o, old, newV ) -> persist() );
    }
    return appendSelectedProperty;
  }
  
  public static SimpleStringProperty appendFileProperty()
  {
    if (appendFileProperty == null) {
      appendFileProperty = new SimpleStringProperty( defaultModel.get().appendFile );
      appendFileProperty.addListener( ( o, old, newV ) -> persist() );
    }
    return appendFileProperty;
  }

  public static Optional< String > getAppendFile()
  {
    return Optional.ofNullable( appendFileProperty().getValue() );
  }
}
