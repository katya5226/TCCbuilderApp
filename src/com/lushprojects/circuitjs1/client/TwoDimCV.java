package com.lushprojects.circuitjs1.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;

import java.util.*;

public class TwoDimCV {
    TwoDimComponent parent;
    Material material;
    int index, xIndex, yIndex;
    int TCEindex;
    int globalIndex;
    double dx, dy;
    double x, y, xGlobal, yGlobal;
    double temperature, temperatureOld;
    TwoDimCV [] neighbours; 
    double [] resistances;
    double [] kh;
    double [] kd;
    double qIn, qOut, qGen;
    double constRho, constCp, constK;
    double emiss;
    int mode;

    public TwoDimCV(int index, TwoDimComponent parent) {
        parent = parent;
        material = parent.material;
        index = index;
        xIndex = index % parent.n;
        yIndex = index / parent.m;
        dx = dy = 0.001;
        temperature = temperatureOld = 0.0;
        neighbours = new TwoDimCV [4]; // 0 - west, 1 - east, 2 - south, 3 - north
        resistances = new double []{0.0, 0.0, 0.0, 0.0};
        kh = new double []{0.0, 0.0, 0.0, 0.0};
        kd = new double []{0.0, 0.0, 0.0, 0.0};
        qIn = qOut = qGen = 0.0;
        constRho = constCp = constK = -1;
        emiss = 1.0;
        mode = 0;
    }

    void setdxdy(double dx, double dy) {
        dx = dx; dy = dy;
    }

    void setxy(double xOffset, double yOffset) {
        if (xIndex == 0) {
            x = xOffset + 0.5 * dx;
        }
        else {
            TwoDimCV wn = neighbours[0];
            x = wn.x + 0.5 * (wn.dx + dx); 
        }
        if (yIndex == 0) {
            y = yOffset + 0.5 * dy;
        }
        else {
            TwoDimCV sn = neighbours[2];
            y = sn.y + 0.5 * (sn.dy + dy);
        }
    }

    double rho() {
        double rho = 0.0;
        if (constRho != -1)
            rho = constRho;
        else if (constRho == -1) {
            rho = ModelMethods.linInterp(temperature, material.interpTemps, material.rho);
        }
        return rho;
    }

    double cp() {
        double cp = 0.0;
        Material m = material;
        int fI = 0;
        if (constCp != -1) {
            cp = constCp;
        } else {
            if (parent.field == true) {
                fI = parent.fieldIndex;
            }
            if (m.cpThysteresis == false) {
                cp = ModelMethods.linInterp(temperature, m.interpTemps, m.cp.get(fI));
            } else if (m.cpThysteresis == true && mode == 1) {
                cp = ModelMethods.linInterp(temperature, m.interpTemps, m.cpHeating.get(fI));
            } else if (m.cpThysteresis == true && mode == -1) {
                cp = ModelMethods.linInterp(temperature, m.interpTemps, m.cpCooling.get(fI));
            }
        }
        return cp;
    }

    double k() {
        double k = 0.01;
        if (constK != -1)
            k = constK;
        else if (constK == -1) {
            k = ModelMethods.linInterp(temperature, material.interpTemps, material.k);
        }
        return k;
    }

    void calcKh() {
        for (int i = 0; i < 4 ; i++) {
            TwoDimCV neigh = neighbours[i];
            double r = resistances[i];
            kh[i] = neigh.k() * k() * (neigh.dx + dx) / 
            (neigh.k() * dx + k() * neigh.dx + neigh.k() * k() * r * (dx + neigh.dx) / 2);
        }
    }

    void calcKd() {
        for (int i = 0; i < 4 ; i++) {
            TwoDimCV neigh = neighbours[i];
            kd[i] = 2 * kh[i] * dx / (neigh.dx + dx);
        }   
    }

    void calculateConductivities() {
        calcKh();
        calcKd();
    }

    double getQgen() {
        return qGen;
    }

    void magnetize() {
        // TODO: inform user
        Vector<Double> dTheatcool = new Vector<Double>();
        double dT = 0.0;
        double T = 0.0;
        TwoDimComponent p = (TwoDimComponent) parent;
        if (!p.field) {
            dTheatcool = material.dTheating.get(p.fieldIndex - 1);
            dT = ModelMethods.linInterp(temperature, material.interpTemps, dTheatcool);
            T = temperature + dT;
            temperature = T;
            temperatureOld = T;
        }
        if (p.field) {
            dTheatcool = material.dTcooling.get(p.fieldIndex - 1);
            dT = ModelMethods.linInterp(temperature, material.interpTemps, dTheatcool);
            T = temperature - dT;
            temperature = T;
            temperatureOld = T;
        }


    }

}
