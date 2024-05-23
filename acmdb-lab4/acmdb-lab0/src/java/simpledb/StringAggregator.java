package simpledb;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Knows how to compute some aggregate over a set of StringFields.
 */
public class StringAggregator implements Aggregator {

    private static final long serialVersionUID = 1L;
    private static final IntField NO_GROUP = new IntField(-1);
    private int gbfield;
    private Type gbfieldtype;
    private int afield;
    private Op what;
    private Map<Field, Tuple> tupleMap;
    private TupleDesc desc;
    /**
     * Aggregate constructor
     * @param gbfield the 0-based index of the group-by field in the tuple, or NO_GROUPING if there is no grouping
     * @param gbfieldtype the type of the group by field (e.g., Type.INT_TYPE), or null if there is no grouping
     * @param afield the 0-based index of the aggregate field in the tuple
     * @param what aggregation operator to use -- only supports COUNT
     * @throws IllegalArgumentException if what != COUNT
     */

    public StringAggregator(int gbfield, Type gbfieldtype, int afield, Op what) {
        // some code goes here
    	if (!what.equals(Op.COUNT)) {
            throw new IllegalArgumentException();
        }
        this.gbfield = gbfield;
        this.gbfieldtype = gbfieldtype;
        this.afield = afield;
        this.what = what;
        this.tupleMap = new ConcurrentHashMap<>();
        if (gbfield == NO_GROUPING) {
            this.desc = new TupleDesc(new Type[] {Type.INT_TYPE}, new String[] {"aggregateValue"});
            Tuple tuple = new Tuple(desc);
            tuple.setField(0, new IntField(0));
            this.tupleMap.put(NO_GROUP, tuple);
        } else {
            this.desc = new TupleDesc(new Type[] {gbfieldtype, Type.INT_TYPE}, new String[] {"groupValue", "aggregateValue"});
        }
    }

    /**
     * Merge a new tuple into the aggregate, grouping as indicated in the constructor
     * @param tup the Tuple containing an aggregate field and a group-by field
     */
    public void mergeTupleIntoGroup(Tuple tup) {
        // some code goes here
    	if (this.gbfield == NO_GROUPING) {
            Tuple tuple = tupleMap.get(NO_GROUP);
            IntField field = (IntField) tuple.getField(0);
            tuple.setField(0, new IntField(field.getValue() + 1));
            tupleMap.put(NO_GROUP, tuple);
        } else {
            Field field = tup.getField(gbfield);
            if (!tupleMap.containsKey(field)) {
                Tuple tuple = new Tuple(this.desc);
                tuple.setField(0, field);
                tuple.setField(1, new IntField(1));
                tupleMap.put(field, tuple);
            } else {
                Tuple tuple = tupleMap.get(field);
                IntField intField = (IntField) tuple.getField(1);
                tuple.setField(1, new IntField(intField.getValue() + 1));
                tupleMap.put(field, tuple);
            }
        }
    }
    
    public TupleDesc getTupleDesc() {
        return desc;
    }


    /**
     * Create a DbIterator over group aggregate results.
     *
     * @return a DbIterator whose tuples are the pair (groupVal,
     *   aggregateVal) if using group, or a single (aggregateVal) if no
     *   grouping. The aggregateVal is determined by the type of
     *   aggregate specified in the constructor.
     */
    public DbIterator iterator() {
        // some code goes here
    	return new StringIterator(this);
    }
    
    public class StringIterator implements DbIterator {
        private StringAggregator aggregator;
        private Iterator<Tuple> iterator;

        public StringIterator(StringAggregator aggregator) {
            this.aggregator = aggregator;
            this.iterator = null;
        }

        @Override
        public void open() throws DbException, TransactionAbortedException {
            this.iterator = aggregator.tupleMap.values().iterator();
        }

        @Override
        public boolean hasNext() throws DbException, TransactionAbortedException {
            return iterator.hasNext();
        }

        @Override
        public Tuple next() throws DbException, TransactionAbortedException, NoSuchElementException {
            return iterator.next();
        }

        @Override
        public void rewind() throws DbException, TransactionAbortedException {
            iterator = aggregator.tupleMap.values().iterator();
        }

        @Override
        public TupleDesc getTupleDesc() {
            return aggregator.desc;
        }

        @Override
        public void close() {
            iterator = null;
        }
    }

}
