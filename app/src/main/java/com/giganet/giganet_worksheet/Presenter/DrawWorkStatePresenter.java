package com.giganet.giganet_worksheet.Presenter;

import com.giganet.giganet_worksheet.Model.DrawWorkStateModel;
import com.giganet.giganet_worksheet.View.WorkStateFragments.DrawWorkStateContract;

public class DrawWorkStatePresenter implements DrawWorkStateContract.Presenter {
    private final DrawWorkStateContract.View view;
    private final DrawWorkStateContract.Model model;

        public DrawWorkStatePresenter(DrawWorkStateContract.View view, int workId,
                                      boolean must, String title, String username) {
        this.view = view;
        this.model = new DrawWorkStateModel(must, workId, title, username);
    }


    @Override
    public boolean isMust() {
        return model.isMust();
    }

    @Override
    public boolean isSet() {
        return model.isSet();
    }

    @Override
    public void setPicture() {
        view.setPicture(model.loadPicture(view.getActivity()));
    }

    @Override
    public void savePicture(String filePath) {
        model.savePicture(filePath);
    }

    @Override
    public String getTitle() {
        return model.getTitle();
    }

    @Override
    public int getWorkId() {
        return model.getWorkId();
    }
}
