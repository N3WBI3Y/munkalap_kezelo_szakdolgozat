package com.giganet.giganet_worksheet.View;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.giganet_worksheet.R;
import com.giganet.giganet_worksheet.DrawContract;
import com.giganet.giganet_worksheet.Presenter.DrawPresenter;

import java.io.File;

public class DrawActivity extends AppCompatActivity implements View.OnClickListener, DrawContract.View {
    private PaintView paintView;
    private DisplayMetrics displayMetrics;
    private ProgressBar progressBar;
    private Button delete, save;
    private DrawContract.Presenter presenter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.draw_activity);
        Intent data = getIntent();

        if (data.getExtras() != null){
            init(data.getExtras());
        } else {
            finish();
        }
        setListeners();
        TextDialogs dialog = new TextDialogs(this);
        dialog.showSignatureDialog("\"Lorem ipsum dolor sit amet, consectetur adipiscing elit. Duis convallis auctor lacus, et euismod neque elementum ac. Nulla facilisi. Sed eu ligula risus. Morbi convallis bibendum est non suscipit. Fusce finibus vestibulum massa nec accumsan. Vivamus tincidunt, mauris eget vulputate fringilla, libero lacus pellentesque nisi, iaculis vehicula enim quam ac mi. Mauris iaculis felis sollicitudin fermentum facilisis. Nam convallis justo eu faucibus elementum. Etiam cursus sapien tempus ante tincidunt, et mattis est lacinia.");

    }

    private void init(Bundle data){
        paintView = (PaintView) findViewById(R.id.draw_view);
        delete = findViewById(R.id.Signature_delete);
        save = findViewById(R.id.Signature_save);
        progressBar = findViewById(R.id.pb_loading);
        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        paintView.init(displayMetrics);
        int workId = data.getInt("workId", -1);
        File filePath = getExternalFilesDir(String.valueOf(workId));
        presenter = new DrawPresenter(this,filePath,data.getString("type"),workId);
        presenter.bindService();
    }

    private void setListeners(){
        delete.setOnClickListener(this);
        save.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int view_id = v.getId();

        if (view_id == R.id.Signature_delete) {
            paintView.clearCanvas();
        }
        if (view_id == R.id.Signature_save) {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setZ(15);
            presenter.saveDrawing(paintView);
        }
    }

    public void onFinish(){
        progressBar.setVisibility(View.GONE);
        Intent returnIntent = new Intent();
        returnIntent.putExtra("filePath", presenter.getFilePath());
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    @Override
    protected void onDestroy() {
        presenter.unbindService();
        super.onDestroy();
    }

    @Override
    public Activity getActivity() {
        return this;
    }
}