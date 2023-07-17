package com.lushprojects.circuitjs1.client;

import com.google.gwt.canvas.dom.client.CanvasGradient;
import com.lushprojects.circuitjs1.client.util.Locale;

import java.lang.Math;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;
import java.util.Collections;
//Comment

public class TCE implements Comparable<TCE> {
    public TCC parent;
    public String name;
    public int index;
    public Vector<Component> components;
    public int num_components;
    public int num_cvs;
    public Vector<ControlVolume> cvs;
    public TCE left_neighbour;
    public TCE right_neighbour;
    public double[] underdiag;
    public double[] diag;
    public double[] upperdiag;
    public double[] rhs;
    public int left_boundary;
    public int right_boundary;
    public double q_in, q_out;
    public double temp_left, temp_right;
    public double h_left, h_right;

    public TCE(String name, int index, Vector<Component> components) {
        this.parent = null;
        this.name = name;
        this.index = index;
        this.components = components;
        this.num_components = components.size();
        this.num_cvs = 0;
        for (Component c : components) {
            this.num_cvs += c.num_cvs;
        }
        this.cvs = new Vector<ControlVolume>();
        this.left_neighbour = this;
        this.right_neighbour = this;
        this.underdiag = new double[this.num_cvs];
        this.diag = new double[this.num_cvs];
        this.upperdiag = new double[this.num_cvs];
        this.rhs = new double[this.num_cvs];
        this.left_boundary = 51;
        this.right_boundary = 52;
        this.q_in = 0.0;
        this.q_out = 0.0;
        this.temp_left = 0.0;
        this.temp_right = 0.0;
        this.h_left = 500.0;
        this.h_right = 500.0;
    }

    @Override
    public int compareTo(TCE e) {
        return this.index - e.index;
    }

    public void cv_neighbours() {
        if (this.num_cvs > 1)
            for (int i = 0; i < this.num_cvs; i++) {
                ControlVolume cv = this.cvs.get(i);
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
            }
    }

    public void build_TCE() {
        Collections.sort(this.components);
        int n1 = this.num_components;
        int n2 = this.components.get(n1 - 1).num_cvs;
        if (this.left_neighbour == null) {
            this.components.get(0).left_neighbour = null;
            this.components.get(0).cvs.get(0).left_neighbour = this.components.get(0).cvs.get(0);
        }
        if (this.right_neighbour == null) {
            this.components.get(n1 - 1).right_neighbour = null;
            this.components.get(n1 - 1).cvs.get(n2 - 1).right_neighbour = this.components.get(n1 - 1).cvs.get(n2 - 1);
        }
        if (this.left_neighbour != null) {
            int m1 = this.left_neighbour.num_components;
            int m2 = this.left_neighbour.components.get(m1 - 1).num_cvs;
            this.components.get(0).left_neighbour = this.left_neighbour.components.get(m1 - 1);
            this.components.get(0).cvs.get(0).left_neighbour = this.left_neighbour.components.get(m1 - 1).cvs.get(m2 - 1);
        }
        if (this.right_neighbour != null) {
            this.components.get(n1 - 1).right_neighbour = this.right_neighbour.components.get(0);
            this.components.get(n1 - 1).cvs.get(n2 - 1).right_neighbour = this.right_neighbour.components.get(0).cvs.get(0);
        }
        for (int i = 0; i < n1 - 1; i++) {
            this.components.get(i).right_boundary = 52;
            this.components.get(i).right_neighbour = this.components.get(i + 1);
            this.components.get(i + 1).left_neighbour = this.components.get(i);
        }
        for (int i = 1; i < n1; i++) {
            this.components.get(i).left_boundary = 51;
            int l = this.components.get(i - 1).num_cvs;
            this.components.get(i - 1).cvs.get(l - 1).right_neighbour = this.components.get(i).cvs.get(0);
            this.components.get(i).cvs.get(0).left_neighbour = this.components.get(i - 1).cvs.get(l - 1);
        }
        this.cvs.clear();
        int TCE_index = 0;
        for (int i = 0; i < n1; i++) {
            this.components.get(0).left_boundary = this.left_boundary;
            this.components.get(n1 - 1).right_boundary = this.right_boundary;
            this.components.get(i).cvs.get(0).left_resistance = this.components.get(i).left_resistance;
            this.components.get(i).cvs.get(components.get(i).num_cvs - 1).right_resistance = this.components.get(i).right_resistance;
            for (int j = 0; j < this.components.get(i).num_cvs; j++) {
                this.components.get(i).cvs.get(j).TCE_index = TCE_index;
                this.cvs.add(this.components.get(i).cvs.get(j));
                TCE_index++;
            }
        }
        this.cv_neighbours();
    }

    public void calc_conduct_lr() {
        Iterator it = this.cvs.iterator();
        ControlVolume cv = null;
        while (it.hasNext()) {
            cv = (ControlVolume) it.next();
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
        Iterator it = this.cvs.iterator();
        ControlVolume cv = null;
        while (it.hasNext()) {
            cv = (ControlVolume) it.next();
            cv.temperature_old = cv.temperature;
        }
    }
//    public void  print_attributes(f) {
//        f.write("\n\nComponents:\n")
//        for cp in this.components:
//        cp.print_attributes(f)
//    }

}

