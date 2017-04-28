package com.example.oleh.opengl2;

import android.opengl.GLES20;
import android.util.Log;

import static com.example.oleh.opengl2.GLErrorUtils.checkGlError;



public class TextureShader extends Shader{
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
