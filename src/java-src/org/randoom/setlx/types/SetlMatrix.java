package org.randoom.setlx.types;

import Jama.EigenvalueDecomposition;
import Jama.SingularValueDecomposition;
import java.util.Arrays;
import java.util.Iterator;
import org.randoom.setlx.exceptions.IncompatibleTypeException;
import org.randoom.setlx.exceptions.MatrixException;
import org.randoom.setlx.exceptions.SetlException;
import org.randoom.setlx.exceptions.UndefinedOperationException;
import org.randoom.setlx.utilities.MatchResult;
import org.randoom.setlx.utilities.State;

/**
 * @author Patrick Robinson
 */
public class SetlMatrix extends IndexedCollectionValue { // TODO Is not a CollectionValue Exception ?
    private Jama.Matrix value;
    
    private SetlMatrix(Jama.Matrix v) {
        super();
        this.value = v;
    }

    public SetlMatrix(final State state, final CollectionValue init) throws SetlException {
        super();
        final int rowCount = init.size();
        final int columnCount = ((CollectionValue)init.firstMember()).size();
        double[][] base = new double[rowCount][columnCount];
        int currentRow = 0;
        for(Value row : init) {
            if(!(row instanceof CollectionValue)) {
                throw new IncompatibleTypeException("Row " + (currentRow + 1) + " is not of collection type.");
            }
            CollectionValue rowAsCollection = (CollectionValue)row;
            if(rowAsCollection.size() < 1) {
                throw new IncompatibleTypeException("Row " + (currentRow + 1) + "is empty.");
            }
            if(rowAsCollection.size() != columnCount) {
                // TODO is this an IncompatibleTypeException?
                throw new IncompatibleTypeException("Row " + (currentRow + 1) + " does not have the same length as the first row.");
            }
            int currentColumn = 0;
            for(Value cell : rowAsCollection) {
                if(!(cell instanceof NumberValue)) { // TODO Term? or isJDoubleConvertable then convert
                    throw new IncompatibleTypeException("Cell(row " + (currentRow + 1) + " column " + (currentColumn + 1) + ") is not a number.");
                }
                base[currentRow][currentColumn] = cell.toJDoubleValue(state);
                currentColumn++;
            }
            currentRow++;
        }
        value = new Jama.Matrix(base);
    }

    /* (non-Javadoc)
     * @see org.randoom.setlx.types.Value#clone()
     */
    @Override
    public Value clone() {
        return new SetlMatrix(value.copy());
    }

    /* (non-Javadoc)
     * @see org.randoom.setlx.types.Value#appendString(org.randoom.setlx.utilities.State, java.lang.StringBuilder, int)
     */
    @Override
    public void appendString(State state, StringBuilder sb, int tabs) {
        // TODO does this work as it should?
        this.canonical(state, sb);
    }

    /* (non-Javadoc)
     * @see org.randoom.setlx.types.Value#compareTo(org.randoom.setlx.types.Value)
     */
    @Override
    public int compareTo(Value other) {
        // TODO Encoding right?
        return this.equalTo(other) ? 0 : 1;
    }

    /* (non-Javadoc)
     * @see org.randoom.setlx.types.Value#compareToOrdering()
     */
    @Override
    protected int compareToOrdering() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.randoom.setlx.types.Value#equalTo(org.randoom.setlx.types.Value)
     */
    @Override
    public boolean equalTo(Value other) {
        return other instanceof SetlMatrix && Arrays.deepEquals(this.value.getArray(), ((SetlMatrix)other).value.getArray());
    }

    /* (non-Javadoc)
     * @see org.randoom.setlx.types.Value#hashCode()
     */
    @Override
    public int hashCode() {
        return value.hashCode();
    }
    
    @Override
    public Value product(final State state, final Value multiplier) throws SetlException {
        if(multiplier instanceof SetlMatrix) {
            SetlMatrix b = (SetlMatrix)multiplier;
            // TODO check conditions
            return new SetlMatrix(this.value.times(b.value));
        } else if(multiplier instanceof NumberValue) {
            NumberValue n = (NumberValue)multiplier;
            return new SetlMatrix(this.value.times(n.toJDoubleValue(state)));
        } else if(multiplier instanceof Term) {
            // TODO implement this:
            return ((Term)multiplier).productFlipped(state, this);
        } else {
            throw new IncompatibleTypeException("Summand is not of type Matrix.");
        }
    }
    
    @Override
    public Value productAssign(final State state, final Value multiplier) throws SetlException {
        if(multiplier instanceof SetlMatrix) {
            SetlMatrix b = (SetlMatrix)multiplier;
            // TODO check conditions
            this.value = this.value.times(b.value);
            return this;
        } else if(multiplier instanceof NumberValue) {
            NumberValue n = (NumberValue)multiplier;
            this.value.timesEquals(n.toJDoubleValue(state));
            return this;
        } else if(multiplier instanceof Term) {
            // TODO implement this
            throw new MatrixException("Not implemented");
        } else {
            throw new IncompatibleTypeException("Summand is not of type Matrix.");
        }
    }
    
    public SetlMatrix transpose() {
        return new SetlMatrix(this.value.transpose());
    }
    
    // TODO broken
    @Override
    public Value power(final State state, final Value exponent) throws SetlException {
        // TODO check condition
        if(exponent.jIntConvertable()) {
            int ex = exponent.toJIntValue(state);
            Jama.Matrix base;
            if(ex < 0) {
                base = this.value.inverse();
                ex = -ex;
            } else {
                if(ex == 0) {
                    // TODO What now? How is this defined on SetlMatrix
                }
                base = this.value;
            }
            Jama.Matrix result = base;
            for(int i = 1 /* No mistake, should be one */; i < ex; i++) {
                result = result.times(base);
            }
            return new SetlMatrix(result);
        } else {
            throw new IncompatibleTypeException("Power is only defined for integer exponents on matrices."); // TODO Check English
        }
    }
    
    @Override
    public Value sum(final State state, final Value summand) throws MatrixException {
        if(summand instanceof SetlMatrix) {
            SetlMatrix b = (SetlMatrix)summand;
            // TODO check conditions
            return new SetlMatrix(this.value.plus(b.value));
        } else {
            throw new MatrixException("Summand is not of type Matrix.");
        }
    }
    
    @Override
    public Value sumAssign(final State state, final Value summand) throws MatrixException {
        if(summand instanceof SetlMatrix) {
            SetlMatrix b = (SetlMatrix)summand;
            // TODO check conditions
            this.value.plusEquals(b.value);
            return this;
        } else {
            throw new MatrixException("Summand is not of type Matrix.");
        }
    }
    
    @Override
    public Value difference(final State state, final Value subtrahend) throws SetlException {
        if(subtrahend instanceof SetlMatrix) {
            SetlMatrix b = (SetlMatrix)subtrahend;
            // TODO check conditions
            return new SetlMatrix(this.value.minus(b.value));
        } else {
            throw new MatrixException("Subtrahend is not of type Matrix.");
        }
    }
    
    @Override
    public Value differenceAssign(final State state, final Value subtrahend) throws SetlException {
        if(subtrahend instanceof SetlMatrix) {
            SetlMatrix b = (SetlMatrix)subtrahend;
            // TODO check conditions
            this.value.minusEquals(b.value);
            return this;
        } else {
            throw new MatrixException("Subtrahend is not of type Matrix.");
        }
    }
    
    @Override
    public void canonical(final State state, final StringBuilder sb) {
        double[][] a = value.getArray();
        sb.append("<");
        for(double[] a1 : a) {
            sb.append(" [");
            for(double a2 : a1) {
                sb.append(" ").append(a2).append(" ");
            }
            sb.append("] ");
        }
        sb.append(">");
    }

    @Override
    public Value getMember(int index) throws SetlException {
        SetlList container = new SetlList(this.value.getColumnDimension());
        for(double d : this.value.getArray()[index - 1]) {
            container.addMember(null, SetlDouble.valueOf(d));
        }
        return container;
    }
    
    private SetlList toSetlList(final State state) {
        SetlList container = new SetlList(this.value.getRowDimension());
        for (double[] a : this.value.getArray()) {
            SetlList row = new SetlList(this.value.getColumnDimension());
            for (double b : a) {
                try {
                    row.addMember(state, SetlDouble.valueOf(b));
                } catch (UndefinedOperationException ex) {
                    // TODO wtf?
                }
            }
            container.addMember(state, row);
        }
        return container;
    }

    @Override
    public Iterator<Value> iterator() {
        return this.toSetlList(null).iterator();
    }

    @Override
    public Iterator<Value> descendingIterator() {
        return this.toSetlList(null).descendingIterator();
    }

    @Override
    public void addMember(State state, Value element) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private static double epsilon = 1E-5;
    
    @Override
    public SetlBoolean containsMember(State state, Value element) throws IncompatibleTypeException {
        if(element instanceof CollectionValue) {
            CollectionValue c = (CollectionValue)element;
            double[] v = new double[c.size()];
            int i = 0;
            for(Value subelem : c) {
                try {
                    v[i] = subelem.toJDoubleValue(state);
                } catch (SetlException ex) {
                    return SetlBoolean.FALSE;
                }
                i++;
            }
            for(double[] a : this.value.getArray()) {
                if(Arrays.equals(a,v)) {
                    return SetlBoolean.TRUE;
                }
            }
            return SetlBoolean.FALSE;
        } else if(element instanceof NumberValue) {
            try {
                double v = ((NumberValue)element).toJDoubleValue(state);
                for(double[] a : this.value.getArray()) {
                    for(double b : a) {
                        if(Math.abs(b - v) < epsilon) {
                            return SetlBoolean.TRUE;
                        }
                    }
                }
            } catch (SetlException ex) {
                // TODO Do something more?
                return SetlBoolean.FALSE;
            }
        }
        return SetlBoolean.FALSE;
    }

    @Override
    public Value firstMember() {
        try {
            return this.getMember(1);
        } catch (SetlException ex) {
            // TODO do something
            // Logger.getLogger(SetlMatrix.class.getName()).log(Level.SEVERE, null, ex);
            return Om.OM;
        }
    }

    @Override
    public Value getMember(State state, Value index) throws SetlException {
        if((index instanceof NumberValue)) {
            return this.getMember(((NumberValue)index).toJIntValue(state));
        } else {
            throw new IncompatibleTypeException("Given index is not a number.");
        }
    }

    @Override
    public Value lastMember() {
        try {
            return this.getMember(this.value.getRowDimension());
        } catch (SetlException ex) {
            // TODO do something
            // Logger.getLogger(SetlMatrix.class.getName()).log(Level.SEVERE, null, ex);
            return Om.OM;
        }
    }

    @Override
    public Value maximumMember(State state) throws SetlException {
        double momentaryMax = Double.NEGATIVE_INFINITY;
        for(double[] a : this.value.getArray()) {
            for(double b : a) {
                if(b > momentaryMax) momentaryMax = b;
            }
        }
        return SetlDouble.valueOf(momentaryMax);
    }

    @Override
    public Value minimumMember(State state) throws SetlException {
        double momentaryMin = Double.POSITIVE_INFINITY;
        for(double[] a : this.value.getArray()) {
            for(double b : a) {
                if(b < momentaryMin) momentaryMin = b;
            }
        }
        return SetlDouble.valueOf(momentaryMin);
    }

    @Override
    public void removeMember(Value element) throws IncompatibleTypeException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Value removeFirstMember() {
        double[][] result = new double[this.value.getColumnDimension() - 1][this.value.getRowDimension()];
        System.arraycopy(this.value.getArray(), 1, result, 0, this.value.getColumnDimension() - 1);
        return new SetlMatrix(new Jama.Matrix(result));
    }

    @Override
    public Value removeLastMember() {
        double[][] result = new double[this.value.getColumnDimension() - 1][this.value.getRowDimension()];
        System.arraycopy(this.value.getArray(), 0, result, 0, this.value.getColumnDimension() - 1);
        return new SetlMatrix(new Jama.Matrix(result));
    }

    @Override
    public int size() {
        return this.value.getRowDimension();
    }

    @Override
    public MatchResult matchesTerm(State state, Value other) throws SetlException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public SetlList eigenValues(State state) throws UndefinedOperationException {
        // TODO check condition
        EigenvalueDecomposition result = this.value.eig();
        // return new SetlMatrix(result.getD()); // TODO right result?
        double[][] values = result.getD().getArray();
        SetlList composition = new SetlList(values.length);
        for(int i = 0; i < values.length; i++) {
            composition.addMember(state, SetlDouble.valueOf(values[i][i]));
        }
        return composition;
    }
    
    // TODO check conditions
    public SetlList singularValueDecomposition(State state) {
        SingularValueDecomposition result = this.value.svd();
        SetlList container = new SetlList(3);
        container.addMember(state, new SetlMatrix(result.getU())); // TODO right format?
        container.addMember(state, new SetlMatrix(result.getS())); // TODO Is this sigma? format?
        container.addMember(state, new SetlMatrix(result.getV())); // TODO right format?
        return container;
    }
    
    // TODO check conditions & TODO vectors
    public SetlMatrix eigenVectors() {
        return new SetlMatrix(this.value.eig().getV());
    }
    
    // TODO are there any conditions
    public SetlDouble determinant() throws UndefinedOperationException {
        return SetlDouble.valueOf(this.value.det());
    }
    
    public SetlMatrix solve(SetlMatrix B) {
        return new SetlMatrix(this.value.solve(B.value));
    }
    
    @Override
    public Value factorial(final State state) throws SetlException {
        return this.transpose();
    }
}