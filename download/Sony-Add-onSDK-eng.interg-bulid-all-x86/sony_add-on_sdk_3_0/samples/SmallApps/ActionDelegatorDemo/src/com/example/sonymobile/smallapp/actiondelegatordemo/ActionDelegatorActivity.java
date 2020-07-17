/*
 * Copyright (C) 2014 Sony Mobile Communications AB.
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

package com.example.sonymobile.smallapp.actiondelegatordemo;

import com.sony.smallapp.SdkInfo;
import com.sony.smallapp.SmallAppNotFoundException;
import com.sony.smallapp.SmallApplicationManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

/*
 * This activity delegates the handling of the android.intent.action.VIEW action
 * to the Small Browser implemented in the SmallBrowserApplication class.
 * With this delegator activity, the Small Browser can be launched from any
 * application that requests to show a web page.
 */
public class ActionDelegatorActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (SdkInfo.VERSION.API_LEVEL >= 2) {
            Intent intent = new Intent(getIntent());
            intent.setClass(this, SmallBrowserApplication.class);
            try {
                // Show specified web pages on Small Browser
                SmallApplicationManager.startApplication(this, intent);
            } catch (SmallAppNotFoundException e) {
            }
        } else {
            Toast.makeText(this, R.string.api_not_supported,
                    Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
