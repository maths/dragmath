
package org.nfunk.jep.function;


import java.util.*;
import org.nfunk.jep.*;
/** * An example custom function class for JEP. */


public class Union extends PostfixMathCommand {
    
    /**	 * Constructor	 */
    public Union() {
        numberOfParameters = 2;
    }
    
    public void run(Stack inStack) throws ParseException {
// no evaluation needed
    }
    
}