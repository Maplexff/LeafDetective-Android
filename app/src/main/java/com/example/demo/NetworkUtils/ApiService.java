package com.example.demo.NetworkUtils;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HEAD;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ApiService {
//    @Multipart
//    @POST("/predict")
//    Call<ResponseBody> uploadImage(@Part MultipartBody.Part file);

    @Multipart
    @POST("/predict")
    Call<ResponseBody> uploadImage(
                                       @Part("time") RequestBody time,
                                       @Part("location") RequestBody location,
                                       @Part("lat") RequestBody lat,
                                       @Part("lng") RequestBody lng,
                                       @Part("reportid") RequestBody reportid,
                                       @Part MultipartBody.Part file);


    @GET("/records/")
    Call<PaginatedResponse> getRecords(
            @Query("page") int page,
            @Query("page_size") int pageSize
    );


    @GET("/ping")
    Call<CheckResponse> checkServerStatus();

    @POST("users/login")
    Call<UserCheck> login(
            @Body UserRegister loginRequest
    );

    @POST("users/register")
    Call<UserInfo> register(
            @Body UserRegister RegisterRequest
    );

    @POST("users/editnamebyid")
    Call<UserCheck> editnamebyid(
            @Body UserInfo editnamebyidRequest
    );

    @POST("users/editpwdbyid")
    Call<UserCheck> editpwdbyid(
            @Body UserPwd editpwdbyidRequest
    );
}

