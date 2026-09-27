package com.example.oleh.opengl2;

import com.example.oleh.opengl2.opengl3.Sprite;

public class PhysicsEngine {

    static {
        System.loadLibrary("native-physics");
    }

    private long worldHandle;
    private final float[] transformBuffer = new float[3]; // [x, y, rotationDegrees]

    public PhysicsEngine(float gravityX, float gravityY) {
        this.worldHandle = createWorld(gravityX, gravityY);
    }

    public void update(float dt) {
        // Box2D 3 recommends 4 sub-steps for smooth simulation
        stepWorld(worldHandle, dt, 4);
    }

    public long addBox(float x, float y, float halfWidth, float halfHeight, boolean isDynamic) {
        return createBoxBody(worldHandle, x, y, halfWidth, halfHeight, isDynamic);
    }

    public void syncSprite(long bodyHandle, Sprite sprite, float ptmRatio) {
        getBodyTransform(bodyHandle, transformBuffer);
        // Convert Box2D meters back to screen pixels
        sprite.setPosition(transformBuffer[0] * ptmRatio, transformBuffer[1] * ptmRatio);
        sprite.setRotation(transformBuffer[2]);
    }

    public void dispose() {
        if (worldHandle != 0) {
            destroyWorld(worldHandle);
            worldHandle = 0;
        }
    }

    // Native declarations
    private native long createWorld(float gravityX, float gravityY);
    private native void stepWorld(long worldHandle, float timeStep, int subSteps);
    private native long createBoxBody(long worldHandle, float x, float y, float hx, float hy, boolean isDynamic);
    private native void getBodyTransform(long bodyHandle, float[] outTransform);
    private native void destroyWorld(long worldHandle);
}