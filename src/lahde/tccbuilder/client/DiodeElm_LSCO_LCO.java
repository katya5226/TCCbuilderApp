package lahde.tccbuilder.client;

public class DiodeElm_LSCO_LCO extends DiodeElm {
    public DiodeElm_LSCO_LCO(int xx, int yy) {
        super(xx, yy);
        hasOperatingRange = true;
        operatingMin = 40;
        operatingMax = 99;
    }

    public DiodeElm_LSCO_LCO(int xa, int ya, int xb, int yb, int f,
                             StringTokenizer st) {
        super(xa, ya, xb, yb, f, st);
        hasOperatingRange = true;
        operatingMin = 40;
        operatingMax = 99;

    }

    @Override
    public EditInfo getEditInfo(int n) {
        switch (n) {
            default:
                return super.getEditInfo(n);
            case 12:
                EditInfo operatingRange = new EditInfo("Operating range", operatingMin + "-" + operatingMax);
                operatingRange.editable = false;
                return operatingRange;
        }
    }


}
