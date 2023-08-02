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

        if (!material2.isLoaded())
            material2.readFiles();
    }

    public ZigZagInterface(int xa, int ya, int xb, int yb, int f, StringTokenizer st) {
        super(xa, ya, xb, yb, f, st);
        material2 = sim.materialHashMap.get("000000-Custom");

        if (!material2.isLoaded())
            material2.readFiles();
    }

    double yTilde(int ci, double xT, double hT) {
        double yT;
        if (ci % 2 == 0)
            yT = ((ci + 1) * hT) - ((hT / length) * xT);
        else
            yT = (ci * hT) + ((hT / length) * xT);
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
                    if (lInd < middleInd)
                        cv.material = material;
                    else
                        cv.material = material2;
                }
            }
        } else {
/*            if (m % c != 0) {
                Window.alert("Discretisation number in y-direction must be divisible by zigzag number c!");
                return;
            }*/
/*            for (int i = 0; i < m; i++) {
                int xAxis = n / 2
                for (int j = 0; j < n; j++) {
                    int yAxis = j / 2
                    TwoDimCV cv = cvs.get(i * n + j);
                    GWT.log(cv.xIndex + " " + cv.yIndex);
                }
            }*/
            double hT = height / c;
            for (int j = 0; j < m; j++) {
                int ci = (int) Math.floor(j / (hT / dy));
                for (int i = 0; i < n; i++) {
                    int k = (j * n) + i;
                    TwoDimCV cv = cvs.get(k);
                    if (ci % 2 == 0) {
                        if (cv.y <= yTilde(ci, cv.x, hT))
                            cv.material = material;
                        else
                            cv.material = material2;
                    } else {
                        if (cv.y >= yTilde(ci, cv.x, hT))
                            cv.material = material;
                        else
                            cv.material = material2;
                    }
                }
            }
            int count= 0;
            for (TwoDimCV cv: cvs) {
                if(cv.material==material)
                    count++;
            }

            GWT.log(String.valueOf((double)count/numCvs));
        }
    }

    @Override
    void calculateLengthHeight() {
        super.calculateLengthHeight();
        try {
            makeZigZag(4);
        } catch (Exception ignore) {
        }
    }

    // @Override
    // void draw(Graphics g) {
    //     boundingBox.setBounds(x, y, Math.abs(x - x2), Math.abs(y - point4.y));
    //     double tmpDx = length / n;
    //     double tmpDy = height / m;
    //     drawRect(g, point1, point4, color.getHexValue());
    //     drawCVs(g, point1, point4, color.getHexValue(), color2.getHexValue());
    //     if (tmpDx < 1e-6 && tmpDx != 0) {
    //         //Window.alert("TwoDimComponent can't have a dx < 1µ, current is " + tmpDx);
    //         isDisabled = true;
    //     } else {
    //         isDisabled = false;
    //     }
    //     TwoDimTCCmanager.setdxdy(cvs, tmpDx, tmpDy);

    //     doDots(g);
    //     drawPosts(g);

    // }

    // void drawCVs(Graphics g, Point pa, Point pb, String color1, String color2) {
    //     Context2d ctx = g.context;
    //     double x = Math.min(pa.x, pb.x);
    //     double y = Math.min(pa.y, pb.y);
    //     double width = Math.abs(pa.x - pb.x);
    //     double cvWidth = width / n;
    //     double height = Math.abs(pa.y - pb.y);
    //     double cvHeight = height / m;
    //     ctx.setStrokeStyle(Color.white.getHexValue());
    //     ctx.strokeRect(x, y, width, height);
    //     ctx.setStrokeStyle(Color.deepBlue.getHexValue());

    //     for (TwoDimCV cv : cvs) {
    //         double cvX = x + cv.xIndex * cvWidth;
    //         double cvY = y + cv.yIndex * cvHeight;
    //         String cvColor = cv.material.equals(material) ? color1 : color2;
    //         ctx.setFillStyle(cvColor);
    //         ctx.strokeRect(cvX, cvY, cvWidth, cvHeight);
    //         ctx.fillRect(cvX, cvY, cvWidth, cvHeight);
    //     }
    // }
}