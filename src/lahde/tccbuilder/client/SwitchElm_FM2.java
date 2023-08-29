package lahde.tccbuilder.client;

public class SwitchElm_FM2 extends SwitchElm{
    public SwitchElm_FM2(int xx, int yy) {
        super(xx, yy);
    }

    SwitchElm_FM2(int xx, int yy, boolean mm) {
        super(xx, yy, mm);
    }

    public SwitchElm_FM2(int xa, int ya, int xb, int yb, int f, StringTokenizer st) {
        super(xa, ya, xb, yb, f, st);
    }
    @Override
    int getDumpType() {
        return 611;
    }
}
