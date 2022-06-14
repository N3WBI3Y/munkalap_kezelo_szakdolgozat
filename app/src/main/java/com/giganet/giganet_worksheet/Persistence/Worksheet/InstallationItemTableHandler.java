package com.giganet.giganet_worksheet.Persistence.Worksheet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

import com.giganet.giganet_worksheet.Persistence.DataBaseHandler;
import com.giganet.giganet_worksheet.Persistence.Worksheet.Entities.InstallationItemEntity;
import com.giganet.giganet_worksheet.Persistence.Worksheet.Entities.InstallationTaskEntity;
import com.giganet.giganet_worksheet.Resources.Enums.DBStatus;
import com.giganet.giganet_worksheet.Resources.Enums.WorkState;

import java.util.ArrayList;

public class InstallationItemTableHandler {

    private static final String TABLE_NAME = "SavedWorkState";
    private final Context context;

    public InstallationItemTableHandler(Context context) {
        this.context = context;
    }

    public static String createTable() {
        return "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ( " + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DBColumnsItem.ID_WORK + " INTEGER NOT NULL, "
                + DBColumnsItem.DATE + " TEXT NOT NULL, " + DBColumnsItem.WORKSTATE + " TEXT , "
                + DBColumnsItem.PHOTO_PATH + " TEXT, " + DBColumnsItem.LONGITUDE + " INTEGER null, "
                + DBColumnsItem.LATITUDE + " INTEGER NULL, " + DBColumnsItem.COMMENT + " TEXT, " + DBColumnsItem.STATUS + " TEXT, "
                + DBColumnsItem.USERNAME + " TEXT NOT NULL )";
    }

    public static String getTableName() {
        return TABLE_NAME;
    }


    public static boolean isTableExists(Context context){
        InstallationItemTableHandler itemTableHandler = new InstallationItemTableHandler(context);
        return itemTableHandler.isTableExists();
    }

    public synchronized boolean addItem(int idWork, String username ,String date, String photo_path, String workState
                           , Double longitude, Double latitude, String comment, String status) {
        SQLiteDatabase db = DataBaseHandler.getInstance(context).getWritableDb();
        try {
            ContentValues cv = new ContentValues();
            cv.put(DBColumnsItem.ID_WORK, idWork);
            cv.put(DBColumnsItem.USERNAME,username);
            cv.put(DBColumnsItem.DATE, date);
            cv.put(DBColumnsItem.PHOTO_PATH, photo_path);
            cv.put(DBColumnsItem.WORKSTATE, workState);
            cv.put(DBColumnsItem.LONGITUDE, longitude);
            cv.put(DBColumnsItem.LATITUDE, latitude);
            cv.put(DBColumnsItem.COMMENT, comment);
            cv.put(DBColumnsItem.STATUS, status);
            return db.insert(TABLE_NAME, null, cv) > 0;
        } catch (Exception e) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            db.execSQL(createTable());
        }
        return false;
    }

    public synchronized boolean addItem(int idWork,String username, String date, String workState
                          , Double longitude, Double latitude, String status) {
        return addItem(idWork,username, date, null, workState, longitude, latitude, null, status);
    }

    public boolean addItem(int idWork,String username, String date, String workState
                          , Double longitude, Double latitude, String comment, String status) {
        return addItem(idWork,username, date, null, workState, longitude, latitude, comment, status);
    }

    public ArrayList<InstallationItemEntity> getWorkItems(int workId, String username) {
        ArrayList<InstallationItemEntity> result = new ArrayList<>();

        SQLiteDatabase db = DataBaseHandler.getInstance(context).getReadableDb();
        Cursor cursor = db.rawQuery("Select * from " + TABLE_NAME + " where " + DBColumnsItem.ID_WORK + "=? AND " + DBColumnsItem.USERNAME +  "=? ORDER BY " + DBColumnsItem.DATE, new String[]{String.valueOf(workId),username});
        cursor.moveToFirst();
        if (cursor.getCount() == 0) {
            cursor.close();
            return result;
        }

        for (int i = 0; i < cursor.getCount(); i++) {

            result.add(new InstallationItemEntity(cursor.getInt(0),
                    cursor.getInt(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    Double.valueOf(cursor.getString(5)),
                    Double.valueOf(cursor.getString(6)), cursor.getString(7), cursor.getString(8)));
            cursor.moveToNext();

        }
        cursor.close();
        return result;
    }

    public ArrayList<InstallationItemEntity> getUploadableWorkItems(String username) {
        ArrayList<InstallationItemEntity> result = new ArrayList<>();

        SQLiteDatabase db = DataBaseHandler.getInstance(context).getReadableDb();
        Cursor cursor = db.rawQuery("Select * from " + TABLE_NAME + " where "
                + DBColumnsItem.USERNAME +  "=? AND " +DBColumnsItem.STATUS +  "=? ORDER BY " + DBColumnsItem.DATE, new String[]{username,DBStatus.UPLOAD.value});
        cursor.moveToFirst();
        if (cursor.getCount() == 0) {
            cursor.close();
            return result;
        }

        for (int i = 0; i < cursor.getCount(); i++) {

            result.add(new InstallationItemEntity(cursor.getInt(0),
                    cursor.getInt(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    Double.valueOf(cursor.getString(5)),
                    Double.valueOf(cursor.getString(6)), cursor.getString(7), cursor.getString(8)));
            cursor.moveToNext();

        }
        cursor.close();
        return result;
    }

    public ArrayList<InstallationItemEntity> getItem(String path, String username) {
        ArrayList<InstallationItemEntity> result = new ArrayList<>();

        SQLiteDatabase db = DataBaseHandler.getInstance(context).getReadableDb();
        Cursor cursor = db.rawQuery("Select * from " + TABLE_NAME + " where " + DBColumnsItem.PHOTO_PATH + "= ? AND " + DBColumnsItem.USERNAME +  "= ? ", new String[]{path, username});
        cursor.moveToFirst();
        if (cursor.getColumnCount() == 0) {
            return result;
        }

        for (int i = 0; i < cursor.getCount(); i++) {
            result.add(new InstallationItemEntity(cursor.getInt(0),
                    cursor.getInt(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    Double.valueOf(cursor.getString(5)),
                    Double.valueOf(cursor.getString(6)), cursor.getString(7), cursor.getString(8)));
            cursor.moveToNext();
        }
        cursor.close();
        return result;
    }

    public void updateComment(String photo_path, String comment, String username) {
        SQLiteDatabase db = DataBaseHandler.getInstance(context).getWritableDb();
        ContentValues cv = new ContentValues();
        cv.put(DBColumnsItem.COMMENT, comment);
        cv.put(DBColumnsItem.STATUS, DBStatus.CREATED.value);
        db.update(TABLE_NAME, cv, DBColumnsItem.PHOTO_PATH + " = ? AND " + DBColumnsItem.USERNAME + " = ?", new String[]{photo_path,username});
    }

    public void updateStatus(int id, DBStatus status, String username) {
        SQLiteDatabase db = DataBaseHandler.getInstance(context).getWritableDb();
        String query = "UPDATE " + TABLE_NAME + " SET " + DBColumnsItem.STATUS + " = " + "\"" + status.value + "\"" + " WHERE "
                        + DBColumnsItem._ID + " = " + id + " AND " + DBColumnsItem.USERNAME + " = " + "\""+ username + "\"";

        db.execSQL(query);
    }

    public void deleteItemByPath(String photo_path, String username) {
        SQLiteDatabase db = DataBaseHandler.getInstance(context).getWritableDb();

        db.delete(TABLE_NAME, DBColumnsItem.PHOTO_PATH + " = ? AND " + DBColumnsItem.USERNAME + " = ?"
                , new String[]{photo_path,username});
    }

    public boolean deleteItemById(int id, String username) {
        SQLiteDatabase db = DataBaseHandler.getInstance(context).getWritableDb();

        return db.delete(TABLE_NAME, DBColumnsItem._ID + " = ? AND " + DBColumnsItem.USERNAME + " = ?"
                , new String[]{String.valueOf(id),username}) > 0;
    }

    public void updateTextItem(int workId, String username, String date, String workState, Double longitude, Double latitude, String comment, String status) {
        SQLiteDatabase db = DataBaseHandler.getInstance(context).getWritableDb();
        ContentValues cv = new ContentValues();

        cv.put(DBColumnsItem.ID_WORK, workId);
        cv.put(DBColumnsItem.DATE, date);
        cv.put(DBColumnsItem.PHOTO_PATH, (String) null);
        cv.put(DBColumnsItem.WORKSTATE, workState);
        cv.put(DBColumnsItem.LONGITUDE, longitude);
        cv.put(DBColumnsItem.LATITUDE, latitude);
        cv.put(DBColumnsItem.COMMENT, comment);
        cv.put(DBColumnsItem.STATUS, status);
        cv.put(DBColumnsItem.USERNAME,username);

        long result = db.updateWithOnConflict(TABLE_NAME, cv, DBColumnsItem.WORKSTATE + " = ? AND "
                        + DBColumnsItem.ID_WORK + " = ? AND " + DBColumnsItem.USERNAME + " = ?"
                , new String[]{workState, String.valueOf(workId),username}, SQLiteDatabase.CONFLICT_IGNORE);
        if (result > 0) {
        } else {
            db.insert(TABLE_NAME, null, cv);
        }
    }

    public boolean updatePictureItem(int workId, String username, String date, String workState, String photoPath, Double longitude, Double latitude, String comment, String status) {
        SQLiteDatabase db = DataBaseHandler.getInstance(context).getWritableDb();

        ContentValues cv = new ContentValues();
        cv.put(DBColumnsItem.ID_WORK, workId);
        cv.put(DBColumnsItem.DATE, date);
        cv.put(DBColumnsItem.PHOTO_PATH, photoPath);
        cv.put(DBColumnsItem.WORKSTATE, workState);
        cv.put(DBColumnsItem.LONGITUDE, longitude);
        cv.put(DBColumnsItem.LATITUDE, latitude);
        cv.put(DBColumnsItem.COMMENT, comment);
        cv.put(DBColumnsItem.STATUS, status);
        cv.put(DBColumnsItem.USERNAME,username);

        long result = db.updateWithOnConflict(TABLE_NAME, cv, DBColumnsItem.WORKSTATE + "= ? AND "
                        + DBColumnsItem.ID_WORK + " = ? AND " + DBColumnsItem.USERNAME + " = ?"
                , new String[]{workState, String.valueOf(workId), username}, SQLiteDatabase.CONFLICT_IGNORE);
        if (result > 0) {
            return true;
        } else {
            result = db.insert(TABLE_NAME, null, cv);
            return result > 0;
        }
    }

    public void deleteTextItem(int workId, String username, String workState) {
        SQLiteDatabase db = DataBaseHandler.getInstance(context).getWritableDb();
        db.delete(TABLE_NAME, DBColumnsItem.WORKSTATE + "= ? AND "
                        + DBColumnsItem.ID_WORK + " = ? AND " + DBColumnsItem.USERNAME + " = ?"
                , new String[]{workState, String.valueOf(workId), username});
    }

    public void updateLocation(int workId, String username, String date, String workState, Double longitude, Double latitude, String comment, String status) {
        InstallationTaskTableHandler tableHandler = new InstallationTaskTableHandler(context);
        String city = tableHandler.getWorkCity(username, workId);
        String address = tableHandler.getWorkAddress(username, workId);
        ArrayList<Integer> idWithSameAddress = tableHandler.getWorkIdWithSameAddress(username,city,address);
        for (Integer id: idWithSameAddress) {
            updateTextItem(id, username, date, workState, longitude, latitude, comment, status);
        }
    }

    public InstallationTaskEntity.GPSLocation getLocation(int workId,String username, String workState) {
        SQLiteDatabase db = DataBaseHandler.getInstance(context).getReadableDb();

        Cursor cursor = db.rawQuery("SELECT " + DBColumnsItem.LONGITUDE + ", " + DBColumnsItem.LATITUDE + " FROM " + TABLE_NAME + " WHERE "
                        + DBColumnsItem.ID_WORK + "= ? AND " + DBColumnsItem.WORKSTATE + "= ? AND " + DBColumnsItem.USERNAME + " = ?"
                , new String[]{String.valueOf(workId), workState,username});
        cursor.moveToFirst();
        if (cursor.getCount() == 0) {
            cursor.close();
            return null;
        }
        InstallationTaskEntity.GPSLocation result = new InstallationTaskEntity.GPSLocation((float) cursor.getDouble(0), (float) cursor.getDouble(1));
        cursor.close();
        return result;
    }

    public String getTextItem(int workId, String workState, String username) {
        SQLiteDatabase db = DataBaseHandler.getInstance(context).getReadableDb();

        Cursor cursor = db.rawQuery("SELECT " + DBColumnsItem.COMMENT + " FROM " + TABLE_NAME + " WHERE "
                        + DBColumnsItem.ID_WORK + "= ? AND " + DBColumnsItem.WORKSTATE + "= ? AND "+ DBColumnsItem.USERNAME + " = ?"
                , new String[]{String.valueOf(workId), workState,username});
        cursor.moveToFirst();
        if (cursor.getCount() == 0) {
            cursor.close();
            return "";
        }
        String result = cursor.getString(0);
        cursor.close();
        return result;
    }

    public String getPhotoItem(int workId, String workState, String username) {
        SQLiteDatabase db = DataBaseHandler.getInstance(context).getReadableDb();

        Cursor cursor = db.rawQuery("SELECT " + DBColumnsItem.PHOTO_PATH + " FROM " + TABLE_NAME + " WHERE "
                        + DBColumnsItem.ID_WORK + "= ? AND " + DBColumnsItem.WORKSTATE + "= ? AND "+ DBColumnsItem.USERNAME + " = ?"
                , new String[]{String.valueOf(workId), workState,username});
        cursor.moveToFirst();
        if (cursor.getCount() == 0) {
            cursor.close();
            return "";
        }
        String result = cursor.getString(0);
        cursor.close();
        return result;
    }

    public ArrayList<InstallationItemEntity> getUploadableStatus(int workId, String username) {
        ArrayList<InstallationItemEntity> result = new ArrayList<>();

        SQLiteDatabase db = DataBaseHandler.getInstance(context).getReadableDb();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + DBColumnsItem.ID_WORK + "=?  AND "
                + DBColumnsItem.USERNAME + " = ? AND "
                + DBColumnsItem.WORKSTATE + " IN " + WorkState.GetStatusChangedString()
                + " AND " + DBColumnsItem.STATUS + "='" + DBStatus.UPLOAD.value +  "' ORDER BY "
                + DBColumnsItem.DATE, new String[]{String.valueOf(workId), username});
        cursor.moveToFirst();
        if (cursor.getCount() == 0) {
            cursor.close();
            return result;
        }

        for (int i = 0; i < cursor.getCount(); i++) {

            result.add(new InstallationItemEntity(cursor.getInt(0),
                    cursor.getInt(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    Double.valueOf(cursor.getString(5)),
                    Double.valueOf(cursor.getString(6)), cursor.getString(7), cursor.getString(8)));
            cursor.moveToNext();

        }
        cursor.close();
        return result;
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

    public static class DBColumnsItem implements BaseColumns {

        public static final String ID_WORK = "id_work";

        public static final String USERNAME = "username";

        public static final String DATE = "date";

        public static final String PHOTO_PATH = "photo_path";

        public static final String WORKSTATE = "work_state";

        public static final String LONGITUDE = "longitude";

        public static final String LATITUDE = "latitude";

        public static final String COMMENT = "comment";

        public static final String STATUS = "status";

    }


}
