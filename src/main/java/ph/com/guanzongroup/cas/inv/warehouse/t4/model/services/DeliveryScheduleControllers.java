package ph.com.guanzongroup.cas.inv.warehouse.t4.model.services;

import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.LogWrapper;
import ph.com.guanzongroup.cas.inv.warehouse.t4.DeliverySchedule;

public class DeliveryScheduleControllers {

    private GRiderCAS poGRider;
    private LogWrapper poLogWrapper;

    private DeliverySchedule poDeliverySchedule;

    public DeliveryScheduleControllers(GRiderCAS applicationDriver, LogWrapper logWrapper) {
        poGRider = applicationDriver;
        poLogWrapper = logWrapper;
    }

    public DeliverySchedule DeliverySchedule() {
        if (poGRider == null) {
            poLogWrapper.severe("TabulationControllers.Bingo: Application driver is not set.");
            return null;
        }

        if (poDeliverySchedule != null) {
            return poDeliverySchedule;
        }

        poDeliverySchedule = new DeliverySchedule();
        poDeliverySchedule.setApplicationDriver(poGRider);
        poDeliverySchedule.setBranchCode(poGRider.getBranchCode());
        poDeliverySchedule.setVerifyEntryNo(false);
        poDeliverySchedule.setWithParent(false);
        poDeliverySchedule.setLogWrapper(poLogWrapper);
        return poDeliverySchedule;
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            poDeliverySchedule = null;

            poLogWrapper = null;
            poGRider = null;
        } finally {
            super.finalize();
        }
    }

}
