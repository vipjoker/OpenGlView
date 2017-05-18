package com.example.oleh.opengl2.shaders;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.example.oleh.opengl2.R;
import com.example.oleh.opengl2.shaders.Shader;

import java.io.IOException;
import java.io.InputStream;


public class TextureShader extends Shader {
    private int programId;
    private static final String mVertexShader =
            "uniform mat4 uMVPMatrix;\n" +
                    "attribute vec4 aPosition;\n" +
                    "attribute vec2 aTextureCoord;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "void main() {\n" +
                    "  gl_Position = uMVPMatrix * aPosition;\n" +
                    "  vTextureCoord = aTextureCoord;\n" +
                    "}\n";

    private static final String mFragmentShader =
            "precision mediump float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform sampler2D sTexture;\n" +
                    "void main() {\n" +
                    "  gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                    "}\n";
    public TextureShader(){
        super(mVertexShader,mFragmentShader);
    }

    public int  prepareTexture(Context context){
           /*
         * Create our texture. This has to be done each time the
         * surface is created.
         */

        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);

        int  mTextureID = textures[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureID);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

        InputStream is = context.getResources().openRawResource(R.raw.woodtexture1);
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
        return mTextureID;
    }


    public int getVertexPosition(){
            return findAttribute("aPosition", new IntFunction() {
                @Override
                public int getLocation(int program, String name) {
                   return GLES20.glGetAttribLocation(program,name);
                }});
    }

    public int getTextureCoordinate(){
        return findAttribute("aTextureCoord", new IntFunction() {
            @Override
            public int getLocation(int program, String name) {
                return GLES20.glGetAttribLocation(program,name);
            }
        });
    }

    public int getUniformMatrix(){
        return findAttribute("uMVPMatrix", new IntFunction() {
            @Override
            public int getLocation(int program, String name) {
                return GLES20.glGetUniformLocation(program,name);
            }
        });
    }


}
