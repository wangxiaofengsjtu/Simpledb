package simpledb;

import static org.junit.Assert.assertNotNull;

import java.io.Serializable;
import java.util.*;

/**
 * TupleDesc describes the schema of a tuple.
 */
public class TupleDesc implements Serializable {

    /**
     * A help class to facilitate organizing the information of each field
     * */
	private final TDItem[] tdItems;
	
    public static class TDItem implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * The type of the field
         * */
        public final Type fieldType;
        
        /**
         * The name of the field
         * */
        public final String fieldName;

        public TDItem(Type t, String n) {
            this.fieldName = n;
            this.fieldType = t;
        }

        public String toString() {
            return fieldName + "(" + fieldType + ")";
        }
    }
    

    /**
     * @return
     *        An iterator which iterates over all the field TDItems
     *        that are included in this TupleDesc
     * */
    public Iterator<TDItem> iterator() {
        // some code goes here
    	List<TDItem> itemlList=Arrays.asList(tdItems);
        return itemlList.iterator();
    }

    private static final long serialVersionUID = 1L;

    /**
     * Create a new TupleDesc with typeAr.length fields with fields of the
     * specified types, with associated named fields.
     * 
     * @param typeAr
     *            array specifying the number of and types of fields in this
     *            TupleDesc. It must contain at least one entry.
     * @param fieldAr
     *            array specifying the names of the fields. Note that names may
     *            be null.
     */
    public TupleDesc(Type[] typeAr, String[] fieldAr) {
        // some code goes here
    	tdItems=new TDItem[typeAr.length];
    	for(int i=0;i<typeAr.length;++i)
    		tdItems[i]=new TDItem(typeAr[i], fieldAr[i]);
    }

    /**
     * Constructor. Create a new tuple desc with typeAr.length fields with
     * fields of the specified types, with anonymous (unnamed) fields.
     * 
     * @param typeAr
     *            array specifying the number of and types of fields in this
     *            TupleDesc. It must contain at least one entry.
     */
    public TupleDesc(Type[] typeAr) {
        // some code goes here
    	tdItems=new TDItem[typeAr.length];
    	for(int i=0;i<typeAr.length;++i)
    		tdItems[i]=new TDItem(typeAr[i],"");
    }

    /**
     * @return the number of fields in this TupleDesc
     */
    public int numFields() {
        // some code goes here
        return tdItems.length;
    }

    /**
     * Gets the (possibly null) field name of the ith field of this TupleDesc.
     * 
     * @param i
     *            index of the field name to return. It must be a valid index.
     * @return the name of the ith field
     * @throws NoSuchElementException
     *             if i is not a valid field reference.
     */
    public String getFieldName(int i) throws NoSuchElementException {
        // some code goes here
        if(i<0||i>=tdItems.length)
        	throw new NoSuchElementException("pos " + i + " is not a valid index");
        else 
        	return tdItems[i].fieldName;
    }

    /**
     * Gets the type of the ith field of this TupleDesc.
     * 
     * @param i
     *            The index of the field to get the type of. It must be a valid
     *            index.
     * @return the type of the ith field
     * @throws NoSuchElementException
     *             if i is not a valid field reference.
     */
    public Type getFieldType(int i) throws NoSuchElementException {
        // some code goes here
    	if(i<0||i>=tdItems.length)
        	throw new NoSuchElementException("pos " + i + " is not a valid index");
        else 
        	return tdItems[i].fieldType;
    }

    /**
     * Find the index of the field with a given name.
     * 
     * @param name
     *            name of the field.
     * @return the index of the field that is first to have the given name.
     * @throws NoSuchElementException
     *             if no field with a matching name is found.
     */
    public int fieldNameToIndex(String name) throws NoSuchElementException {
        // some code goes here
        for(int i=0;i<tdItems.length;++i) {
        	if(tdItems[i].fieldName.equals(name))
        		return i;
        }
        throw new NoSuchElementException(name +" is not a field name");
    }

    /**
     * @return The size (in bytes) of tuples corresponding to this TupleDesc.
     *         Note that tuples from a given TupleDesc are of a fixed size.
     */
    public int getSize() {
        // some code goes here
        int totalSize=0;
    	for(int i=0;i<tdItems.length;++i) {
        	totalSize+=tdItems[i].fieldType.getLen();
        }
    	return totalSize;
    }

    /**
     * Merge two TupleDescs into one, with td1.numFields + td2.numFields fields,
     * with the first td1.numFields coming from td1 and the remaining from td2.
     * 
     * @param td1
     *            The TupleDesc with the first fields of the new TupleDesc
     * @param td2
     *            The TupleDesc with the last fields of the TupleDesc
     * @return the new TupleDesc
     */
    public static TupleDesc merge(TupleDesc td1, TupleDesc td2) {
        // some code goes here
    	int numField1=td1.numFields(),numField2=td2.numFields();
    	Type[] mergeItems_type=new Type[numField1+numField2];
    	String[] mergeItems_ar=new String[numField1+numField2];

    	for(int i=0;i<numField1;++i) {
    		mergeItems_type[i]=td1.tdItems[i].fieldType;
    		mergeItems_ar[i]=td1.tdItems[i].fieldName;
    	}
    	for(int i=0;i<numField2;++i) {
    		mergeItems_type[i+numField1]=td2.tdItems[i].fieldType;
    		mergeItems_ar[i+numField1]=td2.tdItems[i].fieldName;
    	}
        return new TupleDesc(mergeItems_type,mergeItems_ar);
    }

    /**
     * Compares the specified object with this TupleDesc for equality. Two
     * TupleDescs are considered equal if they are the same size and if the n-th
     * type in this TupleDesc is equal to the n-th type in td.
     * 
     * @param o
     *            the Object to be compared for equality with this TupleDesc.
     * @return true if the object is equal to this TupleDesc.
     */
    public boolean equals(Object o) {
        // some code goes here
        /*if(this==o)
        	return true;
        if(!(o instanceof TupleDesc)) {
			return false;
		}
        TupleDesc other=(TupleDesc) o;
        if(numFields()!=other.numFields())
        	return false;
        for(int i=0;i<numFields();) {
        	if(!tdItems[i].fieldType.equals(other.tdItems[i].fieldType));
        	return false;
        }
        return true;
        */
        if (o instanceof TupleDesc) {
			TupleDesc re = (TupleDesc) o;
			if(numFields()!=re.numFields())
				return false;
			for(int i=0;i<numFields();++i) {
				if(!tdItems[i].fieldType.equals(re.tdItems[i].fieldType))
					return false;
			}
			return true;
		}
        return false;
    }

    public int hashCode() {
        // If you want to use TupleDesc as keys for HashMap, implement this so
        // that equal objects have equals hashCode() results
        throw new UnsupportedOperationException("unimplemented");
    }

    /**
     * Returns a String describing this descriptor. It should be of the form
     * "fieldType[0](fieldName[0]), ..., fieldType[M](fieldName[M])", although
     * the exact format does not matter.
     * 
     * @return String describing this descriptor.
     */
    public String toString() {
        // some code goes here
        StringBuilder res=new StringBuilder();
        for(int i=0;i<tdItems.length-1;++i)
        	res.append(tdItems[i].fieldType+"("+tdItems[i].fieldName+"),");
        res.append(tdItems[tdItems.length-1].fieldType+"("+tdItems[tdItems.length-1].fieldName+")");
        return res.toString();
    }
}
