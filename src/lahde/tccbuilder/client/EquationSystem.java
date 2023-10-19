package lahde.tccbuilder.client;

import java.lang.Math;


public class EquationSystem {

    public static void conductionTridag(TCC circuit, double dt) {
        //double dt = circuit.parent_sim.dt;
        int n = circuit.cvs.size();
        for (int i = 0; i < n; i++) {
            ControlVolume cv = circuit.cvs.get(i);

            circuit.underdiag[i] = -cv.kWest * dt;
            circuit.diag[i] = (cv.kWest + cv.kEast) * dt + cv.rho() * cv.cp() * Math.pow(cv.dx, 2);
            circuit.upperdiag[i] = -cv.kEast * dt;
            circuit.rhs[i] = (cv.rho() * cv.cp() * Math.pow(cv.dx, 2)) * cv.temperatureOld + cv.qGenerated * dt * Math.pow(cv.dx, 2);

            if (i == 0 && circuit.westBoundary != null) {
                circuit.underdiag[0] = 0.0;
                circuit.diag[0] = cv.kEast * dt + cv.rho() * cv.cp() * Math.pow(cv.dx, 2);
                if (circuit.westBoundary == Simulation.BorderCondition.CONSTANT_HEAT_FLUX) {
                    circuit.rhs[0] = (cv.rho() * cv.cp() * Math.pow(cv.dx, 2)) * cv.temperatureOld;
                    circuit.rhs[0] += circuit.qWest * cv.dx * dt;
                    circuit.rhs[0] += cv.qGenerated * dt * Math.pow(cv.dx, 2);
                }
                if (circuit.westBoundary == Simulation.BorderCondition.CONSTANT_TEMPERATURE) {
                    // left resistance is approximated with the resistance of the first cv
                    circuit.diag[0] = (2 * cv.k() + cv.kEast) * dt + cv.rho() * cv.cp() * Math.pow(cv.dx, 2);
                    circuit.rhs[0] = (cv.rho() * cv.cp() * Math.pow(cv.dx, 2)) * cv.temperatureOld;
                    circuit.rhs[0] += 2 * cv.k() * dt * circuit.temperatureWest;
                    circuit.rhs[0] += cv.qGenerated * dt * Math.pow(cv.dx, 2);
                }

                if (circuit.westBoundary == Simulation.BorderCondition.CONVECTIVE) {  // left resistance is approximated with the resistance of the first cv
                    circuit.diag[0] = cv.kEast * dt;
                    circuit.diag[0] += cv.rho() * cv.cp() * Math.pow(cv.dx, 2);
                    circuit.diag[0] += 1.5 * circuit.hWest * cv.dx * dt;
                    circuit.upperdiag[0] = -cv.kEast * dt - 0.5 * circuit.hWest * cv.dx * dt;
                    circuit.rhs[0] = cv.rho() * cv.cp() * Math.pow(cv.dx, 2) * cv.temperatureOld;
                    circuit.rhs[0] += circuit.hWest * cv.dx * dt * circuit.temperatureWest;
                    circuit.rhs[0] += cv.qGenerated * dt * Math.pow(cv.dx, 2);
                }

            }
            if (i == n - 1 && circuit.eastBoundary != null) {
                circuit.diag[n - 1] = cv.kWest * dt + cv.rho() * cv.cp() * Math.pow(cv.dx, 2);
                circuit.upperdiag[n - 1] = 0;
                if (circuit.eastBoundary == Simulation.BorderCondition.CONSTANT_HEAT_FLUX) {
                    circuit.rhs[n - 1] = (cv.rho() * cv.cp() * Math.pow(cv.dx, 2)) * cv.temperatureOld;
                    circuit.rhs[n - 1] -= circuit.qEast * cv.dx * dt;
                    circuit.rhs[0] += cv.qGenerated * dt * Math.pow(cv.dx, 2);
                }
                if (circuit.eastBoundary == Simulation.BorderCondition.CONSTANT_TEMPERATURE) { // right resistance is approximated with the resistance of the last cv
                    circuit.diag[n - 1] = (2 * cv.k() + cv.kWest) * dt + cv.rho() * cv.cp() * Math.pow(cv.dx, 2);
                    circuit.rhs[n - 1] = (cv.rho() * cv.cp() * Math.pow(cv.dx, 2)) * cv.temperatureOld;
                    circuit.rhs[n - 1] += 2 * cv.k() * dt * circuit.temperatureEast;
                    circuit.rhs[n - 1] += cv.qGenerated * dt * Math.pow(cv.dx, 2);
                }
                if (circuit.eastBoundary == Simulation.BorderCondition.CONVECTIVE) { // left resistance is approximated with the resistance of the first cv
                    circuit.diag[n - 1] = cv.kWest * dt + cv.rho() * cv.cp() * Math.pow(cv.dx, 2);
                    circuit.diag[n - 1] += 1.5 * circuit.hEast * cv.dx * dt;
                    circuit.underdiag[n - 1] = -cv.kWest * dt - 0.5 * circuit.hEast * cv.dx * dt;
                    circuit.rhs[n - 1] = cv.rho() * cv.cp() * Math.pow(cv.dx, 2) * cv.temperatureOld;
                    circuit.rhs[n - 1] += circuit.hEast * cv.dx * dt * circuit.temperatureEast;
                    circuit.rhs[n - 1] += cv.qGenerated * dt * Math.pow(cv.dx, 2);
                }
            }

        }
    }
}
