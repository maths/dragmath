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

import javax.swing.*;
import javax.swing.event.*;

/**
 * Class to listen for changes of the text inside a text box
 * @author Alex Billingsley
 */
public class DocListener implements DocumentListener {
    
    private JTextField target;
    
    /** Creates a new instance of DocListener
     * @param target the text box to listen for changes to and resize accordingly
     */
    public DocListener(JTextField target) {
        this.target=target;
    }
    
    public void changedUpdate(DocumentEvent e) {
    }
    
    public void removeUpdate(DocumentEvent e) {
        resize();
    }
    
    public void insertUpdate(DocumentEvent e) {
        resize();
    }
    
    public void resize() {
        if (target.getText().length() > 0) {
            target.setColumns(0);
        } else {
            target.setColumns(1);
        }
        if (target.getParent() != null) {
            JPanel layer = (JPanel)target.getParent();
            layer.revalidate();
        }
    }
    
}
