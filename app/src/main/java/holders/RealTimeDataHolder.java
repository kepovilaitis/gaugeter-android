package holders;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RealTimeDataHolder {
    private int _oilTemperature;
    private int _oilPressure;
    private int _waterTemperature;
    private int _charge;
}
