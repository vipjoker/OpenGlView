package com.example.oleh.opengl2.opengl3;

import android.content.Context;
import android.opengl.GLES30;
import android.util.Log;

import androidx.annotation.RawRes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class ShaderUtil {

    private static final String TAG = "ShaderUtil";

    private ShaderUtil() {
        // Utility class
    }

    /**
     * Reads shader source files from raw resources, compiles, and links them into a GL program.
     *
     * @param context            Android context to access resources
     * @param vertexRawResId     Resource ID for the vertex shader (e.g. R.raw.vertex_shader)
     * @param fragmentRawResId   Resource ID for the fragment shader (e.g. R.raw.fragment_shader)
     * @return Linked OpenGL Program ID, or 0 if linking/compilation failed.
     */
    public static int createProgram(Context context, @RawRes int vertexRawResId, @RawRes int fragmentRawResId) {
        String vertexSource = readRawResource(context, vertexRawResId);
        String fragmentSource = readRawResource(context, fragmentRawResId);

        if (vertexSource == null || fragmentSource == null) {
            Log.e(TAG, "Failed to load shader source strings from resources.");
            return 0;
        }

        return createProgram(vertexSource, fragmentSource);
    }

    /**
     * Compiles vertex and fragment shaders from source strings and links them into a program.
     */
    public static int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = compileShader(GLES30.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }

        int fragmentShader = compileShader(GLES30.GL_FRAGMENT_SHADER, fragmentSource);
        if (fragmentShader == 0) {
            GLES30.glDeleteShader(vertexShader);
            return 0;
        }

        int program = GLES30.glCreateProgram();
        if (program == 0) {
            Log.e(TAG, "Could not create OpenGL program object.");
            GLES30.glDeleteShader(vertexShader);
            GLES30.glDeleteShader(fragmentShader);
            return 0;
        }

        GLES30.glAttachShader(program, vertexShader);
        GLES30.glAttachShader(program, fragmentShader);
        GLES30.glLinkProgram(program);

        final int[] linkStatus = new int[1];
        GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, linkStatus, 0);

        if (linkStatus[0] != GLES30.GL_TRUE) {
            String infoLog = GLES30.glGetProgramInfoLog(program);
            Log.e(TAG, "Failed to link shader program: " + infoLog);
            GLES30.glDeleteProgram(program);
            program = 0;
        }

        // Shaders flagged for deletion once linked; GPU retains them in the program.
        GLES30.glDeleteShader(vertexShader);
        GLES30.glDeleteShader(fragmentShader);

        return program;
    }

    /**
     * Compiles an individual shader (Vertex or Fragment).
     */
    public static int compileShader(int type, String source) {
        int shader = GLES30.glCreateShader(type);
        if (shader == 0) {
            Log.e(TAG, "Could not create shader of type: " + type);
            return 0;
        }

        GLES30.glShaderSource(shader, source);
        GLES30.glCompileShader(shader);

        final int[] compileStatus = new int[1];
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compileStatus, 0);

        if (compileStatus[0] != GLES30.GL_TRUE) {
            String infoLog = GLES30.glGetShaderInfoLog(shader);
            Log.e(TAG, "Compilation error in shader type (" + type + "):\n" + infoLog);
            GLES30.glDeleteShader(shader);
            return 0;
        }

        return shader;
    }

    /**
     * Reads a raw resource file into a String UTF-8.
     */
    public static String readRawResource(Context context, @RawRes int rawResId) {
        StringBuilder sb = new StringBuilder();
        try (InputStream is = context.getResources().openRawResource(rawResId);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            Log.e(TAG, "Error reading raw resource: " + rawResId, e);
            return null;
        }
        return sb.toString();
    }
}