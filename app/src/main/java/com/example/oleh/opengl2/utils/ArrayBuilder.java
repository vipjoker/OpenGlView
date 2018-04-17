package com.example.oleh.opengl2.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by oleh on 11/9/17.
 */

public class ArrayBuilder {
    List<Float> mList;
    public ArrayBuilder(){
        mList = new ArrayList<>();
    }

    public void add2Point(float x,float y){
        mList.add(x);
        mList.add(y);
    }

    public void addPointWithColor(float x,float y,float z,float r,float g,float b){
        mList.add(x);
        mList.add(y);
        mList.add(z);
        mList.add(r);
        mList.add(g);
        mList.add(b);
    }

    public float[] build(){
        float [] array=  new float [mList.size()];
        int counter = 0;
        for(float f :mList){
            array[counter++] = f;
        }
        return array;
    }

}
