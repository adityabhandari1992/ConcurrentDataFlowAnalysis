package LiveVariables;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import soot.Local;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.Stmt;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.BackwardFlowAnalysis;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;

public class LiveVariables extends BackwardFlowAnalysis{
	private UnitGraph g;
	
	public LiveVariables(UnitGraph g)
    {
        super(g);
        this.g = g;
        doAnalysis();
    }
	
	private void myRemove(Value v, FlowSet f) {
		Iterator fIt = f.iterator();
		while (fIt.hasNext()) {
			Value fValue = (Value) fIt.next();
			if (fValue.toString().equals(v.toString())) {
				f.remove(fValue);
			}
		}
	}
	
	protected void merge(Object in1, Object in2, Object out)
    {
        FlowSet inSet1 = (FlowSet) in1;
        FlowSet inSet2 = (FlowSet) in2;
        FlowSet outSet = (FlowSet) out;

        inSet1.union(inSet2, outSet);
    }
	// STEP 4: Define flow equations.
	// in(s) = ( out(s) minus defs(s) ) union uses(s)
	//
    protected void flowThrough(Object outValue, Object unit,
            Object inValue)
    {
        FlowSet in  = (FlowSet) inValue;
        FlowSet out = (FlowSet) outValue;
        Stmt    s   = (Stmt)    unit;
        // Copy out to in
        out.copy( in );
        // Take out kill set
        Iterator boxIt = s.getDefBoxes().iterator();
        while( boxIt.hasNext() ) {
            final ValueBox box = (ValueBox) boxIt.next();
            Value value = box.getValue();
            if( value instanceof Local && value.toString().equals("a"))
                myRemove(value, in);
        }

        // Add gen set
        boxIt = s.getUseBoxes().iterator();
        while( boxIt.hasNext() ) {
            final ValueBox box = (ValueBox) boxIt.next();
            Value value = box.getValue();
            if( value instanceof Local && value.toString().equals("a"))
                in.add( value );
        }
    }

    protected void copy(Object source, Object dest)
    {
        FlowSet sourceSet = (FlowSet) source;
        FlowSet destSet   = (FlowSet) dest;
        sourceSet.copy(destSet);
    }
// STEP 5: Determine value for start/end node.
// STEP 6: Initial approximation (bottom).
//
// end node:              empty set
// initial approximation: empty set
    protected Object entryInitialFlow()
    {
        return new ArraySparseSet();
    }
    protected Object newInitialFlow()
    {
        return new ArraySparseSet();
    }
}