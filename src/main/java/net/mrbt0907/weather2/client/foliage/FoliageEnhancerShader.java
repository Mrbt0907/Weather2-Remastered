package net.mrbt0907.weather2.client.foliage;

import net.CoroUtil.config.ConfigCoroUtil;
import net.CoroUtil.util.CoroUtilBlockLightCache;
import net.CoroUtil.util.Vec3;
import net.extendedrenderer.EventHandler;
import net.extendedrenderer.ExtendedRenderer;
import net.extendedrenderer.foliage.Foliage;
import net.extendedrenderer.foliage.FoliageData;
import net.extendedrenderer.particle.ParticleRegistry;
import net.extendedrenderer.render.FoliageRenderer;
import net.extendedrenderer.render.RotatingParticleManager;
import net.extendedrenderer.shader.InstancedMeshFoliage;
import net.extendedrenderer.shader.MeshBufferManagerFoliage;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.config.ConfigFoliage;
import net.mrbt0907.weather2.config.ConfigMisc;
import net.mrbt0907.weather2.config.EZConfigParser;

import org.lwjgl.BufferUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("deprecation")
public class FoliageEnhancerShader implements Runnable {

    public static boolean useThread = true;

    public static volatile boolean meshesReady = false;

    public static List<FoliageReplacerBase> listFoliageReplacers = new ArrayList<>();

    public static ConcurrentHashMap<BlockPos, FoliageLocationData> lookupPosToFoliage = new ConcurrentHashMap<>();

    public static ModelLoader modelLoader;
    public static Map<ResourceLocation, IBakedModel> modelRegistry;
    public static HashMap<ModelResourceLocation, IBakedModel> lookupBackupReplacedModels = new HashMap<>();

    public static void modelBakeEvent(ModelBakeEvent event) {
        modelLoader = event.getModelLoader();
        modelRegistry = event.getModelRegistry();
        processModels();
    }

    public static void liveReloadModels() {
        processModels();
        Minecraft.getInstance().levelRenderer.allChanged();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void processModels() {

        if (modelLoader == null || modelRegistry == null) {
            Weather2.error("modelLoader or modelRegistry null, aborting");
            return;
        }

        boolean replaceVanillaModels = ConfigCoroUtil.foliageShaders
                && EventHandler.queryUseOfShaders()
                && !ConfigMisc.toaster_pc_mode;

        FoliageData.backupBakedModelStore.clear();

        if (!replaceVanillaModels) {
            return;
        }

        lookupBackupReplacedModels.clear();

        String str = "Weather2: Replacing shaderized models";
        Weather2.debug(str);

        Set<ResourceLocation> targetSprites = new HashSet<>();
        for (FoliageReplacerBase replacer : listFoliageReplacers) {
            net.minecraft.block.material.Material mat = replacer.state.getMaterial();
            if (mat == net.minecraft.block.material.Material.PLANT
                    || mat == net.minecraft.block.material.Material.REPLACEABLE_PLANT) {
                continue;
            }
            for (TextureAtlasSprite sprite : replacer.sprites) {
                targetSprites.add(sprite.getName());
            }
        }

        IBakedModel blank = null;
        ModelResourceLocation blankKey = new ModelResourceLocation("coroutil:blank", "");
        blank = modelRegistry.get(blankKey);
        if (blank == null) {
            for (ResourceLocation key : modelRegistry.keySet()) {
                if (key.getNamespace().equals("coroutil") && key.getPath().contains("blank")) {
                    blank = modelRegistry.get(key);
                    break;
                }
            }
        }

        if (blank == null) {
            Weather2.error("[FoliageEnhancerShader] processModels: blank model NOT FOUND, cannot replace models");
            return;
        }

        try {
            Minecraft.getInstance().getModelManager().getBlockModelShaper().rebuildCache();
            java.lang.reflect.Field cacheField = net.minecraft.client.renderer.BlockModelShapes.class
                    .getDeclaredField("modelByStateCache");
            cacheField.setAccessible(true);
            Map<net.minecraft.block.BlockState, IBakedModel> bakedModelStore =
                    (Map<net.minecraft.block.BlockState, IBakedModel>) cacheField.get(
                            Minecraft.getInstance().getModelManager().getBlockModelShaper());
            for (Map.Entry<net.minecraft.block.BlockState, IBakedModel> entry : bakedModelStore.entrySet()) {
                FoliageData.backupBakedModelStore.put(entry.getKey(), entry.getValue());
            }
        } catch (Exception e) {
            Weather2.error("Failed to reflect BlockModelShaper cache: " + e);
        }

        for (ResourceLocation res : new ArrayList<>(modelRegistry.keySet())) {
            if (!(res instanceof ModelResourceLocation)) continue;
            ModelResourceLocation mrl = (ModelResourceLocation) res;
            if (mrl.getVariant().equals("inventory")) continue;
            if (res.toString().contains("flower_pot") || res.toString().contains("potted")) continue;

            IBakedModel bakedModel = modelRegistry.get(res);
            if (bakedModel == null) continue;

            TextureAtlasSprite particle = bakedModel.getParticleIcon();
            if (particle == null) continue;

            if (targetSprites.contains(particle.getName())) {
                lookupBackupReplacedModels.put(mrl, bakedModel);
                modelRegistry.put(res, blank);
            }
        }

        Minecraft.getInstance().getModelManager().getBlockModelShaper().rebuildCache();
    }

    public static void shadersInit() {
        meshesReady = false;

        FoliageEnhancerShader.setupReplacers();

        Weather2.debug("Weather2: Setting up meshes for foliage shader");

        for (FoliageReplacerBase replacer : listFoliageReplacers) {
            for (TextureAtlasSprite sprite : replacer.sprites) {
                MeshBufferManagerFoliage.setupMeshIfMissing(sprite);
            }
        }

        if (modelLoader != null && modelRegistry != null) {
            processModels();
        } else {
            Weather2.error("[FoliageEnhancerShader] shadersInit: modelLoader or modelRegistry is null, cannot replace models. Was modelBakeEvent registered?");
        }

        meshesReady = true;
    }

    public static void setupReplacersAndMeshes() {
    }

    public static void shadersReset() {
        meshesReady = false;
        lookupPosToFoliage.clear();
    }

    @SuppressWarnings("rawtypes")
    public static void setupReplacers() {

        Weather2.debug("Weather2: Setting up foliage replacers");

        listFoliageReplacers.clear();

        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.OAK_SAPLING.defaultBlockState())
                .setSprite(getMeshAndSetupSprite("minecraft:block/oak_sapling"))
                .setBiomeColorize(false));
        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.SPRUCE_SAPLING.defaultBlockState())
                .setSprite(getMeshAndSetupSprite("minecraft:block/spruce_sapling"))
                .setBiomeColorize(false));
        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.BIRCH_SAPLING.defaultBlockState())
                .setSprite(getMeshAndSetupSprite("minecraft:block/birch_sapling"))
                .setBiomeColorize(false));
        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.JUNGLE_SAPLING.defaultBlockState())
                .setSprite(getMeshAndSetupSprite("minecraft:block/jungle_sapling"))
                .setBiomeColorize(false));
        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.ACACIA_SAPLING.defaultBlockState())
                .setSprite(getMeshAndSetupSprite("minecraft:block/acacia_sapling"))
                .setBiomeColorize(false));
        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.DARK_OAK_SAPLING.defaultBlockState())
                .setSprite(getMeshAndSetupSprite("minecraft:block/dark_oak_sapling"))
                .setBiomeColorize(false));

        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.DEAD_BUSH.defaultBlockState())
                .setSprite(getMeshAndSetupSprite("minecraft:block/dead_bush"))
                .setRandomizeCoord(false)
                .setBiomeColorize(false));
        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.GRASS.defaultBlockState())
                .setSprite(getMeshAndSetupSprite("minecraft:block/grass"))
                .setRandomizeCoord(false)
                .setBiomeColorize(true));
        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.FERN.defaultBlockState())
                .setSprite(getMeshAndSetupSprite("minecraft:block/fern"))
                .setRandomizeCoord(false)
                .setBiomeColorize(true));

        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.DANDELION.defaultBlockState())
                .setSprite(getMeshAndSetupSprite("minecraft:block/dandelion"))
                .setRandomizeCoord(false)
                .setBiomeColorize(false));

        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.ALLIUM.defaultBlockState())
                .setSprite(getMeshAndSetupSprite("minecraft:block/allium"))
                .setRandomizeCoord(false).setBiomeColorize(false));
        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.BLUE_ORCHID.defaultBlockState())
                .setSprite(getMeshAndSetupSprite("minecraft:block/blue_orchid"))
                .setRandomizeCoord(false).setBiomeColorize(false));
        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.AZURE_BLUET.defaultBlockState())
                .setSprite(getMeshAndSetupSprite("minecraft:block/azure_bluet"))
                .setRandomizeCoord(false).setBiomeColorize(false));
        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.ORANGE_TULIP.defaultBlockState())
                .setSprite(getMeshAndSetupSprite("minecraft:block/orange_tulip"))
                .setRandomizeCoord(false).setBiomeColorize(false));
        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.OXEYE_DAISY.defaultBlockState())
                .setSprite(getMeshAndSetupSprite("minecraft:block/oxeye_daisy"))
                .setRandomizeCoord(false).setBiomeColorize(false));
        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.PINK_TULIP.defaultBlockState())
                .setSprite(getMeshAndSetupSprite("minecraft:block/pink_tulip"))
                .setRandomizeCoord(false).setBiomeColorize(false));
        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.POPPY.defaultBlockState())
                .setSprite(getMeshAndSetupSprite("minecraft:block/poppy"))
                .setRandomizeCoord(false).setBiomeColorize(false));
        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.RED_TULIP.defaultBlockState())
                .setSprite(getMeshAndSetupSprite("minecraft:block/red_tulip"))
                .setRandomizeCoord(false).setBiomeColorize(false));
        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.WHITE_TULIP.defaultBlockState())
                .setSprite(getMeshAndSetupSprite("minecraft:block/white_tulip"))
                .setRandomizeCoord(false).setBiomeColorize(false));
        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.CORNFLOWER.defaultBlockState())
                .setSprite(getMeshAndSetupSprite("minecraft:block/cornflower"))
                .setRandomizeCoord(false).setBiomeColorize(false));

        for (int i = 0; i < 8; i++) {
            int temp = i;
            listFoliageReplacers.add(new FoliageReplacerCross(Blocks.WHEAT.defaultBlockState())
                    .setBaseMaterial(Material.DIRT)
                    .setSprite(getMeshAndSetupSprite("minecraft:block/wheat_stage" + temp))
                    .setRandomizeCoord(false)
                    .setStateSensitive(true)
                    .addComparable(CropsBlock.AGE, i));
        }

        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.SUGAR_CANE.defaultBlockState(), -1)
                .setSprite(getMeshAndSetupSprite("minecraft:block/sugar_cane"))
                .setBaseMaterial(Material.SAND)
                .setBiomeColorize(true)
                .setRandomizeCoord(false)
                .setLooseness(0.3F));

        HashMap<Integer, Integer> lookupStateToModel = new HashMap<>();
        lookupStateToModel.put(0, 0);
        lookupStateToModel.put(1, 0);
        lookupStateToModel.put(2, 1);
        lookupStateToModel.put(3, 1);
        lookupStateToModel.put(4, 2);
        lookupStateToModel.put(5, 2);
        lookupStateToModel.put(6, 2);
        lookupStateToModel.put(7, 3);

        for (Map.Entry<Integer, Integer> entrySet : lookupStateToModel.entrySet()) {
            listFoliageReplacers.add(new FoliageReplacerCross(Blocks.CARROTS.defaultBlockState())
                    .setBaseMaterial(Material.DIRT)
                    .setSprite(getMeshAndSetupSprite("minecraft:block/carrots_stage" + entrySet.getValue()))
                    .setRandomizeCoord(false)
                    .setStateSensitive(true)
                    .addComparable(CropsBlock.AGE, entrySet.getKey()));
        }

        for (Map.Entry<Integer, Integer> entrySet : lookupStateToModel.entrySet()) {
            listFoliageReplacers.add(new FoliageReplacerCross(Blocks.POTATOES.defaultBlockState())
                    .setBaseMaterial(Material.DIRT)
                    .setSprite(getMeshAndSetupSprite("minecraft:block/potatoes_stage" + entrySet.getValue()))
                    .setRandomizeCoord(false)
                    .setStateSensitive(true)
                    .addComparable(CropsBlock.AGE, entrySet.getKey()));
        }

        for (int i = 0; i < 4; i++) {
            listFoliageReplacers.add(new FoliageReplacerCross(Blocks.BEETROOTS.defaultBlockState())
                    .setBaseMaterial(Material.DIRT)
                    .setSprite(getMeshAndSetupSprite("minecraft:block/beetroots_stage" + i))
                    .setRandomizeCoord(false)
                    .setStateSensitive(true)
                    .addComparable(BeetrootBlock.AGE, i));
        }

        List<TextureAtlasSprite> sprites = new ArrayList<>();
        sprites.add(getMeshAndSetupSprite("minecraft:block/tall_grass_bottom"));
        sprites.add(getMeshAndSetupSprite("minecraft:block/tall_grass_top"));
        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.TALL_GRASS.defaultBlockState(), 2)
                .setSprites(sprites));

        sprites = new ArrayList<>();
        sprites.add(getMeshAndSetupSprite("minecraft:block/rose_bush_bottom"));
        sprites.add(getMeshAndSetupSprite("minecraft:block/rose_bush_top"));
        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.ROSE_BUSH.defaultBlockState(), 2)
                .setSprites(sprites)
                .setBiomeColorize(false));

        sprites = new ArrayList<>();
        sprites.add(getMeshAndSetupSprite("minecraft:block/large_fern_bottom"));
        sprites.add(getMeshAndSetupSprite("minecraft:block/large_fern_top"));
        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.LARGE_FERN.defaultBlockState(), 2)
                .setSprites(sprites)
                .setBiomeColorize(true));

        sprites = new ArrayList<>();
        sprites.add(getMeshAndSetupSprite("minecraft:block/peony_bottom"));
        sprites.add(getMeshAndSetupSprite("minecraft:block/peony_top"));
        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.PEONY.defaultBlockState(), 2)
                .setSprites(sprites)
                .setBiomeColorize(false));

        sprites = new ArrayList<>();
        sprites.add(getMeshAndSetupSprite("minecraft:block/lilac_bottom"));
        sprites.add(getMeshAndSetupSprite("minecraft:block/lilac_top"));
        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.LILAC.defaultBlockState(), 2)
                .setSprites(sprites)
                .setBiomeColorize(false));

        if (ConfigFoliage.enable_extra_grass) {
            listFoliageReplacers.add(new FoliageReplacerCrossGrass(Blocks.AIR.defaultBlockState()) {
                @Override
                public boolean isActive() {
                    return ConfigFoliage.enable_extra_grass;
                }
            }
                    .setSprite(getMeshAndSetupSprite(ExtendedRenderer.modid + ":particles/grass"))
                    .setRandomizeCoord(true)
                    .setBiomeColorize(true));
        }

        boolean extraLeaves = false;
        if (extraLeaves) {
            listFoliageReplacers.add(new FoliageReplacerCrossLeaves(Blocks.OAK_LEAVES.defaultBlockState())
                    .setSprite(getMeshAndSetupSprite("minecraft:block/grass"))
                    .setBiomeColorize(true));
        }
    }

    public static TextureAtlasSprite getMeshAndSetupSprite(String spriteLoc) {
        AtlasTexture atlas = Minecraft.getInstance()
                .getModelManager()
                .getAtlas(AtlasTexture.LOCATION_BLOCKS);
        ResourceLocation rl;
        if (spriteLoc.contains(":")) {
            String[] parts = spriteLoc.split(":", 2);
            rl = new ResourceLocation(parts[0], parts[1]);
        } else {
            rl = new ResourceLocation(spriteLoc);
        }
        TextureAtlasSprite sprite = atlas.getSprite(rl);
        ResourceLocation spriteName = sprite.getName();
        if (spriteName.toString().contains("missingno") || spriteName.toString().contains("missing")) {
            Weather2.error("[FoliageEnhancerShader] getMeshAndSetupSprite: MISSING TEXTURE for requested='"
                    + rl + "' got='" + spriteName + "'");
        }
        return sprite;
    }

    @Override
    public void run() {
        if (useThread) {
            while (true) {
                try {
                    if (!meshesReady) {
                        Thread.sleep(100);
                        continue;
                    }
                    if (ConfigCoroUtil.foliageShaders && RotatingParticleManager.useShaders && !ConfigMisc.toaster_pc_mode) {
                        boolean gotLock = tickClientThreaded();
                        if (gotLock) {
                            Thread.sleep(ConfigFoliage.shader_process_delay);
                        } else {
                            Thread.sleep(20);
                        }
                    } else {
                        Thread.sleep(5000);
                    }
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        }
    }

    public static boolean tickClientCloseToPlayer() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.player != null
                && EZConfigParser.isEffectsEnabled(mc.level.dimension().location())) {
            return tickFoliage(5, false);
        } else {
            return true;
        }
    }

    public static boolean tickClientThreaded() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.player != null
                && EZConfigParser.isEffectsEnabled(mc.level.dimension().location())) {
            return tickFoliage(ConfigFoliage.shader_range, true);
        } else {
            return true;
        }
    }

    @SuppressWarnings("finally")
    public static boolean tickFoliage(int radialRange, boolean trimRange) {
        if (ExtendedRenderer.foliageRenderer.lockVBO2.tryLock()) {
            try {
                return profileForFoliageShader(radialRange, trimRange);
            } finally {
                ExtendedRenderer.foliageRenderer.lockVBO2.unlock();
                return true;
            }
        } else {
            return false;
        }
    }

    @SuppressWarnings("finally")
    public static boolean profileForFoliageShader(int radialRange, boolean trimRange) {

        World world = Minecraft.getInstance().level;
        Entity entityIn = Minecraft.getInstance().getCameraEntity();

        if (world == null || entityIn == null) return true;

        BlockPos pos = entityIn.blockPosition();

        boolean add = true;
        boolean trim = true;

        int xzRange = radialRange;
        int yRange = radialRange;
        Random rand = new Random();

        double centerX = entityIn.getX();
        double centerY = entityIn.getY();
        double centerZ = entityIn.getZ();

        for (TextureAtlasSprite sprite : ExtendedRenderer.foliageRenderer.foliage.keySet()) {
            InstancedMeshFoliage mesh = MeshBufferManagerFoliage.getMesh(sprite);
            if (mesh == null) continue;
            mesh.lastAdditionCount = 0;
            mesh.lastRemovalCount = 0;
        }

        if (trim) {
            Iterator<Map.Entry<BlockPos, FoliageLocationData>> it = lookupPosToFoliage.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<BlockPos, FoliageLocationData> entry = it.next();
                if (!entry.getValue().foliageReplacer.isActive()
                        || !entry.getValue().foliageReplacer.validFoliageSpot(world, entry.getKey().below())) {
                    it.remove();
                    for (Foliage entry2 : entry.getValue().listFoliage) {
                        entry.getValue().foliageReplacer.markMeshesDirty();
                        ExtendedRenderer.foliageRenderer.getFoliageForSprite(entry2.particleTexture).remove(entry2);
                        InstancedMeshFoliage m = MeshBufferManagerFoliage.getMesh(entry2.particleTexture);
                        if (m != null) m.lastRemovalCount++;
                    }
                } else if (trimRange
                        && entry.getKey().distSqr(centerX, centerY, centerZ, false) > (long) radialRange * radialRange) {
                    it.remove();
                    for (Foliage entry2 : entry.getValue().listFoliage) {
                        entry.getValue().foliageReplacer.markMeshesDirty();
                        ExtendedRenderer.foliageRenderer.getFoliageForSprite(entry2.particleTexture).remove(entry2);
                        InstancedMeshFoliage m = MeshBufferManagerFoliage.getMesh(entry2.particleTexture);
                        if (m != null) m.lastRemovalCount++;
                    }
                }
            }
        }

        if (add) {
            for (int x = -xzRange; x <= xzRange; x++) {
                for (int z = -xzRange; z <= xzRange; z++) {
                    for (int y = -yRange; y <= yRange; y++) {
                        BlockPos posScan = pos.offset(x, y, z);
                        if (!lookupPosToFoliage.containsKey(posScan)) {
                            if (posScan.distSqr(centerX, centerY, centerZ, false) <= (long) radialRange * radialRange) {
                                for (FoliageReplacerBase replacer : listFoliageReplacers) {
                                    if (replacer.isActive()
                                            && replacer.validFoliageSpot(entityIn.level, posScan.below())) {
                                        replacer.addForPos(entityIn.level, posScan);
                                        replacer.markMeshesDirty();
                                        for (TextureAtlasSprite sprite : replacer.sprites) {
                                            InstancedMeshFoliage m = MeshBufferManagerFoliage.getMesh(sprite);
                                            if (m != null) m.lastAdditionCount++;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        try {
            for (Map.Entry<TextureAtlasSprite, List<Foliage>> entry
                    : ExtendedRenderer.foliageRenderer.foliage.entrySet()) {
                InstancedMeshFoliage mesh = MeshBufferManagerFoliage.getMesh(entry.getKey());
                if (mesh == null) continue;

                if (mesh.dirtyVBO2Flag) {
                    mesh.interpPosXThread = entityIn.getX();
                    mesh.interpPosYThread = entityIn.getY();
                    mesh.interpPosZThread = entityIn.getZ();

                    updateVBO2Threaded(entry.getKey());
                    mesh.dirtyVBO2Flag = true;
                }
            }
        } finally {
            return true;
        }
    }

    public static void markMeshDirty(TextureAtlasSprite sprite, boolean flag) {
        InstancedMeshFoliage mesh = MeshBufferManagerFoliage.getMesh(sprite);
        if (mesh != null) {
            mesh.dirtyVBO2Flag = flag;
        } else {
            Weather2.debug("MESH NULL HERE, FIX INIT ORDER");
        }
    }

    @SuppressWarnings("static-access")
    public static void updateVBO2Threaded(TextureAtlasSprite sprite) {

        Minecraft mc = Minecraft.getInstance();
        Entity entityIn = mc.getCameraEntity();
        if (entityIn == null) return;

        float partialTicks = 1F;

        InstancedMeshFoliage mesh = MeshBufferManagerFoliage.getMesh(sprite);
        if (mesh == null) return;

        int lastPos = mesh.curBufferPosVBO2;

        mesh.curBufferPosVBO2 = 0;
        mesh.instanceDataBufferVBO2.clear();

        int guessAtExtraMeshesPerFoliage = 4;
        int extraMeshes = (mesh.lastAdditionCount * guessAtExtraMeshesPerFoliage)
                - (mesh.lastRemovalCount * guessAtExtraMeshesPerFoliage);

        if (lastPos + extraMeshes > mesh.numInstances) {
            if (mesh.numInstances * 4 < lastPos + extraMeshes) {
                mesh.numInstances = (int)(Math.ceil((float)(lastPos + extraMeshes) / 10000F) * 10000F);
            } else {
                mesh.numInstances *= 4;
            }
            mesh.instanceDataBufferVBO2 = BufferUtils.createFloatBuffer(
                    mesh.numInstances * InstancedMeshFoliage.INSTANCE_SIZE_FLOATS_SELDOM);
            mesh.instanceDataBufferVBO2.clear();

            mesh.instanceDataBufferVBO1 = BufferUtils.createFloatBuffer(
                    mesh.numInstances * InstancedMeshFoliage.INSTANCE_SIZE_FLOATS);
        }

        for (Foliage foliage : ExtendedRenderer.foliageRenderer.getFoliageForSprite(sprite)) {
            foliage.updateQuaternion(entityIn);
            foliage.renderForShaderVBO2(mesh, ExtendedRenderer.foliageRenderer.transformation,
                    null, entityIn, partialTicks);
        }

        if (FoliageRenderer.testStaticLimit) {
            mesh.instanceDataBufferVBO2.limit(30000 * mesh.INSTANCE_SIZE_FLOATS_SELDOM);
        } else {
            mesh.instanceDataBufferVBO2.limit(mesh.curBufferPosVBO2 * mesh.INSTANCE_SIZE_FLOATS_SELDOM);
        }
    }

    public static void addForPos(FoliageReplacerBase replacer, int height, BlockPos pos) {
        addForPos(replacer, height, pos, new Vec3(0.4, 0, 0.4), true, 0);
    }

    public static void addForPos(FoliageReplacerBase replacer, int height, BlockPos pos,
                                 Vec3 randPosVar, boolean biomeColorize) {
        addForPos(replacer, height, pos, randPosVar, biomeColorize, 0);
    }

    public static void addForPos(FoliageReplacerBase replacer, int height, BlockPos pos,
                                 Vec3 randPosVar, boolean biomeColorize, int colorizeOffset) {
        addForPos(replacer, height, pos, randPosVar, biomeColorize, colorizeOffset, null);
    }

    public static void addForPos(FoliageReplacerBase replacer, int height, BlockPos pos,
                                 Vec3 randPosVar, boolean biomeColorize, int colorizeOffset, Vec3 extraPos) {

        World world = Minecraft.getInstance().level;
        if (world == null) return;

        Random rand = new Random();
        FoliageLocationData data = new FoliageLocationData(replacer);

        int heightIndex;

        float randX = 0;
        float randZ = 0;
        if (randPosVar != null) {
            randX = (rand.nextFloat() - rand.nextFloat()) * (float) randPosVar.xCoord;
            randZ = (rand.nextFloat() - rand.nextFloat()) * (float) randPosVar.zCoord;
        }

        int clutterSize = 2;
        int meshesPerLayer = 2;

        if (replacer instanceof FoliageReplacerCross) {
            clutterSize = 2 * height;
        }
        if (replacer instanceof FoliageReplacerCrossGrass) {
            clutterSize = 4;
        }

        for (int i = 0; i < clutterSize; i++) {
            heightIndex = i / meshesPerLayer;

            if (replacer instanceof FoliageReplacerCrossGrass) {
                heightIndex = 0;
            }

            TextureAtlasSprite sprite = replacer.sprites.get(0);
            if (replacer instanceof FoliageReplacerCross) {
                if (heightIndex < replacer.sprites.size()) {
                    sprite = replacer.sprites.get(heightIndex);
                }
            }

            Foliage foliage = new Foliage(sprite);
            foliage.setPosition(pos.offset(0, 0, 0));
            foliage.prevPosY = foliage.posY;
            foliage.heightIndex = heightIndex;

            Vector3d vec = world.getBlockState(pos).getOffset(world, pos);
            foliage.posX += 0.5F + randX + vec.x;
            foliage.prevPosX = foliage.posX;
            foliage.posZ += 0.5F + randZ + vec.z;
            if (extraPos != null) {
                foliage.posX += extraPos.xCoord;
                foliage.posZ += extraPos.zCoord;
            }
            foliage.prevPosZ = foliage.posZ;

            foliage.rotationYaw = world.random.nextInt(360);
            foliage.rotationYaw = 45;
            if ((i + 1) % 2 == 0) {
                foliage.rotationYaw += 90;
            }

            if (replacer instanceof FoliageReplacerCrossGrass) {
                foliage.rotationYaw = 45;
                double dist = 0.17;
                if (i == 0) {
                    foliage.rotationYaw += 90;
                    foliage.posX += dist;
                    foliage.posZ += dist;
                } else if (i == 1) {
                    foliage.rotationYaw += 90;
                    foliage.posX -= dist;
                    foliage.posZ -= dist;
                } else if (i == 2) {
                    foliage.posX += dist;
                    foliage.posZ -= dist;
                } else if (i == 3) {
                    foliage.posX -= dist;
                    foliage.posZ += dist;
                }
            }

            foliage.looseness = replacer.looseness;
            foliage.particleScale /= 0.2;

            if (biomeColorize) {
                int color = Minecraft.getInstance().getBlockColors().getColor(
                        world.getBlockState(pos.above(colorizeOffset)), world, pos.above(colorizeOffset), 0);
                foliage.particleRed   = (float)(color >> 16 & 255) / 255.0F;
                foliage.particleGreen = (float)(color >> 8  & 255) / 255.0F;
                foliage.particleBlue  = (float)(color       & 255) / 255.0F;

                if (replacer instanceof FoliageReplacerCrossGrass) {
                    color = Minecraft.getInstance().getBlockColors().getColor(
                            Blocks.GRASS.defaultBlockState(), world, pos.above(colorizeOffset), 0);
                    foliage.particleRed   = (float)(color >> 16 & 255) / 255.0F;
                    foliage.particleGreen = (float)(color >> 8  & 255) / 255.0F;
                    foliage.particleBlue  = (float)(color       & 255) / 255.0F;
                }
            }

            foliage.brightnessCache = CoroUtilBlockLightCache.brightnessPlayer;

            data.listFoliage.add(foliage);
            ExtendedRenderer.foliageRenderer.getFoliageForSprite(sprite).add(foliage);
        }

        lookupPosToFoliage.put(pos, data);
    }

    public void addForPosSeaweed(BlockPos pos) {

        World world = Minecraft.getInstance().level;
        if (world == null) return;

        Random rand = new Random();
        BlockState state = world.getBlockState(pos.below());
        List<Foliage> listClutter = new ArrayList<>();

        int heightIndex = 0;

        float variance = 0.4F;
        float randX = (rand.nextFloat() - rand.nextFloat()) * variance;
        float randZ = (rand.nextFloat() - rand.nextFloat()) * variance;

        int clutterSize = rand.nextInt(7) * 2;

        for (int i = 0; i < clutterSize; i++) {
            heightIndex = i / 2;
            TextureAtlasSprite sprite = ParticleRegistry.listSeaweed.get(heightIndex);
            Foliage foliage = new Foliage(sprite);
            foliage.setPosition(pos.offset(0, 0, 0));
            foliage.posY += 0.0F;
            foliage.prevPosY = foliage.posY;
            foliage.heightIndex = heightIndex;
            foliage.posX += 0.5F + randX;
            foliage.prevPosX = foliage.posX;
            foliage.posZ += 0.5F + randZ;
            foliage.prevPosZ = foliage.posZ;

            foliage.rotationYaw = 45;
            if ((i + 1) % 2 == 0) {
                foliage.rotationYaw += 90;
            }
            foliage.rotationYaw = 0;
            if ((i + 1) % 2 == 0) {
                foliage.rotationYaw = 1;
            }

            foliage.particleScale /= 0.2;

            int color = Minecraft.getInstance().getBlockColors().getColor(state, world, pos.below(), 0);
            foliage.particleRed   = (float)(color >> 16 & 255) / 255.0F;
            foliage.particleGreen = (float)(color >> 8  & 255) / 255.0F;
            foliage.particleBlue  = (float)(color       & 255) / 255.0F;

            foliage.particleRed   = 1F;
            foliage.particleGreen = 1F;
            foliage.particleBlue  = 1F;

            foliage.brightnessCache = CoroUtilBlockLightCache.brightnessPlayer;

            listClutter.add(foliage);
            ExtendedRenderer.foliageRenderer.getFoliageForSprite(sprite).add(foliage);
        }
    }
}