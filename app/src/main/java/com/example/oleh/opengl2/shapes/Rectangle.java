package com.example.oleh.opengl2.shapes;

import android.opengl.GLES20;

import java.nio.FloatBuffer;

import static com.example.oleh.opengl2.utils.GLErrorUtils.checkGlError;

/**
 * Created by oleh on 29.04.17.
 */

public class Rectangle extends Shape {
    int positionAttr,textureAttr;
    int textureId;
    public static final int BYTES_PER_FLOAT = 4;
    private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * BYTES_PER_FLOAT;
    private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
    private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;
    private FloatBuffer buffer;
    public Rectangle(int textureId, int positionAttr,int texturePos){

        super();


        this.textureId = textureId;
        this.positionAttr = positionAttr;
        this.textureAttr = texturePos;
        addPoint(0,0,     .5).addTextureCoordintes(0.5,0.5);
        addPoint(.5, .5,  .5).addTextureCoordintes(1, 0);
        addPoint(-.5, .5, .5).addTextureCoordintes(0, 0);
        addPoint(-.5, -.5,.5).addTextureCoordintes(0, 1);
        addPoint(.5, -.5, .5).addTextureCoordintes(1, 1);
        addPoint(.5, .5,  .5).addTextureCoordintes(1, 0);

        buffer = floatBuffer();

    }

    @Override
    public void draw() {

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

        buffer.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
        GLES20.glVertexAttribPointer(positionAttr, 3, GLES20.GL_FLOAT, false, TRIANGLE_VERTICES_DATA_STRIDE_BYTES, buffer);

        buffer.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
        GLES20.glEnableVertexAttribArray(positionAttr);

        GLES20.glVertexAttribPointer(textureAttr, 2, GLES20.GL_FLOAT, false, TRIANGLE_VERTICES_DATA_STRIDE_BYTES, buffer);
        GLES20.glEnableVertexAttribArray(textureAttr);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN,0,6);
    }
}
