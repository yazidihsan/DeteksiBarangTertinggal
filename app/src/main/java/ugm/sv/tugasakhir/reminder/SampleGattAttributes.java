package ugm.sv.tugasakhir.reminder;

import java.util.HashMap;

public class SampleGattAttributes {
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public static String BLE_CONF = "0000ffe0-0000-1000-8000-00805f9b34fb";
    public static String BLE_RX_TX = "0000ffe1-0000-1000-8000-00805f9b34fb";
    private static HashMap<String, String> attributes = new HashMap();

    static {
        attributes.put("0000ffe0-0000-1000-8000-00805f9b34fb", "BLE Serial");
        attributes.put("00001800-0000-1000-8000-00805f9b34fb", "Device Information Service");
        attributes.put(BLE_RX_TX, "RX/TX data");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = (String) attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
