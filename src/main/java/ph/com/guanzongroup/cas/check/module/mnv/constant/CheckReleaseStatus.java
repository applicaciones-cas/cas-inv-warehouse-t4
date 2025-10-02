package ph.com.guanzongroup.cas.check.module.mnv.constant;

import java.util.Arrays;
import java.util.List;

public class CheckReleaseStatus {

    public static final String OPEN = "0";
    public static final String PRINTED = "1";
    public static final String RELEASED = "2";
    public static final List<String> STATUS = Arrays.asList(
            "OPEN",
            "PRINTED",
            "RELEASED"
    );
}
