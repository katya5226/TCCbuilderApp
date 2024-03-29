package lahde.tccbuilder.client;

public class SwitchElm_SSE_01 extends SwitchElm {
    final double DEFINED_LENGTH = 0.0002;

    public SwitchElm_SSE_01(int xx, int yy) {
        super(xx, yy);
        DEFINED_LENGTH_UNIT = CirSim.LengthUnit.MICROMETER_50;
        // sim.selectedLengthUnit = DEFINED_LENGTH_UNIT;
        length = DEFINED_LENGTH;
        // sim.scale.setSelectedIndex(CirSim.LengthUnit.MILLIMETER.ordinal());

        sim.calculateElementsLengths();

        int newX = sim.snapGrid((int) (xx + length * sim.selectedLengthUnit.conversionFactor * sim.gridSize));
        drag(newX, yy);
    }

    SwitchElm_SSE_01(int xx, int yy, boolean mm) {
        super(xx, yy, mm);
    }

    public SwitchElm_SSE_01(int xa, int ya, int xb, int yb, int f, StringTokenizer st) {
        super(xa, ya, xb, yb, f, st);
    }

    @Override
    public void initializeThermalControlElement() {
        super.initializeThermalControlElement();
        hasOperatingRange = true;
        operatingMin = 300;
        operatingMax = 500;
        resizable = true;
        length = DEFINED_LENGTH;
        kOff = 1.0;
        kOn = 5.0;
        rhoOff = 6020;
        rhoOn = 6020;
        cpOff = 439;
        cpOn = 439;
        responseTime = 0;
        constCp = cpOff;
        constK = kOff;
        constRho = rhoOff;
        inputPower = 0;
        crossArea = 0.0001;  // Unknown
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
                // return new EditInfo("Length (" + sim.selectedLengthUnit.unitName + ")", length * CircuitElm.sim.selectedLengthUnit.conversionFactor);
                return new EditInfo("Length (mm)", length * 1e3);
            case 5:
                return new EditInfo("West contact resistance (m²K/W)", westResistance);
            case 6:
                return new EditInfo("East contact resistance (m²K/W)", eastResistance);
            case 7:
                return new EditInfo("Thermal Conductivity (W/m/K) - ON", kOn, false);
            case 8:
                return new EditInfo("Thermal Conductivity (W/m/K) - OFF", kOff, false);
            case 9:
                return new EditInfo("Specific Heat Capacity (J/kg/K) - ON", cpOn, false);
            case 10:
                return new EditInfo("Specific Heat Capacity (J/kg/K) - OFF", cpOff, false);
            case 11:
                return new EditInfo("Density (kg/m³) - ON", rhoOn, false);
            case 12:
                return new EditInfo("Density (kg/m³) - OFF", rhoOff, false);
            case 13:
                return new EditInfo("Response Time (s) - unknown but very short", responseTime, false);
            case 14:
                return new EditInfo("Heat loss rate to the ambient (W/(m³K))", hTransv);
            case 15:
                return new EditInfo("Actuation input power unknown", inputPower, false);
            default:
                return null;
        }
    }

    @Override
    public void setEditValue(int n, EditInfo ei) {
        Material m  = null;
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
            case 14:
                hTransv = ei.value;
                break;
            default:
                break;
        }
        updateElement();
    }

    @Override
    public void calculateLength() {
        //super.calculateLength();
    }

    @Override
    int getDumpType() {
        return 615;
    }



}
