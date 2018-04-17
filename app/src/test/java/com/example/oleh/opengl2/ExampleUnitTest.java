package com.example.oleh.opengl2;

import android.opengl.Matrix;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void matrixTest(){
        float [] vector = {.5f,.5f,.5f,1};
        float [] result = {0,0,0,0};
        float [] matrix = new float[16];
        Matrix.setIdentityM(matrix,0);
        Matrix.frustumM(matrix,0,-1,1,-1,1,1,10);
        System.out.println(Arrays.toString(matrix));

    }
}