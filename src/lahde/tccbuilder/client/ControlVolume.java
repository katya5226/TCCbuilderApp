package lahde.tccbuilder.client;

import com.google.gwt.core.client.GWT;

import java.util.*;

public class ControlVolume {
    public ThermalControlElement parent;
    public Material material;
    public int elementIndex;
    public int TCEIndex;
    public int globalIndex;
    public double temperature;
    public double temperatureOld;
    public ControlVolume westNeighbour;
    public ControlVolume eastNeighbour;
    public double dx;
    public double westResistance;
    public double eastResistance;
    public double kWestFace;
    public double kEastFace;
    public double kWest;
    public double kEast;
    public double qWest;
    public double qEast;
    public double hTransv;
    public double qGenerated;
    public double constQgen;  // W/m3 !!!
    public double constRho;
    public double constCp;
    public double constK;
    public double eps;
    public int mode;


    public ControlVolume(int index) {
        elementIndex = index;
        TCEIndex = 0;
        globalIndex = 0;
        temperature = 0.0;
        temperatureOld = 0.0;
        westNeighbour = null;
        eastNeighbour = null;
        dx = 0.001;
        westResistance = 0.0;
        eastResistance = 0.0;
        kWestFace = 0.0;
        kEastFace = 0.0;
        kWest = 0.0;
        kEast = 0.0;
        qWest = 0.0;
        qEast = 0.0;
        qGenerated = 0.0;
        hTransv = 0.0;
        constRho = -1;
        constCp = -1;
        constK = -1;
        constQgen = 0.0;
        eps = 1.0;
        mode = 0;

        material = parent.sim.materialHashMap.get("100001-Inox");
        if (!material.isLoaded()) {
            material.readFiles();
        }
    }

    double rho() {
        //GWT.log("Calculating rho");
        double rho = 0.0;
        if (constRho != -1)
            rho = constRho;
        else {
            // rho = ModelMethods.linInterp(temperature, material.interpTemps, material.rho);
            rho = material.rho.get((int) Math.round(temperature * 10));
        }
        if (parent instanceof RegulatorElm) {
            RegulatorElm r = (RegulatorElm) parent;
            rho = r.rhoValues.get((int) Math.round(temperature * 10));
        }
        return rho;
    }

    double cp() {
        double cp = 0.0;
        int fI = 0;
        //GWT.log("Calculating cp");
        if (constCp != -1) {
            cp = constCp;
        } else {
            if (parent.field) {
                fI = parent.fieldIndex;
            }
            if (material.cpThysteresis) {
                if (mode == 1)
                    // cp = ModelMethods.linInterp(temperature, material.interpTemps, material.cpHeating.get(fI));
                    cp = material.cpHeating.get(fI).get((int) Math.round(temperature * 10));
                else if (mode == -1)
                    // cp = ModelMethods.linInterp(temperature, material.interpTemps, material.cpCooling.get(fI));
                    cp = material.cpCooling.get(fI).get((int) Math.round(temperature * 10));
            } else {
                // cp = ModelMethods.linInterp(temperature, material.interpTemps, material.cp.get(fI));
                cp = material.cp.get(fI).get((int) Math.round(temperature * 10));
            }
        }
        if (parent instanceof RegulatorElm) {
            RegulatorElm r = (RegulatorElm) parent;
            cp = r.cpCurve.get((int) Math.round(temperature * 10));
        }
        return cp;
    }

    double k() {
        //TODO: update this method for kThysteresis
        //GWT.log("Calculating k");
        double k = 0.01;
        if (constK != -1)
            k = constK;
        else {
            // k = ModelMethods.linInterp(temperature, material.interpTemps, material.k.get(0));
            k = material.k.get(0).get((int) Math.round(temperature * 10));
        }
        if (parent instanceof RegulatorElm) {
            RegulatorElm r = (RegulatorElm) parent;
            k = r.kValues.get((int) Math.round(temperature * 10));
        }
        return k;
    }

    double qGen() {
        double q = 0.0;
        if (constQgen != -1)
            q = constQgen * dx;
        else {
            q = parent.sim.simulation1D.time * 0.0001 + temperature * 100000;
        }
        return q;
    }

    public double getProperty(Simulation.Property property) {
        switch (property) {
            case DENSITY:
                return rho();
            case SPECIFIC_HEAT_CAPACITY:
                return cp();
            case THERMAL_CONDUCTIVITY:
                return k();
            default:
                return 0.0;
        }
    }

    public void setProperty(Simulation.Property property, double value) {
        switch(property) {
            case DENSITY:
                constRho = value;
                break;
            case SPECIFIC_HEAT_CAPACITY:
                constCp = value;
                break;
            case THERMAL_CONDUCTIVITY:
                constK = value;
                break;
        }
    }

    void calculateKWestFace() {
        kWestFace = westNeighbour.k() * k() * (westNeighbour.dx + dx) /
                (westNeighbour.k() * dx + k() * westNeighbour.dx +
                        westNeighbour.k() * k() * westResistance);
    }

    void calculateKEastFace() {
        kEastFace = k() * eastNeighbour.k() * (dx + eastNeighbour.dx) /
                (k() * eastNeighbour.dx + eastNeighbour.k() * dx +
                        eastNeighbour.k() * k() * eastResistance);
    }

    void calculateKWest() {
        kWest = 2 * kWestFace * dx / (westNeighbour.dx + dx);
    }

    void calculateKEast() {
        kEast = 2 * kEastFace * dx / (eastNeighbour.dx + dx);
    }

    void calculateConductivities() {
        calculateKWestFace();
        calculateKEastFace();
        calculateKWest();
        calculateKEast();
    }

    public void magnetize() {
        // TODO: inform user

        Vector<Double> dTheatcool = new Vector<>();
        double dT = 0.0;
        double T = 0.0;
        int fieldIndex = parent.fieldIndex - 1;
        GWT.log("Field = " + material.fields.get(parent.fieldIndex));
        if (fieldIndex < 0) return;

        if (!parent.field) {
            dTheatcool = material.dTheating.get(fieldIndex);
            dT = dTheatcool.get((int) ((temperature * 10) + 0.5));
            T = temperature + dT;
        } else {
            dTheatcool = material.dTcooling.get(fieldIndex);
            dT = dTheatcool.get((int) ((temperature * 10) + 0.5));
            T = temperature - dT;
        }
        GWT.log("Field = " + material.fields.get(parent.fieldIndex));
        GWT.log("Temperature = " + temperature);
        GWT.log("dT = " + (parent.field ? "-" : "") + dT);
        temperature = T;
        temperatureOld = T;
    }

    
    public void ePolarize() {
        // TODO: inform user

        double dT = 0.0;
        double T = 0.0;
        int fieldIndex = parent.fieldIndex - 1;
        GWT.log("Field = " + material.fields.get(parent.fieldIndex));
        if (fieldIndex < 0) return;

        Vector<Double> dTvec = material.dT.get(fieldIndex);
        dT = dTvec.get((int) ((temperature * 10) + 0.5));

        if (!parent.field)
            T = temperature + dT;
        else
            T = temperature - dT;

        GWT.log("Field = " + material.fields.get(parent.fieldIndex));
        GWT.log("Temperature = " + temperature);
        GWT.log("dT = " + (parent.field ? "-" : "") + dT);
        temperature = T;
        temperatureOld = T;
    }

}
