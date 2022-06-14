package com.giganet.giganet_worksheet.Model;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.provider.Settings;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.giganet.giganet_worksheet.DocumentationWorkflowContract;
import com.giganet.giganet_worksheet.Persistence.Documentation.DocumentationItemTableHandler;
import com.giganet.giganet_worksheet.Persistence.Documentation.DocumentationTableHandler;
import com.giganet.giganet_worksheet.Persistence.Documentation.Entities.DocumentationItemEntity;
import com.giganet.giganet_worksheet.Resources.Constants.ServiceConstants;
import com.giganet.giganet_worksheet.Resources.Enums.DBStatus;
import com.giganet.giganet_worksheet.Resources.Enums.WorkState;
import com.giganet.giganet_worksheet.Resources.Events.NumberOfPictureChangeEvent;
import com.giganet.giganet_worksheet.Resources.Services.SaveToDocumentationBackEndWorker;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;


    public class DocumentationModel implements DocumentationWorkflowContract.Model {
    private final int id;
    private final ArrayList<String> pictures;
    private final File filePath;

    public DocumentationModel(int id, File filePath) {
        this.id = id;
        this.pictures = new ArrayList<>();
        this.filePath = filePath;
        if (!filePath.exists()) {
            filePath.mkdirs();
        }
    }

    public int getId() {
        return id;
    }

    public int getNumberOfPicture() {
        return pictures.size();
    }

    public ArrayList<String> getPictures() {
        return pictures;
    }

    public void setPictures(Context activity) {
        pictures.clear();
        File[] fileList = filePath.listFiles();
        if (fileList != null && fileList.length != 0) {
            for (File f : fileList) {
                if (f.length() < 50) {
                    DocumentationItemTableHandler db = new DocumentationItemTableHandler(activity);
                    db.removeItem(f.getAbsolutePath());
                    f.delete();
                } else {
                    pictures.add(f.getAbsolutePath());
                }
            }
        }
    }

    public void setLocation(Context context, double longitude, double latitude) {
        DocumentationTableHandler db = new DocumentationTableHandler(context);
        db.updateLocation(id, longitude, latitude);
    }

    @Override
    public void createDocumentation(Context context, String type) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(com.giganet.giganet_worksheet.Resources.Constants.SharedPreferences.SHAREDPREFERENCES, MODE_PRIVATE);
        DocumentationTableHandler db = new DocumentationTableHandler(context);
        db.addItem(id, sharedPreferences.getString("USERNAME", ""), new SimpleDateFormat(ServiceConstants.DateFormat.DATE_FORMAT).format(new Date(System.currentTimeMillis() - 60 * 1000)), type);
    }

    public void submit(Context context, String description) {
        DocumentationTableHandler documentationDB = new DocumentationTableHandler(context);
        DocumentationItemTableHandler itemDB = new DocumentationItemTableHandler(context);
        ArrayList<DocumentationItemEntity> items = itemDB.getDocumentIdItems(context.getSharedPreferences(com.giganet.giganet_worksheet.Resources.Constants.SharedPreferences.SHAREDPREFERENCES, MODE_PRIVATE).getString("USERNAME", ""), id);
        for (DocumentationItemEntity item : items) {
            itemDB.updateStatus(item.getPhotoPath(), DBStatus.UPLOAD.value);
        }
        documentationDB.updateStatus(id, DBStatus.UPLOAD.value);
        documentationDB.updateSubject(id, description);


        PeriodicWorkRequest uploadDocumentation = new PeriodicWorkRequest.Builder(SaveToDocumentationBackEndWorker.class, 15, TimeUnit.MINUTES)
                .setConstraints(new Constraints.Builder().setRequiresCharging(false).setRequiredNetworkType(NetworkType.CONNECTED).build()).build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork("SaveToDocumentationBackEnd", ExistingPeriodicWorkPolicy.REPLACE, uploadDocumentation);
    }

    @Override
    public void copySelectedFiles(Uri source, String fileName, Context context, Location location) {
        @SuppressLint("HardwareIds") String android_id = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        Random r = new Random();
        String externalIdentifier = android_id + "_" + new SimpleDateFormat(ServiceConstants.DateFormat.DATE_FORMAT).format(new Date(System.currentTimeMillis() - r.nextInt(1000) * 1000));
        DocumentationItemTableHandler db = new DocumentationItemTableHandler(context);
        db.addItem(id, saveFile(source, fileName, context), context.getSharedPreferences(com.giganet.giganet_worksheet.Resources.Constants.SharedPreferences.SHAREDPREFERENCES, MODE_PRIVATE).getString("USERNAME", "")
                , new SimpleDateFormat(ServiceConstants.DateFormat.DATE_FORMAT).format(new Date(System.currentTimeMillis() - 60 * 1000)), externalIdentifier);
        setPictures(context);
        EventBus.getDefault().post(new NumberOfPictureChangeEvent(WorkState.DOCUMENTATION.toString(), 0));
    }


    @Override
    public File getFilePath() {
        return filePath;
    }

    private String saveFile(Uri sourceFile, String fileName, Context context) {
        Random random = new Random();
        String timeStamp = String.valueOf(random.nextInt(10000000));
        String destinationFilename = context.getExternalFilesDir("documentation").getAbsolutePath()
                + File.separatorChar + id + File.separatorChar + fileName
                + "_" + timeStamp + ".jpeg";

        InputStream inputStream = null;
        OutputStream outputStream = null;

        ContentResolver resolver = context.getApplicationContext()
                .getContentResolver();

        try {
            inputStream = resolver.openInputStream(sourceFile);
            outputStream = new BufferedOutputStream(new FileOutputStream(destinationFilename, false));
            byte[] buf = new byte[1024];
            inputStream.read(buf);
            do {
                outputStream.write(buf);
            } while (inputStream.read(buf) != -1);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return destinationFilename;
    }
}
