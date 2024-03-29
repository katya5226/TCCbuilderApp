package lahde.tccbuilder.client;


import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import lahde.tccbuilder.client.math3.linear.LUDecomposition;
import lahde.tccbuilder.client.math3.linear.RealVector;
import lahde.tccbuilder.client.math3.linear.OpenMapRealVector;

import java.util.ArrayList;
import java.util.Vector;

public class Simulation2D extends Simulation {
    Vector<TwoDimComponent> simTwoDimComponents;
    TwoDimTCE twoDimTCE;
    TwoDimEqSys twoDimES;
    TwoDimBC twoDimBC;
    RealVector solutionVector;
    LUDecomposition solver;
    // DMatrixRMaj x;


    public Simulation2D() {
        simTwoDimComponents = new Vector<TwoDimComponent>();
        twoDimBC = new TwoDimBC(BorderCondition.CONVECTIVE);
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

        customTempRange = false;

    }

    @Override
    void makeTCC() {
        GWT.log("MAKING TCE");
        GWT.log(String.valueOf(simTwoDimComponents.size()));
        twoDimTCE = new TwoDimTCE("2D TCE", 0, simTwoDimComponents);
        twoDimTCE.buildTCE();
        twoDimES = new TwoDimEqSys(twoDimTCE, twoDimBC);
        TwoDimTCCmanager.setTemperatures(twoDimTCE.cvs, startTemp, true);
        setTemperatureRange();

        time = 0;
        solutionVector = new OpenMapRealVector(twoDimTCE.cvs.size());
        // x = new DMatrixRMaj(twoDimTCE.cvs.size());

    }

    @Override
    void resetHeatSim() {

    }

    @Override
    void setTemperatureRange() {
        if (customTempRange == true) return;
        minTemp = Math.min(startTemp, Math.min(twoDimBC.T[0], twoDimBC.T[1]));
        maxTemp = Math.max(startTemp, Math.max(twoDimBC.T[0], twoDimBC.T[1]));
    }

    @Override
    String dump() {
        return null;
    }

    @Override
    void undump(StringTokenizer stringTokenizer) {

    }

    @Override
    String getReport() {
        String dump = "Data directory: " + "/materials\n" + "Time step dt: " + dt + "\n" + "Dimensionality: 2D\n" + "West boundary condition: " + westBoundary + "\n" + "East Boundary condition on the right: " + eastBoundary + "\n" + "West temperature: " + " K\n" + "West convection coefficient: " + " W/(m²K)\n" + "East temperature: " + " K\n" + "East convection coefficient: " + " W/(m²K)\n";

        dump += "\nThermal control elements: \n";
        dump += "\nTCE: " + twoDimTCE.name + "\n";
        dump += "Components: \n";
        for (TwoDimComponent component : twoDimTCE.components) {
            dump += "Component name: " + component.name + "\n" + "Component index: " + component.index + "\n" + "Material: " + component.material.materialName + "\n" + "X-discretizaton number:  " + component.n + "\n" + "Y-discretizaton number:  " + component.m + "\n" + "Control volume length: " + CirSim.formatLength(component.cvs.get(0).dx) + "\n" + "Control volume height: " + CirSim.formatLength(component.cvs.get(0).dy) + "\n" + "Constant density: " + (component.cvs.get(0).constRho == -1 ? "not set" : component.cvs.get(0).constRho) + "kg/m³\n" + "Constant specific heat: " + (component.cvs.get(0).constCp == -1 ? "not set" : component.cvs.get(0).constCp) + "J/(kgK)\n" + "Constant thermal conductivity: " + (component.cvs.get(0).constK == -1 ? "not set" : component.cvs.get(0).constK) + " W/(mK)\n" + "West contact resistance: " + component.resistances[0] + "m²K/W\n" + "East contact resistance: " + component.resistances[1] + "m²K/W\n" + "South contact resistance: " + component.resistances[2] + "m²K/W\n" + "North contact resistance: " + component.resistances[3] + "m²K/W\n" + "Generated heat: " + 0.0 + "W/m²\n\n";
        }
        dump += "\nTemperatures at " + NumberFormat.getFormat("0.00").format(time) + "s\n";
        Vector<TwoDimCV> cvs = twoDimTCE.cvs;
        for (int i = 0; i < cvs.size(); i++) {
            TwoDimCV cv = cvs.get(i);
            if (i % twoDimTCE.n == 0) dump += "\n";
            dump += NumberFormat.getFormat("0.00").format(cv.temperature) + "\t";
        }
        dump += "\nfw";
        for (int i = 0; i < twoDimTCE.n - 1; i++) {
            double flow = 0.0;
            for (int j = 0; j < twoDimTCE.m; j++) {
                TwoDimCV cv = cvs.get(j * twoDimTCE.n + i);
                TwoDimCV cve = cvs.get(j * twoDimTCE.n + i + 1);
                flow += (cv.kd[1] * (cv.temperature - cve.temperature) / cv.dx) / twoDimTCE.m;
            }
            dump += "\nflow(" + i + ")=" + NumberFormat.getFormat("0.0000").format(flow);

        }
        dump += "\nfe\n";
        return dump;
    }

    @Override
    String printTCEs() {
        String tces = "";
        for (TwoDimComponent twoDimComponent : simTwoDimComponents) {
            tces += twoDimComponent.index + (twoDimComponent == simTwoDimComponents.get(simTwoDimComponents.size() - 1) ? "" : ", ");
        }
        return tces;
    }

    @Override
    public void heatTransferStep() {
        // GWT.log(String.valueOf(minTemp));
        // GWT.log(String.valueOf(maxTemp));
        twoDimES.conductionMatrix();

        // Apache commons library
        solver = new LUDecomposition(twoDimES.matrix);
        solutionVector = solver.getSolver().solve(twoDimES.rhs);
        for (int i = 0; i < twoDimTCE.cvs.size(); i++) {
            twoDimTCE.cvs.get(i).temperature = solutionVector.getEntry(i);
        }

        //Ejml Library
        // DMatrixRMaj x = new DMatrixRMaj(twoDimTCE.cvs.size());
        // boolean solved = CommonOps_DSCC.solve(twoDimES.matrixEjml, twoDimES.rhsEjml, x);
        // for (int i = 0; i < twoDimTCE.cvs.size(); i++) {
        //     twoDimTCE.cvs.get(i).temperature = x.get(i);
        // }


        time+=dt;
        // TwoDimTCCmanager.printTemps(twoDimTCE.cvs);
        // update modes
        // calculate again
        // set new temperatures
        // replace old with new
        TwoDimTCCmanager.replaceOldNew(twoDimTCE.cvs);
        //this.append_new_temps();

    }

}
