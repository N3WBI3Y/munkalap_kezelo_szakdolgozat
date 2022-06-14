package com.giganet.giganet_worksheet.Network.Worksheet;

import com.giganet.giganet_worksheet.Persistence.Worksheet.Entities.InstallationTaskEntity;
import com.giganet.giganet_worksheet.Persistence.Worksheet.Entities.InstallationMaterialEntity;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface WorksheetApi {


    @GET("tasks?status=All")
    Call<List<InstallationTaskEntity>> getAllTasks(@Header("Authorization") String token);

    @GET("tasks?status=Open")
    Call<List<InstallationTaskEntity>> getAllOpenTasks(@Header("Authorization") String token);

    @GET("tasks/{id}")
    Call<InstallationTaskEntity> getTask(@Header("Authorization") String token, @Path(value = "id", encoded = false) String id);

    @POST("tasks/{id}/documents")
    Call<Void> uploadImage(@Path(value = "id", encoded = false) String id
            , @Header("Authorization") String token
            , @Body EncodedWorksheetImageDto imageEntity);

    @POST("tasks/{id}/status")
    Call<InstallationTaskEntity> updateStatus(@Path(value = "id", encoded = false) String id
            , @Header("Authorization") String token
            , @Body WorksheetStatusDto worksheetStatusDto);

    @PATCH("tasks/{id}/gps-location")
    Call<InstallationTaskEntity> updateLocation(@Header("Authorization") String token,
                                                @Path(value = "id", encoded = false) String id, @Body InstallationTaskEntity.GPSLocation location);


    @GET("service-type/actions")
    Call<List<ServiceTypeDto>> getServiceTypeActions(@Header("Authorization") String token);

    @GET("material/materials")
    Call<List<MaterialDto>> getMaterials(@Header("Authorization") String token);

    @POST("transaction/{id}/transaction")
    Call<Void> uploadTransaction(@Header("Authorization") String token,
                                 @Path(value = "id", encoded = false) String id, @Body TransactionDto transaction);


}
