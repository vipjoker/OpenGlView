package com.example.oleh.opengl2;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.os.Bundle;
import android.util.Log;
import android.view.Choreographer;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;


import android.opengl.GLES20;
import android.opengl.GLES30;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.widget.SeekBar;

import com.example.oleh.opengl2.textureview.OpenGLRenderer;
import com.example.oleh.opengl2.textureview.TextureViewRenderer;
import com.example.oleh.opengl2.thirdParty.gles.EglCore;
import com.example.oleh.opengl2.thirdParty.gles.WindowSurface;


/**
 * Created by oleh on 11/8/17.
 */

/**
 * Simple demonstration of using GLES to draw on a TextureView.
 * <p>
 * Note that rendering is a multi-stage process:
 * <ol>
 * <li>Render thread draws with GL on its local EGLSurface, a window surface it created.  The
 * window surface is backed by the SurfaceTexture from TextureVIew.
 * <li>The SurfaceTexture takes what is rendered onto it and makes it available as a GL texture.
 * <li>TextureView takes the GL texture and renders it onto its EGLSurface.  That EGLSurface
 * is a window surface visible to the compositor.
 * </ol>
 * It's important to bear in mind that Surface and EGLSurface are related but very
 * different things.
 * <p>
 * Unlike GLSurfaceView, TextureView doesn't manage the EGL config or renderer thread, so we
 * take care of that ourselves.
 * <p>
 * Currently renders frames as fast as possible, without waiting for the consumer.
 * <p>
 * As part of experimenting with the framework, this allows the renderer thread to continue
 * to run as the TextureView is being destroyed (we stop the thread in onDestroy() rather
 * than onPause()).  Normally the renderer would be stopped when the application pauses.
 */
public class TextureActivity extends Activity implements SeekBar.OnSeekBarChangeListener,View.OnTouchListener {


    private static final String TAG = TextureActivity.class.getName();

    OpenGLRenderer openGLRenderer;
    private TextureView mTextureView;
    private TextureViewRenderer mRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
//Choreographer.getInstance()(new Choreographer.FrameCallback() {
//    @Override
//    public void doFrame(long frameTimeNanos) {
//        Log.i(TAG, "doFrame: ");
//    }
//});
        // Start up the Renderer thread.  It'll sleep until the TextureView is ready.
        setContentView(R.layout.activity_main);

        openGLRenderer = new OpenGLRenderer(this);

        ((SeekBar) findViewById(R.id.sbEyeX)).setOnSeekBarChangeListener(this);
        ((SeekBar) findViewById(R.id.sbEyeY)).setOnSeekBarChangeListener(this);
        ((SeekBar) findViewById(R.id.sbEyeZ)).setOnSeekBarChangeListener(this);

        findViewById(R.id.textureView).setOnTouchListener(this);
        mRenderer = new TextureViewRenderer(openGLRenderer);
        mRenderer.start();

        mTextureView = (TextureView) findViewById(R.id.textureView);
        mTextureView.setSurfaceTextureListener(mRenderer);
    }
    Point point;
   Point3d point3d;
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                point =new Point((int)event.getX(),(int)event.getY());
                point3d = new Point3d(openGLRenderer.getX(),openGLRenderer.getY(),openGLRenderer.getZ());
                break;
            case MotionEvent.ACTION_MOVE:
                int offsetX =  (int)event.getX() - point.x ;
                int offsetY =  (int)event.getY() - point.y;

                float resultX =  offsetX/100.0f;
                float resultY =   offsetY/100.0f;

                openGLRenderer.setX(point3d.x + (float)Math.cos(resultX) * 4);
                openGLRenderer.setZ(point3d.x + (float)Math.sin(resultX) * 4);

                openGLRenderer.setY(point3d.y + (float) Math.sin(resultY) *4);
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //updateControls();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        // Don't do this -- halt the thread in onPause() and wait for it to finish.
        mRenderer.halt();
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        float offset = progress / 10.0f;

        float result = offset - 5.0f;
        switch (seekBar.getId()) {
            case R.id.sbEyeX:

                openGLRenderer.setX((float)(Math.cos(result ) * 4f));

                break;
            case R.id.sbEyeY:
                openGLRenderer.setY(result);

                break;
            case R.id.sbEyeZ:
                openGLRenderer.setZ((float)(Math.sin(result ) * 4f));

                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    class Point3d{
        float x,y,z;

        public Point3d(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

}
