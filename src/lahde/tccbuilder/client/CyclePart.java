package lahde.tccbuilder.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
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
        VALUE_CHANGE;


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
    Vector<Integer> newFieldIndexes;
    Vector<Double> heatInputs;
    boolean toggleTCE;

    Vector<ThermalControlElement> TCEs;
    Vector<Vector<Double>> newProperties;  // Vector<Double> for each component must have three values, for
    // rho, cp and k. In Cyclic dialog, the value of const_x (x = rho, cp or k) is set to -1 if a constant value needs not be set.
    Vector<HashMap<Simulation.Property, Double>> changedProperties;
    Vector<Vector<PropertyValuePair>> changedValues;
    CirSim sim;
    double duration;
    CyclePart theCyclePart;

    public CyclePart(int index, CirSim sim) {
        theCyclePart = this;
        partIndex = index;
        partType = PartType.HEAT_TRANSFER;
        TCEs = new Vector<ThermalControlElement>();
        newProperties = new Vector<Vector<Double>>();
        newTemperatures = new Vector<Double>();
        heatInputs = new Vector<Double>();
        newIndexes = new Vector<Integer>();
        newFieldIndexes = new Vector<Integer>();
        changedProperties = new Vector<HashMap<Simulation.Property, Double>>();
        changedValues = new Vector<>();
        this.sim = sim;
        duration = 0;
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
                flexTable.setText(row, column++, tce.index + " " + tce.name);
            row++;
            column = 0;
        } else
            flexTable.setText(row++, column, "All components/TCEs");
        if (toggleTCE) {
            //ignore
        } else if (!newFieldIndexes.isEmpty()) {
            for (int i = 0; i < newFieldIndexes.size(); i++) {
                int index = newFieldIndexes.get(i);
                if (TCEs.get(i).material.isLoaded()) {
                    flexTable.setText(row, column++, TCEs.get(i).material.fields.get(index) + "T");
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
        } else if (!newProperties.isEmpty()) {
            int startingRow;
            for (Vector<Double> properties : newProperties) {
                startingRow = row;
                for (Double p : properties) {
                    flexTable.setText(startingRow++, column, p.toString());
                }
                column++;
            }
            startingRow = row;
            for (String s : new String[]{"rho", "cp", "k"})
                flexTable.setText(startingRow++, column, s);

            flexTable.setText(0, column, "property");
        } else if (!changedValues.isEmpty()) {
            flexTable.removeAllRows();
            for (int i = 0; i < changedValues.size(); i++) {
                ThermalControlElement tce = TCEs.get(i);
                flexTable.setText(row++, 0, tce.index + " " + tce.name);
                Vector<PropertyValuePair> v = changedValues.get(i);
                for (PropertyValuePair pvp : v) {
                    flexTable.setText(row, 0, String.valueOf(pvp.property));
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
                removeHeat();
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
                break;
            case TOGGLE_THERMAL_CONTROL_ELEMENT:
                toggleThermalControlElement();
                break;
            case VALUE_CHANGE:
                valueChange();
                if (duration > sim.simulation1D.dt)
                    sim.simulation1D.heatTransferStep();
            case TEMPERATURE_CHANGE:
                temperatureChange();
                if (duration > sim.simulation1D.dt)
                    sim.simulation1D.heatTransferStep();
                break;
            default:
                break;
        }
    }

    void heatInput() {
        for (int i = 0; i < TCEs.size(); i++) {
            for (ControlVolume cv : TCEs.get(i).cvs) {
                cv.qGenerated = heatInputs.get(i);
            }
        }
    }

    void removeHeat() {
        for (int i = 0; i < TCEs.size(); i++) {
            for (ControlVolume cv : TCEs.get(i).cvs) {
                cv.qGenerated = 0.0;
            }
        }
    }

    void mechanicDisplacement() {
        if (duration == 0.0) {
            for (int i = 0; i < TCEs.size(); i++) {
                TCEs.get(i).index = newIndexes.get(i);
            }
            sim.reorderByIndex();
        }
    }

    void magneticFieldChange() {
        if (duration == 0.0) {
            for (ThermalControlElement tce : TCEs) {
                tce.magnetize();
            }
        } else if (duration > 0.0) {  // TO BE CORRECTED
            int steps = (int) (duration / sim.simulation1D.dt); // one execution will have length dt at most
            for (ThermalControlElement tce : TCEs) {
                Vector<Double> dTheatcool = new Vector<Double>();
                dTheatcool = tce.field ? tce.material.dTcooling.get(tce.fieldIndex - 1) : tce.material.dTheating.get(tce.fieldIndex - 1);
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
    }

    void pressureChange() {
    }

    void shearStressChange() {
    }

    // This method must be modified by Katni - use constProperty, also change in cyclic dialog
    void propertiesChange() {
        for (int i = 0; i < TCEs.size(); i++) {
            ThermalControlElement tce = TCEs.get(i);
            GWT.log(newProperties.size() + "");
            Vector<Double> newProps = newProperties.get(i);
            tce.setConstProperty(Simulation.Property.DENSITY, newProps.get(0));
            tce.setConstProperty(Simulation.Property.SPECIFIC_HEAT_CAPACITY, newProps.get(1));
            tce.setConstProperty(Simulation.Property.THERMAL_CONDUCTIVITY, newProps.get(2));
        }
        GWT.log("Properties changed for component: " + String.valueOf(TCEs.get(0).index));
        GWT.log("Component k: " + String.valueOf(TCEs.get(0).cvs.get(0).constK));
    }


    void valueChange() {
        int steps = (int) (duration / sim.simulation1D.dt);
        for (int i = 0; i < changedValues.size(); i++) {
            Vector<PropertyValuePair> v = changedValues.get(i);
            for (PropertyValuePair pvp : v)
                for (ControlVolume cv : TCEs.get(i).cvs) {
                    if (duration == 0.0) {
                        cv.setProperty(pvp.property, pvp.value);
                    } else {
                        double currentValue = cv.getProperty(pvp.property);
                        double finalValue = pvp.value;
                        double changeInStep = (finalValue - currentValue) / steps;
                        cv.setProperty(pvp.property, changeInStep);
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
            int steps = (int) (duration / sim.simulation1D.dt);
            for (int i = 0; i < TCEs.size(); i++) {
                for (ControlVolume cv : TCEs.get(i).cvs) {
                    double changeInStep = (newTemperatures.get(i) - cv.temperature) / steps;
                    cv.temperature += changeInStep;
                    cv.temperatureOld += changeInStep;
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
                dump += newFieldIndexes.size() + " ";
                for (Integer i : newFieldIndexes)
                    dump += i + " ";
                break;
            case ELECTRIC_FIELD_CHANGE:
                break;
            case PRESSURE_CHANGE:
                break;
            case SHEAR_STRESS_CHANGE:
                break;
            case PROPERTIES_CHANGE:
                dump += newProperties.size() + " ";
                for (Vector<Double> v : newProperties)
                    for (Double d : v) {
                        dump += d + " ";
                    }
                break;
            case TEMPERATURE_CHANGE:
                dump += newTemperatures.size() + " ";
                for (Double t : newTemperatures)
                    dump += t + " ";

                break;
            case TOGGLE_THERMAL_CONTROL_ELEMENT:
                break;

            case VALUE_CHANGE:
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
                int numNewFieldIndexes = Integer.parseInt(st.nextToken());
                newFieldIndexes.clear();
                for (int i = 0; i < numNewFieldIndexes; i++) {
                    newFieldIndexes.add(Integer.parseInt(st.nextToken()));
                }
                break;
            case ELECTRIC_FIELD_CHANGE:
                break;
            case PRESSURE_CHANGE:
                break;
            case SHEAR_STRESS_CHANGE:
                break;
            case PROPERTIES_CHANGE:
                int numNewProperties = Integer.parseInt(st.nextToken());
                newProperties.clear();
                for (int i = 0; i < numNewProperties; i++) {
                    Vector<Double> v = new Vector<>();
                    v.add(Double.parseDouble(st.nextToken()));
                    v.add(Double.parseDouble(st.nextToken()));
                    v.add(Double.parseDouble(st.nextToken()));
                    newProperties.add(v);
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
            case VALUE_CHANGE:
                break;
        }
    }
}
