/*
 * Copyright 2014-2015 Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onlab.packet.ipv6;

import org.onlab.packet.BasePacket;
import org.onlab.packet.Data;
import org.onlab.packet.IPacket;
import org.onlab.packet.IPv6;

import java.nio.ByteBuffer;

/**
 * Implements IPv6 fragment extension header format. (RFC 2460)
 */
public class Fragment extends BasePacket implements IExtensionHeader {
    public static final byte HEADER_LENGTH = 8; // bytes

    protected byte nextHeader;
    protected short fragmentOffset;
    protected byte moreFragment;
    protected int identification;

    @Override
    public byte getNextHeader() {
        return this.nextHeader;
    }

    @Override
    public Fragment setNextHeader(final byte nextHeader) {
        this.nextHeader = nextHeader;
        return this;
    }

    /**
     * Gets the fragment offset of this header.
     *
     * @return fragment offset
     */
    public short getFragmentOffset() {
        return this.fragmentOffset;
    }

    /**
     * Sets the fragment offset of this header.
     *
     * @param fragmentOffset the fragment offset to set
     * @return this
     */
    public Fragment setFragmentOffset(final short fragmentOffset) {
        this.fragmentOffset = fragmentOffset;
        return this;
    }

    /**
     * Gets the more fragment flag of this header.
     *
     * @return more fragment flag
     */
    public byte getMoreFragment() {
        return this.moreFragment;
    }

    /**
     * Sets the more fragment flag of this header.
     *
     * @param moreFragment the more fragment flag to set
     * @return this
     */
    public Fragment setMoreFragment(final byte moreFragment) {
        this.moreFragment = moreFragment;
        return this;
    }

    /**
     * Gets the identification of this header.
     *
     * @return identification
     */
    public int getIdentification() {
        return this.identification;
    }

    /**
     * Sets the identification of this header.
     *
     * @param identification the identification to set
     * @return this
     */
    public Fragment setIdentification(final int identification) {
        this.identification = identification;
        return this;
    }

    @Override
    public byte[] serialize() {
        byte[] payloadData = null;
        if (this.payload != null) {
            this.payload.setParent(this);
            payloadData = this.payload.serialize();
        }

        int payloadLength = 0;
        if (payloadData != null) {
            payloadLength = payloadData.length;
        }

        final byte[] data = new byte[HEADER_LENGTH + payloadLength];
        final ByteBuffer bb = ByteBuffer.wrap(data);

        bb.put(this.nextHeader);
        bb.put((byte) 0);
        bb.putShort((short) (
                (this.fragmentOffset & 0x1fff) << 3 |
                this.moreFragment & 0x1
        ));
        bb.putInt(this.identification);

        if (payloadData != null) {
            bb.put(payloadData);
        }

        if (this.parent != null && this.parent instanceof IExtensionHeader) {
            ((IExtensionHeader) this.parent).setNextHeader(IPv6.PROTOCOL_FRAG);
        }
        return data;
    }

    @Override
    public IPacket deserialize(byte[] data, int offset, int length) {
        final ByteBuffer bb = ByteBuffer.wrap(data, offset, length);
        this.nextHeader = bb.get();
        bb.get();
        short sscratch = bb.getShort();
        this.fragmentOffset = (short) (sscratch >> 3 & 0x1fff);
        this.moreFragment = (byte) (sscratch & 0x1);
        this.identification = bb.getInt();

        IPacket payload;
        if (IPv6.PROTOCOL_CLASS_MAP.containsKey(this.nextHeader)) {
            final Class<? extends IPacket> clazz = IPv6.PROTOCOL_CLASS_MAP
                    .get(this.nextHeader);
            try {
                payload = clazz.newInstance();
            } catch (final Exception e) {
                throw new RuntimeException(
                        "Error parsing payload for Fragment packet", e);
            }
        } else {
            payload = new Data();
        }
        this.payload = payload.deserialize(data, bb.position(),
                bb.limit() - bb.position());
        this.payload.setParent(this);

        return this;
    }

    /*
    * (non-Javadoc)
    *
    * @see java.lang.Object#hashCode()
    */
    @Override
    public int hashCode() {
        final int prime = 5807;
        int result = super.hashCode();
        result = prime * result + this.nextHeader;
        result = prime * result + this.fragmentOffset;
        result = prime * result + this.moreFragment;
        result = prime * result + this.identification;
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof Fragment)) {
            return false;
        }
        final Fragment other = (Fragment) obj;
        if (this.nextHeader != other.nextHeader) {
            return false;
        }
        if (this.fragmentOffset != other.fragmentOffset) {
            return false;
        }
        if (this.moreFragment != other.moreFragment) {
            return false;
        }
        if (this.identification != other.identification) {
            return false;
        }
        return true;
    }
}
