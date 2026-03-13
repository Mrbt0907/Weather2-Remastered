#version 330 core

uniform sampler2D texture_sampler;


uniform int   fogmode;
uniform vec4  fogColor;
uniform float fogStart;
uniform float fogEnd;
uniform float fogDensity;


in vec2  outTexCoord;
in vec4  outRGBA;
in float fogDepth;


out vec4 fragColorOut;

void main()
{



    float fogFactor = 1.0;

    if (fogmode == 0) {

        fogFactor = (fogEnd - fogDepth) / (fogEnd - fogStart);
    } else if (fogmode == 1) {

        fogFactor = exp(-fogDensity * fogDepth);
    } else if (fogmode == 2) {

        float fd = fogDensity * fogDepth;
        fogFactor = exp(-fd * fd);
    }




    vec4 fragColor = texture(texture_sampler, outTexCoord);
    fragColor.x *= outRGBA.x;
    fragColor.y *= outRGBA.y;
    fragColor.z *= outRGBA.z;
    fragColor.w *= outRGBA.w;

    if (outRGBA.w > 0.0) {
        fogFactor = clamp(fogFactor, 0.0, 1.0);

        fragColorOut   = mix(fogColor, fragColor, fogFactor);
        fragColorOut.w = fragColor.w;
    } else {
        fragColorOut = fragColor;
    }
}