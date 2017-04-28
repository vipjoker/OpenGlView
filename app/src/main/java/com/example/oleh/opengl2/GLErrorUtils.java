package com.example.oleh.opengl2;

import android.opengl.GLES20;
import android.util.Log;

public abstract class GLErrorUtils {
    public static final String TAG = "OPEN_GL_ERROR";


    public static void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }


    public static void checkProgramError(int program) {
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            Log.e(TAG, "Could not link program: ");
            Log.e(TAG, GLES20.glGetProgramInfoLog(program));
            GLES20.glDeleteProgram(program);
            throw new RuntimeException("Shader program error");
        }
    }

    public static void checkShaderError(int shader, int shaderType){
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e(TAG, "Could not compile shader " + shaderType + ":");
            Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
           throw new RuntimeException(String.format("%s shader error " , shaderType == GLES20.GL_FRAGMENT_SHADER? "Fragment":"Vertex"));
        }
    }

}
