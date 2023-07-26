package com.lushprojects.circuitjs1.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.ui.*;
import com.lushprojects.circuitjs1.client.util.Locale;

import java.util.HashMap;
import java.util.Map;

public class ComponentConstantsDialog extends Dialog {

    private Component component;
    private CirSim sim;

    private ListBox dropdown;
    private Map<String, DoubleBox> valueMap;
    private Button applyButton, cancelButton;

    public ComponentConstantsDialog(Component component, CirSim sim) {
        this.component = component;
        this.sim = sim;

        setText(Locale.LS("Set Constant Parameters"));
        closeOnEnter = true;

        VerticalPanel vp = new VerticalPanel();
        setWidget(vp);

        dropdown = new ListBox();
        vp.add(dropdown);
        dropdown.setStyleName("topSpace");

        // Dropdown values
        String[] dropdownValues = {"Density", "Specific Heat Capacity", "Thermal Conductivity"};

        for (String value : dropdownValues) {
            dropdown.addItem(value);
        }

        valueMap = new HashMap<>();

        // Add DoubleBox inputs
        for (String value : dropdownValues) {
            DoubleBox doubleBox = new DoubleBox();
            doubleBox.setVisible(false);
            vp.add(doubleBox);
            doubleBox.addKeyUpHandler(new KeyUpHandler() {
                @Override
                public void onKeyUp(KeyUpEvent event) {
                    validateInput(doubleBox);
                }
            });
            doubleBox.setStyleName("topSpace");
            valueMap.put(value, doubleBox);
        }

        // Apply and Cancel buttons
        HorizontalPanel buttonPanel = new HorizontalPanel();
        buttonPanel.setWidth("100%");
        buttonPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        applyButton = new Button(Locale.LS("Apply"));
        cancelButton = new Button(Locale.LS("Cancel"));
        buttonPanel.add(cancelButton);
        buttonPanel.add(applyButton);
        vp.add(buttonPanel);
        buttonPanel.setStyleName("topSpace");

        // Event handlers
        dropdown.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                updateInputVisibility();
            }
        });

        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hide();
            }
        });

        applyButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                applyValues();
                new EditDialog(component, sim);
                hide();
            }
        });


        updateInputVisibility();
        this.center();
    }

    private void updateInputVisibility() {
        String selectedValue = dropdown.getSelectedItemText();

        for (Map.Entry<String, DoubleBox> entry : valueMap.entrySet()) {
            String value = entry.getKey();
            DoubleBox doubleBox = entry.getValue();
            doubleBox.setVisible(value.equals(selectedValue));

        }
    }

    private void applyValues() {
        String selectedValue = dropdown.getSelectedItemText();

        DoubleBox doubleBox = valueMap.get(selectedValue);
        if (doubleBox != null) {
            double value = doubleBox.getValue();
            // Apply the value to the component or perform necessary actions
            // For example:
            switch (selectedValue) {
                case "Density":
                    component.constRho = value;
                    break;
                case "Specific Heat Capacity":
                    component.constCp = value;
                    break;
                case "Thermal Conductivity":
                    component.constK = value;
                    break;
            }
        }
        GWT.log(component.constRho+"");
        GWT.log(component.constCp+"");
        GWT.log(component.constK+"");
    }

    public void showDialog() {
        updateInputVisibility();
        center();
        show();
    }

    private void validateInput(DoubleBox doubleBox) {
        try {
            Double dbl = Double.parseDouble(doubleBox.getText());
        } catch (NumberFormatException e) {
            doubleBox.getElement().getStyle().setBorderColor(Color.red.getHexValue());
            doubleBox.getElement().getStyle().setColor(Color.red.getHexValue());
            return;
        }

        doubleBox.getElement().getStyle().setBorderColor(Color.black.getHexValue());
        doubleBox.getElement().getStyle().setColor(Color.black.getHexValue());

        Double value = doubleBox.getValue();
        if (value != null && value < 0) {
            doubleBox.getElement().getStyle().setBorderColor(Color.red.getHexValue());
            doubleBox.getElement().getStyle().setColor(Color.red.getHexValue());
        }
    }
}
