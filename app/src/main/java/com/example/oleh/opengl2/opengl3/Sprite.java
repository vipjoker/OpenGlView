package com.example.oleh.opengl2.opengl3;

import androidx.annotation.Nullable;

public class Sprite {

    // Структура для зберігання трансформованих 4 вершин чотирикутника (Quad)
    public static class QuadVertices {
        public float x0, y0; // Верхній лівий (Top-Left)
        public float x1, y1; // Верхній правий (Top-Right)
        public float x2, y2; // Нижній лівий (Bottom-Left)
        public float x3, y3; // Нижній правий (Bottom-Right)

        public void set(float x0, float y0, float x1, float y1,
                        float x2, float y2, float x3, float y3) {
            this.x0 = x0; this.y0 = y0;
            this.x1 = x1; this.y1 = y1;
            this.x2 = x2; this.y2 = y2;
            this.x3 = x3; this.y3 = y3;
        }
    }

    private Texture2D textureObj;

    @Nullable
    private Float customWidth = null;
    @Nullable
    private Float customHeight = null;

    public double speedX;//delete
    public double speedY;//delete
    public double rotSpeed;//delete

    private float x = 0.0f;
    private float y = 0.0f;
    private float rotation = 0.0f;
    private float scaleX = 1.0f;
    private float scaleY = 1.0f;
    private float anchorX = 0.5f;
    private float anchorY = 0.5f;
    private int zIndex = 0;

    private float r = 1.0f;
    private float g = 1.0f;
    private float b = 1.0f;
    private float opacity = 1.0f;
    private boolean visible = true;

    // Кешований об'єкт для запобігання GC-паузам під час рендеру
    private final QuadVertices cachedVertices = new QuadVertices();

    public Sprite(Texture2D texture2D) {
        this(texture2D, null, null);
    }

    public Sprite(Texture2D texture2D, @Nullable Float width, @Nullable Float height) {
        this.textureObj = texture2D;
        this.customWidth = width;
        this.customHeight = height;
    }

    // Динамічний розмір: якщо customWidth не задано, беремо реальний розмір із Texture2D
    public float getWidth() {
        return customWidth != null ? customWidth : (textureObj != null ? textureObj.getWidth() : 0.0f);
    }

    public float getHeight() {
        return customHeight != null ? customHeight : (textureObj != null ? textureObj.getHeight() : 0.0f);
    }

    public Sprite setSize(float width, float height) {
        this.customWidth = width;
        this.customHeight = height;
        return this;
    }

    public Sprite resetToTextureSize() {
        this.customWidth = null;
        this.customHeight = null;
        return this;
    }

    public Sprite setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public Sprite setScale(float scale) {
        return setScale(scale, scale);
    }

    public Sprite setScale(float sx, float sy) {
        this.scaleX = sx;
        this.scaleY = sy;
        return this;
    }

    public Sprite setRotation(float deg) {
        this.rotation = deg;
        return this;
    }

    public Sprite setAnchorPoint(float ax, float ay) {
        this.anchorX = ax;
        this.anchorY = ay;
        return this;
    }

    public Sprite setColor(float r, float g, float b) {
        this.r = r;
        this.g = g;
        this.b = b;
        return this;
    }

    public Sprite setOpacity(float a) {
        this.opacity = a;
        return this;
    }

    public Sprite setZIndex(int zIndex) {
        this.zIndex = zIndex;
        return this;
    }

    public Sprite setVisible(boolean visible) {
        this.visible = visible;
        return this;
    }

    public Sprite setTexture(Texture2D textureObj) {
        this.textureObj = textureObj;
        return this;
    }

    /**
     * Обчислює світові 2D-координати 4-х вершин чотирикутника з урахуванням
     * зсуву (x, y), обертання (rotation), масштабування (scale) та точки прив'язки (anchor).
     */
    public QuadVertices computeVertices() {
        return computeVertices(this.cachedVertices);
    }

    /**
     * Варіант з передачею цільового об'єкта для уникнення алокацій.
     */
    public QuadVertices computeVertices(QuadVertices out) {
        double rad = Math.toRadians(this.rotation);
        float cos = (float) Math.cos(rad);
        float sin = (float) Math.sin(rad);

        // Матриця 2x2: Scale -> Rotate
        float a = cos * this.scaleX;
        float b = sin * this.scaleX;
        float c = -sin * this.scaleY;
        float d = cos * this.scaleY;

        float w = getWidth();
        float h = getHeight();

        // Локальні координати прямокутника відносно anchor-point
        float lx0 = -this.anchorX * w;
        float ly0 = -this.anchorY * h;
        float lx1 = (1.0f - this.anchorX) * w;
        float ly1 = (1.0f - this.anchorY) * h;

        out.set(
            a * lx0 + c * ly0 + this.x, b * lx0 + d * ly0 + this.y, // (x0, y0)
            a * lx1 + c * ly0 + this.x, b * lx1 + d * ly0 + this.y, // (x1, y0)
            a * lx0 + c * ly1 + this.x, b * lx0 + d * ly1 + this.y, // (x0, y1)
            a * lx1 + c * ly1 + this.x, b * lx1 + d * ly1 + this.y  // (x1, y1)
        );

        return out;
    }

    // Getters
    public Texture2D getTexture() { return textureObj; }
    public float getX() { return x; }
    public float getY() { return y; }
    public float getRotation() { return rotation; }
    public float getScaleX() { return scaleX; }
    public float getScaleY() { return scaleY; }
    public float getAnchorX() { return anchorX; }
    public float getAnchorY() { return anchorY; }
    public int getZIndex() { return zIndex; }
    public float getR() { return r; }
    public float getG() { return g; }
    public float getB() { return b; }
    public float getOpacity() { return opacity; }
    public boolean isVisible() { return visible; }
}