package com.giganet.giganet_worksheet.View;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.example.giganet_worksheet.R;
import com.giganet.giganet_worksheet.Utils.PermissionContainer;
import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class BarcodeReaderActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler, View.OnClickListener {
    private ZXingScannerView scannerView;
    private TextView information;
    private Button back, add;
    private LinearLayoutCompat informationLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_reader);
        if (!PermissionContainer.CameraPermission.checkPermission(this)){
            PermissionContainer.CameraPermission.askPermission(this);
        }
        scannerView = findViewById(R.id.scanner_view);
        information = findViewById(R.id.t_barcode_information);
        back = findViewById(R.id.b_barcode_back);
        add = findViewById(R.id.b_barcode_add);
        informationLayout = findViewById(R.id.infoLayout);
        informationLayout.setVisibility(View.GONE);
        scannerView.setOnClickListener(this);
        back.setOnClickListener(this);
        add.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        scannerView.setResultHandler(this);
        scannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        scannerView.stopCamera();
    }

    @Override
    public void handleResult(Result rawResult) {
        information.setText(rawResult.getText());
        informationLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.scanner_view) {
            if (!scannerView.isActivated()) {
                information.setText("");
                scannerView.resumeCameraPreview(this);
                informationLayout.setVisibility(View.GONE);

            }
        } else if (v.getId() == R.id.b_barcode_back) {
            Intent returnIntent = new Intent();
            setResult(Activity.RESULT_CANCELED, returnIntent);
            scannerView.stopCamera();
            finish();
        } else if (v.getId() == R.id.b_barcode_add) {
            if (information.getText() != null && information.getText().toString().length() > 0) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("itemNumber", information.getText().toString());
                setResult(Activity.RESULT_OK, returnIntent);
                scannerView.stopCamera();
                finish();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean finalResult = false;
        for (int result : grantResults) {
            finalResult |= result == RESULT_OK;
        }
        if (finalResult) {
            Intent returnIntent = new Intent();
            setResult(Activity.RESULT_CANCELED, returnIntent);
            finish();
        } else {
            scannerView.startCamera();
        }
    }
}
