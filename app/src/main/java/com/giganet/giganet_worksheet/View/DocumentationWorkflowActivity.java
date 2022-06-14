package com.giganet.giganet_worksheet.View;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.giganet_worksheet.R;
import com.giganet.giganet_worksheet.DocumentationWorkflowContract;
import com.giganet.giganet_worksheet.Persistence.Documentation.DocumentationTableHandler;
import com.giganet.giganet_worksheet.Presenter.DocumentationWorkflowPresenter;
import com.giganet.giganet_worksheet.Resources.Adapters.DocumentationImagesAdapter;
import com.giganet.giganet_worksheet.Resources.Constants.ServiceConstants;
import com.giganet.giganet_worksheet.Resources.Enums.WorkState;
import com.giganet.giganet_worksheet.Resources.Events.AddCommentEvent;
import com.giganet.giganet_worksheet.Resources.Events.EditTextClickOutEvent;
import com.giganet.giganet_worksheet.Resources.Events.LocationEvent;
import com.giganet.giganet_worksheet.Resources.Events.NumberOfPictureChangeEvent;
import com.giganet.giganet_worksheet.Resources.Events.RemovePictureEvent;
import com.giganet.giganet_worksheet.Resources.Services.LocationService;
import com.giganet.giganet_worksheet.Utils.NetworkHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class DocumentationWorkflowActivity extends AppCompatActivity implements DocumentationWorkflowContract.View,
                                                                                View.OnClickListener {
    private RecyclerView documentView;
    private ActivityResultLauncher<Intent> locationActivityLauncher;
    private ActivityResultLauncher<Intent> documentationResultLauncher;
    private DocumentationWorkflowContract.Presenter presenter;
    private TextView tLongitude, tlatitude;
    private EditText description;
    private Button back, submit, browseDocumentation;
    private AppCompatButton photo, location;
    private LocationService locationService;
    private ProgressBar loadingBar;
    private boolean gpsSet = false;
    private boolean submitable;
    private final ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocationService.LocalBinder binder = (LocationService.LocalBinder) service;
            locationService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_documentation_workflow);
        Bundle data = getIntent().getExtras();
        init(data);

        Intent bindIntent = new Intent(this, LocationService.class);
        bindService(bindIntent, connection, Context.BIND_AUTO_CREATE);

        setListeners();
        changeSubmitButton();
        initCheckMarks();

        registerLocationActivityLauncher();
        registerPhotoActivityLauncher();

        EventBus.getDefault().register(this);
    }

    private void registerPhotoActivityLauncher() {
        documentationResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            locationService.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(@NonNull Location location) {
                                    Intent data = result.getData();
                                    if (data.getClipData() != null) {
                                        int count = data.getClipData().getItemCount();
                                        int currentItem = 0;
                                        while (currentItem < count) {
                                            Uri imageUri = data.getClipData().getItemAt(currentItem).getUri();
                                            presenter.copySelectedFiles(imageUri, WorkState.DOCUMENTATION.toString(), location);
                                            currentItem = currentItem + 1;
                                        }
                                    } else if (data.getData() != null) {
                                        Uri imageUri = data.getData();
                                        presenter.copySelectedFiles(imageUri, WorkState.DOCUMENTATION.toString(), location);
                                    }
                                }
                            });
                        }
                    }
                });
    }

    private void registerLocationActivityLauncher() {
        locationActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // There are no request codes
                            Intent data = result.getData();
                            if (data != null) {
                                if (data.getDoubleExtra("longitude", -1) != -1) {
                                    double longitude = data.getDoubleExtra("longitude", -1);
                                    double latitude = data.getDoubleExtra("latitude", -1);
                                    if (longitude == -1 && latitude == -1) {
                                        Toast.makeText(getApplicationContext(), "Hiba történt a koordináták átadásakor", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    tLongitude.setText(String.format("Lon: %f", longitude));
                                    tlatitude.setText(String.format("Lat: %f", latitude));
                                    presenter.setLocation(longitude, latitude);
                                    gpsSet = true;
                                    changeSubmitButton();
                                    setMarks(findViewById(R.id.iv_LOCATION_mark), true);
                                }
                            }
                        }
                    }
                });
    }

    private void setListeners() {
        back.setOnClickListener(this);
        submit.setOnClickListener(this);
        photo.setOnClickListener(this);
        location.setOnClickListener(this);
        browseDocumentation.setOnClickListener(this);
        description.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ServiceConstants.CameraService.REQUEST_PICTURE_CAPTURED) {
            if (resultCode == RESULT_OK) {
                sendMessage();
            }
        }
    }

    @Override
    public void onBackPressed() {
        DocumentationTableHandler db = new DocumentationTableHandler(this);
        db.removeItem(presenter.getId());
        super.onBackPressed();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    EventBus.getDefault().postSticky(new EditTextClickOutEvent(presenter.getId()));
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private void sendMessage() {
        EventBus.getDefault().post(new NumberOfPictureChangeEvent(WorkState.DOCUMENTATION.toString(), 0));
    }

    @Subscribe
    public void onEditTextClickOutEvent(EditTextClickOutEvent event) {
        changeSubmitButton();
    }

    private void initCheckMarks() {
        if (presenter.getNumberOfPhotos() > 0) {
            setMarks(findViewById(R.id.iv_DOCUMENTATION_mark), true);
        } else {
            setMarks(findViewById(R.id.iv_DOCUMENTATION_mark), false);
        }

        if (gpsSet) {
            setMarks(findViewById(R.id.iv_LOCATION_mark), true);
        } else {
            setMarks(findViewById(R.id.iv_LOCATION_mark), false);
        }
    }

    private void changeSubmitButton() {
        submitable = presenter.getNumberOfPhotos() > 0 && gpsSet && description.getText().toString().length() > 0 && areAllPictureCommented();
        if (submitable) {
            submit.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.Giganet_green, null)));
        } else {
            submit.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.UnSubmitable, null)));
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.b_BACK) {
            dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
            dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
        } else if (v.getId() == R.id.b_SUBMIT) {
            onSubmit();

        } else if (v.getId() == R.id.i_IMAGES) {
            presenter.takePhoto();
        } else if (v.getId() == R.id.i_LOCATION) {
            onMapClick();
        } else if (v.getId() == R.id.i_BROWSER) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            documentationResultLauncher.launch(Intent.createChooser(intent, "Dokumentálni kívánt kép hozzáadása"));
        }
    }

    private void onSubmit() {
        if (!submitable) {
            String msg = (presenter.getNumberOfPhotos() == 0 ? "Még nem készítettél képet a dokumentációhoz!\n" : "") +
                    (!gpsSet ? "Még nincs megadva a helyszín!\n" : "") +
                    (description.getText().toString().length() == 0 ? "Még nincs leírás megadva\n" : "") +
                    (areAllPictureCommented() ? "" : "Még nincs mindegyik képhez komment kapcsolva");
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            return;
        }

        presenter.submit(description.getText().toString());

        finish();
    }

    public void setAdapter(DocumentationImagesAdapter adapter) {
        documentView.setAdapter(adapter);
    }

    private void onMapClick() {
        loadingBar.setVisibility(View.VISIBLE);
        locationService.getCurrentLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                task.addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        Log.d("MyApp", "onSuccess: " + location.getTime() +  " " + location.getAccuracy());
                        if (NetworkHelper.isNetworkAvailable(getApplicationContext())) {
                            Intent placePickerIntent = new Intent(getApplicationContext(), MapActivity.class);
                            placePickerIntent.putExtra("longitude", location.getLongitude());
                            placePickerIntent.putExtra("latitude", location.getLatitude());
                            locationActivityLauncher.launch(placePickerIntent);
                        } else {
                            loadingBar.setVisibility(View.GONE);
                            TextDialogs dialog = new TextDialogs(getApplicationContext());
                            dialog.showLocationDialog("Nem sikerült csatlakozni a térkép szolgáltatáshoz!\nGPSben elmentett legutolsó koordináták:\nLongitude: " + location.getLongitude() + "\nLatitude: " + location.getLatitude() + "\nElfogadod a követekező koordinátákat?", location);
                        }
                        loadingBar.setVisibility(View.GONE);
                    }
                });
                task.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Nem sikerült a helymeghatározás", Toast.LENGTH_SHORT).show();
                        loadingBar.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    @Subscribe
    public void onNumberOfPictureEvent(NumberOfPictureChangeEvent event) {
        loadingBar.setVisibility(View.GONE);
        if (event.getWorkState().equals(WorkState.DOCUMENTATION.toString())) {
            presenter.setNumberOfPictures();
        }
        changeSubmitButton();
        setDocumentMark();
    }

    @Subscribe
    public void onLocationChangeEvent(LocationEvent event) {
        tLongitude.setText(String.format("Lon: %f", event.getLocation().getLongitude()));
        tlatitude.setText(String.format("Lat: %f", event.getLocation().getLatitude()));
        presenter.setLocation(event.getLocation().getLongitude(), event.getLocation().getLatitude());
        gpsSet = true;
        changeSubmitButton();
        setMarks(findViewById(R.id.iv_LOCATION_mark), true);
    }

    @Subscribe
    public void onRemoveDocumentEvent(RemovePictureEvent event) {
        presenter.removePicture(event.getPath());
        changeSubmitButton();
        setDocumentMark();
    }

    @Subscribe
    public void onAddOnCommentEvent(AddCommentEvent event) {
        changeSubmitButton();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        if (EventBus.getDefault().isRegistered(DocumentationImagesAdapter.class)) ;
        {
            EventBus.getDefault().unregister(DocumentationImagesAdapter.class);
        }
        super.onDestroy();
    }

    private void init(Bundle data){
        DocumentationTableHandler db = new DocumentationTableHandler(this);
        int id = db.getLastId() + 1;

        back = findViewById(R.id.b_BACK);
        submit = findViewById(R.id.b_SUBMIT);
        photo = findViewById(R.id.i_IMAGES);
        location = findViewById(R.id.i_LOCATION);
        tLongitude = findViewById(R.id.t_LONG);
        tlatitude = findViewById(R.id.t_LAT);
        description = findViewById(R.id.et_DESCRIPTION);
        documentView = findViewById(R.id.rcw_PICTURES);
        TextView title = findViewById(R.id.t_TITLE);
        loadingBar = findViewById(R.id.pr_loading_bar);
        browseDocumentation = findViewById(R.id.i_BROWSER);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        documentView.setLayoutManager(layoutManager);

        this.presenter = new DocumentationWorkflowPresenter(this,id,getExternalFilesDir("documentation").getAbsolutePath() + "/" + id, data.getString("type"));
        title.setText(data.getString("type"));
    }

    private void setMarks(ImageView markView, boolean type) {
        Drawable normalImage = type ? ResourcesCompat.getDrawable(getResources(), R.drawable.green_check_mark, null)
                : ResourcesCompat.getDrawable(getResources(), R.drawable.exclamation_mark_icon, null);
        markView.setImageDrawable(normalImage);
    }

    private void setDocumentMark() {
        if (presenter.getNumberOfPhotos() > 0) {
            setMarks(findViewById(R.id.iv_DOCUMENTATION_mark), true);
        } else {
            setMarks(findViewById(R.id.iv_DOCUMENTATION_mark), false);
        }
    }

    private boolean areAllPictureCommented() {
        if (documentView.getAdapter() != null) {
            DocumentationImagesAdapter adapter = (DocumentationImagesAdapter) documentView.getAdapter();
            return adapter.areAllPictureCommented();
        }
        return false;
    }

    @Override
    public Activity getActivity() {
        return this;
    }
}