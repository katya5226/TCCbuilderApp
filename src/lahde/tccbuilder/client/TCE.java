package lahde.tccbuilder.client;

import java.util.Vector;
import java.util.Collections;

public class TCE implements Comparable<TCE> {
    public TCC parentCircuit;
    public String name;
    public int index;
    public Vector<Component> components;
    public int numComponents;
    public int numCvs;
    public Vector<ControlVolume> cvs;
    public TCE westNeighbour;
    public TCE eastNeighbour;
    public double[] underdiag;
    public double[] diag;
    public double[] upperdiag;
    public double[] rhs;
    public int westBoundary;
    public int eastBoundary;
    public double qWest, qEast;
    public double temperatureWest, temperatureEast;
    public double hWest, hEast;

    public TCE(String name, int index, Vector<Component> components) {
        parentCircuit = null;
        this.name = name;
        this.index = index;
        this.components = components;
        numComponents = components.size();
        numCvs = 0;
        for (Component c : components) {
            numCvs += c.numCvs;
        }
        cvs = new Vector<ControlVolume>();
        westNeighbour = this;
        eastNeighbour = this;
        underdiag = new double[numCvs];
        diag = new double[numCvs];
        upperdiag = new double[numCvs];
        rhs = new double[numCvs];
        westBoundary = 51;
        eastBoundary = 52;
        qWest = 0.0;
        qEast = 0.0;
        temperatureWest = 0.0;
        temperatureEast = 0.0;
        hWest = 500.0;
        hEast = 500.0;
    }

    @Override
    public int compareTo(TCE e) {
        return index - e.index;
    }

    public void setNeighbours() {
        if (numCvs > 1)
            for (int i = 0; i < numCvs; i++) {
                ControlVolume cv = cvs.get(i);
                if (i != 0 && i != numCvs - 1) {
                    cv.westNeighbour = cvs.get(i - 1);
                    cv.eastNeighbour = cvs.get(i + 1);
                }
                if (i == 0) {
                    cv.eastNeighbour = cvs.get(1);
                }
                if (i == numCvs - 1) {
                    cv.westNeighbour = cvs.get(numCvs - 2);
                }
            }
    }

    public void buildTCE() {
        Collections.sort(components);
        int num_components1 = numComponents;
        int num_cvs1 = components.get(num_components1 - 1).numCvs;
        if (westNeighbour == null) {
            components.get(0).westNeighbour = null;
            components.get(0).cvs.get(0).westNeighbour = components.get(0).cvs.get(0);
        } else {
            int m1 = westNeighbour.numComponents;
            int m2 = westNeighbour.components.get(m1 - 1).numCvs;
            components.get(0).westNeighbour = westNeighbour.components.get(m1 - 1);
            components.get(0).cvs.get(0).westNeighbour = westNeighbour.components.get(m1 - 1).cvs.get(m2 - 1);
        }

        if (eastNeighbour == null) {
            components.get(num_components1 - 1).eastNeighbour = null;
            components.get(num_components1 - 1).cvs.get(num_cvs1 - 1).eastNeighbour = components.get(num_components1 - 1).cvs.get(num_cvs1 - 1);
        } else {
            components.get(num_components1 - 1).eastNeighbour = eastNeighbour.components.get(0);
            components.get(num_components1 - 1).cvs.get(num_cvs1 - 1).eastNeighbour = eastNeighbour.components.get(0).cvs.get(0);
        }

        for (int i = 0; i < num_components1 - 1; i++) {
            components.get(i).eastBoundary = 52;
            components.get(i).eastNeighbour = components.get(i + 1);
            components.get(i + 1).westNeighbour = components.get(i);
        }
        for (int i = 1; i < num_components1; i++) {
            components.get(i).westBoundary = 51;
            int l = components.get(i - 1).numCvs;
            components.get(i - 1).cvs.get(l - 1).eastNeighbour = components.get(i).cvs.get(0);
            components.get(i).cvs.get(0).westNeighbour = components.get(i - 1).cvs.get(l - 1);
        }
        cvs.clear();
        int TCE_index = 0;
        components.get(0).westBoundary = westBoundary;
        for (int i = 0; i < num_components1; i++) {
            components.get(num_components1 - 1).eastBoundary = eastBoundary;
            components.get(i).cvs.get(0).westResistance = components.get(i).westResistance;
            components.get(i).cvs.get(components.get(i).numCvs - 1).eastResistance = components.get(i).eastResistance;
            for (int j = 0; j < components.get(i).numCvs; j++) {
                components.get(i).cvs.get(j).TCEIndex = TCE_index;
                cvs.add(components.get(i).cvs.get(j));
                TCE_index++;
            }
        }
        setNeighbours();
    }

    public void calculateConductivities() {
        for (ControlVolume cv : cvs)
            cv.calculateConductivities();
    }

    public void setStartingTemperatures(double[] start_temps) {
        for (int i = 0; i < numCvs; i++) {
            cvs.get(i).temperature = start_temps[i];
            cvs.get(i).temperatureOld = start_temps[i];
        }
    }

    public void replaceTemperatures() {
        for (ControlVolume cv : cvs)
            cv.temperatureOld = cv.temperature;
    }

}

