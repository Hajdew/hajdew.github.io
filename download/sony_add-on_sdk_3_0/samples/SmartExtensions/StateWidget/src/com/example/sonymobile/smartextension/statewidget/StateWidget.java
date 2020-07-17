/*
Copyright (c) 2014, Sony Mobile Communications AB

 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.

 * Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 * Neither the name of the Sony Mobile Communications AB nor the names
 of its contributors may be used to endorse or promote
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

package com.example.sonymobile.smartextension.statewidget;

import android.content.Intent;
import android.os.Bundle;

import com.sonyericsson.extras.liveware.aef.control.Control;
import com.sonyericsson.extras.liveware.aef.widget.Widget;
import com.sonyericsson.extras.liveware.extension.util.widget.BaseWidget;

/**
 * The state widget shows different layout based on the Accessory State
 * When the widget is tapped, the control part of the extension is opened on
 * the accessory.
 */
class StateWidget extends BaseWidget {

    public StateWidget(WidgetBundle bundle) {
        super(bundle);
    }

    public static final int WIDTH = 215;
    public static final int HEIGHT = 68;

    @Override
    public void onStartRefresh() {
        // Show the layout
        showLayout();
    }

    @Override
    public void onStopRefresh() {
        // Nothing done here
    }

    @Override
    public void onTouch(int type, int x, int y) {
        // User has tapped the widget while online. Open the control.
        Intent intent = new Intent(Control.Intents.CONTROL_START_REQUEST_INTENT);
        intent.putExtra(Control.Intents.EXTRA_AEA_PACKAGE_NAME, mContext.getPackageName());
        sendToHostApp(intent);
    }

    /**
     * Shows a layout that provides a bundle containing the strings and the
     * resource id in the layouts that shall be updated when the accessory
     * switches state.
     */
    private void showLayout() {
        Bundle bundleNoTouchText = new Bundle();
        bundleNoTouchText.putInt(Widget.Intents.EXTRA_LAYOUT_REFERENCE,
                R.id.widget_text_box_view_no_touch);
        bundleNoTouchText.putString(Control.Intents.EXTRA_TEXT,
                mContext.getString(R.string.powersave));

        Bundle bundleOfflineText = new Bundle();
        bundleOfflineText.putInt(Widget.Intents.EXTRA_LAYOUT_REFERENCE,
                R.id.widget_text_box_view_offline);
        bundleOfflineText.putString(Control.Intents.EXTRA_TEXT,
                mContext.getString(R.string.disconnected));

        Bundle[] layoutData = new Bundle[2];
        layoutData[0] = bundleNoTouchText;
        layoutData[1] = bundleOfflineText;

        // The SmartWatch 2 will choose the layout connected to each state. By
        // providing different layouts, it will be possible to adapt different
        // UIs to each of these states.
        showLayout(R.layout.widget_text_box,
                R.layout.widget_text_box_powersave,
                R.layout.widget_text_box_disconnected, layoutData);

    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public int getPreviewUri() {
        return R.drawable.preview_1_2;
    }

    @Override
    public int getName() {
        return R.string.widget_name;
    }

}
