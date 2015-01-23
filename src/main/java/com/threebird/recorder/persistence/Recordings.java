package com.threebird.recorder.persistence;

import java.io.File;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.google.common.base.Function;
import com.threebird.recorder.models.behaviors.Behavior;

public class Recordings
{
  private static class SaveDetails
  {
    public List< Behavior > behaviors;
    public File f;
  }

  private static BlockingQueue< SaveDetails > csvQueue = new ArrayBlockingQueue< SaveDetails >( 1 );
  public static ExecutorService csvExecutor = Executors.newSingleThreadExecutor();

  private static BlockingQueue< SaveDetails > xlsQueue = new ArrayBlockingQueue< SaveDetails >( 1 );
  public static ExecutorService xlsExecutor = Executors.newSingleThreadExecutor();

  private static Thread csvWriter = new Thread( createWriter( csvQueue, csvExecutor, Recordings::writeCsv ) );
  private static Thread xlsWriter = new Thread( createWriter( xlsQueue, xlsExecutor, Recordings::writeXls ) );

  private static Runnable createWriter( BlockingQueue< SaveDetails > bq,
                                        ExecutorService es,
                                        Function< SaveDetails, Long > f )
  {
    return ( ) -> {
      while (true) {
        try {
          SaveDetails sd = bq.take();
          Future< Long > future = es.submit( ( ) -> f.apply( sd ) );
          future.get();
        } catch (InterruptedException e) {
          e.printStackTrace();
        } catch (ExecutionException e) {
          e.printStackTrace();
        }
      }
    };
  }

  private static Long writeCsv( SaveDetails details )
  {
    File file = details.f;
    List< Behavior > behaviors = details.behaviors;
    System.out.println( "Writing CSV file for " + behaviors.size() + " behaviors" );
    return 0L;
  }

  private static Long writeXls( SaveDetails details )
  {
    File file = details.f;
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

  public static void saveCsv( File f, List< Behavior > behaviors )
  {
    if (!csvWriter.isAlive()) {
      csvWriter.setDaemon( true );
      csvWriter.start();
    }

    csvQueue.clear();

    try {
      SaveDetails details = new SaveDetails();
      details.behaviors = behaviors;
      details.f = f;
      csvQueue.put( details );
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public static void saveXls( File f, List< Behavior > behaviors )
  {
    if (!xlsWriter.isAlive()) {
      xlsWriter.setDaemon( true );
      xlsWriter.start();
    }

    xlsQueue.clear();

    try {
      SaveDetails details = new SaveDetails();
      details.behaviors = behaviors;
      details.f = f;
      xlsQueue.put( details );
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
