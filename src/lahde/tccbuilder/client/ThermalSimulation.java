package lahde.tccbuilder.client;


import com.google.gwt.core.client.GWT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Vector;

public class ThermalSimulation {
    Vector<ThermalControlElement> simTCEs;
    BorderCondition left_boundary;
    BorderCondition right_boundary;
    double h_left;
    double h_right;
    double temp_left;
    double temp_right;
    double qIn;
    double qOut;
    double startTemp;
    double ambient_temperature;
    ArrayList<Double> times;
    ArrayList<Double[]> temperatures;
    int multipl;
    int tt;
    double cpart_t;
    double dt;
    double total_time;
    boolean reach_steady;
    boolean cyclic;
    double time;
    int cycle;
    int printing_interval;
    Vector<CyclePart> cycleParts;
    double cyclePartTime;
    public CyclePart cyclePart;
    int cyclePartIndex;
    public int numCycleParts;

    public int ud;
    Vector<Double> x_prev;
    Vector<Double> x_mod;
    double minTemp, maxTemp;
    TCC heatCircuit;
    double[] start_temperatures;

    public enum BorderCondition {
        ADIABATIC,
        CONSTANT_HEAT_FLUX,
        CONSTANT_TEMPERATURE,
        CONVECTIVE
    }

    public ThermalSimulation() {
        simTCEs = new Vector<ThermalControlElement>();
        left_boundary = BorderCondition.CONVECTIVE;
        right_boundary = BorderCondition.CONVECTIVE;
        h_left = 100000.0;
        h_right = 100000.0;
        temp_left = 291.0;
        temp_right = 290.0;
        qIn = 0.0;
        qOut = 0.0;
        startTemp = 290.0;
        ambient_temperature = 293.0;
        // start_temperatures = new double[num_cvs];
        times = new ArrayList<Double>();
        temperatures = new ArrayList<Double[]>();
        multipl = 1000;
        tt = 0;
        cpart_t = 0.0;
        dt = 5e-3;
        total_time = 0.0;
        reach_steady = false;
        cyclic = false;
        time = 0.0;
        cycle = 0;
        printing_interval = 1;
        cycleParts = new Vector<CyclePart>();
        cyclePartTime = 0.0;
        cyclePartIndex = 0;

        ud = 0;
        x_prev = new Vector<Double>();
        x_mod = new Vector<Double>();


    }

    void setHeatSim() {
        // underdiag = new double[heatCircuit.cvs.size()];
        // diag = new double[heatCircuit.cvs.size()];
        // upperdiag = new double[heatCircuit.cvs.size()];
        // rhs = new double[heatCircuit.cvs.size()];
        start_temperatures = new double[heatCircuit.cvs.size()];
        numCycleParts = this.cycleParts.size();
        if (!cycleParts.isEmpty()) cyclePart = this.cycleParts.get(0);
        cyclePartTime = 0.0;
        printing_interval = 1;
        total_time = 1.0;
        reach_steady = false;
    }

    void resetHeatSim() {
        times.clear();
        temperatures.clear();
        numCycleParts = 0;
        cyclic = false;
        time = 0.0;
        cycleParts.clear();
    }

    public void append_new_temps() {
        Double[] ttemps = new Double[heatCircuit.cvs.size()];
        for (int i = 0; i < heatCircuit.cvs.size(); i++) {
            ttemps[i] = heatCircuit.cvs.get(i).temperature;
        }
        this.temperatures.add(ttemps);
        this.times.add(this.time);
    }

    public void heat_transfer_step() {
        x_mod.clear();
        x_prev.clear();

        for (int i = 0; i < heatCircuit.cvs.size(); i++) {
            x_prev.add(heatCircuit.cvs.get(i).temperatureOld);
            x_mod.add(heatCircuit.cvs.get(i).temperatureOld);
        }
        //while (true) {
        for (int k = 0; k < 3; k++) {
            // heatCircuit.neighbours()
            heatCircuit.calculateConductivities();

            heatCircuit.makeMatrix(dt);
            ModelMethods.tdmaSolve(heatCircuit.cvs, heatCircuit.underdiag, heatCircuit.diag, heatCircuit.upperdiag, heatCircuit.rhs);
            for (int i = 0; i < heatCircuit.cvs.size(); i++) {
                x_mod.set(i, heatCircuit.cvs.get(i).temperature);
            }
            // flag = hf.compare(x_mod, x_prev, pa.tolerance)
            boolean flag = true;
            for (int i = 0; i < heatCircuit.cvs.size(); i++) {
                x_prev.set(i, x_mod.get(i));
            }
            for (int i = 0; i < simTCEs.size(); i++) {
                if (simTCEs.get(i).cvs.get(0).material.cpThysteresis == true)  // TODO-material
                    simTCEs.get(i).updateModes();
            }
            // if (flag) {
            //     break;
            // }
        }

        heatCircuit.calculateConductivities();
        heatCircuit.makeMatrix(this.dt);
        ModelMethods.tdmaSolve(heatCircuit.cvs, heatCircuit.underdiag, heatCircuit.diag, heatCircuit.upperdiag, heatCircuit.rhs);
        // if len(this.PCMs) > 0:
        // for i in range(0, len(this.PCMs)):
        // this.PCMs[i].update_temperatures()
        // this.PCMs[i].raise_latent_heat(pa.dt)
        heatCircuit.replaceTemperatures();
        this.append_new_temps();
        // hf.print_darray_row(this.temperatures[-1], this.temperatures_file, 4)
        // ModelMethods.printTemps(this.temperatures.get(this.temperatures.size()-1));
    }

    void makeTCC() {
        // simTCEs.clear();
        // simTCEs.add(new TCE("TCE1", 0, simComponents));
        heatCircuit = new TCC("Heat circuit", simTCEs);
        heatCircuit.westBoundary = 41;
        heatCircuit.eastBoundary = 42;//TODO: change
        heatCircuit.hWest = h_left;
        heatCircuit.hEast = h_right;
        heatCircuit.temperatureWest = temp_left;
        heatCircuit.temperatureEast = temp_right;
        heatCircuit.qWest = qIn;
        heatCircuit.qEast = qOut;

        heatCircuit.buildTCC();
        heatCircuit.initializeMatrix();
        int n = heatCircuit.cvs.size();
        double[] temps = new double[n];
        Arrays.fill(temps, startTemp);
        heatCircuit.setTemperatures(temps);

        setTemperatureRange();

        GWT.log("NUMCVS: " + String.valueOf(heatCircuit.cvs.size()));
        for (ControlVolume cv : heatCircuit.cvs) {
            GWT.log("cvInd: " + String.valueOf(cv.globalIndex));
        }
    }

    void setTemperatureRange() {
        double maxValue = 0;
        for (ThermalControlElement c : simTCEs) {
            if (c.cvs.get(0).material.magnetocaloric) {  // TODO - material
                for (Vector<Double> dTcoolingVector : c.cvs.get(0).material.dTcooling) {
                    maxValue = Math.max(maxValue, Collections.max(dTcoolingVector));
                }

                for (Vector<Double> dTheatingVector : c.cvs.get(0).material.dTheating) {
                    maxValue = Math.max(maxValue, Collections.max(dTheatingVector));
                }
            }
        }
        if (maxValue == 0) {
            minTemp = Math.min(startTemp, Math.min(temp_left, temp_right));
            maxTemp = Math.max(startTemp, Math.max(temp_left, temp_right));
        } else {
            minTemp = startTemp - maxValue;
            maxTemp = startTemp + maxValue;
        }
    }

    String printTCEs() {
        String tces = "";
        for (ThermalControlElement tce : simTCEs) {
            tces += tce.index + (tce == simTCEs.get(simTCEs.size() - 1) ? "" : ", ");
        }
        return tces;
    }
}
