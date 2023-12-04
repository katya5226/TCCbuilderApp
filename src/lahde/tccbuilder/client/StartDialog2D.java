package lahde.tccbuilder.client;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import lahde.tccbuilder.client.util.Locale;

import java.util.ArrayList;

public class StartDialog2D extends Dialog {
    CirSim sim;
    FlowPanel fp;
    HorizontalPanel buttonPanel;
    Button cancelButton;
    Button applyButton;
    DoubleBox timeStep;
    DoubleBox startTemperature;

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

    ArrayList<Widget> leftToggleables = new ArrayList<Widget>();
    ArrayList<Widget> rightToggleables = new ArrayList<Widget>();

    public StartDialog2D(CirSim sim) {
        super();
        setText(Locale.LS("Start TCC"));
        closeOnEnter = true;
        this.sim = sim;
        fp = new FlowPanel();
        setWidget(fp);
        fp.addStyleName("dialogContainer");
        HTML test = new HTML(
                "<iframe src=\"start-help-2D.html\" frameborder=\"0\">" +
                        "</iframe>");

        fp.add(getHelpButton(test));
        westBoundary = new ListBox();
        /*
        westBoundary.addItem("Adiabatic");
        westBoundary.addItem("Constant Heat Flux");
        westBoundary.addItem("Constant Temperature");
        */
        westBoundary.addItem("Convective");
        eastBoundary = new ListBox();
        /*
        eastBoundary.addItem("Adiabatic");
        eastBoundary.addItem("Constant Heat Flux");
        eastBoundary.addItem("Constant Temperature");
        */
        eastBoundary.addItem("Convective");


        timeStep = new DoubleBox();
        timeStep.setValue(sim.simulation2D.dt * 1e3);

        startTemperature = new DoubleBox();
        startTemperature.setValue(sim.simulation2D.startTemp);

        inletHeatFluxLabel = new Label(Locale.LS("Inlet Heat Flux ( W/m² )"));
        westTemperatureLabel = new Label(Locale.LS("West Temperature ( K )"));
        westConvectionCoefficientLabel = new Label(Locale.LS("West Convection Coefficient ( W/(m²K) )"));
        inletHeatFlux = new DoubleBox();
        inletHeatFlux.setValue(sim.simulation2D.qWest);
        westTemperature = new DoubleBox();
        westTemperature.setValue(sim.simulation2D.twoDimBC.T[0]);
        westConvectionCoefficient = new DoubleBox();
        westConvectionCoefficient.setValue(sim.simulation2D.hWest);

        outletHeatFluxLabel = new Label(Locale.LS("Outlet Heat Flux ( W/m² )"));
        eastTemperatureLabel = new Label(Locale.LS("East Temperature ( K )"));
        eastConvectionCoefficientLabel = new Label(Locale.LS("East Convection Coefficient ( W/(m²K) )"));
        outletHeatFlux = new DoubleBox();
        inletHeatFlux.setValue(sim.simulation2D.qEast);
        eastTemperature = new DoubleBox();
        eastTemperature.setValue(sim.simulation2D.twoDimBC.T[1]);
        eastConvectionCoefficient = new DoubleBox();
        eastConvectionCoefficient.setValue(sim.simulation2D.hEast);

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
        fp.add(new Label(Locale.LS("Enter Time Step (ms): ")));
        fp.add(timeStep);
        fp.add(new Label(Locale.LS("Enter starting temperature (K): ")));
        fp.add(startTemperature);
        fp.add(l = new Label(Locale.LS("West Boundary Condition: ")));
        l.addStyleName("dialogHeading");
        fp.add(westBoundary);
        fp.add(inletHeatFluxLabel);
        fp.add(inletHeatFlux);
        fp.add(westTemperatureLabel);
        fp.add(westTemperature);
        fp.add(westConvectionCoefficientLabel);
        fp.add(westConvectionCoefficient);

        fp.add(l = new Label(Locale.LS("East Boundary Condition: ")));
        l.addStyleName("dialogHeading");

        fp.add(eastBoundary);
        fp.add(outletHeatFluxLabel);
        fp.add(outletHeatFlux);
        fp.add(eastTemperatureLabel);
        fp.add(eastTemperature);
        fp.add(eastConvectionCoefficientLabel);
        fp.add(eastConvectionCoefficient);


        applyButton = new Button(Locale.LS("Apply"));
        cancelButton = new Button(Locale.LS("Cancel"));

        for (Widget w : rightToggleables) {
            w.setVisible(false);
        }
        for (Widget w : leftToggleables) {
            w.setVisible(false);
        }
        westTemperatureLabel.setVisible(true);
        westTemperature.setVisible(true);
        westConvectionCoefficientLabel.setVisible(true);
        westConvectionCoefficient.setVisible(true);

        eastTemperatureLabel.setVisible(true);
        eastTemperature.setVisible(true);
        eastConvectionCoefficientLabel.setVisible(true);
        eastConvectionCoefficient.setVisible(true);
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
                if (westTemperature.isVisible()) {
                    double westTempValue = westTemperature.getValue();
                    if (!(westTempValue >= 0.0) || !(westTempValue <= 2000)) {
                        Window.alert("Temperature not between 0 K and 2000 K");
                        return;
                    }
                    sim.simulation2D.twoDimBC.T[0] = westTempValue;
                }
                if (eastTemperature.isVisible()) {
                    double eastTempValue = eastTemperature.getValue();
                    if (!(eastTempValue >= 0.0) || !(eastTempValue <= 2000)) {
                        Window.alert("Temperature not between 0 K and 2000 K");
                        return;
                    }
                    sim.simulation2D.twoDimBC.T[1] = eastTempValue;
                }
                if (westConvectionCoefficient.isVisible()) {
                    double westConvCoeffValue = westConvectionCoefficient.getValue();
                    if (!(westConvCoeffValue >= 0.0)) {
                        Window.alert("Convection coefficient must be positive!");
                        return;
                    }
                    sim.simulation2D.twoDimBC.h[0] = westConvCoeffValue;
                }
                if (eastConvectionCoefficient.isVisible()) {
                    double eastConvCoeffValue = eastConvectionCoefficient.getValue();
                    if (!(eastConvCoeffValue >= 0.0)) {
                        Window.alert("Convection coefficient must be positive!");
                        return;
                    }
                    sim.simulation2D.twoDimBC.h[1] = eastConvCoeffValue;
                }
                if (inletHeatFlux.isVisible()) {
                    double qInValue = inletHeatFlux.getValue();
                    if (!(qInValue >= 0.0)) {
                        Window.alert("Heat flux value must be positive!");
                        return;
                    }
                    sim.simulation2D.qWest = qInValue;
                }
                if (outletHeatFlux.isVisible()) {
                    double qOutValue = outletHeatFlux.getValue();
                    if (!(qOutValue >= 0.0)) {
                        Window.alert("Heat flux value must be positive!");
                        return;
                    }
                    sim.simulation2D.qEast = qOutValue;
                }
                sim.simulation2D.dt = dtValue / 1e3;
                sim.simulation2D.startTemp = startTempValue;
                sim.simulation2D.eastBoundary = Simulation1D.BorderCondition.values()[eastBoundary.getSelectedIndex()];
                sim.simulation2D.westBoundary = Simulation1D.BorderCondition.values()[westBoundary.getSelectedIndex()];

                sim.simulation2D.setTemperatureRange();
                apply();
            }
        });
        westBoundary.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                int selectedIndex = westBoundary.getSelectedIndex();
                String selectedItem = westBoundary.getValue(selectedIndex);
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
                        westTemperatureLabel.setVisible(true);
                        westTemperature.setVisible(true);
                        break;
                    case "Convective":
                        westTemperatureLabel.setVisible(true);
                        westTemperature.setVisible(true);
                        westConvectionCoefficientLabel.setVisible(true);
                        westConvectionCoefficient.setVisible(true);
                        westTemperature.setValue(sim.simulation2D.twoDimBC.T[0]);
                        westConvectionCoefficient.setValue(sim.simulation2D.twoDimBC.h[0]);

                        break;
                    default:

                        break;
                }
                center();
            }
        });
        eastBoundary.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                int selectedIndex = eastBoundary.getSelectedIndex();
                String selectedItem = eastBoundary.getValue(selectedIndex);
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
                        eastTemperatureLabel.setVisible(true);
                        eastTemperature.setVisible(true);
                        break;
                    case "Convective":
                        eastTemperatureLabel.setVisible(true);
                        eastTemperature.setVisible(true);
                        eastConvectionCoefficientLabel.setVisible(true);
                        eastConvectionCoefficient.setVisible(true);
                        eastTemperature.setValue(sim.simulation2D.twoDimBC.T[1]);
                        eastConvectionCoefficient.setValue(sim.simulation2D.twoDimBC.h[1]);
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
        fp.add(buttonPanel);
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

        if (!sim.simulation2D.simTwoDimComponents.isEmpty())
            sim.resetAction();
        closeDialog();
    }

}
