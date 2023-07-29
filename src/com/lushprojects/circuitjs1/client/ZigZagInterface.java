package com.lushprojects.circuitjs1.client;

import com.google.gwt.core.client.GWT;
import com.lushprojects.circuitjs1.client.util.Locale;

import java.lang.Math;
import java.text.MessageFormat;
import java.util.*;

import com.google.gwt.user.client.Window;

public class ZigZagInterface extends TwoDimComponent {

    public ZigZagInterface(int xx, int yy) {
        super(xx, yy);
    }

    public ZigZagInterface(int xa, int ya, int xb, int yb, int f, StringTokenizer st) {
        super(xa, ya, xb, yb, f);
    }

    double yTilde(int ci, double xT) {
        if(ci % 2 == 0)
            yT = (ci + 1) * height - (height / length) * xT;
        else
            yT = ci * height + (height / length) * xT;
        return yT;
    }

    void makeZigZag(int c, Material mA, Material mB) {
        for (TwoDimCV cv : cvs) {
            cv.setxy(0.0, 0.0);
        }
        double dy = cvs.get(0).dy;
        int middleInd = (int) (n / 2);

        if (c == 0) {
            for (int hInd = 0; hInd < m; hInd++) {
                for (int lInd = 0; lInd < n; lInd++) {
                    int k = hInd * n + lIind;
                    ControlVolume cv = cvs.get(k);
                    if (lInd < middleInd)
                        cv.material = mA;
                    else
                        cv.material = mB;
                }
            }
        }
        else {
            double hT = height / c;
            if (m % c != 0) {
                Window.alert("Discretisation number in y-direction must be divisible by zigzag number c!");
            }
            int hm = (int) (m / c);
            for (int hInd = 0; hInd < m; hInd++) {
                int ci = (int) (hInd / (hT / dy));
                for (int lInd = 0; lInd < n; lInd++) {
                    int k = hInd * n + lIind;
                    TwoDimCV cv = cvs.get(k);
                    if (ci % 2 == 0) {
                        if (cv.y <= yTilde(ci, cv.x))
                            cv.material = mA;
                        else
                            cv.material = mB;
                    }
                    else {
                        if (cv.y >= yTilde(ci, cv.x))
                            cv.material = mA;
                        else
                            cv.material = mB;
                    }
                }
            }
        }
    }
}