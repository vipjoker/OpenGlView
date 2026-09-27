package com.example.oleh.opengl2.opengl3;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;

import com.example.oleh.opengl2.R;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class OpenGl3Renderer implements GLSurfaceView.Renderer {
    float time = 0;
    int TOTAL_SPRITES = 1000;
    List<Sprite> sprites = new ArrayList<>();
    private final Context context;
    private final GLSurfaceView glView;
    private int programId;
    private int uResolutionLoc;
    private int width;
    private int height;
    private AutoBatchRenderer autoBatchRenderer;
    private Texture2D []textures = new Texture2D[3];
    public interface FpsListener {
        void onFpsCalculated(int fps);
    }

    private final FpsListener fpsListener;

    // FPS measurement state
    private long lastTimeNs = 0;
    private int frameCount = 0;
    public OpenGl3Renderer(Context context, @NotNull GLSurfaceView glView,FpsListener fpsListener) {
        this.context = context;
        this.glView = glView;
        this.fpsListener = fpsListener;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // Must be called on the GL thread after context is ready

        GLES30.glEnable(GLES30.GL_BLEND);
        GLES30.glBlendFunc(GLES30.GL_ONE, GLES30.GL_ONE_MINUS_SRC_ALPHA);
        textures[0] = Texture2D.loadAsync(this.context, this.glView, R.drawable.ball1);
        textures[1] = Texture2D.loadAsync(this.context, this.glView, R.drawable.ball2);
        textures[2] = Texture2D.loadAsync(this.context, this.glView, R.raw.woodtexture1);
        autoBatchRenderer = new AutoBatchRenderer();

        programId = ShaderUtil.createProgram(context, R.raw.sprite_vertext_shader, R.raw.sprite_fragment_shader);

        if (programId != 0) {
            uResolutionLoc = GLES30.glGetUniformLocation(programId, "u_resolution");
        }



    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES30.glViewport(0, 0, width, height);
        this.width = width;
        this.height = height;

        for (int i = 0; i < TOTAL_SPRITES; i++) {
            Texture2D tex = textures[i % 3];
            Sprite sp = new Sprite(tex, 200f,200f);

            sp.setPosition((float) (Math.random() * width), (float) (Math.random() * height));
            sp.setAnchorPoint(0.5f, 0.5f);
//            sp.setZIndex( i % 2); // Два шари глибини

            // Масштабуємо (оскільки картинки ~200px, зменшуємо для сцени)
//            sp.setScale() = 0.25 + Math.random() * 0.25;
//            sp.setScale(sp.baseScale);

            sp.speedX = (Math.random() - 0.5) * 2;
            sp.speedY = (Math.random() - 0.5) * 2;
            sp.rotSpeed = (Math.random() - 0.5) * 3;

            sprites.add(sp);
        }
    }



    @Override
    public void onDrawFrame(GL10 gl) {
        time += 0.02f;

        // --- FPS Calculation ---
        frameCount++;
        long currentTimeNs = System.nanoTime();
        long elapsedNs = currentTimeNs - lastTimeNs;

        // Update every 1 second (1,000,000,000 nanoseconds)
        if (elapsedNs >= 1_000_000_000L) {
            final int currentFps = (int) ((frameCount * 1_000_000_000L) / elapsedNs);
            frameCount = 0;
            lastTimeNs = currentTimeNs;

            if (fpsListener != null) {
                fpsListener.onFpsCalculated(currentFps);
            }
        }
        GLES30.glClearColor(0.12f, 0.12f, 0.15f, 1.0f);

        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);

        if (programId == 0) return;

        GLES30.glUseProgram(programId);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);

        GLES30.glUseProgram(programId);
        GLES30.glUniform2f(uResolutionLoc, width, height);


        for (Sprite sp : sprites) {


            sp.getX();
            sp.setPosition((float) (sp.getX() + sp.speedX),(float) (sp.getY() + sp.speedY ));

            if (sp.getX() < 0 || sp.getX() > width) sp.speedX *= -1;
            if (sp.getY() < 0 || sp.getY() > height) sp.speedY *= -1;

            sp.setRotation((float) (sp.getRotation() + sp.rotSpeed));

            autoBatchRenderer.drawSprite(sp);
        }



        // Автоматично сортує за Z та текстурами, заповнює VBO і викликає DrawElements
        autoBatchRenderer.render();
        // Pass uniforms, bind VAO, and draw...
    }
}