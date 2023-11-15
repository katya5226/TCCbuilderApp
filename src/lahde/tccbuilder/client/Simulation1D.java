package lahde.tccbuilder.client;


import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;

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
        hWest = 10000.0;
        hEast = 10000.0;
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

        outputInterval = 1;

        ud = 0;
        x_prev = new Vector<Double>();
        x_mod = new Vector<Double>();

        customTempRange = false;

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

    void checkDiodes() {
        for (ThermalControlElement tce : heatCircuit.TCEs) {
            if (tce instanceof DiodeElm) {
                DiodeElm di = (DiodeElm) tce;
                di.checkDirection(heatCircuit.temperatureWest, heatCircuit.temperatureEast);
            }
        }
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
            checkDiodes();
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
        checkDiodes();
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

        for (ThermalControlElement tce : simTCEs) {
            if (tce instanceof RegulatorElm) {
                RegulatorElm reg = (RegulatorElm) tce;
                reg.setThermalProperties();
            }
        }

        heatCircuit = new TCC("Heat circuit", simTCEs);
        heatCircuit.westBoundary = westBoundary;
        heatCircuit.eastBoundary = eastBoundary;
        heatCircuit.hWest = hWest;
        heatCircuit.hEast = hEast;
        heatCircuit.temperatureWest = tempWest;
        heatCircuit.temperatureEast = tempEast;
        heatCircuit.qWest = qWest;
        heatCircuit.qEast = qEast;
        heatCircuit.ambientTemperature = ambientTemperature;

        heatCircuit.buildTCC();
        heatCircuit.initializeMatrix();
        int n = heatCircuit.cvs.size();
        double[] temps = new double[n];

        Arrays.fill(temps, startTemp);
        //heatCircuit.setTemperatures(temps);
        for (ThermalControlElement tce : heatCircuit.TCEs) {
            if (tce.startTemperature == -1) {
                tce.setTemperatures(startTemp);
            }
            else {
                tce.setTemperatures(tce.startTemperature);
            }
            GWT.log(String.valueOf(tce.cvs.get(0).temperature));
        }

        setTemperatureRange();

        start_temperatures = new double[heatCircuit.cvs.size()];
        numCycleParts = this.cycleParts.size();
        if (!cycleParts.isEmpty()) cyclePart = this.cycleParts.get(0);
        cyclePartTime = 0.0;
        printing_interval = 1;
        totalTime = 1.0;
        time = 0.0;
        GWT.log("NUMCVS: " + String.valueOf(heatCircuit.cvs.size()));
        for (ControlVolume cv : heatCircuit.cvs) {
            GWT.log("cvInd: " + String.valueOf(cv.globalIndex));
        }
        time = 0;
    }

    void setTemperatureRange() {
        if (customTempRange == true) return;
        double maxValue = 0, minValue = 0;
        for (ThermalControlElement c : simTCEs) {
            if (c.material.magnetocaloric) {
                for (Vector<Double> dTcoolingVector : c.material.dTcooling) {
                    maxValue = Math.max(maxValue, Collections.max(dTcoolingVector));
                }

                for (Vector<Double> dTheatingVector : c.material.dTheating) {
                    maxValue = Math.max(maxValue, Collections.max(dTheatingVector));
                }
            }
            if (c.material.electrocaloric) {
                for (Vector<Double> dTVector : c.material.dT) {
                    maxValue = Math.max(maxValue, Math.abs(Collections.max(dTVector)));
                }
            }
        }
        double TCEmax = 0;
        double TCEmin = 2000;
        for (ThermalControlElement tce : heatCircuit.TCEs) {
            if (tce.startTemperature >= 0) {
                if (tce.startTemperature > TCEmax) TCEmax = tce.startTemperature;
                if (tce.startTemperature < TCEmin) TCEmin = tce.startTemperature;
            }
        }

        if (maxValue == 0) {
            minTemp = Math.min(Math.min(startTemp, TCEmin), Math.min(tempWest, tempEast));
            maxTemp = Math.max(Math.max(startTemp, TCEmax), Math.max(tempWest, tempEast));
        } else {
            minTemp = Math.min(startTemp, TCEmin) - maxValue;
            maxTemp = Math.max(startTemp, TCEmax) + maxValue;
        }
        maxValue = Double.MIN_VALUE;
        minValue = Double.MAX_VALUE;
        for (CyclePart cp : cycleParts)
            if (cp.partType == CyclePart.PartType.TEMPERATURE_CHANGE) {
                minValue = Math.min(minValue, Collections.min(cp.newTemperatures));
                maxValue = Math.max(maxValue, Collections.max(cp.newTemperatures));
            }
        minTemp = Math.min(minValue, minTemp);
        maxTemp = Math.max(maxValue, maxTemp);

    }
    @Override
    String getReport() {
        String dump;
        dump = "Data directory: " + "/materials\n" + "Time step dt: " + dt + "\n" + "Dimensionality: 1D\n" + "Boundary condition on the left: " + ModelMethods.return_bc_name(heatCircuit.westBoundary) + "\n" + "Boundary condition on the right: " + ModelMethods.return_bc_name(heatCircuit.eastBoundary) + "\n" + "Temperature on the left: " + heatCircuit.temperatureWest + " K\n" + "Convection coefficient on the left: " + heatCircuit.hWest + " W/(m²K)\n" + "Temperature on the right: " + heatCircuit.temperatureEast + " K\n" + "Convection coefficient on the right: " + heatCircuit.hEast + " W/(m²K)\n";

        dump += "\nThermal control elements: \n";
        for (ThermalControlElement tce : simTCEs) {
            dump += "TCE name: " + tce.name + "\n" + "TCE index: " + tce.index + "\n" +
                    //"Material: " + component.material.materialName + "\n" +
                    "Number of control volumes:  " + tce.numCvs + "\n" + "Control volume length: " + CirSim.formatLength(tce.cvs.get(0).dx) + "\n" + "Constant density: " + ((tce.constRho == -1) ? "not set" : tce.constRho + " kg/m³") + "\n" + "Constant specific heat: " + ((tce.constCp == -1) ? "not set" : tce.constCp + " J/(kgK)") + "\n" + "Constant thermal conductivity: " + ((tce.constK == -1) ? "not set" : tce.constK + " W/(mK)") + "\n" + "Left contact resistance: " + tce.westResistance + " mK/W\n" + "Right contact resistance: " + tce.eastResistance + " mK/W\n" + "Generated heat: " + 0.0 + " W/m²\n\n";
        }
        dump += "\nTemperatures:\n";
        dump += "Time\t";
        for (int i = 0; i < heatCircuit.cvs.size(); i++) {
            dump += "CV# " + i + "\t";
        }
        dump += "\n";
        for (int i = 0; i < temperatures.size(); i+= outputInterval) {
            Double[] temp = temperatures.get(i);
            Double time = times.get(i);
            dump += NumberFormat.getFormat("0.000").format(time) + "\t";
            for (double CVTemp : temp) {
                dump += NumberFormat.getFormat("0.00").format(CVTemp) + "\t";
            }
            dump += "\n";
        }
        dump += "\nFluxes:\n";
        heatCircuit.calculateHeatFluxes();
        for (Double f : heatCircuit.fluxes)
            dump += f + "\t";
        return dump;
    }

    @Override
    String printTCEs() {
        String tces = "";
        for (ThermalControlElement tce : simTCEs) {
            tces += tce.index + (tce == simTCEs.get(simTCEs.size() - 1) ? "" : ", ");
        }
        return tces;
    }
    @Override
    public String dump() {
        StringBuilder sb = new StringBuilder();
        sb.append("!").append(" ");
        sb.append(CirSim.theSim.selectedLengthUnit.ordinal()).append(" ");

        sb.append(hWest).append(' ')
                .append(hEast).append(' ')
                .append(westBoundary.ordinal()).append(' ')
                .append(eastBoundary.ordinal()).append(' ')
                .append(tempWest).append(' ')
                .append(tempEast).append(' ')
                .append(qWest).append(' ')
                .append(qEast).append(' ')
                .append(startTemp).append(' ')
                .append(ambientTemperature).append(' ')
                .append(dt).append(' ')
                .append(cyclic).append(' ');


        return sb.append("\n").toString();
    }



    public String dumpSimulationCycleParts() {
        StringBuilder sb = new StringBuilder();
        if (cycleParts.isEmpty())
            return "";
        sb.append("@").append(" ").append(cycleParts.size()).append(" ");
        for (CyclePart cp : cycleParts)
            sb.append(cp.dump());
        return sb.append("\n").toString();
    }

    public void undump(StringTokenizer tokenizer) {
        CirSim.theSim.selectedLengthUnit = CirSim.LengthUnit.values()[Integer.parseInt(tokenizer.nextToken())];
        CirSim.theSim.scale.setSelectedIndex(CirSim.theSim.selectedLengthUnit.ordinal());
        hWest = Double.parseDouble(tokenizer.nextToken());
        hEast = Double.parseDouble(tokenizer.nextToken());
        westBoundary = BorderCondition.values()[Integer.parseInt(tokenizer.nextToken())];
        eastBoundary = BorderCondition.values()[Integer.parseInt(tokenizer.nextToken())];
        tempWest = Double.parseDouble(tokenizer.nextToken());
        tempEast = Double.parseDouble(tokenizer.nextToken());
        qWest = Double.parseDouble(tokenizer.nextToken());
        qEast = Double.parseDouble(tokenizer.nextToken());
        startTemp = Double.parseDouble(tokenizer.nextToken());
        ambientTemperature = Double.parseDouble(tokenizer.nextToken());
        dt = Double.parseDouble(tokenizer.nextToken());
        cyclic = Boolean.parseBoolean(tokenizer.nextToken());
        CirSim.theSim.setCyclic(cyclic);
    }

    public void loadCycleParts(StringTokenizer tokenizer) {
        cycleParts.clear();
        int cyclePartNum = Integer.parseInt(tokenizer.nextToken());
        for (int i = 0; i < cyclePartNum; i++) {
            CyclePart cp = new CyclePart(-1, CirSim.theSim);
            cp.unDump(tokenizer);
            cycleParts.add(cp);
        }



        //this is some reaallly ugly code 0_0

        CirSim.theSim.displayTimer.scheduleRepeating(CirSim.theSim.FASTTIMER);
    }

}


