package com.lushprojects.circuitjs1.client;

import com.google.gwt.canvas.dom.client.CanvasGradient;
import com.google.gwt.core.client.GWT;
import com.lushprojects.circuitjs1.client.util.Locale;

import java.lang.Math;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;
import java.util.Collections;
//Comment

public class TwoDimTCE implements Comparable<TwoDimTCE> {
    // For now, 2D TCE is only a row of TwoDimComponents; they can't be stacked upon each other.
    //TwoDimTCC parent;
    String name;
    int index;
    Vector <TwoDimComponent> components;
    int numComponents;
    int numCvs, n, m; // total # of CVs and # of CVs in x and y directions
    double [] resistances;
    TwoDimTCE [] neighbours;
    int [] boundaries;
    double Tb[];
    double hb[];
    double q[];
    Vector<TwoDimCV> cvs;
    double length, height;

    public TwoDimTCE(String name, int index, Vector<TwoDimComponent> components) {
        //this.parent = null;
        this.name = name;
        this.index = index;
        this.components = components;
        this.cvs = new Vector<TwoDimCV>();
    }

    void cvNeighbours() {
        // corners;
        cvs.get(0).neighbours[1] = cvs.get(1);
        cvs.get(0).neighbours[3] = cvs.get(n);
        cvs.get(n - 1).neighbours[0] = cvs.get(n - 2);
        cvs.get(n - 1).neighbours[3] = cvs.get(2 * n - 1);
        cvs.get((m - 1) * n).neighbours[1] = cvs.get((m - 1) * n + 1);
        cvs.get((m - 1) * n).neighbours[2] = cvs.get((m - 2) * n);
        cvs.get(-1).neighbours[0] = cvs.get(-2);
        cvs.get(-1).neighbours[2] = cvs.get(-n - 1);
        // south row;
        for (int i = 0; i < n-1; i++) {
            cvs.get(i).neighbours[0] = cvs.get(i - 1);
            cvs.get(i).neighbours[1] = cvs.get(i + 1);
            cvs.get(i).neighbours[3] = cvs.get(i + n);
        }
        // north row;
        for (int i = (m - 1) * n + 1; i < m * n - 1; i++) {
            cvs.get(i).neighbours[0] = cvs.get(i - 1);
            cvs.get(i).neighbours[1] = cvs.get(i + 1);
            cvs.get(i).neighbours[2] = cvs.get(i - n);
        }
        // west column;
        for (int j = 1; j < m - 1; j++) {
            cvs.get(j * n).neighbours[1] = cvs.get(j * n + 1);
            cvs.get(j * n).neighbours[2] = cvs.get((j - 1) * n);
            cvs.get(j * n).neighbours[3] = cvs.get((j + 1) * n);
        }
        // east column;
        for (int j = 1; j < m - 1; j++) {
            cvs.get((j + 1) * n - 1).neighbours[0] = cvs.get((j + 1) * n - 2);
            cvs.get((j + 1) * n - 1).neighbours[2] = cvs.get(j * n - 1);
            cvs.get((j + 1) * n - 1).neighbours[3] = cvs.get((j + 2) * n - 1);
        }
        // middle;
        for (int j = 1; j < m - 1; j++) {
            for (int i = 1; i < n - 1; i++) {
                int indx = j * n + i;
                cvs.get(indx).neighbours[0] = cvs.get(indx - 1);
                cvs.get(indx).neighbours[1] = cvs.get(indx + 1);
                cvs.get(indx).neighbours[2] = cvs.get(indx - n);
                cvs.get(indx).neighbours[3] = cvs.get(indx + n);
            }
        }
    }

    void buildTCE() {
//        for comp in components:
//        if comp.m != components.get(0].m:
//        raise ValueError("Components must be of the same height and have the same y-discretisation!")
        numComponents = components.size();
        numCvs = n = 0;
        length = 0.0;
        m = components.get(0).m;  // TODO: check if all are of the same height and m
        height = components.get(0).height;
        for (TwoDimComponent c : components) {
            numCvs += c.numCvs;
            n += c.n;
        }
        Collections.sort(components);
        if (neighbours[0] == null)
            components.get(0).neighbours[0] = null;
        if (neighbours[1] == null)
            components.get(-1).neighbours[1] = null;
        if (neighbours[0] != null)
            components.get(0).neighbours[0] = neighbours[0].components.get(-1);
        if (neighbours[1] != null)
            components.get(-1).neighbours[1] = neighbours[1].components.get(0);
        for (int i = 0; i < numComponents - 1; i++) {
            components.get(i).boundaries[1] = 52;
            components.get(i).neighbours[1] = components.get(i + 1);
            components.get(i + 1).neighbours[0] = components.get(i);
        }
        for (int i = 1; i < numComponents; i++) {
            components.get(i).boundaries[0] = 51;
            components.get(i - 1).cvs.get(-1).neighbours[1] = components.get(i).cvs.get(0);
            components.get(i).cvs.get(0).neighbours[0] = components.get(i - 1).cvs.get(-1);
        }
        components.get(0).boundaries[0] = boundaries[0];
        components.get(-1).boundaries[1] = boundaries[1];
        int cvTCEind = 0;
        cvs.clear();
        for (int j = 0; j < m; j++) {
            for (int k = 0; k < numComponents; k++) {
                TwoDimComponent cmp = components.get(k);
                for (int i = 0; i < cmp.n; i++) {
                    cmp.cvs.get(j * cmp.n + i).TCEindex = cvTCEind;
                    cmp.cvs.get(j * cmp.n + i).globalIndex = cvTCEind;
                    cvs.add(cmp.cvs.get(j * cmp.n + i));
                    cvTCEind += 1;
                }
            }
        }
        cvNeighbours();
        setGlobalXY();
        //hf.calc_length(self);
    }

    void setGlobalXY() {
        double dy0 = cvs.get(0).dy;
        double dx0 = cvs.get(0).dx;
        for (int j = 0; j < m; j++) {
            cvs.get(j * n).xGlobal = dx0 / 2;
            for (int i = 0; i < n; i++) {
                cvs.get(j * n + i).yGlobal = dy0 / 2 + m * dy0;
            }
            for (int i = 1; i < n; i++) {
                cvs.get(j * n + i).xGlobal = cvs.get(j * n + i - 1).xGlobal + (cvs.get(j * n + i - 1).dx + cvs.get(j * n + i).dx) / 2;
            }
        }
    }

    @Override
    public int compareTo(TwoDimTCE e) {
        return this.index - e.index;
    }
}