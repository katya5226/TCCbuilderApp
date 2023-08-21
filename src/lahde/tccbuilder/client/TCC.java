package lahde.tccbuilder.client;

import java.lang.Math;
import java.util.Vector;
import java.util.Collections;

public class TCC {
    public String name;
    public Vector<ThermalControlElement> TCEs;
    public Vector<ControlVolume> cvs;
    public double[] underdiag;
    public double[] diag;
    public double[] upperdiag;
    public double[] rhs;
    public int westBoundary;
    public int eastBoundary;
    //public int[) other_ht_types;
    public double qWest, qEast;
    public double temperatureWest, temperatureEast;
    public double hWest, hEast;

    public Vector<Double> fluxes;

    public TCC(String name, Vector<ThermalControlElement> TCEs) {
        //parent_sim = null;
        this.name = name;
        this.TCEs = TCEs;
        int numCvs = 0;
        for (ThermalControlElement tce : TCEs) {
            numCvs += tce.cvs.size();
        }
        cvs = new Vector<ControlVolume>();
        underdiag = new double[numCvs];
        diag = new double[numCvs];
        upperdiag = new double[numCvs];
        rhs = new double[numCvs];
        westBoundary = 51;
        eastBoundary = 52;
        //other_ht_types = [);
        qWest = 0.0;
        qEast = 0.0;
        temperatureWest = 0.0;
        temperatureEast = 0.0;
        hWest = 500.0;
        hEast = 500.0;

        fluxes = new Vector<Double>();
    }

    public void setNeighbours() {
        cvs.get(0).westNeighbour = cvs.get(0);
        cvs.get(cvs.size() - 1).eastNeighbour = cvs.get(cvs.size() - 1);
        if (cvs.size() > 1) {
            for (int i = 0; i < cvs.size(); i++) {
                ControlVolume cv = cvs.get(i);
                if (i != 0 && i != cvs.size() - 1) {
                    cv.westNeighbour = cvs.get(i - 1);
                    cv.eastNeighbour = cvs.get(i + 1);
                }
                if (i == 0) {
                    cv.eastNeighbour = cvs.get(1);
                }
                if (i == cvs.size() - 1) {
                    cv.westNeighbour = cvs.get(cvs.size() - 2);
                }
            }
        }
    }

    //TODO: add condition to check if TCC has <3 control volumes 
    public void buildTCC() {  // DOPOLNITI!
        Collections.sort(TCEs);
        // TCEs.get(0).westBoundary = westBoundary;
        // TCEs.get(TCEs.size() - 1).eastBoundary = eastBoundary;
        for (int i = 0; i < TCEs.size() - 1; i++) {
            TCEs.get(i).eastBoundary = 52;
            TCEs.get(i).eastNeighbour = TCEs.get(i + 1);
            TCEs.get(i + 1).westNeighbour = TCEs.get(i);
        }
        for (int i = 1; i < TCEs.size(); i++) {
            TCEs.get(i).westBoundary = 51;
            int l = TCEs.get(i - 1).cvs.size();
            TCEs.get(i - 1).cvs.get(l - 1).eastNeighbour = TCEs.get(i).cvs.get(0);
            TCEs.get(i).cvs.get(0).westNeighbour = TCEs.get(i - 1).cvs.get(l - 1);
        }
        cvs.clear();
        int globalIndex = 0;
        for (int i = 0; i < TCEs.size(); i++) {
            TCEs.get(i).cvs.get(0).westResistance = TCEs.get(i).westResistance;
            TCEs.get(i).cvs.get(TCEs.get(i).cvs.size() - 1).eastResistance = TCEs.get(i).eastResistance;
            for (int j = 0; j < TCEs.get(i).cvs.size(); j++) {
                TCEs.get(i).cvs.get(j).globalIndex = globalIndex;
                cvs.add(TCEs.get(i).cvs.get(j));
                globalIndex++;
            }
        }
        setNeighbours();
    }

    public void calculateConductivities() {
        for (ControlVolume cv : cvs) {
            cv.calculateConductivities();
        }
    }

    public void setTemperatures(double[] temps) {
        for (int i = 0; i < cvs.size(); i++) {
            cvs.get(i).temperature = temps[i];
            cvs.get(i).temperatureOld = temps[i];
        }
    }

    public void replaceTemperatures() {
        for (ControlVolume cv : cvs)
            cv.temperatureOld = cv.temperature;
    }


    public void initializeMatrix() {
        for (int i = 0; i < cvs.size(); i++) {
            underdiag[i] = 0.0;
            diag[i] = 0.0;
            upperdiag[i] = 0.0;
            rhs[i] = 0.0;
        }
    }

    public void makeMatrix(double dt) {
        EquationSystem.conductionTridag(this, dt);
    }

    public String printAttributes() {
        String txt = "";
        String bcWest = ModelMethods.return_bc_name(westBoundary);
        String bcEast = ModelMethods.return_bc_name(eastBoundary);
        txt += "\nBoundary condition on the left: " + bcWest;
        txt += "\nBoundary condition on the right: " + bcEast;
        if (westBoundary == 31 || westBoundary == 41)
            txt += "\nTemperature on the left: " + String.valueOf(temperatureWest) + " K";
        if (westBoundary == 41)
            txt += "\nConvection coefficient on the left: " + String.valueOf(hWest) + " W/(m^2K)";
        if (westBoundary == 21)
            txt += "\nHeat flow on the left: " + String.valueOf(qWest) + " W/(m^2K)";
        if (eastBoundary == 32 || eastBoundary == 42)
            txt += "\nTemperature on the right: " + String.valueOf(temperatureEast) + " K";
        if (eastBoundary == 42)
            txt += "\nConvection coefficient on the right: " + String.valueOf(hEast) + " W/(m^2K)";
        if (eastBoundary == 22)
            txt += "\nHeat flow on the right: " + String.valueOf(qEast) + " W/(m^2K)";
        txt += "\n\nThermal control elements:";
        // for el in TCEs:
        // txt += "\n\nTCE: " + String.valueOf(el.name);
        // el.print_attributes(f);
        return txt;
    }

    // This is a temporary method that needs to be modified in the future
    public void calculateHeatFluxes() {
        fluxes.clear();
        ControlVolume cv1 = cvs.get(0);
        ControlVolume cv1r = cvs.get(1);
        ControlVolume cv2 = cvs.get(cvs.size() - 1);
        ControlVolume cv2l = cvs.get(cvs.size() - 2);
        double Tw = cv1.temperature + 2 * ((cv1.temperature - cv1r.temperature) / (cv1.dx + cv1r.dx)) * (0.5 * cv1.dx);
        double Te = cv2.temperature - 2 * ((cv2l.temperature - cv2.temperature) / (cv2.dx + cv2l.dx)) * (0.5 * cv2.dx);
        for (int i = 0; i < cvs.size() - 1; i++) {
            ControlVolume cv = cvs.get(i);
            ControlVolume cvR = cvs.get(i).eastNeighbour;
            fluxes.add((double) Math.round(2 * cv.kEastFace * (cv.temperature - cvR.temperature) / (cv.dx + cvR.dx)));
        }
    }
}
