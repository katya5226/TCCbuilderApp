package lahde.tccbuilder.client;

public class RegulatorElm_F02 extends RegulatorElm {

    final double DEFINED_LENGTH = 0.053;

    public RegulatorElm_F02(int xx, int yy) {
        super(xx, yy);
        DEFINED_LENGTH_UNIT = CirSim.LengthUnit.MILLIMETER;
        // sim.selectedLengthUnit = DEFINED_LENGTH_UNIT;
        length = DEFINED_LENGTH;
        // sim.scale.setSelectedIndex(CirSim.LengthUnit.MILLIMETER.ordinal());

        sim.calculateElementsLengths();

        int newX = sim.snapGrid((int) (xx + length * sim.selectedLengthUnit.conversionFactor * sim.gridSize));
        drag(newX, yy);
    }

    // RegulatorElm_F02(int xx, int yy, boolean mm) {
    //     super(xx, yy, mm);
    // }

    public RegulatorElm_F02(int xa, int ya, int xb, int yb, int f, StringTokenizer st) {
        super(xa, ya, xb, yb, f, st);
    }

    @Override
    public void initializeThermalControlElement() {
        super.initializeThermalControlElement();
        hasOperatingRange = false;
        operatingMin = 254;
        operatingMax = 353;
        resizable = false;
        length = DEFINED_LENGTH;
        k1 = 0.132;
        k2 = 16.2;
        rho1 = 4125;
        rho2 = 4125;
        cp1 = 2200;
        cp2 = 2200;
        responseTime = 180;
        temperature1 = 319;
        temperature2 = 336;
        latentHeat = 200000;
        inputPower = 0;
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
                return new EditInfo("Thermal Conductivity 1 (W/mK)", k1, false);
            case 8:
                return new EditInfo("Thermal Conductivity 2 (W/mK)", k2, false);
            case 9:
                return new EditInfo("Specific Heat Capacity 1 (J/kgK)", cp1, false);
            case 10:
                return new EditInfo("Specific Heat Capacity 2 (J/kgK)", cp2, false);
            case 11:
                return new EditInfo("Density 1 (kg/m³)", rho1, false);
            case 12:
                return new EditInfo("Density 2 (kg/m³)", rho2, false);
            case 13:
                return new EditInfo("Temperature 1 (K)", temperature1, false);
            case 14:
                return new EditInfo("Temperature 2 (K)", temperature2, false);
            case 15:
                return new EditInfo("Latent heat (J/kg)", latentHeat, false);
            case 16:
                return new EditInfo("Response time (s)", responseTime, false);
            case 17:
                return new EditInfo("Heat loss rate to the ambient (W/(m³K))", hTransv);
            case 18:
                return new EditInfo("Actuation input power (W)", inputPower, false);
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
                setNewLength(ei.value);
                break;
            case 5:
                westResistance = ei.value;
                break;
            case 6:
                eastResistance = ei.value;
                break;
            case 17:
                hTransv = ei.value;
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
        return 620;
    }



}