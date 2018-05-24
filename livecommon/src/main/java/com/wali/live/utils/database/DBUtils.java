package com.wali.live.utils.database;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.base.log.MyLog;

/**
 * Created by MK on 15-3-2.
 */
public abstract class DBUtils {

    public static void createVirtualTableUsingFTS4(final SQLiteDatabase db, final String tableName,
                                                   final String[] columnsDefinition) {
        StringBuilder queryStr = new StringBuilder();
        queryStr.append(DBConstants.CREATE_VIRTUAL_TABLE);
        queryStr.append(tableName);
        queryStr.append(DBConstants.USING_FTS4);
        queryStr.append("(");
        // Add the columns now, Increase by 2
        for (int i = 0; i < (columnsDefinition.length - 1); i += 2) {
            if (i != 0) {
                queryStr.append(",");
            }
            queryStr.append(columnsDefinition[i] + " " + columnsDefinition[i + 1]);
        }
        queryStr.append(");");
        db.execSQL(queryStr.toString());
    }

    /**
     * 会自动加上_id这一列，并把此列设置为主键
     *
     * @param db
     * @param tableName
     * @param columnsDefinition ，不需要有_id这一列，会自动加上
     */
    public static void createTable(final SQLiteDatabase db, final String tableName,
                                   final String[] columnsDefinition) {
        StringBuilder queryStr = new StringBuilder();
        queryStr.append(DBConstants.CREATE_TABLE);
        queryStr.append(tableName);
        queryStr.append("(");
        queryStr.append(BaseColumns._ID); // 加上 _id这一列
        queryStr.append(DBConstants.INTEGER_PRIMARY_KEY);
        queryStr.append(", ");
        // Add the columns now, Increase by 2
        for (int i = 0; i < (columnsDefinition.length - 1); i += 2) {
            if (i != 0) {
                queryStr.append(",");
            }
            queryStr.append(columnsDefinition[i] + " " + columnsDefinition[i + 1]);
        }
        queryStr.append(");");
        db.execSQL(queryStr.toString());
    }

    /**
     * 会自动加上_id这一列，主键需要自己指定
     *
     * @param db
     * @param tableName
     * @param columnsDefinition
     * @param primaryKeyColumns 主键列，如果该数组为空，会把_id这列设置成主键
     */
    public static void createTable(final SQLiteDatabase db, final String tableName,
                                   final String[] columnsDefinition, final String[] primaryKeyColumns) {
        StringBuilder queryStr = new StringBuilder();
        queryStr.append(DBConstants.CREATE_TABLE);
        queryStr.append(tableName);
        queryStr.append("(");
        queryStr.append(BaseColumns._ID); // 加上 _id这一列
        queryStr.append(" INTEGER ");
        queryStr.append(",");
        // Add the columns now, Increase by 2
        for (int i = 0; i < (columnsDefinition.length - 1); i += 2) {
            if (i != 0) {
                queryStr.append(",");
            }
            queryStr.append(columnsDefinition[i] + " " + columnsDefinition[i + 1]);
        }
        queryStr.append(",").append(DBConstants.PRIMARY_KEY).append("(");

        // 设置主键
        if (primaryKeyColumns != null) {
            for (int i = 0; i < primaryKeyColumns.length; i++) {
                if (i != 0) {
                    queryStr.append(",");
                }
                queryStr.append(primaryKeyColumns[i]);
            }
        } else {
            queryStr.append(BaseColumns._ID);
        }
        queryStr.append("));");
        db.execSQL(queryStr.toString());
    }

    public static void safeExecuteSQL(final SQLiteDatabase db, String sql) {
        try {
            db.execSQL(sql);
        } catch (final SQLException e) {
            MyLog.e("safeExecuteSQL", e);
        }
    }

    /**
     * 此方法默认把_id这一列设为主键
     *
     * @param db
     * @param tableName
     * @param columns
     */
    public static void safeCreateTable(SQLiteDatabase db, final String tableName,
                                       final String[] columns) {
        try {
            createTable(db, tableName, columns);
        } catch (final SQLException e) {
            MyLog.e("safeCreateTable", e);
        }
    }

    /**
     * 此方法需要指定主键列，如果不指定，默认把_id设为主键
     *
     * @param db
     * @param tableName
     * @param columns
     * @param primeKeyColumns
     */
    public static void safeCreateTable(SQLiteDatabase db, final String tableName,
                                       final String[] columns, final String[] primeKeyColumns) {
        try {
            createTable(db, tableName, columns, primeKeyColumns);
        } catch (final SQLException e) {
            MyLog.e("safeCreateTable", e);
        }
    }

    /**
     * 安全执行，即执行失败不会抛出异常。所以，请保证各个参数的有效值，否则，执行失败。
     *
     * @param db
     * @param tableName
     * @param columnName
     * @param columnType
     */
    public static void safeAddColumn(SQLiteDatabase db, final String tableName, final String columnName, final String columnType) {
        StringBuilder sql = new StringBuilder();
        sql.append(DBConstants.ALTER_TABLE).append(tableName).append(DBConstants.ADD_COLUMN).append(columnName).append(" ").append(columnType);
        safeExecuteSQL(db, sql.toString());
    }

    /**
     * 安全执行，即执行失败不会抛出异常。所以，请保证各个参数的有效值，否则，执行失败。
     *
     * @param db
     * @param tableName
     * @param columnName
     */
    public static void safeDropColumn(SQLiteDatabase db, final String tableName, final String columnName) {
        StringBuilder sql = new StringBuilder();
        sql.append(DBConstants.ALTER_TABLE).append(tableName).append(DBConstants.DROP_COLUMN).append(columnName);
        safeExecuteSQL(db, sql.toString());
    }

    public static void clearDBData() {
    }
}
