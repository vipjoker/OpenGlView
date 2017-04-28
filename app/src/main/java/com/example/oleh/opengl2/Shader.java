package com.example.oleh.opengl2;

import android.opengl.GLES20;

import static com.example.oleh.opengl2.GLErrorUtils.checkGlError;


public abstract class Shader {

    private int programId;
    protected Shader(String vertex,String fragment){
        programId = createProgram(vertex,fragment);
    }


    public int getProgramId(){
        return programId;
    }



    private int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }

        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (pixelShader == 0) {
            return 0;
        }

        int program = GLES20.glCreateProgram();
        if (program != 0) {
            GLES20.glAttachShader(program, vertexShader);
            checkGlError("glAttachShader");
            GLES20.glAttachShader(program, pixelShader);
            checkGlError("glAttachShader");
            GLES20.glLinkProgram(program);
            GLErrorUtils.checkProgramError(program);

        }
        return program;
    }

    private   int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);

        if (shader != 0) {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            GLErrorUtils.checkShaderError(shader,shaderType);
        }
        return shader;
    }



    protected  int findAttribute(String name,IntFunction func){
        int handle = func.getLocation(programId, name);
        if (handle == -1) {
            throw new RuntimeException(String.format("Could not get %s",name));
        }
        return handle;
    }




    public interface IntFunction{
        int getLocation(int program,String name);
    }
}
