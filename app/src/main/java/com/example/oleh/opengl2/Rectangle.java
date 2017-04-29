package com.example.oleh.opengl2;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by oleh on 29.04.17.
 */

public class Rectangle extends VertexArray {

    public Rectangle(){
        super();

        addPoint(.5, .5, .5).addTextureCoordintes(1, 0);
        addPoint(-.5, .5, .5).addTextureCoordintes(0, 0);
        addPoint(-.5, -.5, .5).addTextureCoordintes(0, 1);
        addPoint(.5, -.5, .5).addTextureCoordintes(1, 1);

    }


}
