package com.threebird.recorder.persistence;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import com.threebird.recorder.models.behaviors.Behavior;
import com.threebird.recorder.models.schemas.SchemasManager;
import com.threebird.recorder.models.sessions.SessionManager;

public class Recordings
{
  static class SaveDetails
  {
    public List< Behavior > behaviors;
    public File f;
    public String client;
    public String project;
    public String observer;
    public String therapist;
    public String condition;
    public Integer sessionNumber;
    public Integer totalTimeMillis;
  }

  public static enum Writer
  {
    CSV(Recordings::writeCsv),
    XLS(Recordings::writeXls);

    private final BlockingQueue< SaveDetails > q = new ArrayBlockingQueue< SaveDetails >( 1 );
    private final ExecutorService es = Executors.newSingleThreadExecutor();
    private final Thread taskManager;

    private Writer( Function< SaveDetails, Long > func )
    {
      this.taskManager = new Thread( ( ) -> {
        while (true) {
          try {
            SaveDetails sd = q.take();
            Future< Long > future = es.submit( ( ) -> func.apply( sd ) );
            future.get();
          } catch (Exception e) {
            throw new RuntimeException( e );
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
        throw new RuntimeException( e );
      }
    }

    public void shutdown()
    {
      es.shutdown();
    }
  }

  private static SaveDetails createSaveDetails( File f, List< Behavior > behaviors, int totalTime )
  {
    SaveDetails sd = new SaveDetails();

    sd.f = f;
    sd.behaviors = behaviors;
    sd.client = SchemasManager.getSelected().client;
    sd.project = SchemasManager.getSelected().project;
    sd.observer = SessionManager.getObserver();
    sd.therapist = SessionManager.getTherapist();
    sd.condition = SessionManager.getCondition();
    sd.sessionNumber = SessionManager.getSessionNumber();
    sd.totalTimeMillis = totalTime;
    return sd;
  }

  public static void saveCsv( File f, List< Behavior > behaviors, int count )
  {
    Writer.CSV.schedule( createSaveDetails( f, behaviors, count ) );
  }

  public static void saveXls( File f, List< Behavior > behaviors, int count )
  {
    Writer.XLS.schedule( createSaveDetails( f, behaviors, count ) );
  }

  private static Long writeCsv( SaveDetails details )
  {
    try {
      if (!details.f.exists()) {
        details.f.createNewFile();
      }

      String[] headers = { "Key", "Timestamp", "Description" };
      BufferedWriter out = Files.newWriter( details.f, Charsets.UTF_8 );
      CSVPrinter printer = CSVFormat.DEFAULT.withHeader( headers ).print( out );

      for (Behavior b : details.behaviors) {
        printer.printRecord( b.key.c + "", b.timeDisplay(), b.description );
      }

      printer.flush();
      printer.close();

      return details.f.length();
    } catch (IOException e) {
      throw new RuntimeException( e );
    }
  }

  private static Long writeXls( SaveDetails details )
  {
    try {
      WriteXls.write( details );
      return details.f.length();
    } catch (IOException e) {
      throw new RuntimeException( e );
    }
  }
}
