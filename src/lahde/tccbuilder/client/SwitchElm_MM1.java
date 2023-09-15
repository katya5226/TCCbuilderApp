package lahde.tccbuilder.client;

public class SwitchElm_MM1 extends SwitchElm {
    final double DEFINED_LENGTH = 0.011;

    public SwitchElm_MM1(int xx, int yy) {
        super(xx, yy);
        DEFINED_LENGTH_UNIT = CirSim.LengthUnit.MILLIMETER;
        sim.selectedLengthUnit = DEFINED_LENGTH_UNIT;
        length = DEFINED_LENGTH;
        sim.scale.setSelectedIndex(CirSim.LengthUnit.MILLIMETER.ordinal());

        sim.calculateElementsLengths();

        int newX = sim.snapGrid((int) (xx + length * sim.selectedLengthUnit.conversionFactor * sim.gridSize));
        drag(newX, yy);
    }

    SwitchElm_MM1(int xx, int yy, boolean mm) {
        super(xx, yy, mm);
    }

    public SwitchElm_MM1(int xa, int ya, int xb, int yb, int f, StringTokenizer st) {
        super(xa, ya, xb, yb, f, st);
    }

    @Override
    public void initializeThermalControlElement() {
        super.initializeThermalControlElement();
        hasOperatingRange = true;
        operatingMin = 254;
        operatingMax = 353;
        resizable = false;
        length = DEFINED_LENGTH;
        kOff = 5.09;
        kOn = 184.54;
        rhoOff = 1600;
        rhoOn = 1600;
        cpOff = 450;
        cpOn = 450;
        responseTime = -1;
        constCp = cpOff;
        constK = kOff;
        constRho = rhoOff;

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
                return new EditInfo("Length (" + sim.selectedLengthUnit.unitName + ")", length * CircuitElm.sim.selectedLengthUnit.conversionFactor);
            case 5:
                return new EditInfo("West contact resistance (mK/W)", westResistance);
            case 6:
                return new EditInfo("East contact resistance (mK/W)", eastResistance);
            case 7:
                return new EditInfo("Thermal Conductivity-On (W/m/K)", kOn, false);
            case 8:
                return new EditInfo("Thermal Conductivity-Off (W/m/K)", kOff, false);
            case 9:
                return new EditInfo("Specific Heat Capacity-On (J/kg/K)", cpOn, false);
            case 10:
                return new EditInfo("Specific Heat Capacity-Off (J/kg/K)", cpOff, false);
            case 11:
                return new EditInfo("Density-On (kg/m³)", rhoOn, false);
            case 12:
                return new EditInfo("Density-Off (kg/m³)", rhoOff, false);
            case 13:
                return new EditInfo("Response Time (s)", responseTime, false);
            default:
                return null;
        }
    }

    @Override
    public void setEditValue(int n, EditInfo ei) {
        Material m = null;
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
                setNewLength(ei.value);
                break;
            case 5:
                westResistance = ei.value;
                break;
            case 6:
                eastResistance = ei.value;
                break;
            default:
                break;
        }

        //TODO: Implement this with better functionality

        updateElement();
    }

    @Override
    public void calculateLength() {
        //super.calculateLength();
    }

    @Override
    int getDumpType() {
        return 611;
    }

    @Override
    void toggle() {
        super.toggle();
        if (position == 0) {
            constCp = cpOn;
            constK = kOn;
            constRho = rhoOn;
        } else {
            constCp = cpOff;
            constK = kOff;
            constRho = rhoOff;
        }
        buildThermalControlElement();
    }
}
