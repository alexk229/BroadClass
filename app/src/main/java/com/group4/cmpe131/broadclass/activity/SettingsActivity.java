package com.group4.cmpe131.broadclass.activity;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.group4.cmpe131.broadclass.R;
import com.group4.cmpe131.broadclass.app.Config;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {

    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        final String themePrefKey = getString(R.string.pref_theme), defaultTheme = getResources().getString(R.string.pref_theme_default);
        final String theme = PreferenceManager.getDefaultSharedPreferences(this).getString(themePrefKey, defaultTheme);

        switch (theme) {
            case "dark":
                setTheme(R.style.AppTheme_Dark);
                Config.appTheme = 2;
                break;
            case "light":
                setTheme(R.style.AppTheme_Light);
                Config.appTheme = 1;
                break;
        }

        super.onCreate(savedInstanceState);
        setupActionBar();
        addPreferencesFromResource(R.xml.pref_settings);

        ListPreference themeListPreference = (ListPreference) findPreference(themePrefKey);
        themeListPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                restartActivity(SettingsActivity.this);
                return true;
            }

    });
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void restartActivity(final Activity activity) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            activity.recreate();
        else {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    activity.overridePendingTransition(0, 0);
                    activity.startActivity(activity.getIntent());
                }
            });
            activity.finish();
        }
    }

    private void setupActionBar() {
        ViewGroup rootView = (ViewGroup) findViewById(R.id.action_bar_root); //id from appcompat

        if (rootView != null) {
            View view = getLayoutInflater().inflate(R.layout.app_bar_settings_layout, rootView, false);
            rootView.addView(view, 0);

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(SettingsActivity.this, MainActivity.class));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

}

