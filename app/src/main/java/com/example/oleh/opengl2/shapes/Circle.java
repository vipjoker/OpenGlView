package com.example.oleh.opengl2.shapes;

import static java.lang.Math.PI;

/**
 * Created by oleh on 29.04.17.
 */

public class Circle extends Shape {
    public Circle(float radius,int segments) {
        super();
        for(int i  = 0 ; i < radius; i++){
            double y = Math.sin(PI*2  * (1.0/segments));
            double x = Math.cos(PI*2 * (1.0/segments));
            addPoint(x,y,0);
        }
    }

    @Override
    public void draw() {

    }
}
