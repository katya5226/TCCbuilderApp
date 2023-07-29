package com.lushprojects.circuitjs1.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;

import java.util.*;

public class TwoDimCV {
    public TwoDimComponent parent;
    public Material material;
    public int index, xIndex, yIndex;
    public int TCEindex;
    public int globalIndex;
    public double dx, dy;
    public double temperature, temperatureOld;
    public ControlVolume [] neighbours; 
    public double [] resistances;
    public double [] kh;
    public double [] kd;
    public double qIn, qOut, qGen;
    public double constRho, constCp, constK;
    public double emiss;
    public int mode;

    public TwoDimCV(int index, TwoDimComponent parent) {
        this.parent = parent;
        this.material = parent.material;
        this.index = index;
        this.xIndex = index % parent.n;
        this.yIndex = index / parent.m;
        this.dx = this.dy = 0.001;
        this.temperature = this.temperatureOld = 0.0;
        this.neighbours = new ControlVolume [4]; // 0 - west, 1 - east, 2 - south, 3 - north
        this.resistances = new double []{0.0, 0.0, 0.0, 0.0};
        this.kh = new double []{0.0, 0.0, 0.0, 0.0};
        this.kd = new double []{0.0, 0.0, 0.0, 0.0};
        this.qIn = this.qOut = this.qGen = 0.0;
        this.constRho = this.constCp = this.constK = -1;
        this.emiss = 1.0;
        this.mode = 0.0;
    }

    void setdxdy(double dx, double dy) {
        this.dx = dx, this.dy = dy;
    }

    void setxy(double xOffset, double yOffset) {
        if (this.xIndex == 0) {
            this.x = xOffset + 0.5 * this.dx;
        }
        else {
            TwoDimCV wn = this.westNeighbour;
            this.x = wn.x + 0.5 * (wn.dx + this.dx); 
        }
        if (this.yIndex == 0) {
            this.y = yOffset + 0.5 * this.dy;
        }
        else {
            TwoDimCV ws = this.southNeighbour;
            this.y = sn.y + 0.5 * (sn.dy + this.dy);
        }
    }

    double rho() {
        double rho = 0.0;
        if (this.const_rho != -1)
            rho = this.const_rho;
        else if (this.const_rho == -1) {
            rho = ModelMethods.linInterp(this.temperature, this.material.interpTemps, this.material.rho);
        }
        return rho;
    }

    double cp() {
        double cp = 0.0;
        Material m = this.material;
        int fI = 0;
        if (this.const_cp != -1) {
            cp = this.const_cp;
        } else {
            if (this.parent.field == true) {
                fI = this.parent.fieldIndex;
            }
            if (m.cpThysteresis == false) {
                cp = ModelMethods.linInterp(this.temperature, m.interpTemps, m.cp.get(fI));
            } else if (m.cpThysteresis == true && this.mode == 1) {
                cp = ModelMethods.linInterp(this.temperature, m.interpTemps, m.cpHeating.get(fI));
            } else if (m.cpThysteresis == true && this.mode == -1) {
                cp = ModelMethods.linInterp(this.temperature, m.interpTemps, m.cpCooling.get(fI));
            }
        }
        return cp;
    }

    double k() {
        double k = 0.01;
        if (this.const_k != -1)
            k = this.const_k;
        else if (this.const_k == -1) {
            k = ModelMethods.linInterp(this.temperature, this.material.interpTemps, this.material.k);
        }
        return k;
    }

    void calcKh() {
        for (int i = 0; i < 4 ; i++) {
            ControlVolume neigh = this.neighbours[i];
            double r = this.resistances[i];
            this.kh[i] = neigh.k() * this.k() * (neigh.dx + this.dx) / 
            (neigh.k() * this.dx + this.k() * neigh.dx + neigh.k() * this.k() * r * (this.dx + neigh.dx) / 2);
        }
    }

    void calcKd() {
        for (int i = 0; i < 4 ; i++) {
            ControlVolume neigh = this.neighbours[i];
            this.kd[i] = 2 * this.kh[i] * this.dx / (neigh.dx + this.dx);
        }   
    }

    void calculateConductivities() {
        this.calcKh();
        this.calcKd();
    }

    double get_q_gen() {
        return this.q_gen;
    }

    public void magnetize() {
        // TODO: inform user
        Vector<Double> dTheatcool = new Vector<Double>();
        double dT = 0.0;
        double T = 0.0;
        Component p = (Component) this.parent;
        if (!p.field) {
            dTheatcool = material.dTheating.get(p.fieldIndex - 1);
            dT = ModelMethods.linInterp(this.temperature, material.interpTemps, dTheatcool);
            T = this.temperature + dT;
            this.temperature = T;
            this.temperature_old = T;
        }
        if (p.field) {
            dTheatcool = material.dTcooling.get(p.fieldIndex - 1);
            dT = ModelMethods.linInterp(this.temperature, material.interpTemps, dTheatcool);
            T = this.temperature - dT;
            this.temperature = T;
            this.temperature_old = T;
        }
    }

}
