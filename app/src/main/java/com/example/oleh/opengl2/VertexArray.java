package com.example.oleh.opengl2;



public class VertexArray {

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
}
