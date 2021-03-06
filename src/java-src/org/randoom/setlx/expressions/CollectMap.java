package org.randoom.setlx.expressions;

import org.randoom.setlx.exceptions.SetlException;
import org.randoom.setlx.exceptions.TermConversionException;
import org.randoom.setlx.exceptions.UnknownFunctionException;
import org.randoom.setlx.types.Om;
import org.randoom.setlx.types.Term;
import org.randoom.setlx.types.Value;
import org.randoom.setlx.utilities.State;
import org.randoom.setlx.utilities.TermConverter;

import java.util.List;

/*
grammar rule:
call
    : variable ('(' callParameters ')')? ('[' collectionAccessParams ']' | '{' anyExpr '}')*
    ;

implemented here as:
      ==================================                                       =======
                   mLhs                                                          mArg
*/

public class CollectMap extends Expr {
    // functional character used in terms (MUST be class name starting with lower case letter!)
    private final static String FUNCTIONAL_CHARACTER = "^collectMap";
    // precedence level in SetlX-grammar
    private final static int    PRECEDENCE           = 1900;

    private final Expr  mLhs;      // left hand side (Variable, other CollectMap, CollectionAccess, etc)
    private final Expr  mArg;      // argument

    public CollectMap(final Expr lhs, final Expr arg) {
        mLhs = lhs;
        mArg = arg;
    }

    @Override
    protected Value evaluate(final State state) throws SetlException {
        final Value lhs = mLhs.eval(state);
        if (lhs == Om.OM) {
            throw new UnknownFunctionException(
                "Left hand side \"" + mLhs + "\" is undefined."
            );
        }
        return lhs.collectMap(state, mArg.eval(state).clone());
    }

    /* Gather all bound and unbound variables in this expression and its siblings
          - bound   means "assigned" in this expression
          - unbound means "not present in bound set when used"
          - used    means "present in bound set when used"
       NOTE: Use optimizeAndCollectVariables() when adding variables from
             sub-expressions
    */
    @Override
    protected void collectVariables (
        final List<String> boundVariables,
        final List<String> unboundVariables,
        final List<String> usedVariables
    ) {
        mLhs.collectVariablesAndOptimize(boundVariables, unboundVariables, usedVariables);
        mArg.collectVariablesAndOptimize(boundVariables, unboundVariables, usedVariables);
    }

    /* string operations */

    @Override
    public void appendString(final State state, final StringBuilder sb, final int tabs) {
        mLhs.appendString(state, sb, tabs);
        sb.append("{");
        mArg.appendString(state, sb, tabs);
        sb.append("}");
    }

    /* term operations */

    @Override
    public Term toTerm(final State state) {
        final Term result = new Term(FUNCTIONAL_CHARACTER, 2);
        result.addMember(state, mLhs.toTerm(state));
        result.addMember(state, mArg.toTerm(state));
        return result;
    }

    @Override
    public Term toTermQuoted(final State state) throws SetlException {
        final Term result = new Term(FUNCTIONAL_CHARACTER, 2);
        result.addMember(state, mLhs.toTermQuoted(state));
        result.addMember(state, mArg.eval(state).toTerm(state));
        return result;
    }

    public static CollectMap termToExpr(final Term term) throws TermConversionException {
        if (term.size() != 2) {
            throw new TermConversionException("malformed " + FUNCTIONAL_CHARACTER);
        } else {
            final Expr lhs = TermConverter.valueToExpr(term.firstMember());
            final Expr arg = TermConverter.valueToExpr(term.lastMember());
            return new CollectMap(lhs, arg);
        }
    }

    // precedence level in SetlX-grammar
    @Override
    public int precedence() {
        return PRECEDENCE;
    }
}

