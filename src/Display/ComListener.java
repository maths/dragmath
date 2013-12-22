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
import java.awt.event.*;
import java.awt.*;

/**
 * Class to control the re-sizing of graphics in the display, as other components re-size
 * @author Alex Billingsley
 */
public class ComListener extends ComponentAdapter {
    
    private int status;
    private JPanel layer;
    private int firstPos;
    private int secondPos;
    
    /** Creates a new instance of ComListener
     * @param layer the JPanel to watch if it resizes
     * @param status the int to state what type of components are to be resized
     */
    public ComListener(JPanel layer, int status, int firstPos, int secondPos) {
        this.status = status;
        this.layer=layer;
        this.firstPos=firstPos;
        this.secondPos=secondPos;
        //resize();
    }
    
    public void componentResized(ComponentEvent e) {
        resize();
    }
    
    public void resize() {
        int i=0;
        int height=0;
        java.awt.FontMetrics fontMetrics=null;
        JPanel parentLayer = (JPanel)layer.getParent();
        
        // Determine what size font is required
        while (height < layer.getPreferredSize().getHeight()) {
            i++;
            fontMetrics  = layer.getFontMetrics(new java.awt.Font("SansSerif", 0, i));
            height = fontMetrics.getHeight();
        }
        
        // Brackets
        if (status == 0) {
            JLabel bracket1 = (JLabel)parentLayer.getComponent(firstPos);
            JLabel bracket2 = (JLabel)parentLayer.getComponent(secondPos);
            bracket1.setFont(new java.awt.Font("SansSerif", 0, i));
            bracket2.setFont(new java.awt.Font("SansSerif", 0, i));
        }
        // Symbol for a function e.g. square root
        if (status == 1) {
            JLabel symbol = (JLabel)parentLayer.getComponent(0);
            symbol.setFont(new java.awt.Font("SansSerif", 0, i));
        }
        // Symbol for a function e.g. definite integral
        if (status == 2) {
            JPanel temp = (JPanel)parentLayer.getComponent(0);
            JLabel symbol = (JLabel)temp.getComponent(1);
            symbol.setFont(new java.awt.Font("SansSerif", 0, i));
        }
        
        
        parentLayer.revalidate();
    }
}
