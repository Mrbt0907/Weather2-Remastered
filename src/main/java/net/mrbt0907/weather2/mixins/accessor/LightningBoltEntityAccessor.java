package net.mrbt0907.weather2.mixins.accessor;

import net.minecraft.entity.effect.LightningBoltEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LightningBoltEntity.class)
public interface LightningBoltEntityAccessor {
    @Accessor("life")
    int getLife();

    @Accessor("life")
    void setLife(int life);

    @Accessor("flashes")
    int getFlashes();

    @Accessor("flashes")
    void setFlashes(int flashes);
}