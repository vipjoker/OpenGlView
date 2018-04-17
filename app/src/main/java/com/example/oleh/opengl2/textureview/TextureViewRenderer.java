package com.example.oleh.opengl2.textureview;
import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.TextureView;
import com.example.oleh.opengl2.thirdParty.gles.EglCore;
import com.example.oleh.opengl2.thirdParty.gles.WindowSurface;

/**
 * Created by oleh on 11/8/17.
 */


/**
 * Handles GL rendering and SurfaceTexture callbacks.
 * <p>
 * We don't create a Looper, so the SurfaceTexture-by-way-of-TextureView callbacks
 * happen on the UI thread.
 */

public class TextureViewRenderer extends Thread implements TextureView.SurfaceTextureListener {
        private Object mLock = new Object();        // guards mSurfaceTexture, mDone
        private SurfaceTexture mSurfaceTexture;
        private EglCore mEglCore;
        private boolean mDone;
        public static final String TAG = TextureViewRenderer.class.getName();
        private final OpenGlCallback callback;
        public TextureViewRenderer(OpenGlCallback callback) {
            super("TextureViewGL Renderer");
            this.callback = callback;
        }

        @Override
        public void run() {

            while (true) {

                SurfaceTexture surfaceTexture = null;

                // Latch the SurfaceTexture when it becomes available.  We have to wait for
                // the TextureView to create it.
                synchronized (mLock) {
                    while (!mDone && (surfaceTexture = mSurfaceTexture) == null) {
                        try {
                            mLock.wait();
                        } catch (InterruptedException ie) {
                            throw new RuntimeException(ie);     // not expected
                        }
                    }
                    if (mDone) {
                        break;
                    }
                }
                Log.d(TAG, "Got surfaceTexture=" + surfaceTexture);

                // Create an EGL surface for our new SurfaceTexture.  We're not on the same
                // thread as the SurfaceTexture, which is a concern for the *consumer*, which
                // wants to call updateTexImage().  Because we're the *producer*, i.e. the
                // one generating the frames, we don't need to worry about being on the same
                // thread.
                mEglCore = new EglCore(null, EglCore.FLAG_TRY_GLES3);
                WindowSurface windowSurface = new WindowSurface(mEglCore, mSurfaceTexture);
                windowSurface.makeCurrent();

                callback.onStarted(windowSurface.getWidth(),windowSurface.getHeight());
                // Render frames until we're told to stop or the SurfaceTexture is destroyed.
                doAnimation(windowSurface);
                callback.onStopped();
                windowSurface.release();
                mEglCore.release();
//                if (!sReleaseInCallback) {
//                    Log.i(TAG, "Releasing SurfaceTexture in renderer thread");
//                    surfaceTexture.release();
//                }
            }


            Log.d(TAG, "Renderer thread exiting");
        }

        /**
         * Draws updates as fast as the system will allow.
         * <p>
         * In 4.4, with the synchronous buffer queue queue, the frame rate will be limited.
         * In previous (and future) releases, with the async queue, many of the frames we
         * render may be dropped.
         * <p>
         * The correct thing to do here is use Choreographer to schedule frame updates off
         * of vsync, but that's not nearly as much fun.
         */
        private void doAnimation(WindowSurface eglSurface) {
            final int BLOCK_WIDTH = 80;
            final int BLOCK_SPEED = 2;
            int xpos = -BLOCK_WIDTH / 2;
            int xdir = BLOCK_SPEED;
            int width = eglSurface.getWidth();
            int height = eglSurface.getHeight();
            long delta = 0;
            Log.d(TAG, "Animating " + width + "x" + height + " EGL surface");
            try {
                while (true) {
                    // Check to see if the TextureView's SurfaceTexture is still valid.
                    if(delta == 0)delta = System.currentTimeMillis();
                    Thread.sleep(1000 / 60);
                    synchronized (mLock) {
                        SurfaceTexture surfaceTexture = mSurfaceTexture;
                        if (surfaceTexture == null) {
                            Log.d(TAG, "doAnimation exiting");
                            return;
                        }
                    }
                    long currentTime = System.currentTimeMillis();
                    callback.onDraw(currentTime - delta);
                    delta = currentTime;

                    // Still alive, render a frame.


                    // Publish the frame.  If we overrun the consumer, frames will be dropped,
                    // so on a sufficiently fast device the animation will run at faster than
                    // the display refresh rate.
                    //
                    // If the SurfaceTexture has been destroyed, this will throw an exception.
                    eglSurface.swapBuffers();

                    // Advance state
//                clearColor += 0.015625f;
//                if (clearColor > 1.0f) {
//                    clearColor = 0.0f;
//                }
                    xpos += xdir;
                    if (xpos <= -BLOCK_WIDTH / 2 || xpos >= width - BLOCK_WIDTH / 2) {
                        Log.d(TAG, "change direction");
                        xdir = -xdir;
                    }
                }
            }catch (InterruptedException e){
                Log.e(TAG, "doAnimation: interrupted", e);
            }
        }

        /**
         * Tells the thread to stop running.
         */
        public void halt() {
            synchronized (mLock) {
                mDone = true;
                mLock.notify();
            }
        }

        @Override   // will be called on UI thread
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTex, int width, int height) {
            Log.d(TAG, "onSurfaceTextureAvailable(" + width + "x" + height + ")");
            synchronized (mLock) {
                mSurfaceTexture = surfaceTex;
                mLock.notify();
            }
        }

        @Override   // will be called on UI thread
        public void onSurfaceTextureSizeChanged(SurfaceTexture st, int width, int height) {
            Log.d(TAG, "onSurfaceTextureSizeChanged(" + width + "x" + height + ")");
            // TODO: ?
        }

        @Override   // will be called on UI thread
        public boolean onSurfaceTextureDestroyed(SurfaceTexture st) {
            Log.d(TAG, "onSurfaceTextureDestroyed");

            // We set the SurfaceTexture reference to null to tell the Renderer thread that
            // it needs to stop.  The renderer might be in the middle of drawing, so we want
            // to return false here so that the caller doesn't try to release the ST out
            // from under us.
            //
            // In theory.
            //
            // In 4.4, the buffer queue was changed to be synchronous, which means we block
            // in dequeueBuffer().  If the renderer has been running flat out and is currently
            // sleeping in eglSwapBuffers(), it's going to be stuck there until somebody
            // tears down the SurfaceTexture.  So we need to tear it down here to ensure
            // that the renderer thread will break.  If we don't, the thread sticks there
            // forever.
            //
            // The only down side to releasing it here is we'll get some complaints in logcat
            // when eglSwapBuffers() fails.
            synchronized (mLock) {
                mSurfaceTexture = null;
            }

            return true;
        }

        @Override   // will be called on UI thread
        public void onSurfaceTextureUpdated(SurfaceTexture st) {
            //Log.d(TAG, "onSurfaceTextureUpdated");
        }
        public interface OpenGlCallback{
            void onStarted(int width,int height);
            void onDraw(long delta);
            void onStopped();
        }
    }



