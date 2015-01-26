package org.onosproject.cli.icona;

import static org.onosproject.net.DeviceId.deviceId;
import static org.onosproject.net.PortNumber.portNumber;

import java.util.Optional;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.net.ConnectivityIntentCommand;
import org.onosproject.icona.channel.intra.IntraChannelService;
import org.onosproject.icona.channel.intra.IntraPseudoWireElement.IntentUpdateType;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;

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
        
        intraChannelService.addIntraPseudoWire(src, dst,
                                               IntentUpdateType.INSTALL,
                                               Optional.empty(),
                                               Optional.empty());
        
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
