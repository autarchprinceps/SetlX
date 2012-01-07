package interpreter.statements;

import interpreter.exceptions.CatchableInSetlXException;
import interpreter.exceptions.SetlException;
import interpreter.expressions.Variable;
import interpreter.types.SetlError;
import interpreter.types.Term;
import interpreter.utilities.Environment;

/*
grammar rule:
statement
    : [...]
    | 'try' '{' block '}' 'catch' '(' variable ')' '{' block '}'
    ;

implemented here as:
                =====                 ========         =====
             mBlockToTry              mErrorVar   mBlockToRecover
*/

public class TryCatch extends Statement {
    private Block       mBlockToTry;
    private Variable    mErrorVar;
    private Block       mBlockToRecover;

    public TryCatch(Block blockToTry, Variable errorVar, Block blockToRecover) {
        mBlockToTry     = blockToTry;
        mErrorVar       = errorVar;
        mBlockToRecover = blockToRecover;
    }

    public void execute() throws SetlException {
        try{
            mBlockToTry.execute();
        } catch (CatchableInSetlXException cisxe) {
            mErrorVar.assign(new SetlError(cisxe));
            mBlockToRecover.execute();
        }
    }

    /* string operations */

    public String toString(int tabs) {
        String result = Environment.getTabs(tabs);
        result += "try ";
        result += mBlockToTry.toString(tabs, true);
        result += " catch (" + mErrorVar + ") ";
        result += mBlockToRecover.toString(tabs, true);
        return result;
    }

    /* term operations */

    public Term toTerm() {
        Term result = new Term("'tryCatch");
        result.addMember(mBlockToTry.toTerm());
        result.addMember(mErrorVar.toTerm());
        result.addMember(mBlockToRecover.toTerm());
        return result;
    }
}
