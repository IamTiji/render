#version 430 core

layout (location = 0) in vec3 iPos;
layout (location = 1) in vec2 iUv;

out vec2 aUv;

void main() {
    gl_Position = vec4(iPos, 1.0);
    aUv = iUv;
}