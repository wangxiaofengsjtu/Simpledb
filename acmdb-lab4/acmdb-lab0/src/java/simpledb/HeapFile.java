package simpledb;

import java.io.*;
import java.util.*;

/**
 * HeapFile is an implementation of a DbFile that stores a collection of tuples
 * in no particular order. Tuples are stored on pages, each of which is a fixed
 * size, and the file is simply a collection of those pages. HeapFile works
 * closely with HeapPage. The format of HeapPages is described in the HeapPage
 * constructor.
 * 
 * @see simpledb.HeapPage#HeapPage
 * @author Sam Madden
 */
public class HeapFile implements DbFile {
	
	private final File file;
    private final TupleDesc tupleDesc;

    private final BufferPool bufferPool;
    /**
     * Constructs a heap file backed by the specified file.
     * 
     * @param f
     *            the file that stores the on-disk backing store for this heap
     *            file.
     */
	
    public HeapFile(File f, TupleDesc td) {
        // some code goes here
    	this.file = f;
        this.tupleDesc = td;
        this.bufferPool = Database.getBufferPool();
    }

    /**
     * Returns the File backing this HeapFile on disk.
     * 
     * @return the File backing this HeapFile on disk.
     */
    public File getFile() {
        // some code goes here
        return file;
    }

    /**
     * Returns an ID uniquely identifying this HeapFile. Implementation note:
     * you will need to generate this tableid somewhere ensure that each
     * HeapFile has a "unique id," and that you always return the same value for
     * a particular HeapFile. We suggest hashing the absolute file name of the
     * file underlying the heapfile, i.e. f.getAbsoluteFile().hashCode().
     * 
     * @return an ID uniquely identifying this HeapFile.
     */
    public int getId() {
        // some code goes here
        return file.getAbsoluteFile().hashCode();
    }

    /**
     * Returns the TupleDesc of the table stored in this DbFile.
     * 
     * @return TupleDesc of this DbFile.
     */
    public TupleDesc getTupleDesc() {
        // some code goes here
        return this.tupleDesc;
    }

    // see DbFile.java for javadocs
    public Page readPage(PageId pid) {
        // some code goes here
    	try {
    		RandomAccessFile randomAccessFile=new RandomAccessFile(file, "r");
    		
    		int pgno=pid.pageNumber();
    		//int pageSize=Database.getBufferPool().getPageSize();
    		int pageSize=BufferPool.getPageSize();
    		int offset=pgno*pageSize;
    		byte[] buffer=new byte[pageSize];
    		
    		randomAccessFile.seek(offset);
    		randomAccessFile.read(buffer);
    		HeapPage heapPage=new HeapPage(new HeapPageId(pid.getTableId(), pgno), buffer);
    		return heapPage;
    	}
    	catch(IOException e) {
    		e.printStackTrace();
    	}
    	return null;
        
    }

    // see DbFile.java for javadocs
    public void writePage(Page page) throws IOException {
        // some code goes here
        // not necessary for lab1
    }

    /**
     * Returns the number of pages in this HeapFile.
     */
    public int numPages() {
        // some code goes here
        return (int)Math.floor(file.length()*1.0/BufferPool.getPageSize());
    }

    // see DbFile.java for javadocs
    public ArrayList<Page> insertTuple(TransactionId tid, Tuple t)
            throws DbException, IOException, TransactionAbortedException {
        // some code goes here
    	List<Page> modified = new ArrayList<>();
        for (int i = 0; i < numPages(); i++) {
            HeapPage page = (HeapPage) bufferPool.getPage(tid, new HeapPageId(this.getId(), i), Permissions.READ_WRITE);
            if (page.getNumEmptySlots() == 0) {
                bufferPool.releasePage(tid, page.getId());//注意更改
                continue;
            }
            page.insertTuple(t);
            modified.add(page);
            return (ArrayList<Page>) modified;
        }
        // 当所有的页都满时,我们需要创建新的页并写入文件中
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file, true));
        byte[] emptyPageData = HeapPage.createEmptyPageData();
        // 向文件末尾添加数据
        outputStream.write(emptyPageData);
        outputStream.close();
        // 加载到缓存中,使用numPages() - 1是因为此时numPages()已经变为插入后的大小了
        HeapPage page = (HeapPage) bufferPool.getPage(tid, new HeapPageId(getId(), numPages() - 1), Permissions.READ_WRITE);
        page.insertTuple(t);
        modified.add(page);
        return (ArrayList<Page>) modified;
        // not necessary for lab1
    }

    // see DbFile.java for javadocs
    public ArrayList<Page> deleteTuple(TransactionId tid, Tuple t) throws DbException,
            TransactionAbortedException {
        // some code goes here
    	HeapPage page = (HeapPage) bufferPool.getPage(tid, t.getRecordId().getPageId(), Permissions.READ_WRITE);
        page.deleteTuple(t);
        ArrayList<Page> modified = new ArrayList<>();
        modified.add(page);
        return modified;
        // not necessary for lab1
    }

    // see DbFile.java for javadocs
    public DbFileIterator iterator(TransactionId tid) {
        // some code goes here
    	return new HeapFileIterator(this, tid);
    }
    
    public static final class HeapFileIterator implements DbFileIterator {

        private final HeapFile heapFile;
        private final TransactionId tid;
        private Iterator<Tuple> iterator;
        private int pageNumber;

        public HeapFileIterator(HeapFile file, TransactionId tid) {
            this.heapFile = file;
            this.tid = tid;
        }

        @Override
        public void open() throws DbException, TransactionAbortedException {
            this.pageNumber = 0;
            this.iterator = getPageTuples(pageNumber);
        }

        private Iterator<Tuple> getPageTuples(int pageNo) throws TransactionAbortedException, DbException {
            if (pageNo >= 0 && pageNo < heapFile.numPages()) {
                HeapPageId heapPageId = new HeapPageId(heapFile.getId(), pageNo);
                HeapPage page = (HeapPage) Database.getBufferPool().getPage(tid, heapPageId, Permissions.READ_ONLY);
                return page.iterator();
            } else {
                throw new DbException(String.format("heapfile %d does not contain page %d", pageNo, heapFile.getId()));
            }
        }

        @Override
        public boolean hasNext() throws DbException, TransactionAbortedException {
            if (iterator == null) {
                return false;
            }
            while (iterator != null && !iterator.hasNext()) {
                if (pageNumber < (heapFile.numPages() - 1)) {
                    pageNumber++;
                    iterator = getPageTuples(pageNumber);
                } else {
                    iterator = null;
                }
            }
            if (iterator == null) {
                return false;
            }
            return iterator.hasNext();
        }

        @Override
        public Tuple next() throws DbException, TransactionAbortedException, NoSuchElementException {
            if (iterator == null || !iterator.hasNext()) {
                throw new NoSuchElementException();
            }
            return iterator.next();
        }

        @Override
        public void rewind() throws DbException, TransactionAbortedException {
            close();
            open();
        }

        @Override
        public void close() {
            iterator = null;
        }
    }
}

