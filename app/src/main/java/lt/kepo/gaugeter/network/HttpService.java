package lt.kepo.gaugeter.network;

import lt.kepo.gaugeter.holders.DeviceHolder;
import lt.kepo.gaugeter.holders.LoginHolder;
import lt.kepo.gaugeter.holders.JobHolder;

import io.reactivex.Single;

import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

public interface HttpService {
    @Headers({"Content-Type: application/json"})
    @POST("/authenticate")
    Single<LoginHolder> login(@Body LoginHolder loginHolder);

    @Headers({"Content-Type: application/json"})
    @POST("/api/devices/AddDeviceToUser")
    Single<List<DeviceHolder>> addDeviceToUser(@Body DeviceHolder device);

    @GET("/api/devices/GetUserDevices")
    Single<List<DeviceHolder>> getUserDevices();

    @DELETE("/api/devices/Remove")
    Call<Void> removeDeviceFromUser(@Query("bluetoothAddress") String bluetoothAddress);

    @Headers({"Content-Type: application/json"})
    @POST("/api/jobs/upsert")
    Single<JobHolder> upsertJob(@Body JobHolder jobHolder);

    @DELETE("/api/jobs/Delete")
    Call<Void> deleteJob(@Query("jobId") int jobId);

    @GET("/api/jobs/Get")
    Single<JobHolder> getJob(@Query("jobId") int jobId);

    @GET("/api/jobs/GetByDate")
    Single<List<JobHolder>> getJobsByDate(@Query("start") long start, @Query("end") long end);

    @GET("/api/jobs/GetLast")
    Single<JobHolder> getLast();
}
