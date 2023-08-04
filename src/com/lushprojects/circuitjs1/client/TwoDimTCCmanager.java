package com.lushprojects.circuitjs1.client;

import com.google.gwt.core.client.GWT;
// import com.google.gwt.user.client.Window;

import java.util.*;

public class TwoDimTCCmanager {



    static void setConstProp(Vector<TwoDimCV> cvs, HeatSimProps.Property prop, double value) {
        switch (prop) {
            case DENSITY:
                for (TwoDimCV cv : cvs) {
                    cv.constRho = value;
                }
                break;
            case HEATCAPACITY:
                for (TwoDimCV cv : cvs) {
                    cv.constCp = value;
                }
                break;
            case THCONDUCTIVITY:
                for (TwoDimCV cv : cvs) {
                    cv.constK = value;
                }
                break;
        }
    }

    static void setdxdy(Vector<TwoDimCV> cvs, double dx, double dy) {
        for (TwoDimCV cv : cvs) {
            cv.dx = dx;
            cv.dy = dy;
        }
    }

    static void setxy(Vector<TwoDimCV> cvs, double xOffset, double yOffset) {
        for (TwoDimCV cv : cvs) {
            cv.setxy(xOffset, yOffset);
        }
    }

    static double calcLength(Vector<TwoDimCV> cvs) {
        double length = 0.0;
        for (TwoDimCV cv : cvs) {
            length += cv.dx;
        }
        return length;
    }

    static double calcHeight(Vector<TwoDimCV> cvs) {
        double height = 0.0;
        for (TwoDimCV cv : cvs) {
            height += cv.dy;
        }
        return height;
    }

    static void setTemperatures(Vector<TwoDimCV> cvs, double temp, boolean old) {
        for (TwoDimCV cv : cvs) {
            cv.temperature = temp;
            if (old)
                cv.temperatureOld = temp;
        }
    }

    static void setTemperatureList(Vector<TwoDimCV> cvs, double[] temps, boolean old) {
        //if (temps.length != obj.NumCvs) log error
        for (int i = 0; i < cvs.size(); i++) {
            TwoDimCV cv = cvs.get(i);
            cv.temperature = temps[i];
            if (old)
                cv.temperatureOld = temps[i];
        }
    }

    static void setQgen(Vector<TwoDimCV> cvs, double qGen) {
        for (TwoDimCV cv : cvs) {
            cv.qGen = qGen;
        }
    }

    static void setContactResistance(Vector<TwoDimCV> cvs, int n, int m, double[] resistances, double[] r) {
        resistances[0] = r[0];
        resistances[1] = r[1];
        resistances[2] = r[2];
        resistances[3] = r[3];
        for (int j = 0; j < m; j++) {
            cvs.get(j * n).resistances[0] = r[0];
            cvs.get((j + 1) * n - 1).resistances[1] = r[1];
        }
        for (int i = 0; i < n; i++) {
            cvs.get(i).resistances[2] = r[2];
        }
        for (int i = (m - 1) * n; i < cvs.size(); i++) {
            cvs.get(i).resistances[3] = r[3];
        }
    }

    static void calcConductivities(Vector<TwoDimCV> cvs) {
        for (TwoDimCV cv : cvs) {
            cv.calculateConductivities();
        }
    }

    static void replaceOldNew(Vector<TwoDimCV> cvs) {
        for (TwoDimCV cv : cvs) {
            cv.temperatureOld = cv.temperature;
        }
    }

    static void updateModes(Vector<TwoDimCV> cvs) {
        for (TwoDimCV cv : cvs) {
            if(cv.temperature >= cv.temperatureOld)
                cv.mode = 1;
            else if(cv.temperature < cv.temperatureOld)
                cv.mode = -1;
        }
    }

    static void magnetize(Vector<TwoDimCV> cvs) {
        // Check if given component's' material's magnetocaloric flag is TRUE;
        // if not, abort and inform the user.
        for (TwoDimCV cv : cvs) {
            cv.magnetize();
        }
        // obj.field = !obj.field; DO THIS OUTSIDE THE METHOD
    }

    static double[] listTemps(Vector<TwoDimCV> cvs) {
        double[] temps = new double[cvs.size()];
        for (int i = 0; i < temps.length; i++) {
            temps[i] = Math.round(cvs.get(i).temperature * 100) / 100.0;
        }
        return temps;
    }

    static void printTemps(Vector<TwoDimCV> cvs) {
        double[] temps = new double[cvs.size()];
        for (int i = 0; i < temps.length; i++) {
            temps[i] = Math.round(cvs.get(i).temperature * 100) / 100.0;
            GWT.log("Temp" + String.valueOf(i) + ": " + String.valueOf(temps[i]));
            //GWT.log("(dx, dy)" + String.valueOf(i) + ": " + String.valueOf(cvs.get(i).dx) + ", " + String.valueOf(cvs.get(i).dy));
        }
    }

    // TODO
//    public static <T> void printAttributes(T obj) {
//        name, index, material, numcvs
//        String fLength = String.format("%.6f", obj.length);
//        String fheight = String.format("%.6f", obj.height);
//        String fLength = String.format("%.6f", obj.length);
//        String fLength = String.format("%.6f", obj.length);
//        String fLength = String.format("%.6f", obj.length);
//        String fLength = String.format("%.6f", obj.length);
//        String fLength = String.format("%.6f", obj.length);
//        String fLength = String.format("%.6f", obj.length);
//        String fLength = String.format("%.6f", obj.length);
//        String fLength = String.format("%.6f", obj.length);
//        String fLength = String.format("%.6f", obj.length);
//        String fLength = String.format("%.6f", obj.length);
//        String fLength = String.format("%.6f", obj.length);
//        ...
//        sdxs and dys, material of cvs ...
//    }


}