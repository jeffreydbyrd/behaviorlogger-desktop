package com.threebird.recorder.persistence;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import org.joda.time.DateTime;

import com.google.common.base.Preconditions;
import com.threebird.recorder.models.behaviors.Behavior;
import com.threebird.recorder.models.schemas.Schema;
import com.threebird.recorder.models.schemas.SchemasManager;
import com.threebird.recorder.models.sessions.SessionManager;

public class Recordings
{
  static class SaveDetails
  {
    public File f;
    public CompletableFuture< Long > fResult;
    public List< Behavior > behaviors;
    public Schema schema;
    public String observer;
    public String therapist;
    public String condition;
    public String location;
    public Integer sessionNumber;
    public Integer totalTimeMillis;
    public String notes;
    public String uuid;
    public DateTime startTime;
    public DateTime stopTime;
  }

  public static enum Writer
  {
    JSON(Recordings::writeJson),
    XLS(Recordings::writeXls);

    private final BlockingQueue< SaveDetails > q = new ArrayBlockingQueue< SaveDetails >( 1 );
    private final ExecutorService es = Executors.newSingleThreadExecutor();
    private final Thread taskManager;

    private Writer( Consumer< SaveDetails > c )
    {
      this.taskManager = new Thread( ( ) -> {
        while (true) {
          try {
            SaveDetails sd = q.take();
            es.submit( ( ) -> c.accept( sd ) ).get();
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      } );

      this.taskManager.setDaemon( true );
    }

    public void schedule( SaveDetails details )
    {
      Preconditions.checkState( !es.isShutdown() );

      if (!this.taskManager.isAlive()) {
        this.taskManager.start();
      }

      q.clear();

      try {
        q.put( details );
      } catch (InterruptedException e) {
        e.printStackTrace();
        details.fResult.completeExceptionally( e );
      }
    }

    public void shutdown()
    {
      es.shutdown();
    }
  }

  private static SaveDetails createSaveDetails( File f,
                                                String uuid,
                                                List< Behavior > behaviors,
                                                int totalTime,
                                                String notes,
                                                DateTime startTime,
                                                DateTime stopTime )
  {
    SaveDetails sd = new SaveDetails();

    sd.f = f;
    sd.fResult = new CompletableFuture< Long >();
    sd.uuid = uuid;
    sd.behaviors = behaviors;
    sd.schema = SchemasManager.getSelected();
    sd.observer = SessionManager.getObserver();
    sd.therapist = SessionManager.getTherapist();
    sd.condition = SessionManager.getCondition();
    sd.location = SessionManager.getLocation();
    sd.sessionNumber = SessionManager.getSessionNumber();
    sd.totalTimeMillis = totalTime;
    sd.notes = notes;
    sd.startTime = startTime;
    sd.stopTime = stopTime;

    return sd;
  }

  public static CompletableFuture< Long > saveJson( File f,
                                                    String uuid,
                                                    List< Behavior > behaviors,
                                                    int count,
                                                    String notes,
                                                    DateTime startTime,
                                                    DateTime stopTime )
  {
    SaveDetails saveDetails = createSaveDetails( f, uuid, behaviors, count, notes, startTime, stopTime );
    Writer.JSON.schedule( saveDetails );
    return saveDetails.fResult;
  }

  public static CompletableFuture< Long > saveXls( File f,
                                                   String uuid,
                                                   List< Behavior > behaviors,
                                                   int count,
                                                   String notes,
                                                   DateTime startTime,
                                                   DateTime stopTime )
  {
    SaveDetails saveDetails = createSaveDetails( f, uuid, behaviors, count, notes, startTime, stopTime );
    Writer.XLS.schedule( saveDetails );
    return saveDetails.fResult;
  }

  private static void writeJson( SaveDetails details )
  {
    try {
      RecordingRawJson.write( details );
      details.fResult.complete( details.f.length() );
    } catch (Exception e) {
      details.fResult.completeExceptionally( e );
    }
  }

  private static void writeXls( SaveDetails details )
  {
    try {
      WriteRecordingXls.write( details );
      details.fResult.complete( details.f.length() );
    } catch (IOException e) {
      details.fResult.completeExceptionally( e );
    }
  }
}
