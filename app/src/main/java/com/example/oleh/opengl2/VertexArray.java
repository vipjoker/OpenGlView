package com.example.oleh.opengl2;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class VertexArray {
    private static final int FLOAT_SIZE_BYTES = 4;

    public float [] array ;

    public VertexArray addPoint(double x,double y, double z){
        addPoints((float) x,(float) y,(float) z);
        return this;
    }

    public void addTextureCoordintes(double u,double v){
        addPoints((float) u,(float) v);
    }

    public void addColor(double r,double g,double b){
        addPoints((float)r,(float)g,(float)b);
    }

    public float[] getArray(){
        return array;
    }

    private void addPoints(float... points){
        if(array == null)array = points;
        else{
            float[] temp = new float[array.length + points.length];
            System.arraycopy(array,0,temp,0,array.length);
            System.arraycopy(points,0,temp,array.length,points.length);
            array = temp;
        }
    }

    public  FloatBuffer floatBuffer() {
        FloatBuffer buffer = ByteBuffer
                .allocateDirect(getArray().length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        buffer.put(getArray()).position(0);
        return buffer;
    }
}
