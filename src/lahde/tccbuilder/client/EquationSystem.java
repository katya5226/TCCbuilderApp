package lahde.tccbuilder.client;

import java.lang.Math;
import com.google.gwt.user.client.Window;
import com.google.gwt.core.client.GWT;


public class EquationSystem {

    public static void conductionTridag(TCC circuit, double dt) {
        //double dt = circuit.parent_sim.dt;
        double a = 1.5;
        double b = 0.5;
        double wT = circuit.temperatureWest;
        double eT = circuit.temperatureEast;
        int n = circuit.cvs.size();
        for (int i = 0; i < n; i++) {
            ControlVolume cv = circuit.cvs.get(i);

            circuit.underdiag[i] = -cv.kWest * dt;
            circuit.diag[i] = (cv.kWest + cv.kEast) * dt + cv.rho() * cv.cp() * Math.pow(cv.dx, 2);
            circuit.upperdiag[i] = -cv.kEast * dt;
            circuit.rhs[i] = (cv.rho() * cv.cp() * Math.pow(cv.dx, 2)) * cv.temperatureOld + cv.qGen() * dt * Math.pow(cv.dx, 2);

            if (i == 0 && circuit.westBoundary != null) {
                circuit.underdiag[0] = 0.0;
                circuit.diag[0] = cv.kEast * dt + cv.rho() * cv.cp() * Math.pow(cv.dx, 2);
                if (circuit.westBoundary == Simulation.BorderCondition.CONSTANT_HEAT_FLUX) {
                    circuit.rhs[0] = (cv.rho() * cv.cp() * Math.pow(cv.dx, 2)) * cv.temperatureOld;
                    circuit.rhs[0] += circuit.qWest * cv.dx * dt;
                    circuit.rhs[0] += cv.qGen() * dt * Math.pow(cv.dx, 2);
                }
                if (circuit.westBoundary == Simulation.BorderCondition.CONSTANT_TEMPERATURE) {//} || circuit.westBoundary == Simulation.BorderCondition.PERIODIC) {
                    // left resistance is approximated with the resistance of the first cv
                    circuit.diag[0] = (2 * cv.k() + cv.kEast) * dt + cv.rho() * cv.cp() * Math.pow(cv.dx, 2);
                    circuit.rhs[0] = (cv.rho() * cv.cp() * Math.pow(cv.dx, 2)) * cv.temperatureOld;
                    circuit.rhs[0] += 2 * cv.k() * dt * circuit.temperatureWest;
                    circuit.rhs[0] += cv.qGen() * dt * Math.pow(cv.dx, 2);
                }

                if (circuit.westBoundary == Simulation.BorderCondition.CONVECTIVE) {  // left resistance is approximated with the resistance of the first cv
                    a = (2 * cv.dx + cv.eastNeighbour.dx) / (cv.dx + cv.eastNeighbour.dx);
                    b = cv.dx / (cv.dx + cv.eastNeighbour.dx);
                    circuit.diag[0] = cv.kEast * dt;
                    circuit.diag[0] += cv.rho() * cv.cp() * Math.pow(cv.dx, 2);
                    circuit.diag[0] += a * circuit.hWest * cv.dx * dt;
                    circuit.upperdiag[0] = -cv.kEast * dt - b * circuit.hWest * cv.dx * dt;
                    circuit.rhs[0] = cv.rho() * cv.cp() * Math.pow(cv.dx, 2) * cv.temperatureOld;
                    circuit.rhs[0] += circuit.hWest * cv.dx * dt * circuit.temperatureWest;
                    circuit.rhs[0] += cv.qGen() * dt * Math.pow(cv.dx, 2);
                }

                if (circuit.westBoundary == Simulation.BorderCondition.PERIODIC) {  // left resistance is approximated with the resistance of the first cv
                    wT = circuit.temperatureWest + circuit.amplitudeWest * Math.sin(circuit.frequencyWest * circuit.sim.time);
                    circuit.diag[0] = (2 * cv.k() + cv.kEast) * dt + cv.rho() * cv.cp() * Math.pow(cv.dx, 2);
                    circuit.rhs[0] = (cv.rho() * cv.cp() * Math.pow(cv.dx, 2)) * cv.temperatureOld;
                    circuit.rhs[0] += 2 * cv.k() * dt * wT;
                    circuit.rhs[0] += cv.qGen() * dt * Math.pow(cv.dx, 2);
                }

            }
            if (i == n - 1 && circuit.eastBoundary != null) {
                circuit.diag[n - 1] = cv.kWest * dt + cv.rho() * cv.cp() * Math.pow(cv.dx, 2);
                circuit.upperdiag[n - 1] = 0;
                if (circuit.eastBoundary == Simulation.BorderCondition.CONSTANT_HEAT_FLUX) {
                    circuit.rhs[n - 1] = (cv.rho() * cv.cp() * Math.pow(cv.dx, 2)) * cv.temperatureOld;
                    circuit.rhs[n - 1] -= circuit.qEast * cv.dx * dt;
                    circuit.rhs[0] += cv.qGen() * dt * Math.pow(cv.dx, 2);
                }
                if (circuit.eastBoundary == Simulation.BorderCondition.CONSTANT_TEMPERATURE){//} || circuit.eastBoundary == Simulation.BorderCondition.PERIODIC) { // right resistance is approximated with the resistance of the last cv
                    circuit.diag[n - 1] = (2 * cv.k() + cv.kWest) * dt + cv.rho() * cv.cp() * Math.pow(cv.dx, 2);
                    circuit.rhs[n - 1] = (cv.rho() * cv.cp() * Math.pow(cv.dx, 2)) * cv.temperatureOld;
                    circuit.rhs[n - 1] += 2 * cv.k() * dt * circuit.temperatureEast;
                    circuit.rhs[n - 1] += cv.qGen() * dt * Math.pow(cv.dx, 2);
                }
                if (circuit.eastBoundary == Simulation.BorderCondition.CONVECTIVE) { // left resistance is approximated with the resistance of the first cv
                    a = (2 * cv.dx + cv.westNeighbour.dx) / (cv.dx + cv.westNeighbour.dx);
                    b = cv.dx / (cv.dx + cv.westNeighbour.dx);
                    circuit.diag[n - 1] = cv.kWest * dt + cv.rho() * cv.cp() * Math.pow(cv.dx, 2);
                    circuit.diag[n - 1] += a * circuit.hEast * cv.dx * dt;
                    circuit.underdiag[n - 1] = -cv.kWest * dt - b * circuit.hEast * cv.dx * dt;
                    circuit.rhs[n - 1] = cv.rho() * cv.cp() * Math.pow(cv.dx, 2) * cv.temperatureOld;
                    circuit.rhs[n - 1] += circuit.hEast * cv.dx * dt * circuit.temperatureEast;
                    circuit.rhs[n - 1] += cv.qGen() * dt * Math.pow(cv.dx, 2);
                }
                if (circuit.eastBoundary == Simulation.BorderCondition.PERIODIC) { // left resistance is approximated with the resistance of the first cv
                    eT = circuit.temperatureEast + circuit.amplitudeEast * Math.sin(circuit.frequencyEast * circuit.sim.time);
                    circuit.diag[n - 1] = (2 * cv.k() + cv.kWest) * dt + cv.rho() * cv.cp() * Math.pow(cv.dx, 2);
                    circuit.rhs[n - 1] = (cv.rho() * cv.cp() * Math.pow(cv.dx, 2)) * cv.temperatureOld;
                    circuit.rhs[n - 1] += 2 * cv.k() * dt * eT;
                    circuit.rhs[n - 1] += cv.qGen() * dt * Math.pow(cv.dx, 2);
                }
            }
            circuit.diag[i] += cv.hTransv * Math.pow(cv.dx, 2) * dt;
            circuit.rhs[i] += cv.hTransv * Math.pow(cv.dx, 2) * dt * circuit.ambientTemperature;
        }
        //Thermoelectricity
        for (ThermalControlElement tce : circuit.TCEs) {
            if (tce instanceof TEComponent) {
                TEComponent thermoelectric = (TEComponent) tce;
                if (thermoelectric.crossArea == -1) {
                    Window.alert("Thermoelectric cross area invalid! Setting value to 1 mm²");
                    thermoelectric.crossArea = 1.0;
                }
                ModelMethods.CVinterface cvInter = new ModelMethods.CVinterface();
                int teStartIndex = thermoelectric.cvs.get(0).globalIndex;
                if (teStartIndex == 0) Window.alert("Thermoelectric element start index invalid!");
                int teEndIndex = thermoelectric.cvs.get(thermoelectric.cvs.size() - 1).globalIndex;
                if (teEndIndex >= circuit.cvs.size() - 1) Window.alert("Thermoelectric element end index invalid!");
                for (int i = teStartIndex; i < teEndIndex + 1; i++) {
                    circuit.rhs[i] += circuit.cvs.get(i).elResistivity() * Math.pow(1.0e6 * thermoelectric.elCurrent / thermoelectric.crossArea, 2) * Math.pow(circuit.cvs.get(i).dx, 2) * dt;  // Joule heating
                    double grad = cvInter.temperatureGradient(circuit.cvs.get(i));
                    circuit.diag[i] -= circuit.cvs.get(i).seebeckGradient() * grad * (1.0e6 * thermoelectric.elCurrent / thermoelectric.crossArea) * Math.pow(circuit.cvs.get(i).dx, 2) * dt;
                }
                cvInter.calculateCoefficients(circuit.cvs.get(teStartIndex - 1), circuit.cvs.get(teStartIndex));
                // GWT.log(String.valueOf(thermoelectric.cvs.get(0).seeb(cvInter.T2)));
                double peltier = thermoelectric.cvs.get(0).seeb(cvInter.T2) * (1.0e6 * thermoelectric.elCurrent / thermoelectric.crossArea) * dt;
                circuit.diag[teStartIndex] -= cvInter.b2 * peltier * circuit.cvs.get(teStartIndex).dx;
                circuit.diag[teStartIndex - 1] -= cvInter.a2 * peltier * circuit.cvs.get(teStartIndex - 1).dx;
                cvInter.calculateCoefficients(circuit.cvs.get(teEndIndex), circuit.cvs.get(teEndIndex + 1));

                peltier = thermoelectric.cvs.get(thermoelectric.cvs.size() - 1).seeb(cvInter.T1) * (1.0e6 * thermoelectric.elCurrent / thermoelectric.crossArea) * dt;
                circuit.diag[teEndIndex] += cvInter.b1 * peltier * circuit.cvs.get(teEndIndex).dx;
                circuit.diag[teEndIndex + 1] += cvInter.c1 * peltier * circuit.cvs.get(teEndIndex + 1).dx;
            }
        }

    }
}
