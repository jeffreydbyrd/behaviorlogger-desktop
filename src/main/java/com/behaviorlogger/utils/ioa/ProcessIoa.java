package com.behaviorlogger.utils.ioa;

import java.io.File;
import java.io.IOException;

import com.behaviorlogger.persistence.GsonUtils;
import com.behaviorlogger.persistence.recordings.RecordingRawJson1_0.SessionBean1_0;
import com.behaviorlogger.persistence.recordings.RecordingRawJson1_1.SessionBean1_1;
import com.behaviorlogger.utils.ioa.version1_0.ConvertTo1_1;
import com.behaviorlogger.utils.ioa.version1_1.IoaUtils1_1;
import com.google.common.base.Strings;

import javafx.scene.layout.VBox;

public class ProcessIoa
{

  public static class Versioned
  {
    String version; // bl version for 1.0
    String blVersion; // bl version for 1.1

    @Override public String toString()
    {
      return "Versioned [version=" + version + ", blVersion=" + blVersion + "]";
    }
  }

  /**
   * Calculates IOA and writes the output to 'out'
   * 
   * @param f1
   *          the first raw input file
   * @param f2
   *          the second raw input file
   * @param method
   *          the {@link IoaMethod} used
   * @param blockSize
   *          the blocksize of intervals used
   * @param out
   *          the output file
   * @return a JavaFX pane giving a summary of the output file
   * @throws IOException
   */
  public static VBox process( File f1,
                              File f2,
                              IoaMethod method,
                              int blockSize,
                              boolean appendToFile,
                              File out )
      throws Exception
  {
    Versioned v1 = GsonUtils.get( f1, new Versioned() );
    Versioned v2 = GsonUtils.get( f2, new Versioned() );
    v1.version = Strings.nullToEmpty( v1.version );
    v1.blVersion = Strings.nullToEmpty( v1.blVersion );
    v2.version = Strings.nullToEmpty( v2.version );
    v2.blVersion = Strings.nullToEmpty( v2.blVersion );

    SessionBean1_1 stream1;
    if (v1.version.equals( "1.0" )) {
      SessionBean1_0 temp = GsonUtils.get( f1, new SessionBean1_0() );
      stream1 = ConvertTo1_1.convert( temp );
    } else {
      stream1 = GsonUtils.get( f1, new SessionBean1_1() );
    }

    SessionBean1_1 stream2;
    if (v2.version.equals( "1.0" )) {
      SessionBean1_0 temp = GsonUtils.get( f2, new SessionBean1_0() );
      stream2 = ConvertTo1_1.convert( temp );
    } else {
      stream2 = GsonUtils.get( f2, new SessionBean1_1() );
    }

    if (method != IoaMethod.Time_Window) {
      return IoaUtils1_1.processTimeBlock( method, blockSize, appendToFile, out, stream1, stream2 );
    } else {
      return IoaUtils1_1.processTimeWindow( f1.getName(),
                                            f2.getName(),
                                            appendToFile,
                                            out,
                                            blockSize,
                                            stream1,
                                            stream2 );
    }
  }

}
