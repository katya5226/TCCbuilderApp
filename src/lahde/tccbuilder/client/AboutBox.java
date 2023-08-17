/*    
    Copyright (C) Paul Falstad and Iain Sharp
    
    This file is part of CircuitJS1.

    CircuitJS1 is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 2 of the License, or
    (at your option) any later version.

    CircuitJS1 is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
     
	 some comments
    You should have received a copy of the GNU General Public License
    along with CircuitJS1.  If not, see <http://www.gnu.org/licenses/>.
*/

package lahde.tccbuilder.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.ui.*;

public class AboutBox extends Dialog {

    String aboutText;
    VerticalPanel vp;
    Button okButton;
    Label label;

    AboutBox(String version) {
        super();

        // Add versionString variable to SessionStorage for iFrame in AboutBox
        Storage sstor = Storage.getSessionStorageIfSupported();
        sstor.setItem("versionString", version);

        vp = new VerticalPanel();
        setWidget(vp);
        vp.setWidth("400px");
        //vp.add(new HTML("<iframe src=\"about.html\" width=\"400\" height=\"430\" scrolling=\"auto\" frameborder=\"0\"></iframe><br>"));
        aboutText = "The original code was forked from the project CircuitJS1 (Copyright (C) Paul Falstad and Iain Sharp, https://github.com/pfalstad/circuitjs1).\n" +
                "TCCBuilder is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License.";
        label = new Label();
        setText("About");
        label.setText(aboutText);
        vp.add(label);

        vp.add(okButton = new Button("OK"));
        okButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                close();
            }
        });
        center();
        show();
    }

    public void close() {
        hide();
    }
}
