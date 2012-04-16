package interpreter.statements;

import interpreter.exceptions.SetlException;
import interpreter.exceptions.TermConversionException;
import interpreter.types.Term;
import interpreter.types.Value;
import interpreter.utilities.Environment;
import interpreter.utilities.Iterator;
import interpreter.utilities.IteratorExecutionContainer;
import interpreter.utilities.TermConverter;

/*
grammar rule:
statement
    : [...]
    | 'for' '(' iteratorChain ')' '{' block '}'
    ;

implemented here as:
                ========-----         =====
                  mIterator        mStatements
*/

public class For extends Statement {
    // functional character used in terms (MUST be class name starting with lower case letter!)
    private final static String  FUNCTIONAL_CHARACTER   = "^for";
    // continue execution of this loop in debug mode until it finishes. MAY ONLY BE SET BY ENVIRONMENT CLASS!
    public        static boolean sFinishLoop            = false;

    private Iterator    mIterator;
    private Block       mStatements;

    private class Exec implements IteratorExecutionContainer {
        private Block   mStatements;

        public Exec (Block statements) {
            mStatements = statements;
        }

        public void execute(Value lastIterationValue) throws SetlException {
            mStatements.execute();
            // ContinueException and BreakException are handled by outer iterator
        }
    }

    public For(Iterator iterator, Block statements) {
        mIterator   = iterator;
        mStatements = statements;
    }

    public void exec() throws SetlException {
        Exec    e           = new Exec(mStatements);
        boolean finishLoop  = sFinishLoop;
        if (finishLoop) { // unset, because otherwise it would be reset when this loop finishes
            Environment.setDebugFinishLoop(false);
        }
        mIterator.eval(e);
        if (sFinishLoop) {
            Environment.setDebugModeActive(true);
            Environment.setDebugFinishLoop(false);
        } else if (finishLoop) {
            Environment.setDebugFinishLoop(true);
        }
    }

    /* string operations */

    public String toString(int tabs) {
        String result = Environment.getLineStart(tabs);
        result += "for (" + mIterator.toString(tabs) + ") ";
        result += mStatements.toString(tabs, true);
        return result;
    }

    /* term operations */

    public Term toTerm() {
        Term result = new Term(FUNCTIONAL_CHARACTER);
        result.addMember(mIterator.toTerm());
        result.addMember(mStatements.toTerm());
        return result;
    }

    public static For termToStatement(Term term) throws TermConversionException {
        if (term.size() != 2) {
            throw new TermConversionException("malformed " + FUNCTIONAL_CHARACTER);
        } else {
            Iterator    iterator    = Iterator.valueToIterator(term.firstMember());
            Block       block       = TermConverter.valueToBlock(term.lastMember());
            return new For(iterator, block);
        }
    }
}

