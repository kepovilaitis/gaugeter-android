package lt.kepo.gaugeter.holders;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@Getter
@AllArgsConstructor
public class TelemDataHolder implements Serializable {
    private float _oilTemperature;
    private float _oilPressure;
    private float _waterTemperature;
    private float _charge;
}
