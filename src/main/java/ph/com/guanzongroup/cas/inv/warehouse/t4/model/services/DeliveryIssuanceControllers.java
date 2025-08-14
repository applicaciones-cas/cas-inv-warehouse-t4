package ph.com.guanzongroup.cas.inv.warehouse.t4.model.services;

import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.LogWrapper;
import ph.com.guanzongroup.cas.inv.warehouse.t4.DeliverySchedule;
import ph.com.guanzongroup.cas.inv.warehouse.t4.InventoryRequestApproval;

public class DeliveryIssuanceControllers {

    private GRiderCAS poGRider;
    private LogWrapper poLogWrapper;

    private DeliverySchedule poDeliverySchedule;
    private InventoryRequestApproval poInventoryRequestApproval;

    public DeliveryIssuanceControllers(GRiderCAS applicationDriver, LogWrapper logWrapper) {
        poGRider = applicationDriver;
        poLogWrapper = logWrapper;
    }

    public InventoryRequestApproval InventoryRequestApproval() {
        if (poGRider == null) {
            poLogWrapper.severe("DeliveryIssuanceControllers.InventoryRequestApproval: Application driver is not set.");
            return null;
        }

        if (poInventoryRequestApproval != null) {
            return poInventoryRequestApproval;
        }

        poInventoryRequestApproval = new InventoryRequestApproval();
        poInventoryRequestApproval.setApplicationDriver(poGRider);
        poInventoryRequestApproval.setBranchCode(poGRider.getBranchCode());
        poInventoryRequestApproval.setVerifyEntryNo(true);
        poInventoryRequestApproval.setWithParent(false);
        poInventoryRequestApproval.setLogWrapper(poLogWrapper);
        return poInventoryRequestApproval;
    }

    public DeliverySchedule DeliverySchedule() {
        if (poGRider == null) {
            poLogWrapper.severe("DeliveryIssuanceControllers.DeliverySchedule: Application driver is not set.");
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
