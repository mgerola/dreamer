package org.onlab.onos.icona;

import java.io.Serializable;
import java.nio.ByteBuffer;

public abstract class IconaTopologyElement<T> implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 4676618755423030656L;

    abstract ByteBuffer getIDasByteBuffer();
}
