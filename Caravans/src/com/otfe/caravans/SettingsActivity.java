package com.otfe.caravans;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import com.otfe.caravans.performance_test.PerformanceTestActivity;

public class SettingsActivity extends PreferenceActivity {
	private final String TAG = "Settings Activity";

	private SharedPreferences preferences;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
 /*       
        preferences = getSharedPreferences(Constants.SETTINGS_NAME,
				Constants.SETTINGS_MODE);
        
        PreferenceManager preferenceManager = getPreferenceManager();
		preferenceManager.setSharedPreferencesName(Constants.SETTINGS_NAME);
		preferenceManager.setSharedPreferencesMode(0);
		preferences = preferenceManager.getSharedPreferences();

		PreferenceManager.setDefaultValues(SettingsActivity.this,
				R.xml.preferences, false);
*/
        addPreferencesFromResource(R.xml.preferences);
        setContentView(R.layout.settings);
        setTheme(R.style.LightText);
    }
	
	public void onClick(View view){
		switch(view.getId()){
			case R.id.perf_test_button:
				Intent intent = new Intent(this, PerformanceTestActivity.class);
				startActivity(intent);
				break;
			case R.id.btn_reset:
				Log.d(TAG, "Deleting database");
				this.deleteDatabase(Constants.DATABASE_NAME);
				break;
		}
	}
}