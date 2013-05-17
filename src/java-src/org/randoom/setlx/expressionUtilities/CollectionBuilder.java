package org.randoom.setlx.expressionUtilities;

import org.randoom.setlx.exceptions.SetlException;
import org.randoom.setlx.exceptions.TermConversionException;
import org.randoom.setlx.exceptions.UndefinedOperationException;
import org.randoom.setlx.types.CollectionValue;
import org.randoom.setlx.types.IndexedCollectionValue;
import org.randoom.setlx.types.Om;
import org.randoom.setlx.types.Term;
import org.randoom.setlx.utilities.CodeFragment;
import org.randoom.setlx.utilities.State;
import org.randoom.setlx.utilities.VariableScope;

import java.util.List;

public abstract class CollectionBuilder extends CodeFragment {

    /**
     * Expand given collection with values generated by executing this builder.
     *
     * @param state          Current state of the running setlX program.
     * @param collection     Collection to extend.
     * @throws SetlException Thrown in case of some (user-) error.
     */
    public abstract void fillCollection(
        final State           state,
        final CollectionValue collection
    ) throws SetlException;

    @Override // Explicit override to increase visibility to public.
    public abstract void collectVariablesAndOptimize (
        final List<String> boundVariables,
        final List<String> unboundVariables,
        final List<String> usedVariables
    );

    /**
     * Gather all bound and unbound variables in this expression and its siblings,
     * when it is used as an assignment.
     *
     * @see org.randoom.setlx.utilities.CodeFragment#collectVariablesAndOptimize(List<String>, List<String>, List<String>)
     *
     * @param boundVariables   Variables "assigned" in this fragment.
     * @param unboundVariables Variables not present in bound when used.
     * @param usedVariables    Variables present in bound when used.
     */
    public void collectVariablesWhenAssigned (
        final List<String> boundVariables,
        final List<String> unboundVariables,
        final List<String> usedVariables
    ) {
        /* do nothing by default */
    }

    /**
     * Sets this list of expressions in this builder to the values contained in
     * the given collection value. Does not clone 'collection' and does
     * not return 'collection' for chained assignments.
     *
     * @param state          Current state of the running setlX program.
     * @param collection     Collection to assign from.
     * @param context        Context description of the assignment for trace.
     * @throws SetlException Thrown in case of some (user-) error.
     */
    public          void assignUncloned(
        final State                  state,
        final IndexedCollectionValue collection,
        final String                 context
    ) throws SetlException {
        throw new UndefinedOperationException(
            "Error in \"" + this + "\":\n" +
            "Only explicit lists can be used as targets for list assignments."
        );
    }

    /**
     * Sets this list of expressions in this builder to the values contained in
     * the given collection value. Does not clone 'collection' and does
     * not return 'collection' for chained assignments.
     *
     * Also checks if the variables to be set are already defined in scopes up to
     * (but EXCLUDING) 'outerScope'.
     * Returns true and sets the values, if each variable is undefined or already equal the the value to be set.
     * Returns false, if a variable is defined and different.
     *
     * @param state          Current state of the running setlX program.
     * @param collection     Collection to assign from.
     * @param outerScope     Root scope of scopes to check.
     * @return               True, if variable is undefined or already equal the the value to be set.
     * @throws SetlException Thrown in case of some (user-) error.
     */
    public          boolean assignUnclonedCheckUpTo(
        final State                  state,
        final IndexedCollectionValue collection,
        final VariableScope          outerScope,
        final String                 context
    ) throws SetlException {
        throw new UndefinedOperationException(
            "Error in \"" + this + "\":\n" +
            "Only explicit lists can be used as targets for list assignments."
        );
    }

    /* String operations */

    @Override
    public          void        appendString(final State state, final StringBuilder sb, final int tabs) {
        appendString(state, sb);
    }

    /**
     * Appends a string representation of this code fragment to the given
     * StringBuilder object.
     *
     * @see org.randoom.setlx.utilities.CodeFragment#appendString(State, StringBuilder, int)
     *
     * @param state Current state of the running setlX program.
     * @param sb    StringBuilder to append to.
     */
    public abstract void        appendString(final State state, final StringBuilder sb);

    /* term operations */

    @Override
    public          Om          toTerm(final State state) {
        return Om.OM;
    }

    /**
     * Expand given CollectionValue with the term representation of this builder.
     *
     * @param state      Current state of the running setlX program.
     * @param collection CollectionValue to expand.
     */
    public abstract void        addToTerm(final State state, final CollectionValue collection);

    /**
     * Regenerate CollectionBuilder from a CollectionValue containing a term
     * representation.
     *
     * @param value                    CollectionValue containing the term representation.
     * @return                         Regenerated CollectionBuilder object.
     * @throws TermConversionException Thrown in case the term is malformed.
     */
    public static   CollectionBuilder collectionValueToBuilder(final CollectionValue value) throws TermConversionException {
        if (value.size() == 1 && value.firstMember() instanceof Term) {
            final Term    term    = (Term) value.firstMember();
            final String  fc      = term.functionalCharacter().getUnquotedString();
            if (fc.equals(SetlIteration.FUNCTIONAL_CHARACTER)) {
                return SetlIteration.termToIteration(term);
            } else if (fc.equals(Range.FUNCTIONAL_CHARACTER)) {
                return Range.termToRange(term);
            } else if (fc.equals(ExplicitListWithRest.FUNCTIONAL_CHARACTER)) {
                return ExplicitListWithRest.termToExplicitListWithRest(term);
            } else {
                // assume explicit list of a single term
                return ExplicitList.collectionValueToExplicitList(value);
            }
        } else {
            // assume explicit list;
            return ExplicitList.collectionValueToExplicitList(value);
        }
    }
}

