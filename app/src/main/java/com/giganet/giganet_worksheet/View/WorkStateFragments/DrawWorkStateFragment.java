package com.giganet.giganet_worksheet.View.WorkStateFragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.example.giganet_worksheet.R;
import com.giganet.giganet_worksheet.Presenter.DrawWorkStatePresenter;
import com.giganet.giganet_worksheet.Utils.CheckMarkSetter;
import com.giganet.giganet_worksheet.Utils.SharedPreference;
import com.giganet.giganet_worksheet.View.DrawActivity;

public class DrawWorkStateFragment extends Fragment implements DrawWorkStateContract.View, View.OnClickListener
        , WorkStateFragmentInterface {
    private TextView title;
    private ImageView checkMark;
    private ImageButton drawButton;
    private DrawWorkStateContract.Presenter presenter;
    private int drawIconId;
    private boolean enabled;
    private ActivityResultLauncher<Intent> imagePathResultLauncher;

    public DrawWorkStateFragment() {
    }

    public static DrawWorkStateFragment newInstance(int workId, String title, String type, int icon, boolean must) {
        DrawWorkStateFragment fragment = new DrawWorkStateFragment();
        Bundle data = new Bundle();
        data.putInt("workId", workId);
        data.putString("title", title);
        data.putString("type", type);
        data.putInt("icon", icon);
        data.putBoolean("must", must);
        fragment.setArguments(data);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_draw_work_state, container, false);
        Bundle data = getArguments();
        if (data == null) {
            requireActivity().finish();
            return v;
        }
        init(v, data);

        return v;
    }

    private void init(View v, Bundle data) {
        String username = new SharedPreference(requireActivity()).getSharedPreferences().getString("USERNAME", "");
        title = v.findViewById(R.id.t_title);
        checkMark = v.findViewById(R.id.iv_check_mark);
        drawButton = v.findViewById(R.id.ib_draw_button);
        drawIconId = data.getInt("icon");
        presenter = new DrawWorkStatePresenter(this, data.getInt("workId")
                , data.getBoolean("must"), data.getString("title"), username);
        title.setText(presenter.getTitle());
        presenter.setPicture();
        setOnClickListeners();
        registerImagePathResultLauncher();
    }

    private void registerImagePathResultLauncher() {
        imagePathResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        presenter.savePicture(data.getStringExtra("filePath"));
                        presenter.setPicture();
                    }
                }
            }
        });
    }

    private void setOnClickListeners() {
        drawButton.setOnClickListener(this);
    }

    @Override
    public boolean isMust() {
        return presenter.isMust();
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String onFinishString() {
        if (presenter.isMust()) {
            return presenter.isSet() ? "" : "<p style=\"color:red\">Még nem végezted el a " + presenter.getTitle() + " munkafolyamatot\n</p>";
        }
        return presenter.isSet() ? "" : "<p>Még nem végezted el a " + presenter.getTitle() + " munkafolyamatot\n</p>";
    }


    @Override
    public boolean isSet() {
        return presenter.isSet();
    }

    @Override
    public void setCheckMark() {
        CheckMarkSetter.setCheckMark(checkMark, presenter.isSet(), presenter.isMust(), requireActivity());
    }

    @Override
    public void setPicture(String photoPath) {
        if (photoPath.length() == 0) {
            drawButton.setBackground(getResources().getDrawable(drawIconId, null));
        } else {
            Bitmap image = BitmapFactory.decodeFile(photoPath);
            Drawable sign_draw = new BitmapDrawable(getResources(), image);
            drawButton.setImageDrawable(sign_draw);
            drawButton.setBackground(null);
        }
        setCheckMark();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ib_draw_button && enabled) {
            Intent paintIntent = new Intent(getActivity(), DrawActivity.class);
            paintIntent.putExtra("workId", presenter.getWorkId());
            paintIntent.putExtra("type", presenter.getTitle());
            paintIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            imagePathResultLauncher.launch(paintIntent);
        } else {
            Toast.makeText(requireActivity(), "Indítsd el a munkafolyamatot", Toast.LENGTH_SHORT).show();
        }
    }
}