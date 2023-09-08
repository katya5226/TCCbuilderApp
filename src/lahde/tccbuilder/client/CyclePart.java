package lahde.tccbuilder.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.*;

import java.util.Vector;
import java.util.HashMap;
import java.lang.Math;

@SuppressWarnings("ReassignedVariable")
public class CyclePart {
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
        VALUE_CHANGE;

        @Override
        public String toString() {
            return this.name().toLowerCase().replace("_", " ");
        }

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

    Vector<ThermalControlElement> TCEs;
    Vector<Vector<Double>> newProperties;  // Vector<Double> for each component must have three values, for
    // rho, cp and k. In Cyclic dialog, the value of const_x (x = rho, cp or k) is set to -1 if a constant value needs not be set.
    Vector<HashMap<Simulation.Property, Double>> changedProperties;
    CirSim sim;
    double duration;

    public CyclePart(int index, CirSim sim) {
        partIndex = index;
        partType = PartType.HEAT_TRANSFER;
        TCEs = new Vector<ThermalControlElement>();
        newProperties = new Vector<Vector<Double>>();
        newTemperatures = new Vector<Double>();
        heatInputs = new Vector<Double>();
        newIndexes = new Vector<Integer>();
        fieldIndexes = new Vector<Integer>();
        changedProperties = new Vector<HashMap<Simulation.Property, Double>>();
        this.sim = sim;
        duration = 0;
    }

    public HTML toHTML() {
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
            flexTable.setText(row++, column, "all");

        if (!fieldIndexes.isEmpty()) {
            for (Integer index : fieldIndexes)
                flexTable.setText(row, column++, index.toString() + "T");
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
        }
        row++;
        column = 0;

        HTML html = new HTML();
        html.setHTML("<div>" + partIndex + ".\t" + partType.toSpacedCamelCase() + "</br>" + duration + "s </div>");
        html.setHTML(html.getHTML() + flexTable);

        html.setStyleName("cycle-part-outer");
        return html;
    }


    public void execute() {
        switch (partType) {
            case HEAT_TRANSFER:
                if (duration > sim.simulation1D.dt)
                    sim.simulation1D.heatTransferStep();
                break;
            case HEAT_INPUT:
                heatInput();
                break;
            case MECHANIC_DISPLACEMENT:
                mechanicDisplacement();
                break;
            case MAGNETIC_FIELD_CHANGE:
                magneticFieldChange();
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
            case VALUE_CHANGE:
                valueChange();
                if (duration > sim.simulation1D.dt)
                    sim.simulation1D.heatTransferStep();
            default:
                break;
        }
    }

    void heatInput() {
    }

    void mechanicDisplacement() {
    }

    /* void magneticFieldChange() {
        for (int i = 0; i < TCEs.size(); i++) {
            // Check if given component's' material's magnetocaloric flag is TRUE;
            // if not, abort and inform the user.
            TCEs.get(i).magnetize();
        }
    } */

    void magneticFieldChange() {
        if (duration == 0.0) {
            for (int i = 0; i < TCEs.size(); i++) {
                TCEs.get(i).magnetize();
            }
        } else if (duration > 0.0) {
            int steps = (int) (duration / sim.simulation1D.dt);
            for (ThermalControlElement tce : TCEs) {
                Vector<Double> dTheatcool = new Vector<Double>();
                dTheatcool = tce.field ? tce.material.dTcooling.get(tce.fieldIndex - 1) : tce.material.dTheating.get(tce.fieldIndex - 1);
                for (ControlVolume cv : tce.cvs) {
                    // Check if given cv's material's magnetocaloric flag is TRUE;
                    // if not, abort and inform the user.
                    int pos = (int) Math.round(cv.temperature * 10);
                    double dT = dTheatcool.get(pos);  // Here we would have to take the temperature at the begining of cycle pat,
                    // but I think the error is small if we do this.
                    double changeInStep = dT / steps;
                    cv.temperature = tce.field ? cv.temperature - changeInStep : cv.temperature + changeInStep;
                    cv.temperature = tce.field ? cv.temperatureOld - changeInStep : cv.temperatureOld + changeInStep;
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
            tce.setConstProperties(newProps);
        }
        GWT.log("Properties changed for component: " + String.valueOf(TCEs.get(0).index));
        GWT.log("Component k: " + String.valueOf(TCEs.get(0).cvs.get(0).constK));
    }

    void valueChange() {
        int steps = (int) (duration / sim.simulation1D.dt);
        for (int i = 0; i < changedProperties.size(); i++) {
            for (HashMap.Entry prop : changedProperties.get(i).entrySet()) {
                for (ControlVolume cv : TCEs.get(i).cvs) {
                    double currentValue = cv.getProperty((Simulation.Property) prop.getKey());
                    double finalValue = (double) prop.getValue();
                    double changeInStep = (finalValue - currentValue) / steps;
                    cv.setProperty((Simulation.Property) prop.getKey(), changeInStep);
                }
            }
        }

        // for (ThermalControlEleme222nt tce : TCEs) {
        //     for (HashMap props : changedProperties) {
        //         for (Map.Entry prop : changedProperties.entrySet()) {
        //             for (ControlVolume cv : tce.cvs) {
        //                 double currentValue = cv.getProperty(prop.getKey());
        //                 double finalValue = prop.getValue();
        //                 double changeInStep = (finalValue - currentValue) / steps;
        //                 cv.setProperty(prop.getKey(), changeInStep);
        //             }
        //         }
        //     }   
        // }
    }

}
