package lahde.tccbuilder.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.*;

import java.util.*;
import java.util.Vector;
import java.util.HashMap;
import java.lang.Math;

@SuppressWarnings("ReassignedVariable")
public class CyclePart {

    public static class PropertyValuePair {
        Simulation.Property property;
        Double value;

        public PropertyValuePair(Simulation.Property property, Double value) {
            this.property = property;
            this.value = value;
        }
    }

    public enum PartType {
        HEAT_TRANSFER,
        HEAT_INPUT,
        MECHANIC_DISPLACEMENT,
        MAGNETIC_FIELD_CHANGE,
        ELECTRIC_FIELD_CHANGE,
        PRESSURE_CHANGE,
        SHEAR_STRESS_CHANGE,
        PROPERTIES_CHANGE,
        TEMPERATURE_CHANGE,
        TOGGLE_THERMAL_CONTROL_ELEMENT,
        TIME_PASS,
        LENGTH_CHANGE,
        AMB_TEMP_CHANGE,
        HEAT_LOSS;


        public String toSpacedCamelCase() {
            String[] words = this.name().toLowerCase().split("_");
            StringBuilder camelCase = new StringBuilder();

            for (String word : words) {
                camelCase.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    camelCase.append(word.substring(1)).append(" ");
                }
            }

            return camelCase.toString();
        }
    }

    int partIndex;
    PartType partType;
    Vector<Double> newTemperatures;
    Vector<Double> newLengths;
    Vector<Integer> newIndexes;
    Vector<Integer> fieldIndexes;
    Vector<Double> heatInputs;
    Vector<Double> heatLosses;
    boolean toggleTCE;

    Vector<ThermalControlElement> TCEs;
    Vector<Vector<CyclePart.PropertyValuePair>> changedProperties;
    CirSim sim;
    double duration;
    double partTime;
    double newAmbTemp;
    boolean boundaryTempChange;
    CyclePart theCyclePart;

    public CyclePart(int index, CirSim sim) {
        theCyclePart = this;
        partIndex = index;
        partType = PartType.HEAT_TRANSFER;
        TCEs = new Vector<ThermalControlElement>();
        newTemperatures = new Vector<Double>();
        newLengths = new Vector<Double>();
        heatInputs = new Vector<Double>();
        heatLosses = new Vector<Double>();
        newIndexes = new Vector<Integer>();
        fieldIndexes = new Vector<Integer>();
        changedProperties = new Vector<>();
        this.sim = sim;
        duration = 0.0;
        partTime = 0.0;
        newAmbTemp = 0.0;
        boundaryTempChange = false;
    }

    String shortenName(String name) {
        if (name.length() <= 3) {
            return name;
        } else {
            return name.substring(0, 3) + ".";
        }
    }

    public Widget toWidget(boolean deletable) {
        FlexTable flexTable = new FlexTable();
        flexTable.setStyleName("cycle-part");
        Element tableElement = flexTable.getElement();
        tableElement.setAttribute("rules", "all");
        int row = 0;
        int column = 0;

        if (!TCEs.isEmpty()) {
            for (ThermalControlElement tce : TCEs)
                flexTable.setText(row, column++, tce.index + " " + shortenName(tce.name));
            row++;
            column = 0;
        } else
            flexTable.setText(row++, column, "All components/TCEs");
        if (toggleTCE) {
            //ignore
        } else if (!fieldIndexes.isEmpty()) {
            for (int i = 0; i < fieldIndexes.size(); i++) {
                int index = fieldIndexes.get(i);
                if (TCEs.get(i).material.isLoaded()) {
                    String unit = "";
                    if (TCEs.get(i).material.magnetocaloric) unit = " T";
                    if (TCEs.get(i).material.electrocaloric) unit = " MV/m";
                    if (TCEs.get(i).material.elastocaloric) unit = " N/m";
                    if (TCEs.get(i).material.barocaloric) unit = " bar";
                    flexTable.setText(row, column++, NumberFormat.getFormat("0.0").format(TCEs.get(i).material.fields.get(index)) + unit);
                }
            }
        } else if (!newTemperatures.isEmpty()) {
            for (Double temperature : newTemperatures)
                flexTable.setText(row, column++, temperature.toString() + " K");
        } else if (!newLengths.isEmpty()) {
            for (Double length : newLengths)
                flexTable.setText(row, column++, length.toString() + " mm");
        } else if (!newIndexes.isEmpty()) {
            for (Integer i : newIndexes)
                flexTable.setText(row, column++, i.toString());
        } else if (!heatInputs.isEmpty()) {
            for (Double heatInput : heatInputs)
                flexTable.setText(row, column++, heatInput.toString() + " W/m³");
        } else if (!heatLosses.isEmpty()) {
            for (Double heatLoss : heatLosses)
                flexTable.setText(row, column++, heatLoss.toString() + " W/m³/K");
        } else if (!changedProperties.isEmpty()) {
            flexTable.removeAllRows();
            for (int i = 0; i < changedProperties.size(); i++) {
                ThermalControlElement tce = TCEs.get(i);
                flexTable.setText(row++, 0, tce.index + " " + tce.name);
                Vector<PropertyValuePair> v = changedProperties.get(i);
                for (PropertyValuePair pvp : v) {
                    flexTable.setText(row, 0, String.valueOf(pvp.property) + " (" + Simulation.propUnit(pvp.property) + ") ");
                    flexTable.setText(row++, 1, String.valueOf(pvp.value));
                }
            }
        }
        row++;
        column = 0;

        HTMLPanel htmlPanel = new HTMLPanel("");
        htmlPanel.addStyleName("cycle-part-outer");
        HTMLPanel contentPanel = new HTMLPanel("");
        contentPanel.setStyleName("cycle-part-header");
        HTMLPanel titlePanel = new HTMLPanel(partIndex + ". " + partType.toSpacedCamelCase());
        titlePanel.addStyleName("d-flex");
        titlePanel.addStyleName("justify-between");
        titlePanel.addStyleName("align-v-center");
        contentPanel.add(titlePanel);
        contentPanel.add(new HTMLPanel(duration + " s"));
        if (partType == CyclePart.PartType.AMB_TEMP_CHANGE) {
            contentPanel.add(new HTMLPanel("Ambient temperature: " + newAmbTemp + " K"));
            contentPanel.add(new HTMLPanel("Boundary temperatures set to ambient: " + boundaryTempChange));
        }
        Button button = new Button("x");
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Window.alert("Cycle part deletion is irreversible.");
                CirSim.theSim.simulation1D.cycleParts.remove(theCyclePart);
                CirSim.theSim.fillCyclicPanel();
            }
        });

        htmlPanel.add(contentPanel);
        if (deletable)
            titlePanel.add(button);
        htmlPanel.add(flexTable);

        return htmlPanel;

    }


    public void execute() {
        switch (partType) {
            case HEAT_TRANSFER:
                if (duration > sim.simulation1D.dt)
                    sim.simulation1D.heatTransferStep();
                break;
            case HEAT_INPUT:
                heatInput();
                if (duration > sim.simulation1D.dt)
                    sim.simulation1D.heatTransferStep();
                // removeHeat();
                break;
            case MECHANIC_DISPLACEMENT:
                mechanicDisplacement();
                break;
            case MAGNETIC_FIELD_CHANGE:
                magneticFieldChange();
                if (duration > sim.simulation1D.dt)
                    sim.simulation1D.heatTransferStep();
                break;
            case ELECTRIC_FIELD_CHANGE:
                electricFieldChange();
                break;
            case PRESSURE_CHANGE:
                pressureChange();
                break;
            case SHEAR_STRESS_CHANGE:
                shearStressChange();
                break;
            case PROPERTIES_CHANGE:
                propertiesChange();
                if (duration > sim.simulation1D.dt)
                    sim.simulation1D.heatTransferStep();
                break;
            case TOGGLE_THERMAL_CONTROL_ELEMENT:
                toggleThermalControlElement();
                break;
            case TEMPERATURE_CHANGE:
                temperatureChange();
                if (duration > sim.simulation1D.dt)
                    sim.simulation1D.heatTransferStep();
                break;
            case TIME_PASS:
                break;
            case LENGTH_CHANGE:
                lengthChange();
                break;
            case AMB_TEMP_CHANGE:
                ambTempChange();
                break;
            case HEAT_LOSS:
                heatLoss();
                break;
            default:
                break;
        }
    }

    void heatInput() {
        for (int i = 0; i < TCEs.size(); i++) {
            TCEs.get(i).volumeHeatGeneration = heatInputs.get(i);
            for (ControlVolume cv : TCEs.get(i).cvs) {
                // cv.qGenerated = heatInputs.get(i);
                cv.constQgen = heatInputs.get(i);
            }
        }
    }

    void removeHeat() {
        for (int i = 0; i < TCEs.size(); i++) {
            for (ControlVolume cv : TCEs.get(i).cvs) {
                // cv.qGenerated = 0.0;
                cv.constQgen = 0.0;
            }
        }
    }

    void mechanicDisplacement() {
        if (duration == 0.0) {
            for (int i = 0; i < TCEs.size(); i++) {
                TCEs.get(i).index = newIndexes.get(i);
            }
            sim.reorderByIndex();
            sim.simulation1D.heatCircuit.buildTCC();
        }
    }

    void magneticFieldChange() {
        if (duration == 0.0) {
            for (int i = 0; i < TCEs.size(); i++) {
                ThermalControlElement tce = TCEs.get(i);
                tce.fieldIndex = fieldIndexes.get(i);
                tce.toggleField();
            }
        } else if (duration > 0.0) {  // TODO: FIX THIS
            // int steps = (int) (duration / sim.simulation1D.dt); // one execution will have length dt at most
            // for (int i = 0; i < TCEs.size(); i++) {
            //     ThermalControlElement tce = TCEs.get(i);
            //     int fieldIndex = fieldIndexes.get(i);
            //     if (!tce.material.dTThysteresis)
            //         Vector<Double> dTs = tce.field ? tce.material.dTFieldRemove.get(fieldIndex) : tce.material.dTFieldApply.get(fieldIndex);
            //     // else
            //     //     Vector<Double> dTs = tce.field ? tce.material.dTFieldRemove.get(fieldIndex) : tce.material.dTFieldApply.get(fieldIndex);
            //     for (ControlVolume cv : tce.cvs) {
            //         // Check if given cv's material's magnetocaloric flag is TRUE;
            //         // if not, abort and inform the user.
            //         int pos = (int) Math.round(cv.temperature * 10);
            //         double dT = dTs.get(pos);
            //         double changeInStep = dT / steps;  // This is not correct.
            //         cv.temperature = tce.field ? cv.temperature - changeInStep : cv.temperature + changeInStep;
            //         cv.temperatureOld = cv.temperature;
            //     }
            // }
        }
    }

    void electricFieldChange() {
        if (duration == 0.0) {
            for (int i = 0; i < TCEs.size(); i++) {
                ThermalControlElement tce = TCEs.get(i);
                tce.fieldIndex = fieldIndexes.get(i);
                tce.toggleField();
            }
        }
    }

    void pressureChange() {
        if (duration == 0.0) {
            for (int i = 0; i < TCEs.size(); i++) {
                ThermalControlElement tce = TCEs.get(i);
                tce.fieldIndex = fieldIndexes.get(i);
                tce.toggleField();
            }
        }
    }

    void shearStressChange() {
        if (duration == 0.0) {
            for (int i = 0; i < TCEs.size(); i++) {
                ThermalControlElement tce = TCEs.get(i);
                tce.fieldIndex = fieldIndexes.get(i);
                tce.toggleField();
            }
        }
    }

    void propertiesChange() {
        // int steps = (int) (duration / sim.simulation1D.dt);
        for (int i = 0; i < changedProperties.size(); i++) {
            Vector<PropertyValuePair> v = changedProperties.get(i);
            for (PropertyValuePair pvp : v) {
                for (ControlVolume cv : TCEs.get(i).cvs) {
                    if (duration == 0.0) {
                        cv.setProperty(pvp.property, pvp.value);
                    } else {
                        double currentValue = cv.getProperty(pvp.property);
                        double finalValue = pvp.value;
                        // double changeInStep = (finalValue - currentValue) / steps;
                        double changeTo = currentValue + (partTime / duration) * (pvp.value - currentValue);
                        // cv.setProperty(pvp.property, changeInStep);
                        cv.setProperty(pvp.property, changeTo);
                    }
                }
            }
        }
    }


    void temperatureChange() {
        if (duration == 0.0) {
            for (int i = 0; i < TCEs.size(); i++) {
                for (ControlVolume cv : TCEs.get(i).cvs) {
                    cv.temperature = newTemperatures.get(i);
                    cv.temperatureOld = newTemperatures.get(i);
                }
            }
        } else if (duration > 0.0) {
            // int steps = (int) (duration / sim.simulation1D.dt);
            for (int i = 0; i < TCEs.size(); i++) {
                for (ControlVolume cv : TCEs.get(i).cvs) {
                    // double changeInStep = (newTemperatures.get(i) - cv.temperature) / steps;
                    // cv.temperature += changeInStep;
                    // cv.temperatureOld += changeInStep;
                    cv.temperature = cv.temperature + (partTime / duration) * (newTemperatures.get(i) - cv.temperature);
                    cv.temperatureOld = cv.temperature;
                }
            }
        }
    }

    void lengthChange() {
        if (duration == 0.0) {
            for (int i = 0; i < TCEs.size(); i++) {
                TCEs.get(i).setNewLength(newLengths.get(i));
            }
            double t = sim.simulation1D.time;
            sim.removeZeroLengthElements();
            sim.simulation1D.heatCircuit.buildTCC();
            sim.reorderByIndex();
            sim.simulation1D.time = t;
        }
    }

    void toggleThermalControlElement() {
        for (int i = 0; i < TCEs.size(); i++) {
            ThermalControlElement thermalControlElement = TCEs.get(i);
            SwitchElm switchElm = (SwitchElm) thermalControlElement;
            switchElm.toggle();
        }
    }

    void ambTempChange() {
        sim.simulation1D.heatCircuit.ambientTemperature = newAmbTemp;
        if (boundaryTempChange) {
            sim.simulation1D.heatCircuit.temperatureWest = newAmbTemp;
            sim.simulation1D.heatCircuit.temperatureEast = newAmbTemp;
        }
    }

    void heatLoss() {
        for (int i = 0; i < TCEs.size(); i++) {
            TCEs.get(i).hTransv = heatLosses.get(i);
            for (ControlVolume cv : TCEs.get(i).cvs) {
                cv.hTransv = heatLosses.get(i);
            }
        }
    }

    public String toReport() {
        String report = partIndex + " " + partType + " Duration: " + duration + " s\n";
        switch (partType) {
            case HEAT_TRANSFER:
                break;
            case HEAT_INPUT:
                for (ThermalControlElement tce : TCEs) {
                    report += sim.simulation1D.simTCEs.indexOf(tce) + " " + tce.name + "\t" + "Heat input: ";
                    report += String.valueOf(heatInputs.get(TCEs.indexOf(tce))) + " W/m³\n";
                }
                break;
            case HEAT_LOSS:
                for (ThermalControlElement tce : TCEs) {
                    report += sim.simulation1D.simTCEs.indexOf(tce) + " " + tce.name + "\t" + "Heat loss to ambient: ";
                    report += String.valueOf(heatLosses.get(TCEs.indexOf(tce))) + " W/m³/K\n";
                }
                break;
            case MECHANIC_DISPLACEMENT:
                for (ThermalControlElement tce : TCEs) {
                    report += sim.simulation1D.simTCEs.indexOf(tce) + " " + tce.name + "\t" + "New indeces: ";
                    report += String.valueOf(newIndexes.get(TCEs.indexOf(tce))) + "\n";
                }
                break;
            case MAGNETIC_FIELD_CHANGE:
                for (ThermalControlElement tce : TCEs) {
                    int fieldIndexM = fieldIndexes.get(TCEs.indexOf(tce));
                    report += sim.simulation1D.simTCEs.indexOf(tce) + " " + tce.name + "\t" + "Fields: ";
                    report += String.valueOf(tce.material.fields.get(fieldIndexM)) + " T\n";
                }
                break;
            case ELECTRIC_FIELD_CHANGE:
                for (ThermalControlElement tce : TCEs) {
                    int fieldIndexE = fieldIndexes.get(TCEs.indexOf(tce));
                    report += sim.simulation1D.simTCEs.indexOf(tce) + " " + tce.name + "\t" + "Fields: ";
                    report += String.valueOf(tce.material.fields.get(fieldIndexE)) + " MV/m\n";
                }
                break;
            case PRESSURE_CHANGE:
                for (ThermalControlElement tce : TCEs) {
                    int fieldIndexE = fieldIndexes.get(TCEs.indexOf(tce));
                    report += sim.simulation1D.simTCEs.indexOf(tce) + " " + tce.name + "\t" + "Fields: ";
                    report += String.valueOf(tce.material.fields.get(fieldIndexE)) + " kbar\n";
                }
                break;
            case SHEAR_STRESS_CHANGE:
                for (ThermalControlElement tce : TCEs) {
                    int fieldIndexE = fieldIndexes.get(TCEs.indexOf(tce));
                    report += sim.simulation1D.simTCEs.indexOf(tce) + " " + tce.name + "\t" + "Fields: ";
                    report += String.valueOf(tce.material.fields.get(fieldIndexE)) + " kbar\n";
                }
                break;
            case PROPERTIES_CHANGE:
                for (ThermalControlElement tce : TCEs) {
                    report += sim.simulation1D.simTCEs.indexOf(tce) + " " + tce.name + "\t" + "Properties:\n";
                    for (PropertyValuePair pvp : changedProperties.get(TCEs.indexOf(tce))) {
                        report += pvp.property + " ";
                        report += pvp.value;
                        report += " " + Simulation.propUnit(pvp.property) + "\n";
                    }
                }
                break;
            case TEMPERATURE_CHANGE:
                for (ThermalControlElement tce : TCEs) {
                    report += sim.simulation1D.simTCEs.indexOf(tce) + " " + tce.name + "\t" + "New temperature: ";
                    report += String.valueOf(newTemperatures.get(TCEs.indexOf(tce))) + " K\n";
                }
                break;
            case LENGTH_CHANGE:
                for (ThermalControlElement tce : TCEs) {
                    report += sim.simulation1D.simTCEs.indexOf(tce) + " " + tce.name + "\t" + "New length: ";
                    report += String.valueOf(newLengths.get(TCEs.indexOf(tce))) + " mm\n";
                }
                break;
            case TOGGLE_THERMAL_CONTROL_ELEMENT:
                break;
            case TIME_PASS:
                break;
            case AMB_TEMP_CHANGE:
                report += String.valueOf(newAmbTemp) + " K\t";
                report += String.valueOf(sim.simulation1D.heatCircuit.temperatureWest) + " K\t";
                report += String.valueOf(sim.simulation1D.heatCircuit.temperatureEast) + " K\n";
                break;
        }
        report += "\n";
        return report;
    }

    public String dump() {
        String dump = partIndex + " " + partType + " " + duration + " " + TCEs.size() + " ";
        if (!TCEs.isEmpty())
            for (ThermalControlElement tce : TCEs) {
                dump += sim.simulation1D.simTCEs.indexOf(tce) + " ";
            }

        switch (partType) {
            case HEAT_TRANSFER:
                break;
            case HEAT_INPUT:
                dump += heatInputs.size() + " ";
                for (Double d : heatInputs)
                    dump += d + " ";
                break;
            case HEAT_LOSS:
                dump += heatLosses.size() + " ";
                for (Double d : heatLosses)
                    dump += d + " ";
                break;
            case MECHANIC_DISPLACEMENT:
                dump += newIndexes.size() + " ";
                for (Integer i : newIndexes)
                    dump += i + " ";
                break;
            case MAGNETIC_FIELD_CHANGE:
                dump += fieldIndexes.size() + " ";
                for (Integer i : fieldIndexes)
                    dump += i + " ";
                break;
            case ELECTRIC_FIELD_CHANGE:
                dump += fieldIndexes.size() + " ";
                for (Integer i : fieldIndexes)
                    dump += i + " ";
                break;
            case PRESSURE_CHANGE:
                break;
            case SHEAR_STRESS_CHANGE:
                break;
            case PROPERTIES_CHANGE:
                dump += changedProperties.size() + " ";
                for (Vector<PropertyValuePair> v : changedProperties) {
                    dump += v.size() + " ";
                    for (PropertyValuePair pvp : v) {
                        dump += Simulation.propToInt(pvp.property) + " " + pvp.value.toString() + " ";
                    }                    
                }
                break;
            case TEMPERATURE_CHANGE:
                dump += newTemperatures.size() + " ";
                for (Double t : newTemperatures)
                    dump += t + " ";

                break;
            case LENGTH_CHANGE:
                dump += newLengths.size() + " ";
                for (Double l : newLengths)
                    dump += l + " ";

                break;
            case TOGGLE_THERMAL_CONTROL_ELEMENT:
                break;
            case TIME_PASS:
                break;
            case AMB_TEMP_CHANGE:
                int bT = boundaryTempChange ? 1 : 0;
                dump += newAmbTemp + " ";
                dump += bT + " ";
                break;
        }
        return dump;
    }

    public void unDump(StringTokenizer st) {

        // Parse and set common properties
        partIndex = Integer.parseInt(st.nextToken());
        partType = PartType.valueOf(st.nextToken());
        duration = Double.parseDouble(st.nextToken());
        int numTCEs = Integer.parseInt(st.nextToken());

        // Parse and set TCEs
        TCEs.clear();
        for (int i = 0; i < numTCEs; i++) {
            int tceIndex = Integer.parseInt(st.nextToken());
            TCEs.add(sim.simulation1D.simTCEs.get(tceIndex));
        }


        // Parse and set properties based on partType
        switch (partType) {
            case HEAT_TRANSFER:
                break;
            case HEAT_INPUT:
                int numHeatInputs = Integer.parseInt(st.nextToken());
                heatInputs.clear();
                for (int i = 0; i < numHeatInputs; i++) {
                    heatInputs.add(Double.parseDouble(st.nextToken()));
                }
                break;
            case HEAT_LOSS:
                int numHeatLosses = Integer.parseInt(st.nextToken());
                heatLosses.clear();
                for (int i = 0; i < numHeatLosses; i++) {
                    heatLosses.add(Double.parseDouble(st.nextToken()));
                }
                break;
            case MECHANIC_DISPLACEMENT:
                int numNewIndexes = Integer.parseInt(st.nextToken());
                newIndexes.clear();
                for (int i = 0; i < numNewIndexes; i++) {
                    newIndexes.add(Integer.parseInt(st.nextToken()));
                }
                break;
            case MAGNETIC_FIELD_CHANGE:
                int numMagFieldIndexes = Integer.parseInt(st.nextToken());
                fieldIndexes.clear();
                for (int i = 0; i < numMagFieldIndexes; i++) {
                    fieldIndexes.add(Integer.parseInt(st.nextToken()));
                }
                break;
            case ELECTRIC_FIELD_CHANGE:
                int numElFieldIndexes = Integer.parseInt(st.nextToken());
                fieldIndexes.clear();
                for (int i = 0; i < numElFieldIndexes; i++) {
                    fieldIndexes.add(Integer.parseInt(st.nextToken()));
                }
                break;
            case PRESSURE_CHANGE:
                break;
            case SHEAR_STRESS_CHANGE:
                break;
            case PROPERTIES_CHANGE:
                int numChosenTCEs = Integer.parseInt(st.nextToken());
                changedProperties.clear();
                for (int i = 0; i < numChosenTCEs; i++) {
                    Vector<PropertyValuePair> v = new Vector<>();
                    int numPVPs = Integer.parseInt(st.nextToken());
                    for (int j = 0; j < numPVPs; j++) {
                        v.add(new PropertyValuePair(Simulation.intToProp(Integer.parseInt(st.nextToken())), Double.parseDouble(st.nextToken())));
                    }
                    changedProperties.add(v);
                }
                break;
            case TEMPERATURE_CHANGE:
                int numNewTemperatures = Integer.parseInt(st.nextToken());
                newTemperatures.clear();
                for (int i = 0; i < numNewTemperatures; i++) {
                    newTemperatures.add(Double.parseDouble(st.nextToken()));
                }
                break;
            case LENGTH_CHANGE:
                int numNewLengths = Integer.parseInt(st.nextToken());
                newLengths.clear();
                for (int i = 0; i < numNewLengths; i++) {
                    newLengths.add(Double.parseDouble(st.nextToken()));
                }
                break;
            case TOGGLE_THERMAL_CONTROL_ELEMENT:
                break;
            case TIME_PASS:
                break;
            case AMB_TEMP_CHANGE:
                newAmbTemp = Double.parseDouble(st.nextToken());
                int bT = Integer.parseInt(st.nextToken());
                boundaryTempChange = (bT == 1)? true : false;
                break;
        }
    }
}
