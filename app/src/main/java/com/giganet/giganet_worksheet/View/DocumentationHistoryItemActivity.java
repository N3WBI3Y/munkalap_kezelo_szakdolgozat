package com.giganet.giganet_worksheet.View;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.giganet_worksheet.R;
import com.giganet.giganet_worksheet.Network.Documentation.RetrofitClientDocumentation;
import com.giganet.giganet_worksheet.Utils.SSOResult;
import com.giganet.giganet_worksheet.Persistence.Documentation.Entities.DocumentationHistoryEntity;
import com.giganet.giganet_worksheet.Utils.SSOService;
import com.github.chrisbanes.photoview.PhotoView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DocumentationHistoryItemActivity extends AppCompatActivity {
    private String picture;
    private PhotoView imageView;
    private ProgressBar imageLoadProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_documentation_history_item);
        imageView = findViewById(R.id.pv_fullscreen_document);
        imageLoadProgress = findViewById(R.id.pb_image_loading);
        Intent intent = getIntent();
        int id = intent.getIntExtra("id", 0);
        SSOService.getToken(getApplicationContext(), new SSOResult() {
            @Override
            public void onSuccess(String token) {
                Call<DocumentationHistoryEntity> call = RetrofitClientDocumentation.getInstance(getApplicationContext()).getApi().getImage(token, id);
                imageLoadProgress.setVisibility(View.VISIBLE);
                call.enqueue(new Callback<DocumentationHistoryEntity>() {
                    @Override
                    public void onResponse(Call<DocumentationHistoryEntity> call, Response<DocumentationHistoryEntity> response) {
                        if (response.body() != null) {
                            picture = response.body().getImage();
                            byte[] imageByteArray = Base64.decode(picture, Base64.DEFAULT);
                            Drawable image = new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length));
                            imageView.setImageDrawable(image);
                            imageLoadProgress.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(Call<DocumentationHistoryEntity> call, Throwable t) {
                        imageLoadProgress.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), "Nem sikerült elérnem a szervert", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onFailure(String token) {
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (picture != null && picture.length() > 0) {
            outState.putString("lastPicture", picture);
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        picture = savedInstanceState.getString("lastPicture");
        if (picture != null && picture.length() > 0) {
            imageLoadProgress.setVisibility(View.GONE);
            byte[] imageByteArray = Base64.decode(picture, Base64.DEFAULT);
            Drawable image = new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length));
            imageView.setImageDrawable(image);
        }
    }
}