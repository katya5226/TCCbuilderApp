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

public class TempeaturesDialog extends Dialog {
    CirSim sim;
    FlowPanel flowPanel;
    HorizontalPanel buttonPanel;
    FlowPanel cyclicContainer;
    Button cancelButton;
    Button applyButton;

    Label minTemperature;
    Label maxTemperature;


    public TempeaturesDialog(CirSim sim) {
        super();

        setText(Locale.LS("Set graph temperature range"));
        closeOnEnter = true;
        this.sim = sim;

        flowPanel = new FlowPanel();
        flowPanel.addStyleName("dialogContainer");
        setWidget(flowPanel);

        minTemperature = new DoubleBox();
        minTemperature.setValue(sim.simulation1D.minTemp);
        maxTemperature = new DoubleBox();
        maxTemperature.setValue(sim.simulation1D.maxTemp);

        flowPanel.add(new Label(Locale.LS("Enter minimum temperature (K): ")));
        flowPanel.add(minTemperature);
        flowPanel.add(new Label(Locale.LS("Enter maximum temperature (K): ")));
        flowPanel.add(maxTemperature);

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
                double minTempValue = minTemperature.getValue();
                if (!(minTempValue >= 0.0) || !(minTempValue <= 2000)) {
                    Window.alert("Temperature not between 0 K and 2000 K");
                    return;
                }
                double maxTempValue = maxTemperature.getValue();
                if (!(maxTempValue >= 0.0) || !(maxTempValue <= 2000)) {
                    Window.alert("Temperature not between 0 K and 2000 K");
                    return;
                }
                sim.simulation1D.minTemp = minTempValue;
                sim.simulation1D.maxTemp = maxTempValue;
                apply();
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

    @Override
    public void enterPressed() {
        if (closeOnEnter) {
            apply();
            closeDialog();
        }
    }

    @Override
    void apply() {
        closeDialog();
    }
}
