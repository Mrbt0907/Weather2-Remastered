package net.mrbt0907.weather2.mixins.accessor;

import net.minecraft.util.math.vector.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;


@Mixin(Matrix4f.class)
public interface Matrix4fAccessor {


    @Accessor("m00")
    float getM00();

    @Accessor("m00")
    void setM00(float value);

    @Accessor("m01")
    float getM01();

    @Accessor("m01")
    void setM01(float value);

    @Accessor("m02")
    float getM02();

    @Accessor("m02")
    void setM02(float value);

    @Accessor("m03")
    float getM03();

    @Accessor("m03")
    void setM03(float value);


    @Accessor("m10")
    float getM10();

    @Accessor("m10")
    void setM10(float value);

    @Accessor("m11")
    float getM11();

    @Accessor("m11")
    void setM11(float value);

    @Accessor("m12")
    float getM12();

    @Accessor("m12")
    void setM12(float value);

    @Accessor("m13")
    float getM13();

    @Accessor("m13")
    void setM13(float value);


    @Accessor("m20")
    float getM20();

    @Accessor("m20")
    void setM20(float value);

    @Accessor("m21")
    float getM21();

    @Accessor("m21")
    void setM21(float value);

    @Accessor("m22")
    float getM22();

    @Accessor("m22")
    void setM22(float value);

    @Accessor("m23")
    float getM23();

    @Accessor("m23")
    void setM23(float value);


    @Accessor("m30")
    float getM30();

    @Accessor("m30")
    void setM30(float value);

    @Accessor("m31")
    float getM31();

    @Accessor("m31")
    void setM31(float value);

    @Accessor("m32")
    float getM32();

    @Accessor("m32")
    void setM32(float value);

    @Accessor("m33")
    float getM33();

    @Accessor("m33")
    void setM33(float value);
}