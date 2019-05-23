package lt.kepo.gaugeter.holders;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class UserInfoHolder {
    private String _userId;
    private String _password;
    private String _description;
    private int _measurementSystem;
}