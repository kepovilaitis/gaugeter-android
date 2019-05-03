package lt.kepo.gaugeter.holders;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Setter
@Getter
public class JobHolder implements Serializable {
    public static final int ONGOING = 0;
    public static final int FINISHED = 1;

    private int _id;
    private DeviceHolder _device;
    private int _state;
    private List<TelemDataHolder> _telemData;
    private long _dateCreated;
    private long _dateUpdated;

    public JobHolder(DeviceHolder device) {
        _device = device;
        _state = ONGOING;
        _telemData = new ArrayList<>();
        _dateCreated = System.currentTimeMillis();
    }

    public void addTelemData(TelemDataHolder telemData) {
        _telemData.add(telemData);
    }

    public void resetTelemData() {
        _telemData = new ArrayList<>();
    }
}
