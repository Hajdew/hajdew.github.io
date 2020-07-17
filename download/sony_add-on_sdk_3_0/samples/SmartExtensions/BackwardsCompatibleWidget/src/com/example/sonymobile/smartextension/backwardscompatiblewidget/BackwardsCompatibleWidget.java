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

package com.example.sonymobile.smartextension.backwardscompatiblewidget;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sonyericsson.extras.liveware.aef.widget.Widget;
import com.sonyericsson.extras.liveware.extension.util.SmartWatchConst;
import com.sonyericsson.extras.liveware.extension.util.registration.DeviceInfoHelper;
import com.sonyericsson.extras.liveware.extension.util.widget.BaseWidget;

/**
 * The sample widget implements Widget functionality for both SmartWatch and SmartWatch 2
 */
class BackwardsCompatibleWidget extends BaseWidget {

    public static final int WIDGET_WIDTH_SMARTWATCH = 128;

    public static final int WIDGET_HEIGHT_SMARTWATCH = 110;

    public static final int WIDGET_WIDTH_CELLS = 5;

    public static final int WIDGET_HEIGHT_CELLS = 2;

    private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.RGB_565;

    private static final long UPDATE_INTERVAL = 10 * DateUtils.SECOND_IN_MILLIS;

    private Boolean isSmartWatch2;

    private BackwardsCompatibleWidgetRegistrationInformation mRegistration;

    /**
     * Create sample widget.
     *
     */
    public BackwardsCompatibleWidget(WidgetBundle bundle) {
        super(bundle);
    }

    /**
     * Start refreshing the widget. The widget is now visible.
     */
    @Override
    public void onStartRefresh() {
        Log.d(BackwardsCompatibleWidgetExtensionService.LOG_TAG, "startRefresh");
        updateWidget(1);
        // Update now and every 10th second
        cancelScheduledRefresh(getExtensionKey());
        scheduleRepeatingRefresh(System.currentTimeMillis(), UPDATE_INTERVAL,
                getExtensionKey());
    }

    @Override
    public void onStopRefresh() {
        Log.d(BackwardsCompatibleWidgetExtensionService.LOG_TAG, "stopRefesh");

        // Cancel pending updates
        cancelScheduledRefresh(getExtensionKey());
    }

    @Override
    public void onScheduledRefresh() {
        Log.d(BackwardsCompatibleWidgetExtensionService.LOG_TAG, "scheduledRefresh()");
        updateWidget(1);
    }

    @Override
    public void onDestroy() {
        Log.d(BackwardsCompatibleWidgetExtensionService.LOG_TAG, "onDestroy()");
        onStopRefresh();
    }

    @Override
    public void onTouch(final int type, final int x, final int y) {
        Log.d(BackwardsCompatibleWidgetExtensionService.LOG_TAG, "onTouch() " + type);
        if (!isSmartWatch2() && !SmartWatchConst.ACTIVE_WIDGET_TOUCH_AREA.contains(x, y)) {
            Log.d(BackwardsCompatibleWidgetExtensionService.LOG_TAG, "Ignoring touch outside active area x: " + x
                    + " y: " + y);
            return;
        }

        if (type == Widget.Intents.EVENT_TYPE_SHORT_TAP) {
            // Update widget now.
            updateWidget(-1);
        }
    }

    /**
     * Changes a text value and returns it.
     *
     * @param increment The increment by which to update the number in the
     * returned text.
     * @return The updated string.
     */
    private String getIncrementedText(int increment) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        int numberOfUpdates = prefs.getInt(mContext.getString(R.string.preference_key_updates), 0);
        numberOfUpdates = numberOfUpdates + increment;
        prefs.edit().putInt(mContext.getString(R.string.preference_key_updates), numberOfUpdates)
                .commit();
        return mContext.getString(R.string.updates) + numberOfUpdates;
    }

    /**
     * Updates the widget. This widget simply sends an updated bitmap when
     * requested.
     *
     * @param increment The number by which the text's number should be incremented
     */
    private void updateWidget(int increment) {
        Bitmap bitmap = null;
        bitmap = createWidgetBitmap(increment);
        showBitmap(bitmap);
    }

    private boolean isSmartWatch2() {
        if (isSmartWatch2 == null) {
            isSmartWatch2 = DeviceInfoHelper.isSmartWatch2ApiAndScreenDetected(mContext,
                    mHostAppPackageName);
        }
        return isSmartWatch2;
    }

    /**
     * Returns a bitmap. The bitmap properties will be based on whether the accessory
     * is a SmartWatch or a SmartWatch 2.
     *
     * @param increment The current update value to increment.
     * @return The bitmap.
     */
    private synchronized Bitmap createWidgetBitmap(int increment) {
        // Get text.
        String text = getIncrementedText(increment);
        // Create a bitmap of the correct size to draw in.
        int widgetWidth = (isSmartWatch2() ? getWidth() : WIDGET_WIDTH_SMARTWATCH);
        int widgetHeight = (isSmartWatch2() ? getHeight() : WIDGET_HEIGHT_SMARTWATCH);
        Log.e(BackwardsCompatibleWidgetExtensionService.LOG_TAG, "Widget Update: " + text + " Size: " + widgetWidth
                + ":" + widgetHeight);
        Bitmap bitmap = Bitmap.createBitmap(widgetWidth, widgetHeight, BITMAP_CONFIG);

        // Set default density to avoid scaling.
        bitmap.setDensity(DisplayMetrics.DENSITY_DEFAULT);

        LinearLayout root = new LinearLayout(mContext);
        root.setLayoutParams(new LayoutParams(getWidth(), getHeight()));

        LinearLayout sampleLayout = (LinearLayout) LinearLayout.inflate(mContext,
                R.layout.layout_widget, root);

        // Update the text view with new values.
        TextView tView = (TextView) sampleLayout.findViewById(R.id.widget_text);
        tView.setText(text);

        sampleLayout.measure(widgetWidth, widgetHeight);
        sampleLayout
                .layout(0, 0, sampleLayout.getMeasuredWidth(), sampleLayout.getMeasuredHeight());

        Canvas canvas = new Canvas(bitmap);
        sampleLayout.draw(canvas);

        return bitmap;
    }
    
    private String getExtensionKey() {
        if (mRegistration == null){
            mRegistration = new BackwardsCompatibleWidgetRegistrationInformation(mContext);
        }
        return mRegistration.getExtensionKey();
    }



    // Required methods for registration in Widget API version 3

    @Override
    public int getWidth() {
        return (int) (mContext.getResources().getDimension(R.dimen.smart_watch_2_widget_cell_width)
        * WIDGET_WIDTH_CELLS);
    }

    @Override
    public int getHeight() {
        return (int) (mContext.getResources()
                .getDimension(R.dimen.smart_watch_2_widget_cell_height)
        * WIDGET_HEIGHT_CELLS);
    }

    @Override
    public int getPreviewUri() {
        return R.drawable.widget_frame;
    }

    @Override
    public int getName() {
        return R.string.extension_name;
    }
}
