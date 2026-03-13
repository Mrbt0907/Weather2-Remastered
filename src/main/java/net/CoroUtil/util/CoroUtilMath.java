package net.CoroUtil.util;

import net.minecraft.util.math.vector.Quaternion;

import java.text.DecimalFormat;


public class CoroUtilMath {

    final static double EPS = 0.000001;

    public static Quaternion rotation(Quaternion q, float angleX, float angleY, float angleZ) {
        double thetaX = (double)angleX * 0.5D;
        double thetaY = (double)angleY * 0.5D;
        double thetaZ = (double)angleZ * 0.5D;
        double thetaMagSq = thetaX * thetaX + thetaY * thetaY + thetaZ * thetaZ;
        double s;
        float w;
        if(thetaMagSq * thetaMagSq / 24.0D < 9.99999993922529E-9D) {
            w = (float)(1.0D - thetaMagSq / 2.0D);
            s = 1.0D - thetaMagSq / 6.0D;
        } else {
            double thetaMag = Math.sqrt(thetaMagSq);
            double sin = Math.sin(thetaMag);
            s = sin / thetaMag;
            w = (float)cosFromSin(sin, thetaMag);
        }

        float x = (float)(thetaX * s);
        float y = (float)(thetaY * s);
        float z = (float)(thetaZ * s);

        q.set(x, y, z, w);
        return q;
    }

    public static double cosFromSin(double sin, double angle) {
        return Math.sin(angle + 1.5707963267948966D);

    }


    public static Quaternion interpolate(Quaternion q1, Quaternion q2, float alpha) {







        double dot,s1,s2,om,sinom;

        dot = q2.r()*q1.r() + q2.j()*q1.j() + q2.k()*q1.k() + q2.i()*q1.i();

        if ( dot < 0 ) {

            q1.set(-q1.r(), -q1.j(), -q1.k(), -q1.i());
            dot = -dot;
        }

        if ( (1.0 - dot) > EPS ) {
            om = Math.acos(dot);
            sinom = Math.sin(om);
            s1 = Math.sin((1.0-alpha)*om)/sinom;
            s2 = Math.sin( alpha*om)/sinom;
        } else{
            s1 = 1.0 - alpha;
            s2 = alpha;
        }


        return new Quaternion((float)(s1*q1.r() + s2*q2.r()), (float)(s1*q1.j() + s2*q2.j()), (float)(s1*q1.k() + s2*q2.k()), (float)(s1*q1.i() + s2*q2.i()));
    }

    public static String roundVal(float val) {
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(val);
    }

    public static String roundVal(double val) {
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(val);
    }

}