package lt.kepo.gaugeter.holders;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Setter
@Getter
public class WorkHolder {
    public static final int ONGOING = 0;
    public static final int FINISHED = 1;

    private int _id;
    private String _deviceBluetoothAddress;
    private String _userId;
    private int _state;
    private List<LiveDataHolder> _liveData;
    private long _timeCreated;
    private long _timeUpdated;

    public WorkHolder(String address, String userId) {
        _deviceBluetoothAddress = address;
        _userId = userId;
        _state = ONGOING;
        _liveData = new ArrayList<>();
        _timeCreated = System.currentTimeMillis();
    }

    public void resetLiveData() {
        _liveData = new ArrayList<>();
    }
}
