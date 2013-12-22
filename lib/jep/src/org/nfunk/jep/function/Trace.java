
package org.nfunk.jep.function;


import java.util.*;
import org.nfunk.jep.*;
/** * An example custom function class for JEP. */


public class Trace extends PostfixMathCommand {
    
    /**	 * Constructor	 */
    public Trace() {
        numberOfParameters = 1;
    }
    
    public void run(Stack inStack) throws ParseException {
// no evaluation needed
    }
    
}