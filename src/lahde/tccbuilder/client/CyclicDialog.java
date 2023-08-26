
package lahde.tccbuilder.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.ui.*;
import lahde.tccbuilder.client.util.Locale;
import com.google.gwt.user.client.ui.ListBox;

import java.util.*;

public class CyclicDialog extends Dialog {

    final CirSim sim;
    VerticalPanel vp;
    Button applyButton, cancelButton, addComponentButton;
    HorizontalPanel buttonPanel;
    ListBox addBox;

    DoubleBox heatTransferDuration, propsChangeDuration, newRho, newCp, newK;
    Label heatTransferDurationLabel, propsChangeDurationLabel;
    DoubleBox heatFlux, heatFluxDuration, newComponentIndexes, magneticFieldDuration, electricFieldStrength, electricFieldDuration, pressureFieldStrength, shearStressFieldStrength;
    Label heatFluxLabel, heatFluxDurationLabel, newComponentIndexesLabel, magneticFieldStrengthLabel, magneticFieldDurationLabel, electricFieldStrengthLabel, electricFieldDurationLabel, pressureFieldStrengthLabel, shearStressFieldStrengthLabel;
    ListBox availableComponents, magneticComponents, magneticFieldStrength;
    Label componentsLabel, rhoLabel, cpLabel, kLabel;
    HTML cyclePartLabel;
    List<Widget> inputWidgets;

    CyclePart cyclePart;
    List<Integer> componentIndices;
    ThermalControlElement chosenComponent;

    public CyclicDialog(CirSim sim) {
        super();

        setText(lahde.tccbuilder.client.util.Locale.LS("Add Cycle Part"));
        closeOnEnter = true;
        this.sim = sim;


        componentIndices = new ArrayList<Integer>();

        applyButton = new Button(lahde.tccbuilder.client.util.Locale.LS("Add Part"));
        cancelButton = new Button(lahde.tccbuilder.client.util.Locale.LS("Cancel"));
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
        cyclePartLabel = new HTML();
        vp.add(cyclePartLabel);
        addBox = new ListBox();
        vp.add(addBox);
        addBox.addItem("< Choose Cycle Part >");
        addBox.addItem("Heat Transfer");
        //addBox.addItem("Heat Input");
        //addBox.addItem("Mechanic Displacement");
        addBox.addItem("Magnetic Field Change");
        //addBox.addItem("Electric Field Change");
        //addBox.addItem("Pressure change");
        //addBox.addItem("Shear Stress Change");
        addBox.addItem("Properties Change");


        inputWidgets = new ArrayList<>();

        heatFluxLabel = new Label(lahde.tccbuilder.client.util.Locale.LS("Heat Flux (W/m²): "));
        heatFlux = new DoubleBox();
        inputWidgets.add(heatFluxLabel);
        inputWidgets.add(heatFlux);

        componentsLabel = new Label(lahde.tccbuilder.client.util.Locale.LS("Choose components: "));
        magneticComponents = new ListBox();
        magneticComponents.addItem("< Choose Component >");
        componentIndices.clear();
        for (int i = 0; i < sim.simulation1D.simTCEs.size(); i++) {
            GWT.log(sim.simulation1D.simTCEs.get(i).cvs.get(0).material.materialName);
            if (sim.simulation1D.simTCEs.get(i).cvs.get(0).material.magnetocaloric) {
                // TODO: This has to be modified, the flag will depend on the chosen cycle part type.
                componentIndices.add(i);
                magneticComponents.addItem(String.valueOf(sim.simulation1D.simTCEs.get(i).index) + " " + sim.simulation1D.simTCEs.get(i).name);
            }
        }
        inputWidgets.add(componentsLabel);
        inputWidgets.add(magneticComponents);

        availableComponents = new ListBox();
        availableComponents.addItem("< Choose Component >");
        for (int i = 0; i < sim.simulation1D.simTCEs.size(); i++) {
            availableComponents.addItem(String.valueOf(sim.simulation1D.simTCEs.get(i).index) + " " + sim.simulation1D.simTCEs.get(i).name);
        }
        inputWidgets.add(availableComponents);

        heatTransferDurationLabel = new Label(lahde.tccbuilder.client.util.Locale.LS("Duration (s): "));
        heatTransferDuration = new DoubleBox();
        inputWidgets.add(heatTransferDurationLabel);
        inputWidgets.add(heatTransferDuration);

        propsChangeDurationLabel = new Label(lahde.tccbuilder.client.util.Locale.LS("Duration (s): "));
        propsChangeDuration = new DoubleBox();
        inputWidgets.add(propsChangeDurationLabel);
        inputWidgets.add(propsChangeDuration);

        rhoLabel = new Label(lahde.tccbuilder.client.util.Locale.LS("Input new density (kg/m^3): "));
        newRho = new DoubleBox();
        inputWidgets.add(rhoLabel);
        inputWidgets.add(newRho);

        cpLabel = new Label(lahde.tccbuilder.client.util.Locale.LS("Input new specific heat capacity (J/kg/K): "));
        newCp = new DoubleBox();
        inputWidgets.add(cpLabel);
        inputWidgets.add(newCp);

        kLabel = new Label(lahde.tccbuilder.client.util.Locale.LS("Input new thermal conductivity (W/m/K): "));
        newK = new DoubleBox();
        inputWidgets.add(kLabel);
        inputWidgets.add(newK);

        heatFluxDurationLabel = new Label(lahde.tccbuilder.client.util.Locale.LS("Duration (s): "));
        heatFluxDuration = new DoubleBox();
        inputWidgets.add(heatFluxDurationLabel);
        inputWidgets.add(heatFluxDuration);

        newComponentIndexesLabel = new Label(lahde.tccbuilder.client.util.Locale.LS("New Component Indexes: "));
        newComponentIndexes = new DoubleBox();
        inputWidgets.add(newComponentIndexesLabel);
        inputWidgets.add(newComponentIndexes);

        magneticFieldStrengthLabel = new Label(lahde.tccbuilder.client.util.Locale.LS("Field Strength (T): "));
        magneticFieldStrength = new ListBox();
        inputWidgets.add(magneticFieldStrengthLabel);
        inputWidgets.add(magneticFieldStrength);

        magneticFieldDurationLabel = new Label(lahde.tccbuilder.client.util.Locale.LS("Duration (s): "));
        magneticFieldDuration = new DoubleBox();
        inputWidgets.add(magneticFieldDurationLabel);
        inputWidgets.add(magneticFieldDuration);


        electricFieldStrengthLabel = new Label(lahde.tccbuilder.client.util.Locale.LS("Field Strength (V/m): "));
        electricFieldStrength = new DoubleBox();
        inputWidgets.add(electricFieldStrengthLabel);
        inputWidgets.add(electricFieldStrength);

        electricFieldDurationLabel = new Label(lahde.tccbuilder.client.util.Locale.LS("Duration (s): "));
        electricFieldDuration = new DoubleBox();
        inputWidgets.add(electricFieldDurationLabel);
        inputWidgets.add(electricFieldDuration);

        pressureFieldStrengthLabel = new Label(lahde.tccbuilder.client.util.Locale.LS("Field Strength (Bar): "));
        pressureFieldStrength = new DoubleBox();
        inputWidgets.add(pressureFieldStrengthLabel);
        inputWidgets.add(pressureFieldStrength);

        shearStressFieldStrengthLabel = new Label(lahde.tccbuilder.client.util.Locale.LS("Field Strength (Bar): "));
        shearStressFieldStrength = new DoubleBox();
        inputWidgets.add(shearStressFieldStrengthLabel);
        inputWidgets.add(shearStressFieldStrength);

        addComponentButton = new Button(Locale.LS("Add Component"));
        inputWidgets.add(addComponentButton);


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
        buttonPanel.addStyleName("dialogButtonPanel");
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
                switch (cyclePart.partType) {
                    case HEAT_TRANSFER:
                        cyclePart.duration = heatTransferDuration.getValue();
                        break;
                    case HEAT_INPUT:
                        break;
                    case MECHANIC_DISPLACEMENT:
                        break;
                    case MAGNETIC_FIELD_CHANGE:
                        break;
                    case ELECTRIC_FIELD_CHANGE:
                        break;
                    case PRESSURE_CHANGE:
                        break;
                    case SHEAR_STRESS_CHANGE:
                        break;
                    case PROPERTIES_CHANGE:
                        break;
                }

                sim.simulation1D.cycleParts.add(cyclePart);
                printCyclePart(cyclePart, sim.cyclicOperationLabel);
                closeDialog();
            }
        });
        addComponentButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                switch (cyclePart.partType) {
                    case MAGNETIC_FIELD_CHANGE:
                        if (magneticFieldStrength.isVisible() && !cyclePart.TCEs.contains(chosenComponent)) {
                            cyclePart.TCEs.add(chosenComponent);
                            chosenComponent.fieldIndex = magneticFieldStrength.getSelectedIndex();
                            cyclePart.duration = 0.0;
                        }
                        break;
                    case PROPERTIES_CHANGE:
                        if (propsChangeDuration.isVisible() && !cyclePart.TCEs.contains(chosenComponent)) {
                            if (newRho.getValue() == 0 || newCp.getValue() == 0 || newK.getValue() == 0) {
                                Window.alert("Value must be -1 or greater than 0.001!");
                                sim.simulation1D.cycleParts.remove(sim.simulation1D.cycleParts.size() - 1);
                                break;
                            }

                            cyclePart.TCEs.add(chosenComponent);
                            cyclePart.duration = 0.0;
                            cyclePart.newProperties.add(new Vector<Double>());
                            cyclePart.newProperties.lastElement().add(newRho.getValue());
                            cyclePart.newProperties.lastElement().add(newCp.getValue());
                            cyclePart.newProperties.lastElement().add(newK.getValue());
                            for (Vector<Double> v : cyclePart.newProperties) {
                                GWT.log(Arrays.toString(v.toArray()));
                            }
                        }
                        break;
                }
                cyclePartLabel.setHTML("");
                printCyclePart(cyclePart, cyclePartLabel);
                center();
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
                    if (widget instanceof DoubleBox) ((DoubleBox) widget).setText("");
                }
                switch (addBox.getSelectedItemText()) {
                    case "Heat Transfer":
                        heatTransferDurationLabel.setVisible(true);
                        heatTransferDuration.setVisible(true);
                        cyclePart = new CyclePart(sim.simulation1D.cycleParts.size(), sim);
                        cyclePart.partType = CyclePart.PartType.HEAT_TRANSFER;
                        break;
                    case "Mechanic Displacement":
                        newComponentIndexesLabel.setVisible(true);
                        newComponentIndexes.setVisible(true);
                        cyclePart = new CyclePart(sim.simulation1D.cycleParts.size(), sim);
                        cyclePart.partType = CyclePart.PartType.MECHANIC_DISPLACEMENT;
                        break;
                    case "Magnetic Field Change":
                        componentsLabel.setVisible(true);
                        magneticComponents.setVisible(true);
                        // magneticFieldStrengthLabel.setVisible(true);
                        // magneticFieldStrength.setVisible(true);
                        magneticFieldDurationLabel.setVisible(true);
                        magneticFieldDuration.setVisible(true);
                        addComponentButton.setVisible(true);
                        cyclePart = new CyclePart(sim.simulation1D.cycleParts.size(), sim);
                        cyclePart.partType = CyclePart.PartType.MAGNETIC_FIELD_CHANGE;
                        break;
                    case "Electric Field Change":
                        electricFieldStrengthLabel.setVisible(true);
                        electricFieldStrength.setVisible(true);
                        electricFieldDurationLabel.setVisible(true);
                        electricFieldDuration.setVisible(true);
                        cyclePart = new CyclePart(sim.simulation1D.cycleParts.size(), sim);
                        cyclePart.partType = CyclePart.PartType.ELECTRIC_FIELD_CHANGE;
                        break;
                    case "Pressure Change":
                        pressureFieldStrengthLabel.setVisible(true);
                        pressureFieldStrength.setVisible(true);
                        cyclePart = new CyclePart(sim.simulation1D.cycleParts.size(), sim);
                        cyclePart.partType = CyclePart.PartType.PRESSURE_CHANGE;
                        break;
                    case "Shear Stress Change":
                        shearStressFieldStrengthLabel.setVisible(true);
                        shearStressFieldStrength.setVisible(true);
                        cyclePart = new CyclePart(sim.simulation1D.cycleParts.size(), sim);
                        cyclePart.partType = CyclePart.PartType.SHEAR_STRESS_CHANGE;
                        break;
                    case "Properties Change":
                        componentsLabel.setVisible(true);
                        availableComponents.setVisible(true);
                        propsChangeDurationLabel.setVisible(true);
                        propsChangeDuration.setVisible(true);
                        addComponentButton.setVisible(true);
                        cyclePart = new CyclePart(sim.simulation1D.cycleParts.size(), sim);
                        cyclePart.partType = CyclePart.PartType.PROPERTIES_CHANGE;
                        break;
                }
                center();

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
                int ci = componentIndices.get(chosen);
                chosenComponent = sim.simulation1D.simTCEs.get(ci);
                magneticFieldStrength.clear();
                for (int fi = 0; fi < chosenComponent.cvs.get(0).material.fields.size(); fi++) {
                    magneticFieldStrength.addItem(String.valueOf(chosenComponent.cvs.get(0).material.fields.get(fi)));
                }
                magneticFieldStrengthLabel.setVisible(true);
                magneticFieldStrength.setVisible(true);

                GWT.log("Chosen: " + String.valueOf(chosen));
                GWT.log("Indeces size: " + String.valueOf(componentIndices.size()));
                for (int i = 0; i < sim.simulation1D.simTCEs.size(); i++) {
                    GWT.log(String.valueOf(sim.simulation1D.simTCEs.get(i).index) + " " + sim.simulation1D.simTCEs.get(i).name);
                }
                GWT.log("Chosen component: " + chosenComponent.name);
                GWT.log("Material name: " + chosenComponent.cvs.get(0).material.materialName);  // TODO: correct this
                GWT.log("Fields size: " + String.valueOf(chosenComponent.cvs.get(0).material.fields.size()));
                center();

            }
        });

        availableComponents.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                int chosen = availableComponents.getSelectedIndex() - 1;
                if (chosen < 0) {
                    return;
                }
                chosenComponent = sim.simulation1D.simTCEs.get(chosen);
                newRho.setValue(chosenComponent.cvs.get(0).constRho);
                newCp.setValue(chosenComponent.cvs.get(0).constCp);
                newK.setValue(chosenComponent.cvs.get(0).constK);
                rhoLabel.setVisible(true);
                newRho.setVisible(true);
                cpLabel.setVisible(true);
                newCp.setVisible(true);
                kLabel.setVisible(true);
                newK.setVisible(true);
                GWT.log("Chosen: " + String.valueOf(chosen));
                for (int i = 0; i < sim.simulation1D.simTCEs.size(); i++)
                    GWT.log(String.valueOf(sim.simulation1D.simTCEs.get(i).index) + " " + sim.simulation1D.simTCEs.get(i).name);
                GWT.log("Chosen component: " + chosenComponent.name);
                center();

            }
        });

    }

    public void printCyclePart(CyclePart cp, HTML label) {
        if (sim.simulation1D.cycleParts.size() == 1) {
            if (label.getHTML().equals("")) {
                label.setHTML(label.getHTML() + "<b>Cyclic Operation:</b><br>");
                label.setHTML(label.getHTML() + "&emsp;<b>Cyclic</b> " + String.valueOf(sim.simulation1D.cyclic) + "<br>");
            } else {
                label.setHTML("");
            }
        }

        label.setHTML(label.getHTML() + "&emsp;<b>Cycle Part:</b> " + cp.partType + "<br>");
        switch (cp.partType) {
            case HEAT_TRANSFER:
                label.setHTML(label.getHTML() + "&emsp;&emsp;<b>Components: all</b></br>");
                label.setHTML(label.getHTML() + "&emsp;&emsp;<b>Duration:</b>" + NumberFormat.getFormat("#0.0000").format(cp.duration) + " s<br>");
                break;
            case HEAT_INPUT:
                break;
            case MECHANIC_DISPLACEMENT:
                break;
            case MAGNETIC_FIELD_CHANGE:
                label.setHTML(label.getHTML() + "&emsp;&emsp;<b>Components:</b></br>");
                for (ThermalControlElement c : cp.TCEs)
                    label.setHTML(label.getHTML() + "&emsp;&emsp;&emsp;" + c.name + " " + c.index);
                label.setHTML(label.getHTML() + "<br>");
                label.setHTML(label.getHTML() + "&emsp;&emsp;<b>Magnetic Field Strength:</b> </br> ");
                for (ThermalControlElement c : cp.TCEs)
                    label.setHTML(label.getHTML() + "&emsp;&emsp;&emsp;" + c.cvs.get(0).material.fields.get(c.fieldIndex) + "T for " + c.name + " " + c.index + "</br>");
                label.setHTML(label.getHTML() + "&emsp;&emsp;<b>Duration:</b>" + NumberFormat.getFormat("#0.0000").format(cp.duration) + " s<br>");
                break;
            case ELECTRIC_FIELD_CHANGE:
                break;
            case PRESSURE_CHANGE:
                break;
            case SHEAR_STRESS_CHANGE:
                break;
            case PROPERTIES_CHANGE:
                label.setHTML(label.getHTML() + "&emsp;&emsp;<b>Changed properties: </b></br>");
                Vector<ThermalControlElement> TCEs = cp.TCEs;
                for (int i = 0; i < TCEs.size(); i++) {
                    ThermalControlElement c = TCEs.get(i);
                    label.setHTML(label.getHTML() + "&emsp;&emsp;" + c.name + " " + c.index);
                    if (cyclePart.newProperties.get(i).get(0) != -1)
                        label.setHTML(label.getHTML() + "&emsp;rho: " + cyclePart.newProperties.get(i).get(0) + ", ");
                    if (cyclePart.newProperties.get(i).get(1) != -1)
                        label.setHTML(label.getHTML() + "&emsp;cp: " + cyclePart.newProperties.get(i).get(1) + ", ");
                    if (cyclePart.newProperties.get(i).get(2) != -1)
                        label.setHTML(label.getHTML() + "&emsp;k: " + cyclePart.newProperties.get(i).get(2) + ", ");
                }
                label.setHTML(label.getHTML() + "</br>");
                label.setHTML(label.getHTML() + "&emsp;&emsp;<b>Duration:</b>" + NumberFormat.getFormat("#0.0000").format(cp.duration) + " s<br>");
                break;
        }
        center();
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
        if (value != null && value < sim.simulation1D.dt) {
            durationBox.getElement().getStyle().setBorderColor(Color.red.getHexValue());
            durationBox.getElement().getStyle().setColor(Color.red.getHexValue());
        }
    }

}
