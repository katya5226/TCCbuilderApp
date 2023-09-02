package lahde.tccbuilder.client;

// import com.google.gwt.core.client.GWT;
// import com.google.gwt.user.client.Window;

// import java.util.*;
public class TwoDimBC {
    HeatSimProps.BC[] bcType;
    double[] T;
    double[] q;  // this will be changed with respect to 1D as it will not be an absolute value
    double[] h;

    public TwoDimBC(HeatSimProps.BC[] bt) {
        this.bcType = bt;
        T = new double[]{400.0, 200.0, 300.0, 300.0};
        q = new double[]{0.0, 0.0, 0.0, 0.0};
        h = new double[]{10000.0, 10000.0, 0.0, 0.0};
    }
}