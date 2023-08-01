package com.lushprojects.circuitjs1.client;

// import com.google.gwt.core.client.GWT;
// import com.google.gwt.user.client.Window;

// import java.util.*;

public class TwoDimTCCmanager {

    public static <T> void setConstParam(T obj, Property param, double value) {
        switch (param) {
            case DENSITY:
                for (TwoDimCV cv : obj.cvs) {
                    cv.constRho = value;
                }
                break;
            case HEATCAPACITY:
                for (TwoDimCV cv : obj.cvs) {
                    cv.constCp = value;
                }
                break;
            case THCONDUCTIVITY:
                for (TwoDimCV cv : obj.cvs) {
                    cv.constK = value;
                }
                break;
        }
    }

    public static <T> void setdxdy(T obj, double dx, double dy) {
        for (TwoDimCV cv : obj.cvs) {
            cv.dx = dx;
            cv.dy = dy;
        }
    }

    public static <T> void setxy(T obj, double xOffset, double yOffset) {
        for (TwoDimCV cv : obj.cvs) {
            cv.setxy(xOffset, yOffset);
        }
    }

    public static <T> void calcLength(T obj) {
        obj.length = 0.0;
        for (TwoDimCV cv : obj.cvs) {
            obj.length += cv.dx;
        }
    }

    public static <T> void calcHeight(T obj) {
        obj.height = 0.0;
        for (TwoDimCV cv : obj.cvs) {
            obj.height += cv.dy;
        }
    }

    public static <T> void setTemperatures(T obj, double temp, boolean old) {
        for (TwoDimCV cv : obj.cvs) {
            cv.temperature = temp;
            if (old)
                cv.temperatureOld = temp;
        }
    }

    public static <T> void setTemperatureList(T obj, double[] temps, boolean old) {
        //if (temps.length != obj.NumCvs) log error
        for (int i = 0; i < obj.numCvs; i++) {
            TwoDimCV cv = obj.cvs.get(i);
            cv.temperature = temps[i];
            if (old)
                cv.temperatureOld = temps[i];
        }
    }

    public static <T> void setQgen(T obj, double qGen) {
        for (TwoDimCV cv : obj.cvs) {
            cv.qGen = qGen;
        }
    }

    public static <T> void setContactResistance(T obj, double[] r) {
        obj.resistances[0] = r[0];
        obj.resistances[1] = r[1];
        obj.resistances[2] = r[2];
        obj.resistances[3] = r[3];
        for (int j = 0; j < obj.m; j++) {
            obj.cvs.get(j * n).resistances[0] = r[0];
            obj.cvs.get((j + 1) * n - 1).resistances[1] = r[1];
        }
        for (int i = 0; i < obj.n; i++) {
            obj.cvs.get(i).resistances[2] = r[2];
        }
        for (int i = (obj.m - 1) * obj.n; i < obj.numCvs; i++) {
            obj.cvs.get(i).resistances[3] = r[3];
        }
    }

    public static <T> void calcConductivities(T obj) {
        for (TwoDimCV cv : obj.cvs) {
            cv.calculateConductivities();
        }
    }

    public static <T> void replaceOldNew(T obj) {
        for (TwoDimCV cv : obj.cvs) {
            cv.temperatureOld = cv.temperature;
        }
    }

    public static <T> void updateModes(T obj) {
        for (TwoDimCV cv : obj.cvs) {
            if(cv.temperature >= cv.temperatureOld)
                cv.mode = 1;
            else if(cv.temperature < cv.temperatureOld)
                cv.mode = -1;
        }
    }

    public static <T> void magnetize(T obj) {
        // Check if given component's' material's magnetocaloric flag is TRUE;
        // if not, abort and inform the user.
        for (TwoDimCV cv : obj.cvs) {
            cv.magnetize();
        }
        obj.field = !obj.field;
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