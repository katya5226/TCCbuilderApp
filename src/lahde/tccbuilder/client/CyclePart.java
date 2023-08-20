package lahde.tccbuilder.client;

import com.google.gwt.core.client.GWT;

import java.util.Vector;

public class CyclePart {
    public enum PartType {
        HEAT_TRANSFER,
        HEAT_INPUT,
        MECHANIC_DISPLACEMENT,
        MAGNETIC_FIELD_CHANGE,
        ELECTRIC_FIELD_CHANGE,
        PRESSURE_CHANGE,
        SHEAR_STRESS_CHANGE,
        PROPERTIES_CHANGE;

        @Override
        public String toString() {
            return this.name().toLowerCase().replace("_", " ");
        }
    }

    int partIndex;
    PartType partType;
    Vector<ThermalControlElement> tces;
    Vector<Vector<Double>> newProperties;  // Vector<Double> for each component must have three values, for
    // rho, cp and k. In Cyclic dialog, the value of const_x (x = rho, cp or k) is set to -1 if a constant value needs not be set.
    CirSim sim;
    double duration;

    public CyclePart(int index, CirSim sim) {
        this.partIndex = index;
        this.partType = PartType.HEAT_TRANSFER;
        this.tces = new Vector<ThermalControlElement>();
        this.newProperties = new Vector<Vector<Double>>();
        this.sim = sim;
        this.duration = 0;
    }

    public void execute() {
        switch (this.partType) {
            case HEAT_TRANSFER:
                sim.heat_transfer_step();
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
            default:
                break;
        }
    }

    void heatInput() {
    }

    void mechanicDisplacement() {
    }

    void magneticFieldChange() {
        for (int i = 0; i < tces.size(); i++) {
            // Check if given component's' material's magnetocaloric flag is TRUE;
            // if not, abort and inform the user.
            tces.get(i).magnetize();
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
            GWT.log(this.newProperties.size()+"");
            Vector<Double> newProps = this.newProperties.get(i);
            cp.setConstProperties(newProps);
        }
        GWT.log("Properties changed for component: " + String.valueOf(tces.get(0).index));
        GWT.log("Component k: " + String.valueOf(tces.get(0).controlVolumes.get(0).constK));
    }

}
