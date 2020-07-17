/*
Copyright (c) 2011, Sony Ericsson Mobile Communications AB
Copyright (c) 2011-2013, Sony Mobile Communications AB

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

package com.example.sonymobile.smartextension.hellolayouts;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.sonyericsson.extras.liveware.aef.control.Control;
import com.sonyericsson.extras.liveware.aef.registration.Registration;
import com.sonyericsson.extras.liveware.extension.util.ExtensionUtils;
import com.sonyericsson.extras.liveware.extension.util.control.ControlExtension;
import com.sonyericsson.extras.liveware.extension.util.control.ControlObjectClickEvent;
import com.sonyericsson.extras.liveware.extension.util.control.ControlTouchEvent;

import java.io.ByteArrayOutputStream;

/**
 * This demonstrates two different approaches, bitmap and layout, for displaying
 * a UI. The bitmap approach is useful for accessories without layout support,
 * e.g. SmartWatch.
 * This sample shows all UI components that can be used, except Gallery and
 * ListView.
 */
class HelloLayoutsControl extends ControlExtension {

    enum RenderType {
        LAYOUT, BITMAP
    }

    /** Contains the chosen UI to render, e.g. layout or bitmap. */
    private RenderType mRenderType = RenderType.LAYOUT;

    /** Contains the counter value shown in the UI. */
    private int mCount = 0;

    /** Used to toggle if an icon will be visible in the bitmap UI. */
    private boolean mIconImage = true;

    /**
     * Defines the size of the touch area for one of the images used in the
     * bitmap UI.
     */
    Rect mImageTouchArea = new Rect(0, 0, 48, 48);

    /**
     * Defines the size of the touch area for one of the buttons used in the
     * bitmap UI
     */
    Rect mChangeTouchArea = new Rect(54, 0, 220, 48);

    /**
     * Defines the size of the touch area for one of the buttons used in the
     * bitmap UI
     */
    Rect mUpdateThisArea = new Rect(54, 0, 220, 176);

    /**
     * Create control extension.
     *
     * @param hostAppPackageName Package name of host application.
     * @param context The context.
     * @param handler The handler to use.
     */
    HelloLayoutsControl(final String hostAppPackageName, final Context context, Handler handler) {
        super(context, hostAppPackageName);
        if (handler == null) {
            throw new IllegalArgumentException("handler == null");
        }
    }

    /**
     * Return the width of the screen which this control extension supports.
     *
     * @param context The context.
     * @return The width in pixels.
     */
    public static int getSupportedControlWidth(Context context) {
        return context.getResources().getDimensionPixelSize(R.dimen.smart_watch_2_control_width);
    }

    /**
     * Return the height of the screen which this control extension supports.
     *
     * @param context The context.
     * @return The height in pixels.
     */
    public static int getSupportedControlHeight(Context context) {
        return context.getResources().getDimensionPixelSize(R.dimen.smart_watch_2_control_height);
    }

    @Override
    public void onDestroy() {
        Log.d(HelloLayoutsExtensionService.LOG_TAG, "onDestroy: HelloLayoutsControl");
    };

    @Override
    public void onObjectClick(final ControlObjectClickEvent event) {
        Log.d(HelloLayoutsExtensionService.LOG_TAG,
                "onObjectClick: HelloLayoutsControl click type: " + event.getClickType());

        // Check which view was clicked and then take the desired action.
        switch (event.getLayoutReference()) {
            case R.id.btn_show_bitmap:
                toggleLayoutBitmap();
                break;
            case R.id.btn_update_this:
                incrementAndUpdate();
                break;
            case R.id.btn_update_layout:
                updateLayout();
                break;
            case R.id.image:
                toggleImage();
                break;
        }
    }

    @Override
    public void onTouch(ControlTouchEvent event) {
        super.onTouch(event);

        Log.d(HelloLayoutsExtensionService.LOG_TAG, "onTouch: HelloLayoutsControl " + mRenderType
                + " - " + event.getX()
                + ", " + event.getY());

        // The touch method can be used with both layouts and bitmaps. It is
        // necessary together with bitmaps because there is no reference to a
        // clicked view. When using bitmaps it is necessary to check if the
        // touch area is inside the view area.
        if (mRenderType.equals(RenderType.BITMAP)
                && event.getAction() == Control.Intents.TOUCH_ACTION_RELEASE) {
            if (mImageTouchArea.contains(event.getX(), event.getY())) {
                mIconImage = !mIconImage;
                updateBitmap();
            } else if (mChangeTouchArea.contains(event.getX(), event.getY())) {
                toggleLayoutBitmap();
            } else if (mUpdateThisArea.contains(event.getX(), event.getY())) {
                mCount++;
                updateBitmap();
            }
        }
    }

    /**
     * When using a bitmap as a UI, there is no way to update just a view
     * or a part of the layout, you need to send the whole bitmap. It is
     * necessary to inflate the layout and change the data to be displayed in
     * the view before sending the bitmap.
     */
    private void updateBitmap() {
        RelativeLayout root = new RelativeLayout(mContext);
        root.setLayoutParams(new LayoutParams(220, 176));

        // Set dimensions and properties of the bitmap to use when rendering
        // the UI.
        int bitmapWidth = getSupportedControlWidth(mContext);
        int bitmapHeight = getSupportedControlHeight(mContext);
        Bitmap bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.RGB_565);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, new ByteArrayOutputStream(256));
        bitmap.setDensity(DisplayMetrics.DENSITY_DEFAULT);

        Canvas canvas = new Canvas(bitmap);

        // Inflate an existing layout to use as a base.
        RelativeLayout layout = (RelativeLayout) RelativeLayout.inflate(mContext,
                R.layout.bitmap, root);
        // Set dimensions of the layout to use in the UI. We use the same
        // dimensions as the bitmap.
        layout.measure(bitmapHeight, bitmapWidth);
        layout.layout(0, 0, layout.getMeasuredWidth(), layout.getMeasuredHeight());

        // Update the counter value.
        if (mCount > 0) {
            TextView textView = (TextView) layout.findViewById(R.id.btn_update_this);
            String caption = mContext.getString(R.string.text_tap_to_update);
            caption += " " + mCount;
            textView.setText(caption);
        }

        // Determine what icon to add to the layout.
        int resId = R.drawable.icon_extension48;
        if (!mIconImage) {
            resId = R.drawable.actions_view_in_phone;
        }

        ImageView imageView = (ImageView) layout.findViewById(R.id.image);
        imageView.setImageResource(resId);

        // Convert the layout to a bitmap using the canvas.
        layout.draw(canvas);

        showBitmap(bitmap);
    }

    /**
     * This is an example of how to update the entire layout and some of the
     * views. For each view, a bundle is used. This bundle must have the layout
     * reference, i.e. the view ID and the content to be used. This method
     * updates an ImageView and a TextView.
     *
     * @see Control.Intents#EXTRA_DATA_XML_LAYOUT
     * @see Registration.LayoutSupport
     */
    private void updateLayout() {
        mCount = 0;
        mIconImage = true;

        String caption = mContext.getString(R.string.text_tap_to_update);

        // Prepare a bundle to update the button text.
        Bundle bundle1 = new Bundle();
        bundle1.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE, R.id.btn_update_this);
        bundle1.putString(Control.Intents.EXTRA_TEXT, caption);

        // Prepare a bundle to update the ImageView image.
        Bundle bundle2 = new Bundle();
        bundle2.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE, R.id.image);
        bundle2.putString(Control.Intents.EXTRA_DATA_URI,
                ExtensionUtils.getUriString(mContext, R.drawable.icon_extension48));

        Bundle[] bundleData = new Bundle[2];
        bundleData[0] = bundle1;
        bundleData[1] = bundle2;

        showLayout(R.layout.layout, bundleData);
    }

    /**
     * Toggles the use of a bitmap or layout based UI.
     */
    private void toggleLayoutBitmap() {
        if (mRenderType.equals(RenderType.LAYOUT)) {
            mRenderType = RenderType.BITMAP;
            updateBitmap();
        } else {
            mRenderType = RenderType.LAYOUT;
            updateLayout();
        }
    }

    /**
     * This method updates a non-bitmap TextView in the layout.
     */
    private void incrementAndUpdate() {
        mCount++;
        String caption = mContext.getString(R.string.text_tap_to_update);
        caption += " " + mCount;

        // The sendText method is used to update the text of a single view
        // instead of updating the entire layout.
        sendText(R.id.btn_update_this, caption);
    }

    @Override
    public void onResume() {
        // Send a UI when the extension becomes visible.
        if (mRenderType.equals(RenderType.BITMAP)) {
            updateBitmap();
        } else {
            updateLayout();
        }

        super.onResume();
    }

    /**
     * This method toggles what icon to show.
     */
    private void toggleImage() {
        Log.d(HelloLayoutsExtensionService.LOG_TAG, "toggleImage: HelloLayoutsControl");

        // The sendImage method is used to update an image of a single ImageView.
        if (mIconImage) {
            sendImage(R.id.image, R.drawable.actions_view_in_phone);
        } else {
            sendImage(R.id.image, R.drawable.icon_extension48);
        }
        mIconImage = !mIconImage;
    }
}
