package org.onosproject.icona.utils;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.BitSet;
import org.slf4j.Logger;

public class BitSetIndex {

    private final Logger log = getLogger(getClass());

    private BitSet set;
    private IndexType type;

    public enum IndexType {

        MGMT_CHANNEL(32),
        MPLS_LABEL(0xFFFFF);
        // PSEUDOWIRE_CHANNEL((int) Math.pow(2, 64));

        protected Integer value;

        private IndexType(final Integer value) {
            this.value = value;
        }

        private long getValue() {
            return this.value;
        }
    }

    public BitSetIndex(IndexType type) {
        this.set = new BitSet();
        this.type = type;

        if (type != IndexType.MPLS_LABEL) {
            // Set the first bit to true, in order to start each index from 1
            this.set.flip(0);
        } else {
            this.set.flip(0, 15);
        }
    }

    public synchronized int getNewIndex() {
        Integer index = this.set.nextClearBit(0);
        try {
            this.getNewIndex(index);
        } catch (IndexOutOfBoundsException e) {
            log.error("Could not reserve new index {}: {}", index, e);
            // Will never happen as we obtained the next index through
            // nextClearBit()
        }
        return index;
    }

    public synchronized int getNewIndex(int index) {
        if (index < type.getValue()) {
            if (!this.set.get(index)) {
                this.set.flip(index);
                return index;
            }
        } else {
            throw new IndexOutOfBoundsException("No id available in range [0,"
                    + type.getValue() + "]");
        }
        return index;
    }

    public synchronized boolean releaseIndex(Integer index) {
        if (this.set.get(index)) {
            this.set.flip(index);
            return true;
        } else {
            return false;
        }
    }

    public void reset() {
        this.set.clear();
        this.set.flip(0);
    }
}
