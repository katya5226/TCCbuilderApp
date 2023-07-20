package com.lushprojects.circuitjs1.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.ui.*;
import com.lushprojects.circuitjs1.client.util.Locale;
import com.google.gwt.user.client.ui.ListBox;

import java.util.*;

public class CyclicDialog extends Dialog {

    final CirSim sim;
    VerticalPanel vp;
    Button applyButton, cancelButton;
    HorizontalPanel buttonPanel;
    ListBox addBox;

    DoubleBox heatTransferDuration, propsChangeDuration, newRho, newCp, newK;
    Label heatTransferDurationLabel, propsChangeDurationLabel;
    DoubleBox heatFlux, heatFluxDuration, newComponentIndexes, magneticFieldDuration, electricFieldStrength, electricFieldDuration, pressureFieldStrength, shearStressFieldStrength;
    Label heatFluxLabel, heatFluxDurationLabel, newComponentIndexesLabel, magneticFieldStrengthLabel, magneticFieldDurationLabel, electricFieldStrengthLabel, electricFieldDurationLabel, pressureFieldStrengthLabel, shearStressFieldStrengthLabel;
    ListBox availableComponents, magneticComponents, magneticFieldStrength;
    Label componentsLabel, rhoLabel, cpLabel, kLabel;

    List<Widget> inputWidgets;

    CyclePart cyclePart;
    List<Integer> componentIndices;
    Component chosenComponent;

    public CyclicDialog(CirSim sim) {
        super();

        setText(Locale.LS("Cyclic options"));
        closeOnEnter = true;
        this.sim = sim;

        cyclePart = sim.cycleParts.get(sim.cycleParts.size() - 1);
        componentIndices = new ArrayList<Integer>();

        applyButton = new Button(Locale.LS("Apply"));
        cancelButton = new Button(Locale.LS("Cancel"));
        vp = new VerticalPanel();
        setWidget(vp);

        // CyclePart types:
        //      - 0 heat transfer
        //      - 1 heat input
        //      - 2 mechanic displacement
        //      - 3 magnetic field change
        //      - 4 electric field change
        //      - 5 pressure change
        //      - 6 shear stress change
        //      - 7 properties change 

        addBox = new ListBox();
        vp.add(addBox);
        addBox.addItem("< Choose Cycle Part >");
        addBox.addItem("Heat Transfer");
        addBox.addItem("Heat Input");
        addBox.addItem("Mechanic Displacement");
        addBox.addItem("Magnetic Field Change");
        addBox.addItem("Electric Field Change");
        addBox.addItem("Pressure change");
        addBox.addItem("Shear Stress Change");
        addBox.addItem("Properties Change");


        inputWidgets = new ArrayList<>();

        heatFluxLabel = new Label(Locale.LS("Heat Flux (W/m²): "));
        heatFlux = new DoubleBox();
        inputWidgets.add(heatFluxLabel);
        inputWidgets.add(heatFlux);

        componentsLabel = new Label(Locale.LS("Choose components: "));
        magneticComponents = new ListBox();
        magneticComponents.addItem("< Choose Component >");
        componentIndices.clear();
        for (int i = 0; i < sim.simComponents.size(); i++) {
            if (sim.simComponents.get(i).material.magnetocaloric) {  // TODO: This has to be modified, the flag will depend on the chosen cycle part type.
                componentIndices.add(i);
                magneticComponents.addItem(String.valueOf(sim.simComponents.get(i).index) + " " + sim.simComponents.get(i).name);
            }
        }
        inputWidgets.add(componentsLabel);
        inputWidgets.add(magneticComponents);

        availableComponents = new ListBox();
        availableComponents.addItem("< Choose Component >");
        for (int i = 0; i < sim.simComponents.size(); i++) {
            availableComponents.addItem(String.valueOf(sim.simComponents.get(i).index) + " " + sim.simComponents.get(i).name);
        }
        inputWidgets.add(availableComponents);

        heatTransferDurationLabel = new Label(Locale.LS("Duration (s): "));
        heatTransferDuration = new DoubleBox();
        inputWidgets.add(heatTransferDurationLabel);
        inputWidgets.add(heatTransferDuration);

        propsChangeDurationLabel = new Label(Locale.LS("Duration (s): "));
        propsChangeDuration = new DoubleBox();
        inputWidgets.add(propsChangeDurationLabel);
        inputWidgets.add(propsChangeDuration);

        rhoLabel = new Label(Locale.LS("Input new density (kg/m^3): "));
        newRho = new DoubleBox();
        inputWidgets.add(rhoLabel);
        inputWidgets.add(newRho);

        cpLabel = new Label(Locale.LS("Input new specific heat capacity (J/kg/K): "));
        newCp = new DoubleBox();
        inputWidgets.add(cpLabel);
        inputWidgets.add(newCp);

        kLabel = new Label(Locale.LS("Input new thermal conductivity (W/m/K): "));
        newK = new DoubleBox();
        inputWidgets.add(kLabel);
        inputWidgets.add(newK);

        heatFluxDurationLabel = new Label(Locale.LS("Duration (s): "));
        heatFluxDuration = new DoubleBox();
        inputWidgets.add(heatFluxDurationLabel);
        inputWidgets.add(heatFluxDuration);

        newComponentIndexesLabel = new Label(Locale.LS("New Component Indexes: "));
        newComponentIndexes = new DoubleBox();
        inputWidgets.add(newComponentIndexesLabel);
        inputWidgets.add(newComponentIndexes);

        magneticFieldStrengthLabel = new Label(Locale.LS("Field Strength (T): "));
        magneticFieldStrength = new ListBox();
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
        heatFluxLabel.setVisible(false);
        heatFlux.setVisible(false);
        heatFluxDurationLabel.setVisible(false);
        heatFluxDuration.setVisible(false);

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
                if (magneticComponents.isVisible()) {
                    if (magneticFieldStrength.isVisible()) {
                        cyclePart.partType = 3;
                        chosenComponent.fieldIndex = magneticFieldStrength.getSelectedIndex();
                    }
                    if (magneticFieldDuration.isVisible()) {
                        // cyclePart.duration = magneticFieldDuration.getText().equals("") ? 0 : magneticFieldDuration.getValue();
                        cyclePart.duration = 0.0;
                        // Only 0.0 is allowed at the moment.
                    }
                }
                if (heatTransferDuration.isVisible()) {
                    cyclePart.partType = 0;
                    cyclePart.duration = heatTransferDuration.getValue();
                }
                if (propsChangeDuration.isVisible()) {
                    cyclePart.partType = 7;
                    // cyclePart.duration = propsChangeDuration.getValue();
                    cyclePart.duration = 0.0;
                    // Only 0.0 is allowed at the moment.
                    if (newRho.getValue() == 0 || newCp.getValue() == 0 || newK.getValue() == 0) {
                        Window.alert("Value must be -1 or greater than 0.001!");
                        sim.cycleParts.remove(sim.cycleParts.size()-1);
                    }
                    cyclePart.newProperties.get(0).add(newRho.getValue());
                    cyclePart.newProperties.get(0).add(newCp.getValue());
                    cyclePart.newProperties.get(0).add(newK.getValue());
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
                        heatTransferDurationLabel.setVisible(true);
                        heatTransferDuration.setVisible(true);
                        break;
                    case "Mechanic Displacement":
                        newComponentIndexesLabel.setVisible(true);
                        newComponentIndexes.setVisible(true);
                        break;
                    case "Magnetic Field Change":
                        componentsLabel.setVisible(true);
                        magneticComponents.setVisible(true);
                        // magneticFieldStrengthLabel.setVisible(true);
                        // magneticFieldStrength.setVisible(true);
                        magneticFieldDurationLabel.setVisible(true);
                        magneticFieldDuration.setVisible(true);
                        break;
                    case "Electric Field Change":
                        electricFieldStrengthLabel.setVisible(true);
                        electricFieldStrength.setVisible(true);
                        electricFieldDurationLabel.setVisible(true);
                        electricFieldDuration.setVisible(true);
                        break;
                    case "Pressure Change":
                        pressureFieldStrengthLabel.setVisible(true);
                        pressureFieldStrength.setVisible(true);
                        break;
                    case "Shear Stress Change":
                        shearStressFieldStrengthLabel.setVisible(true);
                        shearStressFieldStrength.setVisible(true);
                        break;
                    case "Properties Change":
                        componentsLabel.setVisible(true);
                        availableComponents.setVisible(true);
                        propsChangeDurationLabel.setVisible(true);
                        propsChangeDuration.setVisible(true);   
                        break;
                }
            }
        });

        magneticComponents.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                int chosen = magneticComponents.getSelectedIndex() - 1;
                if (chosen < 0) {
                    magneticFieldStrengthLabel.setVisible(false);
                    magneticFieldStrength.setVisible(false);
                    return;
                }
                GWT.log("Chosen: " + String.valueOf(chosen));
                GWT.log("Indeces size: " + String.valueOf(componentIndices.size()));
                int ci = componentIndices.get(chosen);
                for (int i = 0; i < sim.simComponents.size(); i++) {
                    GWT.log(String.valueOf(sim.simComponents.get(i).index) + " " + sim.simComponents.get(i).name);
                }
                chosenComponent = sim.simComponents.get(ci);
                GWT.log("Chosen component: " + chosenComponent.name);
                cyclePart.components.add(chosenComponent);
                magneticFieldStrength.clear();
                GWT.log("Material name: " + chosenComponent.material.name);
                GWT.log("Fields size: " + String.valueOf(chosenComponent.material.fields.size()));
                for (int fi = 0; fi < chosenComponent.material.fields.size(); fi++) {
                    magneticFieldStrength.addItem(String.valueOf(chosenComponent.material.fields.get(fi)));
                }
                magneticFieldStrengthLabel.setVisible(true);
                magneticFieldStrength.setVisible(true);
            }
        });

        availableComponents.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                int chosen = availableComponents.getSelectedIndex() - 1;
                if (chosen < 0) {
                    return;
                }
                GWT.log("Chosen: " + String.valueOf(chosen));
                for (int i = 0; i < sim.simComponents.size(); i++) {
                    GWT.log(String.valueOf(sim.simComponents.get(i).index) + " " + sim.simComponents.get(i).name);
                }
                chosenComponent = sim.simComponents.get(chosen);
                GWT.log("Chosen component: " + chosenComponent.name);
                cyclePart.components.add(chosenComponent);
                cyclePart.newProperties.add(new Vector<Double>());
                newRho.setValue(chosenComponent.cvs.get(0).const_rho);
                newCp.setValue(chosenComponent.cvs.get(0).const_cp);
                newK.setValue(chosenComponent.cvs.get(0).const_k);
                rhoLabel.setVisible(true);
                newRho.setVisible(true);
                cpLabel.setVisible(true);
                newCp.setVisible(true);
                kLabel.setVisible(true);
                newK.setVisible(true);             
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
