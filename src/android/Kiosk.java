package com.example.template.kiosk.plugin;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Kiosk extends CordovaPlugin {

    private DevicePolicyManager mDpm;
    private boolean mIsKioskEnabled;
    private CallbackContext mCallbackContext;

    private static final String TAG = "Kiosk.java";

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        mCallbackContext = callbackContext;
        if (action.equals("lockLauncher")) {
            Log.d(TAG, "Kiosk.java action = lockLauncher ");
            Boolean locked = args.getBoolean(0);
            lockLauncher(locked);
            return true;
        } else if (action.equals("isLocked")) {
            Log.d(TAG, "Kiosk.java action = isLocked ");
            callbackContext.success(String.valueOf(mIsKioskEnabled));
            return true;
        } else if (action.equals("switchLauncher")) {
            Log.d(TAG, "Kiosk.java action = switchLauncher ");
            switchLauncher(callbackContext);
            return true;
        } else if (action.equals("deleteDeviceAdmin")) {
            Log.d(TAG, "Kiosk.java action = deleteDeviceAdmin ");
            deleteDeviceAdmin();
        }
        return false;
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
//        hideSystemUI();
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        Log.d(TAG, "Kiosk.java initialize() ");
        super.initialize(cordova, webView);
        lockLauncher(true);
        enterFullscreen();
    }

    private void enterFullscreen() {
        Log.d(TAG, "Kiosk.java enterFullscreen() ");
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                hideSystemUI();
            }
        });
    }

    private void hideSystemUI() {
        Log.d(TAG, "Kiosk.java hideSystemUI() ");
        View mDecorView = cordova.getActivity().getWindow().getDecorView();
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        mDecorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        mDecorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                cordova.getActivity().getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }
        });
    }

    private void switchLauncher(CallbackContext callbackContext) {
        Log.d(TAG, "Kiosk.java switchLauncher() ");
        if (mIsKioskEnabled) {
            Log.d(TAG,"Kiosk.java switchLauncher() if(mIsKioskEnabled) true ");
            lockLauncher(false);
        }
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Intent chooser = Intent.createChooser(intent, "Select destination...");
        if (intent.resolveActivity(cordova.getActivity().getPackageManager()) != null) {
            cordova.getActivity().startActivity(chooser);
        }
        callbackContext.success();
    }

    private void lockLauncher(boolean locked) {
        Log.d(TAG, "Kiosk.java lockLauncher() ");
        if (locked && !mIsKioskEnabled) {
            Log.d(TAG, "Kiosk.java lockLauncher() if (locked && !mIsKioskEnabled) true ");

            mDpm = (DevicePolicyManager) cordova.getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);
            ComponentName deviceAdmin = new ComponentName(cordova.getActivity(), MyAdmin.class);
            if (!mDpm.isAdminActive(deviceAdmin)) {
                Log.d(TAG, "Kiosk.java lockLauncher() not admin active ");
                callbackMessage(false, "not admin active");
            }
            if (mDpm.isDeviceOwnerApp(cordova.getActivity().getPackageName())) {
                Log.d(TAG, "Kiosk.java lockLauncher() isDeviceOwnerApp true  ");
                mDpm.setLockTaskPackages(deviceAdmin, new String[]{cordova.getActivity().getPackageName()});
            } else {
                Log.d(TAG, "Kiosk.java lockLauncher() not device owner app  ");
                callbackMessage(false, "not device owner app");
            }
            Log.d(TAG, "Kiosk.java lockLauncher() enableKioskMode(true) ");
            enableKioskMode(true);
        } else if (!locked) {
            Log.d(TAG, "Kiosk.java lockLauncher() else if (!locked) true  => enableKioskMode(false) ");
            enableKioskMode(false);
        }
    }

    private void enableKioskMode(boolean enabled) {
        Log.d(TAG, "Kiosk.java enableKioskMode() ");
        try {
            if (enabled) {
                Log.d(TAG, "Kiosk.java enableKioskMode() enabled true ");
                if (mDpm.isLockTaskPermitted(cordova.getActivity().getPackageName())) {
                    Log.d(TAG, "Kiosk.java enableKioskMode() isLockTaskPermitted true ");
                    cordova.getActivity().startLockTask();
                    mIsKioskEnabled = true;
                } else {
                    Log.d(TAG, "Kiosk.java enableKioskMode() no permission to lock ");
                    mIsKioskEnabled = false; // esto es una prueba para que no sea null
                    callbackMessage(false, "no permission to lock");
                }
            } else {
                Log.d(TAG, "Kiosk.java enableKioskMode() enabled false ");
                cordova.getActivity().stopLockTask();
                mIsKioskEnabled = false;
            }
        } catch (Exception e) {
            Log.d(TAG, "Kiosk.java enableKioskMode() android lock task exception ");
            callbackMessage(false, "android lock task exception");
            e.printStackTrace();
        }
    }

    private void deleteDeviceAdmin() {
        Log.d(TAG, "Kiosk.java deleteDeviceAdmin() ");
        mDpm.clearDeviceOwnerApp(cordova.getActivity().getPackageName());
    }


    private void callbackMessage(boolean success, String message) {
        Log.d(TAG, "Kiosk.java callbackMessage() ");
        if (mCallbackContext == null) {
            Log.d(TAG, "Kiosk.java callbackMessage() mCallbackContext == null  true");
            return;
        }
        if (success) {
            Log.d(TAG, "Kiosk.java callbackMessage() success  true");
            PluginResult dataResult = new PluginResult(PluginResult.Status.OK, message);
            dataResult.setKeepCallback(true);
            mCallbackContext.sendPluginResult(dataResult);
        } else {
            Log.d(TAG, "Kiosk.java callbackMessage() success  false ");
            PluginResult dataResult = new PluginResult(PluginResult.Status.ERROR, message);
            dataResult.setKeepCallback(true);
            mCallbackContext.sendPluginResult(dataResult);
        }
        mCallbackContext = null;
    }
}
