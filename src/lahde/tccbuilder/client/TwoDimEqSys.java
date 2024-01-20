package lahde.tccbuilder.client;

import lahde.tccbuilder.client.math3.linear.RealMatrix;
import lahde.tccbuilder.client.math3.linear.RealVector;
import lahde.tccbuilder.client.math3.linear.Array2DRowRealMatrix;
import lahde.tccbuilder.client.math3.linear.ArrayRealVector;
import lahde.tccbuilder.client.math3.linear.OpenMapRealMatrix;
import lahde.tccbuilder.client.math3.linear.OpenMapRealVector;

import java.lang.Math;


public class TwoDimEqSys {
    TwoDimTCE aTCE;
    TwoDimCV currCV;
    TwoDimBC boundCond;
    double[] kd;
    double[] ad;
    double[] hd;
    double rcp, gen;
    double dx, dy, dt;
    int n, m;
    int numCvs;
    double tempOld;
    RealMatrix matrix;
    RealVector rhs;

    public TwoDimEqSys(TwoDimTCE obj, TwoDimBC bc) {
        aTCE = obj;
        this.boundCond = bc;
        currCV = obj.cvs.get(0);
        n = obj.n; m = obj.m;
        numCvs = n * m;
        dt = 0.005;
        matrix = new Array2DRowRealMatrix(numCvs, numCvs);
        rhs = new ArrayRealVector(numCvs);
        resetMatrix();
        kd = new double[]{0.0, 0.0, 0.0, 0.0};
        ad = new double[]{0.0, 0.0, 0.0, 0.0};
        hd = new double[]{0.0, 0.0, 0.0, 0.0};
    }

    void resetMatrix() {
        for (int i = 0; i < numCvs; i ++) {
            for (int j = 0; j < numCvs; j ++) {
                matrix.setEntry(i, j, 0.0);
            }
        }
        for (int i = 0; i < numCvs; i ++) {
            rhs.setEntry(i, 0.0);
        }
    }

    void setParameters() {
        dx = currCV.dx;
        dy = currCV.dy;
        tempOld = currCV.temperatureOld;
        rcp = currCV.rho() * currCV.cp() * Math.pow(dx, 2) * Math.pow(dy, 2);
        gen = currCV.qGen * dt * Math.pow(dx, 2) * Math.pow(dy, 2);
        currCV.calculateConductivities();
        for (int i = 0; i < 4; i++) {
            kd[i] = currCV.kd[i];
        }
        hd[0] = Math.pow(dy, 2) * dt * boundCond.h[0] * dx;
        hd[1] = Math.pow(dy, 2) * dt * boundCond.h[0] * dx;
        hd[2] = 0.0; // Math.pow(dx, 2) * dt * h * dy;
        hd[3] = 0.0; // Math.pow(dx, 2) * dt * h * dy;
        ad[0] = Math.pow(dy, 2) * dt * kd[0];
        ad[1] = Math.pow(dy, 2) * dt * kd[1];
        ad[2] = Math.pow(dx, 2) * dt * kd[2];
        ad[3] = Math.pow(dx, 2) * dt * kd[3];

    }

    void conductionMatrix() {
        // south-west corner
        currCV = aTCE.cvs.get(0);
        setParameters();
        matrix.setEntry(0, 1, - ad[1] - 0.5 * hd[0]);
        matrix.setEntry(0, 0, ad[1] + ad[3] + rcp + 1.5 * hd[0]);
        matrix.setEntry(0, n, - ad[3]);
        rhs.setEntry(0, rcp * tempOld + hd[0] * boundCond.T[0] + gen);

        // first (south, bottom) row
        for(int j = 1; j < n-1; j++) {
            currCV = aTCE.cvs.get(j);
            setParameters();

            matrix.setEntry(j, j - 1, - ad[0]);
            matrix.setEntry(j, j, ad[0] + ad[1] + ad[3] + rcp);
            matrix.setEntry(j, j + 1, - ad[1]);
            matrix.setEntry(j, j + n, - ad[3]);
            rhs.setEntry(j, rcp * tempOld + gen);

        }
        // south-east corner
        currCV = aTCE.cvs.get(n - 1);
        setParameters();
        matrix.setEntry(n - 1, n - 2, - ad[0] - 0.5 * hd[1]);
        matrix.setEntry(n - 1, n - 1, ad[0] + ad[3] + rcp + 1.5 * hd[1]);
        matrix.setEntry(n - 1, 2 * n - 1, - ad[3]);
        rhs.setEntry(n - 1, rcp * tempOld + hd[1] * boundCond.T[1] + gen);

        // first (west) column
        for(int j = n; j < (m - 1) * n; j += n) {
            currCV = aTCE.cvs.get(j);
            setParameters();

            matrix.setEntry(j, j, ad[1] + ad[2] + ad[3] + rcp + 1.5 * hd[0]);
            matrix.setEntry(j, j - n, -ad[2]);
            matrix.setEntry(j, j + n, -ad[3]);
            matrix.setEntry(j, j + 1, -ad[1] - 0.5 * hd[0]);
            rhs.setEntry(j, rcp * tempOld + hd[0] * boundCond.T[0] + gen);

        }
        // north-west corner
        int ind = (m - 1) * n;
        int ind_s = (m - 2) * n;
        int ind_e = (m - 1) * n + 1;
        currCV = aTCE.cvs.get(ind);
        setParameters();

        matrix.setEntry(ind, ind_s, - ad[2]);
        matrix.setEntry(ind, ind, ad[1] + ad[2] + rcp + 1.5 * hd[0]);
        matrix.setEntry(ind, ind_e, - ad[1] - 0.5 * hd[0]);
        rhs.setEntry(ind, rcp * tempOld + hd[0] * boundCond.T[0] + gen);

        // last (top, north) row
        for(int j = (m - 1) * n + 1; j <  m * n - 1; j ++) {
            currCV = aTCE.cvs.get(j);
            setParameters();

            matrix.setEntry(j, j - 1, -ad[0]);
            matrix.setEntry(j, j, ad[0] + ad[1] + ad[2] + rcp);
            matrix.setEntry(j, j + 1, -ad[1]);
            matrix.setEntry(j, j - n, -ad[2]);
            rhs.setEntry(j, rcp * tempOld + gen);

        }
        // north-east corner
        ind = m * n - 1;
        int ind_w = m * n - 2;
        ind_s = (m - 1) * n - 1;
        currCV = aTCE.cvs.get(ind);
        setParameters();

        matrix.setEntry(ind, ind_w, - ad[0] - 0.5 * hd[1]);
        matrix.setEntry(ind, ind, ad[0] + ad[2] + rcp + 1.5 * hd[1]);
        matrix.setEntry(ind, ind_s, - ad[2]);
        rhs.setEntry(ind, rcp * tempOld + hd[1] * boundCond.T[1] + gen);

        // last (east) column
        for(int j = 2 * n - 1; j < (m - 1) * n; j += n) {
            currCV = aTCE.cvs.get(j);
            setParameters();

            matrix.setEntry(j, j, ad[0] + ad[2] + ad[3] + rcp + 1.5 * hd[1]);
            matrix.setEntry(j, j - n, -ad[2]);
            matrix.setEntry(j, j + n, -ad[3]);
            matrix.setEntry(j, j - 1, -ad[0] - 0.5 * hd[1]);
            rhs.setEntry(j, rcp * tempOld + hd[1] * boundCond.T[1] + gen);

        }
        // core
        for(int k = 1; k < m - 1; k++) {
            for (int j = k * n + 1; j < (k + 1) * n - 1; j++) {
                currCV = aTCE.cvs.get(j);
                setParameters();

                matrix.setEntry(j, j - 1, -ad[0]);
                matrix.setEntry(j, j, ad[0] + ad[1] + ad[2] + ad[3] + rcp);
                matrix.setEntry(j, j + 1, -ad[1]);
                matrix.setEntry(j, j - n, -ad[2]);
                matrix.setEntry(j, j + n, -ad[3]);
                rhs.setEntry(j, rcp * tempOld + gen);

            }
        }

    }

}
