package com.example.yue.audiorecord;

import android.util.Log;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.NtpV3Packet;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.commons.net.ntp.TimeStamp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Created by qingf on 2017/2/15.
 */

public class Ntp {
    private static final String TAG = "Mqtt";
    private static TimeInfo info = null;
    public static void test() {
        NtpV3Packet message = info.getMessage();
        int stratum = message.getStratum();
        String refType;
        if (stratum <= 0) {
            refType = "(Unspecified or Unavailable)";
        } else if (stratum == 1) {
            refType = "(Primary Reference; e.g., GPS)"; // GPS, radio
            // clock, etc.
        } else {
            refType = "(Secondary Reference; e.g. via NTP or SNTP)";
        }

        TimeStamp refNtpTime = message.getReferenceTimeStamp();
        Log.d(TAG, "test: refNtpTime: "+refNtpTime.toDateString()+" "+String.valueOf(refNtpTime.getTime()));
        // Originate Time is time request sent by client (t1)
        TimeStamp origNtpTime = message.getOriginateTimeStamp();
        Log.d(TAG, "test: origNtpTime: "+ origNtpTime.toDateString()+" "+String.valueOf(origNtpTime.getTime()));

        long destTime = info.getReturnTime();
        // Receive Time is time request received by server (t2)
        TimeStamp rcvNtpTime = message.getReceiveTimeStamp();
        Log.d(TAG, "test: rcvNtpTime: "+ rcvNtpTime.toDateString()+" "+String.valueOf(rcvNtpTime.getTime()));

        info.computeDetails(); // compute offset/delay if not already done

        // Transmit time is time reply sent by server (t3)
        TimeStamp xmitNtpTime = message.getTransmitTimeStamp();
        Log.d(TAG, "test: xmitNtpTime "+ xmitNtpTime.toDateString()+" "+String.valueOf(xmitNtpTime.getTime()));

        // Destination time is time reply received by client (t4)
        TimeStamp destNtpTime = TimeStamp.getNtpTime(destTime);
        Log.d(TAG, "test: destNtpTime: "+destNtpTime.toDateString()+" "+String.valueOf(destNtpTime.getTime()));

        Long offsetValue = info.getOffset();
        Long delayValue = info.getDelay();
        String delay = (delayValue == null) ? "N/A" : delayValue.toString();
        String offset = (offsetValue == null) ? "N/A" : offsetValue.toString();
    }

    public static long query(String ntpServerHostname) throws IOException, SocketException {
        NTPUDPClient client = new NTPUDPClient();
        // We want to timeout if a response takes longer than 10 seconds
        client.setDefaultTimeout(10000);
        try {
            client.open();

            InetAddress hostAddr = InetAddress.getByName(ntpServerHostname);
            Log.d(TAG, "Trying to get time from " + hostAddr.getHostName() + "/"
                    + hostAddr.getHostAddress());

            info = client.getTime(hostAddr);
        } finally {
            client.close();
        }

        // compute offset/delay if not already do
        // ne
        info.computeDetails();

        return info.getOffset();
    }
}
