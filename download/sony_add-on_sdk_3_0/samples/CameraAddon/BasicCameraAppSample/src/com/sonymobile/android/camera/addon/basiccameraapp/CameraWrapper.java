/*
 * Copyright (c) 2013, Sony Mobile Communications AB
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
 *
 * NOTE: This file contains code from:
 *
 *     http://developer.android.com/reference/android/hardware/Camera.html
 *
 * Copyright (C) 2013 The Android Open Source Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @file CameraWrapper.java
 *
 */

package com.sonymobile.android.camera.addon.basiccameraapp;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.util.Log;
import android.view.TextureView;

import java.io.IOException;

/**
 * This class wraps processes which operate a camera device
 * for using it easily.
 */
public class CameraWrapper implements TextureView.SurfaceTextureListener {

    private static final String TAG = CameraWrapper.class.getSimpleName();

    private static final int OPEN_CAMERA_RETRY_MAX = 5;

    private static final int INTERVAL_OPEN_CAMERA = 500;

    private Camera mCamera;

    /** Open camera device and start preview. */
    public void setup(int displayRotation) {
        Log.d(TAG, "setup");

        int cameraId = 0;

        mCamera = getCameraInstance(cameraId);
        if (mCamera != null) {
            int displayOrientation = getCameraDisplayOrientation(
                    displayRotation, cameraId);
            mCamera.setDisplayOrientation(displayOrientation);
            // The TextureView class introduces some limitations when rendering
            // the camera preview on the Android emulator. The emulator shows
            // only a black screen and it does not draw the camera preview.
            // However, this logic should work as expected on real Android
            // devices.
            mCamera.startPreview();
        } else {
            Log.e(TAG, "Camera is not available.");
        }
    }

    /** Initialize and return a camera instance. */
    private static Camera getCameraInstance(int cameraId) {
        Camera camera = null;

        // Exceptions may be thrown when trying to obtain a Camera reference,
        // e.g. the Camera is currently in use by another application or it is not supported in HW.
        for (int i = 0; i < OPEN_CAMERA_RETRY_MAX; i++) {
            // Try to open camera OPEN_CAMERA_RETRY_MAX times at most.
            try {
                camera = Camera.open(cameraId);
                break;
            } catch (RuntimeException e) {
                Log.w(TAG, "Camera is used by another process.");
                // Wait and then retry.
                try {
                    Thread.sleep(INTERVAL_OPEN_CAMERA);
                } catch (InterruptedException ie) {
                    Log.d(TAG,"Retry to open the camera device.");
                }
            }
        }
        return camera;
    }

    /** Determine and return the current camera display orientation */
    private static int getCameraDisplayOrientation(int displayRotation, int cameraId) {
        CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + displayRotation) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - displayRotation + 360) % 360;
        }

        return result;
    }

    /** Release the camera device. */
    public void release() {
        Log.d(TAG, "release");

        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.d(TAG, "surfaceAvailable");

        if (mCamera != null) {
            try {
                mCamera.setPreviewTexture(surface);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Do nothing. camera.open() failed or camera has already been released.
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.d(TAG, "surfaceDestroyed");
        return true;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.d(TAG, "surfaceSizeChanged");
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        Log.d(TAG, "surfaceUpdated");
    }
}