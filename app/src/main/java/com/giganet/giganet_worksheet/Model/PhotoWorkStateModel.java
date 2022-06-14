package com.giganet.giganet_worksheet.Model;

import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.location.Location;
import android.net.Uri;

import com.giganet.giganet_worksheet.Persistence.Worksheet.InstallationItemTableHandler;
import com.giganet.giganet_worksheet.Resources.Constants.ServiceConstants;
import com.giganet.giganet_worksheet.Resources.Enums.DBStatus;
import com.giganet.giganet_worksheet.Resources.Events.NumberOfPictureChangeEvent;
import com.giganet.giganet_worksheet.View.WorkStateFragments.PhotoWorkStateContract;

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

public class PhotoWorkStateModel extends WorkStateModel implements PhotoWorkStateContract.Model {
    private final int minNumberOfPicture;
    private final int maxNumberOfPicture;
    private final boolean needSerialNumberScan;
    private final String photoPath;
    private final String serviceId;
    private final ArrayList<String> pictures;
    private boolean serialNumberScanned;


    public PhotoWorkStateModel(int workId, boolean must, String photoPath
            , int minNumberOfPicture, int maxNumberOfPicture, String type
            , boolean needSerialNumberScan, String serviceId) {
        super(must,workId,type,null);
        this.photoPath = photoPath;
        this.minNumberOfPicture = minNumberOfPicture;
        this.maxNumberOfPicture = maxNumberOfPicture;
        this.needSerialNumberScan = needSerialNumberScan;
        this.serialNumberScanned = false;
        this.serviceId = serviceId;
        pictures = new ArrayList<>();
    }

    public String getType() {
        return title;
    }

    public boolean isNeedSerialNumberScan() {
        return needSerialNumberScan;
    }

    public boolean isSerialNumberScanned() {
        return serialNumberScanned;
    }

    @Override
    public void setSerialNumberScanned(boolean scanned) {
        this.serialNumberScanned = scanned;
    }

    public String getServiceId() {
        return serviceId;
    }

    @Override
    public boolean isMust() {
        return must;
    }

    @Override
    public ArrayList<String> getPictures() {
        pictures.clear();
        File file = new File(photoPath);
        File[] fileList = file.listFiles();
        if (fileList != null && fileList.length != 0) {
            for (File f : fileList) {
                if (f.toString().contains(title)) {
                    pictures.add(f.getAbsolutePath());
                }
            }
        }
        return pictures;
    }

    @Override
    public void copySelectedFiles(Uri imageUri, Location location, Context context) {
        addImageToDb(imageUri, title, context, location);
    }

    @Override
    public void copySelectedFiles(ClipData data, Location location, Context context) {
        int count = data.getItemCount();
        int currentItem = 0;
        while (currentItem < count) {
            Uri imageUri = data.getItemAt(currentItem).getUri();
            addImageToDb(imageUri, title, context, location);
            currentItem = currentItem + 1;
        }
    }

    @Override
    public String getPhotoPath() {
        return photoPath;
    }

    @Override
    public int getWorkId() {
        return workId;
    }

    @Override
    public int getMaxNumberOfPicture() {
        return maxNumberOfPicture;
    }


    @Override
    public boolean isSet() {
        return pictures.size() >= minNumberOfPicture;
    }

    private void addImageToDb(Uri source, String fileName, Context context, Location location) {
        InstallationItemTableHandler db = new InstallationItemTableHandler(context);
        db.addItem(workId, new SimpleDateFormat(ServiceConstants.DateFormat.DATE_FORMAT).format(new Date(System.currentTimeMillis() - 60 * 1000))
                , saveFile(source, fileName, context), fileName, location.getLongitude(), location.getLatitude(), null, DBStatus.CREATED.value);
        EventBus.getDefault().post(new NumberOfPictureChangeEvent(title, 0));
    }

    private String saveFile(Uri sourceFile, String fileName, Context context) {
        Random random = new Random();
        String timeStamp = String.valueOf(random.nextInt(10000000));
        String destinationFilename = context.getExternalFilesDir(String.valueOf(workId)).getAbsolutePath() + File.separatorChar + fileName
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
