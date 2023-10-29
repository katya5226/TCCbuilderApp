package lahde.tccbuilder.client;

import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import lahde.tccbuilder.client.util.Locale;

import java.util.ArrayList;

public class StartDialog extends Dialog {
    CirSim sim;
    FlowPanel flowPanel;
    HorizontalPanel buttonPanel;
    FlowPanel cyclicContainer;
    Button cancelButton;
    Button applyButton;
    DoubleBox timeStep;
    DoubleBox startTemperature;

    Label inletHeatFluxLabel;
    Label leftTemperatureLabel;
    Label leftConvectionCoefficientLabel;
    Label outletHeatFluxLabel;
    Label rightTemperatureLabel;
    Label rightConvectionCoefficientLabel;

    DoubleBox inletHeatFlux;
    DoubleBox leftTemperature;
    DoubleBox leftConvectionCoefficient;
    DoubleBox outletHeatFlux;
    DoubleBox rightTemperature;
    DoubleBox rightConvectionCoefficient;
    ListBox leftBoundary;
    ListBox rightBoundary;

    HorizontalPanel leftHorizontalPanel;
    HorizontalPanel rightHorizontalPanel;
    Checkbox cyclic;
    Button cyclicButton;
    Checkbox includingRadiaton;
    ArrayList<Widget> leftToggleables = new ArrayList<Widget>();
    ArrayList<Widget> rightToggleables = new ArrayList<Widget>();

    public StartDialog(CirSim sim) {
        super();

        setText(Locale.LS("Start TCC"));
        closeOnEnter = true;
        this.sim = sim;

        flowPanel = new FlowPanel();
        flowPanel.addStyleName("dialogContainer");
        setWidget(flowPanel);
                /*new HTML("<ul>\n" +
                "\t<li>1. step</li>\n" +
                "\t<p>Lorem ipsum dolor sit amet consectetur adipisicing elit. Aspernatur nisi voluptatem iure commodi pariatur? Corporis laudantium ipsum repudiandae necessitatibus recusandae alias corrupti dolor odio molestias provident, blanditiis numquam repellendus id nam sed, error voluptatum? Repellat, vel. Velit natus, culpa omnis, in maiores minima asperiores eius quae repellendus nihil, aspernatur odio.</p>\n" +
                "\t<li>1. step</li>\n" +
                "\t<p>Lorem ipsum dolor sit amet consectetur adipisicing elit. Aspernatur nisi voluptatem iure commodi pariatur? Corporis laudantium ipsum repudiandae necessitatibus recusandae alias corrupti dolor odio molestias provident, blanditiis numquam repellendus id nam sed, error voluptatum? Repellat, vel. Velit natus, culpa omnis, in maiores minima asperiores eius quae repellendus nihil, aspernatur odio.</p>\n" +
                "</ul>\n");*/
        HTML test = new HTML(
                "<iframe src=\"start-help.html\" frameborder=\"0\">" +
                        "</iframe>");

        flowPanel.add(getHelpButton(test));

        leftBoundary = new ListBox();
        leftBoundary.addItem("Adiabatic");
        leftBoundary.addItem("Constant Heat Flux");
        leftBoundary.addItem("Constant Temperature");
        leftBoundary.addItem("Convective");
        leftBoundary.setSelectedIndex(sim.simulation1D.westBoundary.ordinal());
        rightBoundary = new ListBox();
        rightBoundary.addItem("Adiabatic");
        rightBoundary.addItem("Constant Heat Flux");
        rightBoundary.addItem("Constant Temperature");
        rightBoundary.addItem("Convective");
        rightBoundary.setSelectedIndex(sim.simulation1D.eastBoundary.ordinal());
        cyclic = new Checkbox("Cyclic");
        cyclic.setState(sim.simulation1D.cyclic);
        cyclicButton = new Button("Add Cycle Part");
        cyclicButton.setEnabled(cyclic.getState());
        cyclicContainer = new FlowPanel();
        cyclicContainer.addStyleName("cyclicContainer");
        cyclicContainer.add(cyclic);
        cyclicContainer.add(cyclicButton);

        //includingRadiaton = new Checkbox("Including radiation");


        timeStep = new DoubleBox();
        timeStep.setValue(sim.simulation1D.dt * 1e3);

        startTemperature = new DoubleBox();
        startTemperature.setValue(sim.simulation1D.startTemp);
        startTemperature.addMouseOverHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent e) {
                startTemperature.setTitle("Starting temperature will be set for a component/TCE only if it hasn't been set earlier.");
            }
        });


        inletHeatFluxLabel = new Label(Locale.LS("Inlet Heat Flux ( W/m² )"));
        leftTemperatureLabel = new Label(Locale.LS("Left Temperature ( K )"));
        leftConvectionCoefficientLabel = new Label(Locale.LS("Left Convection Coefficient ( W/(m²K) )"));
        inletHeatFlux = new DoubleBox();
        inletHeatFlux.setValue(sim.simulation1D.qWest);
        leftTemperature = new DoubleBox();
        leftTemperature.setValue(sim.simulation1D.tempWest);
        leftConvectionCoefficient = new DoubleBox();
        leftConvectionCoefficient.setValue(sim.simulation1D.hWest);

        outletHeatFluxLabel = new Label(Locale.LS("Outlet Heat Flux ( W/m² )"));
        rightTemperatureLabel = new Label(Locale.LS("Right Temperature ( K )"));
        rightConvectionCoefficientLabel = new Label(Locale.LS("Right Convection Coefficient ( W/(m²K) )"));
        outletHeatFlux = new DoubleBox();
        inletHeatFlux.setValue(sim.simulation1D.qEast);
        rightTemperature = new DoubleBox();
        rightTemperature.setValue(sim.simulation1D.tempEast);
        rightConvectionCoefficient = new DoubleBox();
        rightConvectionCoefficient.setValue(sim.simulation1D.hEast);

        leftToggleables.add(inletHeatFluxLabel);
        leftToggleables.add(inletHeatFlux);
        leftToggleables.add(leftTemperature);
        leftToggleables.add(leftTemperatureLabel);
        leftToggleables.add(leftConvectionCoefficient);
        leftToggleables.add(leftConvectionCoefficientLabel);

        rightToggleables.add(outletHeatFlux);
        rightToggleables.add(outletHeatFluxLabel);
        rightToggleables.add(rightTemperature);
        rightToggleables.add(rightTemperatureLabel);
        rightToggleables.add(rightConvectionCoefficient);
        rightToggleables.add(rightConvectionCoefficientLabel);
        Label l;
        flowPanel.add(new Label(Locale.LS("Enter Time Step (ms): ")));
        flowPanel.add(timeStep);
        flowPanel.add(new Label(Locale.LS("Enter starting temperature (K): ")));
        flowPanel.add(startTemperature);
        flowPanel.add(l = new Label(Locale.LS("Left Boundary Condition: ")));
        l.addStyleName("dialogHeading");
        flowPanel.add(leftBoundary);
        flowPanel.add(inletHeatFluxLabel);
        flowPanel.add(inletHeatFlux);
        flowPanel.add(leftTemperatureLabel);
        flowPanel.add(leftTemperature);
        flowPanel.add(leftConvectionCoefficientLabel);
        flowPanel.add(leftConvectionCoefficient);

        flowPanel.add(l = new Label(Locale.LS("Right Boundary Condition: ")));
        l.addStyleName("dialogHeading");

        flowPanel.add(rightBoundary);
        flowPanel.add(outletHeatFluxLabel);
        flowPanel.add(outletHeatFlux);
        flowPanel.add(rightTemperatureLabel);
        flowPanel.add(rightTemperature);
        flowPanel.add(rightConvectionCoefficientLabel);
        flowPanel.add(rightConvectionCoefficient);
        flowPanel.add(cyclicContainer);

        //vp.add(includingRadiaton);


        applyButton = new Button(Locale.LS("Apply"));
        cancelButton = new Button(Locale.LS("Cancel"));

        for (Widget w : rightToggleables) {
            w.setVisible(false);
        }
        for (Widget w : leftToggleables) {
            w.setVisible(false);
        }

        updateRightBoundary();
        updateLeftBoundary();

        applyButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                double dtValue = timeStep.getValue();
                if (!(dtValue >= 0.001) || !(dtValue <= 1000)) {
                    Window.alert("Time Step not between 1μs and 1s");
                    return;
                }
                double startTempValue = startTemperature.getValue();
                if (!(startTempValue >= 0.0) || !(startTempValue <= 2000)) {
                    Window.alert("Temperature not between 0 K and 2000 K");
                    return;
                }
                if (leftTemperature.isVisible()) {
                    double leftTempValue = leftTemperature.getValue();
                    if (!(leftTempValue >= 0.0) || !(leftTempValue <= 2000)) {
                        Window.alert("Temperature not between 0 K and 2000 K");
                        return;
                    }
                    sim.simulation1D.tempWest = leftTempValue;

                }
                if (rightTemperature.isVisible()) {
                    double rightTempValue = rightTemperature.getValue();
                    if (!(rightTempValue >= 0.0) || !(rightTempValue <= 2000)) {
                        Window.alert("Temperature not between 0 K and 2000 K");
                        return;
                    }
                    sim.simulation1D.tempEast = rightTempValue;

                }
                if (leftConvectionCoefficient.isVisible()) {
                    double leftConvCoeffValue = leftConvectionCoefficient.getValue();
                    if (!(leftConvCoeffValue >= 0.0)) {
                        Window.alert("Convection coefficient must be positive!");
                        return;
                    }
                    sim.simulation1D.hWest = leftConvCoeffValue;

                }
                if (rightConvectionCoefficient.isVisible()) {
                    double rightConvCoeffValue = rightConvectionCoefficient.getValue();
                    if (!(rightConvCoeffValue >= 0.0)) {
                        Window.alert("Convection coefficient must be positive!");
                        return;
                    }
                    sim.simulation1D.hEast = rightConvCoeffValue;

                }
                if (inletHeatFlux.isVisible()) {
                    double qInValue = inletHeatFlux.getValue();
                    if (!(qInValue >= 0.0)) {
                        Window.alert("Heat flux value must be positive!");
                        return;
                    }
                    sim.simulation1D.qWest = qInValue;
                }
                if (outletHeatFlux.isVisible()) {
                    double qOutValue = outletHeatFlux.getValue();
                    if (!(qOutValue >= 0.0)) {
                        Window.alert("Heat flux value must be positive!");
                        return;
                    }
                    sim.simulation1D.qEast = qOutValue;
                }
                sim.simulation1D.dt = dtValue / 1e3;
                sim.simulation1D.startTemp = startTempValue;
                sim.simulation1D.eastBoundary = Simulation1D.BorderCondition.values()[rightBoundary.getSelectedIndex()];
                sim.simulation1D.westBoundary = Simulation1D.BorderCondition.values()[leftBoundary.getSelectedIndex()];

                // GWT.log("Left Boundary: " + String.valueOf(sim.thermalSimulation.left_boundary));
                // GWT.log("Left Temperature: " + String.valueOf(sim.thermalSimulation.temp_left));
                // GWT.log("Left Convection coeff: " + String.valueOf(sim.thermalSimulation.h_left));
                // GWT.log("Inlet heat flux: " + String.valueOf(sim.thermalSimulation.qIn));
                // GWT.log("Right Boundary: " + String.valueOf(sim.thermalSimulation.right_boundary));
                // GWT.log("Right Temperature: " + String.valueOf(sim.thermalSimulation.temp_right));
                // GWT.log("Right Convection coeff: " + String.valueOf(sim.thermalSimulation.h_right));
                // GWT.log("Outlet heat flux: " + String.valueOf(sim.thermalSimulation.qOut));
                if (sim.simulation1D.cyclic) {
                    GWT.log("Cycle parts: ");
                    for (int cpi = 0; cpi < sim.simulation1D.cycleParts.size(); cpi++) {
                        GWT.log(String.valueOf(sim.simulation1D.cycleParts.get(cpi).partType));
                    }
                    GWT.log("------");
                }
                apply();
            }
        });
        leftBoundary.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                updateLeftBoundary();
            }
        });
        rightBoundary.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                updateRightBoundary();
            }
        });
        cyclic.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                sim.setCyclic(cyclic.getState());
                cyclicButton.setEnabled(cyclic.getState());

            }


        });
        cyclicButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                new CyclicDialog(CirSim.theSim).show();
            }
        });
        cancelButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                closeDialog();
            }
        });

        buttonPanel = new HorizontalPanel();
        buttonPanel.setWidth("100%");
        buttonPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        buttonPanel.add(cancelButton);
        buttonPanel.add(applyButton);
        buttonPanel.addStyleName("dialogButtonPanel");
        flowPanel.add(buttonPanel);
        this.center();
    }



    private void updateRightBoundary() {
        int selectedIndex = rightBoundary.getSelectedIndex();
        String selectedItem = rightBoundary.getValue(selectedIndex);
        for (Widget w : rightToggleables) {
            w.setVisible(false);
            if (w instanceof DoubleBox)
                ((DoubleBox) w).setText("");
        }
        switch (selectedItem) {
            case "Adiabatic":
                sim.simulation1D.eastBoundary = Simulation.BorderCondition.ADIABATIC;
                break;
            case "Constant Heat Flux":
                sim.simulation1D.eastBoundary = Simulation.BorderCondition.CONSTANT_HEAT_FLUX;
                outletHeatFluxLabel.setVisible(true);
                outletHeatFlux.setVisible(true);
                outletHeatFlux.setValue(sim.simulation1D.qEast);
                break;
            case "Constant Temperature":
                sim.simulation1D.eastBoundary = Simulation.BorderCondition.CONSTANT_TEMPERATURE;
                rightTemperatureLabel.setVisible(true);
                rightTemperature.setVisible(true);
                rightTemperature.setValue(sim.simulation1D.tempEast);
                break;
            case "Convective":
                sim.simulation1D.eastBoundary = Simulation.BorderCondition.CONVECTIVE;
                rightTemperatureLabel.setVisible(true);
                rightTemperature.setVisible(true);
                rightTemperature.setValue(sim.simulation1D.tempEast);
                rightConvectionCoefficientLabel.setVisible(true);
                rightConvectionCoefficient.setVisible(true);
                rightConvectionCoefficient.setValue(sim.simulation1D.hEast);
                break;
            default:
                break;
        }
        center();
    }

    private void updateLeftBoundary() {
        int selectedIndex = leftBoundary.getSelectedIndex();
        String selectedItem = leftBoundary.getValue(selectedIndex);
        for (Widget w : leftToggleables) {
            w.setVisible(false);
            if (w instanceof DoubleBox)
                ((DoubleBox) w).setText("");
        }
        switch (selectedItem) {
            case "Adiabatic":
                sim.simulation1D.westBoundary = Simulation.BorderCondition.ADIABATIC;
                break;
            case "Constant Heat Flux":
                sim.simulation1D.westBoundary = Simulation.BorderCondition.CONSTANT_HEAT_FLUX;
                inletHeatFluxLabel.setVisible(true);
                inletHeatFlux.setVisible(true);
                inletHeatFlux.setValue(sim.simulation1D.qWest);
                break;
            case "Constant Temperature":
                sim.simulation1D.westBoundary = Simulation.BorderCondition.CONSTANT_TEMPERATURE;
                leftTemperatureLabel.setVisible(true);
                leftTemperature.setVisible(true);
                leftTemperature.setValue(sim.simulation1D.tempWest);
                break;
            case "Convective":
                sim.simulation1D.westBoundary = Simulation.BorderCondition.CONVECTIVE;
                leftTemperatureLabel.setVisible(true);
                leftTemperature.setVisible(true);
                leftTemperature.setValue(sim.simulation1D.tempWest);
                leftConvectionCoefficientLabel.setVisible(true);
                leftConvectionCoefficient.setVisible(true);
                leftConvectionCoefficient.setValue(sim.simulation1D.hWest);
                break;
            default:
                break;
        }
        center();
    }


    @Override
    public void enterPressed() {
        if (closeOnEnter) {
            apply();
            closeDialog();
        }
    }

    @Override
    void apply() {
        if (!sim.simulation1D.simTCEs.isEmpty()) {
            sim.resetAction();
            closeDialog();
        }
    }
}
