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

package com.example.sonymobile.smartextension.hellowidget;

import android.util.Log;

import com.sonyericsson.extras.liveware.extension.util.widget.BaseWidget;

/**
 * This demonstrates how to implement a simple text widget.
 */
class HelloWidget extends BaseWidget {

    public static final int WIDGET_WIDTH_CELLS = 5;

    public static final int WIDGET_HEIGHT_CELLS = 2;

    /**
     * Creates a widget extension.
     */
    public HelloWidget(WidgetBundle bundle) {
        super(bundle);
    }

    @Override
    public void onStartRefresh() {
        Log.d(HelloWidgetExtensionService.LOG_TAG, "startRefresh");
        // Send a UI when the widget is visible.
        showLayout(R.layout.layout_widget);
    }

    @Override
    public void onStopRefresh() {
        Log.d(HelloWidgetExtensionService.LOG_TAG, "stopRefesh");
    }

    @Override
    public void onTouch(final int type, final int x, final int y) {
        Log.d(HelloWidgetExtensionService.LOG_TAG, "onTouch() " + type);
    }

    @Override
    public int getWidth() {
        return (int)(mContext.getResources().getDimension(R.dimen.smart_watch_2_widget_cell_width) * WIDGET_WIDTH_CELLS);
    }

    @Override
    public int getHeight() {
        return (int)(mContext.getResources().getDimension(R.dimen.smart_watch_2_widget_cell_height) * WIDGET_HEIGHT_CELLS);
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
