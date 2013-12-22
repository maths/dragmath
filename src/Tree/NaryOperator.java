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

import java.util.ArrayList;

/**
 * Class representing an N-ary Operator, that has 2<x<n arguments
 * @author Alex Billingsley
 */
public class NaryOperator extends MathObject {
    
    private ArrayList list = new ArrayList();
    
    /** Creates a new instance of n_aryOperator */
    public NaryOperator(int id, String name) {
        super(id, name);
    }
    
    /** Returns the number of children
     * @return an integer for the number of children
     */
    public int getSize() {
        return list.size();
    }
    
    /** Adds a child at the location n
     * @param child the <code>MathObject</code> to set as the <code>child</code>
     * @param n the int of the location to add it at
     */
    public void addChild(MathObject child, int n) {
        list.add(child);
    }
    
    /** Returns the child at the location n specified in the parameter
     * @param n int of the location of the child to return
     * @return The child of type <code>MathObject</code> at location n
     */
    public MathObject getChild(int n) {
        return (MathObject)list.get(n);
    }
}
