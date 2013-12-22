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

import java.awt.event.*;
import java.awt.Point;
import javax.swing.*;
import java.awt.*;

/**
 * Class to store mouse start location when dragging,
 * and to deselect any selected components when the mouse is pressed
 * @author Alex Billingsley
 */
public class MseSelectListener extends MouseAdapter {
    
    private Point xy1;
    public static final Color DESELECT = new Color(Color.WHITE.getRGB());
    public static final Color SELECT = new Color(Color.LIGHT_GRAY.getRGB());
    
    /** Creates a new instance of MouseSelectionListener */
    public MseSelectListener() {
    }
    
    public void mousePressed(MouseEvent e) {
        xy1 = new Point(e.getX(), e.getY());
        JPanel layer = (JPanel)e.getComponent();
        deSelect(layer);
    }
    
    
    public static void deSelect(JPanel layer) {
        
        Component[] components = layer.getComponents();
        int i=0;
        while (i < components.length) {
            
            if (components[i].getClass().getName().equals("javax.swing.JLabel")) {
                if (components[i].getBackground().equals(SELECT)) {
                    JLabel temp = (JLabel)components[i];
                    temp.setOpaque(false);
                    components[i].setBackground(DESELECT);
                }
            }
            
            if (components[i].getClass().getName().equals("Display.TextBox")) {
                if (components[i].getBackground().equals(SELECT)) {
                    components[i].setBackground(DESELECT);
                }
            }
            
            // If panel is selected, de-select all components nested on panel
            if (components[i].getClass().getName().equals("javax.swing.JPanel")) {
                if (components[i].getBackground().equals(SELECT)) {
                    components[i].setBackground(DESELECT);
                }
                deSelect((JPanel)components[i]);
            }
            
            i++;
        }
    }
    
    /**
     * Getter for property xy1.
     * @return Value of property xy1.
     */
    public Point getXy1() {
        return this.xy1;
    }
    
}
