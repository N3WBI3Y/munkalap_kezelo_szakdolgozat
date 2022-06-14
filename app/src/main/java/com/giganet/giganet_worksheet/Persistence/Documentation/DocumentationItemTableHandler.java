package com.giganet.giganet_worksheet.Persistence.Documentation;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.giganet.giganet_worksheet.Persistence.DataBaseHandler;
import com.giganet.giganet_worksheet.Persistence.Documentation.Entities.DocumentationItemEntity;
import com.giganet.giganet_worksheet.Resources.Enums.DBStatus;

import java.io.File;
import java.util.ArrayList;

public class DocumentationItemTableHandler {


    private static final String TABLE_NAME = "DocumentationItems";
    private final Context context;

    public DocumentationItemTableHandler(Context context) {
        this.context = context;
    }

    public static String createTable() {
        return "CREATE TABLE " + TABLE_NAME + " ( " + DBColumns.DOCUMENT_ID + " INTEGER NOT NULL, "
                + DBColumns.USER_ID + " TEXT NOT NULL, " + DBColumns.CREATED_TIME + " TEXT NOT NULL, " + DBColumns.PHOTO_PATH + " TEXT NOT NULL, " + DBColumns.SUBJECT + " TEXT,  "
                + DBColumns.STATUS + " TEXT NOT NULL , " + DBColumns.EXTERNALIDENTIFIER + " TEXT NOT NULL )";
    }

    public static String getTableName() {
        return TABLE_NAME;
    }

    public static boolean isTableExists(Context context){
        DocumentationItemTableHandler itemTableHandler = new DocumentationItemTableHandler(context);
        return itemTableHandler.isTableExists();
    }

    private boolean isTableExists() {
        SQLiteDatabase db = DataBaseHandler.getInstance(context).getWritableDb();
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

    public void addItem(int documentId, String photoPath, String userId, String createdTime, String externalIdentifier) {
        SQLiteDatabase db = DataBaseHandler.getInstance(context).getWritableDb();
        try {
            ContentValues cv = new ContentValues();
            cv.put(DBColumns.USER_ID, userId);
            cv.put(DBColumns.PHOTO_PATH, photoPath);
            cv.put(DBColumns.CREATED_TIME, createdTime);
            cv.put(DBColumns.DOCUMENT_ID, documentId);
            cv.put(DBColumns.STATUS, DBStatus.CREATED.value);
            cv.put(DBColumns.EXTERNALIDENTIFIER, externalIdentifier);

            db.insert(TABLE_NAME, null, cv);
        } catch (Exception e) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            db.execSQL(createTable());
        }

    }

    public boolean updateSubject(String photoPath, String subject) {
        SQLiteDatabase db = DataBaseHandler.getInstance(context).getWritableDb();
        ContentValues cv = new ContentValues();
        cv.put(DBColumns.SUBJECT, subject);
        return db.update(TABLE_NAME, cv, DBColumns.PHOTO_PATH + " = ? ", new String[]{photoPath}) > 0;
    }

    public boolean updateStatus(String photoPath, String status) {
        SQLiteDatabase db = DataBaseHandler.getInstance(context).getWritableDb();
        ContentValues cv = new ContentValues();
        cv.put(DBColumns.STATUS, status);
        return db.update(TABLE_NAME, cv, DBColumns.PHOTO_PATH + " = ? ", new String[]{photoPath}) > 0;
    }

    public boolean removeItem(String photoPath) {
        SQLiteDatabase db = DataBaseHandler.getInstance(context).getWritableDb();
        return db.delete(TABLE_NAME, DBColumns.PHOTO_PATH + " = ?", new String[]{photoPath}) > 0;
    }

    public boolean removeItem(String userName, int documentId) {
        SQLiteDatabase db = DataBaseHandler.getInstance(context).getWritableDb();
        ArrayList<DocumentationItemEntity> pictures = getDocumentIdItems(userName, documentId);
        for (DocumentationItemEntity item : pictures) {
            File pic = new File(item.getPhotoPath());
            if (pic.exists()) {
                pic.delete();
            }
        }
        return db.delete(TABLE_NAME, DBColumns.DOCUMENT_ID + " = ?", new String[]{String.valueOf(documentId)}) > 0;
    }

    public String getItemSubject(String photoPath) {
        SQLiteDatabase db = DataBaseHandler.getInstance(context).getReadableDb();
        String result = "";

        Cursor cursor = db.rawQuery("Select " + DBColumns.SUBJECT + " from " + TABLE_NAME + " where " + DBColumns.PHOTO_PATH + "= ? ", new String[]{photoPath});
        cursor.moveToFirst();
        if (cursor.getCount() == 0 || cursor.getColumnCount() == 0) {
            return result;
        }
        result = cursor.getString(0);
        cursor.close();
        return result;
    }

    public ArrayList<DocumentationItemEntity> getDocumentIdItems(String userId, int documentId) {
        SQLiteDatabase db = DataBaseHandler.getInstance(context).getReadableDb();
        ArrayList<DocumentationItemEntity> result = new ArrayList<>();

        Cursor cursor = db.rawQuery("Select * from " + TABLE_NAME + " where " + DBColumns.USER_ID + "= ?  and " + DBColumns.DOCUMENT_ID + " = ?", new String[]{userId, String.valueOf(documentId)});
        cursor.moveToFirst();
        if (cursor.getColumnCount() == 0) {
            return result;
        }
        for (int row = 0; row < cursor.getCount(); ++row) {
            result.add(new DocumentationItemEntity(cursor.getInt(0), cursor.getString(3)
                    , cursor.getString(1), cursor.getString(5), cursor.getString(4), cursor.getString(2), cursor.getString(6)));
            cursor.moveToNext();
        }
        cursor.close();
        return result;
    }

    public static class DBColumns implements BaseColumns {
        private static final String DOCUMENT_ID = "document_id";

        private static final String PHOTO_PATH = "photo_path";

        private static final String USER_ID = "user_id";

        private static final String STATUS = "status";

        private static final String EXTERNALIDENTIFIER = "external_identifier";

        private static final String SUBJECT = "subject";

        private static final String CREATED_TIME = "created_time";
    }

}
