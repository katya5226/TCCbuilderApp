package lahde.tccbuilder.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;

public class AlertBox extends Dialog{
    private AlertBox(String title, Widget message) {
        setText(title);
        setModal(true);
        setGlassEnabled(true);

        VerticalPanel vp = new VerticalPanel();
        vp.setWidth("25vw");
        setWidget(vp);

        vp.add(message);

        Button closeButton = new Button("Close");
        closeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                closeDialog();
            }
        });

        HorizontalPanel buttonPanel = new HorizontalPanel();
        buttonPanel.addStyleName("buttonPanel");
        buttonPanel.add(closeButton);

        vp.add(buttonPanel);
    }

    public static void showAlert(String title, Widget message) {
        AlertBox alertBox = new AlertBox(title, message);
        alertBox.center();
        alertBox.show();
    }
}
