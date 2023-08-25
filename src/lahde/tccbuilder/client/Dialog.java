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

    You should have received a copy of the GNU General Public License
    along with CircuitJS1.  If not, see <http://www.gnu.org/licenses/>.
*/

package lahde.tccbuilder.client;


import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

class Dialog extends DialogBox {
    AlertBox alertBox = null;

    boolean closeOnEnter;

    Dialog() {
        setGlassEnabled(true);
        setGlassStyleName("glassBackdrop");
        closeOnEnter = true;

    }

    public void closeDialog() {
        hide();
        if (CirSim.dialogShowing == this)
            CirSim.dialogShowing = null;
    }


    public void enterPressed() {
        if (closeOnEnter) {
            apply();
            closeDialog();
        }
    }

    void apply() {
    }

    Button getHelpButton(HTML helpText) {
        Button helpButton = new Button("?");

        helpButton.addMouseOverHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                alertBox = AlertBox.showHelpTemporaryMessage("Help", helpText);
            }
        });
        helpButton.addMouseOutHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                if (alertBox != null) alertBox.hide();
            }
        });
        helpButton.setStyleName("helpButton");
        return helpButton;
    }
}

