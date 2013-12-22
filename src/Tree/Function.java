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
 * Class representing a Function, that has one argument, <code>child</code>,
 * which is of type <code>MathObject</code>.
 * @author Alex Billingsley
 */
public class Function extends MathObject {
    
    private MathObject child = null;
    
    /** Creates a new instance of Function */
    public Function(int id, String name) {
        super(id, name);
    }
    
    /** Returns the field <code>child</code>
     * @return The field <code>leftChild</code> of type <code>MathObject</code>
     */
    public MathObject getChild() {
        return child;
    }
    
    /** Sets the field <code>child</code> from the parameter
     * @param child the <code>MathObject</code> to set as the <code>child</code>
     */
    public void setChild(MathObject child) {
        this.child=child;
    }
    
    
    
}