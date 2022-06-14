package com.giganet.giganet_worksheet.View;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.giganet_worksheet.R;
import com.giganet.giganet_worksheet.Persistence.Worksheet.Entities.InstallationItemEntity;
import com.giganet.giganet_worksheet.Persistence.Worksheet.InstallationItemTableHandler;
import com.giganet.giganet_worksheet.Resources.Events.AddCommentEvent;
import com.giganet.giganet_worksheet.Resources.Events.RemovePictureEvent;
import com.giganet.giganet_worksheet.Utils.SharedPreference;
import com.github.chrisbanes.photoview.PhotoView;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;

public class SinglePictureActivity extends AppCompatActivity implements View.OnClickListener {

    private String picturePath;
    private EditText comment;
    private PhotoView image;
    private String type;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_picture);

        Intent data = getIntent();
        if (data != null) {
            picturePath = data.getStringExtra("picturepath");
            type = data.getStringExtra("type");
        }
        comment = findViewById(R.id.et_single_picture);
        image = findViewById(R.id.single_picture_imageview);
        Button negative = findViewById(R.id.b_single_picture_negative);
        Button positive = findViewById(R.id.b_single_picture_positive);
        username = new SharedPreference(this).getSharedPreferences().getString("USERNAME","");

        image.setImageURI(Uri.fromFile(new File(picturePath)));
        InstallationItemTableHandler db = new InstallationItemTableHandler(this);
        ArrayList<InstallationItemEntity> entity = db.getItem(picturePath, username);
        if (entity != null && entity.size() > 0) {
            comment.setText(entity.get(0).getComment() == null ? "" : entity.get(0).getComment());
        }

        negative.setOnClickListener(this);
        positive.setOnClickListener(this);
        image.setOnClickListener(this);
        comment.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.b_single_picture_positive) {
            InstallationItemTableHandler db = new InstallationItemTableHandler(this);
            db.updateComment(picturePath, comment.getText() == null ? null : comment.getText().toString(), username);
            AddCommentEvent addCommentEvent = new AddCommentEvent(picturePath);
            EventBus.getDefault().post(addCommentEvent);
            finish();
        } else if (v.getId() == R.id.b_single_picture_negative) {
            //negative button
            if (new File(picturePath).delete()) {
                InstallationItemTableHandler db = new InstallationItemTableHandler(this);
                db.deleteItemByPath(picturePath, username);
                RemovePictureEvent removePicture = new RemovePictureEvent(picturePath, type);
                EventBus.getDefault().postSticky(removePicture);
                finish();
            }
        } else if (v.getId() == R.id.single_picture_imageview) {
            comment.clearFocus();
        }
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
