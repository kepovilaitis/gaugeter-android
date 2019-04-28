package com.example.kestutis.cargauges.network;

import com.example.kestutis.cargauges.holders.DeviceInfoHolder;
import com.example.kestutis.cargauges.holders.LoginHolder;
import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.Setter;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import io.reactivex.Single;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

public class GaugeterClient {
    private static final String GAUGETER_BASE_URL = "https://kepo.lt";
    @Setter @Getter private String _userToken = "";

    @Getter private static GaugeterClient _instance;

    private GaugeterService _gaugeterService;

    private GaugeterClient() {
        final Gson gson = new GsonBuilder().setFieldNamingStrategy(_namingPolicy).create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(GAUGETER_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(new OkHttpClient.Builder().addInterceptor(_okHttpClientInterceptor).build())
                .build();

        _gaugeterService = retrofit.create(GaugeterService.class);
    }

    public static void setInstance() {
        _instance = new GaugeterClient();
    }

    public Single<LoginHolder> login(LoginHolder loginHolder) {
        return _gaugeterService.login(loginHolder);
    }

    public void logout() {
        _userToken = "";
    }

    public Call<Void> addDeviceToUser(DeviceInfoHolder device) {
        return _gaugeterService.addDeviceToUser(device);
    }

    public Single<List<DeviceInfoHolder>> getUserDevices() {
            return _gaugeterService.getUserDevices();
    }

    public Call<Void> removeDeviceFromUser(String bluetoothAddress) {
            return _gaugeterService.removeDeviceFromUser(bluetoothAddress);
    }

    private FieldNamingStrategy _namingPolicy = new FieldNamingStrategy() {
        @Override
        public String translateName(Field field) {
            return field.getName().replaceFirst("^(_(is)?)", "").substring(0, 1).toLowerCase() + field.getName().replaceFirst("^(_(is)?)", "").substring(1);
        }
    };

    private Interceptor _okHttpClientInterceptor = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request original = chain.request();

            Request request = original.newBuilder()
                    .header("Authorization", "Bearer " + _userToken)
                    .header("api-version", "1.0")
                    .method(original.method(), original.body())
                    .build();

            return chain.proceed(request);
        }
    };
}
