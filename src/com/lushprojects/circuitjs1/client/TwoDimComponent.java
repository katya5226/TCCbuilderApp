package com.lushprojects.circuitjs1.client;

import com.google.gwt.core.client.GWT;
import com.lushprojects.circuitjs1.client.util.Locale;

import java.lang.Math;
import java.text.MessageFormat;
import java.util.*;

import com.google.gwt.user.client.Window;

public class TwoDimComponent extends CircuitElm implements Comparable<TwoDimComponent> {
    double resistance;
    Color color;
    public double length, height;
    public String name;
    public int index;
    public Material material;
    public int numCvs, n, m; // total # of CVs and # of CVs in x and y directions
    public double [] resistances;
    public TwoDimComponent [] neighbours;
    public int [] boundaries;
    public double constRho, constCp, constK;
    public Vector<TwoDimCV> cvs;
    public boolean isDisabled;
    public boolean field;
    public int fieldIndex;

    public TwoDimComponent(int xx, int yy) {
        super(xx, yy);
        //initializeComponent();
        this.index = -1;
    }

    @Override
    public int compareTo(TwoDimComponent o) {
        return this.index - o.index;
    }
}