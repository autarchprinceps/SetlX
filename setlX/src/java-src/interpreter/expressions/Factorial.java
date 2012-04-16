package interpreter.expressions;

import interpreter.exceptions.SetlException;
import interpreter.exceptions.TermConversionException;
import interpreter.types.Term;
import interpreter.types.Value;
import interpreter.utilities.TermConverter;

/*
grammar rule:
factor
    : [...]
    | simpleFactor '!'?
    ;

implemented here as:
      ============
         mExpr
*/

public class Factorial extends Expr {
    // functional character used in terms (MUST be class name starting with lower case letter!)
    private final static String FUNCTIONAL_CHARACTER = "^factorial";
    // precedence level in SetlX-grammar
    private final static int    PRECEDENCE           = 1900;

    private Expr mExpr;

    public Factorial(Expr expr) {
        mExpr = expr;
    }

    public Value evaluate() throws SetlException {
        return mExpr.eval().factorial();
    }

    /* string operations */

    public String toString(int tabs) {
        return mExpr.toString(tabs) + "!";
    }

    /* term operations */

    public Term toTerm() {
        Term result = new Term(FUNCTIONAL_CHARACTER);
        result.addMember(mExpr.toTerm());
        return result;
    }

    public static Factorial termToExpr(Term term) throws TermConversionException {
        if (term.size() != 1) {
            throw new TermConversionException("malformed " + FUNCTIONAL_CHARACTER);
        } else {
            Expr expr = TermConverter.valueToExpr(PRECEDENCE, false, term.firstMember());
            return new Factorial(expr);
        }
    }

    // precedence level in SetlX-grammar
    public int precedence() {
        return PRECEDENCE;
    }
}

