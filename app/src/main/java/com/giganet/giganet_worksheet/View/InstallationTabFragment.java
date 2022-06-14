package com.giganet.giganet_worksheet.View;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;

import static com.giganet.giganet_worksheet.Resources.Constants.SharedPreferences.*;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.giganet_worksheet.R;
import com.giganet.giganet_worksheet.Utils.SSOResult;
import com.giganet.giganet_worksheet.Network.Worksheet.RetrofitClientWorksheet;
import com.giganet.giganet_worksheet.Persistence.Worksheet.Entities.InstallationItemEntity;
import com.giganet.giganet_worksheet.Persistence.Worksheet.Entities.InstallationTaskEntity;
import com.giganet.giganet_worksheet.Persistence.Worksheet.InstallationItemTableHandler;
import com.giganet.giganet_worksheet.Persistence.Worksheet.InstallationTaskTableHandler;
import com.giganet.giganet_worksheet.Resources.Adapters.InstallationTaskAdapter;
import com.giganet.giganet_worksheet.Resources.Constants.ServiceConstants;
import com.giganet.giganet_worksheet.Resources.Enums.InstallationStatusType;
import com.giganet.giganet_worksheet.Resources.Events.DocumentStatusUpdateEvent;
import com.giganet.giganet_worksheet.Resources.Events.NewStatusEvent;
import com.giganet.giganet_worksheet.Resources.Events.SSOResponseEvent;
import com.giganet.giganet_worksheet.Resources.Events.StatusStatusUpdateEvent;
import com.giganet.giganet_worksheet.Resources.Events.TimerTickEvent;
import com.giganet.giganet_worksheet.Utils.SSOService;
import com.giganet.giganet_worksheet.Resources.Services.SaveToWorkSheetBackEndWorker;
import com.giganet.giganet_worksheet.Utils.WorksheetBackEndStatusSyncer;
import com.giganet.giganet_worksheet.Utils.NetworkHelper;
import com.giganet.giganet_worksheet.Utils.ProgressHelper;
import com.giganet.giganet_worksheet.Utils.SharedPreference;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InstallationTabFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private ListView tasksListView;
    private SwipeRefreshLayout refreshLayout;
    private Spinner dropdownStatus;
    private AutoCompleteTextView search;
    private List<InstallationTaskEntity> allTasks;
    private ProgressBar documentUpdate, statusUpdate;
    private RelativeLayout documentLayout, statusLayout;
    private TextView documentUpdateText, statusUpdateText;


    public InstallationTabFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_installation_tab, container, false);
        init(root);

        return root;
    }

    private void init(View root){
        tasksListView = root.findViewById(R.id.lv_tasks);
        refreshLayout = root.findViewById(R.id.swipeRefreshLayout);
        dropdownStatus = root.findViewById(R.id.statuses);
        documentUpdate = root.findViewById(R.id.pb_DocumentUpdateStatus);
        statusUpdate = root.findViewById(R.id.pb_StatusUpdateStatus);
        documentLayout = root.findViewById(R.id.document_update_layout);
        statusLayout = root.findViewById(R.id.status_update_layout);
        statusUpdateText = root.findViewById(R.id.t_StatusUpdateStatus);
        documentUpdateText = root.findViewById(R.id.t_DocumentUpdateStatus);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                testConnection();
            }
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(), R.array.statuses, R.layout.simple_spinner_item_white);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdownStatus.setAdapter(adapter);
        dropdownStatus.setOnItemSelectedListener(this);


        search = root.findViewById(R.id.autocompleteText_Search);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilter(allTasks);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        testConnection();
    }

    @Override
    public void onPause() {
        super.onPause();
        documentLayout.setVisibility(View.GONE);
        statusLayout.setVisibility(View.GONE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void testConnection() {
        if (!NetworkHelper.isNetworkAvailable(requireActivity())) {
            Toast.makeText(requireContext(), "Mobiladat és/vagy wifi kikapcsolva", Toast.LENGTH_SHORT).show();
            loadOfflineData();
            EventBus.getDefault().post(new SSOResponseEvent(false));

        } else {
            if (ProgressHelper.isUploadingToBackEnd(requireContext())) {
                documentLayout.setVisibility(View.VISIBLE);
            } else {
                documentLayout.setVisibility(View.GONE);
            }
            getInstallationWork();
        }
    }


    private void getInstallationWork() {
        if (statusLayout.getVisibility() == View.VISIBLE || documentLayout.getVisibility() == View.VISIBLE) {
            return;
        }
        Executors.newCachedThreadPool().submit(() -> {
            SSOService.getToken(requireActivity(), new SSOResult() {
                @Override
                public void onSuccess(String token) {
                    SharedPreferences sharedPreferences = new SharedPreference(requireActivity()).getSharedPreferences();
                    String username = sharedPreferences.getString("USERNAME", "");
                    Call<List<InstallationTaskEntity>> call = RetrofitClientWorksheet.getInstance(requireActivity()).getApi().getAllTasks(token);
                    call.enqueue(new Callback<List<InstallationTaskEntity>>() {
                        @Override
                        public void onResponse(Call<List<InstallationTaskEntity>> call, Response<List<InstallationTaskEntity>> response) {
                            List<InstallationTaskEntity> tasks = response.body();
                            if (tasks != null) {
                                Executors.newFixedThreadPool(1).submit(InstallationTabFragment.this::uploadDocuments);
                                Executors.newFixedThreadPool(1).submit(() -> {
                                    updateStatusDb(tasks, username);
                                    requireActivity().runOnUiThread(() -> {
                                        loadOfflineData();
                                        refreshLayout.setRefreshing(false);
                                    });
                                });

                            } else {
                                onFailure(call, new Throwable("Üres task"));
                            }
                        }

                        @Override
                        public void onFailure(Call<List<InstallationTaskEntity>> call, Throwable t) {
                            if (getActivity() != null) {
                                EventBus.getDefault().post(new SSOResponseEvent(false));
                                loadOfflineData();
                                requireActivity().runOnUiThread(() -> {
                                    Toast.makeText(getActivity(), "Nem sikerült elérnem a szervert", Toast.LENGTH_LONG).show();
                                });
                            }
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

    private void loadOfflineData() {
        Executors.newCachedThreadPool().submit(() -> {
            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(SHAREDPREFERENCES, Context.MODE_PRIVATE);
            String userName = sharedPreferences.getString("USERNAME", "");

            InstallationTaskTableHandler db = new InstallationTaskTableHandler(getContext());

            allTasks = db.getAllTask(userName);
            requireActivity().runOnUiThread(() -> {
                applyFilter(allTasks);
                refreshLayout.setRefreshing(false);
            });
        });
    }


    private void updateStatusDb(List<InstallationTaskEntity> tasks, String userName) {
        InstallationTaskTableHandler db = new InstallationTaskTableHandler(getContext());
        ArrayList<InstallationTaskEntity> currentTasks = db.getAllTask(userName);
        for (InstallationTaskEntity item : tasks) {
            //Ha nincs a helyi eszközön a feladat
            if (!currentTasks.contains(item)) {
                db.addTask(userName, item.getId(), item.getServiceId(), item.getClientName(), item.getCity()
                        , item.getAddress(), item.getPhone(), item.getLocation(), item.getPartialNumber()
                        , item.getStatus().getCreatedTime(), item.getStatus().getCreatedBy()
                        , item.getStatus().getSubject(), item.getStatus().getStatus(), item.getStatus().getOwner(), item.getVersion(), item.getServiceType().getName());
            }
            //Ha rajta van az eszközön a feladat
            else {
                int index = currentTasks.indexOf(item);
                SimpleDateFormat formatter = new SimpleDateFormat(ServiceConstants.DateFormat.DATE_FORMAT, Locale.getDefault());

                try {
                    Date date = formatter.parse(currentTasks.get(index).getStatus().getCreatedTime());
                    long passedTime = new Date().getTime() - (date != null ? date.getTime() : 0);
                    if (passedTime > 10800000) {
                        db.setRecentlyAddedFalse(userName, item.getId());
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if (currentTasks.get(index).getVersion() != item.getVersion()) {
                    //TODO ha verzió különbség alakul ki
                    WorksheetBackEndStatusSyncer.syncWithBackEnd(item.getStatus().getOwner(), item.getId(), InstallationStatusType.stringToInstallationStatusType(currentTasks.get(index).getStatus().getStatus())
                            , InstallationStatusType.stringToInstallationStatusType(item.getStatus().getStatus()), requireActivity());
                    InstallationTaskTableHandler taskTableHandler = new InstallationTaskTableHandler(requireActivity());
                    taskTableHandler.setVersion(item.getStatus().getOwner(), item.getId(), item.getVersion());
                }
            }
        }
        for (InstallationTaskEntity item : currentTasks) {
            //Van olyan feladat a helyi eszközön ami nincs a backenden vagy lezárt
            if (item.getStatus().getStatus().equals("CLOSED")) {
                db.deleteTask(item.getStatus().getOwner(), item.getId());
            }
            if (!tasks.contains(item)) {
                InstallationItemTableHandler itemDBHandler = new InstallationItemTableHandler(requireContext());
                ArrayList<InstallationItemEntity> images = itemDBHandler.getWorkItems(item.getId(), userName);
                if (images.size() > 0) {
                    //https://www.baeldung.com/java-string-to-date
                    SimpleDateFormat formatter = new SimpleDateFormat(ServiceConstants.DateFormat.DATE_FORMAT);
                    try {
                        Date date = formatter.parse(images.get(images.size() - 1).getDate());
                        long passedTime = new Date().getTime() - (date != null ? date.getTime() : 0);
                        if (passedTime <= 43200000) {
                            db.setTaskStatus(userName, item.getId(), InstallationStatusType.CREATED.name());
                        } else {
                            db.deleteTask(item.getStatus().getOwner(), item.getId());
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else {
                    db.deleteTask(item.getStatus().getOwner(), item.getId());
                }
            }
        }
    }


    private void uploadDocuments() {
        SharedPreferences sharedPreferences = new SharedPreference(requireActivity()).getSharedPreferences();
        Data inputData = new Data.Builder()
                .putString("userName", sharedPreferences.getString("USERNAME", ""))
                .build();


        OneTimeWorkRequest uploadWork = new OneTimeWorkRequest.Builder(SaveToWorkSheetBackEndWorker.class)
                .setInputData(inputData).setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()).build();
        WorkManager.getInstance(requireActivity().getApplicationContext()).enqueueUniqueWork("SaveToBackEndInitializer", ExistingWorkPolicy.APPEND_OR_REPLACE, uploadWork);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (tasksListView.getAdapter() != null) {
            applyFilter(allTasks);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    @Subscribe
    public void onNewStatusEvent(NewStatusEvent event) {
        Optional<InstallationTaskEntity> taskEntity = allTasks.stream()
                .filter(task -> task.getId() == event.getWorkId())
                .findFirst();
        taskEntity.ifPresent(installationTaskEntity -> installationTaskEntity.getStatus().setStatus(event.getStatus()));

        InstallationTaskAdapter adapter = (InstallationTaskAdapter) tasksListView.getAdapter();
        if (adapter != null){
            adapter.updateStatus(event.getWorkId(),event.getStatus(),requireActivity());
        }
    }


    @Subscribe
    public void onTimerTickEvent(TimerTickEvent event) {
        Optional<InstallationTaskEntity> taskEntity = allTasks.stream()
                .filter(task -> task.getId() == event.getWorkId())
                .findFirst();
        taskEntity.ifPresent(installationTaskEntity -> installationTaskEntity.setElapsedTime(event.getElapsed_time()));

        if (tasksListView.getAdapter() != null) {
            InstallationTaskAdapter adapter = (InstallationTaskAdapter) tasksListView.getAdapter();
            adapter.setTimer(event.getWorkId(), event.getElapsed_time(),requireActivity());
        }
    }

    @Subscribe
    public void onDocumentStatusUpdate(DocumentStatusUpdateEvent event) {
        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (event.getProgress() > 0) {
                    documentUpdate.setProgressDrawable(getResources().getDrawable(R.drawable.progress_bar_style, null));
                    documentUpdate.setProgress(event.getProgress());
                    documentUpdateText.setText(event.getMsg());
                    refreshLayout.setEnabled(false);
                    documentLayout.setVisibility(View.VISIBLE);

                }
                if (event.getProgress() == -1) {
                    documentUpdate.setProgressDrawable(getResources().getDrawable(R.drawable.progress_bar_failure, null));
                    refreshLayout.setEnabled(false);
                    documentUpdate.setProgress(100);
                    documentUpdateText.setText(event.getMsg());
                    documentLayout.setVisibility(View.VISIBLE);

                    ValueAnimator animator = ValueAnimator.ofInt(100, documentUpdate.getMax());
                    animator.setDuration(3000);
                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            documentUpdate.setProgress((Integer) animation.getAnimatedValue());
                        }
                    });
                    animator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            // start your activity here
                            documentLayout.setVisibility(View.GONE);
                        }
                    });
                    animator.start();

                }
                documentUpdate.setProgress(event.getProgress());
                if (event.getProgress() == 100) {
                    documentUpdate.setProgressDrawable(getResources().getDrawable(R.drawable.progress_bar_style, null));
                    documentUpdateText.setText(event.getMsg());
                    refreshLayout.setEnabled(true);

                    ValueAnimator animator = ValueAnimator.ofInt(100, documentUpdate.getMax());
                    animator.setDuration(3000);
                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            documentUpdate.setProgress((Integer) animation.getAnimatedValue());
                        }
                    });
                    animator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            // start your activity here
                            documentLayout.setVisibility(View.GONE);
                        }
                    });
                    animator.start();
                }
            }
        });
    }

    @Subscribe
    public void onStatusStatusUpdate(StatusStatusUpdateEvent event) {
        requireActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (event.getProgress() > 0) {
                    statusUpdateText.setTextColor(Color.parseColor("#FFFFFF"));
                    statusUpdateText.setText("Státusz feltöltés!");
                    refreshLayout.setEnabled(false);
                    statusLayout.setVisibility(View.VISIBLE);
                }
                if (event.getProgress() == -1) {
                    statusUpdateText.setText("Sikertelen feltöltés!");
                    statusUpdateText.setTextColor(Color.parseColor("#FF0000"));
                    statusLayout.setVisibility(View.VISIBLE);

                    ValueAnimator animator = ValueAnimator.ofInt(100, statusUpdate.getMax());
                    animator.setDuration(1500);
                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            statusUpdate.setProgress((Integer) animation.getAnimatedValue());
                        }
                    });
                    animator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            // start your activity here
                            statusLayout.setVisibility(View.GONE);
                        }
                    });
                    animator.start();

                }
                statusUpdate.setProgress(event.getProgress());
                if (event.getProgress() == 100) {
                    statusUpdateText.setTextColor(Color.parseColor("#FFFFFF"));
                    statusUpdateText.setText("Sikeres feltöltés!");
                    refreshLayout.setEnabled(true);
                    statusLayout.setVisibility(View.VISIBLE);


                    ValueAnimator animator = ValueAnimator.ofInt(100, statusUpdate.getMax());
                    animator.setDuration(1500);
                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            statusUpdate.setProgress((Integer) animation.getAnimatedValue());
                        }
                    });
                    animator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            // start your activity here
                            statusLayout.setVisibility(View.GONE);
                        }
                    });
                    animator.start();
                }
            }
        });
    }


    private void applyFilter(final List<InstallationTaskEntity> tasks) {

        Executors.newCachedThreadPool().submit(() -> {
            String[] currentStatus = currentStatusFilter(dropdownStatus.getSelectedItem().toString());
            String charFilter = search.getText().toString().toLowerCase(Locale.ROOT);

            if (allTasks == null || allTasks.size() == 0) {
                return;
            }

            List<InstallationTaskEntity> filterdTasks = tasks;

            if (dropdownStatus.getSelectedItem().toString().equals("ÚJ")) {
                filterdTasks = tasks.stream().filter((InstallationTaskEntity task) -> {
                    return task.getRecentlyAdded() == 1;
                }).collect(Collectors.toList());
            }

            filterdTasks = filterdTasks.stream().filter((InstallationTaskEntity task) -> {
                return Arrays.stream(currentStatus).anyMatch(status ->
                        task.getStatus().getStatus().equals(status));

            }).collect(Collectors.toList());
            if (charFilter.length() != 0) {
                filterdTasks = filterdTasks.stream().filter((InstallationTaskEntity task) -> {
                    return (task.getClientName() != null ? task.getClientName().toLowerCase(Locale.ROOT).contains(charFilter) : false) || (task.getServiceId() != null ? task.getServiceId().toLowerCase(Locale.ROOT).contains(charFilter) : false)
                            || (task.getCity() != null ? task.getCity().toLowerCase(Locale.ROOT).contains(charFilter) : false) || (task.getAddress() != null ? task.getAddress().toLowerCase(Locale.ROOT).contains(charFilter) : false)
                            || String.valueOf(task.getId()).equals(charFilter);

                }).collect(Collectors.toList());
            }
            List<InstallationTaskEntity> finalFilterdTasks = filterdTasks;
            requireActivity().runOnUiThread(() -> setAdapter(tasksListView, finalFilterdTasks));
        });


    }

    private String[] currentStatusFilter(String selected) {
        switch (selected) {
            case "NYITOTT":
                return new String[]{"ISSUED", "MODIFIED", "BEGIN", "PAUSED", "STARTED"};
            case "BEFEJEZVE":
                return new String[]{"END"};
            case "KIADVA":
                return new String[]{"ISSUED"};
            case "MÓDOSÍTVA":
                return new String[]{"MODIFIED"};
            case "ELKEZDETT":
                return new String[]{"BEGIN"};
            case "FOLYAMATBAN":
                return new String[]{"STARTED"};
            case "SZÜNETELTETVE":
                return new String[]{"PAUSED"};
            default:
                return new String[]{"ISSUED", "MODIFIED", "BEGIN", "PAUSED", "STARTED", "END"};
        }
    }

    private void setAdapter(ListView documentation, List<InstallationTaskEntity> tasks) {
        tasks.sort(Comparator.comparing(InstallationTaskEntity::getFavorite, Comparator.reverseOrder()).thenComparing(InstallationTaskEntity::getId));
        if (documentation.getAdapter() == null) {
            InstallationTaskAdapter adapter = new InstallationTaskAdapter(getContext(), tasks);
            adapter.setEnabled(true);
            documentation.setAdapter(adapter);
        } else {
            InstallationTaskAdapter adapter = (InstallationTaskAdapter) documentation.getAdapter();
            adapter.clear();
            adapter.addAll(tasks);
            adapter.setEnabled(true);
        }
    }
}