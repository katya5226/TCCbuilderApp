The main class of the app is CirSim.java.
This class holds a Simulation1D object, and this object holds the circuit and the simulation parameters.
The loop for the simulation is in runCircuit() method.

Base class for TCEs is ThermalControlElement.java.
The basic Component, which is just a piece of material with specific properties, inherits from ThermalControlElement.
All other TCEs (conduit, switch, diode, etc.) also inherit from ThermalControlElement.

Sample TCEs usually inherit from their general class (sample switches inherit from SwitchElm).

When Components or TCEs are drawn on the canvas and the simulation is started, a TCC is created from all TCEs,
an EquationSystem is created, and a solver is applied in a loop to advance the time.
For the 1D case, the solver is the tridiagonal matrix algorithm (tdmaSolve method in ModelMethods.java).
