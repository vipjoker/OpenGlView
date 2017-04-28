/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.example.oleh.opengl2;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TextureView;


/**
 * This sample shows how to check for OpenGL ES 2.0 support at runtime, and then
 * use either OpenGL ES 1.0 or OpenGL ES 2.0, as appropriate.
 */
public class GLES20Activity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GLES20TriangleRenderer gles20TriangleRenderer = new GLES20TriangleRenderer(this);
        mGLSurfaceView = new CustomGLSurfaceView(this,gles20TriangleRenderer);

        if (detectOpenGLES20()) {
            // Tell the surface view we want to create an OpenGL ES 2.0-compatible
            // context, and set an OpenGL ES 2.0-compatible renderer.

            mGLSurfaceView.setEGLContextClientVersion(2);
            mGLSurfaceView.setRenderer(gles20TriangleRenderer);

        }
        setContentView(mGLSurfaceView);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.v("activity","touch");
        mGLSurfaceView.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    private boolean detectOpenGLES20() {
        ActivityManager am =
            (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo info = am.getDeviceConfigurationInfo();
        return (info.reqGlEsVersion >= 0x20000);
    }

    @Override
    protected void onResume() {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onResume();
        mGLSurfaceView.onResume();
    }



    @Override
    protected void onPause() {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onPause();
        mGLSurfaceView.onPause();
    }

    private CustomGLSurfaceView mGLSurfaceView;


    private class CustomGLSurfaceView extends GLSurfaceView{
        private OnTouchListener mListener;
        public CustomGLSurfaceView(Context context,OnTouchListener listener) {
            super(context);
            this.mListener = listener;
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            Log.v("surface","Touch");
            mListener.onTouch(this,event);
            return super.onTouchEvent(event);
        }
    }
}
