package org.randoom.setlx.utilities;

import org.randoom.setlx.exceptions.IncompatibleTypeException;
import org.randoom.setlx.exceptions.JVMException;
import org.randoom.setlx.exceptions.SetlException;
import org.randoom.setlx.functions.PreDefinedProcedure;
import org.randoom.setlx.types.NumberValue;
import org.randoom.setlx.types.SetlDouble;
import org.randoom.setlx.types.SetlObject;
import org.randoom.setlx.types.Value;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Objects of this class encapsulate functions from java.Math using two
 * doubles as arguments.
 */
public class MathFunction2 extends PreDefinedProcedure {
    private final Method function;

    /**
     * Encapsulate a java.Math function.
     *
     * @param name     Name of the function.
     * @param function Function to encapsulate.
     */
    public MathFunction2(final String name, final Method function) {
        super();
        setName(name);
        addParameter("x");
        addParameter("y");
        this.function = function;
    }

    @Override
    public Value execute(final State state, final List<Value> args, final List<Value> writeBackVars) throws SetlException {
        final Value arg0 = args.get(0);
        final Value arg1 = args.get(1);
        if (arg0 instanceof NumberValue && arg1 instanceof NumberValue) {
            try {
                final double r = (Double) function.invoke(null, arg0.toJDoubleValue(state), arg1.toJDoubleValue(state));
                return SetlDouble.valueOf(r);
            } catch (final SetlException se) {
                throw se;
            } catch (final Exception e) {
                if (state.isRuntimeDebuggingEnabled()) {
                    final ByteArrayOutputStream out = new ByteArrayOutputStream();
                    e.printStackTrace(new PrintStream(out));
                    state.errWrite(out.toString());
                }
                throw new JVMException(
                    "Error during calling a predefined mathematical function.\n" +
                    "This is probably a bug in the interpreter.\n" +
                    "Please report it including executed source example."
                );
            }
        } else if (arg0 instanceof SetlObject && arg1 instanceof SetlObject) {
            return ((SetlObject) arg0).overloadMathFunction(state, getName(), arg1);
        }

        throw new IncompatibleTypeException(
            "This function requires two numbers as parameters."
        );
    }
}

