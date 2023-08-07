package com.lushprojects.circuitjs1.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.lushprojects.circuitjs1.client.util.Locale;

import java.util.ArrayList;

public class StartDialog2D extends Dialog {
    CirSim sim;
    VerticalPanel vp;
    HorizontalPanel buttonPanel;
    HorizontalPanel cyclicContainer;
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
    ArrayList<Widget> leftToggleables = new ArrayList<Widget>();
    ArrayList<Widget> rightToggleables = new ArrayList<Widget>();

    public StartDialog2D(CirSim sim) {
        super();

        setText(Locale.LS("Start TCC"));
        closeOnEnter = true;
        this.sim = sim;
        vp = new VerticalPanel();
        setWidget(vp);

        leftBoundary = new ListBox();
        /*
        leftBoundary.addItem("Adiabatic");
        leftBoundary.addItem("Constant Heat Flux");
        leftBoundary.addItem("Constant Temperature");
        */
        leftBoundary.addItem("Convective");
        rightBoundary = new ListBox();
        /*
        rightBoundary.addItem("Adiabatic");
        rightBoundary.addItem("Constant Heat Flux");
        rightBoundary.addItem("Constant Temperature");
        */
        rightBoundary.addItem("Convective");


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
        leftTemperature.setValue(sim.twoDimBC.T[0]);
        leftConvectionCoefficient = new DoubleBox();
        leftConvectionCoefficient.setValue(sim.h_left);

        outletHeatFluxLabel = new Label(Locale.LS("Outlet Heat Flux ( W/m² )"));
        rightTemperatureLabel = new Label(Locale.LS("Right Temperature ( K )"));
        rightConvectionCoefficientLabel = new Label(Locale.LS("Right Convection Coefficient ( W/(m²K) )"));
        outletHeatFlux = new DoubleBox();
        inletHeatFlux.setValue(sim.qOut);
        rightTemperature = new DoubleBox();
        rightTemperature.setValue(sim.twoDimBC.T[1]);
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

        vp.setSpacing(1);

        applyButton = new Button(Locale.LS("Apply"));
        cancelButton = new Button(Locale.LS("Cancel"));

        for (Widget w : rightToggleables) {
            w.setVisible(false);
        }
        for (Widget w : leftToggleables) {
            w.setVisible(false);
        }
        leftTemperatureLabel.setVisible(true);
        leftTemperature.setVisible(true);
        leftConvectionCoefficientLabel.setVisible(true);
        leftConvectionCoefficient.setVisible(true);

        rightTemperatureLabel.setVisible(true);
        rightTemperature.setVisible(true);
        rightConvectionCoefficientLabel.setVisible(true);
        rightConvectionCoefficient.setVisible(true);
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
                    sim.twoDimBC.T[0] = leftTempValue;
                }
                if (rightTemperature.isVisible()) {
                    double rightTempValue = rightTemperature.getValue();
                    if (!(rightTempValue >= 0.0) || !(rightTempValue <= 2000)) {
                        Window.alert("Temperature not between 0 K and 2000 K");
                        return;
                    }
                    sim.twoDimBC.T[1] = rightTempValue;
                }
                if (leftConvectionCoefficient.isVisible()) {
                    double leftConvCoeffValue = leftConvectionCoefficient.getValue();
                    if (!(leftConvCoeffValue >= 0.0)) {
                        Window.alert("Convection coefficient must be positive!");
                        return;
                    }
                    sim.twoDimBC.h[0] = leftConvCoeffValue;
                }
                if (rightConvectionCoefficient.isVisible()) {
                    double rightConvCoeffValue = rightConvectionCoefficient.getValue();
                    if (!(rightConvCoeffValue >= 0.0)) {
                        Window.alert("Convection coefficient must be positive!");
                        return;
                    }
                    sim.twoDimBC.h[1] = rightConvCoeffValue;
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
                        break;
                    case "Constant Temperature":
                        leftTemperatureLabel.setVisible(true);
                        leftTemperature.setVisible(true);
                        break;
                    case "Convective":
                        leftTemperatureLabel.setVisible(true);
                        leftTemperature.setVisible(true);
                        leftConvectionCoefficientLabel.setVisible(true);
                        leftConvectionCoefficient.setVisible(true);
                        leftTemperature.setValue(sim.twoDimBC.T[0]);
                        leftConvectionCoefficient.setValue(sim.twoDimBC.h[0]);

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
                        break;
                    case "Constant Temperature":
                        rightTemperatureLabel.setVisible(true);
                        rightTemperature.setVisible(true);
                        break;
                    case "Convective":
                        rightTemperatureLabel.setVisible(true);
                        rightTemperature.setVisible(true);
                        rightConvectionCoefficientLabel.setVisible(true);
                        rightConvectionCoefficient.setVisible(true);
                        rightTemperature.setValue(sim.twoDimBC.T[1]);
                        rightConvectionCoefficient.setValue(sim.twoDimBC.h[1]);
                        break;
                    default:
                        break;
                }
                center();
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

        if (!sim.simTwoDimComponents.isEmpty())
            sim.resetAction();
        closeDialog();
    }

}
