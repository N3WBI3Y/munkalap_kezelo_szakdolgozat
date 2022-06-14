package com.giganet.giganet_worksheet.View.WorkStateFragments;

import static android.text.Html.FROM_HTML_MODE_LEGACY;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.giganet_worksheet.R;
import com.giganet.giganet_worksheet.Presenter.PhotoWorkStatePresenter;
import com.giganet.giganet_worksheet.Resources.Adapters.WorksheetImagePreviewAdapter;
import com.giganet.giganet_worksheet.Resources.Services.LocationService;
import com.giganet.giganet_worksheet.Utils.CheckMarkSetter;
import com.giganet.giganet_worksheet.View.CameraViewActivity;
import com.giganet.giganet_worksheet.View.SerialnumberVerificationActivity;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;


public class PhotoWorkStateFragment extends Fragment implements PhotoWorkStateContract.View, View.OnClickListener
        , WorkStateFragmentInterface {

    private PhotoWorkStateContract.Presenter presenter;
    private RecyclerView imagesView;
    private Button photoButton;
    private ImageView checkMark;
    private Button folderButton;
    private LocationService locationService;
    private TextView title;
    private ActivityResultLauncher<Intent> onSelectedFileResultLauncher;
    private ActivityResultLauncher<Intent> onSerialNumberScanResultLauncher;
    private WorksheetImagePreviewAdapter adapter;
    private String titleString;
    private Boolean enabled;
    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            LocationService.LocalBinder binder = (LocationService.LocalBinder) service;
            locationService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };


    public PhotoWorkStateFragment() {
    }

    public synchronized static PhotoWorkStateFragment newInstance(int workId, boolean mustSet
            , String type, int minPictureNumber, int maxPictureNumber
            , boolean needSerialNumberScan, String serviceId, String title) {
        Bundle data = new Bundle();
        data.putInt("workId", workId);
        data.putInt("minPictureNumber", minPictureNumber);
        data.putInt("maxPictureNumber", maxPictureNumber);
        data.putString("type", type);
        data.putBoolean("mustSet", mustSet);
        data.putBoolean("needSerialNumberScan", needSerialNumberScan);
        data.putString("serviceId", serviceId);
        data.putString("title", title);
        PhotoWorkStateFragment fragment = new PhotoWorkStateFragment();
        fragment.setArguments(data);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_photo_work_state, container, false);
        init(v, getArguments());
        presenter.registForEvents();

        return v;
    }

    @Override
    public void onDestroy() {
        presenter.unregistForEvents();
        super.onDestroy();
    }

    private void init(View v, Bundle data) {
        imagesView = v.findViewById(R.id.rcw_images);
        title = v.findViewById(R.id.t_title);
        titleString = data.getString("title");
        adapter = new WorksheetImagePreviewAdapter(null, requireActivity(), titleString);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireActivity());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        imagesView.setAdapter(adapter);
        imagesView.setLayoutManager(layoutManager);
        adapter.setEnabled(true);
        photoButton = v.findViewById(R.id.b_photo);
        folderButton = v.findViewById(R.id.b_folder);
        checkMark = v.findViewById(R.id.iv_check_mark);
        bindService();
        if (data != null) {
            title.setText(data.getString("title"));
            presenter = new PhotoWorkStatePresenter(this, data.getInt("workId"), data.getBoolean("mustSet")
                    , requireActivity().getExternalFilesDir(String.valueOf(data.getInt("workId"))).getAbsolutePath()
                    , data.getInt("minPictureNumber"), data.getInt("maxPictureNumber")
                    , titleString, data.getBoolean("needSerialNumberScan"), data.getString("serviceId"));
            presenter.updateAdapter();
        } else {
            requireActivity().finish();
        }
        setOnClickListeners();
        registerFileSelectedResultLauncher();
        registerSerialNumberScanResultLauncher();
    }

    private void setOnClickListeners() {
        photoButton.setOnClickListener(this);
        folderButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (enabled) {
            if (v.getId() == R.id.b_photo) {
                if (presenter.isNeedSerialNumberScan() && !presenter.isSerialNumberScanned()) {
                    Intent ScanIntent = new Intent(getActivity(), SerialnumberVerificationActivity.class);
                    ScanIntent.putExtra("serialNumber", presenter.getServiceId());
                    onSerialNumberScanResultLauncher.launch(ScanIntent);
                    return;
                }
             takePhoto();

            } else if (v.getId() == R.id.b_folder) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                onSelectedFileResultLauncher.launch(Intent.createChooser(intent, titleString + "-ról készített kép hozzáadása"));
            }
        } else {
            Toast.makeText(requireActivity(), "Indítsd el a munkafolyamatot", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public boolean isMust() {
        return presenter.isMust();
    }


    @Override
    public void setAdapter(ArrayList<String> pictures) {
        adapter.update(pictures);
        imagesView.setAdapter(adapter);
        CheckMarkSetter.setCheckMark(checkMark, presenter.isSet(), presenter.isMust(), requireActivity());
        title.setText(Html.fromHtml(titleString + " <sup><small> (" + adapter.getItemCount() + ") </small></sup>", FROM_HTML_MODE_LEGACY));
    }

    @Override
    public boolean isSet() {
        return presenter.isSet();
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (adapter != null) {
            adapter.setEnabled(enabled);
        }
    }

    @Override
    public String onFinishString() {
        if (presenter.isMust()) {
            return presenter.isSet() ? "" : "<p style=\"color:red\">Még nem készítettél képet a " + titleString + " munkafolyamathoz\n</p>";
        }
        return presenter.isSet() ? "" : "<p>Még nem készítettél képet a " + titleString + " munkafolyamathoz\n</p>";
    }

    private void takePhoto(){
        Intent intent = new Intent(requireActivity(), CameraViewActivity.class);
        intent.putExtra("filePath", presenter.getPhotoPath());
        intent.putExtra("workState", presenter.getType());
        intent.putExtra("workId", presenter.getId());
        intent.putExtra("maxNumberOfPicture", presenter.getMaxNumberOfPicture());
        requireActivity().startActivity(intent);
    }

    private void registerFileSelectedResultLauncher() {
        onSelectedFileResultLauncher = registerForActivityResult(
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
                                        presenter.copySelectedFiles(data.getClipData(), location, requireActivity());
                                    } else if (data.getData() != null) {
                                        presenter.copySelectedFiles(data.getData(), location, requireActivity());
                                    }
                                }
                            });
                        }
                    }
                });
    }

    private void registerSerialNumberScanResultLauncher() {
        onSerialNumberScanResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null) {
                                boolean value = data.getBooleanExtra("parity", true);
                                presenter.setSerialNumberScanned(value);
                                photoButton.performClick();
                            }
                        }
                    }
                });
    }

    private void bindService() {
        Intent bindIntent = new Intent(requireActivity(), LocationService.class);
        requireActivity().bindService(bindIntent, connection, Context.BIND_AUTO_CREATE);
    }

}