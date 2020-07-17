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

import android.util.Log;

import com.sonyericsson.extras.liveware.extension.util.ExtensionService;
import com.sonyericsson.extras.liveware.extension.util.registration.RegistrationInformation;
import com.sonyericsson.extras.liveware.extension.util.widget.BaseWidget;
import com.sonyericsson.extras.liveware.extension.util.widget.WidgetExtension;

/**
 * The Sample Extension Service handles registration and keeps track of all widgets
 * on all accessories.
 */
public class BackwardsCompatibleWidgetExtensionService extends ExtensionService {

    /**
     * Log tag
     */
    public static final String LOG_TAG = "SampleWidgetExtension";

    public BackwardsCompatibleWidgetExtensionService() {
        super();
    }

    /**
     * {@inheritDoc}
     *
     * @see android.app.Service#onCreate()
     */
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "SampleWidgetService: onCreate");
    }

    /**
     * {@inheritDoc}
     *
     * @see android.app.Service#onDestroy()
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");
    }

    @Override
    protected RegistrationInformation getRegistrationInformation() {
        return new BackwardsCompatibleWidgetRegistrationInformation(this);
    }

    @Override
    protected boolean keepRunningWhenConnected() {
        return false;
    }

    /*
     * This legacy method creates and returns a new instance of SampleWidget.
     * For Widget API 3 and up, this is not required as the class is initialized
     * automatically.
     * @see com.sonyericsson.extras.liveware.extension.util.ExtensionService#
     * createWidgetExtension(java.lang.String)
     */
    @Override
    public WidgetExtension createWidgetExtension(String hostAppPackageName) {
        return new BackwardsCompatibleWidget(new BaseWidget.WidgetBundle(this, hostAppPackageName,
                BaseWidget.NOT_SET));
    }

}
