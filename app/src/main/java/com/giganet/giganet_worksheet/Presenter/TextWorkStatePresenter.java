package com.giganet.giganet_worksheet.Presenter;

import android.location.Location;

import com.giganet.giganet_worksheet.Model.TextWorkStateModel;
import com.giganet.giganet_worksheet.View.WorkStateFragments.TextWorkStateContract;

public class TextWorkStatePresenter implements TextWorkStateContract.Presenter {
    private final TextWorkStateContract.View view;
    private final TextWorkStateContract.Model model;

    public TextWorkStatePresenter(TextWorkStateContract.View view, boolean must, int workId
            , String type, int minLength, int maxLength, String title, String username) {
        this.view = view;
        model = new TextWorkStateModel(username, must, workId, type, minLength, maxLength, title);
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
    public void setText() {
        view.setText(model.loadText(view.getActivity()));
        view.setCheckMark();
    }

    @Override
    public void saveText(String text, Location location) {
        model.saveText(view.getActivity(), text, location);
        view.setCheckMark();
    }

    @Override
    public String getText() {
        return model.getText();
    }


}
