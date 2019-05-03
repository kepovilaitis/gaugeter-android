package lt.kepo.gaugeter.holders;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TelemDataHolder {
    private float _oilTemperature;
    private float _oilPressure;
    private float _waterTemperature;
    private float _charge;
}
