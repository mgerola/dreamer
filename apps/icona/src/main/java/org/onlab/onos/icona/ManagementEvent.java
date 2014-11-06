package org.onlab.onos.icona;

import java.io.Serializable;
import java.util.Date;


public class ManagementEvent implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 2437673865563153537L;

    private String clusterName;
    private MessageType messageType;
    private Date timeStamp;
    private Integer counter;
    private String id;

    enum MessageType {
        HELLO,
    }

    public ManagementEvent() {

    }

    public ManagementEvent(String clusterName,
            MessageType messageType, Integer counter) {
        super();
        this.clusterName = clusterName;
        this.messageType = messageType;
        this.timeStamp = new Date();
        this.counter = counter;
        this.id = clusterName + "-" + counter;
    }


    public String getClusterName() {
        return clusterName;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public Integer getCounter() {
        return counter;
    }

    public String getid() {
        return this.id;
    }
}
