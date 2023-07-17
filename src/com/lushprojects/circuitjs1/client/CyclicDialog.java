package com.lushprojects.circuitjs1.client;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.lushprojects.circuitjs1.client.util.Locale;
import com.google.gwt.user.client.ui.ListBox;

import java.util.ArrayList;
import java.util.List;

public class CyclicDialog extends Dialog {

    final CirSim sim;
    VerticalPanel vp;
    Button applyButton, cancelButton;
    HorizontalPanel buttonPanel;
    ListBox addBox;

    DoubleBox heatFlux, heatFluxDuration, newComponentIndexes, magneticFieldStrength, magneticFieldDuration, electricFieldStrength, electricFieldDuration, pressureFieldStrength, shearStressFieldStrength;
    Label heatFluxLabel, heatFluxDurationLabel, newComponentIndexesLabel, magneticFieldStrengthLabel, magneticFieldDurationLabel, electricFieldStrengthLabel, electricFieldDurationLabel, pressureFieldStrengthLabel, shearStressFieldStrengthLabel;

    List<Widget> inputWidgets;

    public CyclicDialog(CirSim sim) {
        super();

        setText(Locale.LS("Cyclic options"));
        closeOnEnter = true;
        this.sim = sim;

        applyButton = new Button(Locale.LS("Apply"));
        cancelButton = new Button(Locale.LS("Cancel"));
        vp = new VerticalPanel();
        setWidget(vp);

        addBox = new ListBox();
        vp.add(addBox);
        addBox.addItem("Heat Input");
        addBox.addItem("Heat Transfer");
        addBox.addItem("Magnetic Field");
        addBox.addItem("Mechanic Displacement");
        addBox.addItem("Electric Field");
        addBox.addItem("Pressure");
        addBox.addItem("Shear Stress");
        addBox.addItem("Properties Change");


        inputWidgets = new ArrayList<>();

        heatFluxLabel = new Label(Locale.LS("Heat Flux (W/m²): "));
        heatFlux = new DoubleBox();
        inputWidgets.add(heatFluxLabel);
        inputWidgets.add(heatFlux);

        heatFluxDurationLabel = new Label(Locale.LS("Duration (s): "));
        heatFluxDuration = new DoubleBox();
        inputWidgets.add(heatFluxDurationLabel);
        inputWidgets.add(heatFluxDuration);

        newComponentIndexesLabel = new Label(Locale.LS("New Component Indexes: "));
        newComponentIndexes = new DoubleBox();
        inputWidgets.add(newComponentIndexesLabel);
        inputWidgets.add(newComponentIndexes);

        magneticFieldStrengthLabel = new Label(Locale.LS("Field Strength (T): "));
        magneticFieldStrength = new DoubleBox();
        inputWidgets.add(magneticFieldStrengthLabel);
        inputWidgets.add(magneticFieldStrength);

        magneticFieldDurationLabel = new Label(Locale.LS("Duration (s): "));
        magneticFieldDuration = new DoubleBox();
        inputWidgets.add(magneticFieldDurationLabel);
        inputWidgets.add(magneticFieldDuration);

        electricFieldStrengthLabel = new Label(Locale.LS("Field Strength (V/m): "));
        electricFieldStrength = new DoubleBox();
        inputWidgets.add(electricFieldStrengthLabel);
        inputWidgets.add(electricFieldStrength);

        electricFieldDurationLabel = new Label(Locale.LS("Duration (s): "));
        electricFieldDuration = new DoubleBox();
        inputWidgets.add(electricFieldDurationLabel);
        inputWidgets.add(electricFieldDuration);

        pressureFieldStrengthLabel = new Label(Locale.LS("Field Strength (Bar): "));
        pressureFieldStrength = new DoubleBox();
        inputWidgets.add(pressureFieldStrengthLabel);
        inputWidgets.add(pressureFieldStrength);

        shearStressFieldStrengthLabel = new Label(Locale.LS("Field Strength (Bar): "));
        shearStressFieldStrength = new DoubleBox();
        inputWidgets.add(shearStressFieldStrengthLabel);
        inputWidgets.add(shearStressFieldStrength);

        for (Widget widget : inputWidgets) {
            widget.setVisible(false);
            vp.add(widget);
        }

        //enable for heat flux
        heatFluxLabel.setVisible(true);
        heatFlux.setVisible(true);
        heatFluxDurationLabel.setVisible(true);
        heatFluxDuration.setVisible(true);

        buttonPanel = new HorizontalPanel();
        buttonPanel.setWidth("100%");
        buttonPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        buttonPanel.add(cancelButton);
        buttonPanel.add(applyButton);
        this.center();

        vp.add(buttonPanel);
        vp.setSpacing(1);

        cancelButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                closeDialog();
            }
        });
        applyButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                if (electricFieldDuration.getText().equals("")) {

                    return;

                }

                closeDialog();
            }
        });
        heatFluxDuration.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                validateDurationInput(heatFluxDuration);
            }
        });

        magneticFieldDuration.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                validateDurationInput(magneticFieldDuration);
            }
        });

        electricFieldDuration.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                validateDurationInput(electricFieldDuration);
            }
        });

// Add other duration keyup handlers for pressureFieldStrength, shearStressFieldStrength, etc.



        addBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                for (Widget widget : inputWidgets) {
                    widget.setVisible(false);
                    if (widget instanceof DoubleBox)
                        ((DoubleBox) widget).setText("");
                }
                switch (addBox.getSelectedItemText()) {
                    case "Heat Transfer":
                        heatFluxLabel.setVisible(true);
                        heatFlux.setVisible(true);
                        heatFluxDurationLabel.setVisible(true);
                        heatFluxDuration.setVisible(true);
                        break;
                    case "Mechanic Displacement":
                        newComponentIndexesLabel.setVisible(true);
                        newComponentIndexes.setVisible(true);
                        break;
                    case "Magnetic Field":
                        magneticFieldStrengthLabel.setVisible(true);
                        magneticFieldStrength.setVisible(true);
                        magneticFieldDurationLabel.setVisible(true);
                        magneticFieldDuration.setVisible(true);
                        break;
                    case "Electric Field":
                        electricFieldStrengthLabel.setVisible(true);
                        electricFieldStrength.setVisible(true);
                        electricFieldDurationLabel.setVisible(true);
                        electricFieldDuration.setVisible(true);
                        break;
                    case "Pressure":
                        pressureFieldStrengthLabel.setVisible(true);
                        pressureFieldStrength.setVisible(true);
                        break;
                    case "Shear Stress":
                        shearStressFieldStrengthLabel.setVisible(true);
                        shearStressFieldStrength.setVisible(true);
                        break;
                }
            }
        });
    }
    private void validateDurationInput(DoubleBox durationBox) {
        try {
            Double dbl = Double.parseDouble(durationBox.getText());
        } catch (NumberFormatException e) {
            durationBox.getElement().getStyle().setBorderColor(Color.red.getHexValue());
            durationBox.getElement().getStyle().setColor(Color.red.getHexValue());
            return;
        }

        durationBox.getElement().getStyle().setBorderColor(Color.black.getHexValue());
        durationBox.getElement().getStyle().setColor(Color.black.getHexValue());

        Double value = durationBox.getValue();
        if (value != null && value < sim.dt) {
            durationBox.getElement().getStyle().setBorderColor(Color.red.getHexValue());
            durationBox.getElement().getStyle().setColor(Color.red.getHexValue());
        }
    }

}
