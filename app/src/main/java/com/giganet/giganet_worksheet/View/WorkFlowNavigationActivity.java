package com.giganet.giganet_worksheet.View;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.work.WorkManager;

import com.example.giganet_worksheet.R;
import com.example.giganet_worksheet.databinding.ActivityWorkFlowNavigationBinding;
import com.giganet.giganet_worksheet.Utils.SSOResult;
import com.giganet.giganet_worksheet.Persistence.DataBaseHandler;
import com.giganet.giganet_worksheet.Resources.Constants.ServiceConstants;
import com.giganet.giganet_worksheet.Resources.Constants.SharedPreferences;
import com.giganet.giganet_worksheet.Resources.Events.SSOResponseEvent;
import com.giganet.giganet_worksheet.Resources.Services.LocationService;
import com.giganet.giganet_worksheet.Utils.SSOService;
import com.giganet.giganet_worksheet.Utils.ProgressHelper;
import com.giganet.giganet_worksheet.Utils.SharedPreference;
import com.google.android.material.navigation.NavigationView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class WorkFlowNavigationActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private TextView realNameText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init(){
        com.example.giganet_worksheet.databinding.ActivityWorkFlowNavigationBinding binding = ActivityWorkFlowNavigationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarWorkFlowNavigation.toolbar);
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.installation_tab, R.id.documentation_tab, R.id.options_tab)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_work_flow_navigation);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        TextView name = navigationView.getHeaderView(0).findViewById(R.id.t_header_name);
        realNameText = navigationView.getHeaderView(0).findViewById(R.id.t_header_real_name);
        android.content.SharedPreferences sharedPreferences = new SharedPreference(this).getSharedPreferences();
        name.setText(String.format("Felhasználó: %s", sharedPreferences.getString("USERNAME", "")));

        String realName = sharedPreferences.getString("userName", "");
        if (realName != null && realName.length() > 0) {
            realNameText.setText(String.format("Név: %s", realName));
        } else {
            realNameText.setText("Név : Offline");
        }

        navigationView.getMenu().findItem(R.id.b_logout).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                logOut();
                return true;
            }
        });
        String welcomeMsg = getIntent().getExtras().getString("WelcomeMsg");
        if (welcomeMsg != null){
            Toast.makeText(this,welcomeMsg,Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Subscribe
    public void onSSOResponseEvent(SSOResponseEvent event) {
        if (event.isSuccess()) {
            String realName = new SharedPreference(this).getSharedPreferences().getString("realName", "");
            realNameText.setText(String.format("Név: %s", realName));
        } else {
            realNameText.setText("Név : Offline");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_work_flow_navigation);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        Intent service = new Intent(this, LocationService.class);
        service.setAction(ServiceConstants.LocationService.ACTION_STOP_LOCATION_SERVICE);
        startService(service);
        DataBaseHandler.getInstance(this).closeDB();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }

    private void logOut() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(WorkFlowNavigationActivity.this);
        builder.setMessage("Biztosan ki akassz jelentkezni?")
                .setCancelable(false)
                .setPositiveButton("Igen", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        SSOService.getToken(getApplicationContext(), new SSOResult() {
                            @Override
                            public void onSuccess(String token) {
                                SSOService.logoutFromSSO(token, getApplicationContext());
                            }

                            @Override
                            public void onFailure(String token) {

                            }
                        });
                        ProgressHelper.stopInProgressTasks(WorkFlowNavigationActivity.this);
                        WorkManager.getInstance(getApplicationContext()).cancelUniqueWork("newTaskScanner");
                        WorkManager.getInstance(getApplicationContext()).cancelUniqueWork("worksheetSyncWithBackEnd");
                        cleanSharedPreference();
                        Intent intent = new Intent(WorkFlowNavigationActivity.this, AuthenticationActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Nem", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        builder.create().show();
    }

    private void cleanSharedPreference() {
        android.content.SharedPreferences sharedPreferences = WorkFlowNavigationActivity.this.getSharedPreferences(SharedPreferences.SHAREDPREFERENCES, MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("USERNAME");
        editor.remove("REMEMBERME");
        editor.remove("userToken");
        editor.apply();
    }
}