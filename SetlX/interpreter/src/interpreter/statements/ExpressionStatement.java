package interpreter.statements;

import interpreter.Environment;
import interpreter.exceptions.SetlException;
import interpreter.expressions.Expr;
import interpreter.types.Value;

public class ExpressionStatement extends Statement {
    private Expr   mExpr;

    public ExpressionStatement(Expr expression) {
        mExpr = expression;
    }

    public void execute() throws SetlException {
        Value v = mExpr.eval();
        if (Environment.isInteractive()) {
            System.out.println("// Result: " + v);
        }
    }

    public String toString(int tabs) {
        return Environment.getTabs(tabs) + mExpr + ";";
    }
}
