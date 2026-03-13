package net.extendedrenderer.shader;

import net.extendedrenderer.particle.ShaderManager;


public class ShaderProgramParticle extends ShaderProgram {

    private final int vertexShaderAttributeIndexPosition = 0;

    
    private final int vertexShaderAttributeTexCoord      = 1;

    
    private final int vertexShaderAttributeModelMatrix   = InstancedMeshParticle.vboSizeMesh;

    
    private final int vertexShaderAttributeBrightness    = InstancedMeshParticle.vboSizeMesh + 4;

    
    private final int vertexShaderAttributeRGBA          = InstancedMeshParticle.vboSizeMesh + 5;

    
    private final int vertexShaderAttributeLightmapCoord = InstancedMeshParticle.vboSizeMesh + 6;



    public ShaderProgramParticle(String name) throws Exception {
        super(name);
    }

    @Override
    public void setupAttribLocations() {
        final int offset = 0;

        ShaderManager.glBindAttribLocation(getProgramId(), vertexShaderAttributeIndexPosition + offset, "position");
        ShaderManager.glBindAttribLocation(getProgramId(), vertexShaderAttributeTexCoord      + offset, "texCoord");



        ShaderManager.glBindAttribLocation(getProgramId(), vertexShaderAttributeModelMatrix   + offset, "modelMatrix");

        ShaderManager.glBindAttribLocation(getProgramId(), vertexShaderAttributeBrightness    + offset, "brightness");
        ShaderManager.glBindAttribLocation(getProgramId(), vertexShaderAttributeRGBA          + offset, "rgba");


        ShaderManager.glBindAttribLocation(getProgramId(), vertexShaderAttributeLightmapCoord + offset, "lightmapCoord");
    }
}