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
    DoubleBox ambientTemperature;
    // DoubleBox hTransv;

    Label inletHeatFluxLabel;
    Label westTemperatureLabel;
    Label westConvectionCoefficientLabel;
    Label outletHeatFluxLabel;
    Label eastTemperatureLabel;
    Label eastConvectionCoefficientLabel;

    DoubleBox inletHeatFlux;
    DoubleBox westTemperature;
    DoubleBox westConvectionCoefficient;
    DoubleBox outletHeatFlux;
    DoubleBox eastTemperature;
    DoubleBox eastConvectionCoefficient;
    ListBox westBoundary;
    ListBox eastBoundary;

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

        westBoundary = new ListBox();
        westBoundary.addItem("Adiabatic");
        westBoundary.addItem("Constant Heat Flux");
        westBoundary.addItem("Constant Temperature");
        westBoundary.addItem("Convective");
        westBoundary.setSelectedIndex(sim.simulation1D.westBoundary.ordinal());
        eastBoundary = new ListBox();
        eastBoundary.addItem("Adiabatic");
        eastBoundary.addItem("Constant Heat Flux");
        eastBoundary.addItem("Constant Temperature");
        eastBoundary.addItem("Convective");
        eastBoundary.setSelectedIndex(sim.simulation1D.eastBoundary.ordinal());
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
                startTemperature.setTitle("Starting temperature will be set for a component/TCE only if it hasn't been set earlier in component/TCE edit dialog.");
            }
        });

        ambientTemperature = new DoubleBox();
        ambientTemperature.setValue(sim.simulation1D.ambientTemperature);

        // hTransv = new DoubleBox();
        // hTransv.setValue(sim.simulation1D.hTransv);
        // hTransv.addMouseOverHandler(new MouseOverHandler() {
        //     @Override
        //     public void onMouseOver(MouseOverEvent e) {
        //         hTransv.setTitle("Refers to heat losses to the surroundings in transversal direction.
        //         Usually calculated as convection coefficient of natural convection, multiplied by the ratio of the perimeter and cross area of considered object.");
        //     }
        // });


        inletHeatFluxLabel = new Label(Locale.LS("Inlet Heat Flux ( W/m² )"));
        westTemperatureLabel = new Label(Locale.LS("West Temperature ( K )"));
        westConvectionCoefficientLabel = new Label(Locale.LS("West Convection Coefficient ( W/(m²K) )"));
        inletHeatFlux = new DoubleBox();
        inletHeatFlux.setValue(sim.simulation1D.qWest);
        westTemperature = new DoubleBox();
        westTemperature.setValue(sim.simulation1D.tempWest);
        westConvectionCoefficient = new DoubleBox();
        westConvectionCoefficient.setValue(sim.simulation1D.hWest);

        outletHeatFluxLabel = new Label(Locale.LS("Outlet Heat Flux ( W/m² )"));
        eastTemperatureLabel = new Label(Locale.LS("East Temperature ( K )"));
        eastConvectionCoefficientLabel = new Label(Locale.LS("East Convection Coefficient ( W/(m²K) )"));
        outletHeatFlux = new DoubleBox();
        inletHeatFlux.setValue(sim.simulation1D.qEast);
        eastTemperature = new DoubleBox();
        eastTemperature.setValue(sim.simulation1D.tempEast);
        eastConvectionCoefficient = new DoubleBox();
        eastConvectionCoefficient.setValue(sim.simulation1D.hEast);

        leftToggleables.add(inletHeatFluxLabel);
        leftToggleables.add(inletHeatFlux);
        leftToggleables.add(westTemperature);
        leftToggleables.add(westTemperatureLabel);
        leftToggleables.add(westConvectionCoefficient);
        leftToggleables.add(westConvectionCoefficientLabel);

        rightToggleables.add(outletHeatFlux);
        rightToggleables.add(outletHeatFluxLabel);
        rightToggleables.add(eastTemperature);
        rightToggleables.add(eastTemperatureLabel);
        rightToggleables.add(eastConvectionCoefficient);
        rightToggleables.add(eastConvectionCoefficientLabel);
        Label l;
        flowPanel.add(new Label(Locale.LS("Enter Time Step (ms): ")));
        flowPanel.add(timeStep);
        flowPanel.add(new Label(Locale.LS("Enter starting temperature (K): ")));
        flowPanel.add(startTemperature);
        flowPanel.add(new Label(Locale.LS("Enter ambient temperature (K): ")));
        flowPanel.add(ambientTemperature);
        // flowPanel.add(new Label(Locale.LS("Enter heat loss rate to the ambient (W/(m³K)): ")));
        // flowPanel.add(hTransv);
        flowPanel.add(l = new Label(Locale.LS("West Boundary Condition: ")));
        l.addStyleName("dialogHeading");
        flowPanel.add(westBoundary);
        flowPanel.add(inletHeatFluxLabel);
        flowPanel.add(inletHeatFlux);
        flowPanel.add(westTemperatureLabel);
        flowPanel.add(westTemperature);
        flowPanel.add(westConvectionCoefficientLabel);
        flowPanel.add(westConvectionCoefficient);

        flowPanel.add(l = new Label(Locale.LS("East Boundary Condition: ")));
        l.addStyleName("dialogHeading");

        flowPanel.add(eastBoundary);
        flowPanel.add(outletHeatFluxLabel);
        flowPanel.add(outletHeatFlux);
        flowPanel.add(eastTemperatureLabel);
        flowPanel.add(eastTemperature);
        flowPanel.add(eastConvectionCoefficientLabel);
        flowPanel.add(eastConvectionCoefficient);
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

        updateEastBoundary();
        updateWestBoundary();

        applyButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                double dtValue = timeStep.getValue();
                if (!(dtValue >= 0.000001) || !(dtValue <= 1000000)) {
                    Window.alert("Time Step not between 1 ns and 1000 s");
                    return;
                }
                double startTempValue = startTemperature.getValue();
                if (!(startTempValue >= 0.0) || !(startTempValue <= 2000)) {
                    Window.alert("Temperature not between 0 K and 2000 K");
                    return;
                }
                double ambientTempValue = ambientTemperature.getValue();
                if (!(ambientTempValue >= 0.0) || !(ambientTempValue <= 2000)) {
                    Window.alert("Temperature not between 0 K and 2000 K");
                    return;
                }
                // double hTransvValue = hTransv.getValue();
                if (westTemperature.isVisible()) {
                    double westTempValue = westTemperature.getValue();
                    if (!(westTempValue >= 0.0) || !(westTempValue <= 2000)) {
                        Window.alert("Temperature not between 0 K and 2000 K");
                        return;
                    }
                    sim.simulation1D.tempWest = westTempValue;

                }
                if (eastTemperature.isVisible()) {
                    double eastTempValue = eastTemperature.getValue();
                    if (!(eastTempValue >= 0.0) || !(eastTempValue <= 2000)) {
                        Window.alert("Temperature not between 0 K and 2000 K");
                        return;
                    }
                    sim.simulation1D.tempEast = eastTempValue;

                }
                if (westConvectionCoefficient.isVisible()) {
                    double westConvCoeffValue = westConvectionCoefficient.getValue();
                    if (!(westConvCoeffValue >= 0.0)) {
                        Window.alert("Convection coefficient must be positive!");
                        return;
                    }
                    sim.simulation1D.hWest = westConvCoeffValue;

                }
                if (eastConvectionCoefficient.isVisible()) {
                    double eastConvCoeffValue = eastConvectionCoefficient.getValue();
                    if (!(eastConvCoeffValue >= 0.0)) {
                        Window.alert("Convection coefficient must be positive!");
                        return;
                    }
                    sim.simulation1D.hEast = eastConvCoeffValue;

                }
                if (inletHeatFlux.isVisible()) {
                    double qInValue = inletHeatFlux.getValue();
                    if (!(qInValue >= 0.0)) {
                        Window.alert("Negative heat flux value means the heat flows out of the CV at west boundary.");
                        // return;
                    }
                    sim.simulation1D.qWest = qInValue;
                }
                if (outletHeatFlux.isVisible()) {
                    double qOutValue = outletHeatFlux.getValue();
                    if (!(qOutValue >= 0.0)) {
                        Window.alert("Negative heat flux value means the heat flows into the CV at east boundary.");
                        // return;
                    }
                    sim.simulation1D.qEast = qOutValue;
                }
                sim.simulation1D.dt = dtValue / 1e3;
                sim.simulation1D.startTemp = startTempValue;
                sim.simulation1D.ambientTemperature = ambientTempValue;
                sim.simulation1D.eastBoundary = Simulation1D.BorderCondition.values()[eastBoundary.getSelectedIndex()];
                sim.simulation1D.westBoundary = Simulation1D.BorderCondition.values()[westBoundary.getSelectedIndex()];

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
        westBoundary.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                updateWestBoundary();
            }
        });
        eastBoundary.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                updateEastBoundary();
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



    private void updateEastBoundary() {
        int selectedIndex = eastBoundary.getSelectedIndex();
        String selectedItem = eastBoundary.getValue(selectedIndex);
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
                eastTemperatureLabel.setVisible(true);
                eastTemperature.setVisible(true);
                eastTemperature.setValue(sim.simulation1D.tempEast);
                break;
            case "Convective":
                sim.simulation1D.eastBoundary = Simulation.BorderCondition.CONVECTIVE;
                eastTemperatureLabel.setVisible(true);
                eastTemperature.setVisible(true);
                eastTemperature.setValue(sim.simulation1D.tempEast);
                eastConvectionCoefficientLabel.setVisible(true);
                eastConvectionCoefficient.setVisible(true);
                eastConvectionCoefficient.setValue(sim.simulation1D.hEast);
                break;
            default:
                break;
        }
        center();
    }

    private void updateWestBoundary() {
        int selectedIndex = westBoundary.getSelectedIndex();
        String selectedItem = westBoundary.getValue(selectedIndex);
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
                westTemperatureLabel.setVisible(true);
                westTemperature.setVisible(true);
                westTemperature.setValue(sim.simulation1D.tempWest);
                break;
            case "Convective":
                sim.simulation1D.westBoundary = Simulation.BorderCondition.CONVECTIVE;
                westTemperatureLabel.setVisible(true);
                westTemperature.setVisible(true);
                westTemperature.setValue(sim.simulation1D.tempWest);
                westConvectionCoefficientLabel.setVisible(true);
                westConvectionCoefficient.setVisible(true);
                westConvectionCoefficient.setValue(sim.simulation1D.hWest);
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
