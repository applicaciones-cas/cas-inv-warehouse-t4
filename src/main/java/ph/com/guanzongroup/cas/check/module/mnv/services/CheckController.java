package ph.com.guanzongroup.cas.check.module.mnv.services;

import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.LogWrapper;
import ph.com.guanzongroup.cas.check.module.mnv.CheckTransfer;
public class CheckController {

    private GRiderCAS poGRider;
    private LogWrapper poLogWrapper;

    private CheckTransfer poCheckTransfer;

    public CheckController(GRiderCAS applicationDriver, LogWrapper logWrapper) {
        poGRider = applicationDriver;
        poLogWrapper = logWrapper;
    }

    public CheckTransfer CheckTransfer() {
        if (poGRider == null) {
            poLogWrapper.severe("CheckController.CheckTransfer: Application driver is not set.");
            return null;
        }

        if (poCheckTransfer != null) {
            return poCheckTransfer;
        }

        poCheckTransfer = new CheckTransfer();
        poCheckTransfer.setApplicationDriver(poGRider);
        poCheckTransfer.setBranchCode(poGRider.getBranchCode());
        poCheckTransfer.setVerifyEntryNo(true);
        poCheckTransfer.setWithParent(false);
        poCheckTransfer.setLogWrapper(poLogWrapper);
        return poCheckTransfer;
    }


    @Override
    protected void finalize() throws Throwable {
        try {
            poCheckTransfer = null;

            poLogWrapper = null;
            poGRider = null;
        } finally {
            super.finalize();
        }
    }

}
