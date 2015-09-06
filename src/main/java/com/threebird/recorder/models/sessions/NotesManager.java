package com.threebird.recorder.models.sessions;

import java.io.File;

import javafx.beans.property.SimpleDoubleProperty;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.threebird.recorder.persistence.GsonUtils;
import com.threebird.recorder.utils.resources.ResourceUtils;

/**
 * This simply exists to keep the state of the Notes tab across sessions.
 */
public class NotesManager
{
  private static class GsonBean
  {
    double xPos;
    double yPos;
  }

  private static SimpleDoubleProperty xPosProperty;
  private static SimpleDoubleProperty yPosProperty;

  private static File file = ResourceUtils.getNotesDetails();
  private static Supplier< GsonBean > defaultModel = Suppliers.memoize( ( ) -> {
    GsonBean bean = new GsonBean();
    try {
      return GsonUtils.get( file, bean );
    } catch (Exception e) {
      // No err message...the user can still continue if this fails
      return bean;
    }
  } );

  private static void persist()
  {
    GsonBean model = new GsonBean();
    model.xPos = xPosProperty().get();
    model.yPos = yPosProperty().get();

    try {
      GsonUtils.save( file, model );
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static SimpleDoubleProperty xPosProperty()
  {
    if (xPosProperty == null) {
      xPosProperty = new SimpleDoubleProperty( defaultModel.get().xPos );
      xPosProperty.addListener( ( obs, old, newV ) -> persist() );
    }

    return xPosProperty;
  }

  public static SimpleDoubleProperty yPosProperty()
  {
    if (yPosProperty == null) {
      yPosProperty = new SimpleDoubleProperty( defaultModel.get().yPos );
      yPosProperty.addListener( ( obs, old, newV ) -> persist() );
    }

    return yPosProperty;
  }
}
