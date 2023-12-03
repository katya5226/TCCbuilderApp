package lahde.tccbuilder.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.*;

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
        TIME_PASS;


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
    Vector<Integer> newIndexes;
    Vector<Integer> fieldIndexes;
    Vector<Double> heatInputs;
    boolean toggleTCE;

    Vector<ThermalControlElement> TCEs;
    Vector<Vector<CyclePart.PropertyValuePair>> changedProperties;
    CirSim sim;
    double duration;
    double partTime;
    CyclePart theCyclePart;

    public CyclePart(int index, CirSim sim) {
        theCyclePart = this;
        partIndex = index;
        partType = PartType.HEAT_TRANSFER;
        TCEs = new Vector<ThermalControlElement>();
        newTemperatures = new Vector<Double>();
        heatInputs = new Vector<Double>();
        newIndexes = new Vector<Integer>();
        fieldIndexes = new Vector<Integer>();
        changedProperties = new Vector<>();
        this.sim = sim;
        duration = 0.0;
        partTime = 0.0;
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
                flexTable.setText(row, column++, temperature.toString());
        } else if (!newIndexes.isEmpty()) {
            for (Integer i : newIndexes)
                flexTable.setText(row, column++, i.toString());
        } else if (!heatInputs.isEmpty()) {
            for (Double heatInput : heatInputs)
                flexTable.setText(row, column++, heatInput.toString());
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
        contentPanel.add(new HTMLPanel(duration + "s"));
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
            default:
                break;
        }
    }

    void heatInput() {
        for (int i = 0; i < TCEs.size(); i++) {
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
                tce.magnetize();
            }
        } else if (duration > 0.0) {  // TODO: FIX THIS
            int steps = (int) (duration / sim.simulation1D.dt); // one execution will have length dt at most
            for (int i = 0; i < TCEs.size(); i++) {
                ThermalControlElement tce = TCEs.get(i);
                int fieldIndex = fieldIndexes.get(i);
                Vector<Double> dTheatcool = tce.field ? tce.material.dTcooling.get(fieldIndex) : tce.material.dTheating.get(fieldIndex);
                for (ControlVolume cv : tce.cvs) {
                    // Check if given cv's material's magnetocaloric flag is TRUE;
                    // if not, abort and inform the user.
                    int pos = (int) Math.round(cv.temperature * 10);
                    double dT = dTheatcool.get(pos);
                    double changeInStep = dT / steps;  // This is not correct.
                    cv.temperature = tce.field ? cv.temperature - changeInStep : cv.temperature + changeInStep;
                    cv.temperatureOld = cv.temperature;
                }
            }
        }
    }

    void electricFieldChange() {
        if (duration == 0.0) {
            for (int i = 0; i < TCEs.size(); i++) {
                ThermalControlElement tce = TCEs.get(i);
                tce.fieldIndex = fieldIndexes.get(i);
                tce.ePolarize();
            }
        }
    }

    void pressureChange() {
    }

    void shearStressChange() {
    }

    // This method must be modified by Katni - use constProperty, also change in cyclic dialog
    // void propertiesChange() {
    //     for (int i = 0; i < TCEs.size(); i++) {
    //         ThermalControlElement tce = TCEs.get(i);
    //         Vector<Double> newProps = newProperties.get(i);
    //         tce.setConstProperty(Simulation.Property.DENSITY, newProps.get(0));
    //         tce.setConstProperty(Simulation.Property.SPECIFIC_HEAT_CAPACITY, newProps.get(1));
    //         tce.setConstProperty(Simulation.Property.THERMAL_CONDUCTIVITY, newProps.get(2));
    //     }
    // }


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

    void toggleThermalControlElement() {
        for (int i = 0; i < TCEs.size(); i++) {
            ThermalControlElement thermalControlElement = TCEs.get(i);
            SwitchElm switchElm = (SwitchElm) thermalControlElement;
            switchElm.toggle();
        }
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
            case TOGGLE_THERMAL_CONTROL_ELEMENT:
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
            case TOGGLE_THERMAL_CONTROL_ELEMENT:
                break;
        }
    }
}
