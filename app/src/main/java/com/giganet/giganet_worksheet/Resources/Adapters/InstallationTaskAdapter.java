package com.giganet.giganet_worksheet.Resources.Adapters;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;

import com.example.giganet_worksheet.R;
import com.giganet.giganet_worksheet.Persistence.Worksheet.Entities.InstallationTaskEntity;
import com.giganet.giganet_worksheet.Persistence.Worksheet.InstallationTaskTableHandler;
import com.giganet.giganet_worksheet.Resources.Constants.ServiceConstants;
import com.giganet.giganet_worksheet.Resources.Constants.SharedPreferences;
import com.giganet.giganet_worksheet.Resources.Enums.InstallationStatusType;
import com.giganet.giganet_worksheet.Resources.Services.TimerService;
import com.giganet.giganet_worksheet.View.InstallationWorkFlowActivity;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class InstallationTaskAdapter extends ArrayAdapter<InstallationTaskEntity> {
    private TextView name, address, phone, subject, serviceId, workId, status, timer, recentlyAdded;
    private ImageView mapButton, callButton;
    private AppCompatButton openTaskButton, favoriteButton;
    private CardView view;
    private final List<InstallationTaskEntity> tasks;
    private boolean enabled = true;
    private int alarmTimer;

    public InstallationTaskAdapter(Context context, List<InstallationTaskEntity> tasks) {
        super(context, 0, tasks);
        this.tasks = tasks;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        InstallationTaskEntity task = getItem(position);
        int index = tasks.indexOf(task);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.single_installation_work_item, parent, false);
        }
        view = convertView.findViewById(R.id.item_view);
        name = convertView.findViewById(R.id.item_name);
        address = convertView.findViewById(R.id.item_address);
        phone = convertView.findViewById(R.id.item_phone);
        subject = convertView.findViewById(R.id.item_subject);
        serviceId = convertView.findViewById(R.id.item_service_serial);
        workId = convertView.findViewById(R.id.item_work_serial);
        mapButton = convertView.findViewById(R.id.item_marker_icon);
        callButton = convertView.findViewById(R.id.item_phone_icon);
        status = convertView.findViewById(R.id.item_status);
        timer = convertView.findViewById(R.id.item_timer);
        openTaskButton = convertView.findViewById(R.id.b_open_task_button);
        favoriteButton = convertView.findViewById(R.id.tb_favorite_button);
        recentlyAdded = convertView.findViewById(R.id.t_recentlyAdded);

        name.setText(task.getClientName());
        address.setText(String.format("%s, %s %s", task.getCity(), task.getAddress(), (task.getPartialNumber()) != null ? (task.getPartialNumber().equals("N/A") ? "" : task.getPartialNumber()) : ""));
        phone.setText(task.getPhone());
        subject.setText(task.getOriginalSubject());
        serviceId.setText(task.getServiceId());
        status.setText(InstallationStatusType.statusToHungarian(task.getStatus().getStatus()));
        workId.setText(String.format("%d", task.getId()));
        android.content.SharedPreferences preferences = getContext().getSharedPreferences(SharedPreferences.SHAREDPREFERENCES, MODE_PRIVATE);
        alarmTimer = preferences.getInt(ServiceConstants.Options.ALARMTIME, ServiceConstants.Options.DEFAULT_ALARM_TIME);
        setTimerText(timer, tasks.get(index));
        if (tasks.get(index).getFavorite() != 0) {
            favoriteButton.setBackgroundDrawable(ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.favorite_icon_true, null));
        } else {
            favoriteButton.setBackgroundDrawable(ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.favorite_icon_false, null));
        }
        if (task.getRecentlyAdded() == 0) {
            recentlyAdded.setText("");
        } else {
            recentlyAdded.setText("ÚJ!");
        }

        address.setOnClickListener(v -> {
            if (!enabled) {
                return;
            }
            String destination = task.getCity() + " " + task.getAddress();
            destination = destination.replace(" ", "+");
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + destination);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            getContext().startActivity(mapIntent);
        });
        phone.setOnClickListener(v -> {
            if (!enabled) {
                return;
            }
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + task.getPhone()));
            getContext().startActivity(intent);
        });
        mapButton.setOnClickListener(v -> {
            if (!enabled) {
                return;
            }
            String destination = task.getCity() + " " + task.getAddress();
            destination = destination.replace(" ", "+");
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + destination);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            getContext().startActivity(mapIntent);
        });
        callButton.setOnClickListener(v -> {
            if (!enabled) {
                return;
            }
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + task.getPhone()));
            getContext().startActivity(intent);
        });
        setStatusColor(task.getStatus().getStatus());

        view.setOnClickListener(new OpenTaskOnClickListener(enabled, task, getContext(), alarmTimer));

        openTaskButton.setOnClickListener(new OpenTaskOnClickListener(enabled, task, getContext(), alarmTimer));

        favoriteButton.setOnClickListener(v -> {
            InstallationTaskTableHandler taskDB = new InstallationTaskTableHandler(getContext());
            taskDB.updateFavorite(tasks.get(index).getStatus().getOwner(), tasks.get(index).getId(), (tasks.get(index).getFavorite() == 0) ? 1 : 0);
            tasks.get(index).setFavorite(tasks.get(index).getFavorite() == 0 ? 1 : 0);
            tasks.sort(Comparator.comparing(InstallationTaskEntity::getFavorite, Comparator.reverseOrder()).thenComparing(InstallationTaskEntity::getId));
            notifyDataSetChanged();
        });


        return convertView;
    }

    public void updateStatus(int workId, String status, Activity activity){
        Optional<InstallationTaskEntity> taskEntity = tasks.stream()
                .filter(task -> task.getId() == workId)
                .findFirst();
        taskEntity.ifPresent(installationTaskEntity -> installationTaskEntity.getStatus().setStatus(status));
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    private void setTimerText(TextView timer, InstallationTaskEntity task) {
        android.content.SharedPreferences sharedPreferences = getContext().getSharedPreferences(SharedPreferences.SHAREDPREFERENCES, MODE_PRIVATE);
        InstallationTaskTableHandler db = new InstallationTaskTableHandler(getContext());
        long elapsedTime = db.getElapsedTime(sharedPreferences.getString("USERNAME", ""), task.getId());
        if ((task.getStatus().getStatus().equals(InstallationStatusType.BEGIN.toString())
                || task.getStatus().getStatus().equals(InstallationStatusType.STARTED.toString()))) {
            if (sharedPreferences.getInt("WORKID", -1) == task.getId()) {
                elapsedTime += (System.currentTimeMillis() - sharedPreferences.getLong("LASTUPDATE", 0)) / 60000;
                db.updateElapsedTime(sharedPreferences.getString("USERNAME", ""), task.getId(), elapsedTime);
                sharedPreferences.edit().putBoolean("NEEDUPDATE", false).commit();
            }
            String information = task.getClientName() + " " + task.getAddress();
            TimerService.getInstance().setTaskService(sharedPreferences.getString("USERNAME", ""), task.getId(), information,getContext());
        }
        elapsedTime = db.getElapsedTime(sharedPreferences.getString("USERNAME", ""), task.getId());
        formatElapsedTimeText(timer, elapsedTime);
    }

    private void formatElapsedTimeText(TextView timer, long elapsedTime) {
        int hour = (int) (elapsedTime / 60);
        int minute = (int) (elapsedTime % 60);
        timer.setText(String.format("%02d:%02d", hour, minute));
        if (elapsedTime > alarmTimer) {
            timer.setTextColor(Color.parseColor("#FF0000"));
        } else {
            timer.setTextColor(Color.parseColor("#FFFFFF"));
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setTimer(int workId, long elapsedTime,Activity activity) {
        Optional<InstallationTaskEntity> taskEntity = tasks.stream()
                .filter(task -> task.getId() == workId)
                .findFirst();
        taskEntity.ifPresent(installationTaskEntity -> installationTaskEntity.setElapsedTime(elapsedTime));
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
                if (elapsedTime > alarmTimer) {
                    timer.setTextColor(Color.parseColor("#FF0000"));
                } else {
                    timer.setTextColor(Color.parseColor("#FFFFFF"));
                }
            }
        });
    }


    private void setStatusColor(String status) {
        if (status.equals(InstallationStatusType.ISSUED.toString())) {
            this.status.setBackgroundColor(Color.parseColor("#4277cf"));

        } else if (status.equals(InstallationStatusType.BEGIN.toString())
                || status.equals(InstallationStatusType.STARTED.toString())) {
            this.status.setBackgroundColor(Color.parseColor("#e46828"));

        } else if (status.equals(InstallationStatusType.PAUSED.toString())) {
            this.status.setBackgroundColor(Color.parseColor("#b8b84f"));

        } else if (status.equals(InstallationStatusType.END.toString())) {
            this.status.setBackgroundColor(Color.parseColor("#5ba85b"));
        }
    }

    private static class OpenTaskOnClickListener implements View.OnClickListener {

        private final boolean enabled;
        private final InstallationTaskEntity task;
        private final Context context;
        private final int alarmTime;

        public OpenTaskOnClickListener(boolean enabled, InstallationTaskEntity task, Context context, int alarmTime) {
            this.enabled = enabled;
            this.task = task;
            this.context = context;
            this.alarmTime = alarmTime;
        }

        @Override
        public void onClick(View v) {
            if (!enabled) {
                return;
            }
            Intent intent = new Intent(context, InstallationWorkFlowActivity.class);
            intent.putExtra("id", String.valueOf(task.getId()));
            intent.putExtra("name", task.getClientName());
            intent.putExtra("city", task.getCity());
            intent.putExtra("address", task.getAddress());
            intent.putExtra("phone", task.getPhone());
            intent.putExtra("subject", task.getStatus().getSubject());
            intent.putExtra("serviceId", task.getServiceId());
            intent.putExtra("status", task.getStatus().getStatus());
            intent.putExtra("lot_number", task.getPartialNumber() != null ? (task.getPartialNumber().equals("N/A") ? "" : task.getPartialNumber()) : "");
            if (task.getLocation() != null) {
                intent.putExtra("longitude", task.getLocation().getLongitude());
                intent.putExtra("latitude", task.getLocation().getLatitude());
            }
            intent.putExtra("elapsedTime", task.getElapsedTime());
            intent.putExtra("alarmTime", alarmTime);
            intent.putExtra("serviceType", task.getServiceType().getName());
            context.startActivity(intent);
        }
    }
}
