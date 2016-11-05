package com.threebird.recorder.persistence;

import java.io.File;
import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.threebird.recorder.models.preferences.PreferencesManager;
import com.threebird.recorder.utils.persistence.SqlCallback;
import com.threebird.recorder.utils.persistence.SqliteDao;

public class SessionDirectories
{
  private static final String TBL_NAME = "session_dirs_v1_1";

  public static File getForSchemaIdOrDefault( String uuid )
  {
    File f = new File( PreferencesManager.getSessionDirectory() );

    if (Strings.isNullOrEmpty( uuid )) {
      return f;
    }

    return getForSchemaId( uuid ).orElse( f );
  }

  public static Optional< File > getForSchemaId( String uuid )
  {
    Preconditions.checkState( !Strings.isNullOrEmpty( uuid ) );

    String sql = "SELECT session_directory FROM " + TBL_NAME + " WHERE schema_uuid = ?";

    StringBuilder sb = new StringBuilder( "" );

    SqlCallback handle = ( ResultSet rs ) -> {
      int counter = 0;
      while (rs.next()) {
        Preconditions.checkState( counter < 2 ); // this should never go higher than 1 iteration
        sb.append( rs.getString( "session_directory" ) );
        counter++;
      }
    };

    try {
      SqliteDao.query( sql, Lists.newArrayList( uuid ), handle );
    } catch (Exception e) {
      e.printStackTrace();
      return Optional.empty();
    }

    File f = new File( sb.toString() );

    if (!f.exists()) {
      Optional.empty();
    }

    return Optional.of( f );
  }

  public static void update( String schemaId, File sessionDir ) throws Exception
  {
    Preconditions.checkState( sessionDir.exists() );
    String sqlFmt = "UPDATE " + TBL_NAME + " SET session_directory = ? WHERE schema_uuid = ?";
    List< Object > params = Lists.newArrayList( sessionDir.getPath(), schemaId );
    SqliteDao.update( sqlFmt, params, SqlCallback.NOOP );
  }

  public static void create( String schemaId, File sessionDir ) throws Exception
  {
    Preconditions.checkState( sessionDir.exists() );

    String sqlFmt = "INSERT INTO " + TBL_NAME + " (schema_uuid, session_directory) VALUES (?,?)";
    List< Object > params = Lists.newArrayList( schemaId, sessionDir.getPath() );

    SqliteDao.update( sqlFmt, params, SqlCallback.NOOP );
  }
}
