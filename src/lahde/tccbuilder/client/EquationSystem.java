package lahde.tccbuilder.client;

import java.lang.Math;


public class EquationSystem {

    public static void conductionTridag(TCC circuit, double dt) {
        //double dt = circuit.parent_sim.dt;
        int n = circuit.num_cvs;
        for (int i = 0; i < n; i++) {
            ControlVolume cv = circuit.cvs.get(i);

            circuit.underdiag[i] = -cv.k_left * dt;
            circuit.diag[i] = (cv.k_left + cv.k_right) * dt + cv.rho() * cv.cp() * Math.pow(cv.dx, 2);
            circuit.upperdiag[i] = -cv.k_right * dt;
            circuit.rhs[i] = (cv.rho() * cv.cp() * Math.pow(cv.dx, 2)) * cv.temperature_old + cv.get_q_gen() * dt * Math.pow(cv.dx, 2);

            if (i == 0 && circuit.left_boundary != 0) {
                circuit.underdiag[0] = 0.0;
                circuit.diag[0] = cv.k_right * dt + cv.rho() * cv.cp() * Math.pow(cv.dx, 2);
                if (circuit.left_boundary == 21) {
                    circuit.rhs[0] = (cv.rho() * cv.cp() * Math.pow(cv.dx, 2)) * cv.temperature_old;
                    circuit.rhs[0] += circuit.getQ_in() * cv.dx * dt;
                    circuit.rhs[0] += cv.get_q_gen() * dt * Math.pow(cv.dx, 2);
                }
                if (circuit.left_boundary == 31) {
                    // left resistance is approximated with the resistance of the first cv
                    circuit.diag[0] = (cv.k() + cv.k_right) * dt + cv.rho() * cv.cp() * Math.pow(cv.dx, 2);
                    circuit.rhs[0] = (cv.rho() * cv.cp() * Math.pow(cv.dx, 2)) * cv.temperature_old;
                    circuit.rhs[0] += cv.k() * dt * circuit.getTemp_left();
                    circuit.rhs[0] += cv.get_q_gen() * dt * Math.pow(cv.dx, 2);
                }

                if (circuit.left_boundary == 41) {  // left resistance is approximated with the resistance of the first cv
                    circuit.diag[0] = cv.k_right * dt;
                    circuit.diag[0] += cv.rho() * cv.cp() * Math.pow(cv.dx, 2);
                    circuit.diag[0] += 1.5 * circuit.getH_left() * cv.dx * dt;
                    circuit.upperdiag[0] = -cv.k_right * dt - 0.5 * circuit.getH_left() * cv.dx * dt;
                    circuit.rhs[0] = cv.rho() * cv.cp() * Math.pow(cv.dx, 2) * cv.temperature_old;
                    circuit.rhs[0] += circuit.getH_left() * cv.dx * dt * circuit.getTemp_left();
                    circuit.rhs[0] += cv.get_q_gen() * dt * Math.pow(cv.dx, 2);
                }

            }
            if (i == n - 1 && circuit.right_boundary != 0) {
                circuit.diag[n - 1] = cv.k_left * dt + cv.rho() * cv.cp() * Math.pow(cv.dx, 2);
                circuit.upperdiag[n - 1] = 0;
                if (circuit.right_boundary == 22) {
                    circuit.rhs[n - 1] = (cv.rho() * cv.cp() * Math.pow(cv.dx, 2)) * cv.temperature_old;
                    circuit.rhs[n - 1] -= circuit.getQ_out() * cv.dx * dt;
                    circuit.rhs[0] += cv.get_q_gen() * dt * Math.pow(cv.dx, 2);
                }
                if (circuit.right_boundary == 32) { // right resistance is approximated with the resistance of the last cv
                    circuit.diag[n - 1] = (cv.k() + cv.k_left) * dt + cv.rho() * cv.cp() * Math.pow(cv.dx, 2);
                    circuit.rhs[n - 1] = (cv.rho() * cv.cp() * Math.pow(cv.dx, 2)) * cv.temperature_old;
                    circuit.rhs[n - 1] += cv.k() * dt * circuit.getTemp_right();
                    circuit.rhs[n - 1] += cv.get_q_gen() * dt * Math.pow(cv.dx, 2);
                }
                if (circuit.right_boundary == 42) { // left resistance is approximated with the resistance of the first cv
                    circuit.diag[n - 1] = cv.k_left * dt + cv.rho() * cv.cp() * Math.pow(cv.dx, 2);
                    circuit.diag[n - 1] += 1.5 * circuit.getH_right() * cv.dx * dt;
                    circuit.underdiag[n - 1] = -cv.k_left * dt - 0.5 * circuit.getH_right() * cv.dx * dt;
                    circuit.rhs[n - 1] = cv.rho() * cv.cp() * Math.pow(cv.dx, 2) * cv.temperature_old;
                    circuit.rhs[n - 1] += circuit.getH_right() * cv.dx * dt * circuit.getTemp_right();
                    circuit.rhs[n - 1] += cv.get_q_gen() * dt * Math.pow(cv.dx, 2);
                }
            }

        }
    }
}
