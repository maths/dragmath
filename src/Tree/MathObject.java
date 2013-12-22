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

package Tree;


/**
 * Superclass for all different Math objects of the tree to inherit fields/methods from
 * @author Alex Billingsley
 */
public class MathObject implements java.io.Serializable {
    
    private String name;
    private int id;
    private MathObject parent = null;
    
    /** Creates a new instance of MathObject */
    public MathObject(int id, String name) {
        this.name=name;
        this.id=id;
    }
    
    /** Returns the field <code>name</code>
     * @return The string <code>name</code>
     */
    public String getName() {
        return name;
    }
    
    public int getID() {
        return id;
    }
    
    /** Sets the field <code>parent</code> from the parameter
     * @param parent the <code>MathObject</code> to set as the <code>parent</code>
     */
    public void setParent(MathObject parent) {
        this.parent = parent;
    }
    
    /** Returns the field <code>parent</code>
     * @return The field <code>parent</code> of type <code>MathObject</code>
     */
    public MathObject getParent() {
        return parent;
    }
    
}
