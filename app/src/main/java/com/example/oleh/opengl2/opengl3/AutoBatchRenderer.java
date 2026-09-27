package com.example.oleh.opengl2.opengl3;

import android.opengl.GLES30;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Comparator;

public class AutoBatchRenderer {

    public static final int FLOATS_PER_VERTEX = 8; // x, y, u, v, r, g, b, a
    public static final int VERTICES_PER_QUAD = 4;
    public static final int INDICES_PER_QUAD = 6;
    public static final int BYTES_PER_FLOAT = 4;
    public static final int BYTES_PER_SHORT = 2;

    private final int maxQuads;
    private final float[] vertexData;
    private final FloatBuffer vertexByteBuffer;

    private int vaoId;
    private int vboId;
    private int iboId;

    private int quadCount = 0;
    private int drawCalls = 0;
    private Texture2D currentTextureObj = null;

    private final ArrayList<Sprite> queue = new ArrayList<>();
    private final Sprite.QuadVertices tempVertices = new Sprite.QuadVertices();

    // Компаратор для злиття однакових текстур всередині однакового Z-індексу
    private final Comparator<Sprite> batchComparator = (s1, s2) -> {
        if (s1.getZIndex() != s2.getZIndex()) {
            return Integer.compare(s1.getZIndex(), s2.getZIndex());
        }
        int id1 = (s1.getTexture() != null) ? s1.getTexture().getInstanceId() : -1;
        int id2 = (s2.getTexture() != null) ? s2.getTexture().getInstanceId() : -1;
        return Integer.compare(id1, id2);
    };

    public AutoBatchRenderer(int maxQuads) {
        this.maxQuads = maxQuads;

        int totalFloats = this.maxQuads * VERTICES_PER_QUAD * FLOATS_PER_VERTEX;
        this.vertexData = new float[totalFloats];

        // Виділення прямої нативної пам'яті для передачі в OpenGL драйвер
        ByteBuffer vbb = ByteBuffer.allocateDirect(totalFloats * BYTES_PER_FLOAT);
        vbb.order(ByteOrder.nativeOrder());
        this.vertexByteBuffer = vbb.asFloatBuffer();

        initGLBuffers();
    }

    public AutoBatchRenderer() {
        this(5000);
    }

    private void initGLBuffers() {
        final int[] buffers = new int[2];
        final int[] vaos = new int[1];

        // 1. Створюємо VAO
        GLES30.glGenVertexArrays(1, vaos, 0);
        this.vaoId = vaos[0];
        GLES30.glBindVertexArray(this.vaoId);

        // 2. Створюємо VBO та IBO
        GLES30.glGenBuffers(2, buffers, 0);
        this.vboId = buffers[0];
        this.iboId = buffers[1];

        // Налаштування VBO
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, this.vboId);
        GLES30.glBufferData(
                GLES30.GL_ARRAY_BUFFER,
                this.vertexData.length * BYTES_PER_FLOAT,
                null,
                GLES30.GL_DYNAMIC_DRAW
        );

        // 3. Генерація та заповнення індексного буфера (IBO)
        int totalIndices = this.maxQuads * INDICES_PER_QUAD;
        short[] indices = new short[totalIndices];

        for (int i = 0; i < this.maxQuads; i++) {
            int v = i * 4;
            int idx = i * 6;
            indices[idx]     = (short) (v);
            indices[idx + 1] = (short) (v + 1);
            indices[idx + 2] = (short) (v + 2);
            indices[idx + 3] = (short) (v + 2);
            indices[idx + 4] = (short) (v + 1);
            indices[idx + 5] = (short) (v + 3);
        }

        ByteBuffer ibb = ByteBuffer.allocateDirect(totalIndices * BYTES_PER_SHORT);
        ibb.order(ByteOrder.nativeOrder());
        ShortBuffer indexByteBuffer = ibb.asShortBuffer();
        indexByteBuffer.put(indices).position(0);

        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, this.iboId);
        GLES30.glBufferData(
                GLES30.GL_ELEMENT_ARRAY_BUFFER,
                totalIndices * BYTES_PER_SHORT,
                indexByteBuffer,
                GLES30.GL_STATIC_DRAW
        );

        // 4. Налаштування вершинних атрибутів (layout locations: 0=pos, 1=uv, 2=color)
        int stride = FLOATS_PER_VERTEX * BYTES_PER_FLOAT;

        // a_pos (location = 0): vec2
        GLES30.glEnableVertexAttribArray(0);
        GLES30.glVertexAttribPointer(0, 2, GLES30.GL_FLOAT, false, stride, 0);

        // a_uv (location = 1): vec2
        GLES30.glEnableVertexAttribArray(1);
        GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, stride, 2 * BYTES_PER_FLOAT);

        // a_color (location = 2): vec4
        GLES30.glEnableVertexAttribArray(2);
        GLES30.glVertexAttribPointer(2, 4, GLES30.GL_FLOAT, false, stride, 4 * BYTES_PER_FLOAT);

        // Відв'язуємо VAO
        GLES30.glBindVertexArray(0);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public void drawSprite(Sprite sprite) {
        if (!sprite.isVisible() || sprite.getOpacity() <= 0.0f || sprite.getTexture() == null) {
            return;
        }
        this.queue.add(sprite);
    }

    private void writeSpriteQuad(Sprite sprite) {
        sprite.computeVertices(this.tempVertices);

        float r = sprite.getR();
        float g = sprite.getG();
        float b = sprite.getB();
        float a = sprite.getOpacity();

        int ptr = this.quadCount * VERTICES_PER_QUAD * FLOATS_PER_VERTEX;

        // V0: Верхній лівий (Top-Left)
        vertexData[ptr++] = tempVertices.x0;
        vertexData[ptr++] = tempVertices.y0;
        vertexData[ptr++] = 0.0f;
        vertexData[ptr++] = 0.0f;
        vertexData[ptr++] = r;
        vertexData[ptr++] = g;
        vertexData[ptr++] = b;
        vertexData[ptr++] = a;

        // V1: Верхній правий (Top-Right)
        vertexData[ptr++] = tempVertices.x1;
        vertexData[ptr++] = tempVertices.y1;
        vertexData[ptr++] = 1.0f;
        vertexData[ptr++] = 0.0f;
        vertexData[ptr++] = r;
        vertexData[ptr++] = g;
        vertexData[ptr++] = b;
        vertexData[ptr++] = a;

        // V2: Нижній лівий (Bottom-Left)
        vertexData[ptr++] = tempVertices.x2;
        vertexData[ptr++] = tempVertices.y2;
        vertexData[ptr++] = 0.0f;
        vertexData[ptr++] = 1.0f;
        vertexData[ptr++] = r;
        vertexData[ptr++] = g;
        vertexData[ptr++] = b;
        vertexData[ptr++] = a;

        // V3: Нижній правий (Bottom-Right)
        vertexData[ptr++] = tempVertices.x3;
        vertexData[ptr++] = tempVertices.y3;
        vertexData[ptr++] = 1.0f;
        vertexData[ptr++] = 1.0f;
        vertexData[ptr++] = r;
        vertexData[ptr++] = g;
        vertexData[ptr++] = b;
        vertexData[ptr++] = a;

        this.quadCount++;
    }

    public void flush() {
        if (this.quadCount == 0 || this.currentTextureObj == null) {
            return;
        }

        // Прив'язуємо текстуру до текстурного юніту 0
        currentTextureObj.bind(0);

        int totalFloats = this.quadCount * VERTICES_PER_QUAD * FLOATS_PER_VERTEX;

        // Записуємо дані у Native Direct Buffer
        this.vertexByteBuffer.position(0);
        this.vertexByteBuffer.put(this.vertexData, 0, totalFloats);
        this.vertexByteBuffer.position(0);

        // Оновлюємо піддіапазон VBO
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, this.vboId);
        GLES30.glBufferSubData(
                GLES30.GL_ARRAY_BUFFER,
                0,
                totalFloats * BYTES_PER_FLOAT,
                this.vertexByteBuffer
        );
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);

        // Виконуємо відмальовку через зв'язаний VAO
        GLES30.glBindVertexArray(this.vaoId);
        GLES30.glDrawElements(
                GLES30.GL_TRIANGLES,
                this.quadCount * INDICES_PER_QUAD,
                GLES30.GL_UNSIGNED_SHORT,
                0
        );
        GLES30.glBindVertexArray(0);

        this.drawCalls++;
        this.quadCount = 0;
    }

    public void render() {
        this.drawCalls = 0;
        this.currentTextureObj = null;

        if (this.queue.isEmpty()) {
            return;
        }

        // Сортуємо: 1) Z-Index, 2) Texture ID
        this.queue.sort(this.batchComparator);

        int size = this.queue.size();
        for (int i = 0; i < size; i++) {
            Sprite sprite = this.queue.get(i);
            Texture2D tex = sprite.getTexture();

            if (tex != this.currentTextureObj || this.quadCount >= this.maxQuads) {
                this.flush();
                this.currentTextureObj = tex;
            }

            this.writeSpriteQuad(sprite);
        }

        this.flush();
        this.queue.clear();
    }

    public void dispose() {
        GLES30.glDeleteBuffers(2, new int[]{vboId, iboId}, 0);
        GLES30.glDeleteVertexArrays(1, new int[]{vaoId}, 0);
    }

    public int getDrawCalls() {
        return drawCalls;
    }
}