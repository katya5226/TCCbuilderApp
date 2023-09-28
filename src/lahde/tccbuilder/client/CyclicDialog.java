package lahde.tccbuilder.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.ui.*;
import lahde.tccbuilder.client.util.Locale;
import com.google.gwt.user.client.ui.ListBox;

import java.util.*;

public class CyclicDialog extends Dialog {

    final CirSim sim;
    FlowPanel vp;
    Button applyButton, cancelButton, addComponentButton;
    HorizontalPanel buttonPanel;
    ListBox cyclePartListBox;
    ListBox componentsListBox;
    ListBox magneticFieldListBox;
    DoubleBox duration;
    DoubleBox newRho;
    DoubleBox newCp;
    DoubleBox newK;

    Checkbox rhoCheckBox;
    Checkbox cpCheckBox;
    Checkbox kCheckBox;

    DoubleBox heatInput;
    DoubleBox newIndex;
    DoubleBox electricFieldStrength;
    DoubleBox pressureFieldStrength;
    DoubleBox shearStressFieldStrength;
    DoubleBox newTemperature;
    Label heatInputLabel;
    Label durationLabel;
    Label newIndexLabel;
    Label magneticFieldStrengthLabel;
    Label electricFieldStrengthLabel;
    Label pressureFieldStrengthLabel;
    Label shearStressFieldStrengthLabel;
    Label newTemperatureLabel;
    Label componentsLabel;
    Label rhoLabel;
    Label cpLabel;
    Label kLabel;
    HTML cyclePartLabel;
    List<Widget> inputWidgets;

    CyclePart cyclePart;
    List<ThermalControlElement> availableTCEs;
    ThermalControlElement chosenComponent;

    public CyclicDialog(CirSim sim) {
        super();

        setText(lahde.tccbuilder.client.util.Locale.LS("Add Cycle Part"));
        closeOnEnter = true;
        this.sim = sim;

        availableTCEs = new ArrayList<ThermalControlElement>();

        applyButton = new Button(lahde.tccbuilder.client.util.Locale.LS("Add Part"));
        cancelButton = new Button(lahde.tccbuilder.client.util.Locale.LS("Cancel"));
        vp = new FlowPanel();
        vp.addStyleName("dialogContainer");
        setWidget(vp);

        inputWidgets = new ArrayList<>();

        cyclePartLabel = new HTML();
        cyclePartLabel.addStyleName("cycle-part-outer");
        cyclePartListBox = new ListBox();
        vp.add(cyclePartLabel);
        vp.add(cyclePartListBox);

        durationLabel = new Label(lahde.tccbuilder.client.util.Locale.LS("Part Duration (s): "));
        duration = new DoubleBox();
        inputWidgets.add(durationLabel);
        inputWidgets.add(duration);

        cyclePartListBox.addItem("< Choose Cycle Part >");
        for (CyclePart.PartType pt : CyclePart.PartType.values())
            cyclePartListBox.addItem(pt.toSpacedCamelCase());
/*        cyclePartListBox.addItem("Heat Transfer");
        cyclePartListBox.addItem("Heat Input");
        cyclePartListBox.addItem("Mechanic Displacement");
        cyclePartListBox.addItem("Magnetic Field Change");
        //addBox.addItem("Electric Field Change");
        //addBox.addItem("Shear Stress Change");
        //addBox.addItem("Pressure change");
        cyclePartListBox.addItem("Properties Change");
        cyclePartListBox.addItem("Temperature Change");*/


        componentsLabel = new Label(lahde.tccbuilder.client.util.Locale.LS("Choose components: "));
        componentsListBox = new ListBox();
        inputWidgets.add(componentsLabel);
        inputWidgets.add(componentsListBox);

        heatInputLabel = new Label(lahde.tccbuilder.client.util.Locale.LS("Heat Input (W/m²): "));
        heatInput = new DoubleBox();
        inputWidgets.add(heatInputLabel);
        inputWidgets.add(heatInput);

        rhoCheckBox = new Checkbox("New density (kg/m^3): ");
        inputWidgets.add(rhoCheckBox);
        rhoCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                newRho.setVisible(!newRho.isVisible());
            }
        });

        rhoLabel = new Label(Locale.LS("Input new density (kg/m^3): "));
        newRho = new DoubleBox();
        inputWidgets.add(rhoLabel);
        inputWidgets.add(newRho);

        cpCheckBox = new Checkbox("New specific heat capacity (J/kg/K): ");
        inputWidgets.add(cpCheckBox);

        cpCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                newCp.setVisible(!newCp.isVisible());
            }
        });

        cpLabel = new Label(lahde.tccbuilder.client.util.Locale.LS("Input new specific heat capacity (J/kg/K): "));
        newCp = new DoubleBox();
        inputWidgets.add(cpLabel);
        inputWidgets.add(newCp);


        kCheckBox = new Checkbox("New thermal conductivity (W/m/K): ");
        inputWidgets.add(kCheckBox);

        kCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                newK.setVisible(!newK.isVisible());
            }
        });
        kLabel = new Label(lahde.tccbuilder.client.util.Locale.LS("Input new thermal conductivity (W/m/K): "));
        newK = new DoubleBox();
        inputWidgets.add(kLabel);
        inputWidgets.add(newK);


/*

        heatFluxDurationLabel = new Label(lahde.tccbuilder.client.util.Locale.LS("Duration (s): "));
        heatFluxDuration = new DoubleBox();
        inputWidgets.add(heatFluxDurationLabel);
        inputWidgets.add(heatFluxDuration);
*/

        newIndexLabel = new Label(lahde.tccbuilder.client.util.Locale.LS("New Index: "));
        newIndex = new DoubleBox();
        inputWidgets.add(newIndexLabel);
        inputWidgets.add(newIndex);

        magneticFieldStrengthLabel = new Label(lahde.tccbuilder.client.util.Locale.LS("Field Strength (T): "));
        magneticFieldListBox = new ListBox();
        inputWidgets.add(magneticFieldStrengthLabel);
        inputWidgets.add(magneticFieldListBox);


        electricFieldStrengthLabel = new Label(lahde.tccbuilder.client.util.Locale.LS("Field Strength (V/m): "));
        electricFieldStrength = new DoubleBox();
        inputWidgets.add(electricFieldStrengthLabel);
        inputWidgets.add(electricFieldStrength);


        pressureFieldStrengthLabel = new Label(lahde.tccbuilder.client.util.Locale.LS("Field Strength (Bar): "));
        pressureFieldStrength = new DoubleBox();
        inputWidgets.add(pressureFieldStrengthLabel);
        inputWidgets.add(pressureFieldStrength);

        shearStressFieldStrengthLabel = new Label(lahde.tccbuilder.client.util.Locale.LS("Field Strength (Bar): "));
        shearStressFieldStrength = new DoubleBox();
        inputWidgets.add(shearStressFieldStrengthLabel);
        inputWidgets.add(shearStressFieldStrength);


        newTemperatureLabel = new Label(lahde.tccbuilder.client.util.Locale.LS("Temperature (K): "));
        newTemperature = new DoubleBox();
        inputWidgets.add(newTemperatureLabel);
        inputWidgets.add(newTemperature);

        addComponentButton = new Button(Locale.LS("Add Component"));
        inputWidgets.add(addComponentButton);


        duration.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                duration.setText("");
            }
        });


        for (Widget widget : inputWidgets) {
            widget.setVisible(false);
            vp.add(widget);
        }


        buttonPanel = new HorizontalPanel();
        buttonPanel.setWidth("100%");
        buttonPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        buttonPanel.addStyleName("dialogButtonPanel");
        buttonPanel.add(cancelButton);
        buttonPanel.add(applyButton);
        this.center();

        vp.add(buttonPanel);

        cancelButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                closeDialog();
            }
        });
        applyButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                switch (cyclePart.partType) {
                    case HEAT_TRANSFER:
                    case PROPERTIES_CHANGE:
                    case TEMPERATURE_CHANGE:
                    case VALUE_CHANGE:
                    case HEAT_INPUT:
                    case ELECTRIC_FIELD_CHANGE:
                    case PRESSURE_CHANGE:
                    case SHEAR_STRESS_CHANGE:
                        cyclePart.duration = duration.getValue();
                        break;
                    case TOGGLE_THERMAL_CONTROL_ELEMENT:
                    case MAGNETIC_FIELD_CHANGE:
                    case MECHANIC_DISPLACEMENT:
                        cyclePart.duration = 0.0;
                        break;
                }
                sim.simulation1D.cycleParts.add(cyclePart);
                sim.fillCyclicPanel();
//                printCyclePart(cyclePart, sim.cyclicOperationLabel);
                closeDialog();
            }
        });
        addComponentButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (!cyclePart.TCEs.contains(chosenComponent))
                    switch (cyclePart.partType) {
                        case HEAT_TRANSFER:
                            break;
                        case HEAT_INPUT:
                            cyclePart.TCEs.add(chosenComponent);
                            cyclePart.heatInputs.add(heatInput.getValue());
                            break;
                        case MECHANIC_DISPLACEMENT:
                            cyclePart.TCEs.add(chosenComponent);
                            cyclePart.newIndexes.add(newIndex.getValue().intValue());
                            break;
                        case MAGNETIC_FIELD_CHANGE:
                            cyclePart.TCEs.add(chosenComponent);
                            chosenComponent.fieldIndex = magneticFieldListBox.getSelectedIndex();
                            cyclePart.newFieldIndexes.add(chosenComponent.fieldIndex);
                            break;
                        case ELECTRIC_FIELD_CHANGE:
                            break;
                        case PRESSURE_CHANGE:
                            break;
                        case SHEAR_STRESS_CHANGE:
                            break;
                        case PROPERTIES_CHANGE:
                            if (newRho.getValue() == 0 || newCp.getValue() == 0 || newK.getValue() == 0) {
                                Window.alert("Value must be -1 or greater than 0.001!");
                                sim.simulation1D.cycleParts.remove(sim.simulation1D.cycleParts.size() - 1);
                                break;
                            }

                            cyclePart.TCEs.add(chosenComponent);
                            cyclePart.newProperties.add(new Vector<Double>());
                            cyclePart.newProperties.lastElement().add(newRho.getValue());
                            cyclePart.newProperties.lastElement().add(newCp.getValue());
                            cyclePart.newProperties.lastElement().add(newK.getValue());

                            break;
                        case TEMPERATURE_CHANGE:
                            cyclePart.TCEs.add(chosenComponent);
                            cyclePart.newTemperatures.add(newTemperature.getValue());
                            break;
                        case TOGGLE_THERMAL_CONTROL_ELEMENT:
                            cyclePart.TCEs.add(chosenComponent);
                            break;
                        case VALUE_CHANGE:
                            cyclePart.TCEs.add(chosenComponent);
                            cyclePart.changedValues.add(new Vector<>());
//                            GWT.log(cyclePart.changedValues.lastElement().toString());
                            CyclePart.PropertyValuePair propertyValuePair = null;
                            if (newRho.isVisible() && !newRho.getText().isEmpty())
                                cyclePart.changedValues.lastElement().add(propertyValuePair = new CyclePart.PropertyValuePair(Simulation.Property.DENSITY, newRho.getValue()));
                            if (newCp.isVisible() && !newCp.getText().isEmpty())
                                cyclePart.changedValues.lastElement().add(propertyValuePair = new CyclePart.PropertyValuePair(Simulation.Property.SPECIFIC_HEAT_CAPACITY, newCp.getValue()));
                            if (newK.isVisible() && !newK.getText().isEmpty())
                                cyclePart.changedValues.lastElement().add(propertyValuePair = new CyclePart.PropertyValuePair(Simulation.Property.THERMAL_CONDUCTIVITY, newK.getValue()));
                            break;
                    }
                else {
                    int index = cyclePart.TCEs.indexOf(chosenComponent);
                    switch (cyclePart.partType) {
                        case HEAT_TRANSFER:
                            break;
                        case HEAT_INPUT:
                            cyclePart.heatInputs.set(index, heatInput.getValue());
                            break;
                        case MECHANIC_DISPLACEMENT:
                            cyclePart.newIndexes.set(index, newIndex.getValue().intValue());
                            break;
                        case MAGNETIC_FIELD_CHANGE:
                            chosenComponent.fieldIndex = magneticFieldListBox.getSelectedIndex();
                            cyclePart.newFieldIndexes.set(index, chosenComponent.fieldIndex);
                            break;
                        case ELECTRIC_FIELD_CHANGE:
                            break;
                        case PRESSURE_CHANGE:
                            break;
                        case SHEAR_STRESS_CHANGE:
                            break;
                        case PROPERTIES_CHANGE:
                            if (newRho.getValue() == 0 || newCp.getValue() == 0 || newK.getValue() == 0) {
                                Window.alert("Value must be -1 or greater than 0.001!");
                                sim.simulation1D.cycleParts.remove(sim.simulation1D.cycleParts.size() - 1);
                                break;
                            }

                            cyclePart.newProperties.set(index, new Vector<Double>());
                            cyclePart.newProperties.get(index).add(newRho.getValue());
                            cyclePart.newProperties.get(index).add(newCp.getValue());
                            cyclePart.newProperties.get(index).add(newK.getValue());

                            break;
                        case TEMPERATURE_CHANGE:
                            cyclePart.newTemperatures.set(index, newTemperature.getValue());
                            sim.simulation1D.setTemperatureRange();
                            break;
                        case TOGGLE_THERMAL_CONTROL_ELEMENT:
                            break;
                        case VALUE_CHANGE:
                            cyclePart.changedValues.set(index, new Vector<>());
                            if (newRho.isVisible() && !newRho.getText().isEmpty())
                                cyclePart.changedValues.get(index).add(new CyclePart.PropertyValuePair(Simulation.Property.DENSITY, newRho.getValue()));
                            if (newCp.isVisible() && !newCp.getText().isEmpty())
                                cyclePart.changedValues.get(index).add(new CyclePart.PropertyValuePair(Simulation.Property.SPECIFIC_HEAT_CAPACITY, newCp.getValue()));
                            if (newK.isVisible() && !newK.getText().isEmpty())
                                cyclePart.changedValues.get(index).add(new CyclePart.PropertyValuePair(Simulation.Property.THERMAL_CONDUCTIVITY, newK.getValue()));
                            break;
                    }
                }


                //just for cycle part display, will be overridden when clicking apply
                if (duration.isVisible()) {
                    cyclePart.duration = duration.getValue();
                    cyclePartLabel.setHTML("");
                }


                CirSim.theSim.fillCyclicPanel();
                center();
            }
        });
        duration.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                validateDurationInput(duration);
            }
        });

        cyclePartListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                for (Widget widget : inputWidgets) {
                    widget.setVisible(false);
                    if (widget instanceof DoubleBox) ((DoubleBox) widget).setText("");
                }
                duration.setValue(1.0);
                switch (cyclePartListBox.getSelectedItemText()) {
                    case "Heat Transfer":
                        durationLabel.setVisible(true);
                        duration.setVisible(true);
                        cyclePart = new CyclePart(sim.simulation1D.cycleParts.size(), sim);
                        cyclePart.partType = CyclePart.PartType.HEAT_TRANSFER;
                        break;
                    case "Heat Input":
                        componentsLabel.setVisible(true);
                        componentsListBox.setVisible(true);
                        durationLabel.setVisible(true);
                        duration.setVisible(true);

                        cyclePart = new CyclePart(sim.simulation1D.cycleParts.size(), sim);
                        cyclePart.partType = CyclePart.PartType.HEAT_INPUT;
                        break;
                    case "Mechanic Displacement":
                        componentsLabel.setVisible(true);
                        componentsListBox.setVisible(true);
                        durationLabel.setVisible(true);
                        duration.setVisible(true);

                        cyclePart = new CyclePart(sim.simulation1D.cycleParts.size(), sim);
                        cyclePart.partType = CyclePart.PartType.MECHANIC_DISPLACEMENT;
                        break;
                    case "Magnetic Field Change":
                        componentsLabel.setVisible(true);
                        componentsListBox.setVisible(true);
//                        durationLabel.setVisible(true);
//                        duration.setVisible(true);

                        cyclePart = new CyclePart(sim.simulation1D.cycleParts.size(), sim);
                        cyclePart.partType = CyclePart.PartType.MAGNETIC_FIELD_CHANGE;
                        break;
                    case "Electric Field Change":
                        electricFieldStrengthLabel.setVisible(true);
                        electricFieldStrength.setVisible(true);
                        durationLabel.setVisible(true);
                        duration.setVisible(true);
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
                        componentsListBox.setVisible(true);
                        durationLabel.setVisible(true);
                        duration.setVisible(true);

                        cyclePart = new CyclePart(sim.simulation1D.cycleParts.size(), sim);
                        cyclePart.partType = CyclePart.PartType.PROPERTIES_CHANGE;
                        break;
                    case "Temperature Change":
                        componentsLabel.setVisible(true);
                        componentsListBox.setVisible(true);
                        durationLabel.setVisible(true);
                        duration.setVisible(true);

                        cyclePart = new CyclePart(sim.simulation1D.cycleParts.size(), sim);
                        cyclePart.partType = CyclePart.PartType.TEMPERATURE_CHANGE;
                        break;
                    case "Toggle Thermal Control Element":
                        componentsLabel.setVisible(true);
                        componentsListBox.setVisible(true);
                        cyclePart = new CyclePart(sim.simulation1D.cycleParts.size(), sim);
                        cyclePart.partType = CyclePart.PartType.TOGGLE_THERMAL_CONTROL_ELEMENT;
                        break;
                    case "Value Change":
                        componentsLabel.setVisible(true);
                        componentsListBox.setVisible(true);
                        durationLabel.setVisible(true);
                        duration.setVisible(true);

                        cyclePart = new CyclePart(sim.simulation1D.cycleParts.size(), sim);
                        cyclePart.partType = CyclePart.PartType.VALUE_CHANGE;
                        break;
                    default:
                        Window.alert("Please select a cycle part");
                        return;
                }
                fillComponentListBox();
                center();

            }
        });

        componentsListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {

                int chosen = componentsListBox.getSelectedIndex() - 1;
                if (chosen < 0) {
                    heatInput.setVisible(false);
                    heatInputLabel.setVisible(false);
                    newIndex.setVisible(false);
                    newIndexLabel.setVisible(false);
                    magneticFieldStrengthLabel.setVisible(false);
                    magneticFieldListBox.setVisible(false);
                    rhoLabel.setVisible(false);
                    newRho.setVisible(false);
                    cpLabel.setVisible(false);
                    newCp.setVisible(false);
                    kLabel.setVisible(false);
                    newK.setVisible(false);
                    newTemperatureLabel.setVisible(false);
                    newTemperature.setVisible(false);
                    addComponentButton.setVisible(false);
                    return;
                }
                chosenComponent = availableTCEs.get(chosen);

                switch (cyclePart.partType) {
                    case HEAT_TRANSFER:
                        break;
                    case HEAT_INPUT:
                        heatInput.setVisible(true);
                        heatInputLabel.setVisible(true);
                        addComponentButton.setVisible(true);

                        break;
                    case MECHANIC_DISPLACEMENT:
                        newIndex.setVisible(true);
                        newIndexLabel.setVisible(true);
                        addComponentButton.setVisible(true);

                        break;
                    case MAGNETIC_FIELD_CHANGE:
                        magneticFieldListBox.clear();
                        for (int fi = 0; fi < chosenComponent.material.fields.size(); fi++) {
                            magneticFieldListBox.addItem(String.valueOf(chosenComponent.material.fields.get(fi)));
                        }
                        magneticFieldStrengthLabel.setVisible(true);
                        magneticFieldListBox.setVisible(true);
                        addComponentButton.setVisible(true);

                        break;
                    case ELECTRIC_FIELD_CHANGE:
                        break;
                    case PRESSURE_CHANGE:
                        break;
                    case SHEAR_STRESS_CHANGE:
                        break;
                    case PROPERTIES_CHANGE:
                        newRho.setValue(chosenComponent.constRho);
                        newCp.setValue(chosenComponent.constCp);
                        newK.setValue(chosenComponent.constK);
                        rhoLabel.setVisible(true);
                        newRho.setVisible(true);
                        cpLabel.setVisible(true);
                        newCp.setVisible(true);
                        kLabel.setVisible(true);
                        newK.setVisible(true);
                        addComponentButton.setVisible(true);

                        break;
                    case TEMPERATURE_CHANGE:
                        newTemperatureLabel.setVisible(true);
                        newTemperature.setVisible(true);
                        addComponentButton.setVisible(true);

                        break;
                    case TOGGLE_THERMAL_CONTROL_ELEMENT:
                        addComponentButton.setVisible(true);
                        break;
                    case VALUE_CHANGE:
                        rhoCheckBox.setVisible(true);
                        cpCheckBox.setVisible(true);
                        kCheckBox.setVisible(true);
                        addComponentButton.setVisible(true);

                        break;
                }


//                GWT.log("Chosen: " + chosen);
///*                GWT.log("Indices size: " + String.valueOf(availableTCEs.size()));
//                for (int i = 0; i < sim.simulation1D.simTCEs.size(); i++) {
//                    GWT.log(String.valueOf(sim.simulation1D.simTCEs.get(i).index) + " " + sim.simulation1D.simTCEs.get(i).name);
//                }*/
//                GWT.log("Chosen component: " + chosenComponent.name);
//                GWT.log("Material name: " + chosenComponent.material.materialName);
//                GWT.log("Fields size: " + chosenComponent.material.fields.size());
                center();

            }
        });


    }

    private void fillComponentListBox() {
        componentsListBox.clear();
        componentsListBox.addItem("< Choose Component >");
        for (ThermalControlElement tce : sim.simulation1D.simTCEs) {
            boolean add = true;
            switch (cyclePart.partType) {
                case HEAT_TRANSFER:
                    break;
                case HEAT_INPUT:
                    break;
                case MECHANIC_DISPLACEMENT:
                    break;
                case MAGNETIC_FIELD_CHANGE:
                    add = tce.material.magnetocaloric;
                    break;
                case ELECTRIC_FIELD_CHANGE:
                    break;
                case PRESSURE_CHANGE:
                    break;
                case SHEAR_STRESS_CHANGE:
                    break;
                case PROPERTIES_CHANGE:
                    break;
                case TEMPERATURE_CHANGE:
                    break;
                case TOGGLE_THERMAL_CONTROL_ELEMENT:
                    add = tce instanceof SwitchElm;
                    break;
                case VALUE_CHANGE:
                    break;

            }
            if (add) {
                availableTCEs.add(tce);
                componentsListBox.addItem(tce.index + " " + tce.name);
            }
        }
    }

    @Deprecated
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
            case TEMPERATURE_CHANGE:
                break;
            case VALUE_CHANGE:
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
