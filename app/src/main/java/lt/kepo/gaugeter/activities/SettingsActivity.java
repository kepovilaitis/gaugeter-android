package lt.kepo.gaugeter.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import lt.kepo.gaugeter.R;
import lt.kepo.gaugeter.fragments.preferences.SettingsFragment;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setTitle(getResources().getString(R.string.settings));

        android.app.FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.mainContent, new SettingsFragment());
        fragmentTransaction.commit();
    }
}

