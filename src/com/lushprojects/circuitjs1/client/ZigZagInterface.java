package com.lushprojects.circuitjs1.client;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.GWT;
import com.lushprojects.circuitjs1.client.util.Locale;

import java.lang.Math;
import java.text.MessageFormat;
import java.util.*;

import com.google.gwt.user.client.Window;

public class ZigZagInterface extends TwoDimComponent {

    Material material2;

    public ZigZagInterface(int xx, int yy) {
        super(xx, yy);
        //CirSim.debugger();
        material2 = sim.materialHashMap.get("000000-Custom");

        if (!material2.isLoaded()) material2.readFiles();
    }

    public ZigZagInterface(int xa, int ya, int xb, int yb, int f, StringTokenizer st) {
        super(xa, ya, xb, yb, f, st);
        material2 = sim.materialHashMap.get("000000-Custom");

        if (!material2.isLoaded()) material2.readFiles();
    }

    double yTilde(int ci, double xT, double hT) {
        double yT;
        if (ci % 2 == 0) yT = ((ci + 1) * hT) - ((hT / length) * xT);
        else yT = (ci * hT) + ((hT / length) * xT);
        return yT;
    }

    void makeZigZag(int c) {
        for (TwoDimCV cv : cvs) {
            cv.setxy(0.0, 0.0);
        }

        double dy = cvs.get(0).dy;
        int middleInd = (int) (n / 2);
        if (c == 0) {
            for (int j = 0; j < m; j++) {
                for (int lInd = 0; lInd < n; lInd++) {
                    int k = j * n + lInd;
                    TwoDimCV cv = cvs.get(k);
                    if (lInd < middleInd) cv.material = material;
                    else cv.material = material2;
                }
            }
        } else {
            if (m % c != 0) {
                Window.alert("Discretisation number in y-direction must be divisible by zigzag number c!");
                return;
            }
            double hT = height / c;
            for (int j = 0; j < m; j++) {
                int ci = (int) Math.floor(j / (hT / dy));
                for (int i = 0; i < n; i++) {
                    int k = (j * n) + i;
                    TwoDimCV cv = cvs.get(k);
                    if (ci % 2 == 0) {
                        if (cv.y <= yTilde(ci, cv.x, hT)) cv.material = material;
                        else cv.material = material2;
                    } else {
                        if (cv.y >= yTilde(ci, cv.x, hT)) cv.material = material;
                        else cv.material = material2;
                    }
                }
            }
        }
    }

    @Override
    void calculateLengthHeight() {
        super.calculateLengthHeight();
        if (cvs != null)
            makeZigZag(zigzagFactor);
    }


}