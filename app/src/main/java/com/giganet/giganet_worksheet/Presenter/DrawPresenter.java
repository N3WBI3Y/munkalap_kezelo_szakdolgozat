package com.giganet.giganet_worksheet.Presenter;

import android.content.Context;
import android.content.Intent;

import com.giganet.giganet_worksheet.DrawContract;
import com.giganet.giganet_worksheet.Model.DrawModel;
import com.giganet.giganet_worksheet.Resources.Services.LocationService;
import com.giganet.giganet_worksheet.View.PaintView;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DrawPresenter implements DrawContract.Presenter {
    private final DrawContract.View view;
    private final DrawContract.Model model;

    public DrawPresenter(DrawContract.View view, File filePath, String type, int workId) {
        this.view = view;
        model = new DrawModel(filePath, type, workId);
    }

    @Override
    public void bindService() {
        Intent bindIntent = new Intent(view.getActivity(), LocationService.class);
        view.getActivity().bindService(bindIntent, model.getConnection(), Context.BIND_AUTO_CREATE);
    }

    @Override
    public void unbindService() {
        view.getActivity().unbindService(model.getConnection());
    }

    @Override
    public void saveDrawing(PaintView paintView) {
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                if(model.saveDrawing(view.getActivity(),paintView).get(10, TimeUnit.SECONDS)){
                    view.getActivity().runOnUiThread(view::onFinish);
                }
            } catch (ExecutionException | InterruptedException | TimeoutException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public File getFilePath() {
        return model.getFilePath();
    }
}
