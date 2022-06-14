package com.giganet.giganet_worksheet.Persistence.Worksheet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.giganet.giganet_worksheet.Persistence.DataBaseHandler;
import com.giganet.giganet_worksheet.Persistence.Worksheet.Entities.InstallationItemEntity;
import com.giganet.giganet_worksheet.Persistence.Worksheet.Entities.InstallationTransactionEntity;
import com.giganet.giganet_worksheet.Resources.Enums.DBStatus;

import java.util.ArrayList;

public class InstallationTransactionTableHandler {

    private static final String TABLE_NAME = "InstallationTransaction";
    private final Context context;

    public InstallationTransactionTableHandler(Context context) {
        this.context = context;
    }

    public static String createTable() {
        return "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ( " + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DBColumns.ID_WORK + " INTEGER NOT NULL, "
                + DBColumns.USERNAME + " TEXT NOT NULL, " + DBColumns.DATE+ " TEXT NOT NULL, "
                + DBColumns.MATERIAL + " TEXT NOT NULL, " + DBColumns.QUANTITY + " INTEGER NOT NULL, "
                + DBColumns.SERIALNUM + " TEXT NULL, " + DBColumns.STATUS + " TEXT NOT NULL ) ";
    }

    public static String getTableName() {
        return TABLE_NAME;
    }

    public static boolean isTableExists(Context context){
        InstallationTransactionTableHandler installationTransactionTableHandler = new InstallationTransactionTableHandler(context);
        return installationTransactionTableHandler.isTableExists();
    }



    private boolean isTableExists() {
        SQLiteDatabase db = DataBaseHandler.getInstance(context).getReadableDb();
        String query = String.format("select DISTINCT tbl_name from sqlite_master where tbl_name = '%s'", TABLE_NAME);
        try (Cursor cursor = db.rawQuery(query, null)) {
            if(cursor!=null) {
                if(cursor.getCount()>0) {
                    return true;
                }
            }
            return false;
        }
    }

    public boolean addTransaction(int workId, String username ,String date, String material, int quantity
            , String serialNum,String status) {
        SQLiteDatabase db = DataBaseHandler.getInstance(context).getWritableDb();
        try {
            ContentValues cv = new ContentValues();
            cv.put(DBColumns.ID_WORK, workId);
            cv.put(DBColumns.USERNAME, username);
            cv.put(DBColumns.DATE, date);
            cv.put(DBColumns.MATERIAL, material);
            cv.put(DBColumns.QUANTITY, quantity);
            cv.put(DBColumns.SERIALNUM, serialNum);
            cv.put(DBColumns.STATUS, status);

            return db.insert(TABLE_NAME, null, cv) > 0;
        } catch (Exception e) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            db.execSQL(createTable());
        }
        return false;
    }

    public ArrayList<InstallationTransactionEntity> getWorkTransaction(int workId, String username){
            ArrayList<InstallationTransactionEntity> result = new ArrayList<>();

            SQLiteDatabase db = DataBaseHandler.getInstance(context).getReadableDb();
            Cursor cursor = db.rawQuery("Select * from " + TABLE_NAME + " where " + DBColumns.ID_WORK + "=? AND " + DBColumns.USERNAME +  "=?", new String[]{String.valueOf(workId),username});
            cursor.moveToFirst();
            if (cursor.getCount() == 0) {
                cursor.close();
                return result;
            }

            for (int i = 0; i < cursor.getCount(); i++) {

                result.add(new InstallationTransactionEntity(cursor.getInt(0)
                        , cursor.getInt(1),cursor.getString(2), cursor.getString(3), cursor.getString(4)
                        , cursor.getInt(5), cursor.getString(6), cursor.getString(7)));
                cursor.moveToNext();

            }
            cursor.close();
            return result;
    }

    public void removeItem(int id) {
        SQLiteDatabase db = DataBaseHandler.getInstance(context).getWritableDb();

        db.delete(TABLE_NAME, DBColumns._ID + " = ? "
                , new String[]{String.valueOf(id)});
    }

    public void updateTransaction(int rowId, String date, String material, int quantity
            , String serialNum, String status) {

        SQLiteDatabase db = DataBaseHandler.getInstance(context).getWritableDb();
        ContentValues cv = new ContentValues();

        cv.put(DBColumns.DATE, date);
        cv.put(DBColumns.MATERIAL, material);
        cv.put(DBColumns.QUANTITY, quantity);
        cv.put(DBColumns.SERIALNUM, serialNum);
        cv.put(DBColumns.STATUS, status);

        db.update(TABLE_NAME, cv, DBColumns._ID + " = ?", new String[]{String.valueOf(rowId)});
    }

    public void updateStatus(int rowId, String status){
        SQLiteDatabase db = DataBaseHandler.getInstance(context).getWritableDb();
        ContentValues cv = new ContentValues();

        cv.put(DBColumns.STATUS, status);

        db.update(TABLE_NAME, cv, DBColumns._ID + " = ?", new String[]{String.valueOf(rowId)});
    }

    public ArrayList<InstallationTransactionEntity> getUpladableTransactions(String username){
        ArrayList<InstallationTransactionEntity> result = new ArrayList<>();

        SQLiteDatabase db = DataBaseHandler.getInstance(context).getReadableDb();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE "
                + DBColumns.USERNAME +  "=? AND " + DBColumns.STATUS + "=?"
                , new String[]{username, DBStatus.UPLOAD.value});
        cursor.moveToFirst();
        if (cursor.getCount() == 0) {
            cursor.close();
            return result;
        }

        for (int i = 0; i < cursor.getCount(); i++) {

            result.add(new InstallationTransactionEntity(cursor.getInt(0)
                    , cursor.getInt(1),cursor.getString(2), cursor.getString(3), cursor.getString(4)
                    , cursor.getInt(5), cursor.getString(6), cursor.getString(7)));
            cursor.moveToNext();

        }
        cursor.close();
        return result;
    }


    public static class DBColumns implements BaseColumns {

        public static final String ID_WORK = "id_work";

        public static final String USERNAME = "username";

        public static final String DATE = "date";

        public static final String MATERIAL = "material";

        public static final String QUANTITY = "quantity";

        public static final String SERIALNUM = "serialnum";

        public static final String STATUS = "status";

    }
}
