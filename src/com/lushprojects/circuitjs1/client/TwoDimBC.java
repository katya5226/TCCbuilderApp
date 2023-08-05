package com.lushprojects.circuitjs1.client;

// import com.google.gwt.core.client.GWT;
// import com.google.gwt.user.client.Window;

// import java.util.*;

public class TwoDimBC {
    BC[] bcType;
    double[] T;
    double[] q;  // this will be changed with respect to 1D as it will not be an absolute value
    double[] h;

    public TwoDimBC(BC[] bt) {
        bcType = new BC[]{bt[0], bt[1], bt[2], bt[3]};
        T = new double[]{300.0, 300.0, 300.0, 300.0};
        q = new double[]{0.0, 0.0, 0.0, 0.0};
        h = new double[]{0.0, 0.0, 0.0, 0.0};
    }
}