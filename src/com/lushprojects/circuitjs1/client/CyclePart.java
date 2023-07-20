package com.lushprojects.circuitjs1.client;
import com.google.gwt.core.client.GWT;

import java.util.Vector;

public class CyclePart {
    int partIndex;
    int partType;
    Vector<Component> components;
    Vector<Vector<Double>> newProperties;  // Vector<Double> for each component must have three values, for
    // rho, cp and k. In Cyclic dialog, the value of const_x (x = rho, cp or k) is set to -1 if a constant value needs not be set.
    CirSim sim;
    double duration;

    public CyclePart(int index, CirSim sim) {
        this.partIndex = index;
        this.partType = 0;
        this.components = new Vector<Component>();
        this.newProperties = new Vector<Vector<Double>>();
        this.sim = sim;
        this.duration = 0;
    }

    public void execute() {
        switch (this.partType) {
            case 0:
                sim.heat_transfer_step();
                break;
            case 1:
                heatInput();
                break;
            case 2:
                mechanicDisplacement();
                break;
            case 3:
                magneticFieldChange();
                break;
            case 4:
                electricFieldChange();
                break;
            case 5:
                pressureChange();
                break;
            case 6:
                shearStressChange();
                break;
            case 7:
                propertiesChange();
                break;
            default:
                break;
        }
    }

    void heatInput() {
    }

    void mechanicDisplacement() {
    }

    void magneticFieldChange() {
        for (int i = 0; i < this.components.size(); i++) {
            // Check if given component's' material's magnetocaloric flag is TRUE;
            // if not, abort and inform the user.
            this.components.get(i).magnetize();
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
        for (int i = 0; i < this.components.size(); i++) {
            Component cp = this.components.get(i);
            Vector<Double> newProps = this.newProperties.get(i);
            cp.setConstProperties(newProps);
        }
        GWT.log("Properties changed for component: " + String.valueOf(this.components.get(0).index));
        GWT.log("Component k: " + String.valueOf(this.components.get(0).cvs.get(0).const_k));
    }

}
