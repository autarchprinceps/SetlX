package org.randoom.setlx.statements;

import org.randoom.setlx.exceptions.IncompatibleTypeException;
import org.randoom.setlx.exceptions.SetlException;
import org.randoom.setlx.exceptions.TermConversionException;
import org.randoom.setlx.expressions.Variable;
import org.randoom.setlx.types.CollectionValue;
import org.randoom.setlx.types.SetlList;
import org.randoom.setlx.types.SetlString;
import org.randoom.setlx.types.Term;
import org.randoom.setlx.types.Value;
import org.randoom.setlx.utilities.Condition;
import org.randoom.setlx.utilities.Environment;
import org.randoom.setlx.utilities.MatchResult;
import org.randoom.setlx.utilities.TermConverter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/*
grammar rule:
statement
    : [...]
    | 'match' '(' expr ')' '{' ('case' '[' listOfVariables '|' variable ']' ('|' condition)? ':' block | [...] )* ('default' ':' block)? '}'
    ;

implemented here as:
                                           ===============     ========          =========       =====
                                                mVars           mRest            mCondition   mStatements
*/

public class MatchSplitListBranch extends MatchAbstractBranch {
    // functional character used in terms
    /*package*/ final static String FUNCTIONAL_CHARACTER = "^matchSplitListBranch";

    private final List<Variable> mVars;       // variables which are to be extracted
    private final List<Value>    mVarTerms;   // terms of variables which are to be extracted
    private final Variable       mRest;       // variable for the rest of the list
    private final Value          mRestTerm;   // term of variable for the rest of the list
    private final Condition      mCondition;  // optional condition to confirm match
    private final Block          mStatements; // block to execute after match

    public MatchSplitListBranch(final List<Variable> vars, final Variable rest, final Condition condition, final Block statements){
        mVars       = vars;
        mVarTerms   = new ArrayList<Value>(vars.size());
        for (final Variable var : vars) {
            mVarTerms.add(var.toTerm());
        }
        mRest       = rest;
        mRestTerm   = rest.toTerm();
        mCondition  = condition;
        mStatements = statements;
    }

    public MatchResult matches(final Value term) throws IncompatibleTypeException {
        if (term instanceof SetlList || term instanceof SetlString) {
            final CollectionValue other = (CollectionValue) term.clone();
            if (other.size() >= mVars.size()) {
                final MatchResult result = new MatchResult(true);
                for (final Value varTerm : mVarTerms) {
                    final MatchResult subResult = varTerm.matchesTerm(other.firstMember());
                    if (subResult.isMatch()) {
                        result.addBindings(subResult);
                        other.removeFirstMember();
                    } else {
                        return new MatchResult(false);
                    }
                }
                final MatchResult subResult = mRestTerm.matchesTerm(other);
                if (subResult.isMatch()) {
                    result.addBindings(subResult);
                    return result;
                } else {
                    return new MatchResult(false);
                }
            } else {
                return new MatchResult(false);
            }
        } else {
            return new MatchResult(false);
        }
    }

    public boolean evalConditionToBool() throws SetlException {
        if (mCondition != null) {
            return mCondition.evalToBool();
        } else {
            return true;
        }
    }

    public Value execute() throws SetlException {
        return mStatements.execute();
    }

    protected Value exec() throws SetlException {
        return execute();
    }

    /* string operations */

    public void appendString(final StringBuilder sb, final int tabs) {
        Environment.getLineStart(sb, tabs);
        sb.append("case [");

        final Iterator<Variable> iter = mVars.iterator();
        while (iter.hasNext()) {
            iter.next().appendString(sb, 0);
            if (iter.hasNext()) {
                sb.append(", ");
            }
        }

        sb.append(" | ");
        mRest.appendString(sb, 0);
        sb.append("] ");

        if (mCondition != null) {
            sb.append("| ");
            mCondition.appendString(sb, tabs);
        }

        sb.append(":");
        sb.append(Environment.getEndl());
        mStatements.appendString(sb, tabs + 1);
        sb.append(Environment.getEndl());
    }

    /* term operations */

    public Term toTerm() {
        final Term     result  = new Term(FUNCTIONAL_CHARACTER, 4);

        final SetlList varList = new SetlList(mVars.size());
        for (final Value varTerm: mVarTerms) {
            varList.addMember(varTerm.clone());
        }
        result.addMember(varList);

        result.addMember(mRest.toTerm());

        if (mCondition != null) {
            result.addMember(mCondition.toTerm());
        } else {
            result.addMember(new SetlString("nil"));
        }

        result.addMember(mStatements.toTerm());

        return result;
    }

    public static MatchSplitListBranch termToBranch(final Term term) throws TermConversionException {
        if (term.size() != 4 || ! (term.firstMember() instanceof SetlList)) {
            throw new TermConversionException("malformed " + FUNCTIONAL_CHARACTER);
        } else {
            try {
                final SetlList        varList = (SetlList) term.firstMember();
                final List<Variable>  vars    = new ArrayList<Variable>(varList.size());
                for (final Value var : varList) {
                    if (var instanceof Term) {
                        vars.add(Variable.termToExpr((Term) var));
                    } else {
                        throw new TermConversionException("malformed " + FUNCTIONAL_CHARACTER);
                    }
                }
                Variable rest = null;
                if (term.getMember(2) instanceof Term) {
                    rest = Variable.termToExpr((Term) term.getMember(2));
                } else {
                    throw new TermConversionException("malformed " + FUNCTIONAL_CHARACTER);
                }
                Condition condition = null;
                if (! term.getMember(3).equals(new SetlString("nil"))) {
                    condition = TermConverter.valueToCondition(term.getMember(3));
                }
                final Block block = TermConverter.valueToBlock(term.lastMember());
                return new MatchSplitListBranch(vars, rest, condition, block);
            } catch (SetlException se) {
                throw new TermConversionException("malformed " + FUNCTIONAL_CHARACTER);
            }
        }
    }
}

