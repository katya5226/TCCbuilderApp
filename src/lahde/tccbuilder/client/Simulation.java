package lahde.tccbuilder.client;

import java.util.ArrayList;

public abstract class Simulation {
    BorderCondition westBoundary;
    BorderCondition eastBoundary;
    double hWest;
    double hEast;
    double tempWest;
    double tempEast;
    double qWest;
    double qEast;
    double startTemp;
    double ambientTemperature;
    ArrayList<Double> times;
    ArrayList<Double[]> temperatures;
    double dt;
    double totalTime;
    double time;
    double minTemp;
    double maxTemp;
    boolean customTempRange;

    public static enum BorderCondition {
        ADIABATIC,
        CONSTANT_HEAT_FLUX,
        CONSTANT_TEMPERATURE,
        CONVECTIVE
    }
    public static enum Property {
        DENSITY,
        SPECIFIC_HEAT_CAPACITY,
        THERMAL_CONDUCTIVITY,
        EMISSIVITY
    }
    abstract void makeTCC();
    abstract void heatTransferStep();
    abstract  void resetHeatSim();
    abstract  void setTemperatureRange();
    abstract String dump();
    abstract void undump(StringTokenizer stringTokenizer);
    abstract String getReport();
    abstract String printTCEs();
}
