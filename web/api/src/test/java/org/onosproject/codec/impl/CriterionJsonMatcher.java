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

import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.onosproject.net.flow.criteria.Criteria;
import org.onosproject.net.flow.criteria.Criterion;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Hamcrest matcher for criterion objects.
 */
public final class CriterionJsonMatcher extends
        TypeSafeDiagnosingMatcher<JsonNode> {

    final Criterion criterion;
    Description description;
    JsonNode jsonCriterion;

    /**
     * Constructs a matcher object.
     *
     * @param criterionValue criterion to match
     */
    private CriterionJsonMatcher(Criterion criterionValue) {
        criterion = criterionValue;
    }

    /**
     * Factory to allocate an criterion matcher.
     *
     * @param criterion criterion object we are looking for
     * @return matcher
     */
    public static CriterionJsonMatcher matchesCriterion(Criterion criterion) {
        return new CriterionJsonMatcher(criterion);
    }

    /**
     * Matches a port criterion object.
     *
     * @param criterion criterion to match
     * @return true if the JSON matches the criterion, false otherwise.
     */
    private boolean matchCriterion(Criteria.PortCriterion criterion) {
        final long port = criterion.port().toLong();
        final long jsonPort = jsonCriterion.get("port").asLong();
        if (port != jsonPort) {
            description.appendText("port was " + Long.toString(jsonPort));
            return false;
        }
        return true;
    }

    /**
     * Matches a metadata criterion object.
     *
     * @param criterion criterion to match
     * @return true if the JSON matches the criterion, false otherwise.
     */
    private boolean matchCriterion(Criteria.MetadataCriterion criterion) {
        final long metadata = criterion.metadata();
        final long jsonMetadata = jsonCriterion.get("metadata").asLong();
        if (metadata != jsonMetadata) {
            description.appendText("metadata was "
                    + Long.toString(jsonMetadata));
            return false;
        }
        return true;
    }

    /**
     * Matches an eth criterion object.
     *
     * @param criterion criterion to match
     * @return true if the JSON matches the criterion, false otherwise.
     */
    private boolean matchCriterion(Criteria.EthCriterion criterion) {
        final String mac = criterion.mac().toString();
        final String jsonMac = jsonCriterion.get("mac").textValue();
        if (!mac.equals(jsonMac)) {
            description.appendText("mac was " + jsonMac);
            return false;
        }
        return true;
    }

    /**
     * Matches an eth type criterion object.
     *
     * @param criterion criterion to match
     * @return true if the JSON matches the criterion, false otherwise.
     */
    private boolean matchCriterion(Criteria.EthTypeCriterion criterion) {
        final int ethType = criterion.ethType();
        final int jsonEthType = jsonCriterion.get("ethType").intValue();
        if (ethType != jsonEthType) {
            description.appendText("ethType was "
                    + Integer.toString(jsonEthType));
            return false;
        }
        return true;
    }

    /**
     * Matches a VLAN ID criterion object.
     *
     * @param criterion criterion to match
     * @return true if the JSON matches the criterion, false otherwise.
     */
    private boolean matchCriterion(Criteria.VlanIdCriterion criterion) {
        final short vlanId = criterion.vlanId().toShort();
        final short jsonVlanId = jsonCriterion.get("vlanId").shortValue();
        if (vlanId != jsonVlanId) {
            description.appendText("vlanId was " + Short.toString(jsonVlanId));
            return false;
        }
        return true;
    }

    /**
     * Matches a VLAN PCP criterion object.
     *
     * @param criterion criterion to match
     * @return true if the JSON matches the criterion, false otherwise.
     */
    private boolean matchCriterion(Criteria.VlanPcpCriterion criterion) {
        final byte priority = criterion.priority();
        final byte jsonPriority =
                (byte) jsonCriterion.get("priority").shortValue();
        if (priority != jsonPriority) {
            description.appendText("priority was " + Byte.toString(jsonPriority));
            return false;
        }
        return true;
    }

    /**
     * Matches an IP DSCP criterion object.
     *
     * @param criterion criterion to match
     * @return true if the JSON matches the criterion, false otherwise.
     */
    private boolean matchCriterion(Criteria.IPDscpCriterion criterion) {
        final byte ipDscp = criterion.ipDscp();
        final byte jsonIpDscp = (byte) jsonCriterion.get("ipDscp").shortValue();
        if (ipDscp != jsonIpDscp) {
            description.appendText("IP DSCP was " + Byte.toString(jsonIpDscp));
            return false;
        }
        return true;
    }

    /**
     * Matches an IP ECN criterion object.
     *
     * @param criterion criterion to match
     * @return true if the JSON matches the criterion, false otherwise.
     */
    private boolean matchCriterion(Criteria.IPEcnCriterion criterion) {
        final byte ipEcn = criterion.ipEcn();
        final byte jsonIpEcn = (byte) jsonCriterion.get("ipEcn").shortValue();
        if (ipEcn != jsonIpEcn) {
            description.appendText("IP ECN was " + Byte.toString(jsonIpEcn));
            return false;
        }
        return true;
    }

    /**
     * Matches an IP protocol criterion object.
     *
     * @param criterion criterion to match
     * @return true if the JSON matches the criterion, false otherwise.
     */
    private boolean matchCriterion(Criteria.IPProtocolCriterion criterion) {
        final short protocol = criterion.protocol();
        final short jsonProtocol = jsonCriterion.get("protocol").shortValue();
        if (protocol != jsonProtocol) {
            description.appendText("protocol was "
                    + Short.toString(jsonProtocol));
            return false;
        }
        return true;
    }

    /**
     * Matches an IP address criterion object.
     *
     * @param criterion criterion to match
     * @return true if the JSON matches the criterion, false otherwise.
     */
    private boolean matchCriterion(Criteria.IPCriterion criterion) {
        final String ip = criterion.ip().toString();
        final String jsonIp = jsonCriterion.get("ip").textValue();
        if (!ip.equals(jsonIp)) {
            description.appendText("ip was " + jsonIp);
            return false;
        }
        return true;
    }

    /**
     * Matches a TCP port criterion object.
     *
     * @param criterion criterion to match
     * @return true if the JSON matches the criterion, false otherwise.
     */
    private boolean matchCriterion(Criteria.TcpPortCriterion criterion) {
        final int tcpPort = criterion.tcpPort();
        final int jsonTcpPort = jsonCriterion.get("tcpPort").intValue();
        if (tcpPort != jsonTcpPort) {
            description.appendText("tcp port was "
                    + Integer.toString(jsonTcpPort));
            return false;
        }
        return true;
    }

    /**
     * Matches a UDP port criterion object.
     *
     * @param criterion criterion to match
     * @return true if the JSON matches the criterion, false otherwise.
     */
    private boolean matchCriterion(Criteria.UdpPortCriterion criterion) {
        final int udpPort = criterion.udpPort();
        final int jsonUdpPort = jsonCriterion.get("udpPort").intValue();
        if (udpPort != jsonUdpPort) {
            description.appendText("udp port was "
                    + Integer.toString(jsonUdpPort));
            return false;
        }
        return true;
    }

    /**
     * Matches an SCTP port criterion object.
     *
     * @param criterion criterion to match
     * @return true if the JSON matches the criterion, false otherwise.
     */
    private boolean matchCriterion(Criteria.SctpPortCriterion criterion) {
        final int sctpPort = criterion.sctpPort();
        final int jsonSctpPort = jsonCriterion.get("sctpPort").intValue();
        if (sctpPort != jsonSctpPort) {
            description.appendText("sctp port was "
                    + Integer.toString(jsonSctpPort));
            return false;
        }
        return true;
    }

    /**
     * Matches an ICMP type criterion object.
     *
     * @param criterion criterion to match
     * @return true if the JSON matches the criterion, false otherwise.
     */
    private boolean matchCriterion(Criteria.IcmpTypeCriterion criterion) {
        final short icmpType = criterion.icmpType();
        final short jsonIcmpType = jsonCriterion.get("icmpType").shortValue();
        if (icmpType != jsonIcmpType) {
            description.appendText("icmp type was "
                    + Short.toString(jsonIcmpType));
            return false;
        }
        return true;
    }

    /**
     * Matches an ICMP code criterion object.
     *
     * @param criterion criterion to match
     * @return true if the JSON matches the criterion, false otherwise.
     */
    private boolean matchCriterion(Criteria.IcmpCodeCriterion criterion) {
        final short icmpCode = criterion.icmpCode();
        final short jsonIcmpCode = jsonCriterion.get("icmpCode").shortValue();
        if (icmpCode != jsonIcmpCode) {
            description.appendText("icmp code was "
                    + Short.toString(jsonIcmpCode));
            return false;
        }
        return true;
    }

    /**
     * Matches an IPV6 flow label criterion object.
     *
     * @param criterion criterion to match
     * @return true if the JSON matches the criterion, false otherwise.
     */
    private boolean matchCriterion(Criteria.IPv6FlowLabelCriterion criterion) {
        final int flowLabel = criterion.flowLabel();
        final int jsonFlowLabel = jsonCriterion.get("flowLabel").intValue();
        if (flowLabel != jsonFlowLabel) {
            description.appendText("IPv6 flow label was "
                    + Integer.toString(jsonFlowLabel));
            return false;
        }
        return true;
    }

    /**
     * Matches an ICMP V6 type criterion object.
     *
     * @param criterion criterion to match
     * @return true if the JSON matches the criterion, false otherwise.
     */
    private boolean matchCriterion(Criteria.Icmpv6TypeCriterion criterion) {
        final short icmpv6Type = criterion.icmpv6Type();
        final short jsonIcmpv6Type =
                jsonCriterion.get("icmpv6Type").shortValue();
        if (icmpv6Type != jsonIcmpv6Type) {
            description.appendText("icmpv6 type was "
                    + Short.toString(jsonIcmpv6Type));
            return false;
        }
        return true;
    }

    /**
     * Matches an IPV6 code criterion object.
     *
     * @param criterion criterion to match
     * @return true if the JSON matches the criterion, false otherwise.
     */
    private boolean matchCriterion(Criteria.Icmpv6CodeCriterion criterion) {
        final short icmpv6Code = criterion.icmpv6Code();
        final short jsonIcmpv6Code =
                jsonCriterion.get("icmpv6Code").shortValue();
        if (icmpv6Code != jsonIcmpv6Code) {
            description.appendText("icmpv6 code was "
                    + Short.toString(jsonIcmpv6Code));
            return false;
        }
        return true;
    }

    /**
     * Matches an IPV6 ND target criterion object.
     *
     * @param criterion criterion to match
     * @return true if the JSON matches the criterion, false otherwise.
     */
    private boolean matchCriterion(Criteria.IPv6NDTargetAddressCriterion criterion) {
        final String targetAddress =
                criterion.targetAddress().toString();
        final String jsonTargetAddress =
                jsonCriterion.get("targetAddress").textValue();
        if (!targetAddress.equals(jsonTargetAddress)) {
            description.appendText("target address was " +
                    jsonTargetAddress);
            return false;
        }
        return true;
    }

    /**
     * Matches an IPV6 ND link layer criterion object.
     *
     * @param criterion criterion to match
     * @return true if the JSON matches the criterion, false otherwise.
     */
    private boolean matchCriterion(Criteria.IPv6NDLinkLayerAddressCriterion criterion) {
        final String llAddress =
                criterion.mac().toString();
        final String jsonLlAddress =
                jsonCriterion.get("mac").textValue();
        if (!llAddress.equals(jsonLlAddress)) {
            description.appendText("mac was " + jsonLlAddress);
            return false;
        }
        return true;
    }

    /**
     * Matches an MPLS label criterion object.
     *
     * @param criterion criterion to match
     * @return true if the JSON matches the criterion, false otherwise.
     */
    private boolean matchCriterion(Criteria.MplsCriterion criterion) {
        final int label = criterion.label().toInt();
        final int jsonLabel = jsonCriterion.get("label").intValue();
        if (label != jsonLabel) {
            description.appendText("label was " + Integer.toString(jsonLabel));
            return false;
        }
        return true;
    }

    /**
     * Matches an IPV6 exthdr criterion object.
     *
     * @param criterion criterion to match
     * @return true if the JSON matches the criterion, false otherwise.
     */
    private boolean matchCriterion(Criteria.IPv6ExthdrFlagsCriterion criterion) {
        final int exthdrFlags = criterion.exthdrFlags();
        final int jsonExthdrFlags =
                jsonCriterion.get("exthdrFlags").intValue();
        if (exthdrFlags != jsonExthdrFlags) {
            description.appendText("exthdrFlags was "
                    + Long.toHexString(jsonExthdrFlags));
            return false;
        }
        return true;
    }

    /**
     * Matches a lambda criterion object.
     *
     * @param criterion criterion to match
     * @return true if the JSON matches the criterion, false otherwise.
     */
    private boolean matchCriterion(Criteria.LambdaCriterion criterion) {
        final int lambda = criterion.lambda();
        final int jsonLambda = jsonCriterion.get("lambda").intValue();
        if (lambda != jsonLambda) {
            description.appendText("lambda was " + Integer.toString(lambda));
            return false;
        }
        return true;
    }

    /**
     * Matches an optical signal type criterion object.
     *
     * @param criterion criterion to match
     * @return true if the JSON matches the criterion, false otherwise.
     */
    private boolean matchCriterion(Criteria.OpticalSignalTypeCriterion criterion) {
        final short signalType = criterion.signalType();
        final short jsonSignalType = jsonCriterion.get("signalType").shortValue();
        if (signalType != jsonSignalType) {
            description.appendText("signal type was " + Short.toString(signalType));
            return false;
        }
        return true;
    }

    @Override
    public boolean matchesSafely(JsonNode jsonCriterion,
                                 Description description) {
        this.description = description;
        this.jsonCriterion = jsonCriterion;
        final String type = criterion.type().name();
        final String jsonType = jsonCriterion.get("type").asText();
        if (!type.equals(jsonType)) {
            description.appendText("type was " + type);
            return false;
        }

        switch (criterion.type()) {

            case IN_PORT:
            case IN_PHY_PORT:
                return matchCriterion((Criteria.PortCriterion) criterion);

            case METADATA:
                return matchCriterion((Criteria.MetadataCriterion) criterion);

            case ETH_DST:
            case ETH_SRC:
                return matchCriterion((Criteria.EthCriterion) criterion);

            case ETH_TYPE:
                return matchCriterion((Criteria.EthTypeCriterion) criterion);

            case VLAN_VID:
                return matchCriterion((Criteria.VlanIdCriterion) criterion);

            case VLAN_PCP:
                return matchCriterion((Criteria.VlanPcpCriterion) criterion);

            case IP_DSCP:
                return matchCriterion((Criteria.IPDscpCriterion) criterion);

            case IP_ECN:
                return matchCriterion((Criteria.IPEcnCriterion) criterion);

            case IP_PROTO:
                return matchCriterion((Criteria.IPProtocolCriterion) criterion);

            case IPV4_SRC:
            case IPV4_DST:
            case IPV6_SRC:
            case IPV6_DST:
                return matchCriterion((Criteria.IPCriterion) criterion);

            case TCP_SRC:
            case TCP_DST:
                return matchCriterion((Criteria.TcpPortCriterion) criterion);

            case UDP_SRC:
            case UDP_DST:
                return matchCriterion((Criteria.UdpPortCriterion) criterion);

            case SCTP_SRC:
            case SCTP_DST:
                return matchCriterion((Criteria.SctpPortCriterion) criterion);

            case ICMPV4_TYPE:
                return matchCriterion((Criteria.IcmpTypeCriterion) criterion);

            case ICMPV4_CODE:
                return matchCriterion((Criteria.IcmpCodeCriterion) criterion);

            case IPV6_FLABEL:
                return matchCriterion((Criteria.IPv6FlowLabelCriterion) criterion);

            case ICMPV6_TYPE:
                return matchCriterion((Criteria.Icmpv6TypeCriterion) criterion);

            case ICMPV6_CODE:
                return matchCriterion((Criteria.Icmpv6CodeCriterion) criterion);

            case IPV6_ND_TARGET:
                return matchCriterion(
                        (Criteria.IPv6NDTargetAddressCriterion) criterion);

            case IPV6_ND_SLL:
            case IPV6_ND_TLL:
                return matchCriterion(
                        (Criteria.IPv6NDLinkLayerAddressCriterion) criterion);

            case MPLS_LABEL:
                return matchCriterion((Criteria.MplsCriterion) criterion);

            case IPV6_EXTHDR:
                return matchCriterion(
                        (Criteria.IPv6ExthdrFlagsCriterion) criterion);

            case OCH_SIGID:
                return matchCriterion((Criteria.LambdaCriterion) criterion);

            case OCH_SIGTYPE:
                return matchCriterion(
                        (Criteria.OpticalSignalTypeCriterion) criterion);

            default:
                // Don't know how to format this type
                description.appendText("unknown criterion type " +
                        criterion.type());
                return false;
        }
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(criterion.toString());
    }
}
