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
    int zigzagFactor;

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

    @Override
    void initializeComponent() {
        super.initializeComponent();
        name = "ZigZagComponent";
        zigzagFactor = 4;
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
                GWT.log("Discretisation number in y-direction must be divisible by zigzag number c!");
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

    @Override
    void buildComponent() {
        super.buildComponent();
        makeZigZag(zigzagFactor);
    }

    @Override
    void getInfo(String[] arr) {
        arr[0] = name;
        // getBasicInfo(arr);
        arr[1] = "TwoDimComponent index = " + String.valueOf(index);
        arr[2] = "Material 1 = " + material.materialName;
        arr[3] = "Material 2 = " + material2.materialName;
        arr[4] = "Length = " + sim.formatLength(length);
        arr[5] = "Height = " + sim.formatLength(height);
        arr[6] = "#CVs (x) = " + n;
        arr[7] = "#CVs (y) = " + m;
        arr[8] = "CV dx = " + sim.formatLength(cvs.get(0).dx);
        arr[9] = "CV dy = " + sim.formatLength(cvs.get(0).dy);
        arr[10] = "Zigzag factor =  = " + zigzagFactor;
    }

    @Override
    public EditInfo getEditInfo(int n) {
        switch (n) {
            case 0:
                return new EditInfo("Name", String.valueOf(name));
            case 1:
                return new EditInfo("Index", index);
            case 2:
                return new EditInfo("Number of control volumes in x-direction", this.n);
            case 3:
                return new EditInfo("Number of control volumes in y-direction", this.m);
            case 4:
                EditInfo ei = new EditInfo("Material 1", 0);
                ei.choice = new Choice();
                for (String m : sim.materialNames) {
                    ei.choice.add(m);
                }
                ei.choice.select(sim.materialNames.indexOf(material.materialName));
                return ei;
            case 5:
                EditInfo ei1 = new EditInfo("Material 2", 0);
                ei1.choice = new Choice();
                for (String m : sim.materialNames) {
                    ei1.choice.add(m);
                }
                ei1.choice.select(sim.materialNames.indexOf(material2.materialName));
                return ei1;
            case 6:
                EditInfo ei2 = new EditInfo("Color 1", 0);
                ei2.choice = new Choice();
                for (int ch = 0; ch < sim.colorChoices.size(); ch++) {
                    ei2.choice.add(sim.colorChoices.get(ch));
                }
                ei2.choice.select(Color.colorToIndex(color));

                return ei2;
            case 7:
                EditInfo ei3 = new EditInfo("Color 2", 0);
                ei3.choice = new Choice();
                for (int ch = 0; ch < sim.colorChoices.size(); ch++) {
                    ei3.choice.add(sim.colorChoices.get(ch));
                }
                ei3.choice.select(Color.colorToIndex(color));

                return ei3;
            case 8:
                return new EditInfo("Length (" + sim.selectedLengthUnit.unitName + ")", length * CircuitElm.sim.selectedLengthUnit.conversionFactor);
            case 9:
                return new EditInfo("Height (" + sim.selectedLengthUnit.unitName + ")", height * CircuitElm.sim.selectedLengthUnit.conversionFactor);
            case 10:
                return new EditInfo("Left contact resistance (mK/W)", resistances[0]);
            case 11:
                return new EditInfo("Right contact resistance (mK/W)", resistances[1]);
            case 12:
                return new EditInfo("Zigzag factor", zigzagFactor);
            default:
                return null;
        }
    }

    @Override
    public void setEditValue(int n, EditInfo ei) {
        switch (n) {
            case 0:
                name = ei.textf.getText();
                break;
            case 1:
                index = (int) ei.value;
                break;
            case 2:
                this.n = (int) ei.value;
                break;
            case 3:
                this.m = (int) ei.value;
                break;
            case 4:
                material = sim.materialHashMap.get(sim.materialNames.get(ei.choice.getSelectedIndex()));
                if (!material.isLoaded())
                    material.readFiles();
                break;
            case 5:
                material2 = sim.materialHashMap.get(sim.materialNames.get(ei.choice.getSelectedIndex()));
                if (!material2.isLoaded())
                    material2.readFiles();
                break;
            case 6:
                color = Color.translateColorIndex(ei.choice.getSelectedIndex());
                break;
            case 7:
                color = Color.translateColorIndex(ei.choice.getSelectedIndex());
                break;
            case 8:
                double prevLength = length;
                length = (ei.value / sim.selectedLengthUnit.conversionFactor);

                double ratio = length / prevLength;
                int deltaX = (int) (Math.abs(point2.x - point1.x) * ratio);
                drag(sim.snapGrid(point1.x + deltaX), point4.y);
                break;
            case 9:
                double prevHeight = height;
                height = (ei.value / sim.selectedLengthUnit.conversionFactor);

                double ratio2 = height / prevHeight;
                int deltaY = (int) (Math.abs(point1.y - point4.y) * ratio2);

                drag(point4.x, sim.snapGrid(point1.y + deltaY));

                break;
            case 10:
                resistances[0] = ei.value;
                break;
            case 11:
                resistances[1] = ei.value;
                break;
            case 12:
                zigzagFactor = (int) ei.value;
                break;

        }
        buildComponent();
    }


}