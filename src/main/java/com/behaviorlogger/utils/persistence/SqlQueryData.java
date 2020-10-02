package com.behaviorlogger.utils.persistence;

import java.sql.ResultSet;
import java.util.List;
import java.util.function.Consumer;

import com.google.common.collect.Lists;

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
   * 
   * @throws Exception
   */
  void handle( ResultSet rs ) throws Exception;

  /**
   * Equivalent to calling create(sql, Lists.newArrayList(), callback);
   */
  public static SqlQueryData create( final String sql )
  {
    return create( sql, Lists.newArrayList(), SqlCallback.NOOP );
  }

  /**
   * Equivalent to calling create(sql, Lists.newArrayList(), callback);
   */
  public static SqlQueryData create( final String sql, final SqlCallback callback )
  {
    return create( sql, Lists.newArrayList(), callback );
  }

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

      @Override public void handle( ResultSet rs ) throws Exception
      {
        callback.handle( rs );
      }
    };
  }
}
