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

package com.example.sonymobile.smartextension.helloevents;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

/**
 * The HelloEventsActivity presents data on the phone based on user triggered
 * events.
 */
public class HelloEventsActivity extends Activity {

    public final static String EXTRA_EVENT_TYPE = "EXTRA_EVENT_TYPE";

    public final static String EXTRA_EVENT_CONTENT = "EXTRA_EVENT_CONTENT";

    public final static String ACTION_UPDATE_ACTIVITY = "com.example.sonymobile.smartextension.helloevents.ACTION_UPDATE_ACTIVITY";

    private TextView mTextViewType;

    private TextView mTextViewContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hello_events_activity);

        mTextViewType = (TextView)findViewById(R.id.txt_event_type);
        mTextViewContent = (TextView)findViewById(R.id.txt_event_content);

        Intent intent = getIntent();
        updateValues(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        updateValues(intent);
    }

    /**
     * Extracts the user event type and content and presents them in an activity.
     *
     * @param intent The intent with the data to present.
     */
    private void updateValues(Intent intent) {
        if (TextUtils.equals(intent.getAction(), ACTION_UPDATE_ACTIVITY)) {
            String type = intent.getStringExtra(EXTRA_EVENT_TYPE);
            String content = intent.getStringExtra(EXTRA_EVENT_CONTENT);

            mTextViewType.setText(type);
            mTextViewContent.setText(content);
        }
    }
}
