package com.lushprojects.circuitjs1.client;

import java.util.Vector;

public class CyclePart {
    int partIndex;
    int partType;
    Vector<Component> components;
    CirSim sim;
    double duration;

    public CyclePart(int index, CirSim sim) {
        this.partIndex = index;
        this.partType = 0;
        this.components = new Vector<Component>();
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

            //components.get(i).magnetize();
        }
        sim.simComponents.get(0).magnetize();  // Katni test!!
    }

    void electricFieldChange() {
    }

    void pressureChange() {
    }

    void shearStressChange() {
    }

    void propertiesChange() {
    }

}
