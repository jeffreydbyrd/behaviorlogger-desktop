package com.threebird.recorder.models.ioa;

import java.io.File;
import java.util.Optional;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.threebird.recorder.persistence.GsonUtils;
import com.threebird.recorder.utils.ioa.IoaMethod;
import com.threebird.recorder.utils.resources.ResourceUtils;

public class IoaManager
{

  private static class GsonBean
  {
    String file1;
    String file2;
    String method = IoaMethod.Partial_Agreement.name();
    int threshold = 1;
    boolean appendSelected;
    String appendFile;
  }

  private static SimpleStringProperty file1Property;
  private static SimpleStringProperty file2Property;
  private static SimpleStringProperty methodProperty;
  private static SimpleIntegerProperty thresholdProperty;
  private static SimpleBooleanProperty appendSelectedProperty;
  private static SimpleStringProperty appendFileProperty;

  private static File file = ResourceUtils.getIoaDetails();

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
    model.file1 = file1Property().get();
    model.file2 = file2Property().get();
    model.method = methodProperty().get();
    model.threshold = thresholdProperty().get();
    model.appendSelected = appendSelectedProperty().get();
    model.appendFile = appendFileProperty().get();

    try {
      GsonUtils.save( file, model );
    } catch (Exception e) {
      // XXX: No err message...the user can still continue if this fails
      e.printStackTrace();
    }
  }

  public static SimpleStringProperty file1Property()
  {
    if (file1Property == null) {
      file1Property = new SimpleStringProperty( defaultModel.get().file1 );
      file1Property.addListener( ( o, old, newV ) -> persist() );
    }
    return file1Property;
  }

  public static SimpleStringProperty file2Property()
  {
    if (file2Property == null) {
      file2Property = new SimpleStringProperty( defaultModel.get().file2 );
      file2Property.addListener( ( o, old, newV ) -> persist() );
    }
    return file2Property;
  }

  public static SimpleStringProperty methodProperty()
  {
    if (methodProperty == null) {
      methodProperty = new SimpleStringProperty( defaultModel.get().method );
      methodProperty.addListener( ( o, old, newV ) -> persist() );
    }
    return methodProperty;
  }

  public static IoaMethod getSelectedMethod()
  {
    return IoaMethod.valueOf( methodProperty().get() );
  }

  public static SimpleIntegerProperty thresholdProperty()
  {
    if (thresholdProperty == null) {
      thresholdProperty = new SimpleIntegerProperty( defaultModel.get().threshold );
      thresholdProperty.addListener( ( o, old, newV ) -> persist() );
    }
    return thresholdProperty;
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
