package holders;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class DeviceInfoHolder {
    private String _name;
    private String _address;
    private int _bondState;
}
