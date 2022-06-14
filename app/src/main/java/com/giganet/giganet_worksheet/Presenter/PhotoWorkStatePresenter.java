package com.giganet.giganet_worksheet.Presenter;

import android.content.ClipData;
import android.content.Context;
import android.location.Location;
import android.net.Uri;

import com.giganet.giganet_worksheet.Model.PhotoWorkStateModel;
import com.giganet.giganet_worksheet.Resources.Events.NumberOfPictureChangeEvent;
import com.giganet.giganet_worksheet.Resources.Events.RemovePictureEvent;
import com.giganet.giganet_worksheet.View.WorkStateFragments.PhotoWorkStateContract;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class PhotoWorkStatePresenter implements PhotoWorkStateContract.Presenter {
    private final PhotoWorkStateContract.View view;
    private final PhotoWorkStateContract.Model model;

    public PhotoWorkStatePresenter(PhotoWorkStateContract.View view, int id, boolean must, String photoPath
            , int minNumberOfPictures, int maxNumberOfPictures, String type
            , boolean needSerialNumberScan, String serviceId) {
        this.view = view;
        model = new PhotoWorkStateModel(id, must, photoPath, minNumberOfPictures
                , maxNumberOfPictures, type, needSerialNumberScan, serviceId);
    }

    @Override
    public boolean isMust() {
        return model.isMust();
    }

    @Override
    public void copySelectedFiles(Uri image, Location location, Context context) {
        model.copySelectedFiles(image, location, context);
    }

    @Override
    public void copySelectedFiles(ClipData data, Location location, Context context) {
        model.copySelectedFiles(data, location, context);
        view.setAdapter(model.getPictures());
    }

    @Override
    public boolean isSet() {
        return model.isSet();
    }

    @Override
    public void updateAdapter() {
        view.setAdapter(model.getPictures());
    }

    @Override
    public void registForEvents() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void unregistForEvents() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    public boolean isNeedSerialNumberScan() {
        return model.isNeedSerialNumberScan();
    }

    @Override
    public boolean isSerialNumberScanned() {
        return model.isSerialNumberScanned();
    }

    @Override
    public void setSerialNumberScanned(boolean scanned) {
        model.setSerialNumberScanned(scanned);
    }

    @Override
    public String getServiceId() {
        return model.getServiceId();
    }

    @Override
    public String getPhotoPath() {
        return model.getPhotoPath();
    }

    @Override
    public int getId() {
        return model.getWorkId();
    }

    @Override
    public int getMaxNumberOfPicture() {
        return model.getMaxNumberOfPicture();
    }

    @Override
    public String getType() {
        return model.getType();
    }

    @Subscribe
    public void onNumberOfPictureChangeEvent(NumberOfPictureChangeEvent event) {
        if (event.getWorkState().equals(model.getType())) {
            view.setAdapter(model.getPictures());
        }
    }

    @Subscribe
    public void onRemovePictureEvent(RemovePictureEvent event) {
        if (event.getType().equals(model.getType())) {
            view.setAdapter(model.getPictures());
        }
    }
}
