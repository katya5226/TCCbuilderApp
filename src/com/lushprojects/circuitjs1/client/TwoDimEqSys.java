package com.lushprojects.circuitjs1.client;

import com.google.gwt.canvas.dom.client.CanvasGradient;
import com.lushprojects.circuitjs1.client.util.Locale;
import java.lang.Math;
import java.util.*;


public class TwoDimEqSys {
    TwoDimTCE aTCE;
    TwoDimControlVolume currCV;
    double[] kd;
    double[] ad;
    double[] hd;
    double rcp, gen;
    double dx, dy, dt;
    int n, m;
    int numCvs;
    double[] Tb;
    double tempOld;
    double[][] matrix;
    double[] rhs;
    double h;

    public TwoDimEquationSystem(TwoDimTCE obj) {
        aTCE = obj;
        currCV = obj.cvs.get(0);
        n = obj.n, m = obj.m;
        numCvs = n * m;
        dt = 0.01;
        matrix = new double[n * m][n * m];
        for (double[] row: matrix)
            Arrays.fill(row, 0.0);
        rhs = new double[n * m];
        Arrays.fill(rhs, 0.0);
        kd = new double[]{0.0, 0.0, 0.0, 0.0};
        ad = new double[]{0.0, 0.0, 0.0, 0.0};
        hd = new double[]{0.0, 0.0, 0.0, 0.0};
        Tb = new double[]{0.0, 0.0, 0.0, 0.0};
        h = 10000;
    }

    void setParameters() {
        dx = currCV.dx;
        dy = currCV.dy;
        tempOld = currCV.temperatureOld;
        rcp = currCV.rho() * currCV.cp() * pow(dx, 2) * pow(dy, 2);
        gen = currCV.qGen * dt * pow(dx, 2) * pow(dy, 2);
        currCV.calculateConductivities();
        for (int i = 0; i < 4; i++) {
            kd[i] = currCV.kd[i];
        }
        hd[0] = pow(dy, 2 ) * dt * h * dx;
        hd[1] = pow(dy, 2 ) * dt * h * dx;
        hd[2] = pow(dx, 2 ) * dt * h * dy;
        hd[3] = pow(dx, 2 ) * dt * h * dy;
        ad[0] = pow(dy, 2 ) * dt * kd[0];
        ad[1] = pow(dy, 2 ) * dt * kd[1];
        ad[2] = pow(dx, 2 ) * dt * kd[2];
        ad[3] = pow(dx, 2 ) * dt * kd[3];
    }

    void conductionMatrix() {
        // south-west corner
        currCV = aTCE.cvs.get(0);
        setParameters();
        matrix[0][1] = - ad[1] - 0.5 * hd[0];
        matrix[0][0] = ad[1] + ad[3] + rcp + 1.5 * hd[0];
        matrix[0][n] = - ad[3];
        rhs[0] = rcp * tempOld + hd[0] * Tb[0];

        // first (south, bottom) row
        for(int j = 0; j < n-1; j++) {
            currCV = aTCE.cvs.get(j);
            setParameters();
            matrix[j][j - 1] = - ad[0];
            matrix[j][j] = ad[0] + ad[1] + ad[3] + rcp;
            matrix[j][j + 1] = - ad[1];
            matrix[j][j + n] = - ad[3];
            rhs[j] = rcp * tempOld;
        }

        // south-east corner
        currCV = aTCE.cvs.get(n - 1);
        setParameters();
        matrix[n - 1][n - 2] = - ad[0] - 0.5 * hd[1];
        matrix[n - 1][n - 1] = ad[0] + ad[3] + rcp + 1.5 * hd[1];
        matrix[n - 1][2 * n - 1] = - ad[3];
        rhs[n - 1] = rcp * tempOld + hd[1] * Tb[1];

        // first (west) column
        for(int j = 0; j < (m - 1) * n; j += n) {
            currCV = aTCE.cvs.get(j);
            setParameters();
            matrix[j][j] = ad[1] + ad[2] + ad[3] + rcp + 1.5 * hd[0];
            matrix[j][j - n] = -ad[2];
            matrix[j][j + n] = -ad[3];
            matrix[j][j + 1] = -ad[1] - 0.5 * hd[0];
            rhs[j] = rcp * tempOld + hd[0] * Tb[0];
        }

        // north-west corner
        int ind = (m - 1) * n;
        int ind_s = (m - 2) * n;
        int ind_e = (m - 1) * n + 1;
        currCV = aTCE.cvs.get(ind);
        setParameters();
        matrix[ind][ind_s] = - ad[2];
        matrix[ind][ind] = ad[1] + ad[2] + rcp + 1.5 * hd[0];
        matrix[ind][ind_e] = - ad[1] - 0.5 * hd[0];
        rhs[ind] = rcp * tempOld + hd[0] * Tb[0];

        // last (top, north) row
        for(int j = (m - 1) * n + 1; j <  m * n - 1; j ++) {
            currCV = aTCE.cvs.get(j);
            setParameters();
            matrix[j][j - 1] = -ad[0];
            matrix[j][j] = ad[0] + ad[1] + ad[2] + rcp;
            matrix[j][j + 1] = -ad[1];
            matrix[j][j - n] = -ad[2];
            rhs[j] = rcp * tempOld;
        }

        // north-east corner
        ind = m * n - 1;
        int ind_w = m * n - 2;
        ind_s = (m - 1) * n - 1;
        currCV = aTCE.cvs.get(ind);
        setParameters();
        matrix[ind][ind_w] = - ad[0] - 0.5 * hd[1];
        matrix[ind][ind] = ad[0] + ad[2] + rcp + 1.5 * hd[1];
        matrix[ind][ind_s] = - ad[2];
        rhs[ind] = rcp * tempOld + hd[1] * Tb[1];

        // last (east) column
        for(int j = 2 * n - 1; j <  (m - 1) * n; j += n) {
            currCV = aTCE.cvs.get(j);
            setParameters();
            matrix[j][j] = ad[0] + ad[2] + ad[3] + rcp + 1.5 * hd[1];
            matrix[j][j - n] = -ad[2];
            matrix[j][j + n] = -ad[3];
            matrix[j][j - 1] = -ad[0] - 0.5 * hd[1];
            rhs[j] = rcp * tempOld + hd[1] * Tb[1];
        }

        // core
        for(int k = 1; k < m - 1; k++) {
            for (int j = k * n + 1; j < (k + 1) * n - 1; j++) {
                currCV = aTCE.cvs.get(j);
                setParameters();
                matrix[j][j - 1] = -ad[0];
                matrix[j][j] = ad[0] + ad[1] + ad[2] + ad[3] + rcp;
                matrix[j][j + 1] = -ad[1];
                matrix[j][j - n] = -ad[2];
                matrix[j][j + n] = -ad[3];
                rhs[j] = rcp * tempOld + gen
            }
        }
    }

}
