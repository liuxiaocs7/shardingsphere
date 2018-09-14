/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.executor;

import io.shardingsphere.core.constant.SQLType;
import io.shardingsphere.core.constant.transaction.TransactionType;
import io.shardingsphere.core.executor.sql.SQLExecuteCallback;
import io.shardingsphere.core.executor.sql.StatementExecuteUnit;
import io.shardingsphere.transaction.TransactionTypeHolder;
import io.shardingsphere.transaction.manager.base.executor.SagaSQLExeucteCallback;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * Sql Execute callback factory.
 *
 * @author yangyi
 */
public final class SQLExecuteCallbackFactory {
    
    /**
     * Get update SQLExecuteCallBack for PreparedStatementExecutor.
     *
     * @param sqlType           types of sql
     * @param isExceptionThrown is exception thrown
     * @param dataMap           data map
     * @return update SQLExecuteCallBack
     */
    public static SQLExecuteCallback<Integer> getPreparedUpdateSQLExecuteCallback(final SQLType sqlType, final boolean isExceptionThrown, final Map<String, Object> dataMap) {
        if (isSagaTransaction(sqlType)) {
            return getSagaUpdateSQLExecuteCallback(sqlType, isExceptionThrown, dataMap);
        }
        return new SQLExecuteCallback<Integer>(sqlType, isExceptionThrown, dataMap) {
            @Override
            protected Integer executeSQL(final StatementExecuteUnit executeUnit) throws SQLException {
                return ((PreparedStatement) executeUnit.getStatement()).executeUpdate();
            }
        };
    }
    
    /**
     * Get update SQLExecuteCallBack for StatementExecutor.
     *
     * @param sqlType           types of sql
     * @param isExceptionThrown is exception thrown
     * @param dataMap           data map
     * @return update SQLExecuteCallBack
     */
    public static SQLExecuteCallback<Integer> getUpdateSQLExecuteCallback(final SQLType sqlType, final boolean isExceptionThrown, final Map<String, Object> dataMap) {
        if (isSagaTransaction(sqlType)) {
            return getSagaUpdateSQLExecuteCallback(sqlType, isExceptionThrown, dataMap);
        }
        return new SQLExecuteCallback<Integer>(sqlType, isExceptionThrown, dataMap) {
            @Override
            protected Integer executeSQL(final StatementExecuteUnit executeUnit) throws SQLException {
                return executeUnit.getStatement().executeUpdate(executeUnit.getSqlExecutionUnit().getSqlUnit().getSql());
            }
        };
    }
    
    /**
     * Get update SQLExecuteCallBack with autoGeneratedKeys for StatementExecutor.
     *
     * @param sqlType           types of sql
     * @param isExceptionThrown is exception thrown
     * @param dataMap           data map
     * @param autoGeneratedKeys auto generated keys
     * @return update SQLExecuteCallBack with autoGeneratedKeys
     */
    public static SQLExecuteCallback<Integer> getUpdateSQLExecuteCallback(final SQLType sqlType, final boolean isExceptionThrown, final Map<String, Object> dataMap, final int autoGeneratedKeys) {
        return new SQLExecuteCallback<Integer>(sqlType, isExceptionThrown, dataMap) {
            @Override
            protected Integer executeSQL(final StatementExecuteUnit executeUnit) throws SQLException {
                return executeUnit.getStatement().executeUpdate(executeUnit.getSqlExecutionUnit().getSqlUnit().getSql(), autoGeneratedKeys);
            }
        };
    }
    
    /**
     * Get update SQLExecuteCallBack with column indexes for StatementExecutor.
     *
     * @param sqlType           types of sql
     * @param isExceptionThrown is exception thrown
     * @param dataMap           data map
     * @param columnIndexes     column indexes
     * @return update SQLExecuteCallBack with column indexes
     */
    public static SQLExecuteCallback<Integer> getUpdateSQLExecuteCallback(final SQLType sqlType, final boolean isExceptionThrown, final Map<String, Object> dataMap, final int[] columnIndexes) {
        return new SQLExecuteCallback<Integer>(sqlType, isExceptionThrown, dataMap) {
            @Override
            protected Integer executeSQL(final StatementExecuteUnit executeUnit) throws SQLException {
                return executeUnit.getStatement().executeUpdate(executeUnit.getSqlExecutionUnit().getSqlUnit().getSql(), columnIndexes);
            }
        };
    }
    
    /**
     * Get update SQLExecuteCallBack with column names for StatementExecutor.
     *
     * @param sqlType           types of sql
     * @param isExceptionThrown is exception thrown
     * @param dataMap           data map
     * @param columnNames       column names
     * @return update SQLExecuteCallBack with column names
     */
    public static SQLExecuteCallback<Integer> getUpdateSQLExecuteCallback(final SQLType sqlType, final boolean isExceptionThrown, final Map<String, Object> dataMap, final String[] columnNames) {
        return new SQLExecuteCallback<Integer>(sqlType, isExceptionThrown, dataMap) {
            @Override
            protected Integer executeSQL(final StatementExecuteUnit executeUnit) throws SQLException {
                return executeUnit.getStatement().executeUpdate(executeUnit.getSqlExecutionUnit().getSqlUnit().getSql(), columnNames);
            }
        };
    }
    
    /**
     * Get query SQLExecuteCallBack for PreparedStatementExecutor.
     *
     * @param sqlType           types of sql
     * @param isExceptionThrown is exception thrown
     * @param dataMap           data map
     * @return query SQLExecuteCallBack
     */
    public static SQLExecuteCallback<ResultSet> getPreparedQuerySQLExecuteCallback(final SQLType sqlType, final boolean isExceptionThrown, final Map<String, Object> dataMap) {
        return new SQLExecuteCallback<ResultSet>(sqlType, isExceptionThrown, dataMap) {
            @Override
            protected ResultSet executeSQL(final StatementExecuteUnit executeUnit) throws SQLException {
                return ((PreparedStatement) executeUnit.getStatement()).executeQuery();
            }
        };
    }
    
    /**
     * Get query SQLExecuteCallBack for StatementExecutor.
     *
     * @param sqlType           types of sql
     * @param isExceptionThrown is exception thrown
     * @param dataMap           data map
     * @return query SQLExecuteCallBack
     */
    public static SQLExecuteCallback<ResultSet> getQuerySQLExecuteCallback(final SQLType sqlType, final boolean isExceptionThrown, final Map<String, Object> dataMap) {
        return new SQLExecuteCallback<ResultSet>(sqlType, isExceptionThrown, dataMap) {
            @Override
            protected ResultSet executeSQL(final StatementExecuteUnit executeUnit) throws SQLException {
                return executeUnit.getStatement().executeQuery(executeUnit.getSqlExecutionUnit().getSqlUnit().getSql());
            }
        };
    }
    
    /**
     * Get single SQLExecuteCallBack for PreparedStatementExecutor.
     *
     * @param sqlType           types of sql
     * @param isExceptionThrown is exception thrown
     * @param dataMap           data map
     * @return single SQLExecuteCallBack
     */
    public static SQLExecuteCallback<Boolean> getPreparedSQLExecuteCallback(final SQLType sqlType, final boolean isExceptionThrown, final Map<String, Object> dataMap) {
        if (isSagaTransaction(sqlType)) {
            return getSagaSQLExecuteCallback(sqlType, isExceptionThrown, dataMap);
        }
        return new SQLExecuteCallback<Boolean>(sqlType, isExceptionThrown, dataMap) {
        
            @Override
            protected Boolean executeSQL(final StatementExecuteUnit executeUnit) throws SQLException {
                return ((PreparedStatement) executeUnit.getStatement()).execute();
            }
        };
    }
    
    /**
     * Get single SQLExecuteCallBack for StatementExecutor.
     *
     * @param sqlType           types of sql
     * @param isExceptionThrown is exception thrown
     * @param dataMap           data map
     * @return single SQLExecuteCallBack
     */
    public static SQLExecuteCallback<Boolean> getSQLExecuteCallback(final SQLType sqlType, final boolean isExceptionThrown, final Map<String, Object> dataMap) {
        if (isSagaTransaction(sqlType)) {
            return getSagaSQLExecuteCallback(sqlType, isExceptionThrown, dataMap);
        }
        return new SQLExecuteCallback<Boolean>(sqlType, isExceptionThrown, dataMap) {
            
            @Override
            protected Boolean executeSQL(final StatementExecuteUnit executeUnit) throws SQLException {
                return executeUnit.getStatement().execute(executeUnit.getSqlExecutionUnit().getSqlUnit().getSql());
            }
        };
    }
    
    /**
     * Get single SQLExecuteCallBack with autoGeneratedKeys for StatementExecutor.
     *
     * @param sqlType           types of sql
     * @param isExceptionThrown is exception thrown
     * @param dataMap           data map
     * @param autoGeneratedKeys auto generated keys
     * @return single SQLExecuteCallBack with autoGeneratedKeys
     */
    public static SQLExecuteCallback<Boolean> getSQLExecuteCallback(final SQLType sqlType, final boolean isExceptionThrown, final Map<String, Object> dataMap, final int autoGeneratedKeys) {
        return new SQLExecuteCallback<Boolean>(sqlType, isExceptionThrown, dataMap) {
            @Override
            protected Boolean executeSQL(final StatementExecuteUnit executeUnit) throws SQLException {
                return executeUnit.getStatement().execute(executeUnit.getSqlExecutionUnit().getSqlUnit().getSql(), autoGeneratedKeys);
            }
        };
    }
    
    /**
     * Get single SQLExecuteCallBack with column indexes for StatementExecutor.
     *
     * @param sqlType           types of sql
     * @param isExceptionThrown is exception thrown
     * @param dataMap           data map
     * @param columnIndexes     column indexes
     * @return single SQLExecuteCallBack with column indexes
     */
    public static SQLExecuteCallback<Boolean> getSQLExecuteCallback(final SQLType sqlType, final boolean isExceptionThrown, final Map<String, Object> dataMap, final int[] columnIndexes) {
        return new SQLExecuteCallback<Boolean>(sqlType, isExceptionThrown, dataMap) {
            @Override
            protected Boolean executeSQL(final StatementExecuteUnit executeUnit) throws SQLException {
                return executeUnit.getStatement().execute(executeUnit.getSqlExecutionUnit().getSqlUnit().getSql(), columnIndexes);
            }
        };
    }
    
    /**
     * Get single SQLExecuteCallBack with column names for StatementExecutor.
     *
     * @param sqlType           types of sql
     * @param isExceptionThrown is exception thrown
     * @param dataMap           data map
     * @param columnNames       column names
     * @return single SQLExecuteCallBack with column names
     */
    public static SQLExecuteCallback<Boolean> getSQLExecuteCallback(final SQLType sqlType, final boolean isExceptionThrown, final Map<String, Object> dataMap, final String[] columnNames) {
        return new SQLExecuteCallback<Boolean>(sqlType, isExceptionThrown, dataMap) {
            @Override
            protected Boolean executeSQL(final StatementExecuteUnit executeUnit) throws SQLException {
                return executeUnit.getStatement().execute(executeUnit.getSqlExecutionUnit().getSqlUnit().getSql(), columnNames);
            }
        };
    }
    
    private static SQLExecuteCallback<Integer> getSagaUpdateSQLExecuteCallback(final SQLType sqlType, final boolean isExceptionThrown, final Map<String, Object> dataMap) {
        return new SagaSQLExeucteCallback<Integer>(sqlType, isExceptionThrown, dataMap) {
            @Override
            protected Integer executeResult() {
                return 0;
            }
        };
    }
    
    private static SQLExecuteCallback<Boolean> getSagaSQLExecuteCallback(final SQLType sqlType, final boolean isExceptionThrown, final Map<String, Object> dataMap) {
        return new SagaSQLExeucteCallback<Boolean>(sqlType, isExceptionThrown, dataMap) {
            @Override
            protected Boolean executeResult() {
                return false;
            }
        };
    }
    
    private static boolean isSagaTransaction(final SQLType sqlType) {
        return SQLType.DML.equals(sqlType) && TransactionType.BASE.equals(TransactionTypeHolder.get());
    }
    
}
