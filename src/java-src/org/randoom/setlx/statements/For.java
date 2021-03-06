package org.randoom.setlx.statements;

import org.randoom.setlx.exceptions.SetlException;
import org.randoom.setlx.exceptions.TermConversionException;
import org.randoom.setlx.expressionUtilities.Condition;
import org.randoom.setlx.expressionUtilities.SetlIterator;
import org.randoom.setlx.expressionUtilities.SetlIteratorExecutionContainer;
import org.randoom.setlx.types.SetlBoolean;
import org.randoom.setlx.types.SetlString;
import org.randoom.setlx.types.Term;
import org.randoom.setlx.types.Value;
import org.randoom.setlx.utilities.ReturnMessage;
import org.randoom.setlx.utilities.State;
import org.randoom.setlx.utilities.TermConverter;

import java.util.List;

/**
 * SetlX's version of a counting loop
 *
 * grammar rule:
 * statement
 *     : [...]
 *     | 'for' '(' iteratorChain | condition ')' '{' block '}'
 *     ;
 *
 * implemented here as:
 *                 ========-----   =========         =====
 *                   iterator      condition       statements
 *
 */
public class For extends Statement {
    // functional character used in terms
    private final static String  FUNCTIONAL_CHARACTER = generateFunctionalCharacter(For.class);

    private final SetlIterator   iterator;
    private final Condition      condition;
    private final Block          statements;
    private final Exec           exec;

    private class Exec implements SetlIteratorExecutionContainer {
        private final Condition condition;
        private final Block     statements;

        public Exec(final Condition condition, final Block statements) {
            this.condition  = condition;
            this.statements = statements;
        }

        @Override
        public ReturnMessage execute(final State state, final Value lastIterationValue) throws SetlException {
            if (condition == null || condition.eval(state) == SetlBoolean.TRUE) {
                return statements.execute(state);
                // ContinueException and BreakException are handled by outer iterator
            }
            return null;
        }

        @Override
        public void collectVariablesAndOptimize (
            final List<String> boundVariables,
            final List<String> unboundVariables,
            final List<String> usedVariables
        ) {
            if (condition != null) {
                condition.collectVariablesAndOptimize(boundVariables, unboundVariables, usedVariables);
            }
            statements.collectVariablesAndOptimize(boundVariables, unboundVariables, usedVariables);
        }
    }

    public For(final SetlIterator iterator, final Condition condition, final Block statements) {
        this.iterator   = iterator;
        this.condition  = condition;
        this.statements = statements;
        this.exec       = new Exec(condition, statements);
    }

    @Override
    public ReturnMessage execute(final State state) throws SetlException {
        return iterator.eval(state, exec);
    }

    @Override
    public void collectVariablesAndOptimize (
        final List<String> boundVariables,
        final List<String> unboundVariables,
        final List<String> usedVariables
    ) {
        iterator.collectVariablesAndOptimize(exec, boundVariables, unboundVariables, usedVariables);
    }

    /* string operations */

    @Override
    public void appendString(final State state, final StringBuilder sb, final int tabs) {
        state.appendLineStart(sb, tabs);
        sb.append("for (");
        iterator.appendString(state, sb, 0);
        if (condition != null) {
            sb.append(" | ");
            condition.appendString(state, sb, 0);
        }
        sb.append(") ");
        statements.appendString(state, sb, tabs, true);
    }

    /* term operations */

    @Override
    public Term toTerm(final State state) {
        final Term result = new Term(FUNCTIONAL_CHARACTER, 3);
        result.addMember(state, iterator.toTerm(state));
        if (condition != null) {
            result.addMember(state, condition.toTerm(state));
        } else {
            result.addMember(state, new SetlString("nil"));
        }
        result.addMember(state, statements.toTerm(state));
        return result;
    }

    public static For termToStatement(final Term term) throws TermConversionException {
        if (term.size() != 3) {
            throw new TermConversionException("malformed " + FUNCTIONAL_CHARACTER);
        } else {
            try {
                final SetlIterator  iterator  = SetlIterator.valueToIterator(term.firstMember());
                      Condition condition = null;
                if ( ! term.getMember(2).equals(new SetlString("nil"))) {
                    condition = TermConverter.valueToCondition(term.getMember(2));
                }
                final Block     block     = TermConverter.valueToBlock(term.lastMember());
                return new For(iterator, condition, block);
            } catch (final SetlException se) {
                throw new TermConversionException("malformed " + FUNCTIONAL_CHARACTER);
            }
        }
    }
}

