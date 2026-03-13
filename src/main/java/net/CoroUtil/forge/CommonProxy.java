package net.CoroUtil.forge;

import net.CoroUtil.block.BlockBlank;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class CommonProxy {
    public CoroUtil mod;

    public static final String block_repairing_name = "repairing_block";


    public static final String block_blank_name = "blank";


    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, CoroUtil.modID);

    public static final RegistryObject<Block> blockRepairingBlock =
            BLOCKS.register(block_repairing_name, () -> new Block(
                    Block.Properties.of(net.minecraft.block.material.Material.STONE)));



    public static final RegistryObject<BlockBlank> blockBlank =
            BLOCKS.register(block_blank_name, BlockBlank::new);

    public CommonProxy() {
    }


    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}