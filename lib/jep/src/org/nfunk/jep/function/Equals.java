
package org.nfunk.jep.function;

import org.nfunk.jep.*;
/** * An example custom function class for JEP. */


public class Equals extends PostfixMathCommand implements CallbackEvaluationI {
    
    /**	 * Constructor	 */
    public Equals() {
        super();
        numberOfParameters = 2;
    }
    

    	public Object evaluate(Node node,EvaluatorI pv) throws ParseException
	{
            return new Object();
        
        }
    
}