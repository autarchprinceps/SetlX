package org.randoom.setlx.statements;

import org.randoom.setlx.exceptions.BacktrackException;
import org.randoom.setlx.exceptions.SetlException;
import org.randoom.setlx.exceptions.TermConversionException;
import org.randoom.setlx.types.SetlString;
import org.randoom.setlx.types.Term;
import org.randoom.setlx.utilities.ReturnMessage;
import org.randoom.setlx.utilities.State;
import org.randoom.setlx.utilities.TermConverter;

import java.util.List;

/**
 * The check statement, which is used when implementing a backtrack-like
 * algorithm in conjunction with the backtrack-statement.
 *
 * grammar rule:
 * statement
 *     : [...]
 *     | 'check' '{' block '}' ('afterBacktrack' '{' block '}')?
 *     ;
 *
 * implemented here as:
 *                   =====                           =====
 *                mStatements                      mRecovery
 */
public class Check extends Statement {
    // functional character used in terms
    private final static String FUNCTIONAL_CHARACTER = generateFunctionalCharacter(Check.class);

    private final Block statements;
    private final Block recovery;

    public Check(final Block statements, final Block recovery) {
        this.statements = statements;
        this.recovery   = recovery;
    }

    @Override
    public ReturnMessage execute(final State state) throws SetlException {
        try {
            return statements.execute(state);
        } catch (final BacktrackException bte) {
            if (recovery != null) {
                return recovery.execute(state);
            } else {
                return null;
            }
        }
    }

    @Override
    public void collectVariablesAndOptimize (
        final List<String> boundVariables,
        final List<String> unboundVariables,
        final List<String> usedVariables
    ) {
        statements.collectVariablesAndOptimize(boundVariables, unboundVariables, usedVariables);

        // bindings inside the recovery block are not always valid --- ignore them
        final int preBound = boundVariables.size();
        if (recovery != null) {
            recovery.collectVariablesAndOptimize(boundVariables, unboundVariables, usedVariables);
        }
        while (boundVariables.size() > preBound) {
            boundVariables.remove(boundVariables.size() - 1);
        }
    }

    /* string operations */

    @Override
    public void appendString(final State state, final StringBuilder sb, final int tabs) {
        state.appendLineStart(sb, tabs);
        sb.append("check ");
        statements.appendString(state,sb, tabs, true);
        if (recovery != null) {
            sb.append(" afterBacktrack ");
            recovery.appendString(state, sb, tabs, true);
        }
    }

    /* term operations */

    @Override
    public Term toTerm(final State state) {
        final Term result = new Term(FUNCTIONAL_CHARACTER, 2);
        result.addMember(state, statements.toTerm(state));
        if (recovery != null) {
            result.addMember(state, recovery.toTerm(state));
        } else {
            result.addMember(state, new SetlString("nil"));
        }
        return result;
    }

    public static Check termToStatement(final Term term) throws TermConversionException {
        if (term.size() != 2) {
            throw new TermConversionException("malformed " + FUNCTIONAL_CHARACTER);
        } else {
            final Block block    = TermConverter.valueToBlock(term.firstMember());
                  Block recovery = null;
            if ( ! term.lastMember().equals(new SetlString("nil"))) {
                recovery = TermConverter.valueToBlock(term.lastMember());
            }
            return new Check(block, recovery);
        }
    }
}

