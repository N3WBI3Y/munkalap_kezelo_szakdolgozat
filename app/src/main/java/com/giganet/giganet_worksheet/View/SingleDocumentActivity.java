package com.giganet.giganet_worksheet.View;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.giganet_worksheet.R;
import com.giganet.giganet_worksheet.Persistence.Documentation.DocumentationItemTableHandler;
import com.giganet.giganet_worksheet.Resources.Events.AddCommentEvent;
import com.giganet.giganet_worksheet.Resources.Events.RemovePictureEvent;
import com.github.chrisbanes.photoview.PhotoView;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

public class SingleDocumentActivity extends AppCompatActivity implements View.OnClickListener {

    private String picturePath;
    private EditText comment;
    private PhotoView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_picture);

        Intent data = getIntent();
        if (data != null) {
            picturePath = data.getStringExtra("picturepath");
        }
        comment = findViewById(R.id.et_single_picture);
        image = findViewById(R.id.single_picture_imageview);
        Button negative = findViewById(R.id.b_single_picture_negative);
        Button positive = findViewById(R.id.b_single_picture_positive);

        image.setImageURI(Uri.fromFile(new File(picturePath)));
        DocumentationItemTableHandler db = new DocumentationItemTableHandler(this);
        String subject = db.getItemSubject(picturePath);
        if (subject != null && subject.length() > 0) {
            comment.setText(subject);
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
            DocumentationItemTableHandler db = new DocumentationItemTableHandler(this);
            db.updateSubject(picturePath, comment.getText() == null ? null : comment.getText().toString());
            AddCommentEvent addCommentEvent = new AddCommentEvent(picturePath);
            EventBus.getDefault().post(addCommentEvent);
            finish();
        } else if (v.getId() == R.id.b_single_picture_negative) {
            //negative button
            if (new File(picturePath).delete()) {
                DocumentationItemTableHandler db = new DocumentationItemTableHandler(this);
                db.removeItem(picturePath);
                RemovePictureEvent removePicture = new RemovePictureEvent(picturePath);
                EventBus.getDefault().post(removePicture);
                finish();
            }
        } else if (v.getId() == R.id.single_picture_imageview) {
            comment.clearFocus();
        }
    }

    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
