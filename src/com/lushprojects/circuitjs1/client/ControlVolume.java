package com.lushprojects.circuitjs1.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;

import java.util.*;

public class ControlVolume {
    public Component parent;
    public int element_index;
    public int TCE_index;
    public int global_index;
    public double temperature;
    public double temperature_old;
    public ControlVolume left_neighbour;
    public ControlVolume right_neighbour;
    public double dx;
    public double left_resistance;
    public double right_resistance;
    public double k_hl;
    public double k_hr;
    public double k_left;
    public double k_right;
    public double q_in;
    public double q_out;
    public double q_gen;
    public double const_rho;
    public double const_cp;
    public double const_k;
    public double eps;
    public int mode;

    //public ControlVolume(Component parent, int index) {
    public ControlVolume(int index) {
        this.parent = parent;
        this.element_index = index;
        this.TCE_index = 0;
        this.global_index = 0;
        this.temperature = 0.0;
        this.temperature_old = 0.0;
        this.left_neighbour = null;
        this.right_neighbour = null;
        this.dx = 0.001;
        this.left_resistance = 0.0;
        this.right_resistance = 0.0;
        this.k_hl = 0.0;
        this.k_hr = 0.0;
        this.k_left = 0.0;
        this.k_right = 0.0;
        this.q_in = 0.0;
        this.q_out = 0.0;
        this.q_gen = 0.0;
        this.const_rho = -1;
        this.const_cp = -1;
        this.const_k = -1;
        this.eps = 1.0;
        this.mode = 0;
    }

    double rho() {
        double rho = 0.0;
        if (this.const_rho != -1)
            rho = this.const_rho;
        else if (this.const_rho == -1) {
            rho = ModelMethods.linInterp(this.temperature, this.parent.material.interpTemps, this.parent.material.rho);
        }
        return rho;
    }

    double cp(){
        double cp = 0.0;
        Material m = this.parent.material;
        int fI = 0;
        if (this.const_cp != -1) {
            cp = this.const_cp;
        }
        else {
            if (this.parent.field == true) {
                fI = this.parent.fieldIndex;
            }
            if (m.cpThysteresis == false) {
                cp = ModelMethods.linInterp(this.temperature, m.interpTemps, m.cp.get(fI));
            }
            else if (m.cpThysteresis == true && this.mode == 1){
                cp = ModelMethods.linInterp(this.temperature, m.interpTemps, m.cpHeating.get(fI));
            }
            else if (m.cpThysteresis == true && this.mode == -1){
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
            k = ModelMethods.linInterp(this.temperature, this.parent.material.interpTemps, this.parent.material.k);
        }
        return k;
    }

    void calc_k_hl() {
        this.k_hl = this.left_neighbour.k() * this.k() * (this.left_neighbour.dx + this.dx) /
                (this.left_neighbour.k() * this.dx + this.k() * this.left_neighbour.dx +
                        this.left_neighbour.k() * this.k() * this.left_resistance * (this.dx + this.left_neighbour.dx) / 2);
    }

    void calc_k_hr() {
        this.k_hr = this.k() * this.right_neighbour.k() * (this.dx + this.right_neighbour.dx) /
                (this.k() * this.right_neighbour.dx + this.right_neighbour.k() * this.dx +
                        this.right_neighbour.k() * this.k() * this.right_resistance * (this.right_neighbour.dx + this.dx) / 2);
    }

    void calc_k_left() {
        this.k_left = 2 * this.k_hl * this.dx / (this.left_neighbour.dx + this.dx);
    }

    void calc_k_right() {
        this.k_right = 2 * this.k_hr * this.dx / (this.right_neighbour.dx + this.dx);
    }

    void calc_conduct_lr() {
        this.calc_k_hl();
        this.calc_k_hr();
        this.calc_k_left();
        this.calc_k_right();
    }

    double get_q_gen() {
        return this.q_gen;
    }


    public void magnetize() {
        // TO DO: inform user

        Vector<Double> dTheatcool = new Vector<Double>();
        double dT = 0.0;
        double T = 0.0;
        Component p = (Component)this.parent;
        if (!p.field) {
            dTheatcool = p.material.dTheating.get(p.fieldIndex - 1);
            dT = ModelMethods.linInterp(this.temperature, p.material.interpTemps, dTheatcool);
            T = this.temperature + dT;
            this.temperature = T; this.temperature_old = T;
            GWT.log("Field = " + String.valueOf(p.field));
            GWT.log("dT = " + String.valueOf(dT));
        }
        if (p.field) {
            dTheatcool = p.material.dTcooling.get(p.fieldIndex - 1);
            dT = ModelMethods.linInterp(this.temperature, p.material.interpTemps, dTheatcool);
            T = this.temperature - dT;
            GWT.log("Field = " + String.valueOf(p.field));
            GWT.log("dT = -" + String.valueOf(dT));
            this.temperature = T; this.temperature_old = T;
        }
    }

}
