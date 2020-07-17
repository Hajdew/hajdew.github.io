/*
 * Copyright (c) 2013, Sony Mobile Communications AB
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sony Mobile Communications AB nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * @file BasicCameraApp.java
 *
 */

package com.sonymobile.android.camera.addon.basiccameraapp;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.sonymobile.camera.addon.capturingmode.CapturingModeSelector;

/**
 * This is a sample activity class for a Camera Add-on
 * which demonstrates how to use the CapturingModeSelector.
 * In this class, concrete usage of the CapturingModeSelector is written.
 */
public class BasicCameraApp extends Activity {

    private static final String TAG = BasicCameraApp.class.getSimpleName();

    // The following capturing mode name is registered to the Camera Add-on framework.
    // The name must match the same value as "name" attribute of "<mode>" tag
    // of "res/xml/sample_mode_attributes.xml".
    public static final String MODE_NAME = "basic_camera_app";

    // Value of pressed icon color filter.
    private static final int PRESSED_COLOR_FILTER = 0x66000000;

    private CapturingModeSelector mCapturingModeSelector;

    private OrientationEventListener mOrientationEventListener;

    private ImageButton mModeSelectorButton;

    private CameraWrapper mCameraWrapper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        // Setup views and click and touch listeners.
        mModeSelectorButton = ((ImageButton)findViewById(R.id.button));
        mModeSelectorButton.setOnClickListener(new ModeSelectorButtonClickListener());
        mModeSelectorButton.setOnTouchListener(new ModeSelectorButtonTouchListener());
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Open camera device and start preview.
        TextureView textureView = (TextureView)findViewById(R.id.surface);
        mCameraWrapper = new CameraWrapper();
        textureView.setSurfaceTextureListener(mCameraWrapper);
        mCameraWrapper.setup(getDisplayRotation());


        // Set a button which will open the CapturingModeSelector so that a
        // user can launch any mode of any camera add-on app, including the
        // SOMC camera app, by tapping a mode icon listed in the
        // CapturingModeSelector.

        // Create a parent view for capturing mode selector view.
        ViewGroup modeSelectorContainer = (ViewGroup)findViewById(R.id.modeselector_container);

        // Check for the existence of Camera Add-on API implementation.
        try {
            // Create a CapturingModeSelector.
            mCapturingModeSelector = new CapturingModeSelector(this, modeSelectorContainer);
        } catch (NoClassDefFoundError e) {
            // If the camera add-on library is not found (Camera Add-on API not supported),
            // implement suitable exception handling.
            Log.e(TAG, "Camera add-on library not found. Handle the exception,"
                    + " e.g. finish the activity.");
        }

        if (mCapturingModeSelector != null) {
            // Set two listeners and make the mode selector button visible
            mCapturingModeSelector.setOnModeSelectListener(new MyOnModeSelectListener());
            mCapturingModeSelector.setOnModeFinishListener(new MyOnModeFinishListener());
            mModeSelectorButton.setVisibility(View.VISIBLE);
        } else {
            // If the Camera Add-on API is not supported, the capturing mode button should
            // not be shown.
            mModeSelectorButton.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Release the camera device.
        mCameraWrapper.release();
        mCameraWrapper = null;

        // Release the CapturingModeSelector.
        if (mCapturingModeSelector != null) {
            mCapturingModeSelector.release();
            mCapturingModeSelector = null;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Setup orientation event listener.
        mOrientationEventListener = new ExtendedOrientationEventListener(this);
        mOrientationEventListener.enable();
    }

    @Override
    protected void onStop() {
        // Release the orientation listener.
        mOrientationEventListener.disable();
        mOrientationEventListener = null;
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Release the selector button.
        mModeSelectorButton.setOnClickListener(null);
        mModeSelectorButton.setOnTouchListener(null);
    }

    /**
     * When Back is pressed while CapturingModeSelector is opened, close the
     * CapturingModeSelector. In all other cases, finish this activity.
     */
    @Override
    public void onBackPressed() {
        if (mCapturingModeSelector != null && mCapturingModeSelector.isOpened()) {
            mCapturingModeSelector.close();
            mModeSelectorButton.setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Returns the rotation of the screen from its "natural" orientation in degrees.
     */
    private int getDisplayRotation() {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        return degrees;
    }

    private class ModeSelectorButtonTouchListener implements View.OnTouchListener {
        /**
         * According to the UI guidelines, the mode selector button should apply a color filter
         * to display a pressed state when pressed.
         */
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mModeSelectorButton.onTouchEvent(event);
            if (mModeSelectorButton.isPressed()) {
                mModeSelectorButton.setColorFilter(PRESSED_COLOR_FILTER);
            } else {
                mModeSelectorButton.clearColorFilter();
            }
            return true;
        }
    }

    private class ModeSelectorButtonClickListener implements View.OnClickListener {
        /**
         * When a user opens the CapturingModeSelector by tapping mode selector button in the UI,
         * pass the current mode name to the camera add-on framework.
         * The camera add-on framework will highlight the icon of the mode whose name was passed.
         */
        @Override
        public void onClick(View v) {
            if (mCapturingModeSelector != null) {
                // If the view tapped is the CapturingModeSelector button, hide the
                // mode name text view, buttons, open the CapturingModeSelector and
                // remove the color filter
                mCapturingModeSelector.open(MODE_NAME);
                mModeSelectorButton.setVisibility(View.INVISIBLE);
                mModeSelectorButton.clearColorFilter();
            }
        }
    }

    private class MyOnModeSelectListener implements CapturingModeSelector.OnModeSelectListener {
        /**
         * onModeSelect(String modeName) is called when the current mode and next mode are in
         * <UL>
         * <LI> the same package and the same activity.</LI>
         * </UL>
         * The next mode is specified by an argument, modeName. The modeName is
         * the name of the mode that has been selected by the user.
         */
        @Override
        public void onModeSelect(String modeName) {
            if (mCapturingModeSelector != null) {
                mCapturingModeSelector.close();
                mModeSelectorButton.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Implementation of CapturingModeSelector.OnModeFinishListener.
     */
    private class MyOnModeFinishListener implements CapturingModeSelector.OnModeFinishListener {
        /**
         * onModeFinish() is called when the current mode and the next mode are in
         * <UL>
         * <LI> the same package and different activity.</LI>
         * <LI> a different package and same activity.</LI>
         * <LI> a different package and different activity.</LI>
         * </UL>
         * In other words, this is called when an activity of the current mode needs to finish.
         */
        @Override
        public void onModeFinish() {
            if (mCapturingModeSelector != null) {
                mCapturingModeSelector.close();
            }
            finish();
        }
    }

    /**
     * In order to properly handle situations where the user rotates the device,
     * implement an OrientationEventListener and perform UI orientation updates
     * in onOrientationChanged().
     */
    private class ExtendedOrientationEventListener extends OrientationEventListener {
        public ExtendedOrientationEventListener(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(final int orientation) {

            // Do not update UI when orientation is unknown.
            if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
                return;
            }

            // Update UI orientation.
            int rotation = (orientation + getDisplayRotation()) % 360;
            if (rotation < 30 || 150 < rotation) {
                if (mCapturingModeSelector != null) {
                    mCapturingModeSelector.setUiOrientation(Configuration.ORIENTATION_LANDSCAPE);
                }
                mModeSelectorButton.setRotation(0);
            } else if (60 <= rotation && rotation < 120) {
                if (mCapturingModeSelector != null) {
                    mCapturingModeSelector.setUiOrientation(Configuration.ORIENTATION_PORTRAIT);
                }
                mModeSelectorButton.setRotation(270);
            }
        }
    }
}