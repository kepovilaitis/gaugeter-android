package com.example.kestutis.cargauges.network;

import com.example.kestutis.cargauges.holders.LoginHolder;
import com.example.kestutis.cargauges.holders.UserInfoHolder;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import rx.Observable;

public interface GaugeterService {
    @Headers({"Content-Type: application/json", "api-version: 1.0"})
    @POST("api/users/authenticate")
    Observable<UserInfoHolder> login(@Body LoginHolder loginHolder);
}
