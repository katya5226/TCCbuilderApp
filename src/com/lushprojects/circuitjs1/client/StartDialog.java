package com.lushprojects.circuitjs1.client;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.lushprojects.circuitjs1.client.util.Locale;

import java.util.ArrayList;

public class StartDialog extends Dialog {
    CirSim sim;
    VerticalPanel vp;
    HorizontalPanel buttonPanel;
    HorizontalPanel cyclicContainer;
    Button cancelButton;
    Button applyButton;
    DoubleBox timeStep;

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
    //ListBox scale;
    ListBox dimensionality;

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

/*        scale = new ListBox();
        scale.addItem("micrometer");
        scale.addItem("millimeter");
        scale.addItem("centimeter");
        scale.addItem("meter");*/

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
        cyclicButton = new Button("Add Cyclic Parts");
        cyclicButton.setEnabled(false);
        cyclicContainer = new HorizontalPanel();
        cyclicContainer.setWidth("100%");
        cyclicContainer.add(cyclic);
        cyclicContainer.add(cyclicButton);

        includingRadiaton = new Checkbox("Including radiation");

        dimensionality = new ListBox();
        dimensionality.addItem("1D");
        dimensionality.addItem("2D");


        timeStep = new DoubleBox();
        timeStep.setValue(sim.dt*1e3);


        inletHeatFluxLabel = new Label(Locale.LS("Inlet Heat Flux ( W/m^2 )"));
        leftTemperatureLabel = new Label(Locale.LS("Left Temperature ( K )"));
        leftConvectionCoefficientLabel = new Label(Locale.LS("Left Convection Coefficient ( W/(m^2K) )"));
        inletHeatFlux = new DoubleBox();
        leftTemperature = new DoubleBox();
        leftConvectionCoefficient = new DoubleBox();

        outletHeatFluxLabel = new Label(Locale.LS("Outlet Heat Flux ( W/m^2 )"));
        rightTemperatureLabel = new Label(Locale.LS("Right Temperature ( K )"));
        rightConvectionCoefficientLabel = new Label(Locale.LS("Right Convection Coefficient ( W/(m^2K) )"));
        outletHeatFlux = new DoubleBox();
        rightTemperature = new DoubleBox();
        rightConvectionCoefficient = new DoubleBox();

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

        vp.add(new Label(Locale.LS("Enter Time Step (ms): ")));
        vp.add(timeStep);
        vp.add(new Label(Locale.LS("Left Boundary Condition: ")));
        vp.add(leftBoundary);
        vp.add(inletHeatFluxLabel);
        vp.add(inletHeatFlux);
        vp.add(leftTemperatureLabel);
        vp.add(leftTemperature);
        vp.add(leftConvectionCoefficientLabel);
        vp.add(leftConvectionCoefficient);

        vp.add(new Label(Locale.LS("Right Boundary Condition: ")));
        vp.add(rightBoundary);
        vp.add(outletHeatFluxLabel);
        vp.add(outletHeatFlux);
        vp.add(rightTemperatureLabel);
        vp.add(rightTemperature);
        vp.add(rightConvectionCoefficientLabel);
        vp.add(rightConvectionCoefficient);
        vp.add(cyclicContainer);

        vp.add(includingRadiaton);
        vp.add(new Label(Locale.LS("Dimensionality: ")));
        vp.add(dimensionality);
        //vp.add(scale);

        vp.add(new Label(Locale.LS("Scale: ")));
        vp.setSpacing(1);
/*        switch (sim.selectedLengthUnit) {
            case MICROMETER:
                scale.setSelectedIndex(0);
                break;
            case MILLIMETER:
                scale.setSelectedIndex(1);
                break;
            case CENTIMETER:
                scale.setSelectedIndex(2);
                break;
            case METER:
                scale.setSelectedIndex(3);
                break;
        }*/

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


                double value = timeStep.getValue();
                if (!(value >= 0.001) || !(value <= 1000)) {
                    Window.alert("Time Step not between 1μs and 1s");
                    return;
                }

/*
                switch (scale.getSelectedItemText()) {
                    case "micrometer":
                        sim.selectedLengthUnit = CirSim.LengthUnit.MICROMETER;
                        break;
                    case "millimeter":
                        sim.selectedLengthUnit = CirSim.LengthUnit.MILLIMETER;
                        break;
                    case "centimeter":
                        sim.selectedLengthUnit = CirSim.LengthUnit.CENTIMETER;
                        break;
                    case "meter":
                        sim.selectedLengthUnit = CirSim.LengthUnit.METER;
                        break;
                }
*/

                sim.dt = value / 1e3;

                apply();
                closeDialog();
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
                        break;
                    default:

                        break;
                }
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
                        break;
                    default:
                        break;
                }
            }
        });
        cyclic.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
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
        if (!sim.simComponents.isEmpty())
            sim.resetAction();
    }
}
