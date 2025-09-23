package ph.com.guanzongroup.cas.inv.warehouse.t4.model.services;

import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.LogWrapper;
import ph.com.guanzongroup.cas.inv.warehouse.t4.DeliverySchedule;
import ph.com.guanzongroup.cas.inv.warehouse.t4.InventoryRequestApproval;
import ph.com.guanzongroup.cas.inv.warehouse.t4.InventoryStockIssuance;
import ph.com.guanzongroup.cas.inv.warehouse.t4.InventoryStockIssuanceNeo;

public class DeliveryIssuanceControllers {

    private GRiderCAS poGRider;
    private LogWrapper poLogWrapper;

    private DeliverySchedule poDeliverySchedule;
    private InventoryRequestApproval poInventoryRequestApproval;
    private InventoryStockIssuanceNeo poInventoryIssuanceNeo;
    private InventoryStockIssuance poInventoryIssuance;

    public DeliveryIssuanceControllers(GRiderCAS applicationDriver, LogWrapper logWrapper) {
        poGRider = applicationDriver;
        poLogWrapper = logWrapper;
    }

    public InventoryStockIssuanceNeo InventoryStockIssuanceNeo() {
        if (poGRider == null) {
            poLogWrapper.severe("DeliveryIssuanceControllers.InventoryStockIssuanceNeo: Application driver is not set.");
            return null;
        }

        if (poInventoryIssuanceNeo != null) {
            return poInventoryIssuanceNeo;
        }

        poInventoryIssuanceNeo = new InventoryStockIssuanceNeo();
        poInventoryIssuanceNeo.setApplicationDriver(poGRider);
        poInventoryIssuanceNeo.setBranchCode(poGRider.getBranchCode());
        poInventoryIssuanceNeo.setVerifyEntryNo(true);
        poInventoryIssuanceNeo.setWithParent(false);
        poInventoryIssuanceNeo.setLogWrapper(poLogWrapper);
        return poInventoryIssuanceNeo;
    }

    public InventoryStockIssuance InventoryStockIssuance() {
        if (poGRider == null) {
            poLogWrapper.severe("DeliveryIssuanceControllers.InventoryStockIssuance: Application driver is not set.");
            return null;
        }

        if (poInventoryIssuance != null) {
            return poInventoryIssuance;
        }

        poInventoryIssuance = new InventoryStockIssuance();
        poInventoryIssuance.setApplicationDriver(poGRider);
        poInventoryIssuance.setBranchCode(poGRider.getBranchCode());
        poInventoryIssuance.setVerifyEntryNo(true);
        poInventoryIssuance.setWithParent(false);
        poInventoryIssuance.setLogWrapper(poLogWrapper);
        return poInventoryIssuance;
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
