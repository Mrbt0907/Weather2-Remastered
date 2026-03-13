#version 120

attribute vec3 position;
varying vec2 particle_uv;
varying vec4 fragment_color;
varying vec3 particle_size;
varying vec3 mesh_pos;

uniform vec3 camera;
uniform int quality;
uniform float brightness;
uniform float particle_height;
uniform float particle_width;
uniform vec2 particle_rotation;
uniform vec4 color;
uniform vec3 particle_pos;



void main()
{
    particle_size = vec3(particle_width, particle_width, particle_height);
    particle_uv = position.xy;
    float yC = cos(radians(particle_rotation.x)); float yS = sin(radians(particle_rotation.x)); float pC = cos(radians(particle_rotation.y)); float pS = sin(radians(particle_rotation.y));
    mesh_pos = position * vec3(particle_width, particle_width, particle_height);
    mesh_pos = vec3(mesh_pos.x, mesh_pos.y * pC - mesh_pos.z * pS, mesh_pos.y * pS + mesh_pos.z * pC);
    mesh_pos = vec3(mesh_pos.x * yC + mesh_pos.z * yS, mesh_pos.y, -mesh_pos.x * yS + mesh_pos.z * yC);
    gl_Position = gl_ModelViewProjectionMatrix * vec4(particle_pos + mesh_pos - camera, 1.0F);
    fragment_color = color * vec4(brightness, brightness, brightness, 1.0F);
}