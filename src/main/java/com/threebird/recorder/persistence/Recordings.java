package com.threebird.recorder.persistence;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
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

public class Recordings
{
  private static class SaveDetails
  {
    public List< Behavior > behaviors;
    public File f;

    public SaveDetails( List< Behavior > behaviors, File f )
    {
      this.behaviors = behaviors;
      this.f = f;
    }
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
          } catch (InterruptedException e) {
            e.printStackTrace();
          } catch (ExecutionException e) {
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
        throw new RuntimeException( e );
      }
    }

    public void shutdown()
    {
      es.shutdown();
    }
  }

  public static void saveCsv( File f, List< Behavior > behaviors )
  {
    Writer.CSV.schedule( new SaveDetails( behaviors, f ) );
  }

  public static void saveXls( File f, List< Behavior > behaviors )
  {
    Writer.XLS.schedule( new SaveDetails( behaviors, f ) );
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
    List< Behavior > behaviors = details.behaviors;
    try {
      Thread.sleep( 1000 );
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    System.out.println( "Writing XLS file for " + behaviors.size() + " behaviors" );
    return 0L;
  }
}
