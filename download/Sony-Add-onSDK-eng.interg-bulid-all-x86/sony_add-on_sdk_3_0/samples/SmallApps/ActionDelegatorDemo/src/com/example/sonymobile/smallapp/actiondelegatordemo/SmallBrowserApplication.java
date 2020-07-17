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
import com.sony.smallapp.SmallAppWindow;
import com.sony.smallapp.SmallApplication;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebIconDatabase;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class SmallBrowserApplication extends SmallApplication {
    private View mMinimizedView;

    @Override
    public void onCreate() {
        super.onCreate();

        /* Set the layout of the application */
        setContentView(R.layout.smallapp_main);

        /*
         * Set the layout displayed when the application is minimized.
         */
        mMinimizedView = LayoutInflater.from(this).inflate(R.layout.smallapp_minimized, null);
        setMinimizedView(mMinimizedView);
        setFaviconToMinimizedView(null);

        /* Set the title of the application to be displayed in the titlebar */
        setTitle(R.string.app_name_small_browser);

        SmallAppWindow.Attributes attr = getWindow().getAttributes();

        /* Set the requested width of the application */
        attr.width = getResources().getDimensionPixelSize(R.dimen.width);
        /* Set the requested height of the application */
        attr.height = getResources().getDimensionPixelSize(R.dimen.height);

        /* Set the window attributes to apply the changes above */
        getWindow().setAttributes(attr);

        WebView webView = (WebView) findViewById(R.id.webview);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

                setFaviconToMinimizedView(null);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                setFaviconToMinimizedView(view.getFavicon());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });

        WebIconDatabase.getInstance().open(getDir("icons", MODE_PRIVATE).getPath());
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (SdkInfo.VERSION.API_LEVEL >= 2) {
            String url = getIntent().getDataString();
            WebView webView = (WebView) findViewById(R.id.webview);
            webView.loadUrl(url);
        } else {
            Toast.makeText(this, R.string.api_not_supported,
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        WebIconDatabase.getInstance().close();
    }

    /*
     * Show the favicon of displayed web page. When the favicon is unavailable,
     * the name of application is shown.
     */
    private void setFaviconToMinimizedView(Bitmap icon) {
        ImageView faviconView = (ImageView) mMinimizedView.findViewById(R.id.favicon_view);
        TextView fallbackView = (TextView) mMinimizedView.findViewById(R.id.fallback_view);
        if (icon != null) {
            faviconView.setVisibility(View.VISIBLE);
            faviconView.setImageBitmap(icon);
            fallbackView.setVisibility(View.GONE);
        } else {
            faviconView.setVisibility(View.GONE);
            fallbackView.setVisibility(View.VISIBLE);
        }
    }
}
