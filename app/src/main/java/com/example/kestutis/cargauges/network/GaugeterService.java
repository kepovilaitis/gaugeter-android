package com.example.kestutis.cargauges.network;

import com.example.kestutis.cargauges.holders.LoginHolder;
import com.example.kestutis.cargauges.holders.UserInfoHolder;
import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface GaugeterService {
    @Headers({"Content-Type: application/json", "api-version: 1.0"})
    @POST("api/users/authenticate")
    Single<UserInfoHolder> login(@Body LoginHolder loginHolder);
}
