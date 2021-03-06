package org.randoom.setlx.functions;

import org.randoom.setlx.exceptions.SetlException;
import org.randoom.setlx.types.SetlString;
import org.randoom.setlx.types.Value;
import org.randoom.setlx.utilities.SetlXUserPanel;
import org.randoom.setlx.utilities.State;

import java.util.List;

public class PD_gfx_getInput extends GfxFunction {
public final static PreDefinedProcedure DEFINITION = new PD_gfx_getInput();

    public PD_gfx_getInput(){
        super();
    }

    @Override
    protected Value execute(final State state, final List<Value> args, final List<Value> writeBackVars) throws SetlException{
        final String result = SetlXUserPanel.getInstance().getInput();
        return new SetlString(result);
    }
}
