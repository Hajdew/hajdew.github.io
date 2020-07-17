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
 * @file MultiFunctionCameraApp.java
 *
 */

package com.sonymobile.android.camera.addon.multifunctioncameraapp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.sonymobile.camera.addon.capturingmode.CapturingModeSelector;

/**
 * This is a sample activity class for a Camera Add-on implementation
 * that demonstrates how to use the CapturingModeSelector.
 * In this class, concrete usage of the CapturingModeSelector is written
 * as well as including a common camera app use case.
 */
public class MultiFunctionCameraApp extends Activity implements View.OnClickListener, Camera.PictureCallback {
    private static final String TAG = MultiFunctionCameraApp.class.getSimpleName();

    // The following capturing mode name is registered to the Camera Add-on framework.
    // The name must match the same value as "name" attribute of "<mode>" tag of
    // "res/xml/sample_mode_attributes.xml".
    private static final String MODE_NAME = "multi_function_camera_app";

    // Value of pressed icon color filter.
    private static final int PRESSED_COLOR_FILTER = 0x66000000;

    private static boolean sBusy = false;

    private CapturingModeSelector mCapturingModeSelector;

    private OrientationEventListener mOrientationEventListener;

    private ImageButton mButton;
    private ImageButton mThumbnailButton;
    private ImageButton mCaptureButton;

    private TextView mTextView;

    private long mImageId = -1;
    private String mImageFileLocation = null;
    private long mImageDateTaken = 0;

    private long mVideoId = -1;
    private String mVideoFileLocation = null;
    private long mVideoDateTaken = 0;

    private String mThumbnailFilePath;
    private Uri mThumbnailUri;

    private CameraTask mCameraTask;

    private static final String STORAGE_PATH_PREFIX = "/DCIM/";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Determine if the camera is disabled because of a security policy
        if(isCameraDisabled(this)) {
            // Report an error
            return;
        }

        setContentView(R.layout.main);

        // Setup views
        mTextView = ((TextView)findViewById(R.id.text));
        mTextView.setText("Mode : " + MODE_NAME);

        mButton = ((ImageButton)findViewById(R.id.button));
        mButton.setImageResource(R.drawable.mode_icon);
        mButton.setOnClickListener(new ModeSelectorButtonClickListener());
        mButton.setOnTouchListener(new ModeSelectorButtonTouchListener());

        mThumbnailButton = ((ImageButton)findViewById(R.id.thumbnail_button));
        mThumbnailButton.setOnClickListener(this);
        mCaptureButton = ((ImageButton)findViewById(R.id.capture));
        mCaptureButton.setOnClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Open camera device and start preview.
        TextureView textureView = (TextureView)findViewById(R.id.surface);
        mCameraTask = new CameraTask();
        textureView.setSurfaceTextureListener(mCameraTask);
        mCameraTask.setup(getDisplayRotation());

        // Set a button which will open the CapturingModeSelector so that a
        // user can launch any mode of any camera add-on app, including the
        // SOMC camera app, by tapping a mode icon listed in the
        // CapturingModeSelector.

        // Check for the existence of Camera Add-on API implementation and
        // sufficient privileges.
        try {
            Class.forName("com.sonymobile.camera.addon.capturingmode.CapturingModeSelector");

            // Create a parent view for the capturing mode selector view.
            ViewGroup modeSelectorContainer = (ViewGroup)findViewById(R.id.modeselector_container);

            // Create a CapturingModeSelector
            mCapturingModeSelector = new CapturingModeSelector(this, modeSelectorContainer);

            // Set two listeners on the CapturingModeSelector
            mCapturingModeSelector.setOnModeSelectListener(new MyOnModeSelectListener());
            mCapturingModeSelector.setOnModeFinishListener(new MyOnModeFinishListener());
        } catch (ClassNotFoundException e) {
            // If the camera add-on library is not found (Camera add-on API not supported),
            // implement suitable exception handling.
            Log.w(TAG, "Camera add-on library not found. Handle the exception, eg. finish the activity.", e);
            finish();
        } catch(SecurityException e) {
            // If a SecurityException (Insufficient privileges) is caught,
            // implement suitable exception handling.
            Log.w(TAG, "Camera add-on permission not granted. Handle the exception.", e);
            showPopupReinstall();
        }
        setViewsVisibility(View.VISIBLE);
        setThumbnail();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Release the camera device
        if(mCameraTask != null) {
            mCameraTask.release();
            mCameraTask = null;
        }

        // Release the CapturingModeSelector
        if (mCapturingModeSelector != null) {
            mCapturingModeSelector.release();
            mCapturingModeSelector = null;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Setup orientation event listener
        mOrientationEventListener = new ExtendedOrientationEventListener(this);
        mOrientationEventListener.enable();
    }

    @Override
    protected void onStop() {
        // Release the orientation listener
        mOrientationEventListener.disable();
        mOrientationEventListener = null;

        super.onStop();
    }

    /**
     * Open CapturingModeSelector by pressing mode selector button in the UI.
     * Inform current mode name to camera add-on framework. The Camera add-on
     * framework will highlight an icon of a mode which is informed.
     */
    @Override
    public void onClick(View v) {
        Log.d(TAG, "View clicked: " + v);
        // If the view tapped is the thumbnail button, create an intent
        // to launch an image viewer
        if(v == findViewById(R.id.thumbnail_button)) {
            Log.d(TAG, "Thumbnail click!");
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    MimeTypeMap.getFileExtensionFromUrl(mThumbnailFilePath));
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setDataAndType(mThumbnailUri, mimeType);

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, R.string.no_image_viewer, Toast.LENGTH_LONG).show();
            }
        } else if (v == findViewById(R.id.capture)) {
            // If the view tapped is the capturing button, call takePicture
            // to capture the picture
            mCameraTask.takePicture(this);
        }
    }

    /**
     * When back is pressed while CapturingModeSelector is opened, close the
     * CapturingModeSelector. In all other cases, finish this activity.
     */
    @Override
    public void onBackPressed() {
        if (mCapturingModeSelector.isOpened()) {
            mCapturingModeSelector.close();
            setViewsVisibility(View.VISIBLE);
            setThumbnail();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Returns the rotation of the screen from its "natural" orientation in degrees.
     */
    private int getDisplayRotation() {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        return degrees;
    }

    private class ModeSelectorButtonTouchListener implements View.OnTouchListener {
        /**
         * According to the UI guidelines, the mode selector button should apply a color filter
         * to display a pressed state when pressed.
         */
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mButton.onTouchEvent(event);
            if (mButton.isPressed()) {
                mButton.setColorFilter(PRESSED_COLOR_FILTER);
            } else {
                mButton.clearColorFilter();
            }
            return true;
        }
    }

    private class ModeSelectorButtonClickListener implements View.OnClickListener {
        /**
         * When a user opens the CapturingModeSelector by tapping mode selector button in the UI,
         * pass the current mode name to the camera add-on framework.
         * The camera add-on framework will highlight the icon of the mode whose name was passed.
         */
        @Override
        public void onClick(View v) {
            if (mCapturingModeSelector != null) {
                // If the view tapped is the CapturingModeSelector button, hide the
                // mode name text view, buttons, open the CapturingModeSelector and
                // remove the color filter
                mCapturingModeSelector.open(MODE_NAME);
                setViewsVisibility(View.INVISIBLE);
                mButton.clearColorFilter();
            }
        }
    }


    /**
     * If a permission error occurs, prompt the user to reinstall the apk in
     * case the device SW has been updated
     */
    private void showPopupReinstall() {
        DialogFragment reinstallPopup = new AlertDialogFragment();
        reinstallPopup.show(getFragmentManager(), "reinstall_popup");
    }

    /**
     * DialogFragment class which will guide the user to Google Play when
     * a reinstall is required
     */
    public static final class AlertDialogFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    getActivity());

            // Set title
            alertDialogBuilder.setTitle(R.string.popup_permission_error_title);

            // Set dialog message
            alertDialogBuilder
                    .setMessage(R.string.popup_permission_error_message)
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int id) {
                                    Intent intent = new Intent(
                                            Intent.ACTION_VIEW);
                                    intent.setData(Uri
                                            .parse("market://details?id="
                                                    + getActivity()
                                                            .getPackageName()));
                                    startActivity(intent);
                                }
                            })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int id) {
                                    dialog.cancel();
                                    getActivity().finish();
                                }
                            });

            // Create the alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();
            setCancelable(false);
            return alertDialog;
        }

        @Override
        public void onStop() {
            // Dismiss the Google Play dialog when it is no longer visible
            Dialog dialog = getDialog();
            if (dialog != null) {
                dialog.dismiss();
            }
            super.onStop();
        }
    }

    /**
     * Sets the visibility of the UI elements
     */
    private void setViewsVisibility(int visibility) {
        mButton.setVisibility(visibility);
        mCaptureButton.setVisibility(visibility);
        mThumbnailButton.setVisibility(visibility);
        mTextView.setVisibility(visibility);
    }

    /**
     * Creates a cursor containing media items,
     * extracts the last one captured and displays it in a thumbnail view.
     * If there is no photo available, present a container image.
     */
    private void setThumbnail() {
        // Reset last values
        mImageDateTaken = 0;
        mVideoDateTaken = 0;

        // Latest media thumbnail requirement
        getLatestImage();
        getLatestVideo();

        // No video or image available on device
        if (mImageDateTaken == 0 && mVideoDateTaken == 0) {
            mThumbnailButton.setVisibility(View.INVISIBLE);
        } else {
            mThumbnailButton.setVisibility(View.VISIBLE);
            Bitmap bitmap = null;

            if (mImageDateTaken >= mVideoDateTaken) {
                bitmap = MediaStore.Images.Thumbnails.getThumbnail(getContentResolver(),
                        mImageId, MediaStore.Images.Thumbnails.MINI_KIND, null);
                mThumbnailFilePath = mImageFileLocation;
                Uri imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                mThumbnailUri = imageUri.buildUpon().appendPath(String.valueOf(mImageId)).build();
            } else {
                bitmap = MediaStore.Video.Thumbnails.getThumbnail(getContentResolver(),
                        mVideoId, MediaStore.Video.Thumbnails.MINI_KIND, null);
                mThumbnailFilePath = mVideoFileLocation;
                Uri videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                mThumbnailUri = videoUri.buildUpon().appendPath(String.valueOf(mVideoId)).build();
            }
            Log.v(TAG, "setThumbnail(), display thumbnail");
            mThumbnailButton.setImageBitmap(bitmap);
        }
    }

    /**
     * This is a convenience method to get the latest captured image from the
     * MediaStore
     */
    private void getLatestImage() {
        Log.v(TAG, "getLatestImage");
        Uri imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[]{MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.DATE_TAKEN};
        String order = MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC";
        String selection =  MediaStore.Images.ImageColumns.DATA + " LIKE '%" +
                STORAGE_PATH_PREFIX + "%'";

        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(imageUri, projection, selection, null, order);
            if (cursor != null && cursor.moveToFirst()) {
                mImageId = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.
                        ImageColumns._ID));
                mImageFileLocation = cursor.getString(cursor.getColumnIndex(MediaStore.
                        Images.ImageColumns.DATA));
                mImageDateTaken = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.
                        ImageColumns.DATE_TAKEN));
                Log.v(TAG, "getLatestImage, mImageId = " + mImageId);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * This is a convenience method to get the latest captured video from the
     * MediaStore
     */
    private void getLatestVideo() {
        Log.v(TAG, "getLatestVideo");
        Uri videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[] {MediaStore.Video.VideoColumns._ID,
                MediaStore.MediaColumns.DATA,
                MediaStore.Video.VideoColumns.DATE_TAKEN};
        String order = MediaStore.Video.VideoColumns.DATE_TAKEN + " DESC";
        String selection =  MediaStore.MediaColumns.DATA + " LIKE '%" + STORAGE_PATH_PREFIX + "%'";

        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(videoUri, projection, selection, null, order);
            if (cursor != null && cursor.moveToFirst()) {
                mVideoId = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.
                        VideoColumns._ID));
                mVideoFileLocation = cursor.getString(cursor.getColumnIndex(MediaStore.
                        MediaColumns.DATA));
                mVideoDateTaken = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.
                        VideoColumns.DATE_TAKEN));
                Log.v(TAG, "getLatestVideo, mVideoId = " + mVideoId);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Implementation of CapturingModeSelector.OnModeSelectListener.
     */
    private class MyOnModeSelectListener implements CapturingModeSelector.OnModeSelectListener {
        /**
         * onModeSelect(String modeName) is called when the current mode and the next mode are in
         * <UL>
         * <LI> the same package and the same activity.</LI>
         * </UL>
         * The next mode is specified by an argument, modeName. The modeName is
         * the name of the mode that has been selected by the user.
         */
        @Override
        public void onModeSelect(String modeName) {
            mCapturingModeSelector.close();
            setViewsVisibility(View.VISIBLE);
            setThumbnail();
        }
    }

    /**
     * Implementation of CapturingModeSelector.OnModeFinishListener.
     */
    private class MyOnModeFinishListener implements CapturingModeSelector.OnModeFinishListener {
        /**
         * onModeFinish() is called when current mode and the next mode are in
         * <UL>
         * <LI> same package and different activity.</LI>
         * <LI> different package and same activity.</LI>
         * <LI> different package and different activity.</LI>
         * </UL>
         * In other words, this is called when an activity of the current mode needs to finish.
         */
        @Override
        public void onModeFinish() {
            mCapturingModeSelector.close();
            finish();
        }
    }

    /**
     * In order to properly handle situations where the user rotates the device,
     * implement an OrientationEventListener and perform UI orientation updates
     * in onOrientationChanged().
     */
    private class ExtendedOrientationEventListener extends OrientationEventListener {
        public ExtendedOrientationEventListener(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(final int orientation) {
            // Do not update UI when orientation is unknown.  Or if mCapturingModeSelector is null then
            // activity has already been destroyed
            if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN || mCapturingModeSelector == null) {
                return;
            }

            // Update UI orientation.
            int rotation = (orientation + getDisplayRotation()) % 360;
            if (rotation < 30 || 150 < rotation) {
                mCapturingModeSelector.setUiOrientation(Configuration.ORIENTATION_LANDSCAPE);
                setViewsRotation(0);

            } else if (60 <= rotation && rotation < 120) {
                mCapturingModeSelector.setUiOrientation(Configuration.ORIENTATION_PORTRAIT);
                setViewsRotation(270);
            }
        }

        /**
         * Sets the rotation of all of the UI elements
         * @param degrees degrees to rotate
         */
        private void setViewsRotation(int degrees) {
            mButton.setRotation(degrees);
            mThumbnailButton.setRotation(degrees);
            mTextView.setRotation(degrees);
            mCaptureButton.setRotation(degrees);
        }
    }

    /** Determine if the device's camera has been disabled by the admin(s) and return the value */
    private boolean isCameraDisabled(Context context) {
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        if (dpm.getCameraDisabled(null)) {
            return true;
        }
        return false;
    }

    /**
     * Convenience method to generate a new file name for newly taken picture. This is just
     * an example and the actual add-on application(s) should take their own folder/file
     * naming conventions to generate folder/files for newly created media
     */
    private File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM),
                "MyCameraAddOnApp");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.e(TAG, "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());

        // Create media file with specific extension. Here is "JPEG" for example
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }

    /**
     * MediaScannerConnection::scanFile(Context context, String[] paths, String[] mimeTypes,
     *         MediaScannerConnection.OnScanCompletedListener callback) method provides a way
     * for applications to pass a newly created or downloaded media file to the media
     * scanner service. Media scanner service will read metadata from the file and add
     * the file to the Android media content provider.
     *
     * All Camera add-on applications are required to call this method in order to
     * add the newly taken picture to Media Content Provider database.
     */
    private void scanFile(String filePath) {
        MediaScannerConnection.scanFile(this, new String[]{filePath}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String file, Uri fileUri) {
                long imageId = Long.valueOf(fileUri.getLastPathSegment()).longValue();
                Log.d(TAG, "onScanCompleted() IN, file = " + file + ", fileUri = " +
                        fileUri.toString() + ", imageId = " + imageId);
                mHandler.post(mUIUpdateHandler);
            }
        });
    }

    /*
     * (non-Javadoc)
     * @see android.hardware.Camera.PictureCallback#onPictureTaken(byte[], android.hardware.Camera)
     */
    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Log.d(TAG, "onPictureTaken() IN ");

        File pictureFile = getOutputMediaFile();
        if (pictureFile == null) {
            return;
        }

        // NOTE: Handle the image rotation before saving the file.
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found: " + e.getMessage());
            return;
        } catch (IOException e) {
            Log.e(TAG, "Error accessing file: " + e.getMessage());
            return;
        }

        ScanThread tmp = new ScanThread(pictureFile.getAbsolutePath());
        tmp.start();
    }

    /**
     * Runs scanFile() to trigger camera framework scanning of new files, e.g.
     * when a new photo has been captured
     */
    private class ScanThread extends Thread {
        private String mFilePath;

        public ScanThread(String file) {
            super();
            mFilePath = file;
        }

        public void run() {
            scanFile(mFilePath);
        }
    }

    private final Runnable mUIUpdateHandler = new Runnable() {
        public void run() {
            mCameraTask.restartPreview();
            setThumbnail();

            // Clear busy flag
            sBusy = false;
        }
    };

    private final Handler mHandler = new Handler();

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            // Handle zoom in/out here and consume the key event
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_FOCUS) {
            // Handle camera focus and consume the key event
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_CAMERA && !sBusy) {
            // Mark busy flag
            sBusy = true;

            // Handle take picture
            mCameraTask.takePicture(this);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
            return true;
        }
        return false;
    }

}
