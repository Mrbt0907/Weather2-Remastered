#version 330 core

in vec3 position;
in vec2 texCoord;
in vec3 vertexNormal;
in float brightness;
in vec4 rgba;

in mat4 modelMatrix;


out vec2  outTexCoord;
out vec4  outRGBA;
out float fogDepth;


uniform mat4 modelViewMatrixCamera;

void main()
{
    vec4 eyePos = modelViewMatrixCamera * modelMatrix * vec4(position, 1.0);
    gl_Position = eyePos;

    fogDepth = abs(eyePos.z);

    outTexCoord = texCoord;
    int lightMap = int(brightness);
    float lR = float((lightMap >> 16) & 255) / 255.0;
    float lG = float((lightMap >>  8) & 255) / 255.0;
    float lB = float( lightMap        & 255) / 255.0;

    outRGBA   = rgba;
    outRGBA.x = rgba.x * lR;
    outRGBA.y = rgba.y * lG;
    outRGBA.z = rgba.z * lB;

}