package lahde.tccbuilder.client;


import com.google.gwt.core.client.GWT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Vector;

public class Simulation1D extends Simulation {
    Vector<ThermalControlElement> simTCEs;
    boolean cyclic;
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
    TCC heatCircuit;
    double[] start_temperatures;


    public Simulation1D() {
        simTCEs = new Vector<ThermalControlElement>();
        westBoundary = BorderCondition.CONVECTIVE;
        eastBoundary = BorderCondition.CONVECTIVE;
        hWest = 100000.0;
        hEast = 100000.0;
        tempWest = 291.0;
        tempEast = 290.0;
        qWest = 0.0;
        qEast = 0.0;
        startTemp = 290.0;
        ambientTemperature = 293.0;
        // start_temperatures = new double[num_cvs];
        times = new ArrayList<Double>();
        temperatures = new ArrayList<Double[]>();
        dt = 5e-3;
        totalTime = 0.0;

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

    public void heatTransferStep() {
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
        heatCircuit.hWest = hWest;
        heatCircuit.hEast = hEast;
        heatCircuit.temperatureWest = tempWest;
        heatCircuit.temperatureEast = tempEast;
        heatCircuit.qWest = qWest;
        heatCircuit.qEast = qEast;

        heatCircuit.buildTCC();
        heatCircuit.initializeMatrix();
        int n = heatCircuit.cvs.size();
        double[] temps = new double[n];
        Arrays.fill(temps, startTemp);
        heatCircuit.setTemperatures(temps);

        setTemperatureRange();


        start_temperatures = new double[heatCircuit.cvs.size()];
        numCycleParts = this.cycleParts.size();
        if (!cycleParts.isEmpty()) cyclePart = this.cycleParts.get(0);
        cyclePartTime = 0.0;
        printing_interval = 1;
        totalTime = 1.0;

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
            minTemp = Math.min(startTemp, Math.min(tempWest, tempEast));
            maxTemp = Math.max(startTemp, Math.max(tempWest, tempEast));
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
