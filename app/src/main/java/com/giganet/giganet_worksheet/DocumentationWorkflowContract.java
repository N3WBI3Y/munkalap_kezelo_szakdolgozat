package com.giganet.giganet_worksheet;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.net.Uri;

import com.giganet.giganet_worksheet.Resources.Adapters.DocumentationImagesAdapter;

import java.io.File;
import java.util.ArrayList;

public interface DocumentationWorkflowContract {
    interface Model {
        int getId();

        int getNumberOfPicture();

        ArrayList<String> getPictures();

        void setPictures(Context activity);

        File getFilePath();

        void setLocation(Context context, double longitude, double latitude);

        void createDocumentation(Context context, String type);

        void submit(Context context, String description);

        void copySelectedFiles(Uri source, String fileName, Context context, Location location);
    }

    interface Presenter {
        void takePhoto();

        void setNumberOfPictures();

        void removePicture(String path);

        void setLocation(double longitude, double latitude);

        void submit(String description);

        int getNumberOfPhotos();

        void copySelectedFiles(Uri source, String fileName, Location location);

        int getId();

    }

    interface View {

        Activity getActivity();

        void setAdapter(DocumentationImagesAdapter adapter);
    }
}
