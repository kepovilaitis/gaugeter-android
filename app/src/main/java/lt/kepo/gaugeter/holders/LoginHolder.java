package lt.kepo.gaugeter.holders;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class LoginHolder {
    private String _token;
    private UserInfoHolder _user;
}
