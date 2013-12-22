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
 * Class representing an NaryFunction, that has two n-arguments
 * which are of type <code>MathObject</code>.
 * @author Alex Billingsley
 */
public class NaryFunction extends MathObject {
    
    private MathObject[] child;
    
    
    /** Creates a new instance of NaryFunction */
    public NaryFunction(int id, String name, int n) {
        super(id, name);
        child = new MathObject[n];
    }
    
     public MathObject getChild(int n) {
        return child[n];
    }

    public int getSize() {
        return child.length;
    }
     
 
    public void setChild(MathObject newChild, int n) {
        child[n] = newChild;
    }
    
    
}