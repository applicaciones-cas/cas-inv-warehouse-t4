package ph.com.guanzongroup.cas.inv.warehouse.t4.constant;

import java.util.Arrays;
import java.util.List;

public class DeliveryIssuanceType {

    public static final String PICK_UP = "0";
    public static final String DELIVERY = "1";
    public static final String DROP_OFF = "2";
    public static final List<String> DeliveryType = Arrays.asList(
            "PICK-UP",
            "DELIVERY",
            "DROP-OFF"
    );
}
