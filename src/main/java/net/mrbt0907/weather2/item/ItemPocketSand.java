package net.mrbt0907.weather2.item;

import CoroUtil.packet.PacketHelper;
import extendedrenderer.particle.ParticleRegistry;
import extendedrenderer.particle.behavior.ParticleBehaviorSandstorm;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.client.SceneEnhancer;
import net.mrbt0907.weather2.client.entity.particle.ParticleSandstorm;
import net.mrbt0907.weather2.client.event.ClientTickHandler;
import net.mrbt0907.weather2.registry.BlockRegistry;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.util.WeatherUtilBlock;

import java.util.Random;

public class ItemPocketSand extends Item
{
	@SideOnly(Side.CLIENT)
    public static ParticleBehaviorSandstorm particleBehavior;

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer player, EnumHand hand) {

        ItemStack itemStackIn = player.getHeldItem(hand);

        if (!player.world.isRemote) {

            if (!(player).capabilities.isCreativeMode)
            {
                if (itemStackIn.getCount() > 0) {
                    itemStackIn.shrink(1);
                }
            }
            int y = (int) player.getEntityBoundingBox().minY;
            double randSize = 20;
            double randAngle = player.world.rand.nextDouble() * randSize - player.world.rand.nextDouble() * randSize;
            WeatherUtilBlock.fillAgainstWallSmoothly(player.world, new Vec3(player.posX, y + 0.5D, player.posZ), player.rotationYawHead + (float)randAngle, 15, 2, BlockRegistry.sand_layer, 2);

            particulateToClients(worldIn, player);
        } else {
            particulate(player.world, player);
        }

        return super.onItemRightClick(worldIn, player, hand);
    }

    /**
     *
     * @param world
     * @param player The sand item using source
     */
    @SideOnly(Side.CLIENT)
    public static void particulate(World world, EntityLivingBase player) {

        if (particleBehavior == null) {
            particleBehavior = new ParticleBehaviorSandstorm(new Vec3(player.getPosition()).toVec3Coro());
        }

        Random rand = world.rand;

        TextureAtlasSprite sprite = ParticleRegistry.cloud256;

        double distCast = 10;
        double xzAdj = Math.cos(Math.toRadians(player.rotationPitch));
        double vecYCast = (-Math.sin(Math.toRadians(player.rotationPitch)) * (distCast));
        double vecXCast = (-Math.sin(Math.toRadians(player.rotationYawHead)) * (distCast)) * xzAdj;
        double vecZCast = (Math.cos(Math.toRadians(player.rotationYawHead)) * (distCast)) * xzAdj;

        BlockPos pos = new BlockPos(player.posX + vecXCast, player.posY + vecYCast, player.posZ + vecZCast);
        //pos = new BlockPos(player.getLookVec().add(new Vec3d(player.posX, player.posY, player.posZ)));

        double dist = Math.sqrt(Minecraft.getMinecraft().player.getDistanceSq(pos));

        //System.out.println(dist);

        if (Minecraft.getMinecraft().player != player && dist < 7) {
            SceneEnhancer.adjustAmountTargetPocketSandOverride = 1.3F;
        }

        for (int i = 0; i < 15; i++) {
            ParticleSandstorm part = new ParticleSandstorm(world, player.posX, player.posY + 1.5D, player.posZ
                    , 0, 0, 0, sprite);
            particleBehavior.initParticle(part);

            double speed = 0.6F;
            double randSize = 20;
            double randAngle = player.world.rand.nextDouble() * randSize - player.world.rand.nextDouble() * randSize;
            double vecX = (-Math.sin(Math.toRadians(player.rotationYawHead + randAngle)) * (speed));
            randAngle = player.world.rand.nextDouble() * randSize - player.world.rand.nextDouble() * randSize;
            double vecZ = (Math.cos(Math.toRadians(player.rotationYawHead + randAngle)) * (speed));
            randAngle = player.world.rand.nextDouble() * randSize - player.world.rand.nextDouble() * randSize;

            //double xzAdj = Math.cos(Math.toRadians(player.rotationPitch));

            double vecY = (-Math.sin(Math.toRadians(player.rotationPitch + randAngle)) * (speed));

            //System.out.println("?:" + xzAdj);

            part.setMotionX(vecX * xzAdj);
            part.setMotionZ(vecZ * xzAdj);
            part.setMotionY(vecY);

            part.setFacePlayer(false);
            part.isTransparent = true;
            part.rotationYaw = (float) rand.nextInt(360);
            part.rotationPitch = (float) rand.nextInt(360);
            part.setMaxAge(80);
            part.setGravity(0.09F);
            part.setAlphaF(1F);
            float brightnessMulti = 1F - (rand.nextFloat() * 0.5F);
            part.setRBGColorF(0.65F * brightnessMulti, 0.6F * brightnessMulti, 0.3F * brightnessMulti);
            part.setScale(20);

            part.aboveGroundHeight = 0.5D;
            part.collisionSpeedDampen = false;
            part.bounceSpeed = 0.03D;
            part.bounceSpeedAhead = 0.0D;

            part.setKillOnCollide(false);

            part.windWeight = 1F;

            particleBehavior.particles.add(part);
            ClientTickHandler.weatherManager.addEffectedParticle(part);
            part.spawnAsWeatherEffect();
        }

        //System.out.println("spawn!");
    }

    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {

        if (worldIn.isRemote) {
            tickClient(stack, worldIn, entityIn, itemSlot, isSelected);
        }

        super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);
    }

    @SideOnly(Side.CLIENT)
    public void tickClient(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (particleBehavior == null) {
            particleBehavior = new ParticleBehaviorSandstorm(new Vec3(entityIn.getPosition()).toVec3Coro());
        }
        particleBehavior.tickUpdateList();
    }

    public static void particulateToClients(World world, EntityLivingBase player) {
        NBTTagCompound data = new NBTTagCompound();
        data.setString("packetCommand", "PocketSandData");
        data.setString("command", "create");
        data.setString("playerName", player.getName());
        Weather2.event_channel.sendToAllAround(PacketHelper.getNBTPacket(data, Weather2.MODID),
                new NetworkRegistry.TargetPoint(world.provider.getDimension(), player.posX, player.posY, player.posZ, 50));
    }

    @SideOnly(Side.CLIENT)
    public static void particulateFromServer(String username) {
        World world = Minecraft.getMinecraft().world;
        EntityPlayer player = world.getPlayerEntityByName(username);
        if (player != null) {
            particulate(world, player);
        }
    }
}
