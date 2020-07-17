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

package com.example.sonymobile.smartextension.helloevents;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.sonyericsson.extras.liveware.aef.control.Control;
import com.sonyericsson.extras.liveware.extension.util.ExtensionUtils;
import com.sonyericsson.extras.liveware.extension.util.control.ControlExtension;
import com.sonyericsson.extras.liveware.extension.util.control.ControlObjectClickEvent;
import com.sonyericsson.extras.liveware.extension.util.control.ControlTouchEvent;

/**
 * A control extension implementation showing how to handle different events
 * such as touch and using features such as the options menu and hardware
 * touch buttons.
 */
class HelloEventsControl extends ControlExtension {

    enum EventType {
        TOUCH, OBJECT_CLICK, KEY_OPTIONS, KEY_BACK, MENU_ITEM_CLICK
    }

    private static final int MENU_ITEM_0 = 0;

    private static final int MENU_ITEM_1 = 1;

    private static final int MENU_ITEM_2 = 2;

    private static final int MENU_ITEM_3 = 3;

    private static final int MENU_ITEM_4 = 4;

    private static final int MENU_ITEM_5 = 5;

    /**
     * Used to toggle if text instead of icons will be used in the option menu.
     */
    private boolean mTextMenu = false;

    Bundle[] mMenuItemsText = new Bundle[3];

    Bundle[] mMenuItemsIcons = new Bundle[3];

    /**
     * Defines the size of the touch area for one of the images used in the
     * bitmap UI
     */
    private Rect mTouchRect = new Rect(0, 89, 220, 176);

    /**
     * Creates a HelloEvents control object.
     *
     * @param hostAppPackageName Package name of host application.
     * @param context The context.
     * @param handler The handler to use.
     */
    HelloEventsControl(final String hostAppPackageName, final Context context, Handler handler) {
        super(context, hostAppPackageName);
        if (handler == null) {
            throw new IllegalArgumentException("handler == null");
        }

        initializeMenus();
    }

    /**
     * Prepares icons and text menu items for the SmartWatch 2 options menu.
     * For each menu item you define an ID and a label, or an icon.
     */
    private void initializeMenus() {
        mMenuItemsText[0] = new Bundle();
        mMenuItemsText[0].putInt(Control.Intents.EXTRA_MENU_ITEM_ID, MENU_ITEM_0);
        mMenuItemsText[0].putString(Control.Intents.EXTRA_MENU_ITEM_TEXT, "Item 1");

        mMenuItemsText[1] = new Bundle();
        mMenuItemsText[1].putInt(Control.Intents.EXTRA_MENU_ITEM_ID, MENU_ITEM_1);
        mMenuItemsText[1].putString(Control.Intents.EXTRA_MENU_ITEM_TEXT, "Item 2");

        mMenuItemsText[2] = new Bundle();
        mMenuItemsText[2].putInt(Control.Intents.EXTRA_MENU_ITEM_ID, MENU_ITEM_2);
        mMenuItemsText[2].putString(Control.Intents.EXTRA_MENU_ITEM_TEXT, "Item 3");

        mMenuItemsIcons[0] = new Bundle();
        mMenuItemsIcons[0].putInt(Control.Intents.EXTRA_MENU_ITEM_ID, MENU_ITEM_3);
        mMenuItemsIcons[0].putString(Control.Intents.EXTRA_MENU_ITEM_ICON,
                ExtensionUtils.getUriString(mContext, R.drawable.actions_call));

        mMenuItemsIcons[1] = new Bundle();
        mMenuItemsIcons[1].putInt(Control.Intents.EXTRA_MENU_ITEM_ID, MENU_ITEM_4);
        mMenuItemsIcons[1].putString(Control.Intents.EXTRA_MENU_ITEM_ICON,
                ExtensionUtils.getUriString(mContext, R.drawable.actions_reply));

        mMenuItemsIcons[2] = new Bundle();
        mMenuItemsIcons[2].putInt(Control.Intents.EXTRA_MENU_ITEM_ID, MENU_ITEM_5);
        mMenuItemsIcons[2].putString(Control.Intents.EXTRA_MENU_ITEM_ICON,
                ExtensionUtils.getUriString(mContext, R.drawable.actions_view_in_phone));
    }

    /**
     * Returns the width of the screen which this control extension supports.
     *
     * @param context The context.
     * @return The width in pixels.
     */
    public static int getSupportedControlWidth(Context context) {
        return context.getResources().getDimensionPixelSize(R.dimen.smart_watch_2_control_width);
    }

    /**
     * Returns the height of the screen which this control extension supports.
     *
     * @param context The context.
     * @return The height in pixels.
     */
    public static int getSupportedControlHeight(Context context) {
        return context.getResources().getDimensionPixelSize(R.dimen.smart_watch_2_control_height);
    }

    @Override
    public void onResume() {
        // Send a UI when the extension becomes visible.
        showLayout(R.layout.hello_events_control, null);
    }

    @Override
    public void onTouch(final ControlTouchEvent event) {
        Log.d(HelloEventsExtensionService.LOG_TAG, "onTouch() " + event.getAction());

        // The touch method can be used with both layouts and bitmaps. It is
        // necessary together with bitmaps because there is no reference to a
        // clicked view. When using bitmaps it is necessary to check if the
        // touch area is inside the view area.
        if (event.getAction() == Control.Intents.TOUCH_ACTION_RELEASE) {
            if (mTouchRect.contains(event.getX(), event.getY())) {
                sendEventToActivity(EventType.TOUCH, event.getX() + ", " + event.getY());
                startVibrator(500, 500, 2);
            }
        }
    }

    @Override
    public void onObjectClick(final ControlObjectClickEvent event) {
        Log.d(HelloEventsExtensionService.LOG_TAG, "onObjectClick() " + event.getClickType());

        // Check which view was clicked and then take the desired action.
        if (event.getLayoutReference() == R.id.btn_vibrate_1) {
            sendEventToActivity(EventType.OBJECT_CLICK, "layout: " + event.getLayoutReference());
            startVibrator(1000, 1000, 1);
        }
    }

    @Override
    public void onKey(final int action, final int keyCode, final long timeStamp) {
        Log.d(HelloEventsExtensionService.LOG_TAG, "onKey()");
        if (action == Control.Intents.KEY_ACTION_RELEASE
                && keyCode == Control.KeyCodes.KEYCODE_OPTIONS) {
            // When pressing the menu button on the SmartWatch 2, we toggle the
            // menu to switch between showing text or icon items.
            toggleMenu();
            sendEventToActivity(EventType.KEY_OPTIONS, "timestamp: " + timeStamp);
        } else if (action == Control.Intents.KEY_ACTION_RELEASE
                && keyCode == Control.KeyCodes.KEYCODE_BACK) {
            Log.d(HelloEventsExtensionService.LOG_TAG, "onKey() - back button intercepted.");
            sendEventToActivity(EventType.KEY_BACK, "timestamp: " + timeStamp);
            stopRequest();
        }
    }

    @Override
    public void onMenuItemSelected(final int menuItem) {
        Log.d(HelloEventsExtensionService.LOG_TAG, "onMenuItemSelected() - menu item " + menuItem);
        sendEventToActivity(EventType.MENU_ITEM_CLICK, "menuitem: " + menuItem);
    }

    /**
     * Toggles the use of icons or text in the options menu.
     */
    private void toggleMenu() {
        if (mTextMenu) {
            showMenu(mMenuItemsIcons);
        } else {
            showMenu(mMenuItemsText);
        }

        mTextMenu = !mTextMenu;
    }

    /**
     * Shows the event content in the phone activity. If the activity is closed,
     * it will be opened.
     *
     * @param type Type of event.
     * @param content Content of the event (depends on the type of event).
     */
    private void sendEventToActivity(EventType type, String content) {
        Intent intent = new Intent(HelloEventsActivity.ACTION_UPDATE_ACTIVITY);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        intent.putExtra(HelloEventsActivity.EXTRA_EVENT_TYPE, type.toString());
        intent.putExtra(HelloEventsActivity.EXTRA_EVENT_CONTENT, content);

        try {
            mContext.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e(HelloEventsExtensionService.LOG_TAG, e.toString());
        }
    }
}
