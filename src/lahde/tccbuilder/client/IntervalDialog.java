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

public class IntervalDialog extends Dialog {
    CirSim sim;
    FlowPanel flowPanel;
    HorizontalPanel buttonPanel;
    FlowPanel cyclicContainer;
    Button cancelButton;
    Button applyButton;

    IntegerBox interval;

    public IntervalDialog(CirSim sim) {
        super();

        setText(Locale.LS("Set output interval"));
        closeOnEnter = true;
        this.sim = sim;

        flowPanel = new FlowPanel();
        flowPanel.addStyleName("dialogContainer");
        setWidget(flowPanel);

        interval = new IntegerBox();
        if (sim.simDimensionality == 1) {
            interval.setValue(sim.simulation1D.outputInterval);
        }
        else if (sim.simDimensionality == 2) {
            interval.setValue(sim.simulation2D.outputInterval);
        }

        flowPanel.add(new Label(Locale.LS("Output temperatures every ? time steps:")));
        flowPanel.add(interval);

        applyButton = new Button(Locale.LS("Apply"));
        cancelButton = new Button(Locale.LS("Cancel"));

        // for (Widget w : rightToggleables) {
        //     w.setVisible(false);
        // }
        // for (Widget w : leftToggleables) {
        //     w.setVisible(false);
        // }

        applyButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                int intervalValue = (int)interval.getValue();

                if (sim.simDimensionality == 1) {
                    sim.simulation1D.outputInterval = intervalValue;
                }

                if (sim.simDimensionality == 2) {
                    sim.simulation2D.outputInterval = intervalValue;
                }
                
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
