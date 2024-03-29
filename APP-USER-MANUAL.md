# TCCbuilder app

## Menu

### File
New blank circuit: Clears the canvas and all settings.
Open file: Opens a file with a saved circuit and draws it on the canvas.
Import from text: Opens a textbox where one can paste the circuit that was previously saved.
Save circuit as: Saves the circuit that is currently on the canvas, to one's computer, as text.
Save report as: Saves the description and results of simulation at current point in time, to one's computer.
Export circuit as Text: Shows the text for the circuit that is currently on the canvas.
Export report as text:  Shows the description and results of simulation at current point in time, in a textbox.
Export circuit as image: Saves the circuit that is currently on the canvas as a png file, to one's computer.
Export circuit as svg: Exports the circuit that is currently on the canvas as a svg file, to one's computer.
Recover Auto-Save: Recovers the last saved circuit.
Print: Prints the circuit that is currently on the canvas.
Toggle full screen: Opens the full screen view of the app.

### Edit
Various edit options.

### Draw
One should draw a circuit either with Components, or using TCEs and other elements. After selecting one of them, click on the canvas and drag to draw them.
Some sample TCEs (Samples) can't be resized and will draw automatically with a click on the canvas.
The Drag options offer different options for dragging the elements or their posts.
Select/Drag Sel. selects the element that is clicked so it can be moved through the canvas.

### Options
The user can toggle each of the available options.
Show temperatures on graph gives a graphical representation of temperatures of all control volumes of the circuit in a graph in the lower part of the canvas.
Show temperature overlay shows temperatures as colors where blue is low temperature and red is high temperature. The legend for the colors is shown in the lower part of the canvas.
Set custom temperature range gives the option to set the range for temperatures shown in the graph. This is necessary when the model itself cannot predict the temperature range of the whole simulation.
Set custom output interval gives the option to specify the interval for exporting temperatures of control volumes in the simulation report. One can export the values of temperatures in each time step, or for example, every 100th time step.

### Circuits
Here are some examples of thermal control circuits with preset simulation parameters. Running the simulation for any of these circuits shows their operation.


## Editing components or TCEs

By right clicking on any component or element, the editing dialog opens.
The following properties can be set: Name of the TCE, TCE index within the TCC, material, number of control volumes, color, length, contact resistances, and thermal properties.
Depending on the type of component or TCE, some properties may not be modifiable, or are not applicable, or there may be additional properties that need to be set.


## Right-hand side of the canvas

Build TCC - opens the starting dialog for setting the simulation parameters and running a simulation.
Reset TCC - resets the circuit to initial values and sets the time to zero.
Run/STOP - pauses or resumes the simulation.
Simulation speed - the speed of computing and refreshing the screen.
Scale - the space scale of the canvas. Each pixel on screen coresponds to some real length, depending on the chosen scale.
Dimensionality - At the moment, the app works well only for 1D.


## Setting the simulation parameters

Clicking the "Build TCC" button opens the dialog where all the parameters relevant for a simulation can be set.
Cyclic operation is set by checking the "Cyclic" checkbox, that can be unchecked at any time. Cycle parts can then be added.
When adding cycle parts, all the components affected during that part must be added first by clicking the "Add Component" button for each chosen component, after that the "Add part" button can be clicked.
For some cycle parts like heat transfer, no components need to be added as all are included.
Clicking the "Apply" button starts the simulation.


## Workflow

First, the dimensionality should be set and the scale should be set in a way that the considered circuit will fit on the canvas. One can only draw either exclusively in horizontal or exclusively in vertical direction.
The simulation speed should be set to low value at first and can be later increased.
Then, the TCC is drawn on the canvas, and after that, the simulation parameters are set, and simulation is started.
To export the circuit or the simulation report, pause the simulation and choose one of the options from the "File" menu.
