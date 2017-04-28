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

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static com.example.oleh.opengl2.GLErrorUtils.checkGlError;


class GLES20TriangleRenderer implements GLSurfaceView.Renderer, View.OnTouchListener {
    volatile int ang;

    public GLES20TriangleRenderer(Context context) {
        mContext = context;
        VertexArray array = new VertexArray();


        array.addPoint(-.5, -.5, -.5).addTextureCoordintes(0, 1);
        array.addPoint(-.5, .5, -.5).addTextureCoordintes(0, 0);
        array.addPoint(.5, .5, -.5).addTextureCoordintes(1, 0);
        array.addPoint(.5, -.5, -.5).addTextureCoordintes(1, 1);

        array.addPoint(.5, .5, .5).addTextureCoordintes(1, 0);
        array.addPoint(-.5, .5, .5).addTextureCoordintes(0, 0);
        array.addPoint(.5, -.5, .5).addTextureCoordintes(1, 1);
        array.addPoint(-.5, -.5, .5).addTextureCoordintes(0, 1);

        mTriangleVertices = ByteBuffer.allocateDirect(array.getArray().length
                * FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTriangleVertices.put(array.getArray()).position(0);
    }

    int counter = 0;

    public void onDrawFrame(GL10 glUnused) {
        // Ignore the passed-in GL10 interface, and use the GLES20
        // class's static methods instead.

        GLES20.glClearColor(1, 1, 1, 1.0f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glUseProgram(mProgram);
        checkGlError("glUseProgram");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureID);

        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
        GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false, TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
        checkGlError("glVertexAttribPointer maPosition");
        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        checkGlError("glEnableVertexAttribArray maPositionHandle");
        GLES20.glVertexAttribPointer(maTextureHandle, 2, GLES20.GL_FLOAT, false, TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
        checkGlError("glVertexAttribPointer maTextureHandle");
        GLES20.glEnableVertexAttribArray(maTextureHandle);
        checkGlError("glEnableVertexAttribArray maTextureHandle");

        long time = SystemClock.uptimeMillis() % 4000L;
        float angle = 0.090f * ((int) time);
//        float[] temp = new float[16];


//        Matrix.setRotateM(mMMatrix, 0, angle, 0, 1, 0);

//        Matrix.multiplyMM(mMMatrix,0,temp,0,mMMatrix,0);
        Matrix.setRotateM(mMMatrix, 0, ang, 1, 0, 0);

        Matrix.multiplyMM(mMVPMatrix, 0, mVMatrix, 0, mMMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVPMatrix, 0);

        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glLineWidth(5);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 4, 4);
        checkGlError("glDrawArrays");
    }

    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        // Ignore the passed-in GL10 interface, and use the GLES20
        // class's static methods instead.
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;
        Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }

    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        // Ignore the passed-in GL10 interface, and use the GLES20
        // class's static methods instead.
        TextureShader shader = new TextureShader();
        ColorShader colorShader = new ColorShader();
        mProgram = shader.getProgramId();
        maPositionHandle = shader.getVertexPosition();
        maTextureHandle = shader.getTextureCoordinate();
        muMVPMatrixHandle = shader.getUniformMatrix();


        Matrix.setIdentityM(mMMatrix, 0);


        /*
         * Create our texture. This has to be done each time the
         * surface is created.
         */

        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);

        mTextureID = textures[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureID);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

        InputStream is = mContext.getResources().openRawResource(R.raw.woodtexture1);
        Bitmap bitmap;
        try {
            bitmap = BitmapFactory.decodeStream(is);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                // Ignore.
            }
        }

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();

        Matrix.setLookAtM(mVMatrix, 0, 0, 0, -5, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
    }


    private static final int FLOAT_SIZE_BYTES = 4;
    private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
    private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
    private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;


    private FloatBuffer mTriangleVertices;


    private float[] mMVPMatrix = new float[16];
    private float[] mProjMatrix = new float[16];
    private float[] mMMatrix = new float[16];
    private float[] mVMatrix = new float[16];

    private int mProgram;
    private int mTextureID;
    private int muMVPMatrixHandle;
    private int maPositionHandle;
    private int maTextureHandle;

    private Context mContext;

    volatile MotionEvent first;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.v("Touch event", "works");
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onStart(event);
                break;
            case MotionEvent.ACTION_MOVE:
                onMove(event);
                break;
            case MotionEvent.ACTION_UP:
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

            ang = (int) eventDistance(first, event)[0];

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
