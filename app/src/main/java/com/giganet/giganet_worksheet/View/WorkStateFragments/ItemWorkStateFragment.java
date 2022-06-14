package com.giganet.giganet_worksheet.View.WorkStateFragments;

import static android.text.Html.FROM_HTML_MODE_LEGACY;

import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.giganet_worksheet.R;
import com.giganet.giganet_worksheet.Persistence.Worksheet.Entities.InstallationTransactionEntity;
import com.giganet.giganet_worksheet.Presenter.ItemWorkStatePresenter;
import com.giganet.giganet_worksheet.Resources.Adapters.WorksheetMaterialPreviewAdapter;
import com.giganet.giganet_worksheet.Resources.Enums.TransactionType;
import com.giganet.giganet_worksheet.Utils.CheckMarkSetter;
import com.giganet.giganet_worksheet.Utils.SharedPreference;
import com.giganet.giganet_worksheet.View.AddMaterialDialog;

import java.util.ArrayList;

public class ItemWorkStateFragment extends Fragment implements WorkStateFragmentInterface, ItemWorkStateContract.View
        , View.OnClickListener {

    private Button addButton, minusButton;
    private ItemWorkStateContract.Presenter presenter;
    private RecyclerView itemViews;
    private ImageView checkMark;
    private TextView title;
    private WorksheetMaterialPreviewAdapter adapter;
    private Boolean enabled;
    private int maxNumberOfItems;

    public ItemWorkStateFragment() {
    }

    public static ItemWorkStateFragment newInstance(int workId, String title
                                                    , String type, boolean must
                                                    , int minItem, int maxItem, String serviceType) {
        ItemWorkStateFragment fragment = new ItemWorkStateFragment();
        Bundle data = new Bundle();
        data.putInt("workId", workId);
        data.putString("title", title);
        data.putString("type", type);
        data.putBoolean("must", must);
        data.putInt("minNumItem", minItem);
        data.putInt("maxNumItem", maxItem);
        data.putString("serviceType",serviceType);
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
        View v = inflater.inflate(R.layout.fragment_item_work_state, container, false);
        if (getArguments() != null) {
            init(v, getArguments());
        } else {
            requireActivity().finish();
        }
        return v;
    }

    private void init(View v, Bundle data) {
        String username = new SharedPreference(requireActivity()).getSharedPreferences().getString("USERNAME", "");
        itemViews = v.findViewById(R.id.rcw_items);
        title = v.findViewById(R.id.t_title);
        checkMark = v.findViewById(R.id.iv_check_mark);
        addButton = v.findViewById(R.id.b_add_button);
        minusButton = v.findViewById(R.id.b_minus_button);
        int workId = data.getInt("workId");
        presenter = new ItemWorkStatePresenter(this, workId, data.getBoolean("must")
                , data.getInt("minNumItem"), data.getString("title"), username, data.getString("serviceType"));
        adapter = new WorksheetMaterialPreviewAdapter(null,requireActivity().getSupportFragmentManager(),presenter.getServiceType());
        LinearLayoutManager itemManager = new LinearLayoutManager(requireActivity());
        itemManager.setOrientation(LinearLayoutManager.VERTICAL);
        itemViews.setAdapter(adapter);
        itemViews.setLayoutManager(itemManager);
        adapter.setEnabled(true);
        maxNumberOfItems = data.getInt("maxNumItem");
        presenter.registForEvents();
        presenter.updateAdapter();
        setListeners();
        setFragmentResult();
    }

    private void setListeners(){
        addButton.setOnClickListener(this);
        minusButton.setOnClickListener(this);
    }

    private void setFragmentResult(){
        requireActivity().getSupportFragmentManager().setFragmentResultListener(TransactionType.ADD.type, this, (requestKey, result) -> {
            presenter.addItem(result.getString("material"),result.getInt("quantity",0),result.getString("serialNum"));
        });

        requireActivity().getSupportFragmentManager().setFragmentResultListener(TransactionType.MODIFY.type, this, (requestKey, result) -> {
            presenter.modifyItem(result.getString("material")
                    ,result.getInt("quantity",0),result.getString("serialNum"), result.getInt("transactionId",0));
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.unregistForEvents();
    }

    @Override
    public boolean isSet() {
        return presenter.isSet();
    }

    @Override
    public boolean isMust() {
        return presenter.isMust();
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
            return presenter.isSet() ? "" : "<p style=\"color:red\">Még nem végezted el a " + presenter.getTitle() + " munkafolyamatot\n</p>";
        }
        return presenter.isSet() ? "" : "<p>Még nem végezted el a " + presenter.getTitle() + " munkafolyamatot\n</p>";
    }



    @Override
    public void onClick(View view) {
        if (enabled) {
            if (adapter.getItemCount() >= maxNumberOfItems) {
                Toast.makeText(requireActivity(), "Maximum " + maxNumberOfItems + " eszköz kapcsolható a feladathoz", Toast.LENGTH_SHORT).show();
                return;
            }
            if (view.getId() == R.id.b_add_button){
                 new AddMaterialDialog("Anyag visszavétele",null,null,null,null, false,presenter.getServiceType()).show(getChildFragmentManager(),"AddMaterial");
            }
            else if (view.getId() == R.id.b_minus_button) {
                new AddMaterialDialog("Anyag felhasználása",null,null,null,null, true,presenter.getServiceType()).show(getChildFragmentManager(),"AddMaterial");
            }
        } else {
            Toast.makeText(requireActivity(), "Indítsd el a munkafolyamatot", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void setAdapter(ArrayList<InstallationTransactionEntity> items) {
        adapter.update(items);
        itemViews.setAdapter(adapter);
        CheckMarkSetter.setCheckMark(checkMark, presenter.isSet(), presenter.isMust(), requireActivity());
        title.setText(Html.fromHtml(presenter.getTitle() + " <sup><small> (" + adapter.getItemCount() + ") </small></sup>", FROM_HTML_MODE_LEGACY));
    }

}