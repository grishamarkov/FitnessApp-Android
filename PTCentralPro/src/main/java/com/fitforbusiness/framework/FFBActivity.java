package com.fitforbusiness.framework;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.appboy.Appboy;
import com.appboy.BuildConfig;
import com.appboy.ui.slideups.AppboySlideupManager;

import org.json.JSONException;
import org.json.JSONObject;

/*
 * Appboy integration sample (using Appboy Fragments)
 *
 * To start tracking analytics using the Appboy Android SDK, in all activities, you must call Appboy.openSession()
 * and Appboy.closeSession() in the activity's onStart() and onStop() respectively. You can see that in this
 * activity (inherited by most other activities) and com.appboy.sample.PreferencesActivity.
 */
public class FFBActivity extends ActionBarActivity {
    protected static final String TAG = String.format("%s.%s", "FFBFragmentActivity", FFBActivity.class.getName());
    private boolean mRefreshData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activateStrictMode();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("Super method called is", "onStart()");
        // Opens (or reopens) an Appboy session.
        // Note: This must be called in the onStart lifecycle method of EVERY Activity. Failure to do so
        // will result in incomplete and/or erroneous analytics.
        if (Appboy.getInstance(this).openSession(this)) {
            mRefreshData = true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("Super method called is", "onResume()");
        // Registers the AppboySlideupManager for the current Activity. This Activity will now listen for
        // slideup messages from Appboy.
        AppboySlideupManager.getInstance().registerSlideupManager(this);
        if (mRefreshData) {
            Appboy.getInstance(this).requestSlideupRefresh();
            mRefreshData = false;
        }
        //Crittercism.leaveBreadcrumb(this.getClass().getName());
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("Super method called is", "onPause()");
        // Unregisters the AppboySlideupManager.
        AppboySlideupManager.getInstance().unregisterSlideupManager(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("Super method called is", "onStop()");
        // Closes the current Appboy session.
        // Note: This must be called in the onStop lifecycle method of EVERY Activity. Failure to do so
        // will result in incomplete and/or erroneous analytics.
        Appboy.getInstance(this).closeSession(this);
    }

    @TargetApi(9)
    private void activateStrictMode() {
        // Set the activity to Strict mode so that we get LogCat warnings when code misbehaves on the main thread.
        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
        }
    }

    protected void setTitle(String title) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setTitleOnActionBar(title);
        } else {
            super.setTitle(title);
        }
    }

    @TargetApi(11)
    private void setTitleOnActionBar(String title) {
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(title);
    }

    private JSONObject getCrittercismConfiguration() {
        JSONObject crittercismConfiguration = new JSONObject();
        String customVersionName = getCustomVersionName();
        try {
            crittercismConfiguration.put("includeVersionCode", true);
            crittercismConfiguration.put("shouldCollectLogcat", true);
            if (customVersionName != null) {
                crittercismConfiguration.put("customVersionName", customVersionName);
            }
        } catch (JSONException e) {
            Log.w(TAG, "Error while creating the Crittercism configuration. Using default configuration.");
        }
        return crittercismConfiguration;
    }

    private String getCustomVersionName() {
        String customVersionName = null;
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            customVersionName = String.format("%s-%s", packageInfo.versionName, com.appboy.Constants.APPBOY_SDK_VERSION);
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "Unable to read the version name.");
        }
        return customVersionName;
    }

}
