package ph.com.guanzongroup.cas.check.module.mnv.constant;

import java.util.Arrays;
import java.util.List;

public class CheckReleaseStatus {

    public static final String OPEN = "0";
    public static final String PRINTED = "1";
    public static final String CONFIRMED = "2";
    public static final String POSTED = "3";
    public static final String CANCELLED = "4";
    public static final String VOID = "5";
    public static final List<String> STATUS = Arrays.asList(
            "OPEN",
            "PRINTED",
            "CONFIRMED",
            "POSTED", 
            "CANCELLED", 
            "VOID"
    );
}
