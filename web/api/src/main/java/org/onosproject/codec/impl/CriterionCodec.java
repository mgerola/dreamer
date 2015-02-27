/*
 * Copyright 2015 Open Networking Laboratory
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
package org.onosproject.codec.impl;

import java.util.EnumMap;

import org.onosproject.codec.CodecContext;
import org.onosproject.codec.JsonCodec;
import org.onosproject.net.flow.criteria.Criteria;
import org.onosproject.net.flow.criteria.Criterion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.node.ObjectNode;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Criterion codec.
 */
public final class CriterionCodec extends JsonCodec<Criterion> {

    protected static final Logger log =
            LoggerFactory.getLogger(CriterionCodec.class);

    private final EnumMap<Criterion.Type, CriterionTypeFormatter> formatMap;

    public CriterionCodec() {
        formatMap = new EnumMap<>(Criterion.Type.class);

        formatMap.put(Criterion.Type.IN_PORT, new FormatInPort());
        formatMap.put(Criterion.Type.IN_PHY_PORT, new FormatInPort());
        formatMap.put(Criterion.Type.METADATA, new FormatMetadata());
        formatMap.put(Criterion.Type.ETH_DST, new FormatEth());
        formatMap.put(Criterion.Type.ETH_SRC, new FormatEth());
        formatMap.put(Criterion.Type.ETH_TYPE, new FormatEthType());
        formatMap.put(Criterion.Type.VLAN_VID, new FormatVlanVid());
        formatMap.put(Criterion.Type.VLAN_PCP, new FormatVlanPcp());
        formatMap.put(Criterion.Type.IP_DSCP, new FormatIpDscp());
        formatMap.put(Criterion.Type.IP_ECN, new FormatIpEcn());
        formatMap.put(Criterion.Type.IP_PROTO, new FormatIpProto());
        formatMap.put(Criterion.Type.IPV4_SRC, new FormatIp());
        formatMap.put(Criterion.Type.IPV4_DST, new FormatIp());
        formatMap.put(Criterion.Type.TCP_SRC, new FormatTcp());
        formatMap.put(Criterion.Type.TCP_DST, new FormatTcp());
        formatMap.put(Criterion.Type.UDP_SRC, new FormatUdp());
        formatMap.put(Criterion.Type.UDP_DST, new FormatUdp());
        formatMap.put(Criterion.Type.SCTP_SRC, new FormatSctp());
        formatMap.put(Criterion.Type.SCTP_DST, new FormatSctp());
        formatMap.put(Criterion.Type.ICMPV4_TYPE, new FormatIcmpV4Type());
        formatMap.put(Criterion.Type.ICMPV4_CODE, new FormatIcmpV4Code());
        formatMap.put(Criterion.Type.IPV6_SRC, new FormatIp());
        formatMap.put(Criterion.Type.IPV6_DST, new FormatIp());
        formatMap.put(Criterion.Type.IPV6_FLABEL, new FormatIpV6FLabel());
        formatMap.put(Criterion.Type.ICMPV6_TYPE, new FormatIcmpV6Type());
        formatMap.put(Criterion.Type.ICMPV6_CODE, new FormatIcmpV6Code());
        formatMap.put(Criterion.Type.IPV6_ND_TARGET, new FormatV6NDTarget());
        formatMap.put(Criterion.Type.IPV6_ND_SLL, new FormatV6NDTll());
        formatMap.put(Criterion.Type.IPV6_ND_TLL, new FormatV6NDTll());
        formatMap.put(Criterion.Type.MPLS_LABEL, new FormatMplsLabel());
        formatMap.put(Criterion.Type.IPV6_EXTHDR, new FormatIpV6Exthdr());
        formatMap.put(Criterion.Type.OCH_SIGID, new FormatOchSigId());
        formatMap.put(Criterion.Type.OCH_SIGTYPE, new FormatOchSigType());

        // Currently unimplemented
        formatMap.put(Criterion.Type.ARP_OP, new FormatUnknown());
        formatMap.put(Criterion.Type.ARP_SPA, new FormatUnknown());
        formatMap.put(Criterion.Type.ARP_TPA, new FormatUnknown());
        formatMap.put(Criterion.Type.ARP_SHA, new FormatUnknown());
        formatMap.put(Criterion.Type.ARP_THA, new FormatUnknown());
        formatMap.put(Criterion.Type.MPLS_TC, new FormatUnknown());
        formatMap.put(Criterion.Type.MPLS_BOS, new FormatUnknown());
        formatMap.put(Criterion.Type.PBB_ISID, new FormatUnknown());
        formatMap.put(Criterion.Type.TUNNEL_ID, new FormatUnknown());
        formatMap.put(Criterion.Type.UNASSIGNED_40, new FormatUnknown());
        formatMap.put(Criterion.Type.PBB_UCA, new FormatUnknown());
        formatMap.put(Criterion.Type.TCP_FLAGS, new FormatUnknown());
        formatMap.put(Criterion.Type.ACTSET_OUTPUT, new FormatUnknown());
        formatMap.put(Criterion.Type.PACKET_TYPE, new FormatUnknown());
    }

    private interface CriterionTypeFormatter {
        ObjectNode formatCriterion(ObjectNode root, Criterion criterion);
    }

    private static class FormatUnknown implements CriterionTypeFormatter {
        @Override
        public ObjectNode formatCriterion(ObjectNode root, Criterion criterion) {
            return root;
        }
    }

    private static class FormatInPort implements CriterionTypeFormatter {
        @Override
        public ObjectNode formatCriterion(ObjectNode root, Criterion criterion) {
            final Criteria.PortCriterion portCriterion = (Criteria.PortCriterion) criterion;
            return root.put("port", portCriterion.port().toLong());
        }
    }

    private static class FormatMetadata implements CriterionTypeFormatter {
        @Override
        public ObjectNode formatCriterion(ObjectNode root, Criterion criterion) {
            final Criteria.MetadataCriterion metadataCriterion =
                    (Criteria.MetadataCriterion) criterion;
            return root.put("metadata", metadataCriterion.metadata());
        }
    }

    private static class FormatEth implements CriterionTypeFormatter {
        @Override
        public ObjectNode formatCriterion(ObjectNode root, Criterion criterion) {
            final Criteria.EthCriterion ethCriterion = (Criteria.EthCriterion) criterion;
            return root.put("mac", ethCriterion.mac().toString());
        }
    }

    private static class FormatEthType implements CriterionTypeFormatter {
        @Override
        public ObjectNode formatCriterion(ObjectNode root, Criterion criterion) {
            final Criteria.EthTypeCriterion ethTypeCriterion =
                    (Criteria.EthTypeCriterion) criterion;
            return root.put("ethType", ethTypeCriterion.ethType());
        }
    }

    private static class FormatVlanVid implements CriterionTypeFormatter {
        @Override
        public ObjectNode formatCriterion(ObjectNode root, Criterion criterion) {
            final Criteria.VlanIdCriterion vlanIdCriterion =
                    (Criteria.VlanIdCriterion) criterion;
            return root.put("vlanId", vlanIdCriterion.vlanId().toShort());
        }
    }

    private static class FormatVlanPcp implements CriterionTypeFormatter {
            @Override
            public ObjectNode formatCriterion(ObjectNode root, Criterion criterion) {
                final Criteria.VlanPcpCriterion vlanPcpCriterion =
                        (Criteria.VlanPcpCriterion) criterion;
                return root.put("priority", vlanPcpCriterion.priority());
            }
    }

    private static class FormatIpDscp implements CriterionTypeFormatter {
            @Override
            public ObjectNode formatCriterion(ObjectNode root, Criterion criterion) {
                final Criteria.IPDscpCriterion ipDscpCriterion =
                        (Criteria.IPDscpCriterion) criterion;
                return root.put("ipDscp", ipDscpCriterion.ipDscp());
            }
    }

    private static class FormatIpEcn implements CriterionTypeFormatter {
            @Override
            public ObjectNode formatCriterion(ObjectNode root, Criterion criterion) {
                final Criteria.IPEcnCriterion ipEcnCriterion =
                        (Criteria.IPEcnCriterion) criterion;
                return root.put("ipEcn", ipEcnCriterion.ipEcn());
            }
    }

    private static class FormatIpProto implements CriterionTypeFormatter {
        @Override
        public ObjectNode formatCriterion(ObjectNode root, Criterion criterion) {
            final Criteria.IPProtocolCriterion iPProtocolCriterion =
                    (Criteria.IPProtocolCriterion) criterion;
            return root.put("protocol", iPProtocolCriterion.protocol());
        }
    }

    private static class FormatIp implements CriterionTypeFormatter {
            @Override
            public ObjectNode formatCriterion(ObjectNode root, Criterion criterion) {
                final Criteria.IPCriterion iPCriterion = (Criteria.IPCriterion) criterion;
                return root.put("ip", iPCriterion.ip().toString());
        }
    }

    private static class FormatTcp implements CriterionTypeFormatter {
            @Override
            public ObjectNode formatCriterion(ObjectNode root, Criterion criterion) {
                final Criteria.TcpPortCriterion tcpPortCriterion =
                        (Criteria.TcpPortCriterion) criterion;
                return root.put("tcpPort", tcpPortCriterion.tcpPort());
            }
    }

    private static class FormatUdp implements CriterionTypeFormatter {
            @Override
            public ObjectNode formatCriterion(ObjectNode root, Criterion criterion) {
                final Criteria.UdpPortCriterion udpPortCriterion =
                        (Criteria.UdpPortCriterion) criterion;
                return root.put("udpPort", udpPortCriterion.udpPort());
            }
    }

    private static class FormatSctp implements CriterionTypeFormatter {
        @Override
        public ObjectNode formatCriterion(ObjectNode root, Criterion criterion) {
            final Criteria.SctpPortCriterion sctpPortCriterion =
                    (Criteria.SctpPortCriterion) criterion;
            return root.put("sctpPort", sctpPortCriterion.sctpPort());
        }
    }

    private static class FormatIcmpV4Type implements CriterionTypeFormatter {
        @Override
        public ObjectNode formatCriterion(ObjectNode root, Criterion criterion) {
            final Criteria.IcmpTypeCriterion icmpTypeCriterion =
                    (Criteria.IcmpTypeCriterion) criterion;
            return root.put("icmpType", icmpTypeCriterion.icmpType());
        }
    }

    private static class FormatIcmpV4Code implements CriterionTypeFormatter {
        @Override
        public ObjectNode formatCriterion(ObjectNode root, Criterion criterion) {
            final Criteria.IcmpCodeCriterion icmpCodeCriterion =
                    (Criteria.IcmpCodeCriterion) criterion;
            return root.put("icmpCode", icmpCodeCriterion.icmpCode());
        }
    }

    private static class FormatIpV6FLabel implements CriterionTypeFormatter {
        @Override
        public ObjectNode formatCriterion(ObjectNode root, Criterion criterion) {
            final Criteria.IPv6FlowLabelCriterion ipv6FlowLabelCriterion =
                    (Criteria.IPv6FlowLabelCriterion) criterion;
            return root.put("flowLabel", ipv6FlowLabelCriterion.flowLabel());
        }
    }

    private static class FormatIcmpV6Type implements CriterionTypeFormatter {
        @Override
        public ObjectNode formatCriterion(ObjectNode root, Criterion criterion) {
            final Criteria.Icmpv6TypeCriterion icmpv6TypeCriterion =
                    (Criteria.Icmpv6TypeCriterion) criterion;
            return root.put("icmpv6Type", icmpv6TypeCriterion.icmpv6Type());
        }
    }

    private static class FormatIcmpV6Code implements CriterionTypeFormatter {
        @Override
        public ObjectNode formatCriterion(ObjectNode root, Criterion criterion) {
            final Criteria.Icmpv6CodeCriterion icmpv6CodeCriterion =
                    (Criteria.Icmpv6CodeCriterion) criterion;
            return root.put("icmpv6Code", icmpv6CodeCriterion.icmpv6Code());
        }
    }

    private static class FormatV6NDTarget implements CriterionTypeFormatter {
        @Override
        public ObjectNode formatCriterion(ObjectNode root, Criterion criterion) {
            final Criteria.IPv6NDTargetAddressCriterion ipv6NDTargetAddressCriterion
                = (Criteria.IPv6NDTargetAddressCriterion) criterion;
            return root.put("targetAddress", ipv6NDTargetAddressCriterion.targetAddress().toString());
        }
    }

    private static class FormatV6NDTll implements CriterionTypeFormatter {
        @Override
        public ObjectNode formatCriterion(ObjectNode root, Criterion criterion) {
            final Criteria.IPv6NDLinkLayerAddressCriterion ipv6NDLinkLayerAddressCriterion
                = (Criteria.IPv6NDLinkLayerAddressCriterion) criterion;
            return root.put("mac", ipv6NDLinkLayerAddressCriterion.mac().toString());
        }
    }

    private static class FormatMplsLabel implements CriterionTypeFormatter {
        @Override
        public ObjectNode formatCriterion(ObjectNode root, Criterion criterion) {
            final Criteria.MplsCriterion mplsCriterion =
                    (Criteria.MplsCriterion) criterion;
            return root.put("label", mplsCriterion.label().toInt());
        }
    }

    private static class FormatIpV6Exthdr implements CriterionTypeFormatter {
        @Override
        public ObjectNode formatCriterion(ObjectNode root, Criterion criterion) {
            final Criteria.IPv6ExthdrFlagsCriterion exthdrCriterion =
                    (Criteria.IPv6ExthdrFlagsCriterion) criterion;
            return root.put("exthdrFlags", exthdrCriterion.exthdrFlags());
        }
    }

    private static class FormatOchSigId implements CriterionTypeFormatter {
        @Override
        public ObjectNode formatCriterion(ObjectNode root, Criterion criterion) {
            final Criteria.LambdaCriterion lambdaCriterion =
                    (Criteria.LambdaCriterion) criterion;
            return root.put("lambda", lambdaCriterion.lambda());
        }
    }

    private static class FormatOchSigType implements CriterionTypeFormatter {
        @Override
        public ObjectNode formatCriterion(ObjectNode root, Criterion criterion) {
            final Criteria.OpticalSignalTypeCriterion opticalSignalTypeCriterion =
                    (Criteria.OpticalSignalTypeCriterion) criterion;
            return root.put("signalType", opticalSignalTypeCriterion.signalType());
        }
    }

    @Override
    public ObjectNode encode(Criterion criterion, CodecContext context) {
        checkNotNull(criterion, "Criterion cannot be null");

        final ObjectNode result = context.mapper().createObjectNode()
                .put("type", criterion.type().toString());

        CriterionTypeFormatter formatter =
                checkNotNull(
                        formatMap.get(criterion.type()),
                        "No formatter found for criterion type "
                                + criterion.type().toString());

        return formatter.formatCriterion(result, criterion);
    }
}
