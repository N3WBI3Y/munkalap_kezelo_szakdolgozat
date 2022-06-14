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
import com.giganet.giganet_worksheet.Persistence.Worksheet.Entities.InstallationTransactionEntity;
import com.giganet.giganet_worksheet.Resources.Events.NewStatusEvent;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;

public class InstallationTaskTableHandler {

    private static final String TABLE_NAME = "InstallationTasks";
    private final Context context;

    public InstallationTaskTableHandler(Context context) {
        this.context = context;
    }

    public static String createTable() {
        return "CREATE TABLE " + TABLE_NAME + " ( " + DBColumns.USER_ID + " TEXT NOT NULL, " + DBColumns.ID + " INTEGER NOT NULL, "
                + DBColumns.SERVICE_ID + " INTEGER, " + DBColumns.CLIENTNAME + " TEXT NOT NULL, "
                + DBColumns.CITY + " TEXT NOT NULL, " + DBColumns.ADDRESS + " TEXT NOT NULL, " + DBColumns.PHONE + " TEXT NOT NULL, "
                + DBColumns.LOCATION + " TEXT, " + DBColumns.PARTIALNUMBER + " TEXT, " + DBColumns.CREATED_TIME + " TEXT NOT NULL, "
                + DBColumns.CREATED_BY + " TEXT NOT NULL, " + DBColumns.SUBJECT + " TEXT, " + DBColumns.STATUS + " TEXT NOT NULL , "
                + DBColumns.OWNERID + " TEXT NOT NULL, " + DBColumns.ORIGINAL_SUBJECT + " TEXT, "
                + DBColumns.ELAPSED_TIME + " INTEGER, " + DBColumns.VERSION + " INTEGER NOT NULL, "
                + DBColumns.FAVORITE + " INTEGER NOT NULL, " + DBColumns.RECENTLYADDED + " INTEGER NOT NULL , "
                + DBColumns.SERVICE_TYPE + " TEXT NOT NULL )";
    }

    public static String getTableName() {
        return TABLE_NAME;
    }

    public static boolean isTableExists(Context context){
        InstallationTaskTableHandler itemTableHandler = new InstallationTaskTableHandler(context);
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

    public void addTask(String userId, int id, String serviceId, String name, String city, String address
            , String phone, InstallationTaskEntity.GPSLocation location, String partialNumber, String createdTime, String createdBy
            , String subject, String status, String owner_id, int version, String serviceType) {
        SQLiteDatabase db = DataBaseHandler.getInstance(context).getWritableDb();

        String locationToJson = null;
        if (location != null) {
            locationToJson = new Gson().toJson(location);
        }

        ContentValues cv = new ContentValues();
        cv.put(DBColumns.USER_ID, userId);
        cv.put(DBColumns.ID, id);
        cv.put(DBColumns.SERVICE_ID, serviceId);
        cv.put(DBColumns.CLIENTNAME, name);
        cv.put(DBColumns.CITY, city);
        cv.put(DBColumns.ADDRESS, address);
        cv.put(DBColumns.PHONE, phone);
        cv.put(DBColumns.LOCATION, locationToJson);
        cv.put(DBColumns.PARTIALNUMBER, partialNumber);
        cv.put(DBColumns.CREATED_TIME, createdTime);
        cv.put(DBColumns.CREATED_BY, createdBy);
        cv.put(DBColumns.SUBJECT, subject);
        cv.put(DBColumns.STATUS, status);
        cv.put(DBColumns.OWNERID, owner_id);
        cv.put(DBColumns.ELAPSED_TIME, 0);
        cv.put(DBColumns.FAVORITE, 0);
        cv.put(DBColumns.VERSION, version);
        cv.put(DBColumns.RECENTLYADDED, 1);
        cv.put(DBColumns.SERVICE_TYPE, serviceType);
        cv.put(DBColumns.ORIGINAL_SUBJECT,subject);

        db.insert(TABLE_NAME, null, cv);

    }

    public ArrayList<InstallationTaskEntity> getAllTask(String userId) {
        SQLiteDatabase db = DataBaseHandler.getInstance(context).getReadableDb();
        ArrayList<InstallationTaskEntity> result = new ArrayList<>();
        try (Cursor cursor = db.rawQuery("Select * from " + TABLE_NAME + " where " + DBColumns.USER_ID + " = ? ", new String[]{userId})) {

            cursor.moveToFirst();
            if (cursor.getCount() == 0) {
                cursor.close();
                return result;
            }

            for (int i = 0; i < cursor.getCount(); i++) {

                InstallationTaskEntity.GPSLocation location = new Gson().fromJson(cursor.getString(7), InstallationTaskEntity.GPSLocation.class);

                InstallationTaskEntity.ServiceType serviceType = new InstallationTaskEntity.ServiceType(cursor.getString(19));

                InstallationTaskEntity.InstallationTaskStatus status = new InstallationTaskEntity.InstallationTaskStatus(0
                        , cursor.getString(12)
                        , cursor.getString(11), null
                        , cursor.getString(9), cursor.getString(13), cursor.getString(10));


                result.add(new InstallationTaskEntity(cursor.getInt(1), cursor.getInt(16), cursor.getString(2), null
                        , cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6),
                        location, cursor.getString(8), status, cursor.getLong(15), cursor.getInt(17), cursor.getInt(18)
                        , serviceType,cursor.getString(14)));
                cursor.moveToNext();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public void deleteTask(String username, int workId) {
        SQLiteDatabase db = DataBaseHandler.getInstance(context).getWritableDb();
        InstallationItemTableHandler itemDB = new InstallationItemTableHandler(context);
        ArrayList<InstallationItemEntity> workitems = itemDB.getWorkItems(workId, username);
        for (InstallationItemEntity item : workitems) {
            itemDB.deleteItemById(item.getId(), username);
            if (item.getPhoto_path() != null) {
                File file = new File(item.getPhoto_path());
                file.delete();
            }
        }
        InstallationTransactionTableHandler transactionTableHandler = new InstallationTransactionTableHandler(context);
        ArrayList<InstallationTransactionEntity> transactionEntities = transactionTableHandler.getWorkTransaction(workId,username);
        for (InstallationTransactionEntity transaction :transactionEntities) {
            transactionTableHandler.removeItem(transaction.getRowId());
        }

        File file = new File(context.getExternalFilesDir(String.valueOf(workId)).getAbsolutePath() + "/" + workId + ".txt");
        if (file.exists()) {
            file.delete();
        }

        db.delete(TABLE_NAME, DBColumns.USER_ID + " = ?" + " AND " + DBColumns.ID + " = ? ", new String[]{username, String.valueOf(workId)});
    }

    public void setTaskStatus(String username, int workId, String newStatus) {
        SQLiteDatabase db = DataBaseHandler.getInstance(context).getWritableDb();
        ContentValues cv = new ContentValues();
        cv.put(DBColumns.STATUS, newStatus);
        EventBus.getDefault().postSticky(new NewStatusEvent(workId, newStatus));
        db.update(TABLE_NAME, cv, DBColumns.USER_ID + " = ?" + " AND " + DBColumns.ID + " = ?", new String[]{username, String.valueOf(workId)});
    }
    
    public void setVersion(String username, int workId, int version){
        SQLiteDatabase db = DataBaseHandler.getInstance(context).getWritableDb();
        ContentValues cv = new ContentValues();
        cv.put(DBColumns.VERSION, version);
        db.update(TABLE_NAME, cv, DBColumns.USER_ID + " = ?" + " AND " + DBColumns.ID + " = ?", new String[]{username, String.valueOf(workId)});
    }

    public void incrementVersion(String username, int workId) {
        SQLiteDatabase db = DataBaseHandler.getInstance(context).getWritableDb();
        Cursor cursor = db.rawQuery("Select " + DBColumns.VERSION + " from " + TABLE_NAME + " where " + DBColumns.USER_ID + " = ?" + " AND " + DBColumns.ID + " = ?", new String[]{username, String.valueOf(workId)});
        cursor.moveToFirst();
        if (cursor.getCount() != 0) {
            int version = cursor.getInt(0);
            ContentValues cv = new ContentValues();
            cv.put(DBColumns.VERSION, version + 1);
            db.update(TABLE_NAME, cv, DBColumns.USER_ID + " = ?" + " AND " + DBColumns.ID + " = ?", new String[]{username, String.valueOf(workId)});
        }
        cursor.close();
    }

    public void updateFavorite(String username, int workId, int favorite) {
        SQLiteDatabase db = DataBaseHandler.getInstance(context).getWritableDb();
        ContentValues cv = new ContentValues();
        cv.put(DBColumns.FAVORITE, favorite);
        db.update(TABLE_NAME, cv, DBColumns.USER_ID + " = ?" + " AND " + DBColumns.ID + " = ?", new String[]{username, String.valueOf(workId)});
    }

    public void updateElapsedTime(String username, int workId, long elapsedTime) {
        SQLiteDatabase db = DataBaseHandler.getInstance(context).getWritableDb();
        ContentValues cv = new ContentValues();
        cv.put(DBColumns.ELAPSED_TIME, elapsedTime);
        db.update(TABLE_NAME, cv, DBColumns.USER_ID + " = ?" + " AND " + DBColumns.ID + " = ?", new String[]{username, String.valueOf(workId)});
    }

    public long getElapsedTime(String username, int workId) {
        SQLiteDatabase db = DataBaseHandler.getInstance(context).getReadableDb();
        long currentSubject = 0L;

        Cursor cursor = db.rawQuery("Select " + DBColumns.ELAPSED_TIME + " from " + TABLE_NAME + " where " + DBColumns.USER_ID + " = ?" + " AND " + DBColumns.ID + " = ?", new String[]{username, String.valueOf(workId)});
        cursor.moveToFirst();
        if (cursor.getCount() != 0) {
            currentSubject = (cursor.getLong(0));
        }
        cursor.close();
        return currentSubject;
    }

    public String getWorkCity(String username, int workId){
        SQLiteDatabase db = DataBaseHandler.getInstance(context).getReadableDb();
        String city = null;

        Cursor cursor = db.rawQuery("Select " + DBColumns.CITY + " from " + TABLE_NAME + " where " + DBColumns.USER_ID + " = ?" + " AND " + DBColumns.ID + " = ?", new String[]{username, String.valueOf(workId)});
        cursor.moveToFirst();
        if (cursor.getCount() != 0) {
            city = (cursor.getString(0));
        }
        cursor.close();
        return city;
    }

    public String getWorkAddress(String username, int workId){
        SQLiteDatabase db = DataBaseHandler.getInstance(context).getReadableDb();
        String city = null;

        Cursor cursor = db.rawQuery("Select " + DBColumns.ADDRESS + " from " + TABLE_NAME + " where " + DBColumns.USER_ID + " = ?" + " AND " + DBColumns.ID + " = ?", new String[]{username, String.valueOf(workId)});
        cursor.moveToFirst();
        if (cursor.getCount() != 0) {
            city = (cursor.getString(0));
        }
        cursor.close();
        return city;
    }

    public void setRecentlyAddedFalse(String username, int workId) {
        SQLiteDatabase db = DataBaseHandler.getInstance(context).getWritableDb();
        ContentValues cv = new ContentValues();
        cv.put(DBColumns.RECENTLYADDED, 0);
        db.update(TABLE_NAME, cv, DBColumns.USER_ID + " = ?" + " AND " + DBColumns.ID + " = ?", new String[]{username, String.valueOf(workId)});
    }

    public ArrayList<InstallationTaskEntity> getRunningTasks(String userId) {
        SQLiteDatabase db = DataBaseHandler.getInstance(context).getReadableDb();
        ArrayList<InstallationTaskEntity> result = new ArrayList<>();
        try (Cursor cursor = db.rawQuery("Select * from " + TABLE_NAME + " where " + DBColumns.USER_ID + " = ? AND " + DBColumns.STATUS + " IN ('BEGIN', 'STARTED')", new String[]{userId})) {

            cursor.moveToFirst();
            if (cursor.getCount() == 0) {
                cursor.close();
                return result;
            }

            for (int i = 0; i < cursor.getCount(); i++) {

                InstallationTaskEntity.GPSLocation location = new Gson().fromJson(cursor.getString(7), InstallationTaskEntity.GPSLocation.class);

                InstallationTaskEntity.ServiceType serviceType = new InstallationTaskEntity.ServiceType(cursor.getString(19));


                InstallationTaskEntity.InstallationTaskStatus status = new InstallationTaskEntity.InstallationTaskStatus(0
                        , cursor.getString(12)
                        , cursor.getString(11), null
                        , cursor.getString(9), cursor.getString(13), cursor.getString(10));


                result.add(new InstallationTaskEntity(cursor.getInt(1), cursor.getInt(16), cursor.getString(2), null
                        , cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6),
                        location, cursor.getString(8), status, cursor.getLong(15), cursor.getInt(17), cursor.getInt(18)
                        , serviceType,cursor.getString(14)));
                cursor.moveToNext();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public InstallationTaskEntity.GPSLocation getLocation(int workId) {
        SQLiteDatabase db = DataBaseHandler.getInstance(context).getReadableDb();
        InstallationTaskEntity.GPSLocation result = null;
        try (Cursor cursor = db.rawQuery("Select * from " + TABLE_NAME + " where " + DBColumns.ID + " = ? ", new String[]{String.valueOf(workId)})) {

            cursor.moveToFirst();
            if (cursor.getCount() == 0) {
                cursor.close();
                return null;
            }

            result = new Gson().fromJson(cursor.getString(7), InstallationTaskEntity.GPSLocation.class);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public ArrayList<Integer> getWorkIdWithSameAddress(String username, String city, String address){
        SQLiteDatabase db = DataBaseHandler.getInstance(context).getReadableDb();
        ArrayList<Integer> result = new ArrayList<>();

        try (Cursor cursor = db.rawQuery("Select " +DBColumns.ID +  " from " + TABLE_NAME + " where "
                + DBColumns.USER_ID + " = ? AND " + DBColumns.CITY + " = ? AND " + DBColumns.ADDRESS + " = ?" , new String[]{username,city,address})) {

            cursor.moveToFirst();
            if (cursor.getCount() == 0) {
                cursor.close();
                return null;
            }
            for (int i = 0; i < cursor.getCount(); i++) {
                result.add(cursor.getInt(0));
                cursor.moveToNext();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static class DBColumns implements BaseColumns {

        private static final String USER_ID = "user_id";

        private static final String ID = "id";

        private static final String SERVICE_ID = "service_id";

        private static final String CLIENTNAME = "client_name";

        private static final String CITY = "city";

        private static final String ADDRESS = "address";

        private static final String PHONE = "phone";

        private static final String LOCATION = "location";

        private static final String PARTIALNUMBER = "partialnumber";

        private static final String CREATED_TIME = "created_time";

        private static final String CREATED_BY = "created_by";

        private static final String SUBJECT = "subject";

        private static final String STATUS = "status";

        private static final String OWNERID = "owner_id";

        private static final String ORIGINAL_SUBJECT = "original_subject";

        private static final String ELAPSED_TIME = "elapsed_time";

        private static final String FAVORITE = "favorite";

        private static final String VERSION = "version";

        private static final String RECENTLYADDED = "recentlyAdded";

        private static final String SERVICE_TYPE = "serviceType";
    }

}
