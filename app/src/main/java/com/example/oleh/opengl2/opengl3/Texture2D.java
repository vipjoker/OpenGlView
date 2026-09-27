package com.example.oleh.opengl2.opengl3;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.util.Log;

import androidx.annotation.DrawableRes;
import androidx.annotation.RawRes;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Texture2D {

    private static final String TAG = "Texture2D";
    private static final AtomicInteger NEXT_INSTANCE_ID = new AtomicInteger(1);
    private static final ExecutorService IO_EXECUTOR = Executors.newFixedThreadPool(2);

    private final int instanceId;
    private final int glTextureId;
    private int width = 1;
    private int height = 1;
    private volatile boolean isLoaded = false;

    public Texture2D() {
        this.instanceId = NEXT_INSTANCE_ID.getAndIncrement();

        // Генеруємо один текстурний ID
        final int[] textures = new int[1];
        GLES30.glGenTextures(1, textures, 0);
        this.glTextureId = textures[0];

        // Створюємо піксель-заглушку 1x1 (Magenta: R=255, G=0, B=255, A=255)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, this.glTextureId);

        ByteBuffer magentaPixel = ByteBuffer.allocateDirect(4);
        magentaPixel.order(ByteOrder.nativeOrder());
        magentaPixel.put(new byte[]{(byte) 255, 0, (byte) 255, (byte) 255});
        magentaPixel.position(0);

        GLES30.glTexImage2D(
                GLES30.GL_TEXTURE_2D,
                0,
                GLES30.GL_RGBA8, // Внутрішній формат OpenGL ES 3.0
                1, 1, 0,
                GLES30.GL_RGBA,
                GLES30.GL_UNSIGNED_BYTE,
                magentaPixel
        );

        // Стандартна конфігурація для NPOT (Non-Power-of-Two) текстур
        setupTextureParameters();
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
    }

    /**
     * Асинхронно завантажує текстуру з ресурсів (res/drawable або res/raw).
     */
    public static Texture2D loadAsync(Context context, GLSurfaceView glSurfaceView,  int resourceId) {
        final Texture2D texObj = new Texture2D();

        IO_EXECUTOR.execute(() -> {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false; // Вимикає масштабування під DPI екрана

            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
            if (bitmap == null) {
                Log.e(TAG, "Failed to decode bitmap from resource: " + resourceId);
                return;
            }

            // Передаємо завантажений Bitmap у GLThread для викликів GLES
            glSurfaceView.queueEvent(() -> texObj.uploadBitmap(bitmap));
        });

        return texObj;
    }

    /**
     * Асинхронно завантажує текстуру з зовнішнього URL чи локального InputStream.
     */
    public static Texture2D loadFromStreamAsync(GLSurfaceView glSurfaceView, InputStream stream) {
        final Texture2D texObj = new Texture2D();

        IO_EXECUTOR.execute(() -> {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;

            Bitmap bitmap = BitmapFactory.decodeStream(stream, null, options);
            if (bitmap == null) {
                Log.e(TAG, "Failed to decode bitmap from InputStream");
                return;
            }

            glSurfaceView.queueEvent(() -> texObj.uploadBitmap(bitmap));
        });

        return texObj;
    }

    /**
     * Синхронне завантаження (якщо вже виконується всередині GLThread).
     */
    public static Texture2D loadDirect(Bitmap bitmap) {
        Texture2D texObj = new Texture2D();
        texObj.uploadBitmap(bitmap);
        return texObj;
    }

    private void uploadBitmap(Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled()) return;

        this.width = bitmap.getWidth();
        this.height = bitmap.getHeight();

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, this.glTextureId);

        // GLUtils бере на себе вирівнювання та завантаження каналів напряму з Bitmap
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0);

        setupTextureParameters();

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
        this.isLoaded = true;

        // Звільняємо пам'ять JVM/Native Bitmap після заливки у VRAM
        bitmap.recycle();
    }

    private static void setupTextureParameters() {
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
    }

    public void bind(int textureSlotIndex) {
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0 + textureSlotIndex);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, glTextureId);
    }

    public void dispose() {
        GLES30.glDeleteTextures(1, new int[]{glTextureId}, 0);
    }

    public int getInstanceId() { return instanceId; }
    public int getGlTextureId() { return glTextureId; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public boolean isLoaded() { return isLoaded; }
}