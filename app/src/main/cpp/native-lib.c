#include <jni.h>
#include <box2d/box2d.h>
#include <android/log.h>

#define TAG "Box2D3_JNI"

// Helper: Pack b2WorldId into jlong
static jlong packWorldId(b2WorldId id) {
    uint64_t val = ((uint64_t)id.index1 << 32) | (uint64_t)id.generation;
    return (jlong)val;
}

// Helper: Unpack jlong back to b2WorldId
static b2WorldId unpackWorldId(jlong handle) {
    uint64_t val = (uint64_t)handle;
    b2WorldId id;
    id.index1 = (int32_t)(val >> 32);
    id.generation = (uint16_t)(val & 0xFFFF);
    return id;
}

// Helper: Pack b2BodyId into jlong
static jlong packBodyId(b2BodyId id) {
    uint64_t val = ((uint64_t)id.index1 << 32) | ((uint64_t)id.world0 << 16) | (uint64_t)id.generation;
    return (jlong)val;
}

// Helper: Unpack jlong to b2BodyId
static b2BodyId unpackBodyId(jlong handle) {
    uint64_t val = (uint64_t)handle;
    b2BodyId id;
    id.index1 = (int32_t)(val >> 32);
    id.world0 = (uint16_t)((val >> 16) & 0xFFFF);
    id.generation = (uint16_t)(val & 0xFFFF);
    return id;
}

JNIEXPORT jlong JNICALL
Java_com_example_oleh_opengl2_PhysicsEngine_createWorld(JNIEnv *env, jobject thiz, jfloat gravityX, jfloat gravityY) {
    b2WorldDef worldDef = b2DefaultWorldDef();
    worldDef.gravity = (b2Vec2){gravityX, gravityY};

    b2WorldId worldId = b2CreateWorld(&worldDef);
    return packWorldId(worldId);
}

JNIEXPORT void JNICALL
Java_com_example_oleh_opengl2_PhysicsEngine_stepWorld(JNIEnv *env, jobject thiz, jlong worldHandle, jfloat timeStep, jint subSteps) {
b2WorldId worldId = unpackWorldId(worldHandle);
b2World_Step(worldId, timeStep, subSteps);
}

JNIEXPORT jlong JNICALL
Java_com_example_oleh_opengl2_PhysicsEngine_createBoxBody(JNIEnv *env, jobject thiz, jlong worldHandle,
        jfloat x, jfloat y, jfloat hx, jfloat hy, jboolean isDynamic) {
    b2WorldId worldId = unpackWorldId(worldHandle);

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = isDynamic ? b2_dynamicBody : b2_staticBody;
    bodyDef.position = (b2Vec2){x, y};

    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);

    // Attach box shape
    b2Polygon box = b2MakeBox(hx, hy);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.density = 1.0f;
    shapeDef.material.friction = 0.3f;
    b2CreatePolygonShape(bodyId, &shapeDef, &box);

    return packBodyId(bodyId);
}

JNIEXPORT void JNICALL
Java_com_example_oleh_opengl2_PhysicsEngine_getBodyTransform(JNIEnv *env, jobject thiz, jlong bodyHandle, jfloatArray outArray) {
b2BodyId bodyId = unpackBodyId(bodyHandle);
b2Vec2 pos = b2Body_GetPosition(bodyId);
b2Rot rot = b2Body_GetRotation(bodyId);

// Box2D 3 rotation is stored as cosine and sine: rot.c, rot.s
float radians = b2Rot_GetAngle(rot);
float degrees = radians * (180.0f / 3.14159265f);

float data[3] = { pos.x, pos.y, degrees };
(*env)->SetFloatArrayRegion(env, outArray, 0, 3, data);
}

JNIEXPORT void JNICALL
Java_com_example_oleh_opengl2_PhysicsEngine_destroyWorld(JNIEnv *env, jobject thiz, jlong worldHandle) {
b2WorldId worldId = unpackWorldId(worldHandle);
b2DestroyWorld(worldId);
}