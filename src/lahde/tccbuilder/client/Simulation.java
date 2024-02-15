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
    double westFrequency;
    double eastFrequency;
    double westAmplitude;
    double eastAmplitude;
    double startTemp;
    double ambientTemperature;
    ArrayList<Double> times;
    ArrayList<Double[]> temperatures;
    ArrayList<ArrayList<Double[]>> TEpowerOutputs;  // Double arraylist because there may be more than one TE engine in a curcuit, each has two power values.
    double dt;
    double totalTime;
    double time;
    double minTemp;
    double maxTemp;
    boolean customTempRange;
    int outputInterval;

    public static enum BorderCondition {
        ADIABATIC,
        CONSTANT_HEAT_FLUX,
        CONSTANT_TEMPERATURE,
        CONVECTIVE,
        PERIODIC
    }
    public static enum Property {
        DENSITY,
        SPECIFIC_HEAT_CAPACITY,
        THERMAL_CONDUCTIVITY,
        EMISSIVITY
    }
    
    public static int propToInt(Simulation.Property p) {
        switch(p) {
            case DENSITY:
                return 0;
            case SPECIFIC_HEAT_CAPACITY:
                return 1;
            case THERMAL_CONDUCTIVITY:
                return 2;
            case EMISSIVITY:
                return 3;
            default:
                return -1;
        }
    }

    public static Simulation.Property intToProp(int i) {
        switch(i) {
            case 0:
                return Property.DENSITY;
            case 1:
                return Property.SPECIFIC_HEAT_CAPACITY;
            case 2:
                return Property.THERMAL_CONDUCTIVITY;
            case 3:
                return Property.EMISSIVITY;
            default:
                return Property.DENSITY;
        }
    }

    public static String propUnit(Simulation.Property p) {
        switch(p) {
            case DENSITY:
                return "kg/m³";
            case SPECIFIC_HEAT_CAPACITY:
                return "J/(kgK)";
            case THERMAL_CONDUCTIVITY:
                return "W/(mK)";
            case EMISSIVITY:
                return "";
            default:
                return "";
        }
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
