package com.threebird.recorder.utils.persistence;

import java.sql.ResultSet;

/**
 * A {@link FunctionalInterface} that users can use to handle ResultSets
 */
@FunctionalInterface
public interface SqlCallback
{
  public SqlCallback NOOP = rs -> {};

  void handle( ResultSet input ) throws Exception;
}
