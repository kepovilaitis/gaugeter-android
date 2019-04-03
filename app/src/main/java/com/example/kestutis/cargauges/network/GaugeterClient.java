package com.example.kestutis.cargauges.network;

import com.example.kestutis.cargauges.holders.LoginHolder;
import com.example.kestutis.cargauges.holders.UserInfoHolder;
import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import io.reactivex.Single;

import java.lang.reflect.Field;

public class GaugeterClient {
    private static final String GAUGETER_BASE_URL = "https://kepo.lt/";

    private static GaugeterClient _instance;
    private GaugeterService _gaugeterService;

    private GaugeterClient() {
        final Gson gson = new GsonBuilder().setFieldNamingStrategy(_namingPolicy).create();

        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(GAUGETER_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        _gaugeterService = retrofit.create(GaugeterService.class);
    }

    public static GaugeterClient getInstance() {
        if (_instance == null) {
            _instance = new GaugeterClient();
        }

        return _instance;
    }

    public Single<UserInfoHolder> login(LoginHolder loginHolder) {
        return _gaugeterService.login(loginHolder);
    }

    private FieldNamingStrategy _namingPolicy = new FieldNamingStrategy() {
        @Override
        public String translateName(Field field) {
            return field.getName().replaceFirst("^(_(is)?)", "").substring(0, 1).toLowerCase() + field.getName().replaceFirst("^(_(is)?)", "").substring(1);
        }
    };
}
