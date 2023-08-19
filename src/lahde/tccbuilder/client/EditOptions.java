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

import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.Window;
import lahde.tccbuilder.client.util.Locale;


//TODO: CHANGE OPTIONS
class EditOptions implements Editable {
    CirSim sim;

    public EditOptions(CirSim s) {
        sim = s;
    }

    public EditInfo getEditInfo(int n) {
        if (n == 0)
            return new EditInfo("Time step size (s)", sim.maxTimeStep, 0, 0);
        if (n == 1)
            return new EditInfo("Range for voltage color (V)",
                    0, 0, 0);
        if (n == 2) {
            EditInfo ei = new EditInfo("Change Language", 0, -1, -1);
            ei.choice = new Choice();
            ei.choice.add("(no change)");
            ei.choice.add("Čeština");
            ei.choice.add("Dansk");
            ei.choice.add("Deutsch");
            ei.choice.add("English");
            ei.choice.add("Español");
            ei.choice.add("Français");
            ei.choice.add("Italiano");
            ei.choice.add("Norsk bokmål");
            ei.choice.add("Polski");
            ei.choice.add("Português");
            ei.choice.add("\u0420\u0443\u0441\u0441\u043a\u0438\u0439"); // Russian
            ei.choice.add("\u4e2d\u6587 (\u4e2d\u56fd\u5927\u9646)"); // Chinese
            ei.choice.add("\u4e2d\u6587 (\u53f0\u6e7e)"); // Chinese (tw)
            return ei;
        }

        if (n == 3)
            return new EditInfo("Positive Color", "#000");
        if (n == 4)
            return new EditInfo("Negative Color", "#000");
        if (n == 5)
            return new EditInfo("Neutral Color", "#000");
        if (n == 6)
            return new EditInfo("Selection Color", "#000");
        if (n == 7)
            return new EditInfo("Current Color", "#000");
        if (n == 8)
            return new EditInfo("# of Decimal Digits (short format)", 0);
        if (n == 9)
            return new EditInfo("# of Decimal Digits (long format)", 0);
        if (n == 10) {
            EditInfo ei = new EditInfo("", 0, -1, -1);
            ei.checkbox = new Checkbox("Developer Mode", sim.developerMode);
            return ei;
        }
        if (n == 11) {
            EditInfo ei = new EditInfo("", 0, -1, -1);
            ei.checkbox = new Checkbox("Auto-Adjust Timestep", sim.adjustTimeStep);
            return ei;
        }
        if (n == 12 && sim.adjustTimeStep)
            return new EditInfo("Minimum time step size (s)", sim.minTimeStep, 0, 0);

        if (n == 13)
            return new EditInfo("Left temperature", sim.heatCircuit.temperatureWest);

        return null;
    }

    public void setEditValue(int n, EditInfo ei) {
        if (n == 0 && ei.value > 0) {
            sim.maxTimeStep = ei.value;
        }
        if (n == 1 && ei.value > 0)
            return;
        if (n == 2) {
            int lang = ei.choice.getSelectedIndex();
            if (lang == 0)
                return;
            String langString = null;
            switch (lang) {
                // Czech is csx instead of cs because we are not ready to use it automatically yet
                case 1:
                    langString = "csx";
                    break;
                case 2:
                    langString = "da";
                    break;
                case 3:
                    langString = "de";
                    break;
                case 4:
                    langString = "en";
                    break;
                case 5:
                    langString = "es";
                    break;
                case 6:
                    langString = "fr";
                    break;
                case 7:
                    langString = "it";
                    break;
                case 8:
                    langString = "nb";
                    break;
                case 9:
                    langString = "pl";
                    break;
                case 10:
                    langString = "pt";
                    break;
                case 11:
                    langString = "ru";
                    break;
                case 12:
                    langString = "zh";
                    break;
                case 13:
                    langString = "zh-tw";
                    break;
            }
            if (langString == null)
                return;
            Storage stor = Storage.getLocalStorageIfSupported();
            if (stor == null) {
                Window.alert(Locale.LS("Can't set language"));
                return;
            }
            stor.setItem("language", langString);
            if (Window.confirm(Locale.LS("Must restart to set language.  Restart now?")))
                Window.Location.reload();
        }
        if (n == 3) {
            setColor("positiveColor", ei, Color.green);
        }
        if (n == 4) {
            setColor("negativeColor", ei, Color.red);
        }
        if (n == 5) {
            setColor("neutralColor", ei, Color.gray);
        }
        if (n == 6)
            setColor("selectColor", ei, Color.cyan);
        if (n == 7)
            setColor("currentColor", ei, Color.yellow);
        if (n == 8) {
        //CircuitElm.setDecimalDigits((int) ei.value, true, true);
        }
        if (n == 9) {
        //CircuitElm.setDecimalDigits((int) ei.value, false, true);
        }
        if (n == 10)
            sim.developerMode = ei.checkbox.getState();
        if (n == 11) {
            sim.adjustTimeStep = ei.checkbox.getState();
            ei.newDialog = true;
        }
        if (n == 12 && ei.value > 0)
            sim.minTimeStep = ei.value;
        if (n == 13)
            sim.temp_left = (double) ei.value;
    }

    Color setColor(String name, EditInfo ei, Color def) {
        String val = ei.textf.getText();
        if (val.isEmpty())
            val = def.getHexValue();
        Storage stor = Storage.getLocalStorageIfSupported();
        if (stor != null)
            stor.setItem(name, val);
        return new Color(val);
    }
};
