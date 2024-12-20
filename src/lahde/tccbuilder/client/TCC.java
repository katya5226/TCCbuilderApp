package lahde.tccbuilder.client;

import java.beans.Transient;
import java.lang.Math;
import java.util.Vector;
import java.util.Collections;


public class TCC {
    public Simulation1D sim;
    public String name;
    public Vector<ThermalControlElement> TCEs;
    public Vector<ControlVolume> cvs;
    public double[] underdiag;
    public double[] diag;
    public double[] upperdiag;
    public double[] rhs;
    public Simulation.BorderCondition westBoundary;
    public Simulation.BorderCondition eastBoundary;
    //public int[) other_ht_types;
    public double qWest, qEast;
    public double temperatureWest, temperatureEast, ambientTemperature;
    public double hWest, hEast;
    public double amplitudeWest, amplitudeEast;
    public double frequencyWest, frequencyEast;
    boolean containsTEEngines;

    public Vector<Double> fluxes;

    public TCC(Simulation1D sim, String name, Vector<ThermalControlElement> TCEs) {
        this.sim = sim;
        this.name = name;
        this.TCEs = TCEs;
        int numCvs = 0;
        for (ThermalControlElement tce : TCEs) {
            numCvs += tce.cvs.size();
            tce.parent = this;
        }
        cvs = new Vector<ControlVolume>();
        underdiag = new double[numCvs];
        diag = new double[numCvs];
        upperdiag = new double[numCvs];
        rhs = new double[numCvs];
        westBoundary = Simulation.BorderCondition.CONVECTIVE;
        eastBoundary = Simulation.BorderCondition.CONVECTIVE;
        //other_ht_types = [);
        qWest = 0.0;
        qEast = 0.0;
        temperatureWest = 0.0;
        temperatureEast = 0.0;
        ambientTemperature = 0.0;
        hWest = 500.0;
        hEast = 500.0;

        fluxes = new Vector<Double>();

        containsTEEngines = false;
        for (ThermalControlElement tce : TCEs) {
            if (tce instanceof TEHeatEngine) {
                containsTEEngines = true;
            }
        }
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
        // for (ThermalControlElement tce : TCEs) {
        //     tce.buildThermalControlElement();
        // }
        Collections.sort(TCEs);
        // TCEs.get(0).westBoundary = westBoundary;
        // TCEs.get(TCEs.size() - 1).eastBoundary = eastBoundary;
        for (int i = 0; i < TCEs.size() - 1; i++) {
            TCEs.get(i).eastBoundary = Simulation.BorderCondition.CONVECTIVE;
            TCEs.get(i).eastNeighbour = TCEs.get(i + 1);
            TCEs.get(i + 1).westNeighbour = TCEs.get(i);
        }
        for (int i = 1; i < TCEs.size(); i++) {
            TCEs.get(i).westBoundary = Simulation.BorderCondition.CONVECTIVE;
            int l = TCEs.get(i - 1).cvs.size();
            TCEs.get(i - 1).cvs.get(l - 1).eastNeighbour = TCEs.get(i).cvs.get(0);
            TCEs.get(i).cvs.get(0).westNeighbour = TCEs.get(i - 1).cvs.get(l - 1);
        }
        for (int i = 0; i < TCEs.size(); i++) {
            TCEs.get(i).cvs.get(0).westResistance = TCEs.get(i).westResistance;
            TCEs.get(i).cvs.get(TCEs.get(i).cvs.size() - 1).eastResistance = TCEs.get(i).eastResistance;
            TCEs.get(i).set_dx(TCEs.get(i).length / TCEs.get(i).cvs.size());
        }
        cvs.clear();
        for (int i = 0; i < TCEs.size(); i++) {
            for (int j = 0; j < TCEs.get(i).cvs.size(); j++) {
                cvs.add(TCEs.get(i).cvs.get(j));
            }
        }
        setGlobalIndeces();
        setNeighbours();
    }

    public void setGlobalIndeces() {
        int globalIndex = 0;
        for (int i = 0; i < TCEs.size(); i++) {
            for (int j = 0; j < TCEs.get(i).cvs.size(); j++) {
                TCEs.get(i).cvs.get(j).globalIndex = globalIndex;
                globalIndex++;
            }
        }
    }

    public void calculateConductivities() {
        for (ControlVolume cv : cvs) {
            cv.calculateConductivities();
        }
    }

    public void setTemperatures(Vector<Double> temps) {
        for (int i = 0; i < cvs.size(); i++) {
            cvs.get(i).temperature = temps.get(i);
            cvs.get(i).temperatureOld = temps.get(i);
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
        txt += "\nWest boundary condition: " + bcWest;
        txt += "\nEast boundary condition: " + bcEast;
        if (westBoundary == Simulation.BorderCondition.CONSTANT_TEMPERATURE || westBoundary == Simulation.BorderCondition.CONVECTIVE)
            txt += "\nWest temperature: " + String.valueOf(temperatureWest) + " K";
        if (westBoundary == Simulation.BorderCondition.CONVECTIVE)
            txt += "\nWest convection coefficient: " + String.valueOf(hWest) + " W/(m^2K)";
        if (westBoundary == Simulation.BorderCondition.CONSTANT_HEAT_FLUX)
            txt += "\nHeat flux at west boundary: " + String.valueOf(qWest) + " W/(m^2K)";
        if (eastBoundary == Simulation.BorderCondition.CONSTANT_TEMPERATURE || eastBoundary == Simulation.BorderCondition.CONVECTIVE)
            txt += "\nEast temperature: " + String.valueOf(temperatureEast) + " K";
        if (eastBoundary == Simulation.BorderCondition.CONVECTIVE)
            txt += "\nEast convection coefficient: " + String.valueOf(hEast) + " W/(m^2K)";
        if (eastBoundary == Simulation.BorderCondition.CONSTANT_HEAT_FLUX)
            txt += "\nHeat flux at east boundary: " + String.valueOf(qEast) + " W/(m^2K)";
        txt += "\n\nThermal control elements:";
        // for el in TCEs:
        // txt += "\n\nTCE: " + String.valueOf(el.name);
        // el.print_attributes(f);
        return txt;
    }


    public void calculateHeatFluxes() {
        fluxes.clear();
        switch(westBoundary) {
            case ADIABATIC:
                fluxes.add(0.0);
                break;
            case CONSTANT_HEAT_FLUX:
                fluxes.add(qWest);
                break;
            case CONSTANT_TEMPERATURE:
                fluxes.add((double) Math.round(2 * cvs.get(0).k() * (temperatureWest - cvs.get(0).temperature) / cvs.get(0).dx ));
                break;
            case CONVECTIVE:
                ControlVolume cv1 = cvs.get(0);
                ControlVolume cv1r = cvs.get(1);
                double al = (2 * cv1.dx + cv1r.dx) / (cv1.dx + cv1r.dx);
                double bl = cv1.dx / (cv1.dx + cv1r.dx);
                double Tw = al * cv1.temperature - bl * cv1r.temperature;
                fluxes.add((double) Math.round((temperatureWest - Tw) * hWest));
                break;
            // case PERIODIC:
            //     double wT = temperatureWest + amplitudeWest * Math.sin(frequencyWest * sim.time);
            //     fluxes.add((double) Math.round(2 * cvs.get(0).k() * (wT - cvs.get(0).temperature) / cvs.get(0).dx ));
            //     break;
        }

        for (int i = 0; i < cvs.size() - 1; i++) {
            ControlVolume cv = cvs.get(i);
            ControlVolume cvR = cvs.get(i).eastNeighbour;
            fluxes.add((double) Math.round(2 * cv.kEastFace * (cv.temperature - cvR.temperature) / (cv.dx + cvR.dx)));
        }

        switch(eastBoundary) {
            case ADIABATIC:
                fluxes.add(0.0);
                break;
            case CONSTANT_HEAT_FLUX:
                fluxes.add(qEast);
                break;
            case CONSTANT_TEMPERATURE:
                fluxes.add((double) Math.round(2 * cvs.get(cvs.size() - 1).k() * (cvs.get(cvs.size() - 1).temperature - temperatureEast) / cvs.get(cvs.size() - 1).dx));
                break;
            case CONVECTIVE:
                ControlVolume cv2 = cvs.get(cvs.size() - 1);
                ControlVolume cv2l = cvs.get(cvs.size() - 2);
                double ar = (2 * cv2.dx + cv2l.dx) / (cv2.dx + cv2l.dx);
                double br = cv2.dx / (cv2.dx + cv2l.dx);
                double Te = ar * cv2.temperature - br * cv2l.temperature;
                fluxes.add((double) Math.round((Te - temperatureEast) * hEast));
                break;
            // case PERIODIC:
            //     double eT = temperatureEast + amplitudeEast * Math.sin(frequencyEast * sim.time);
            //     fluxes.add((double) Math.round(2 * cvs.get(cvs.size() - 1).k() * (cvs.get(cvs.size() - 1).temperature - eT) / cvs.get(cvs.size() - 1).dx));
            //     break;
        }

    }
}
