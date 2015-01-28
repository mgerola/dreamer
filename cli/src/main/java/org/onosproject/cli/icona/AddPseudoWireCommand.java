package org.onosproject.cli.icona;

import static org.onosproject.net.DeviceId.deviceId;
import static org.onosproject.net.PortNumber.portNumber;

import java.util.List;
import java.util.Optional;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.onosproject.cli.net.ConnectivityIntentCommand;
import org.onosproject.icona.channel.intra.IntraChannelService;
import org.onosproject.icona.channel.intra.IntraPseudoWireElement.IntentUpdateType;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.intent.Constraint;

/**
 * Installs point-to-point connectivity intents.
 */
@Command(scope = "icona", name = "add-pseudowire-intent",
         description = "Installs PseudoWire intent between two End-Points")


public class AddPseudoWireCommand extends ConnectivityIntentCommand {

  @Argument(index = 0, name = "ingressDevices",
            description = "Ingress Device/Port End-Point",
            required = true, multiValued = false)
  String ingressString = null;
  
  @Argument(index = 1, name = "egressDevices",
          description = "Egress Device/Port End-Point",
          required = true, multiValued = false)
  String egressString = null;
  
  
    @Override
    protected void execute() {
        
        IntraChannelService intraChannelService = get(IntraChannelService.class);
        
        DeviceId ingressDeviceId = deviceId(getDeviceId(ingressString));
        PortNumber ingressPortNumber = portNumber(getPortNumber(ingressString));
        ConnectPoint src = new ConnectPoint(ingressDeviceId, ingressPortNumber);
        
        DeviceId egressDeviceId = deviceId(getDeviceId(egressString));
        PortNumber egressPortNumber = portNumber(getPortNumber(egressString));
        ConnectPoint dst = new ConnectPoint(egressDeviceId, egressPortNumber);
        
        
        TrafficSelector selector = buildTrafficSelector();
        TrafficTreatment treatment = buildTrafficTreatment();

        //TODO: to be managed....
        List<Constraint> constraints = buildConstraints();
        
        intraChannelService.addIntraPseudoWire(src, dst, selector, treatment,
                                               IntentUpdateType.INSTALL);
        
    }
    
    /**
     * Extracts the port number portion of the ConnectPoint.
     *
     * @param deviceString string representing the device/port
     * @return port number as a string, empty string if the port is not found
     */
    public static String getPortNumber(String deviceString) {
        int slash = deviceString.indexOf('/');
        if (slash <= 0) {
            return "";
        }
        return deviceString.substring(slash + 1, deviceString.length());
    }

    /**
     * Extracts the device ID portion of the ConnectPoint.
     *
     * @param deviceString string representing the device/port
     * @return device ID string
     */
    public static String getDeviceId(String deviceString) {
        int slash = deviceString.indexOf('/');
        if (slash <= 0) {
            return "";
        }
        return deviceString.substring(0, slash);
    }

}
