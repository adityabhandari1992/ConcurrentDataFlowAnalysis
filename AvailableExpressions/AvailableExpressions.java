package AvailableExpressions;

import java.util.Iterator;

import soot.Local;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AddExpr;
import soot.jimple.DivExpr;
import soot.jimple.MulExpr;
import soot.jimple.Stmt;
import soot.jimple.SubExpr;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ArrayFlowUniverse;
import soot.toolkits.scalar.ArrayPackedSet;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.FlowUniverse;
import soot.toolkits.scalar.ForwardFlowAnalysis;

public class AvailableExpressions extends ForwardFlowAnalysis {
	private UnitGraph g;	
	
	public AvailableExpressions(UnitGraph g)
    {
        super(g);
        this.g = g;
        doAnalysis();
    }
	
	private FlowSet myIntersection(FlowSet set1, FlowSet set2) {
		FlowSet result = new ArraySparseSet();
		Iterator set1It = set1.iterator();
		while (set1It.hasNext()) {
			Value set1Value = (Value) set1It.next();
			Iterator set2It = set2.iterator();
			while (set2It.hasNext()) {
				Value set2Value = (Value) set2It.next();
				if (set1Value.toString().equals(set2Value.toString())) {
					result.add(set1Value);
				}
			}
		}
		
		return result;
	}
	
	protected void merge(Object in1, Object in2, Object out)
    {
        FlowSet inSet1 = (FlowSet) in1;
        FlowSet inSet2 = (FlowSet) in2;
        FlowSet outSet = (FlowSet) out;

        outSet = myIntersection(inSet1, inSet2);
    }
	
	// STEP 4: Define flow equations.
	// in(s) = ( out(s) minus defs(s) ) union uses(s)
	//
    protected void flowThrough(Object inValue, Object unit,
            Object outValue)
    {
        FlowSet in  = (FlowSet) inValue;
        FlowSet out = (FlowSet) outValue;
        Stmt    s   = (Stmt)    unit;
        
        Iterator boxIt = s.getUseBoxes().iterator();
        
        // Copy in to out
        in.copy( out );
        // Take out kill set
        boxIt = s.getDefBoxes().iterator();
        while( boxIt.hasNext() ) {
            final ValueBox box = (ValueBox) boxIt.next();
            Value value = box.getValue();
            if(value instanceof Local) {
            	Iterator outIt = out.iterator();
            	while (outIt.hasNext()) {
            		Value outNext = (Value) outIt.next();
            		Iterator it = outNext.getUseBoxes().iterator();
                	while (it.hasNext()) {
                		Value variable = ((ValueBox) it.next()).getValue();
                		if (variable.toString().equals(value.toString())) {
                			out.remove(outNext);
                		}
                	}
            	}
            }
        }

        // Add gen set
        boxIt = s.getUseBoxes().iterator();
        while( boxIt.hasNext() ) {
            final ValueBox box = (ValueBox) boxIt.next();
            Value value = box.getValue();
            if( value instanceof AddExpr || value instanceof SubExpr || value instanceof MulExpr || value instanceof DivExpr) {
            	if (out.isEmpty()) out.add(value);
            	else {
            		Iterator outIt = out.iterator();
	            	while (outIt.hasNext()) {
	            		Value outVal = (Value) outIt.next();
	                    if (!value.toString().equals(outVal.toString())) {
	                    	out.add(value);
	                    }
	            	}
            	}
            }
        }
    }
    
    protected void copy(Object source, Object dest)
    {
        FlowSet sourceSet = (FlowSet) source;
        FlowSet destSet   = (FlowSet) dest;
        sourceSet.copy(destSet);
    }

	// start node:            empty set
	// initial approximation: full set
    protected Object entryInitialFlow()
    {
        return new ArraySparseSet();
    }
    
    protected Object newInitialFlow()
    {
    	String[] elements = {"a+b"};
		FlowUniverse<String> universe = new ArrayFlowUniverse<String>(elements );
        FlowSet init = new ArrayPackedSet(universe);
        return init;
    }
}