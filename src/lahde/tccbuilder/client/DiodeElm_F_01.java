package lahde.tccbuilder.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

public class DiodeElm_F_01 extends DiodeElm {
    final double DEFINED_LENGTH = 0.014;

    public DiodeElm_F_01(int xx, int yy) {
        super(xx, yy);
        // DEFINED_LENGTH_UNIT = CirSim.LengthUnit.MILLIMETER;
        // sim.selectedLengthUnit = DEFINED_LENGTH_UNIT;
        length = DEFINED_LENGTH;
        // sim.scale.setSelectedIndex(CirSim.LengthUnit.MILLIMETER.ordinal());
        sim.calculateElementsLengths();

        int newX = sim.snapGrid((int) (xx + length * sim.selectedLengthUnit.conversionFactor * sim.gridSize));
        drag(newX, yy);
    }


    public DiodeElm_F_01(int xa, int ya, int xb, int yb, int f,
                             StringTokenizer st) {
        super(xa, ya, xb, yb, f, st);
    }

    @Override
    public EditInfo getEditInfo(int n) {
        switch (n) {
            case 0:
                return new EditInfo("Name", String.valueOf(name));
            case 1:
                return new EditInfo("Index", index);
            case 2:
                return new EditInfo("Number of control volumes", (double) numCvs);
            case 3:
                EditInfo ei2 = new EditInfo("Color", 0);
                ei2.choice = new Choice();
                for (int ch = 0; ch < sim.colorChoices.size(); ch++) {
                    ei2.choice.add(sim.colorChoices.get(ch));
                }

                ei2.choice.select(Color.colorToIndex(color));
                return ei2;
            case 4:
                // EditInfo editInfo = new EditInfo("Length (" + sim.selectedLengthUnit.unitName + ")", length * CircuitElm.sim.selectedLengthUnit.conversionFactor);
                EditInfo editInfo = new EditInfo("Length", CirSim.formatLength(length));
                editInfo.editable = resizable;
                return editInfo;
            case 5:
                return new EditInfo("West contact resistance (m²K/W)", westResistance);
            case 6:
                return new EditInfo("East contact resistance (m²K/W)", eastResistance);
            case 7:
                EditInfo operatingRange = new EditInfo("Operating range", operatingMin + "K - " + operatingMax + "K");
                operatingRange.editable = false;
                return operatingRange;
            case 8:
                return new EditInfo("Heat loss rate to the ambient (W/(m³K))", hTransv);
            case 9:
                EditInfo ei3 = EditInfo.createButton("Change direction");
                ei3.button.addClickHandler(new ClickHandler() {
                    public void onClick(ClickEvent event) {
                        int xx = x; x = x2; x2 = xx;
                        if (direction == CircuitElm.Direction.RIGHT) direction = CircuitElm.Direction.LEFT;
                        else if (direction == CircuitElm.Direction.LEFT) direction = CircuitElm.Direction.RIGHT;
                    }
                });
                return ei3;
            default:
                return null;
        }
    }

    @Override
    public void setEditValue(int n, EditInfo ei) {
        switch (n) {
            case 0:
                name = ei.textf.getText();
                break;
            case 1:
                index = (int) ei.value;
                break;
            case 2:
                numCvs = (int) ei.value;
                break;
            case 3:
                color = Color.translateColorIndex(ei.choice.getSelectedIndex());
                break;
            case 4:
                // setNewLength(ei.value);
                break;
            case 5:
                westResistance = ei.value;
                break;
            case 6:
                eastResistance = ei.value;
                break;
            case 8:
                hTransv = ei.value;
                break;
            default:
                break;

        }

        updateElement();
    }
/*                case 12:
    EditInfo operatingRange = new EditInfo("Operating range", operatingMin + "K - " + operatingMax + "K");
    operatingRange.editable = false;
                return operatingRange;*/

    @Override
    public void initializeThermalControlElement() {
        super.initializeThermalControlElement();
        hasOperatingRange = true;
        operatingMin = 303;
        operatingMax = 318;
        resizable = false;
        length = DEFINED_LENGTH;
        kForward = 0.2;
        kReverse = 0.1;
        setConstProperty(Simulation.Property.DENSITY, 1030.0);
        setConstProperty(Simulation.Property.SPECIFIC_HEAT_CAPACITY, 4200.0);
    }


    @Override
    public void calculateLength() {
        //super.calculateLength();
    }

    @Override
    int getDumpType() {
        return 602;
    }
}
