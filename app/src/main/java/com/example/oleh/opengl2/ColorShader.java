package com.example.oleh.opengl2;


import android.opengl.GLES20;

public class ColorShader extends Shader {

    private static final String mVertexShader =
                    "uniform mat4 uMVPMatrix;\n" +
                    "attribute vec4 aPosition;\n" +
                    "void main() {\n" +
                    "  gl_Position = uMVPMatrix * aPosition;\n" +
                    "}\n";

    private static final String mFragmentShader =
            "precision mediump float;\n" +
                    "uniform vec3 uColor;\n" +
                    "void main() {\n" +
                    "  gl_FragColor = vec4(uColor,1);\n" +
                    "}\n";

    public ColorShader() {
        super(mVertexShader, mFragmentShader);
    }

    public int getColorLocation(){
        return findAttribute("uColor", new IntFunction() {
            @Override
            public int getLocation(int program, String name) {
                return GLES20.glGetUniformLocation(program,name);
            }
        });
    }

    public int getPositionLocation(){
        return findAttribute("aPosition", new IntFunction() {
            @Override
            public int getLocation(int program, String name) {
                return GLES20.glGetAttribLocation(program,name);
            }
        });
    }

}
