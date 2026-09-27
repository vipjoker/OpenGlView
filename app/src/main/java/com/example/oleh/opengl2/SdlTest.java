package com.example.oleh.opengl2;

import org.libsdl.app.SDLActivity;

public class SdlTest extends SDLActivity {

    @Override
    protected String[] getLibraries() {
        return new String[] {
                "SDL3",
                "SDL3_image",
                "SDL3_mixer",
                "SDL3_ttf",
                "sdl_test" // Your native application code
        };
    }
}
