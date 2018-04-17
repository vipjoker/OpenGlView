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
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TextureView;

import java.util.Arrays;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

import static android.hardware.SensorManager.SENSOR_DELAY_FASTEST;
import static android.hardware.SensorManager.SENSOR_DELAY_NORMAL;
import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_LOW;
import static java.lang.Math.sqrt;


/**
 * This sample shows how to check for OpenGL ES 2.0 support at runtime, and then
 * use either OpenGL ES 1.0 or OpenGL ES 2.0, as appropriate.
 */
public class GLES20Activity extends Activity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mSensor;
    // Create a constant to convert nanoseconds to seconds.
    private static final float NS2S = 1.0f / 1000000000.0f;
    private final float[] deltaRotationVector = new float[4];
    private float timestamp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



//        initSensors();
        GLES20TriangleRenderer gles20TriangleRenderer = new GLES20TriangleRenderer(this);
        mGLSurfaceView = new CustomGLSurfaceView(this,gles20TriangleRenderer);

        if (detectOpenGLES20()) {
            // Tell the surface view we want to create an OpenGL ES 2.0-compatible
            // context, and set an OpenGL ES 2.0-compatible renderer.

            mGLSurfaceView.setEGLContextClientVersion(2);
            mGLSurfaceView.setEGLConfigChooser(new MyConfigChooser());
            mGLSurfaceView.setRenderer(gles20TriangleRenderer);

        }
        setContentView(R.layout.activity_main);

        TextureView view = (TextureView)findViewById(R.id.textureView);

        view.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

                GLES20.glViewport(0, 0, width, height);
                GLES20.glEnable(GLES20.GL_ALIASED_LINE_WIDTH_RANGE);
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                GLES20.glClearColor(1, 0, 0, 1.0f);
                GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

            }
        });


    }


    private void initSensors(){

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorManager.registerListener(this,mSensor,SENSOR_DELAY_FASTEST);
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

    @Override
    public void onSensorChanged(SensorEvent event) {
        // This timestep's delta rotation to be multiplied by the current rotation
        // after computing it from the gyro sample data.



        if(event.values[2] > 0.5f){
            getWindow().getDecorView().setBackgroundColor(Color.RED);
        }if(event.values[2] < -0.5f){
            getWindow().getDecorView().setBackgroundColor(Color.YELLOW);
        }



//
//        if (timestamp != 0) {
//            final float dT = (event.timestamp - timestamp) * NS2S;
//            // Axis of the rotation sample, not normalized yet.
//            float axisX = event.values[0];
//            float axisY = event.values[1];
//            float axisZ = event.values[2];
//
//            // Calculate the angular speed of the sample
//            float omegaMagnitude = (float) sqrt(axisX*axisX + axisY*axisY + axisZ*axisZ);
//
//            // Normalize the rotation vector if it's big enough to get the axis
//            // (that is, EPSILON should represent your maximum allowable margin of error)
//            if (omegaMagnitude > 1) {
//                axisX /= omegaMagnitude;
//                axisY /= omegaMagnitude;
//                axisZ /= omegaMagnitude;
//            }
//
//            // Integrate around this axis with the angular speed by the timestep
//            // in order to get a delta rotation from this sample over the timestep
//            // We will convert this axis-angle representation of the delta rotation
//            // into a quaternion before turning it into the rotation matrix.
//            float thetaOverTwo = omegaMagnitude * dT / 2.0f;
//            float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
//            float cosThetaOverTwo = (float) Math.cos(thetaOverTwo);
//            deltaRotationVector[0] = sinThetaOverTwo * axisX;
//            deltaRotationVector[1] = sinThetaOverTwo * axisY;
//            deltaRotationVector[2] = sinThetaOverTwo * axisZ;
//            deltaRotationVector[3] = cosThetaOverTwo;
//        }
//        timestamp = event.timestamp;
//        float[] deltaRotationMatrix = new float[9];
//        SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector);

        // User code should concatenate the delta rotation we computed with the current rotation
        // in order to get the updated rotation.
        // rotationCurrent = rotationCurrent * deltaRotationMatrix;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


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




    class MyConfigChooser implements GLSurfaceView.EGLConfigChooser {
        @Override
        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
            int attribs[] = {
                    EGL10.EGL_LEVEL, 0,
                    EGL10.EGL_RENDERABLE_TYPE, 4,  // EGL_OPENGL_ES2_BIT
                    EGL10.EGL_COLOR_BUFFER_TYPE, EGL10.EGL_RGB_BUFFER,
                    EGL10.EGL_RED_SIZE, 8,
                    EGL10.EGL_GREEN_SIZE, 8,
                    EGL10.EGL_BLUE_SIZE, 8,
                    EGL10.EGL_DEPTH_SIZE, 16,
                    EGL10.EGL_SAMPLE_BUFFERS, 1,
                    EGL10.EGL_SAMPLES, 4,  // This is for 4x MSAA.
                    EGL10.EGL_NONE
            };
            EGLConfig[] configs = new EGLConfig[1];
            int[] configCounts = new int[1];
            egl.eglChooseConfig(display, attribs, configs, 1, configCounts);

            if (configCounts[0] == 0) {
                // Failed! Error handling.
                return null;
            } else {
                return configs[0];
            }
        }
    }

}
