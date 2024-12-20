package lahde.tccbuilder.client;


import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Window;

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
        tempWest = 293.0;
        tempEast = 293.0;
        qWest = 0.0;
        qEast = 0.0;
        westFrequency = 0.0;
        eastFrequency = 0.0;
        westAmplitude = 0.0;
        eastAmplitude = 0.0;
        startTemp = 293.0;
        ambientTemperature = 293.0;
        // start_temperatures = new double[num_cvs];
        times = new ArrayList<Double>();
        temperatures = new ArrayList<Double[]>();
        TEpowerOutputs = new ArrayList<ArrayList<Double[]>>();
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

    public void appendNewTemps() {
        Double[] ttemps = new Double[heatCircuit.cvs.size()];
        for (int i = 0; i < heatCircuit.cvs.size(); i++) {
            ttemps[i] = heatCircuit.cvs.get(i).temperature;
        }
        this.temperatures.add(ttemps);
        this.times.add(this.time);
    }

    void logPowerOutput() {
        ArrayList<Double[]> ar = new ArrayList<>();
        for (ThermalControlElement tce : heatCircuit.TCEs) {
            if (tce instanceof TEHeatEngine) {
                TEHeatEngine TE = (TEHeatEngine) tce;
                ar.add(TE.outputPower());
            }
        }
        TEpowerOutputs.add(ar);
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
        for (int k = 0; k < 15; k++) {
            // heatCircuit.neighbours()
            checkDiodes();
            heatCircuit.calculateConductivities();

            heatCircuit.makeMatrix(dt);
            ModelMethods.tdmaSolve(heatCircuit.cvs, heatCircuit.underdiag, heatCircuit.diag, heatCircuit.upperdiag, heatCircuit.rhs);
            for (int i = 0; i < heatCircuit.cvs.size(); i++) {
                x_mod.set(i, heatCircuit.cvs.get(i).temperature);
            }
            boolean flag = ModelMethods.compareTemps(x_mod, x_prev, 0.5);
            // boolean flag = true;
            for (int i = 0; i < heatCircuit.cvs.size(); i++) {
                x_prev.set(i, x_mod.get(i));
            }
            for (int i = 0; i < simTCEs.size(); i++) {
                if (simTCEs.get(i).cvs.get(0).material.cpThysteresis == true)  // TODO-material
                    simTCEs.get(i).updateModes();
            }
            if (flag && k >= 2) { break; }
            if (!flag && k > 10) {
                Window.alert("Not converged; change time step!");
            }
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
        this.appendNewTemps();
        if (heatCircuit.containsTEEngines) logPowerOutput();
        // hf.print_darray_row(this.temperatures[-1], this.temperatures_file, 4)
        // ModelMethods.printTemps(this.temperatures.get(this.temperatures.size()-1));
    }

    void setStartTemps() {
        for (ThermalControlElement tce : heatCircuit.TCEs) {
            if (tce.startTemperature == -1) {
                tce.setTemperatures(startTemp);
            }
            else {
                tce.setTemperatures(tce.startTemperature);
            }
            // GWT.log(String.valueOf(tce.cvs.get(0).temperature));
        }
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

        heatCircuit = new TCC(this, "Heat circuit", simTCEs);
        heatCircuit.westBoundary = westBoundary;
        heatCircuit.eastBoundary = eastBoundary;
        heatCircuit.hWest = hWest;
        heatCircuit.hEast = hEast;
        heatCircuit.temperatureWest = tempWest;
        heatCircuit.temperatureEast = tempEast;
        heatCircuit.qWest = qWest;
        heatCircuit.qEast = qEast;
        heatCircuit.ambientTemperature = ambientTemperature;
        heatCircuit.amplitudeEast = eastAmplitude;
        heatCircuit.amplitudeWest = westAmplitude;
        heatCircuit.frequencyEast = eastFrequency;
        heatCircuit.frequencyWest = westFrequency;

        heatCircuit.buildTCC();
        heatCircuit.initializeMatrix();
        int n = heatCircuit.cvs.size();
        double[] temps = new double[n];

        Arrays.fill(temps, startTemp);
        //heatCircuit.setTemperatures(temps);
        setStartTemps();

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
    }

    void setTemperatureRange() {
        if (customTempRange == true) return;
        double maxValue = 0, minValue = 0;
        for (ThermalControlElement c : simTCEs) {
            if (c.material.magnetocaloric || c.material.electrocaloric || c.material.elastocaloric || c.material.barocaloric) {
                if (!c.material.dTThysteresis) {
                    for (Vector<Double> dTFRVector : c.material.dTFieldRemove) {
                        maxValue = Math.max(maxValue, Collections.max(dTFRVector));
                    }
                    for (Vector<Double> dTFAVector : c.material.dTFieldApply) {
                        maxValue = Math.max(maxValue, Collections.max(dTFAVector));
                    }
                }
                else {
                    for (Vector<Double> dTFRHVector : c.material.dTFieldRemoveHeating) {
                        maxValue = Math.max(maxValue, Collections.max(dTFRHVector));
                    }
                    for (Vector<Double> dTFAHVector : c.material.dTFieldApplyHeating) {
                        maxValue = Math.max(maxValue, Collections.max(dTFAHVector));
                    }
                    for (Vector<Double> dTFRCVector : c.material.dTFieldRemoveCooling) {
                        maxValue = Math.max(maxValue, Collections.max(dTFRCVector));
                    }
                    for (Vector<Double> dTFACVector : c.material.dTFieldApplyCooling) {
                        maxValue = Math.max(maxValue, Collections.max(dTFACVector));
                    }                    
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
        dump = "Data directory: " + "/materials\n" + "Time step dt: " + dt + "\n" + "Dimensionality: 1D\n" + "West boundary condition: " + ModelMethods.return_bc_name(heatCircuit.westBoundary) + "\n" + "East boundary condition: " + ModelMethods.return_bc_name(heatCircuit.eastBoundary) + "\n" + "West temperature: " + heatCircuit.temperatureWest + " K\n" + "West convection coefficient: " + heatCircuit.hWest + " W/(m²K)\n" + "East temperature: " + heatCircuit.temperatureEast + " K\n" + "East convection coefficient: " + heatCircuit.hEast + " W/(m²K)\n";

        dump += "\nThermal control elements: \n";
        for (ThermalControlElement tce : simTCEs) {
            dump += "TCE name: " + tce.name + "\n" + "TCE index: " + tce.index + "\n" +
                    //"Material: " + component.material.materialName + "\n" +
                    "Number of control volumes:  " + tce.numCvs + "\n" + "Control volume length: " + CirSim.formatLength(tce.cvs.get(0).dx) + "\n" + "Constant density: " + ((tce.constRho == -1) ? "not set" : tce.constRho + " kg/m³") + "\n" + "Constant specific heat: " + ((tce.constCp == -1) ? "not set" : tce.constCp + " J/(kgK)") + "\n" + "Constant thermal conductivity: " + ((tce.constK == -1) ? "not set" : tce.constK + " W/(mK)") + "\n" + "West contact resistance: " + tce.westResistance + " m²K/W\n" + "East contact resistance: " + tce.eastResistance + " m²K/W\n" + "Generated heat: " + 0.0 + " W/m²\n\n";
        }
        if (cyclic) {
            dump += "\nCycle parts:\n";
            for (CyclePart cp : cycleParts) {
                dump += cp.toReport();
            }
        }
        dump += "\nTemperatures:\n";
        dump += "Time (s)\t";
        for (int i = 0; i < heatCircuit.cvs.size(); i++) {
            dump += "CV# " + i + " T (K)\t";
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
        dump += "\nFluxes at CV faces (W/m²) at the time of report generation:\n";
        heatCircuit.calculateHeatFluxes();
        for (Double f : heatCircuit.fluxes)
            dump += f + "\t";
        if(TEpowerOutputs.size() > 0) {
            dump += "\n\nPower outputs of TE heat engines:\n";
            dump += "Time (s)\t";
            for (int i = 0; i < TEpowerOutputs.get(0).size(); i++) {
                dump += "TEengine#" + i + ": Average dT² (K²)\t P (W)\t";
            }
            dump += "\n";
            for (int i = 0; i < TEpowerOutputs.size(); i+= outputInterval) {
                ArrayList<Double[]> powers = TEpowerOutputs.get(i);
                Double time = times.get(i);
                dump += NumberFormat.getFormat("0.000").format(time) + "\t";
                for (Double[] p : powers) {
                    dump += NumberFormat.getFormat("0.00").format(p[0]) + "\t" + NumberFormat.getFormat("0.00").format(p[1]) + "\t";
                }
                dump += "\n";
            }
        }
        dump += "\n\n" + assessment();
        return dump;
    }

    String assessment() {
        String as = "";
        as += "AVERAGE THERMAL DIFFUSIVITIES OF TCEs:\n";
        for (ThermalControlElement tce : heatCircuit.TCEs) {
            as += tce.name + "\t" + NumberFormat.getFormat("0.00").format(tce.calculateDiffusivity()) + "x10\u207B⁶ m²/s\n";
        }
        as += "LARGEST TEMPERATURE DIFFERENCE BETWEEN CVs:\n";
        for (ThermalControlElement tce : heatCircuit.TCEs) {
            as += tce.name + "\t" + tce.calculateLargestDeltaT() + "\n";
        }
        as += "HEAT FLUX AT WEST BOUNDARY: " + heatCircuit.fluxes.get(0) + " W/m²\n";
        as += "HEAT FLUX AT EAST BOUNDARY: " + heatCircuit.fluxes.get(heatCircuit.fluxes.size() - 1) + " W/m²\n";
        // as += "TCE ACTUATION POWER INPUTS (divided by TCE cross area):\n";
        as += "TCE ACTUATION POWER INPUTS:\n";
        for (ThermalControlElement tce : heatCircuit.TCEs) {
            as += tce.name + "\tInput power " + tce.inputPower + " W\n";
            // if (tce.crossArea == -1) as += tce.name + "\tCross area unknown\n";  // TO DO IF THERE'S TIME
            // else as += tce.name + "\t Input power " + tce.inputPower / tce.crossArea + " W/m²\n";
        } 
        return as;
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


