#version 120

varying vec3 mesh_pos;
varying vec4 fragment_color;
varying vec3 particle_size;

void main()
{
    vec3 localPos = vec3(
        mesh_pos.x / particle_size.x,
        mesh_pos.y / particle_size.y,
        mesh_pos.z / particle_size.z
    );

    float dist2 = dot(localPos, localPos);
    if(dist2 > 1.0) discard;

    float density = 1.0 - sqrt(dist2);
    density = pow(density, 0.2);

    vec3 color = fragment_color.rgb;
    gl_FragColor = vec4(color, fragment_color.a * density);
}