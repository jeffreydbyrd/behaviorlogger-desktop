package com.threebird.recorder.utils.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

/**
 * Defines the information needed to create a PreparedStatement and how to
 * handle its ResultSet. Use the
 * {@link SqlQueryData#create(String, List, Consumer)} factory method and pass
 * it to {@link SqliteDao#query(SqlQueryData)} or
 * {@link SqliteDao#update(SqlQueryData)}
 */
public interface SqlQueryData
{
  /**
   * @return the SQL string
   */
  String getSql();

  /**
   * @return The PreparedStatement arguments
   */
  List< Object > getSqlParams();

  /**
   * Handles the ResultSet created after executing the query
   */
  void handle( ResultSet rs );

  /**
   * @return a new instance of {@link SqlQueryData} where getSql() returns
   *         'sql', getSqlParams() returns 'params', and handle() simply calls
   *         handle.accept()
   */
  public static SqlQueryData create( final String sql,
                                     final List< Object > params,
                                     final SqlCallback callback )
  {
    return new SqlQueryData() {
      @Override public List< Object > getSqlParams()
      {
        return params;
      }

      @Override public String getSql()
      {
        return sql;
      }

      @Override public void handle( ResultSet rs )
      {
        try {
          callback.handle( rs );
        } catch (SQLException e) {
          throw new RuntimeException( e );
        }
      }
    };
  }
}
