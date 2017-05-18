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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import com.example.oleh.opengl2.shaders.ColorShader;
import com.example.oleh.opengl2.shaders.TextureShader;
import com.example.oleh.opengl2.shapes.Rectangle;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


class GLES20TriangleRenderer implements GLSurfaceView.Renderer, View.OnTouchListener {
    AccelerateInterpolator interpolator;
    Rectangle rectangle;

    public GLES20TriangleRenderer(Context context) {
        interpolator = new AccelerateInterpolator();
        mContext = context;
    }

    public void onDrawFrame(GL10 glUnused) {

        GLES20.glClearColor(1, 1, 1, 1.0f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glUseProgram(mProgram);

        long time = SystemClock.uptimeMillis() % 4000L;
        float angle = 0.090f * ((int) time);

        Matrix.setRotateM(mMMatrix, 0, angle, 0, 1, 0);

        Matrix.multiplyMM(mMVPMatrix, 0, mVMatrix, 0, mMMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVPMatrix, 0);

        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glLineWidth(5);

        rectangle.draw();
    }

    public void onSurfaceChanged(GL10 glUnused, int width, int height) {

        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;
        Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }

    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {

        TextureShader shader = new TextureShader();

        ColorShader colorShader = new ColorShader();
        mProgram = shader.getProgramId();
        colorProgram = colorShader.getProgramId();
        maPositionHandle = shader.getVertexPosition();
        maTextureHandle = shader.getTextureCoordinate();
        muMVPMatrixHandle = shader.getUniformMatrix();
        mTextureID = shader.prepareTexture(mContext);


        circleColor = colorShader.getColorLocation();
        circlePos = colorShader.getPositionLocation();
        rectangle = new Rectangle(mTextureID, maPositionHandle, maTextureHandle);
        Matrix.setIdentityM(mMMatrix, 0);


        Matrix.setLookAtM(mVMatrix, 0, 0, 0, -5, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
    }


    private float[] mMVPMatrix = new float[16];
    private float[] mProjMatrix = new float[16];
    private float[] mMMatrix = new float[16];
    private float[] mVMatrix = new float[16];

    private int mProgram;
    private int colorProgram;
    private int mTextureID;
    private int muMVPMatrixHandle;
    private int maPositionHandle;
    private int maTextureHandle;

    private int circlePos;
    private int circleColor;

    private Context mContext;

    volatile MotionEvent first;

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onStart(event);
                break;
            case MotionEvent.ACTION_MOVE:
                onMove(event);
                break;
            case MotionEvent.ACTION_UP:
                Log.v("POSITION", String.format("x = %f y = %f", event.getX(), event.getY()));
                onEnd(event);
                break;
        }

        return true;
    }

    private void onStart(MotionEvent event) {
        first = event;
    }

    private void onMove(MotionEvent event) {
        if (first != null) {

            float[] floats = eventDistance(first, event);

        }
    }


    private void onEnd(MotionEvent event) {
        first = null;
    }

    private float[] eventDistance(MotionEvent start, MotionEvent end) {
        float[] result = new float[2];
        result[0] = end.getX() - start.getY();
        result[1] = end.getY() - start.getY();

        return result;

    }

}
