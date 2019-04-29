package lt.kepo.gaugeter.holders;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@Getter
@AllArgsConstructor
public class DeviceInfoHolder implements Serializable {
    private String _name;
    private String _bluetoothAddress;
}
