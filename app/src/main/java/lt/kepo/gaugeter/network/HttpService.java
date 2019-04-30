package lt.kepo.gaugeter.network;

import lt.kepo.gaugeter.holders.DeviceInfoHolder;
import lt.kepo.gaugeter.holders.LoginHolder;
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
    Single<List<DeviceInfoHolder>> addDeviceToUser(@Body DeviceInfoHolder device);

    @GET("/api/devices/GetUserDevices")
    Single<List<DeviceInfoHolder>> getUserDevices();

    @DELETE("/api/devices/Remove")
    Call<Void> removeDeviceFromUser(@Query("bluetoothAddress") String bluetoothAddress);
}
