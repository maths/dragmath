/*
Copyright (C) 2010 Alex Billingsley, email@alexbillingsley.co.uk
www.dragmath.bham.ac.uk
 
 This file is part of DragMath.

    DragMath is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    DragMath is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with DragMath. If not, see <http://www.gnu.org/licenses/>.

 */

package Display;
import javax.swing.JLabel;

/** Class to provide access to a JLabel on the display of the applet, that is a status bar to provide
 * feedback to the user
 * @author Alex Billingsley
 */
public class StatusBar {
    
    private JLabel bar;
    
    /** Creates a new instance of StatusBar */
    public StatusBar(JLabel bar) {
        this.bar=bar;
    }
    
    public void println(String s) {
        bar.setText(s);
    }
}
