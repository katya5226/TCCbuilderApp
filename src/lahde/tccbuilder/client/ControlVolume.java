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
    public double qGenerated;
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
        constRho = -1;
        constCp = -1;
        constK = -1;
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
            rho = ModelMethods.linInterp(temperature, material.interpTemps, material.rho);
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
                    cp = ModelMethods.linInterp(temperature, material.interpTemps, material.cpHeating.get(fI));
                else if (mode == -1)
                    cp = ModelMethods.linInterp(temperature, material.interpTemps, material.cpCooling.get(fI));
            } else {
                cp = ModelMethods.linInterp(temperature, material.interpTemps, material.cp.get(fI));
            }
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
            k = ModelMethods.linInterp(temperature, material.interpTemps, material.k.get(0));
        }
        return k;
    }

    void calculateKWestFace() {
        kWestFace = westNeighbour.k() * k() * (westNeighbour.dx + dx) /
                (westNeighbour.k() * dx + k() * westNeighbour.dx +
                        westNeighbour.k() * k() * westResistance * (dx + westNeighbour.dx) / 2);
    }

    void calculateKEastFace() {
        kEastFace = k() * eastNeighbour.k() * (dx + eastNeighbour.dx) /
                (k() * eastNeighbour.dx + eastNeighbour.k() * dx +
                        eastNeighbour.k() * k() * eastResistance * (eastNeighbour.dx + dx) / 2);
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

        Vector<Double> dTheatcool = new Vector<Double>();
        double dT = 0.0;
        double T = 0.0;
        ThermalControlElement tce = (ThermalControlElement) parent;
        if (!tce.field) {
            dTheatcool = material.dTheating.get(tce.fieldIndex - 1);
            dT = ModelMethods.linInterp(temperature, material.interpTemps, dTheatcool);
            T = temperature + dT;
            temperature = T;
            temperatureOld = T;
            // GWT.log("Field = " + String.valueOf(tce.field));
            // GWT.log("dT = " + String.valueOf(dT));
        }
        if (tce.field) {
            dTheatcool = material.dTcooling.get(tce.fieldIndex - 1);
            dT = ModelMethods.linInterp(temperature, material.interpTemps, dTheatcool);
            T = temperature - dT;
            GWT.log("Field = " + String.valueOf(tce.field));
            GWT.log("dT = -" + String.valueOf(dT));
            temperature = T;
            temperatureOld = T;
        }
    }

}
