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
 * Class representing a Binary Operator, that has two arguments, <code>leftChild</code> and <code>rightChild</code>,
 * which are of type <code>MathObject</code>.
 * @author Alex Billingsley
 */
public class BinaryOperator extends MathObject {
    
    private MathObject leftChild = null;
    private MathObject rightChild = null;
    
    
    /** Creates a new instance of BinaryOperator */
    public BinaryOperator(int id, String name) {
        super(id, name);
    }
    
    /** Returns the field <code>leftChild</code>
     * @return The field <code>leftChild</code> of type <code>MathObject</code>
     */
    public MathObject getLeftChild() {
        return leftChild;
    }
    
   /** Returns the field <code>rightChild</code>
     * @return The field <code>rightChild</code> of type <code>MathObject</code>
     */
    public MathObject getRightChild() {
        return rightChild;
    }
    
    /** Sets the field <code>leftChild</code> from the parameter
     * @param leftChild The <code>MathObject</code> to set as the <code>leftChild</code>
     */
    public void setLeftChild(MathObject leftChild) {
        this.leftChild = leftChild;
    }
    
    /** Sets the field <code>rightChild</code> from the parameter
     * @param rightChild The <code>MathObject</code> to set as the <code>rightChild</code>
     */
    public void setRightChild(MathObject rightChild) {
        this.rightChild = rightChild;
    }

    
}