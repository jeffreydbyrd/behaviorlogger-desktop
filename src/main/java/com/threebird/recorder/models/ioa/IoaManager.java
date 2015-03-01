package com.threebird.recorder.models.ioa;

import java.io.File;

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
    String method = IoaMethod.Partial_Agreement.display;
    int threshold = 1;
  }

  private static SimpleStringProperty file1Property;
  private static SimpleStringProperty file2Property;
  private static SimpleStringProperty methodProperty;
  private static SimpleIntegerProperty thresholdProperty;

  private static File file = ResourceUtils.getIoaDetails();

  private static Supplier< GsonBean > defaultModel = Suppliers.memoize( ( ) -> GsonUtils.get( file, new GsonBean() ) );

  private static void persist()
  {
    GsonBean model = new GsonBean();
    model.file1 = file1Property().get();
    model.file2 = file2Property().get();
    model.method = methodProperty().get();
    model.threshold = thresholdProperty().get();
    GsonUtils.save( file, model );
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

  public static SimpleIntegerProperty thresholdProperty()
  {
    if (thresholdProperty == null) {
      thresholdProperty = new SimpleIntegerProperty( defaultModel.get().threshold );
      thresholdProperty.addListener( ( o, old, newV ) -> persist() );
    }
    return thresholdProperty;
  }
}
