package com.threebird.recorder.models.sessions;

import java.io.File;

import javafx.beans.property.SimpleBooleanProperty;

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
    boolean notesOpen;
  }

  private static SimpleBooleanProperty notesOpenProperty;

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
    model.notesOpen = isNotesOpen();
    try {
      GsonUtils.save( file, model );
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static SimpleBooleanProperty notesOpenProperty()
  {
    if (notesOpenProperty == null) {
      notesOpenProperty = new SimpleBooleanProperty( defaultModel.get().notesOpen );
      notesOpenProperty.addListener( ( obs, old, newV ) -> persist() );
    }

    return notesOpenProperty;
  }

  public static boolean isNotesOpen()
  {
    return notesOpenProperty().get();
  }

}
