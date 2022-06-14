package com.giganet.giganet_worksheet.Persistence.Worksheet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.provider.BaseColumns;

import com.giganet.giganet_worksheet.Network.Worksheet.MaterialDto;
import com.giganet.giganet_worksheet.Network.Worksheet.ServiceTypeDto;
import com.giganet.giganet_worksheet.Persistence.DataBaseHandler;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InstallationMaterialTableHandler {

    private static final String TABLE_NAME = "InstallationMaterials";
    private final Context context;

    public InstallationMaterialTableHandler(Context context) {
        this.context = context;
    }

    public static String createTable() {
        return "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ( "  + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DBColumns.NAME + " TEXT NOT NULL, " + DBColumns.MATERIALS + " TEXT NOT NULL )";
    }

    public static String getTableName() {
        return TABLE_NAME;
    }

    public static boolean isTableExists(Context context){
        InstallationMaterialTableHandler materialTableHandler = new InstallationMaterialTableHandler(context);
        return materialTableHandler.isTableExists();
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

    public void addMaterial(String name, String materials) {
        SQLiteDatabase db = DataBaseHandler.getInstance(context).getWritableDb();
        try {
            ContentValues cv = new ContentValues();
            cv.put(DBColumns.NAME, name);
            cv.put(DBColumns.MATERIALS, materials);
            db.insert(TABLE_NAME, null, cv);

        } catch (SQLiteException e) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            db.execSQL(createTable());
        }
    }

    public List<MaterialDto.Material> getMaterials(String serviceType) {
        ArrayList<MaterialDto.Material> actions = new ArrayList<>();
        SQLiteDatabase db = DataBaseHandler.getInstance(context).getReadableDb();
        Cursor cursor = db.rawQuery("Select " + DBColumns.MATERIALS + " from " + TABLE_NAME + " where " + DBColumns.NAME + "= ? ", new String[]{serviceType});
        cursor.moveToFirst();
        if (cursor.getCount() == 0) {
            cursor.close();
            return actions;
        }
        String materialString = cursor.getString(0);

        MaterialDto.Material[] actionArray = new Gson().fromJson(materialString, MaterialDto.Material[].class);

        return Arrays.asList(actionArray);
    }

    public void clearDb() {
        SQLiteDatabase db = DataBaseHandler.getInstance(context).getWritableDb();
        try {
            db.execSQL("DELETE FROM " + TABLE_NAME);

        } catch (SQLiteException e) {
            db.execSQL(createTable());
        }
    }



    public static class DBColumns implements BaseColumns {

        private static final String NAME = "name";

        private static final String MATERIALS = "materials";

    }
}
