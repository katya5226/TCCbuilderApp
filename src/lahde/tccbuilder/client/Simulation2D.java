package lahde.tccbuilder.client;


import com.google.gwt.core.client.GWT;
import lahde.tccbuilder.client.math3.linear.LUDecomposition;
import lahde.tccbuilder.client.math3.linear.RealVector;

import java.util.ArrayList;
import java.util.Vector;

public class Simulation2D extends Simulation {
    Vector<TwoDimComponent> simTwoDimComponents;
    TwoDimTCE twoDimTCE;
    TwoDimEqSys twoDimES;
    TwoDimBC twoDimBC;


    public Simulation2D() {
        westBoundary = BorderCondition.CONVECTIVE;
        eastBoundary = BorderCondition.CONVECTIVE;
        hWest = 100000.0;
        hEast = 100000.0;
        tempWest = 291.0;
        tempEast = 290.0;
        qWest = 0.0;
        qEast = 0.0;
        startTemp = 290.0;
        ambientTemperature = 293.0;
        times = new ArrayList<Double>();
        temperatures = new ArrayList<Double[]>();
        dt = 5e-3;
        totalTime = 0.0;
        time = 0.0;

    }

    @Override
    void makeTCC() {
        GWT.log("MAKING TCE");
        GWT.log(String.valueOf(simTwoDimComponents.size()));
        twoDimTCE = new TwoDimTCE("2D TCE", 0, simTwoDimComponents);
        twoDimTCE.buildTCE();
        twoDimES = new TwoDimEqSys(twoDimTCE, twoDimBC);
        TwoDimTCCmanager.setTemperatures(twoDimTCE.cvs, 300.0, true);
        setTemperatureRange();

        GWT.log(minTemp + " - " + maxTemp);
    }

    @Override
    void resetHeatSim() {

    }

    @Override
    void setTemperatureRange() {
        minTemp = Math.min(twoDimBC.T[0], twoDimBC.T[1]);
        maxTemp = Math.max(twoDimBC.T[0], twoDimBC.T[1]);
    }

    @Override
    public void heatTransferStep() {
        // GWT.log(String.valueOf(minTemp));
        // GWT.log(String.valueOf(maxTemp));
        twoDimES.conductionMatrix();
        LUDecomposition solver = new LUDecomposition(twoDimES.matrix);
        RealVector solutionVector = solver.getSolver().solve(twoDimES.rhs);
        for (int i = 0; i < twoDimTCE.cvs.size(); i++) {
            twoDimTCE.cvs.get(i).temperature = solutionVector.getEntry(i);
        }
        // TwoDimTCCmanager.printTemps(twoDimTCE.cvs);
        // update modes
        // calculate again
        // set new temperatures
        // replace old with new
        TwoDimTCCmanager.replaceOldNew(twoDimTCE.cvs);
        //this.append_new_temps();

    }

}
