package lt.kepo.gaugeter.holders;

import lt.kepo.gaugeter.constants.Enums.MEASUREMENT_SYSTEM;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class UserInfoHolder
{
    private String _userId;
    private String _password;
    private String _description;
    private MEASUREMENT_SYSTEM _measurementSystem;
}