package lahde.tccbuilder.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;

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
    public double constSeeb;
    public double constElResistivity;
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
        constSeeb = -1;
        constElResistivity = -1;
        constQgen = 0.0;
        eps = 1.0;
        mode = 1;

        material = parent.sim.materialHashMap.get("100001-Inox");
        if (!material.isLoaded()) {
            material.readFiles();
        }
    }

    double rho() {
        double rho = 0.0;
        if (constRho != -1)
            rho = constRho;
        else {
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
        if (constCp != -1) {
            cp = constCp;
        }
        else if (!material.cpFields) {
            if (material.cpThysteresis) {
                if (mode == 1)
                    cp = material.cpHeating.get(0).get((int) Math.round(temperature * 10));
                else if (mode == -1)
                    cp = material.cpCooling.get(0).get((int) Math.round(temperature * 10));                
            } else {
                cp = material.cp.get(0).get((int) Math.round(temperature * 10));
            }
        } 
        else {
            if (parent.field) {
                fI = parent.fieldIndex;
            }
            if (material.cpThysteresis) {
                if (mode == 1)
                    cp = material.cpHeating.get(fI).get((int) Math.round(temperature * 10));
                else if (mode == -1)
                    cp = material.cpCooling.get(fI).get((int) Math.round(temperature * 10));
            } else {
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
        double k = 1;
        if (constK != -1)
            k = constK;
        else {
            if(material.kThysteresis) {
                if (mode == 1)
                    k = material.kHeating.get(0).get((int) Math.round(temperature * 10));
                else if (mode == -1)
                    k = material.kCooling.get(0).get((int) Math.round(temperature * 10));
            } else {
                k = material.k.get(0).get((int) Math.round(temperature * 10));
            }
        }
        if (parent instanceof RegulatorElm) {
            RegulatorElm r = (RegulatorElm) parent;
            k = r.kValues.get((int) Math.round(temperature * 10));
        }
        if (parent instanceof DiodeElm_T_01) {
            DiodeElm_T_01 diode = (DiodeElm_T_01) parent;
            double Twest, Teast;
            if (westNeighbour != null && westNeighbour != this)
                Twest = westNeighbour.temperature;
            else {
                Twest = parent.sim.simulation1D.heatCircuit.temperatureWest;
                if (parent.sim.simulation1D.heatCircuit.westBoundary == Simulation.BorderCondition.PERIODIC)
                    Twest = parent.sim.simulation1D.heatCircuit.temperatureWest +
                    parent.sim.simulation1D.heatCircuit.amplitudeWest * Math.sin(parent.sim.simulation1D.heatCircuit.frequencyWest * parent.sim.simulation1D.time);
                }
            if (westNeighbour != null && westNeighbour != this)
                Teast = eastNeighbour.temperature;
            else {
                Teast = parent.sim.simulation1D.heatCircuit.temperatureEast;
                if (parent.sim.simulation1D.heatCircuit.eastBoundary == Simulation.BorderCondition.PERIODIC)
                    Teast = parent.sim.simulation1D.heatCircuit.temperatureEast +
                    parent.sim.simulation1D.heatCircuit.amplitudeEast * Math.sin(parent.sim.simulation1D.heatCircuit.frequencyEast * parent.sim.simulation1D.time);
            }
            if (diode.direction == CircuitElm.Direction.RIGHT)
                k = diode.k0 * (1 + 2 * (diode.beta / Math.PI) * Math.atan(diode.gamma * (Twest - Teast)));
            if (diode.direction == CircuitElm.Direction.LEFT)
                k = diode.k0 * (1 + 2 * (diode.beta / Math.PI) * Math.atan(diode.gamma * (Teast - Twest)));
        }
        return k;
    }

    double seeb(double T) {
        double seeb = 1;
        if (constSeeb != -1)
            seeb = constSeeb * 1.0e-6;
        else {
            seeb = material.seebeckCoeff.get((int) Math.round(T * 10));
        }
        return seeb;
    }

    double seebeckGradient() {
        double seebGrad = 0;
        if (constSeeb != -1)
            seebGrad = 0;
        else {
            // Window.alert("Seebeck coefficient not constant, check that the material has data on temperature dependence of Seebeck coefficient.");
            seebGrad = material.dSeebeckdT.get((int) Math.round(temperature * 10));
        }
        return seebGrad;
    }

    double elResistivity() {
        double elRes = 0;
        if (constElResistivity != -1)
            elRes = constElResistivity * 1.0e-6;
        else {
            elRes = material.elRes.get((int) Math.round(temperature * 10));
        }
        return elRes;
    }

    double qGen() {
        double q = 0.0;
        if (constQgen != -1)
            q = constQgen;
        // else {
        //     q = parent.sim.simulation1D.time * 0.0001 + temperature * 100000;
        // }
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

    public void toggleField() {
        // TODO: inform user
        Vector<Double> dTs = new Vector<>();

        double dT = 0.0;
        double T = 0.0;
        int fieldIndex = parent.fieldIndex - 1;
        // GWT.log("Field = " + material.fields.get(parent.fieldIndex));
        if (fieldIndex < 0) return;

        if (!parent.field) {
            if (!material.dTThysteresis) {
                dTs = material.dTFieldApply.get(fieldIndex);
                dT = dTs.get((int) ((temperature * 10) + 0.5));
                T = temperature + dT;
            }
            else {
                if (mode == 1) {
                    dTs = material.dTFieldApplyHeating.get(fieldIndex);
                    dT = dTs.get((int) ((temperature * 10) + 0.5));
                    T = temperature + dT;
                }
                if (mode == -1) {
                    dTs = material.dTFieldApplyCooling.get(fieldIndex);
                    dT = dTs.get((int) ((temperature * 10) + 0.5));
                    T = temperature + dT;
                }
            }
        } else {
            if (!material.dTThysteresis) {
                dTs = material.dTFieldRemove.get(fieldIndex);
                dT = dTs.get((int) ((temperature * 10) + 0.5));
                T = temperature + dT;
            }
            else {
                if (mode == 1) {
                    dTs = material.dTFieldRemoveHeating.get(fieldIndex);
                    dT = dTs.get((int) ((temperature * 10) + 0.5));
                    T = temperature + dT;
                }
                if (mode == -1) {
                    dTs = material.dTFieldRemoveCooling.get(fieldIndex);
                    dT = dTs.get((int) ((temperature * 10) + 0.5));
                    T = temperature + dT;
                }
            }
        }
        // GWT.log("Field = " + material.fields.get(parent.fieldIndex));
        // GWT.log("Temperature = " + temperature);
        // GWT.log("dT = " + (parent.field ? "-" : "") + dT);
        temperature = T;
        temperatureOld = T;
    }

}
