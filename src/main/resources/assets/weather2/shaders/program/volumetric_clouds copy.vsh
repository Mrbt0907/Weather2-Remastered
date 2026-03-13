#version 120

// The data that was passed to this shader via a texture
uniform sampler2D particle_data;
uniform int particle_index;
// The pov of the screen
uniform vec3 camera;
// Used to determine how many vertices to render
uniform int quality;

uniform int width;
uniform int height;

attribute vec3 position;
varying vec3 local_pos;
varying vec3 world_pos;
varying vec4 fragment_color;

void main()
{
    // Gather UVs for sampling
    float u1 = 0.33F; // 1st Texel
    float u2 = 0.66F; // 2nd Texel
    float u3 = 0.99F; // 3rd Texel
    float v = (float(particle_index) + 0.5F) / float(height);

    // Sample the data for the specified particle
    vec4 tex1 = texture2D(particle_data, vec2(u1, v));
    vec4 tex2 = texture2D(particle_data, vec2(u2, v));
    vec4 tex3 = texture2D(particle_data, vec2(u3, v));

    // Extract the data from the texels
    vec3 particle_pos = tex1.xyz * 1000.0F;
    float particle_height = tex1.w * 1000.0F;
    float particle_width = tex2.x * 1000.0F;
    vec4 color = vec4(tex2.y, tex2.z, tex2.w, tex3.x);
    float brightness = tex3.y;

    // Use the data and pass color to the fragment shader
    local_pos = position * vec3(particle_width, particle_height, particle_width);
    world_pos = particle_pos + local_pos - camera;
    gl_Position = gl_ModelViewProjectionMatrix * vec4(world_pos, 1.0F);
    fragment_color = color;
}