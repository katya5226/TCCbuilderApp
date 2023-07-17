package com.lushprojects.circuitjs1.client;

import com.google.gwt.canvas.dom.client.CanvasGradient;
import com.google.gwt.core.client.GWT;
import com.lushprojects.circuitjs1.client.util.Locale;
import com.google.gwt.user.client.Window;

import java.lang.Math;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

public class TCC {
    //public CirSim parent_sim;
    public String name;
    public Vector<TCE> TCEs;
    public int num_el;
    public int num_cvs;
    public Vector<ControlVolume> cvs;
    public TCC left_neighbour;
    public TCC right_neighbour;
    public double[] underdiag;
    public double[] diag;
    public double[] upperdiag;
    public double[] rhs;
    public int left_boundary;
    public int right_boundary;
    //public int[) other_ht_types;
    public double q_in, q_out;
    public double temp_left, temp_right;
    public double h_left, h_right;

    public TCC(String name, Vector<TCE> TCEs) {
        //this.parent_sim = null;
        this.name = name;
        this.TCEs = TCEs;
        this.num_el = TCEs.size();
        this.num_cvs = 0;
        for (TCE tce : TCEs) {
            this.num_cvs += tce.num_cvs;
        }
        this.cvs = new Vector<ControlVolume>();
        this.left_neighbour = null;
        this.right_neighbour = null;
        this.underdiag = new double[this.num_cvs];
        this.diag = new double[this.num_cvs];
        this.upperdiag = new double[this.num_cvs];
        this.rhs = new double[this.num_cvs];
        this.left_boundary = 51;
        this.right_boundary = 52;
        //this.other_ht_types = [);
        this.q_in = 0.0;
        this.q_out = 0.0;
        this.temp_left = 0.0;
        this.temp_right = 0.0;
        this.h_left = 500.0;
        this.h_right = 500.0;
    }

    public void cv_neighbours() {
        //for (int i = 0; i < this.num_cvs; i++) {
        for (ControlVolume cv : cvs) {
            int i = cv.global_index;
            if (i != 0 && i != this.num_cvs - 1) {
                cv.left_neighbour = this.cvs.get(i - 1);
                cv.right_neighbour = this.cvs.get(i + 1);
            }
            if (i == 0) {
                cv.right_neighbour = this.cvs.get(1);
            }
            if (i == this.num_cvs - 1) {
                cv.left_neighbour = this.cvs.get(this.num_cvs - 2);
            }
            //Window.alert(String.valueOf(cv.left_neighbour.global_index) + "\t" + String.valueOf(cv.right_neighbour.global_index));
        }
        //}
    }

    //TODO: add condition to check if TCC has <3 control volumes 
    public void build_TCC() {  // DOPOLNITI!
        //Arrays.sort(this.TCEs);

        int n1 = this.num_el;
        int m2 = 0;
        this.TCEs.get(0).left_neighbour = null;
        this.TCEs.get(n1 - 1).right_neighbour = null;
        if (this.TCEs.size() == 1) {
            this.TCEs.get(0).right_neighbour = null;
            this.TCEs.get(n1 - 1).left_neighbour = null;
        } else {
            int m1 = TCEs.get(n1 - 1).num_components;
            this.TCEs.get(0).right_neighbour = this.TCEs.get(1);
            this.TCEs.get(n1 - 1).left_neighbour = this.TCEs.get(n1 - 2);
            this.TCEs.get(0).components.get(0).left_neighbour = null;
            this.TCEs.get(n1 - 1).components.get(m1 - 1).right_neighbour = null;
        }

        for (int i = 1; i < this.num_el - 1; i++) {
            this.TCEs.get(i).right_neighbour = this.TCEs.get(i + 1);
            this.TCEs.get(i).left_neighbour = this.TCEs.get(i - 1);
            m2 = TCEs.get(i).num_components;
            this.TCEs.get(i).components.get(m2 - 1).right_neighbour = this.TCEs.get(i + 1).components.get(0);
            this.TCEs.get(i + 1).left_neighbour = this.TCEs.get(i);
            this.TCEs.get(i + 1).components.get(0).left_neighbour = this.TCEs.get(i).components.get(TCEs.get(i).num_components - 1);
        }

        for (int i = 0; i < this.num_el; i++) {
            this.TCEs.get(i).build_TCE();
        }
        this.cvs.clear();
        int global_index = 0;
        for (int i = 0; i < num_el; i++) {
            this.TCEs.get(i).parent = this;
            for (int j = 0; j < this.TCEs.get(i).num_cvs; j++) {
                this.TCEs.get(i).cvs.get(j).global_index = global_index;
                this.cvs.add(this.TCEs.get(i).cvs.get(j));
                global_index++;
            }
        }
        this.cv_neighbours();
    }

    public void calc_conduct_lr() {
        for (ControlVolume cv : cvs) {
            cv.calc_conduct_lr();
        }
    }

    public void set_starting_temps(double[] start_temps) {
        for (int i = 0; i < this.num_cvs; i++) {
            this.cvs.get(i).temperature = start_temps[i];
            this.cvs.get(i).temperature_old = start_temps[i];
        }
    }

    public void replace_old_new() {
        Iterator it = cvs.iterator();
        while (it.hasNext()) {
            ControlVolume cv = (ControlVolume) it.next();
            cv.temperature_old = cv.temperature;
        }
    }

    public double getQ_in() {
        return this.q_in;
    }

    public double getQ_out() {
        return this.q_out;
    }

    public double getTemp_left() {
        return this.temp_left;
    }

    public double getTemp_right() {
        return this.temp_right;
    }

    public double getH_left() {
        return this.h_left;
    }

    public double getH_right() {
        return this.h_right;
    }

    public void initializeMatrix() {
        for (int i = 0; i < this.num_cvs; i++) {
            this.underdiag[i] = 0.0;
            this.diag[i] = 0.0;
            this.upperdiag[i] = 0.0;
            this.rhs[i] = 0.0;
        }
    }

    public void makeMatrix(double dt) {
        EquationSystem.conductionTridag(this, dt);
    }

    public String print_attributes() {
        String txt = "";
        String bcleft = ModelMethods.return_bc_name(this.left_boundary);
        String bcright = ModelMethods.return_bc_name(this.right_boundary);
        txt += "\nBoundary condition on the left: " + bcleft;
        txt += "\nBoundary condition on the right: " + bcright;
        if (this.left_boundary == 31 || this.left_boundary == 41)
            txt += "\nTemperature on the left: " + String.valueOf(this.temp_left) + " K";
        if (this.left_boundary == 41)
            txt += "\nConvection coefficient on the left: " + String.valueOf(this.h_left) + " W/(m^2K)";
        if (this.left_boundary == 21)
            txt += "\nHeat flow on the left: " + String.valueOf(this.q_in) + " W/(m^2K)";
        if (this.right_boundary == 32 || this.right_boundary == 42)
            txt += "\nTemperature on the right: " + String.valueOf(this.temp_right) + " K";
        if (this.right_boundary == 42)
            txt += "\nConvection coefficient on the right: " + String.valueOf(this.h_right) + " W/(m^2K)";
        if (this.right_boundary == 22)
            txt += "\nHeat flow on the right: " + String.valueOf(this.q_out) + " W/(m^2K)";
        txt += "\n\nThermal control elements:";
        // for el in this.TCEs:
        // txt += "\n\nTCE: " + String.valueOf(el.name);
        // el.print_attributes(f);
        return txt;
    }
}
