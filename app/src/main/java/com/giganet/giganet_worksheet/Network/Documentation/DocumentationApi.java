package com.giganet.giganet_worksheet.Network.Documentation;

import com.giganet.giganet_worksheet.Persistence.Documentation.Entities.DocumentationEntity;
import com.giganet.giganet_worksheet.Persistence.Documentation.Entities.DocumentationHistoryEntity;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

    public interface DocumentationApi {

    @POST("task")
    Call<DocumentationEntity.DocumentationEntityReceived> createTaskOnBackEnd(@Header("Authorization") String token
            , @Body DocumentationEntity statusEntity);

    @POST("image")
    Call<Void> uploadImageToBackEnd(@Header("Authorization") String token
            , @Body EncodedDocumentationImageDto encodedImage);

    @GET("tasktypes")
    Call<ArrayList<DocumentationStatusDto>> getStatusTypes(@Header("Authorization") String token);

    @GET("images/{order}/{pagesize}/{page}/{category}/{search}")
    Call<ArrayList<DocumentationHistoryEntity>> getDocumentedImages(@Header("Authorization") String token
            , @Path(value = "order", encoded = false) String order
            , @Path(value = "pagesize", encoded = false) int pageSize
            , @Path(value = "page", encoded = false) int page
            , @Path(value = "category", encoded = false) String category
            , @Path(value = "search", encoded = false) String search);

    @GET("images/{order}/{pagesize}/{page}")
    Call<ArrayList<DocumentationHistoryEntity>> getLastDocumentedImage(@Header("Authorization") String token
            , @Path(value = "order", encoded = false) String order
            , @Path(value = "pagesize", encoded = false) int pageSize
            , @Path(value = "page", encoded = false) int page);


    @GET("image/{imageId}")
    Call<DocumentationHistoryEntity> getImage(@Header("Authorization") String token
            , @Path(value = "imageId", encoded = false) int imageId);

}
