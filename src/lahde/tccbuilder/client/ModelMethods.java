package lahde.tccbuilder.client;

import java.util.Vector;

import com.google.gwt.i18n.client.NumberFormat;

public class ModelMethods {

    /**
     * Real number version. c and d are modified during the operation. All input arrays must be of the same size.
     *
     * @param a
     *            the subdiagonal elements
     * @param b
     *            the diagonal elements
     * @param c
     *            the superdiagonal elements
     * @param d
     *            the right-hand-side vector
     */
    public static void tdmaSolve(Vector<ControlVolume> cvs, double[] a, double[] b, double[] c, double[] d) {
        int n = d.length;
        double temp;
        c[0] /= b[0];
        d[0] /= b[0];
        for (int i = 1; i < n; i++) {
            temp = 1.0 / (b[i] - c[i - 1] * a[i]);
            c[i] *= temp; // redundant at the last step as c[n-1]=0.
            d[i] = (d[i] - d[i - 1] * a[i]) * temp;
        }
        double[] x = new double[n];
        x[n - 1] = d[n - 1];
        for (int i = n - 2; i >= 0; i--) {
            x[i] = d[i] - c[i] * x[i + 1];
        }
        for (int i = 0; i != x.length; i++) {
            cvs.get(i).temperature = x[i];
        }
    }

    public static double linInterp(double u, Vector<Double> x, Vector<Double> y) {
        if (x.size() != y.size()) {
            throw new IllegalArgumentException("Sizes of arrays don't match!");
        }
        int low = 0, high = x.size(), mid = 0;
        while (low != high) {
            mid = (low + high) / 2;
            if (x.get(mid) <= u) {
                low = mid + 1;
            }
            else { high = mid; }
        }
        if (high >= x.size()) {
            //if (x[mid] == u) return y[mid];
            return y.get(mid);
            // else throw new IndexOutOfBoundsException("Couldn't find value!");
        }
        if (high == 0) {
            //if (x[mid] == u) return y[mid];
            return y.get(0);
            // else throw new IndexOutOfBoundsException("Couldn't find value!");
        }
        double x1 = x.get(high - 1), y1 = y.get(high - 1);
        double x2 = x.get(high), y2 = y.get(high);
        double v = y1 + (u - x1) * (y2 - y1)/(x2 - x1);
        return v;
    }

    public static String printTemps(Double time, Double[] temps) {
        String s = NumberFormat.getFormat("0.000").format(time) + "\t";
        for (double temp : temps) {
            s += NumberFormat.getFormat("0.00").format(temp) + "\t";
        }
        return s;
    }

    public static String return_bc_name(Simulation.BorderCondition bc) {
        String bcName = "";
        if (bc == Simulation.BorderCondition.ADIABATIC) {
            bcName = "Adiabatic";
        }
        if (bc == Simulation.BorderCondition.CONSTANT_HEAT_FLUX) {
            bcName = "Constant heat flux";
        }
        if (bc == Simulation.BorderCondition.CONSTANT_TEMPERATURE) {
            bcName = "Constant temperature";
        }
        if (bc == Simulation.BorderCondition.CONVECTIVE) {
            bcName = "Convection";
        }

        return bcName;
    }

    public static class CVinterface {

        public double b1;
        public double c1;
        public double b2;
        public double a2;
        public double T1;
        public double T2;

        public void calculateCoefficients(ControlVolume westCV, ControlVolume eastCV) {
            // From 1D conduction(2013), eq. 37
            double rr = westCV.eastResistance;
            double lr = eastCV.westResistance;
            double u1 = westCV.k() / westCV.dx + eastCV.k() / eastCV.dx + rr * (westCV.k() / westCV.dx) * (eastCV.k() / eastCV.dx);
            double u2 = westCV.k() / westCV.dx + eastCV.k() / eastCV.dx + lr * (westCV.k() / westCV.dx) * (eastCV.k() / eastCV.dx);
            b1 = (westCV.k() / westCV.dx + rr * (westCV.k() / westCV.dx) * (eastCV.k() / eastCV.dx)) / u1;
            c1 = (eastCV.k() / eastCV.dx) / u1;
            b2 = (eastCV.k() / eastCV.dx + lr * (westCV.k() / westCV.dx) * (eastCV.k() / eastCV.dx)) / u2;
            a2 = (westCV.k() / westCV.dx) / u2;
            T1 = b1 * westCV.temperature + c1 * eastCV.temperature;  // This is temperature on the west side of interface, in cv_w
            T2 = b2 * eastCV.temperature + a2 * westCV.temperature;  // This is temperature on the east side of interface, in cv_e
        }

        public double temperatureGradient(ControlVolume cv) {
            calculateCoefficients(cv.westNeighbour, cv);
            double Tw = T1;
            calculateCoefficients(cv, cv.eastNeighbour);
            double Te = T2;
            double der = (T2 - T1) / cv.dx;
            return der;
        }

    }

}
