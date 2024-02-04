package lahde.tccbuilder.client;

// This diode is from Zhang et al., Applied Energy 280 (2020) 115881
// It has analytically defined thermal conductivity.
public class DiodeElm_T_01 extends DiodeElm {

    double k0;
    double beta;
    double gamma;

    public DiodeElm_T_01(int xx, int yy) {
        super(xx, yy);
    }

    public DiodeElm_T_01(int xa, int ya, int xb, int yb, int f, StringTokenizer st) {
        super(xa, ya, xb, yb, f, st);
    }

    @Override
    public void initializeThermalControlElement() {
        super.initializeThermalControlElement();
        k0 = 1.0;
        beta = 0.0;
        gamma = 1.0;
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
                EditInfo editInfo = new EditInfo("Length (mm)", length * 1e3);
                editInfo.editable = resizable;
                return editInfo;
            case 5:
                return new EditInfo("West contact resistance (m²K/W)", westResistance);
            case 6:
                return new EditInfo("East contact resistance (m²K/W)", eastResistance);
            case 7:
                return new EditInfo("Base thermal conductivity (W/(mK))", k0);
            case 8:
                return new EditInfo("Level of thermal rectification β (/)", beta);
            case 9:
                return new EditInfo("Steepness of thermal rectification γ (1/K)", gamma);
            case 10:
                return new EditInfo("Specific Heat Capacity (J/kgK)", constCp);
            case 11:
                return new EditInfo("Density (kg/m³)", constRho);
            // case 10:
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
            case 7:
                k0 = ei.value;
                break;
            case 8:
                beta = ei.value;
                break;
            case 9:
                gamma = ei.value;
                break;
            case 10:
                constRho = ei.value;
                break;
            case 11:
                responseTime = ei.value;
                break;
            // case 10:
            //     hTransv = ei.value;   
            default:
                break;


        }


        updateElement();
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
        return 604;
    }
}
