package com.giganet.giganet_worksheet.Resources.Services;

import static com.giganet.giganet_worksheet.Resources.Constants.ServiceConstants.Documentation.DOCUMENTATIONNOTIFYCHANNELNAME;
import static com.giganet.giganet_worksheet.Resources.Constants.ServiceConstants.Documentation.FINISHEDNOTIFICATIONID;
import static com.giganet.giganet_worksheet.Resources.Constants.ServiceConstants.Documentation.NOTIFICATIONID;
import static com.giganet.giganet_worksheet.Resources.Constants.ServiceConstants.Documentation.NOTIFICATIONNAME;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.ServiceInfo;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.ForegroundInfo;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.giganet.giganet_worksheet.Network.Documentation.EncodedDocumentationImageDto;
import com.giganet.giganet_worksheet.Network.Documentation.RetrofitClientDocumentation;
import com.giganet.giganet_worksheet.Utils.SSOResult;
import com.giganet.giganet_worksheet.Persistence.Documentation.DocumentationItemTableHandler;
import com.giganet.giganet_worksheet.Persistence.Documentation.DocumentationTableHandler;
import com.giganet.giganet_worksheet.Persistence.Documentation.Entities.DocumentationEntity;
import com.giganet.giganet_worksheet.Persistence.Documentation.Entities.DocumentationItemEntity;
import com.giganet.giganet_worksheet.Resources.Constants.SharedPreferences;
import com.giganet.giganet_worksheet.Resources.Enums.DBStatus;
import com.giganet.giganet_worksheet.Utils.Converters;
import com.giganet.giganet_worksheet.Utils.NetworkHelper;
import com.giganet.giganet_worksheet.Utils.SSOService;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import retrofit2.Response;


public class SaveToDocumentationBackEndWorker extends Worker {


    private NotificationCompat.Builder notificationBuilder;


    public SaveToDocumentationBackEndWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String username = getApplicationContext().getSharedPreferences(SharedPreferences.SHAREDPREFERENCES, Context.MODE_PRIVATE).getString("USERNAME", "");
        DocumentationTableHandler db = new DocumentationTableHandler(getApplicationContext());
        ArrayList<DocumentationEntity> documentationEntities = db.getUploadableDocumentations(username);
        if (documentationEntities.size() > 0) {
            createNotificationChannel();
            if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) > 7 && Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 18) {
                setForegroundAsync(createForegroundInfo());
            }
        }
        else {
            return Result.success();
        }
        boolean result = true;
        for (int index = 0; index < documentationEntities.size(); index++) {
            documentationEntities.get(index).setBackEndId(getDocumentationBackEndId(documentationEntities.get(index)));
            result &= uploadDocumentationPictures(documentationEntities.get(index));
            if (result) {
                float progress = ((float) (index + 1) / (float) documentationEntities.size()) * 100;
                updateNotificationProgress((int) (progress));
                db = new DocumentationTableHandler(getApplicationContext());
                db.removeItem(documentationEntities.get(index).getId());
            } else {
                createNotificationChannel();
                notificationFinished( documentationEntities.get(index).getType()+" típusú feltöltés sikertelen");
                return Result.retry();
            }

        }
        if (result) {
            notificationFinished("Feltöltés sikeresen befejeződött");
        }

        return Result.success();
    }


    private Integer getDocumentationBackEndId(DocumentationEntity entity) {
        if (entity.getCity() == null && entity.getLatitude() != null && entity.getLongitude() != null) {
            entity.setCity(Converters.coordinateToAddress(new LatLng(entity.getLatitude(), entity.getLongitude()),getApplicationContext()));
        }
        final int[] backendId = {0};
        if (entity.getBackEndId() == 0) {
            SSOService.getToken(getApplicationContext(), new SSOResult() {
                @Override
                public void onSuccess(String token) {
                    Response<DocumentationEntity.DocumentationEntityReceived> call;
                    try {
                        call = RetrofitClientDocumentation.getInstance(getApplicationContext()).getApi().createTaskOnBackEnd(token, entity).execute();
                        if (call.body() != null) {
                            DocumentationTableHandler db = new DocumentationTableHandler(getApplicationContext());
                            db.updateBackendId(entity.getId(), call.body().getBackendId());
                            backendId[0] =  call.body().getBackendId();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        backendId[0] = 0;
                    }
                }

                @Override
                public void onFailure(String token) {

                }
            });

            return backendId[0];

        } else {
            return entity.getBackEndId();
        }
    }

    private Boolean uploadDocumentationPictures(DocumentationEntity entity) {
        if (entity.getBackEndId() == 0) {
            return false;
        }
        DocumentationItemTableHandler itemDb = new DocumentationItemTableHandler(getApplicationContext());
        boolean allUploaded = true;
        ArrayList<DocumentationItemEntity> items = itemDb.getDocumentIdItems(entity.getUserId(), entity.getId());
        for (DocumentationItemEntity item : items) {
            if (item.getStatus().equals(DBStatus.UPLOAD.value)) {
                boolean resultOfUpload = false;
                int counter = 0;
                try {
                    resultOfUpload = uploadPicture(entity.getBackEndId(), item.getSubject(), item.getPhotoPath(), item.getCreatedTime(), item.getExternalIdentifier());
                    while (!resultOfUpload && counter < 9) {
                        resultOfUpload = uploadPicture(entity.getBackEndId(), item.getSubject(), item.getPhotoPath(), item.getCreatedTime(), item.getExternalIdentifier());
                        Thread.sleep(2000);
                        counter++;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                allUploaded &= resultOfUpload;
            }
        }
        return allUploaded;
    }

    private Boolean uploadPicture(int taskId, String subject, String picturePath, String createdTime, String externalIdentifier) {
        final Boolean[] result = {false};
        if (!NetworkHelper.isNetworkAvailable(getApplicationContext())) {
            return false;
        }
        SSOService.getToken(getApplicationContext(), new SSOResult() {
            @Override
            public void onSuccess(String token) {
                Response<Void> call;
                try {
                    call = RetrofitClientDocumentation.getInstance(getApplicationContext()).getApi().uploadImageToBackEnd(token, new EncodedDocumentationImageDto(taskId, createdTime, picturePath, subject, externalIdentifier)).execute();
                    DocumentationItemTableHandler db = new DocumentationItemTableHandler(getApplicationContext());
                    if (call.code() == 201) {
                        db.updateStatus(picturePath, DBStatus.DONE.value);
                        result[0] = true;
                    } else if (call.code() == 409) {
                        db.removeItem(picturePath);
                        result[0] = true;

                    } else if (call.code() > 499) {
                        result[0] = false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    result[0] = false;
                }
            }

            @Override
            public void onFailure(String token) {

            }
        });
        return result[0];
    }

    private ForegroundInfo createForegroundInfo() {

        notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), DOCUMENTATIONNOTIFYCHANNELNAME);
        notificationBuilder.setOngoing(true)
                .setSmallIcon(android.R.drawable.ic_menu_upload)
                .setContentTitle("Dokumentáció feltöltése")
                .setContentText("Feltöltés jelenlegi állapota: ")
                .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setProgress(100, 0, true)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return new ForegroundInfo(NOTIFICATIONID, notificationBuilder.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
        } else {
            return new ForegroundInfo(NOTIFICATIONID, notificationBuilder.build());
        }
    }

    private void updateNotificationProgress(int progress) {
        if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) > 7 && Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 18) {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
            notificationBuilder.setProgress(100, progress, true)
                    .setSmallIcon(android.R.drawable.ic_menu_upload)
                    .setAutoCancel(true);

            notificationManager.notify(NOTIFICATIONID, notificationBuilder.build());
        }
    }


    private void createNotificationChannel() {
        String description = "Feltötés állapota";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(DOCUMENTATIONNOTIFYCHANNELNAME, NOTIFICATIONNAME, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = getApplicationContext().getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    private void notificationFinished(String massage) {
        if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) > 7 && Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 18) {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());

            try {
                notificationBuilder.setContentTitle(massage)
                        .setContentText("")
                        .setProgress(0, 0, false)
                        .setAutoCancel(true)
                        .setOngoing(false)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setSmallIcon(android.R.drawable.ic_menu_upload);
                notificationManager.notify(FINISHEDNOTIFICATIONID, notificationBuilder.build());
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }
}