package com.giganet.giganet_worksheet.Presenter;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;

import com.giganet.giganet_worksheet.DocumentationWorkflowContract;
import com.giganet.giganet_worksheet.Model.DocumentationModel;
import com.giganet.giganet_worksheet.Resources.Adapters.DocumentationImagesAdapter;
import com.giganet.giganet_worksheet.Resources.Enums.WorkState;
import com.giganet.giganet_worksheet.View.CameraViewActivity;

import java.io.File;


public class DocumentationWorkflowPresenter implements DocumentationWorkflowContract.Presenter {
    private final DocumentationWorkflowContract.View view;
    private final DocumentationWorkflowContract.Model model;
    private final DocumentationImagesAdapter adapter;


    public DocumentationWorkflowPresenter(DocumentationWorkflowContract.View view, int id, String filePath, String type) {
        this.view = view;
        this.model = new DocumentationModel(id,new File(filePath));

        model.createDocumentation(view.getActivity(), type);

        model.setPictures(view.getActivity());
        adapter = new DocumentationImagesAdapter(model.getPictures(), view.getActivity());
        view.setAdapter(adapter);
    }

    public void takePhoto() {
        Intent intent = new Intent(view.getActivity(), CameraViewActivity.class);
        intent.putExtra("filePath", model.getFilePath().getAbsolutePath());
        intent.putExtra("workState", WorkState.DOCUMENTATION.toString());
        intent.putExtra("workId", model.getId());
        view.getActivity().startActivity(intent);
    }

    public void setNumberOfPictures() {
        model.setPictures(view.getActivity());
        adapter.addPicture(model.getPictures());
        view.setAdapter(adapter);
    }

    public void removePicture(String path) {
        model.setPictures(view.getActivity());
        adapter.removePicture(model.getPictures());
        view.setAdapter(adapter);

    }

    public void submit(String description) {
        model.submit(view.getActivity(), description);
    }

    @Override
    public int getNumberOfPhotos() {
        return model.getNumberOfPicture();
    }

    @Override
    public void copySelectedFiles(Uri source, String fileName, Location location) {
        model.copySelectedFiles(source, fileName, view.getActivity(), location);
    }

    @Override
    public int getId() {
        return model.getId();
    }


    @Override
    public void setLocation(double longitude, double latitude) {
        model.setLocation(view.getActivity(), longitude, latitude);
    }

}
