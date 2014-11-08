package com.threebird.recorder.persistence.util;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A {@link FunctionalInterface} that users can use to handle ResultSets
 */
@FunctionalInterface
public interface SqlCallback
{
  void handle( ResultSet input ) throws SQLException;
}
