package com.giganet.giganet_worksheet.View;

import static android.text.Html.FROM_HTML_MODE_LEGACY;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Html;
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

public class SerialnumberVerificationActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler, View.OnClickListener {
    private ZXingScannerView scannerView;
    private TextView information;
    private LinearLayoutCompat informationLayout;
    private String serialNumber;
    private String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent data = getIntent();
        serialNumber = data.getStringExtra("serialNumber");
        type = data.getStringExtra("type");
        setContentView(R.layout.activity_serialnumber_verification);
        scannerView = findViewById(R.id.scanner_view);
        information = findViewById(R.id.t_barcode_information);
        Button back = findViewById(R.id.b_barcode_back);
        Button skip = findViewById(R.id.b_barcode_skip);
        informationLayout = findViewById(R.id.infoLayout);
        informationLayout.setVisibility(View.GONE);
        scannerView.setOnClickListener(this);
        back.setOnClickListener(this);
        skip.setOnClickListener(this);
        if (!PermissionContainer.CameraPermission.checkPermission(this)) {
            PermissionContainer.CameraPermission.askPermission(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean permission = true;
        for (int result : grantResults) {
            permission &= result == PackageManager.PERMISSION_GRANTED;
        }
        if (permission) {
            scannerView.setResultHandler(this);
            scannerView.startCamera();
        } else {
            finish();
        }
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
        String rawText = rawResult.getText().substring(rawResult.getText().length() - 8);
        rawText = rawText.substring(0, rawText.length() - 2).replaceFirst("^0+(?!$)", "");
        if (rawText.equals(serialNumber)) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("type", type);
            returnIntent.putExtra("parity", true);
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        } else {
            information.setText(Html.fromHtml("<p style=\"color:red\">A dokumentum nem felel meg!</p>", FROM_HTML_MODE_LEGACY));
            informationLayout.setVisibility(View.VISIBLE);
        }
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
            finish();
        } else if (v.getId() == R.id.b_barcode_skip) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("parity", true);
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        }
    }
}