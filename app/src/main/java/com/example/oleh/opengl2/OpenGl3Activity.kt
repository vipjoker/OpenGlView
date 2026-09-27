package com.example.oleh.opengl2;
import android.app.Activity
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.widget.TextView
import com.example.oleh.opengl2.opengl3.OpenGl3Renderer

class OpenGl3Activity : Activity() , OpenGl3Renderer.FpsListener{
    private lateinit var glSurfaceView: GLSurfaceView
    private lateinit var tvFps: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        setContentView(R.layout.gl_activity);

        tvFps = findViewById(R.id.tv_fps);
        glSurfaceView = findViewById(R.id.gl_surface_view);
        // Set to OpenGL ES 3.0 context [citation:16]
        glSurfaceView.setEGLContextClientVersion(3)
        glSurfaceView.setRenderer(OpenGl3Renderer(this,glSurfaceView,this))
    }

    override fun onResume() {
        super.onResume()
        glSurfaceView.onResume()
    }

    override fun onPause() {
        super.onPause()
        glSurfaceView.onPause()
    }

    public override fun onFpsCalculated(fps: Int) {
        // Must switch from GLThread to UI thread to update View
        runOnUiThread {
            tvFps.text = "FPS: $fps"
        }
    }
}

