package lahde.tccbuilder.client;

public class DiodeElm_SS_02 extends DiodeElm {
    final double DEFINED_LENGTH = 0.004;

    public DiodeElm_SS_02(int xx, int yy) {
        super(xx, yy);
        DEFINED_LENGTH_UNIT = CirSim.LengthUnit.MILLIMETER;
        // sim.selectedLengthUnit = DEFINED_LENGTH_UNIT;
        length = DEFINED_LENGTH;
        // sim.scale.setSelectedIndex(CirSim.LengthUnit.MILLIMETER.ordinal());
        sim.calculateElementsLengths();

        int newX = sim.snapGrid((int) (xx + length * sim.selectedLengthUnit.conversionFactor * sim.gridSize));
        drag(newX, yy);
    }

    public DiodeElm_SS_02(int xa, int ya, int xb, int yb, int f,
                                  StringTokenizer st) {
        super(xa, ya, xb, yb, f, st);

    }

    @Override
    public void initializeThermalControlElement() {
        super.initializeThermalControlElement();
        hasOperatingRange = true;
        operatingMin = 290;
        operatingMax = 450;
        resizable = false;
        length = DEFINED_LENGTH;
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
            // case 8:
            //     return new EditInfo("Heat loss rate to the ambient (W/(m³K))", hTransv);
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
            // case 8:
            //     hTransv = ei.value;   
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
    public void buildThermalControlElement() {
        super.buildThermalControlElement();
        Material NiTi = sim.materialHashMap.get("100004-NiTi");
        Material Graphite = sim.materialHashMap.get("500006-Graphite");
        if (!NiTi.isLoaded())
            NiTi.readFiles();
        if (!Graphite.isLoaded())
            Graphite.readFiles();
        int ratioIndex = (int) (0.5 * numCvs);
        if (direction == CircuitElm.Direction.RIGHT) {
            for (int i = 0; i < numCvs; i++) {
                cvs.get(i).material = i < ratioIndex ? NiTi : Graphite;
            }
        }
        if (direction == CircuitElm.Direction.LEFT) {
            for (int i = 0; i < numCvs; i++) {
                cvs.get(i).material = i < (numCvs - ratioIndex) ? Graphite : NiTi;
            }
        }
    }

    @Override
        public void checkDirection(double boundaryTw, double boundaryTe) {
        return;
    }

    @Override
    int getDumpType() {
        return 601;
    }
}
