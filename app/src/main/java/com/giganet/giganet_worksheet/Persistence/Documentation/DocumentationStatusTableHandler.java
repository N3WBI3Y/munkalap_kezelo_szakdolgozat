package com.giganet.giganet_worksheet.Persistence.Documentation;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.giganet.giganet_worksheet.Network.Documentation.DocumentationStatusDto;
import com.giganet.giganet_worksheet.Persistence.DataBaseHandler;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

public class DocumentationStatusTableHandler {

    private static final String TABLE_NAME = "DocumentationStatusDataBase";
    private final Context context;

    public DocumentationStatusTableHandler(Context context) {
        this.context = context;
    }

    public static String createTable() {
        return "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ( " + DBColumns.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DBColumns.USERNAME + " TEXT NOT NULL, " + DBColumns.TYPES + " TEXT )";
    }

    public static String getTableName() {
        return TABLE_NAME;
    }


    public static boolean isTableExists(Context context){
        DocumentationStatusTableHandler itemTableHandler = new DocumentationStatusTableHandler(context);
        return itemTableHandler.isTableExists();
    }

    private boolean isTableExists() {
        SQLiteDatabase db = DataBaseHandler.getInstance(context).getReadableDb();
        String query = String.format("SELECT DISTINCT tbl_name FROM sqlite_master WHERE tbl_name = '%s'", TABLE_NAME);
        try (Cursor cursor = db.rawQuery(query, null)) {
            if(cursor!=null) {
                if(cursor.getCount()>0) {
                    return true;
                }
            }
            return false;
        }
    }

    public ArrayList<DocumentationStatusDto> getTaskTypes(String username) {
        ArrayList<DocumentationStatusDto> result = new ArrayList<>();
        SQLiteDatabase db = DataBaseHandler.getInstance(context).getReadableDb();

        try (Cursor cursor = db.rawQuery("Select "+DBColumns.TYPES + " from " + TABLE_NAME
                                        + " WHERE " + DBColumns.USERNAME + " = ?", new String[]{username})) {
            cursor.moveToFirst();
            if (cursor.getCount() == 0) {
                cursor.close();
                return result;
            }
            String typesRawString = cursor.getString(0);
            DocumentationStatusDto[] taskTypes = new Gson().fromJson(typesRawString, DocumentationStatusDto[].class);
            result.addAll(Arrays.asList(taskTypes));
            return result;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return  result;
    }

    public void addOrUpdateTypes(ArrayList<DocumentationStatusDto> types, String userName){
        SQLiteDatabase db = DataBaseHandler.getInstance(context).getWritableDb();
        Gson converter = new Gson();
        JSONArray typesString = new JSONArray();
        for (DocumentationStatusDto entity : types){
            String jsonString =  converter.toJson(entity);
            try {
                JSONObject object = new JSONObject(jsonString);
                typesString.put(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        ContentValues cv = new ContentValues();
        cv.put(DBColumns.USERNAME,userName);
        cv.put(DBColumns.TYPES,typesString.toString());

        long result = db.updateWithOnConflict(TABLE_NAME, cv, DBColumns.USERNAME + "= ? "
                                              , new String[]{userName}, SQLiteDatabase.CONFLICT_IGNORE);
        if (result > 0) {
        } else {
            db.insert(TABLE_NAME, null, cv);
        }
    }

    public static class DBColumns implements BaseColumns {

        private static final String ID = "id";

        private static final String USERNAME = "username";

        private static final String TYPES = "types";

    }


}
