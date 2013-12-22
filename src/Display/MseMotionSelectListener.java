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
import java.awt.*;
import javax.swing.*;

/**
 *
 * @author Alex Billingsley
 */
public class MseMotionSelectListener extends MouseMotionAdapter {
    
    private JPanel jPanelWorkspace;
    private MseSelectListener mouseListener;
    
    /** Creates a new instance of MouseMotionSelectionListener */
    public MseMotionSelectListener(JPanel jPanelWorkspace, MseSelectListener mouseListener) {
        this.jPanelWorkspace = jPanelWorkspace;
        this.mouseListener = mouseListener;
    }
    
    public void mouseDragged(MouseEvent e) {
        Point xy2 = new Point(e.getX(), e.getY());
        MseSelectListener.deSelect((JPanel)e.getSource());
        highlight((JPanel)e.getSource(), xy2, false);
        jPanelWorkspace.requestFocus();
    }
    
    // Changes the colour of the background of components that are being selected as the mouse is being dragged
    // to show they are highlighted
    public void highlight(JPanel layer, Point xy2, boolean nested) {
        Component[] components = layer.getComponents();
        int i=0;
        Point c_xy = null;
        double x = 0;
        double y = 0;
        while (i < components.length) {
            
            if (nested == false) {
                c_xy = components[i].getLocation();
                
                SwingUtilities.convertPointToScreen(c_xy, components[i].getParent());
                SwingUtilities.convertPointFromScreen(c_xy, jPanelWorkspace);
                
                x = components[i].getWidth();
                y = components[i].getHeight();
            }
            
            if (nested || (((c_xy.getX() > mouseListener.getXy1().getX() && c_xy.getX() < xy2.getX()) ||(c_xy.getX() + x > mouseListener.getXy1().getX() && c_xy.getX() + x < xy2.getX())) &&
                    ((c_xy.getY() > mouseListener.getXy1().getY() && c_xy.getY() < xy2.getY()) || (c_xy.getY() + y > mouseListener.getXy1().getY() && c_xy.getY() + y < xy2.getY())
                    || mouseListener.getXy1().getY() > c_xy.getY() && xy2.getY() < c_xy.getY() +y))) {
                
                
                if (components[i].getClass().getName().equals("javax.swing.JLabel")) {
                    JLabel temp = (JLabel)components[i];
                    if (AddComponent.getGroup(temp.getName()) == 1 || AddComponent.getGroup(temp.getName()) == 2) {
                        temp.setOpaque(true);
                        components[i].setBackground(mouseListener.SELECT);
                    }
                }
                
                if (components[i].getClass().getName().equals("Display.TextBox")) {
                    components[i].setBackground(mouseListener.SELECT);
                }
                
                // If panel is selected, select all components nested on panel
                if (components[i].getClass().getName().equals("javax.swing.JPanel")) {
                    components[i].setBackground(mouseListener.SELECT);
                    highlight((JPanel)components[i], xy2, true);
                }
                
                // Check if all components on panel have been selected
                int j=0;
                boolean all=true;
                while (j < components.length && all) {
                    if (components[j].getBackground().equals(mouseListener.SELECT)) {
                    } else {
                        // Ignore certain layout components to make whole layout be selected when both arguments are
                        if (AddComponent.getID(layer.getName()) == 8 && j==2 || AddComponent.getID(layer.getName()) == 6 && j==0
                                || AddComponent.getID(layer.getName()) == 6 && j==1
                                || AddComponent.getID(layer.getName()) == 1 && j==1) {
                            // ignore these components
                        } else {
                            all = false;
                        }
                    }
                    j++;
                }
                if (all) {
                    JPanel temp = (JPanel)components[i].getParent();
                    if (temp != jPanelWorkspace) {
                        temp.setBackground(mouseListener.SELECT);
                    }
                }
                
            }
            
            if (components[i].getClass().getName().equals("javax.swing.JPanel")) {
                highlight((JPanel)components[i], xy2, nested);
            }
            i++;
        }
    }
    
    public void clickSelect(JComponent component) {
        MseSelectListener.deSelect(jPanelWorkspace);
        
        if (component.getClass().getName().equals("javax.swing.JLabel")) {
            JLabel temp = (JLabel)component;
            // If labels is operator label
            if (AddComponent.getGroup(temp.getName()) == 1 || AddComponent.getGroup(temp.getName()) == 2) {
                temp.setOpaque(true);
                component.setBackground(MseSelectListener.SELECT);
            } else {
                // highlight all on label panel
                component.getParent().setBackground(MseSelectListener.SELECT);
                highlightNested((JPanel)component.getParent());
            }
        }
        
        if (component.getClass().getName().equals("Display.TextBox")) {
            component.setBackground(MseSelectListener.SELECT);
        }
        
        // If panel is selected, select all components nested on panel
        if (component.getClass().getName().equals("javax.swing.JPanel")) {
            if (component != jPanelWorkspace) {
                component.setBackground(MseSelectListener.SELECT);
                // highlight nested
                highlightNested((JPanel)component);
            }
        }
    }
    
    public void highlightNested(JPanel layer) {
        Component[] components = layer.getComponents();
        int i=0;
        while (i < components.length) {
            if (components[i].getClass().getName().equals("javax.swing.JLabel")) {
                JLabel temp = (JLabel)components[i];
                temp.setOpaque(true);
                components[i].setBackground(MseSelectListener.SELECT);
            }
            
            if (components[i].getClass().getName().equals("Display.TextBox")) {
                components[i].setBackground(MseSelectListener.SELECT);
            }
            
            // If panel is selected, select all components nested on panel
            if (components[i].getClass().getName().equals("javax.swing.JPanel")) {
                components[i].setBackground(MseSelectListener.SELECT);
                highlightNested((JPanel)components[i]);
            }
            i++;
        }
    }
    
    
}

