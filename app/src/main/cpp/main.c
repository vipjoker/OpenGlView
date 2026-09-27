#include <SDL3/SDL.h>
#include <SDL3/SDL_main.h>
#include <SDL3_image/SDL_image.h>
#include <stdbool.h>
#include <stdlib.h>
#include <time.h>

#define NUM_SPRITES 1000
#define NUM_TEXTURES 3

typedef struct {
    float x;
    float y;
    float vx;
    float vy;
    float width;
    float height;
    float rotation;
    float rot_speed;
    int texture_index;
} Sprite;

int main(int argc, char* argv[]) {
    (void)argc;
    (void)argv;

    // 1. Initialize SDL
    if (!SDL_Init(SDL_INIT_VIDEO | SDL_INIT_EVENTS)) {
        SDL_Log("SDL_Init Error: %s", SDL_GetError());
        return 1;
    }

    // 2. Create Fullscreen Window & Renderer
    SDL_Window* window = NULL;
    SDL_Renderer* renderer = NULL;
    if (!SDL_CreateWindowAndRenderer("SDL3 3000 Sprites", 0, 0, SDL_WINDOW_FULLSCREEN, &window, &renderer)) {
        SDL_Log("SDL_CreateWindowAndRenderer Error: %s", SDL_GetError());
        SDL_Quit();
        return 1;
    }

    int screen_w = 800;
    int screen_h = 600;
    SDL_GetRenderOutputSize(renderer, &screen_w, &screen_h);
    if (screen_w <= 0) screen_w = 800;
    if (screen_h <= 0) screen_h = 600;

    // 3. Load Textures from Assets folder
    const char* asset_files[NUM_TEXTURES] = {
            "ball1.png",
            "ball2.png",
            "popup.png"
    };

    SDL_Texture* textures[NUM_TEXTURES] = { NULL };
    int loaded_count = 0;

    for (int i = 0; i < NUM_TEXTURES; i++) {
        textures[i] = IMG_LoadTexture(renderer, asset_files[i]);
        if (textures[i]) {
            loaded_count++;
            SDL_Log("Successfully loaded asset: %s", asset_files[i]);
        } else {
            SDL_Log("Failed to load asset '%s': %s", asset_files[i], SDL_GetError());
        }
    }

    // Fallback procedural texture if no images loaded
    SDL_Texture* fallback_texture = NULL;
    if (loaded_count == 0) {
        SDL_Surface* surf = SDL_CreateSurface(32, 32, SDL_PIXELFORMAT_RGBA8888);
        if (surf) {
            SDL_FillSurfaceRect(surf, NULL, SDL_MapRGBA(SDL_GetPixelFormatDetails(surf->format), NULL, 255, 100, 100, 255));
            fallback_texture = SDL_CreateTextureFromSurface(renderer, surf);
            SDL_DestroySurface(surf);
        }
    }

    // 4. Initialize 3000 Sprites with Random Positions & Velocities
    srand((unsigned int)time(NULL));
    Sprite* sprites = (Sprite*)malloc(sizeof(Sprite) * NUM_SPRITES);
    if (!sprites) {
        SDL_Log("Failed to allocate memory for sprites");
        SDL_Quit();
        return 1;
    }

    for (int i = 0; i < NUM_SPRITES; i++) {
        sprites[i].width = 32.0f + (float)(rand() % 33); // Size between 32 and 64 px
        sprites[i].height = sprites[i].width;
        sprites[i].x = (float)(rand() % (int)(screen_w > (int)sprites[i].width ? (screen_w - (int)sprites[i].width) : 1));
        sprites[i].y = (float)(rand() % (int)(screen_h > (int)sprites[i].height ? (screen_h - (int)sprites[i].height) : 1));
        sprites[i].vx = ((float)(rand() % 200) - 100.0f); // Velocity -100 to +100 px/sec
        sprites[i].vy = ((float)(rand() % 200) - 100.0f);
        sprites[i].rotation = (float)(rand() % 360);
        sprites[i].rot_speed = ((float)(rand() % 180) - 90.0f); // Rotation -90 to +90 deg/sec
        sprites[i].texture_index = rand() % NUM_TEXTURES;
    }

    // 5. Main Render Loop
    bool running = true;
    SDL_Event event;
    Uint64 last_time = SDL_GetTicks();

    // FPS calculation variables
    int frame_count = 0;
    float fps = 0.0f;
    Uint64 fps_last_time = SDL_GetTicks();

    while (running) {
        Uint64 current_time = SDL_GetTicks();
        float delta_time = (float)(current_time - last_time) / 1000.0f;
        if (delta_time > 0.1f) delta_time = 0.1f;
        last_time = current_time;

        // Calculate FPS every 500ms
        frame_count++;
        if (current_time - fps_last_time >= 500) {
            fps = (float)frame_count / ((float)(current_time - fps_last_time) / 1000.0f);
            frame_count = 0;
            fps_last_time = current_time;
        }

        while (SDL_PollEvent(&event)) {
            if (event.type == SDL_EVENT_QUIT) {
                running = false;
            }
        }

        // Update sprite positions & rotations
        for (int i = 0; i < NUM_SPRITES; i++) {
            sprites[i].x += sprites[i].vx * delta_time;
            sprites[i].y += sprites[i].vy * delta_time;
            sprites[i].rotation += sprites[i].rot_speed * delta_time;

            // Bounce off screen boundaries
            if (sprites[i].x < 0) {
                sprites[i].x = 0;
                sprites[i].vx = -sprites[i].vx;
            } else if (sprites[i].x + sprites[i].width > (float)screen_w) {
                sprites[i].x = (float)screen_w - sprites[i].width;
                sprites[i].vx = -sprites[i].vx;
            }

            if (sprites[i].y < 0) {
                sprites[i].y = 0;
                sprites[i].vy = -sprites[i].vy;
            } else if (sprites[i].y + sprites[i].height > (float)screen_h) {
                sprites[i].y = (float)screen_h - sprites[i].height;
                sprites[i].vy = -sprites[i].vy;
            }
        }

        // Render scene
        SDL_SetRenderDrawColor(renderer, 20, 20, 30, 255);
        SDL_RenderClear(renderer);

        // Render sprites
        for (int i = 0; i < NUM_SPRITES; i++) {
            SDL_Texture* tex = textures[sprites[i].texture_index];
            if (!tex) tex = fallback_texture;

            if (tex) {
                SDL_FRect dst_rect = {
                        .x = sprites[i].x,
                        .y = sprites[i].y,
                        .w = sprites[i].width,
                        .h = sprites[i].height
                };

                SDL_RenderTextureRotated(
                        renderer,
                        tex,
                        NULL,
                        &dst_rect,
                        (double)sprites[i].rotation,
                        NULL,
                        SDL_FLIP_NONE
                );
            }
        }

        // Render FPS Overlay Box & Text with enlarged font scale (2.5x)
        SDL_SetRenderScale(renderer, 2.5f, 2.5f);

        SDL_SetRenderDrawBlendMode(renderer, SDL_BLENDMODE_BLEND);
        SDL_FRect fps_bg = { 8.0f, 8.0f, 205.0f, 20.0f };
        SDL_SetRenderDrawColor(renderer, 0, 0, 0, 180); // Semi-transparent black
        SDL_RenderFillRect(renderer, &fps_bg);

        SDL_SetRenderDrawColor(renderer, 0, 255, 128, 255); // Neon green
        SDL_RenderDebugTextFormat(renderer, 12.0f, 14.0f, "FPS: %.1f | %d Sprites", fps, NUM_SPRITES);

        // Reset render scale back to default
        SDL_SetRenderScale(renderer, 1.0f, 1.0f);

        SDL_RenderPresent(renderer);
    }

    // 6. Cleanup
    free(sprites);
    for (int i = 0; i < NUM_TEXTURES; i++) {
        if (textures[i]) SDL_DestroyTexture(textures[i]);
    }
    if (fallback_texture) SDL_DestroyTexture(fallback_texture);

    SDL_DestroyRenderer(renderer);
    SDL_DestroyWindow(window);
    SDL_Quit();

    return 0;
}
