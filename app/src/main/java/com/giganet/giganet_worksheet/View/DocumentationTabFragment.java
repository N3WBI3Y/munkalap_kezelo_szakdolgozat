package com.giganet.giganet_worksheet.View;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.giganet_worksheet.R;
import com.giganet.giganet_worksheet.Network.Documentation.DocumentationStatusDto;
import com.giganet.giganet_worksheet.Utils.SSOResult;
import com.giganet.giganet_worksheet.Network.Worksheet.RetrofitClientWorksheet;
import com.giganet.giganet_worksheet.Network.Worksheet.ServiceTypeDto;
import com.giganet.giganet_worksheet.Persistence.Documentation.DocumentationStatusTableHandler;
import com.giganet.giganet_worksheet.Persistence.Worksheet.InstallationServiceTypesTableHandler;
import com.giganet.giganet_worksheet.Resources.Adapters.DocumentationButtonsAdapter;
import com.giganet.giganet_worksheet.Resources.Events.SSOResponseEvent;
import com.giganet.giganet_worksheet.Utils.SSOService;
import com.giganet.giganet_worksheet.Utils.NetworkHelper;
import com.giganet.giganet_worksheet.Utils.SharedPreference;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class DocumentationTabFragment extends Fragment {

    private  RecyclerView documentationButtons;

    public DocumentationTabFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_documentation_tab, container, false);
        documentationButtons = root.findViewById(R.id.rw_documentationButtons);

        if (NetworkHelper.isNetworkAvailable(requireActivity())){
            syncTypesWithBackEnd();
        } else {
            setAdapter();
        }
        documentationButtons.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Disallow the touch request for parent scroll on touch of child view
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
        return root;
    }

    private void syncTypesWithBackEnd(){
        Executors.newCachedThreadPool().submit(() -> {
            SSOService.getToken(requireActivity(), new SSOResult() {
                @Override
                public void onSuccess(String token) {
                    Call<List<ServiceTypeDto>> call = RetrofitClientWorksheet.getInstance(requireActivity()).getApi().getServiceTypeActions(token);
                    call.enqueue(new Callback<List<ServiceTypeDto>>() {
                        @Override
                        public void onResponse(Call<List<ServiceTypeDto>> call, Response<List<ServiceTypeDto>> response) {
                            if (response.code() == 200 && response.body() != null) {
                                InstallationServiceTypesTableHandler tableHandler = new InstallationServiceTypesTableHandler(requireActivity());
                                tableHandler.clearDb();
                                List<ServiceTypeDto> serviceTypeEntities = response.body();
                                for (ServiceTypeDto entity : serviceTypeEntities) {
                                    String serviceJson = new Gson().toJson(entity.getActionList());
                                    tableHandler.addService(entity.getName(), serviceJson);
                                }
                                setAdapter();
                            }
                        }

                        @Override
                        public void onFailure(Call<List<ServiceTypeDto>> call, Throwable t) {
                            EventBus.getDefault().post(new SSOResponseEvent(false));
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(getActivity(), "Nem sikerült elérnem a szervert", Toast.LENGTH_LONG).show();
                                setAdapter();
                            });
                        }
                    });
                }

                @Override
                public void onFailure(String token) {
                    requireActivity().runOnUiThread(() -> {
                        Intent intent = new Intent(requireActivity(), AuthenticationActivity.class);
                        intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
                        requireActivity().startActivity(intent);
                        Toast.makeText(requireActivity(), "Nem megfelelő felhasználó név vagy jelszó", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });

    }

    private void setAdapter(){
        String username = new SharedPreference(requireActivity()).getSharedPreferences().getString("USERNAME","");

        DocumentationStatusTableHandler db = new DocumentationStatusTableHandler(getActivity());
        ArrayList<DocumentationStatusDto> types = db.getTaskTypes(username);
        types.add(new DocumentationStatusDto(100, "BEKÜLDÖTT KÉPEK", 100));
        requireActivity().runOnUiThread(() -> {
            LinearLayoutManager installationManger = new LinearLayoutManager(requireActivity());
            installationManger.setOrientation(LinearLayoutManager.VERTICAL);
            DocumentationButtonsAdapter adapter = new DocumentationButtonsAdapter(types, requireActivity());

            documentationButtons.setAdapter(adapter);
            documentationButtons.setLayoutManager(installationManger);
        });
    }
}