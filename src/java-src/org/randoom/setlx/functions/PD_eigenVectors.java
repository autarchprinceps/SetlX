package org.randoom.setlx.functions;

import java.util.List;
import org.randoom.setlx.exceptions.IncompatibleTypeException;
import org.randoom.setlx.exceptions.SetlException;
import org.randoom.setlx.types.SetlMatrix;
import org.randoom.setlx.types.Value;
import org.randoom.setlx.utilities.ParameterDef;
import org.randoom.setlx.utilities.State;

/**
 * @author Patrick Robinson
 */
public class PD_eigenVectors extends PreDefinedProcedure {

	public final static PreDefinedProcedure DEFINITION = new PD_eigenVectors();

	private PD_eigenVectors() {
		super();
		addParameter("Matrix", ParameterDef.READ_ONLY);
	}

	/**
	 * Calculate eigen vector matrix
	 *
	 * @param state
	 * @param args SetlMatrix
	 * @param writeBackVars
	 * @return SetlMatrix
	 * @throws SetlException
	 */
	@Override
	public Value execute(State state, List<Value> args, List<Value> writeBackVars) throws SetlException {
		if((args.get(0) instanceof SetlMatrix)) {
			return ((SetlMatrix)args.get(0)).eigenVectors();
		} else {
			throw new IncompatibleTypeException("The parameter needs to be a matrix.");
		}
	}
}
