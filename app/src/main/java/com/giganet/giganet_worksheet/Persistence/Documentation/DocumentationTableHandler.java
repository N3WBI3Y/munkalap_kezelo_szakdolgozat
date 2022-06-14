package com.giganet.giganet_worksheet.Persistence.Documentation;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.giganet.giganet_worksheet.Persistence.DataBaseHandler;
import com.giganet.giganet_worksheet.Persistence.Documentation.Entities.DocumentationEntity;
import com.giganet.giganet_worksheet.Resources.Enums.DBStatus;

import java.io.File;
import java.util.ArrayList;

public class DocumentationTableHandler {

    private static final String TABLE_NAME = "DocumentationDataBase";
    private final Context context;

    public DocumentationTableHandler(Context context) {
        this.context = context;
    }

    public static String getTableName() {
        return TABLE_NAME;
    }

    public static String createTable() {

        return "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ( " + DBColumns.ID + " INTEGER PRIMARY KEY NOT NULL, "
                + DBColumns.USER_ID + " TEXT NOT NULL, " + DBColumns.BACK_END_ID + " INTEGER ," + DBColumns.CREATED_TIME + " TEXT NOT NULL, " + DBColumns.LOCATION + " TEXT,  "
                + DBColumns.SUBJECT + " TEXT, " + DBColumns.TYPE + " INTEGER NOT NULL, " + DBColumns.STATUS + " TEXT NOT NULL  )";
    }

    public static boolean isTableExists(Context context){
        DocumentationTableHandler itemTableHandler = new DocumentationTableHandler(context);
        return itemTableHandler.isTableExists();
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

    public int getLastId() {
        SQLiteDatabase db = DataBaseHandler.getInstance(context).getReadableDb();
        Cursor cursor = db.rawQuery("Select " + DBColumns.ID + " from " + TABLE_NAME + " ORDER BY " + DBColumns.ID + " DESC LIMIT 1", new String[]{});
        cursor.moveToFirst();
        if (cursor.getColumnCount() == 0 || cursor.getCount() == 0) {
            return 0;
        }
        int result = cursor.getInt(0);
        cursor.close();

        return result;
    }

    public void addItem(int id, String username, String createdTime, String type) {
        SQLiteDatabase db = DataBaseHandler.getInstance(context).getWritableDb();
        try {
            ContentValues cv = new ContentValues();
            cv.put(DBColumns.ID, id);
            cv.put(DBColumns.USER_ID, username);
            cv.put(DBColumns.CREATED_TIME, createdTime);
            cv.put(DBColumns.TYPE, type);
            cv.put(DBColumns.STATUS, DBStatus.CREATED.value);

            db.insert(TABLE_NAME, null, cv);
        } catch (Exception e) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            db.execSQL(createTable());
        }
    }

    public boolean updateLocation(int id, double longitude, double latitude) {
        SQLiteDatabase db = DataBaseHandler.getInstance(context).getWritableDb();
        ContentValues cv = new ContentValues();
        String location = longitude + "," + latitude;
        cv.put(DBColumns.LOCATION, location);
        return db.update(TABLE_NAME, cv, DBColumns.ID + " = ? ", new String[]{String.valueOf(id)}) > 0;
    }

    public boolean updateBackendId(int id, int backendId) {
        SQLiteDatabase db = DataBaseHandler.getInstance(context).getWritableDb();
        ContentValues cv = new ContentValues();
        cv.put(DBColumns.BACK_END_ID, backendId);
        return db.update(TABLE_NAME, cv, DBColumns.ID + " = ? ", new String[]{String.valueOf(id)}) > 0;
    }

    public boolean updateSubject(int id, String subject) {
        SQLiteDatabase db = DataBaseHandler.getInstance(context).getWritableDb();
        ContentValues cv = new ContentValues();
        cv.put(DBColumns.SUBJECT, subject);
        return db.update(TABLE_NAME, cv, DBColumns.ID + " = ? ", new String[]{String.valueOf(id)}) > 0;
    }

    public boolean updateStatus(int id, String status) {
        SQLiteDatabase db = DataBaseHandler.getInstance(context).getWritableDb();
        ContentValues cv = new ContentValues();
        cv.put(DBColumns.STATUS, status);
        return db.update(TABLE_NAME, cv, DBColumns.ID + " = ? ", new String[]{String.valueOf(id)}) > 0;
    }

    public boolean removeItem(int id) {
        SQLiteDatabase db = DataBaseHandler.getInstance(context).getWritableDb();
        DocumentationItemTableHandler itemDBHandler = new DocumentationItemTableHandler(context.getApplicationContext());
        Cursor cursor = db.rawQuery("Select " + DBColumns.USER_ID + " from " + TABLE_NAME + " where " + DBColumns.ID + " = ?", new String[]{String.valueOf(id)});
        cursor.moveToFirst();
        if (cursor.getColumnCount() == 0 || cursor.getCount() == 0) {
            return false;
        }
        itemDBHandler.removeItem(cursor.getString(0), id);
        cursor.close();


        File file = new File(context.getExternalFilesDir("documentation").getAbsolutePath() + "/" + id);
        File[] createdPhotos = file.listFiles();
        if (createdPhotos != null && createdPhotos.length > 0) {
            for (File photo : createdPhotos) {
                photo.delete();
            }
        }

        return db.delete(TABLE_NAME, DBColumns.ID + " = ?", new String[]{String.valueOf(id)}) > 0;
    }

    public ArrayList<DocumentationEntity> getCreatedDocumentations() {
        SQLiteDatabase db = DataBaseHandler.getInstance(context).getReadableDb();
        ArrayList<DocumentationEntity> result = new ArrayList<>();

        Cursor cursor = db.rawQuery("Select * from " + TABLE_NAME + " where " + DBColumns.STATUS + " = 'CREATED'", new String[]{});

        cursor.moveToFirst();
        if (cursor.getColumnCount() == 0) {
            return result;
        }

        for (int row = 0; row < cursor.getCount(); ++row) {
            String[] location = cursor.getString(4) != null ? cursor.getString(4).split(",") : null;
            if (location != null) {
            }
            result.add(new DocumentationEntity(cursor.getString(1), cursor.getInt(0)
                    , cursor.getInt(2), location != null ? Double.parseDouble(location[0]) : null, location != null ? Double.parseDouble(location[1]) : null
                    , cursor.getString(3), cursor.getString(6), cursor.getString(5), cursor.getString(7), null));

            cursor.moveToNext();
        }

        cursor.close();
        return result;
    }

    public ArrayList<DocumentationEntity> getUploadableDocumentations(String username) {
        SQLiteDatabase db = DataBaseHandler.getInstance(context).getReadableDb();
        ArrayList<DocumentationEntity> result = new ArrayList<>();

        Cursor cursor = db.rawQuery("Select * from " + TABLE_NAME + " where " + DBColumns.STATUS + " = ? and " + DBColumns.USER_ID + " = ? ", new String[]{DBStatus.UPLOAD.value, username});

        cursor.moveToFirst();
        if (cursor.getColumnCount() == 0) {
            return result;
        }

        for (int row = 0; row < cursor.getCount(); ++row) {
            String[] location = cursor.getString(4) != null ? cursor.getString(4).split(",") : null;
            result.add(new DocumentationEntity(cursor.getString(1), cursor.getInt(0)
                    , cursor.getInt(2), location != null ? Double.parseDouble(location[0]) : null, location != null ? Double.parseDouble(location[1]) : null
                    , cursor.getString(3), cursor.getString(6), cursor.getString(5), cursor.getString(7), null));

            cursor.moveToNext();
        }

        cursor.close();
        return result;
    }

    public static class DBColumns implements BaseColumns {
        private static final String USER_ID = "user_id";

        private static final String ID = "id";

        private static final String BACK_END_ID = "back_end_id";

        private static final String LOCATION = "location";

        private static final String CREATED_TIME = "created_time";

        private static final String TYPE = "type";

        private static final String SUBJECT = "subject";

        private static final String STATUS = "status";

    }


}
