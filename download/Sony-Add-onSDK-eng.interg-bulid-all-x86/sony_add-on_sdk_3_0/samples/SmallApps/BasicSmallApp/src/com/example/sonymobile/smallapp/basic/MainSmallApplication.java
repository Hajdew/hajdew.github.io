/*
 * Copyright 2011, 2012 Sony Corporation
 * Copyright (C) 2012-2013 Sony Mobile Communications AB.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the names of the Sony Corporation / the Sony Mobile
 *       Communications AB nor the names of their contributors may be used
 *       to endorse or promote products derived from this software without
 *       specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.example.sonymobile.smallapp.basic;

import com.sony.smallapp.SmallAppWindow;
import com.sony.smallapp.SmallApplication;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

public class MainSmallApplication extends SmallApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        /* Set the layout of the application */
        setContentView(R.layout.main);

        /*
         * Set the layout displayed when the application is minimized.
         * Calling this method is optional. If this is not called, application
         * icon is displayed (if it exists) when the Small App is minimized.
         */
        setMinimizedView(R.layout.minimized);

        /* Set the title of the application to be displayed in the titlebar */
        setTitle(R.string.app_name);

        SmallAppWindow.Attributes attr = getWindow().getAttributes();

        /* Set the requested width of the application */
        attr.width = getResources().getDimensionPixelSize(R.dimen.width);
        /* Set the requested height of the application */
        attr.height = getResources().getDimensionPixelSize(R.dimen.height);

        /*
         * Set the minimum width of the application, if it's resizable.
         *
         * If you don't have strong intention to specify minimum window size,
         * it is preferable not to set minimum window size.
         * If you still want to specify the minimum size, set as small value as possible
         * to make your application work properly on the devices with small screens.
         */
//      attr.minWidth = getResources().getDimensionPixelSize(R.dimen.min_width);
        /* Set the minimum height of the application, if it's resizable */
//      attr.minHeight = getResources().getDimensionPixelSize(R.dimen.min_height);

        /* Use this flag to make the application window resizable */
        attr.flags |= SmallAppWindow.Attributes.FLAG_RESIZABLE;
        /* Use this flag to remove the titlebar from the window */
//      attr.flags |= SmallAppWindow.Attributes.FLAG_NO_TITLEBAR;
        /* Use this flag to enable hardware accelerated rendering */
//      attr.flags |= SmallAppWindow.Attributes.FLAG_HARDWARE_ACCELERATED;

        /* Set the window attributes to apply the changes above */
        getWindow().setAttributes(attr);

        setupOptionMenu();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void setupOptionMenu() {
        View header = LayoutInflater.from(this).inflate(R.layout.header, null);

        final View optionMenu = header.findViewById(R.id.option_menu);
        optionMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(MainSmallApplication.this, optionMenu);
                popup.getMenuInflater().inflate(R.menu.menus, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        Toast.makeText(MainSmallApplication.this,
                                R.string.menu_clicked, Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });
                popup.show();
            }
        });

        /* Deploy the option menu in the header area of the titlebar */
        getWindow().setHeaderView(header);
    }
}
