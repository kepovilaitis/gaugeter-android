package lt.kepo.gaugeter.holders;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
public class DeviceHolder implements Serializable {
    private String _name;
    private String _bluetoothAddress;
}
