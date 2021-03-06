package lt.kepo.gaugeter.network;

import lt.kepo.gaugeter.holders.DeviceHolder;
import lt.kepo.gaugeter.holders.LoginHolder;
import lt.kepo.gaugeter.holders.JobHolder;

import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.Getter;
import lombok.Setter;

import lt.kepo.gaugeter.holders.UserInfoHolder;
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

public class HttpClient {
    private static final String GAUGETER_BASE_URL = "https://kepo.lt";
    @Setter @Getter private String _userToken = "";

    @Getter private static HttpClient _instance;

    private HttpService _httpService;

    private HttpClient() {
        Gson gson = new GsonBuilder()
                .setFieldNamingStrategy(_namingPolicy)
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(GAUGETER_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(new OkHttpClient.Builder().addInterceptor(_okHttpClientInterceptor).build())
                .build();

        _httpService = retrofit.create(HttpService.class);
    }

    public static void setInstance() {
        _instance = new HttpClient();
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

    public Single<LoginHolder> login(LoginHolder loginHolder) {
        return _httpService.login(loginHolder);
    }

    public void logout() {
        _userToken = "";
    }

    public Single<List<DeviceHolder>> addDeviceToUser(DeviceHolder device) {
        return _httpService.addDeviceToUser(device);
    }

    public Single<List<DeviceHolder>> getUserDevices() {
        return _httpService.getUserDevices();
    }

    public Call<Void> removeDeviceFromUser(String bluetoothAddress) {
        return _httpService.removeDeviceFromUser(bluetoothAddress);
    }

    public Single<JobHolder> upsertJob(JobHolder jobHolder) {
        return _httpService.upsertJob(jobHolder);
    }

    public Call<Void> deleteJob(int jobId) {
        return _httpService.deleteJob(jobId);
    }

    public Single<JobHolder> getJob(int jobId) {
        return _httpService.getJob(jobId);
    }

    public Single<List<JobHolder>> getJobsByDate(long start, long end) {
        return _httpService.getJobsByDate(start, end);
    }

    public Single<JobHolder> getLast() {
        return _httpService.getLast();
    }

    public Single<UserInfoHolder> updateUser(UserInfoHolder userInfo) {
        return _httpService.updateUser(userInfo);
    }

    public Single<DeviceHolder> updateDevice(DeviceHolder device) {
        return _httpService.updateDevice(device);
    }
}
