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
public class JobHolder implements Serializable{
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

    public JobHolder(JobHolder job) {
        _id = job._id;
        _device = job.getDevice();
        _state = job.getState();
        _telemData = job.getTelemData();
        _dateCreated = job.getDateCreated();
        _dateUpdated = job.getDateUpdated();
    }

    public void addTelemData(TelemDataHolder telemData) {
        _telemData.add(telemData);
    }

    public void resetTelemData() {
        _telemData = new ArrayList<>();
    }
}
