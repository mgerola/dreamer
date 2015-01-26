package org.onosproject.icona.channel.intra;

import java.io.Serializable;
import java.nio.ByteBuffer;

public abstract class IconaIntraElement<T> implements Serializable {


    /**
     *
     */
    private static final long serialVersionUID = 4780191437036875021L;

    abstract ByteBuffer getIDasByteBuffer();
}
