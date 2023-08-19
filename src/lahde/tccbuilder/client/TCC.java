package lahde.tccbuilder.client;

import java.lang.Math;
import java.util.Vector;

public class TCC {
    public String name;
    public Vector<TCE> TCEs;
    public int numTCEs;
    public int numCvs;
    public Vector<ControlVolume> controlVolumes;
    public TCC westNeighbour;
    public TCC eastNeighbour;
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

    public TCC(String name, Vector<TCE> TCEs) {
        //parent_sim = null;
        this.name = name;
        this.TCEs = TCEs;
        numTCEs = TCEs.size();
        numCvs = 0;
        for (TCE tce : TCEs) {
            numCvs += tce.numCvs;
        }
        controlVolumes = new Vector<ControlVolume>();
        westNeighbour = null;
        eastNeighbour = null;
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
        //for (int i = 0; i < num_cvs; i++) {
        for (ControlVolume cv : controlVolumes) {
            int i = cv.globalIndex;
            if (i != 0 && i != numCvs - 1) {
                cv.westNeighbour = controlVolumes.get(i - 1);
                cv.eastNeighbour = controlVolumes.get(i + 1);
            }
            if (i == 0) {
                cv.eastNeighbour = controlVolumes.get(1);
            }
            if (i == numCvs - 1) {
                cv.westNeighbour = controlVolumes.get(numCvs - 2);
            }
            //Window.alert(String.valueOf(cv.left_neighbour.global_index) + "\t" + String.valueOf(cv.right_neighbour.global_index));
        }
        //}
    }

    //TODO: add condition to check if TCC has <3 control volumes 
    public void buildTCC() {  // DOPOLNITI!
        //Arrays.sort(TCEs);
        int n1 = numTCEs;
        int m2 = 0;
        TCEs.get(0).westNeighbour = null;
        TCEs.get(n1 - 1).eastNeighbour = null;
        if (TCEs.size() == 1) {
            TCEs.get(0).eastNeighbour = null;
            TCEs.get(n1 - 1).westNeighbour = null;
        } else {
            int m1 = TCEs.get(n1 - 1).numComponents;
            TCEs.get(0).eastNeighbour = TCEs.get(1);
            TCEs.get(n1 - 1).westNeighbour = TCEs.get(n1 - 2);
            TCEs.get(0).components.get(0).westNeighbour = null;
            TCEs.get(n1 - 1).components.get(m1 - 1).eastNeighbour = null;
        }
        for (int i = 1; i < numTCEs - 1; i++) {
            TCEs.get(i).eastNeighbour = TCEs.get(i + 1);
            TCEs.get(i).westNeighbour = TCEs.get(i - 1);
            m2 = TCEs.get(i).numComponents;
            TCEs.get(i).components.get(m2 - 1).eastNeighbour = TCEs.get(i + 1).components.get(0);
            TCEs.get(i + 1).westNeighbour = TCEs.get(i);
            TCEs.get(i + 1).components.get(0).westNeighbour = TCEs.get(i).components.get(TCEs.get(i).numComponents - 1);
        }

        for (int i = 0; i < numTCEs; i++) {
            TCEs.get(i).buildTCE();
        }
        controlVolumes.clear();
        int global_index = 0;
        for (int i = 0; i < numTCEs; i++) {
            TCEs.get(i).parentCircuit = this;
            for (int j = 0; j < TCEs.get(i).numCvs; j++) {
                TCEs.get(i).controlVolumes.get(j).globalIndex = global_index;
                controlVolumes.add(TCEs.get(i).controlVolumes.get(j));
                global_index++;
            }
        }
        setNeighbours();
    }

    public void calculateConductivities() {
        for (ControlVolume cv : controlVolumes) {
            cv.calculateConductivities();
        }
    }

    public void setTemperatures(double[] temps) {
        for (int i = 0; i < numCvs; i++) {
            controlVolumes.get(i).temperature = temps[i];
            controlVolumes.get(i).temperatureOld = temps[i];
        }
    }

    public void replaceTemperatures() {
        for (ControlVolume cv : controlVolumes)
            cv.temperatureOld = cv.temperature;
    }


    public void initializeMatrix() {
        for (int i = 0; i < numCvs; i++) {
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
        String bcleft = ModelMethods.return_bc_name(westBoundary);
        String bcright = ModelMethods.return_bc_name(eastBoundary);
        txt += "\nBoundary condition on the left: " + bcleft;
        txt += "\nBoundary condition on the right: " + bcright;
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
        ControlVolume cv1 = controlVolumes.get(0);
        ControlVolume cv1r = controlVolumes.get(1);
        ControlVolume cv2 = controlVolumes.get(controlVolumes.size() - 1);
        ControlVolume cv2l = controlVolumes.get(controlVolumes.size() - 2);
        double Tw = cv1.temperature + 2 * ((cv1.temperature - cv1r.temperature) / (cv1.dx + cv1r.dx)) * (0.5 * cv1.dx);
        double Te = cv2.temperature - 2 * ((cv2l.temperature - cv2.temperature) / (cv2.dx + cv2l.dx)) * (0.5 * cv2.dx);
        for (int i = 0; i < numCvs - 1; i++) {
            ControlVolume cv = controlVolumes.get(i);
            ControlVolume cvR = controlVolumes.get(i).eastNeighbour;
            fluxes.add((double) Math.round(2 * cv.kEastFace * (cv.temperature - cvR.temperature) / (cv.dx + cvR.dx)));
        }
    }
}
