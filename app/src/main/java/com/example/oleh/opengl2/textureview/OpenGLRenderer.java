package com.example.oleh.opengl2.textureview;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import com.example.oleh.opengl2.R;
import com.example.oleh.opengl2.shaders.Shader;
import com.example.oleh.opengl2.thirdParty.gles.GlUtil;
import com.example.oleh.opengl2.utils.ArrayBuilder;
import com.example.oleh.opengl2.utils.CameraHandler;
import com.example.oleh.opengl2.utils.ResourceLoader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;

/**
 * Created by oleh on 11/8/17.
 */

public class OpenGLRenderer implements TextureViewRenderer.OpenGlCallback,CameraHandler {
    private static final String TAG = "TextureViewRenderer";
    float[] points;
    private volatile float eyeX = 0;
    private volatile float eyeY = 0;
    private volatile float eyeZ = -5;


    float[] projectionM = new float[16];
    float[] resultMatrix = new float[16];
    float[] lookAtMatrix = new float[16];
    private Context mContext;
    FloatBuffer buffer;

    int program;
    int a_color;
    int a_position;
    int u_matrix;

    public OpenGLRenderer(Context context) {
        this.mContext = context;
        ArrayBuilder builder = new ArrayBuilder();

        builder.addPointWithColor(0, 1,   2f, 1, 0, 0);
        builder.addPointWithColor(1, -1,  2f, 0, 1, 0);
        builder.addPointWithColor(-1, -1, 2f, 0, 0, 1);

        builder.addPointWithColor(0, 1,   -2f, 1, 0, 0);
        builder.addPointWithColor(1, -1,  -2f, 0, 1, 0);
        builder.addPointWithColor(-1, -1, -2f, 0, 0, 1);
        //x
        builder.addPointWithColor(-5, 0, 0, 1, 0, 0);
        builder.addPointWithColor( 5, 0, 0, 1, 0, 0);

        //y
        builder.addPointWithColor(0, -5, 0, 0, 1, 0);
        builder.addPointWithColor(0, 5, 0, 0, 1, 0);
        //z
        builder.addPointWithColor(0, 0, -5, 0, 0, 1);
        builder.addPointWithColor(0, 0, 5, 0, 0, 1);




        points = builder.build();
        Log.i("ARRAY", "OpenGLRenderer: " + Arrays.toString(points));

        buffer = GlUtil.createFloatBuffer(points);
    }

    @Override
    public void onStarted(int width, int height) {
        GLES20.glClearColor(0, 0, 0, 1.0f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        String fragShader = ResourceLoader.loadRawResource(mContext, R.raw.simple_fragment_shader);
        String vertShader = ResourceLoader.loadRawResource(mContext, R.raw.simple_vertex_shader);
        program = GlUtil.createProgram(vertShader, fragShader);

        a_position = GLES20.glGetAttribLocation(program, "a_Position");
        a_color = GLES20.glGetAttribLocation(program, "a_Color");
        u_matrix = GLES20.glGetUniformLocation(program, "mvp");
        GLES20.glUseProgram(program);


    }

    @Override
    public void onDraw(long delta) {

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);



        Matrix.setLookAtM(lookAtMatrix, 0, eyeX, eyeY, eyeZ, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        Matrix.frustumM(projectionM, 0, -1,1,-1,1,1,100);
        Matrix.multiplyMM(resultMatrix, 0, projectionM, 0, lookAtMatrix, 0);
        GLES20.glUniformMatrix4fv(u_matrix, 1, false, resultMatrix, 0);


        buffer.position(0);

        GLES20.glVertexAttribPointer(a_position, 3, GLES20.GL_FLOAT, false, 4 * 6, buffer);
        GLES20.glEnableVertexAttribArray(a_position);

        buffer.position(3);
        GLES20.glVertexAttribPointer(a_color, 3, GLES20.GL_FLOAT, false, 4 * 6, buffer);
        GLES20.glEnableVertexAttribArray(a_color);
        GLES20.glLineWidth(5);

        GLES20.glDrawArrays(GLES20.GL_LINES, 6, 2);
        GLES20.glDrawArrays(GLES20.GL_LINES, 8, 2);
        GLES20.glDrawArrays(GLES20.GL_LINES, 10, 2);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES,  0, 3);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 3, 3);





    }

    @Override
    public void onStopped() {

    }


    @Override
    public void setX(float x) {
        eyeX = x;
    }

    @Override
    public void setY(float y) {
        eyeY = y;
    }

    @Override
    public void setZ(float z) {
        eyeZ = z;
    }

    @Override
    public float getX() {
        return eyeX;
    }

    @Override
    public float getY() {
        return eyeY;
    }

    @Override
    public float getZ() {
        return eyeZ;
    }
}
