attribute vec3 a_Position;
attribute vec4 a_Color;
varying vec4 v_Color;
uniform mat4 mvp;
void main() {
    v_Color = a_Color;
    gl_Position = mvp * vec4(a_Position,1);
}