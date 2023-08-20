package lahde.tccbuilder.client;

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
    VerticalPanel vp;
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

        vp = new VerticalPanel();
        setWidget(vp);

        leftBoundary = new ListBox();
        leftBoundary.addItem("Adiabatic");
        leftBoundary.addItem("Constant Heat Flux");
        leftBoundary.addItem("Constant Temperature");
        leftBoundary.addItem("Convective");
        rightBoundary = new ListBox();
        rightBoundary.addItem("Adiabatic");
        rightBoundary.addItem("Constant Heat Flux");
        rightBoundary.addItem("Constant Temperature");
        rightBoundary.addItem("Convective");

        cyclic = new Checkbox("Cyclic");
        cyclic.setState(sim.cyclic);
        cyclicButton = new Button("Add Cyclic Part");
        cyclicButton.setEnabled(cyclic.getState());
        cyclicContainer = new FlowPanel();
        cyclicContainer.addStyleName("cyclicContainer");
        cyclicContainer.add(cyclic);
        cyclicContainer.add(cyclicButton);

        //includingRadiaton = new Checkbox("Including radiation");


        timeStep = new DoubleBox();
        timeStep.setValue(sim.dt * 1e3);

        startTemperature = new DoubleBox();
        startTemperature.setValue(sim.startTemp);

        inletHeatFluxLabel = new Label(Locale.LS("Inlet Heat Flux ( W/m² )"));
        leftTemperatureLabel = new Label(Locale.LS("Left Temperature ( K )"));
        leftConvectionCoefficientLabel = new Label(Locale.LS("Left Convection Coefficient ( W/(m²K) )"));
        inletHeatFlux = new DoubleBox();
        inletHeatFlux.setValue(sim.qIn);
        leftTemperature = new DoubleBox();
        leftTemperature.setValue(sim.temp_left);
        leftConvectionCoefficient = new DoubleBox();
        leftConvectionCoefficient.setValue(sim.h_left);

        outletHeatFluxLabel = new Label(Locale.LS("Outlet Heat Flux ( W/m² )"));
        rightTemperatureLabel = new Label(Locale.LS("Right Temperature ( K )"));
        rightConvectionCoefficientLabel = new Label(Locale.LS("Right Convection Coefficient ( W/(m²K) )"));
        outletHeatFlux = new DoubleBox();
        inletHeatFlux.setValue(sim.qOut);
        rightTemperature = new DoubleBox();
        rightTemperature.setValue(sim.temp_right);
        rightConvectionCoefficient = new DoubleBox();
        rightConvectionCoefficient.setValue(sim.h_right);

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
        vp.add(new Label(Locale.LS("Enter Time Step (ms): ")));
        vp.add(timeStep);
        vp.add(new Label(Locale.LS("Enter starting temperature (K): ")));
        vp.add(startTemperature);
        vp.add(l = new Label(Locale.LS("Left Boundary Condition: ")));
        l.addStyleName("dialogHeading");
        vp.add(leftBoundary);
        vp.add(inletHeatFluxLabel);
        vp.add(inletHeatFlux);
        vp.add(leftTemperatureLabel);
        vp.add(leftTemperature);
        vp.add(leftConvectionCoefficientLabel);
        vp.add(leftConvectionCoefficient);

        vp.add(l = new Label(Locale.LS("Right Boundary Condition: ")));
        l.addStyleName("dialogHeading");

        vp.add(rightBoundary);
        vp.add(outletHeatFluxLabel);
        vp.add(outletHeatFlux);
        vp.add(rightTemperatureLabel);
        vp.add(rightTemperature);
        vp.add(rightConvectionCoefficientLabel);
        vp.add(rightConvectionCoefficient);
        vp.add(cyclicContainer);

        //vp.add(includingRadiaton);

        vp.setSpacing(1);

        applyButton = new Button(Locale.LS("Apply"));
        cancelButton = new Button(Locale.LS("Cancel"));

        for (Widget w : rightToggleables) {
            w.setVisible(false);
        }
        for (Widget w : leftToggleables) {
            w.setVisible(false);
        }
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
                    sim.temp_left = leftTempValue;

                }
                if (rightTemperature.isVisible()) {
                    double rightTempValue = rightTemperature.getValue();
                    if (!(rightTempValue >= 0.0) || !(rightTempValue <= 2000)) {
                        Window.alert("Temperature not between 0 K and 2000 K");
                        return;
                    }
                    sim.temp_right = rightTempValue;

                }
                if (leftConvectionCoefficient.isVisible()) {
                    double leftConvCoeffValue = leftConvectionCoefficient.getValue();
                    if (!(leftConvCoeffValue >= 0.0)) {
                        Window.alert("Convection coefficient must be positive!");
                        return;
                    }
                    sim.h_left = leftConvCoeffValue;

                }
                if (rightConvectionCoefficient.isVisible()) {
                    double rightConvCoeffValue = rightConvectionCoefficient.getValue();
                    if (!(rightConvCoeffValue >= 0.0)) {
                        Window.alert("Convection coefficient must be positive!");
                        return;
                    }
                    sim.h_right = rightConvCoeffValue;

                }
                if (inletHeatFlux.isVisible()) {
                    double qInValue = inletHeatFlux.getValue();
                    if (!(qInValue >= 0.0)) {
                        Window.alert("Heat flux value must be positive!");
                        return;
                    }
                    sim.qIn = qInValue;
                }
                if (outletHeatFlux.isVisible()) {
                    double qOutValue = outletHeatFlux.getValue();
                    if (!(qOutValue >= 0.0)) {
                        Window.alert("Heat flux value must be positive!");
                        return;
                    }
                    sim.qOut = qOutValue;
                }
                sim.dt = dtValue / 1e3;
                sim.startTemp = startTempValue;
                sim.left_boundary = (int) (10 * (leftBoundary.getSelectedIndex() + 1) + 1);//what is this (o_O)
                sim.right_boundary = (int) (10 * (rightBoundary.getSelectedIndex() + 1) + 2);//what is this (o_O)

                // GWT.log("Left Boundary: " + String.valueOf(sim.left_boundary));
                // GWT.log("Left Temperature: " + String.valueOf(sim.temp_left));
                // GWT.log("Left Convection coeff: " + String.valueOf(sim.h_left));
                // GWT.log("Inlet heat flux: " + String.valueOf(sim.qIn));
                // GWT.log("Right Boundary: " + String.valueOf(sim.right_boundary));
                // GWT.log("Right Temperature: " + String.valueOf(sim.temp_right));
                // GWT.log("Right Convection coeff: " + String.valueOf(sim.h_right));
                // GWT.log("Outlet heat flux: " + String.valueOf(sim.qOut));
                if (sim.cyclic) {
                    GWT.log("Cycle parts: ");
                    for (int cpi = 0; cpi < sim.cycleParts.size(); cpi++) {
                        GWT.log(String.valueOf(sim.cycleParts.get(cpi).partType));
                    }
                    GWT.log("------");
                    GWT.log("Inox k: " + String.valueOf(sim.simTCEs.get(0).cvs.get(0).constK));
                }
                apply();
            }
        });
        leftBoundary.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                int selectedIndex = leftBoundary.getSelectedIndex();
                String selectedItem = leftBoundary.getValue(selectedIndex);
                for (Widget w : leftToggleables) {
                    w.setVisible(false);
                    if (w instanceof DoubleBox)
                        ((DoubleBox) w).setText("");
                }
                switch (selectedItem) {
                    case "Constant Heat Flux":
                        inletHeatFluxLabel.setVisible(true);
                        inletHeatFlux.setVisible(true);
                        inletHeatFlux.setValue(sim.qIn);
                        break;
                    case "Constant Temperature":
                        leftTemperatureLabel.setVisible(true);
                        leftTemperature.setVisible(true);
                        leftTemperature.setValue(sim.temp_left);
                        break;
                    case "Convective":
                        leftTemperatureLabel.setVisible(true);
                        leftTemperature.setVisible(true);
                        leftTemperature.setValue(sim.temp_left);
                        leftConvectionCoefficientLabel.setVisible(true);
                        leftConvectionCoefficient.setVisible(true);
                        leftConvectionCoefficient.setValue(sim.h_left);


                        break;
                    default:

                        break;
                }
                center();
            }
        });
        rightBoundary.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                int selectedIndex = rightBoundary.getSelectedIndex();
                String selectedItem = rightBoundary.getValue(selectedIndex);
                for (Widget w : rightToggleables) {
                    w.setVisible(false);
                    if (w instanceof DoubleBox)
                        ((DoubleBox) w).setText("");
                }
                switch (selectedItem) {
                    case "Constant Heat Flux":
                        outletHeatFluxLabel.setVisible(true);
                        outletHeatFlux.setVisible(true);
                        outletHeatFlux.setValue(sim.qOut);
                        break;
                    case "Constant Temperature":
                        rightTemperatureLabel.setVisible(true);
                        rightTemperature.setVisible(true);
                        rightTemperature.setValue(sim.temp_right);
                        break;
                    case "Convective":
                        rightTemperatureLabel.setVisible(true);
                        rightTemperature.setVisible(true);
                        rightTemperature.setValue(sim.temp_right);
                        rightConvectionCoefficientLabel.setVisible(true);
                        rightConvectionCoefficient.setVisible(true);
                        rightConvectionCoefficient.setValue(sim.h_right);

                        break;
                    default:
                        break;
                }
                center();
            }
        });
        cyclic.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                sim.cyclic = cyclic.getState();
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
        vp.add(buttonPanel);
        this.center();
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

        if (!sim.simTCEs.isEmpty()) {
            sim.resetAction();


            closeDialog();
        }
    }
}
