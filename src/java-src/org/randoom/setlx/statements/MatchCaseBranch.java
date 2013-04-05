package org.randoom.setlx.statements;

import org.randoom.setlx.exceptions.SetlException;
import org.randoom.setlx.exceptions.TermConversionException;
import org.randoom.setlx.expressionUtilities.Condition;
import org.randoom.setlx.expressions.Expr;
import org.randoom.setlx.types.SetlList;
import org.randoom.setlx.types.SetlString;
import org.randoom.setlx.types.Term;
import org.randoom.setlx.types.Value;
import org.randoom.setlx.utilities.MatchResult;
import org.randoom.setlx.utilities.ReturnMessage;
import org.randoom.setlx.utilities.State;
import org.randoom.setlx.utilities.TermConverter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/*
grammar rule:
statement
    : [...]
    | 'match' '(' expr ')' '{' ('case' exprList ('|' condition)? ':' block | [...] )* ('default' ':' block)? '}'
    ;

implemented here as:
                                       ========      =========       =====
                                        mTerms       mCondition    mStatements
*/

public class MatchCaseBranch extends MatchAbstractBranch {
    // functional character used in terms
    /*package*/ final static String FUNCTIONAL_CHARACTER = generateFunctionalCharacter(MatchCaseBranch.class);

    private final List<Expr>  exprs;      // expressions which creates terms to match
    private final List<Value> terms;      // terms to match
    private final Condition   condition;  // optional condition to confirm match
    private final Block       statements; // block to execute after match

    public MatchCaseBranch(final List<Expr> exprs, final Condition condition, final Block statements){
        this.exprs      = exprs;
        this.terms      = new ArrayList<Value>(exprs.size());
        this.condition  = condition;
        this.statements = statements;
    }

    @Override
    public MatchResult matches(final State state, final Value term) throws SetlException {
        if (exprs.size() > terms.size()) {
            for (final Expr expr: exprs) {
                terms.add(expr.toTerm(state));
            }
        }

        MatchResult last = new MatchResult(false);
        for (final Value v : terms) {
            last = v.matchesTerm(state, term);
            if (last.isMatch()) {
                return last;
            }
        }
        return last;
    }

    @Override
    public boolean evalConditionToBool(final State state) throws SetlException {
        if (condition != null) {
            return condition.evalToBool(state);
        } else {
            return true;
        }
    }

    @Override
    public ReturnMessage exec(final State state) throws SetlException {
        return statements.exec(state);
    }

    @Override
    protected ReturnMessage execute(final State state) throws SetlException {
        return exec(state);
    }

    /* Gather all bound and unbound variables in this statement and its siblings
          - bound   means "assigned" in this expression
          - unbound means "not present in bound set when used"
          - used    means "present in bound set when used"
       Optimize sub-expressions during this process by calling optimizeAndCollectVariables()
       when adding variables from them.
    */
    @Override
    public void collectVariablesAndOptimize (
        final List<String> boundVariables,
        final List<String> unboundVariables,
        final List<String> usedVariables
    ) {
        /* Variables in these expressions get assigned temporarily.
           Collect them into a temporary list, add them to boundVariables and
           remove them again before returning. */
        final List<String> tempAssigned = new ArrayList<String>();
        for (final Expr expr : exprs) {
            expr.collectVariablesAndOptimize(new ArrayList<String>(), tempAssigned, tempAssigned);
        }

        final int preIndex = boundVariables.size();
        boundVariables.addAll(tempAssigned);

        if (condition != null) {
            condition.collectVariablesAndOptimize(boundVariables, unboundVariables, usedVariables);
        }

        statements.collectVariablesAndOptimize(boundVariables, unboundVariables, usedVariables);

        // remove the added variables (DO NOT use removeAll(); same variable name could be there multiple times!)
        for (int i = tempAssigned.size(); i > 0; --i) {
            boundVariables.remove(preIndex + (i - 1));
        }
    }

    /* string operations */

    @Override
    public void appendString(final State state, final StringBuilder sb, final int tabs) {
        state.appendLineStart(sb, tabs);
        sb.append("case ");

        final Iterator<Expr> iter = exprs.iterator();
        while (iter.hasNext()) {
            iter.next().appendString(state, sb, tabs);
            if (iter.hasNext()) {
                sb.append(", ");
            }
        }

        if (condition != null) {
            sb.append(" | ");
            condition.appendString(state, sb, tabs);
        }

        sb.append(":");
        sb.append(state.getEndl());
        statements.appendString(state, sb, tabs + 1);
        sb.append(state.getEndl());
    }

    /* term operations */

    @Override
    public Term toTerm(final State state) {
        final Term     result   = new Term(FUNCTIONAL_CHARACTER, 3);

        final SetlList termList = new SetlList(terms.size());
        for (final Expr expr: exprs) {
            termList.addMember(state, expr.toTerm(state));
        }
        result.addMember(state, termList);

        if (condition != null) {
            result.addMember(state, condition.toTerm(state));
        } else {
            result.addMember(state, new SetlString("nil"));
        }

        result.addMember(state, statements.toTerm(state));

        return result;
    }

    public static MatchCaseBranch termToBranch(final Term term) throws TermConversionException {
        if (term.size() != 3 || ! (term.firstMember() instanceof SetlList)) {
            throw new TermConversionException("malformed " + FUNCTIONAL_CHARACTER);
        } else {
            try {
                final SetlList    termList  = (SetlList) term.firstMember();
                final List<Expr>  exprs     = new ArrayList<Expr>(termList.size());
                for (final Value v : termList) {
                    exprs.add(TermConverter.valueToExpr(v));
                }
                Condition condition = null;
                if (! term.getMember(2).equals(new SetlString("nil"))) {
                    condition = TermConverter.valueToCondition(term.getMember(2));
                }
                final Block block = TermConverter.valueToBlock(term.lastMember());
                return new MatchCaseBranch(exprs, condition, block);
            } catch (final SetlException se) {
                throw new TermConversionException("malformed " + FUNCTIONAL_CHARACTER);
            }
        }
    }
}

