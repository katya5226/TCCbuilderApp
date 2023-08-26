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

    public enum BorderCondition {
        ADIABATIC,
        CONSTANT_HEAT_FLUX,
        CONSTANT_TEMPERATURE,
        CONVECTIVE
    }
    abstract void makeTCC();
    abstract void heatTransferStep();
    abstract  void resetHeatSim();
    abstract  void setTemperatureRange();
}
