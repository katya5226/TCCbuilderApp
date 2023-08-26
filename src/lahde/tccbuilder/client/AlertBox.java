package lahde.tccbuilder.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;

public class AlertBox extends Dialog {


    private AlertBox(String title, HTML message, boolean close) {
        setText(title);
        setModal(true);
        setGlassEnabled(true);

        FlowPanel flowPanel = new FlowPanel();
        flowPanel.addStyleName("dialogContainer");
        setWidget(flowPanel);

        flowPanel.add(message);
        if (close) {
            Button closeButton = new Button("Close");
            closeButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    closeDialog();
                }
            });
            flowPanel.add(closeButton);
        }


    }

    public static void showAlert(String title, HTML message) {
        AlertBox alertBox = new AlertBox(title, message, true);
        alertBox.center();
        alertBox.show();
    }

    public static AlertBox showHelpTemporaryMessage(String title, HTML message) {
        AlertBox alertBox = showTemporaryMessage(title, message);
        alertBox.setHeight("75%");
        alertBox.center();
        alertBox.setPopupPosition(0,alertBox.getPopupTop());
        return alertBox;
    }

    public static AlertBox showTemporaryMessage(String title, HTML message) {
        AlertBox alertBox = new AlertBox(title, message, false);
        alertBox.setModal(false);
        alertBox.setGlassEnabled(false);
        alertBox.center();
        alertBox.show();
        return alertBox;
    }

}
