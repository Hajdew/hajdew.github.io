/*
Copyright (c) 2011, Sony Ericsson Mobile Communications AB
Copyright (c) 2011-2014, Sony Mobile Communications AB

 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.

 * Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 * Neither the name of the Sony Ericsson Mobile Communications AB / Sony Mobile
 Communications AB nor the names of its contributors may be used to endorse or promote
 products derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.example.sonymobile.smartextension.helloactivelowpower;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.sonyericsson.extras.liveware.aef.control.Control;
import com.sonyericsson.extras.liveware.extension.util.control.ControlExtension;
import com.sonyericsson.extras.liveware.extension.util.control.ControlObjectClickEvent;
import com.sonyericsson.extras.liveware.extension.util.control.ControlView;
import com.sonyericsson.extras.liveware.extension.util.control.ControlView.OnClickListener;
import com.sonyericsson.extras.liveware.extension.util.control.ControlViewGroup;

/**
 * A Control Extension that supports Active Low Power.
 */
class HelloActiveLowPowerControl extends ControlExtension {

    private boolean mIsInActiveLowPower = false;

    private ControlViewGroup mLayout = null;

    protected boolean mLowPowerButtonPressed;

    private int mCounter;

    /**
     * Simple sample control that supports Active Low Power.
     *
     * @param hostAppPackageName Package name of host application.
     * @param context The context.
     */
    HelloActiveLowPowerControl(final String hostAppPackageName, final Context context) {
        super(context, hostAppPackageName);
        setupClickables(context);
    }

    /**
     * Get supported control width.
     *
     * @param context The context.
     * @return the width.
     */
    public static int getSupportedControlWidth(Context context) {
        return context.getResources().getDimensionPixelSize(R.dimen.smart_watch_2_control_width);
    }

    /**
     * Get supported control height.
     *
     * @param context The context.
     * @return the height.
     */
    public static int getSupportedControlHeight(Context context) {
        return context.getResources().getDimensionPixelSize(R.dimen.smart_watch_2_control_height);
    }

    @Override
    public void onResume() {
        showLayout(R.layout.sample_active_low_power_control, null);
        updatePressedText();

    }

    @Override
    public void onObjectClick(final ControlObjectClickEvent event) {
        Log.d(HelloActiveLowPowerExtensionService.LOG_TAG,
                "onObjectClick() " + event.getClickType());
        if (event.getLayoutReference() != -1) {
            mLayout.onClick(event.getLayoutReference());
        }
    }

    @Override
    public void onActiveLowPowerModeChange(boolean lowPowerModeOn) {
        mIsInActiveLowPower = lowPowerModeOn;
        Log.d(HelloActiveLowPowerExtensionService.LOG_TAG, "onActiveLowPowerModeChange: "
                + mIsInActiveLowPower);
        showLowPowerText(mIsInActiveLowPower);
        updatePressedText();
        if (mIsInActiveLowPower) {
            // Show why ALP was reached
            if (mLowPowerButtonPressed) {
                sendText(R.id.tv_low_power, "ALP via Button Press");
            } else {
                // Otherwise show that we timed out on text display
                sendText(R.id.tv_low_power, "ALP via Timeout");
            }
        }
        mLowPowerButtonPressed = false;
    }

    private void setupClickables(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.sample_active_low_power_control
                , null);
        mLayout = parseLayout(layout);
        if (mLayout != null) {
            ControlView btnPowerMode = mLayout.findViewById(R.id.btn_low_power);
            btnPowerMode.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick() {
                    Log.d(HelloActiveLowPowerExtensionService.LOG_TAG, "ALP Button pressed");
                    // This buttons starts active low power mode, simply by
                    // requesting screen state off. The logic for handling low
                    // power mode is in onActiveLowPowerModeChange
                    if (!mIsInActiveLowPower) {
                        mLowPowerButtonPressed = true;
                        setScreenState(Control.Intents.SCREEN_STATE_OFF);
                    } else {
                        // Active Low Power mode can be exited programmatically
                        // by setting the screen state back to Auto. The user
                        // can always wake the accessory up via presses of the
                        // power button.
                        setScreenState(Control.Intents.SCREEN_STATE_AUTO);
                    }
                }
            });
            ControlView tvPowerMode = mLayout.findViewById(R.id.tv_low_power);

            tvPowerMode.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick() {
                    // If the accessory enters Active Low Power mode via
                    // timeout, tapping on the accessory's display will wake it
                    // up. If the screen is programmatically turned off, the
                    // user can interact with the display without leaving Active
                    // Low Power mode.
                    mCounter++;
                    updatePressedText();
                }
            });
        }
    }

    /**
     * Show Button label based on power mode
     *
     * @param active
     */
    private void showLowPowerText(boolean active) {
        int textRes = (active) ? R.string.btn_low_power_stop : R.string.btn_low_power_start;
        sendText(R.id.btn_low_power, mContext.getString(textRes));
    }

    /**
     * Show text label based on button presses
     */
    private void updatePressedText() {
        String text = "Text pressed: " + mCounter;
        sendText(R.id.tv_low_power, text);
    }
}
