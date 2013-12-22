
package org.nfunk.jep.function;


import java.util.*;
import org.nfunk.jep.*;
/** * An example custom function class for JEP. */


public class DefiniteIntegral extends PostfixMathCommand  implements PostfixMathCommandI {
    
    /**	 * Constructor	 */
    public DefiniteIntegral() {
        numberOfParameters = -1;
    }
    
    public void run(Stack inStack) throws ParseException {
        checkStack(inStack); // check the stack
        if(curNumberOfParameters != 2 && curNumberOfParameters != 4) {
            throw new ParseException("Empty list");
        }
        return;
    }
    
}