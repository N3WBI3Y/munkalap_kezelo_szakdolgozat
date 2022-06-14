package com.giganet.giganet_worksheet.Persistence;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.CursorWindow;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.giganet_worksheet.BuildConfig;
import com.giganet.giganet_worksheet.Persistence.Documentation.DocumentationItemTableHandler;
import com.giganet.giganet_worksheet.Persistence.Documentation.DocumentationTableHandler;
import com.giganet.giganet_worksheet.Persistence.Documentation.DocumentationStatusTableHandler;
import com.giganet.giganet_worksheet.Persistence.Worksheet.InstallationItemTableHandler;
import com.giganet.giganet_worksheet.Persistence.Worksheet.InstallationMaterialTableHandler;
import com.giganet.giganet_worksheet.Persistence.Worksheet.InstallationServiceTypesTableHandler;
import com.giganet.giganet_worksheet.Persistence.Worksheet.InstallationTaskTableHandler;
import com.giganet.giganet_worksheet.Persistence.Worksheet.InstallationTransactionTableHandler;
import com.giganet.giganet_worksheet.Resources.Constants.ServiceConstants;

import java.lang.reflect.Field;

public class DataBaseHandler extends SQLiteOpenHelper{

    private static final String DB_NAME = "WorksheetDB";
    private static DataBaseHandler _instance;

    public DataBaseHandler(Context context){
        super(context, DB_NAME, null, ServiceConstants.VersionControl.DATABASEVERSION);
    }

    public synchronized static DataBaseHandler getInstance(Context context) {
        if (_instance == null) {
            _instance = new DataBaseHandler(context);
        }
        return _instance;
    }

    public SQLiteDatabase getReadableDb(){
        return  getReadableDatabase();
    }

    public SQLiteDatabase getWritableDb(){
        return getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
       createTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        deleteTables(db);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        deleteTables(db);
        onCreate(db);
    }


    public void createDataBase(Context context) {
        setCursorWindowSize();
        SQLiteDatabase db = this.getWritableDatabase();
        if (db != null && db.getVersion() <  ServiceConstants.VersionControl.DATABASEVERSION){
            onUpgrade(db,db.getVersion(),ServiceConstants.VersionControl.DATABASEVERSION);
        } else if (db != null && db.getVersion() > ServiceConstants.VersionControl.DATABASEVERSION){
            onDowngrade(db,db.getVersion(),ServiceConstants.VersionControl.DATABASEVERSION);
        }
        checkIfAllTableExists(db,context);
    }

    private void deleteTables(SQLiteDatabase db){
        db.execSQL("DROP TABLE IF EXISTS " + DocumentationTableHandler.getTableName());
        db.execSQL("DROP TABLE IF EXISTS " + DocumentationItemTableHandler.getTableName());
        db.execSQL("DROP TABLE IF EXISTS " + DocumentationStatusTableHandler.getTableName());
        db.execSQL("DROP TABLE IF EXISTS " + InstallationTaskTableHandler.getTableName());
        db.execSQL("DROP TABLE IF EXISTS " + InstallationItemTableHandler.getTableName());
        db.execSQL("DROP TABLE IF EXISTS " + InstallationServiceTypesTableHandler.getTableName());
        db.execSQL("DROP TABLE IF EXISTS " + InstallationMaterialTableHandler.getTableName());
        db.execSQL("DROP TABLE IF EXISTS " + InstallationTransactionTableHandler.getTableName());
    }

    private void createTables(SQLiteDatabase db){
        db.execSQL(DocumentationTableHandler.createTable());
        db.execSQL(DocumentationItemTableHandler.createTable());
        db.execSQL(DocumentationStatusTableHandler.createTable());
        db.execSQL(InstallationTaskTableHandler.createTable());
        db.execSQL(InstallationItemTableHandler.createTable());
        db.execSQL(InstallationServiceTypesTableHandler.createTable());
        db.execSQL(InstallationMaterialTableHandler.createTable());
        db.execSQL(InstallationTransactionTableHandler.createTable());
    }

    private void checkIfAllTableExists(SQLiteDatabase db ,Context context){
        if (!DocumentationTableHandler.isTableExists(context)){
            db.execSQL(DocumentationTableHandler.createTable());
        }

        if (!DocumentationItemTableHandler.isTableExists(context)){
            db.execSQL(DocumentationItemTableHandler.createTable());
        }

        if (!DocumentationStatusTableHandler.isTableExists(context)){
            db.execSQL(DocumentationStatusTableHandler.createTable());
        }

        if(!InstallationTaskTableHandler.isTableExists(context)){
            db.execSQL(InstallationTaskTableHandler.createTable());
        }

        if (!InstallationItemTableHandler.isTableExists(context)){
            db.execSQL(InstallationItemTableHandler.createTable());
        }

        if (!InstallationServiceTypesTableHandler.isTableExists(context)){
            db.execSQL(InstallationServiceTypesTableHandler.createTable());
        }

        if (!InstallationTransactionTableHandler.isTableExists(context)){
            db.execSQL(InstallationTransactionTableHandler.createTable());
        }

        if(!InstallationMaterialTableHandler.isTableExists(context)){
            db.execSQL(InstallationMaterialTableHandler.createTable());
        }
    }

    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen()){
            db.close();
        }
    }

    private void setCursorWindowSize(){
        try {
            @SuppressLint("DiscouragedPrivateApi") Field field = CursorWindow.class.getDeclaredField(ServiceConstants.VersionControl.CURSORWINDOWSIZE);
            field.setAccessible(true);
            field.set(null, 100 * 1024 * 1024); //100MB
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
    }

}
