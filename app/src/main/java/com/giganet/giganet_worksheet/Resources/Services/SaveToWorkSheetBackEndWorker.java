package com.giganet.giganet_worksheet.Resources.Services;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static com.giganet.giganet_worksheet.Resources.Constants.ServiceConstants.WorksheetInstallation.WORKSHEETNOTIFICATIONID;
import static com.giganet.giganet_worksheet.Resources.Constants.ServiceConstants.WorksheetInstallation.WORKSHEETNOTIFICATIONNAME;
import static com.giganet.giganet_worksheet.Resources.Constants.ServiceConstants.WorksheetInstallation.WORKSHEETNOTIFYCHANNELNAME;
import static com.giganet.giganet_worksheet.Resources.Enums.WorkState.GPS_LOCATION;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.ForegroundInfo;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.giganet.giganet_worksheet.Utils.SSOResult;
import com.giganet.giganet_worksheet.Network.Worksheet.EncodedWorksheetImageDto;
import com.giganet.giganet_worksheet.Network.Worksheet.RetrofitClientWorksheet;
import com.giganet.giganet_worksheet.Network.Worksheet.TransactionDto;
import com.giganet.giganet_worksheet.Persistence.Worksheet.Entities.InstallationItemEntity;
import com.giganet.giganet_worksheet.Persistence.Worksheet.Entities.InstallationTaskEntity;
import com.giganet.giganet_worksheet.Persistence.Worksheet.Entities.InstallationTransactionEntity;
import com.giganet.giganet_worksheet.Persistence.Worksheet.InstallationItemTableHandler;
import com.giganet.giganet_worksheet.Persistence.Worksheet.InstallationTransactionTableHandler;
import com.giganet.giganet_worksheet.Resources.Enums.DBStatus;
import com.giganet.giganet_worksheet.Resources.Events.DocumentStatusUpdateEvent;
import com.giganet.giganet_worksheet.Utils.RunningProcessHelper;
import com.giganet.giganet_worksheet.Utils.SSOService;
import com.giganet.giganet_worksheet.View.AuthenticationActivity;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import retrofit2.Response;

public class SaveToWorkSheetBackEndWorker extends Worker {

    private final String username;
    private NotificationCompat.Builder notificationBuilder;


    public SaveToWorkSheetBackEndWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        username = getInputData().getString("userName");
    }

    @NonNull
    @Override
    public Result doWork() {
        InstallationItemTableHandler dbHandler = new InstallationItemTableHandler(getApplicationContext());
        ArrayList<InstallationItemEntity> uploadableItems = dbHandler.getUploadableWorkItems(username);
        InstallationTransactionTableHandler transactionTableHandler = new InstallationTransactionTableHandler(getApplicationContext());
        ArrayList<InstallationTransactionEntity> uploadableTransactions = transactionTableHandler.getUpladableTransactions(username);
        int progress = uploadableItems.size() + uploadableTransactions.size() == 0 ? 0 : 1;
        if (progress == 0) {
            return Result.success();
        }
        if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) > 7 && Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 18){
            createNotificationChannel();
            setForegroundAsync(createForegroundInfo());
        }
        updateNotificationProgress(progress,"Feltöltés megkezdése");
        int increment = Math.round(100 / (Math.max(uploadableItems.size() + uploadableTransactions.size(), 1)));
        Boolean result;
        boolean needRetry = false;
        try {
            for (InstallationItemEntity item : uploadableItems) {
                int counter = 0;
                result = false;
                if (item.getComment() != null && item.getComment().equals(GPS_LOCATION)
                        && item.getStatus().equals(DBStatus.UPLOAD.value)) {
                    result = uploadLocation(String.valueOf(item.getIdWork()), (float) item.getLongitude(), (float) item.getLatitude(), item.getId());
                    while (!result && counter < 9) {
                        result = uploadLocation(String.valueOf(item.getIdWork()), (float) item.getLongitude(), (float) item.getLatitude(), item.getId());
                        Thread.sleep(2000);
                        counter++;
                    }
                    if (!result) {
                        needRetry = true;
                    }
                } else if (item.getPhoto_path() != null
                        && item.getStatus().equals(DBStatus.UPLOAD.value)) {
                    result = uploadDocument(String.valueOf(item.getIdWork()), item.getWorkState(), item.getComment(), item.getPhoto_path(), item.getId(), item.getDate());
                    while (!result && counter < 9) {
                        result = uploadDocument(String.valueOf(item.getIdWork()), item.getWorkState(), item.getComment(), item.getPhoto_path(), item.getId(), item.getDate());
                        Thread.sleep(2000);
                        counter++;
                    }
                    if (!result) {
                        needRetry = true;
                    }
                }
                else {
                    InstallationItemTableHandler db = new InstallationItemTableHandler(getApplicationContext());
                    db.updateStatus(item.getId(), DBStatus.DONE, username);
                }
                progress += increment;
                updateNotificationProgress(progress, "Dokumentumok felöltése...");

            }
            for (InstallationTransactionEntity transaction :uploadableTransactions) {
                int counter = 0;
                result = false;
                result = uploadTransaction(transaction.getWorkId(),transaction.getMaterial()
                                          ,transaction.getQuantity(), transaction.getSerialNum()
                                          , transaction.getDate(),transaction.getRowId());
                while (!result && counter < 9) {
                    result = uploadTransaction(transaction.getWorkId(), transaction.getMaterial()
                                              , transaction.getQuantity(), transaction.getSerialNum()
                                              , transaction.getDate(),transaction.getRowId());
                    Thread.sleep(2000);
                    counter++;
                }
                progress += increment;
                updateNotificationProgress(progress, "Anyagok felöltése...");
                if (!result) {
                    needRetry = true;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (needRetry){
            updateNotificationProgress(-1,"Sikertelen feltöltés!");
            return Result.retry();
        }

        updateNotificationProgress(100,"A feltöltés sikeresen befejeződött");
        return Result.success();
    }


    private Boolean uploadDocument(String id, String type, String subject, String picturePath, int rowId, String createdTime) {
        final Boolean[] result = new Boolean[1];
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        if (activeNetworkInfo == null || !activeNetworkInfo.isConnected() || !activeNetworkInfo.isAvailable()) {
            result[0] = false;
            return result[0];
        }
        SSOService.getToken(getApplicationContext(), new SSOResult() {
            @Override
            public void onSuccess(String token) {
                try {
                    Response<Void> call = RetrofitClientWorksheet.getInstance(getApplicationContext()).getApi().uploadImage(id, token, new EncodedWorksheetImageDto(type, subject, picturePath, createdTime)).execute();
                    if (call.code() == 201) {
                        InstallationItemTableHandler db = new InstallationItemTableHandler(getApplicationContext());
                        db.updateStatus(rowId, DBStatus.DONE, username);
                        result[0] = true;
                    } else if (call.code() > 499) {
                        result[0] = false;
                    } else {
                        result[0] = false;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    result[0] = false;
                }
            }

            @Override
            public void onFailure(String token) {
                if (RunningProcessHelper.isApplicationRunning(getApplicationContext(),"com.giganet.giganet_worksheet")){
                    Intent intent = new Intent(getApplicationContext(), AuthenticationActivity.class);
                    intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
                    getApplicationContext().startActivity(intent);
                    Toast.makeText(getApplicationContext().getApplicationContext(), "Nem megfelelő felhasználó név vagy jelszó", Toast.LENGTH_SHORT).show();
                }
            }
        });


        return result[0];
    }

    private Boolean uploadTransaction(int workId, String material, int quantity, String serialNum, String date, int rowId){
        final Boolean[] result = new Boolean[1];

        SSOService.getToken(getApplicationContext(), new SSOResult() {
            @Override
            public void onSuccess(String token) {
                try {
                    Response<Void> call = RetrofitClientWorksheet.getInstance(getApplicationContext()).getApi().uploadTransaction(
                                            token, String.valueOf(workId), new TransactionDto(material,quantity,serialNum,date)).execute();
                    if (call.code() == 201 ||call.code() == 200) {
                        InstallationTransactionTableHandler db = new InstallationTransactionTableHandler(getApplicationContext());
                        db.updateStatus(rowId, DBStatus.DONE.value);
                        result[0] = true;
                    } else if (call.code() > 499) {
                        result[0] = false;
                    } else {
                        result[0] = false;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    result[0] = false;
                }

            }

            @Override
            public void onFailure(String token) {
                Intent intent = new Intent(getApplicationContext(), AuthenticationActivity.class);
                intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
                getApplicationContext().startActivity(intent);
                Toast.makeText(getApplicationContext().getApplicationContext(), "Nem megfelelő felhasználó név vagy jelszó", Toast.LENGTH_SHORT).show();
            }
        });
        return result[0];
    }

    private Boolean uploadLocation(String id, float longitude, float latitude, int rowId) {
        final Boolean[] result = new Boolean[1];

        SSOService.getToken(getApplicationContext(), new SSOResult() {
            @Override
            public void onSuccess(String token) {
                try {
                    Response<InstallationTaskEntity> call = RetrofitClientWorksheet.getInstance(getApplicationContext()).getApi().updateLocation(token, id, new InstallationTaskEntity.GPSLocation(longitude, latitude)).execute();
                    if (call.code() == 200 || call.code() == 201) {
                        InstallationItemTableHandler db = new InstallationItemTableHandler(getApplicationContext());
                        db.updateStatus(rowId, DBStatus.DONE, username);
                        result[0] = true;
                    } else if (call.code() > 499) {
                        result[0] = false;
                    } else if (call.code() >= 400) {
                        result[0] = false;
                    } else {
                        result[0] = false;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    result[0] = false;
                }
            }

            @Override
            public void onFailure(String token) {
                Intent intent = new Intent(getApplicationContext(), AuthenticationActivity.class);
                intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
                getApplicationContext().startActivity(intent);
                Toast.makeText(getApplicationContext().getApplicationContext(), "Nem megfelelő felhasználó név vagy jelszó", Toast.LENGTH_SHORT).show();
            }
        });

        return result[0];
    }


    @NonNull
    private ForegroundInfo createForegroundInfo() {
        notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), WORKSHEETNOTIFYCHANNELNAME);

        notificationBuilder.setOngoing(true)
                .setSmallIcon(android.R.drawable.ic_menu_upload)
                .setContentTitle("Dokumentumok feltöltése")
                .setContentText("Feltöltés jelenlegi állapota: ")
                .setDefaults(NotificationCompat.PRIORITY_LOW)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setAutoCancel(true)
                .setProgress(100, 0, false)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return new ForegroundInfo(WORKSHEETNOTIFICATIONID, notificationBuilder.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
        } else {
            return new ForegroundInfo(WORKSHEETNOTIFICATIONID, notificationBuilder.build());
        }
    }

    private void updateNotificationProgress(int progress, String msg) {
        if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) > 7 && Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 18) {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
            notificationBuilder.setProgress(100, progress, false)
                    .setSmallIcon(android.R.drawable.ic_menu_upload)
                    .setAutoCancel(true)
                    .setContentText(msg);

            notificationManager.notify(WORKSHEETNOTIFICATIONID, notificationBuilder.build());
        }
        EventBus.getDefault().post(new DocumentStatusUpdateEvent(progress,msg));
    }

    private void createNotificationChannel() {
        String description = "Státusz feltöltés állapota";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(WORKSHEETNOTIFYCHANNELNAME, WORKSHEETNOTIFICATIONNAME, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = getApplicationContext().getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }
}
