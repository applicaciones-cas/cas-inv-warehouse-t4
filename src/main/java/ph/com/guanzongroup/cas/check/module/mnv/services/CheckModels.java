package ph.com.guanzongroup.cas.check.module.mnv.services;

import ph.com.guanzongroup.cas.inv.warehouse.t4.parameter.services.*;
import org.guanzon.appdriver.base.GRiderCAS;
import ph.com.guanzongroup.cas.check.module.mnv.models.Model_Check_Transfer_Detail;
import ph.com.guanzongroup.cas.check.module.mnv.models.Model_Check_Transfer_Master;
import ph.com.guanzongroup.cas.inv.warehouse.t4.parameter.model.Model_Branch_Area;
import ph.com.guanzongroup.cas.inv.warehouse.t4.parameter.model.Model_Branch_Cluster;
import ph.com.guanzongroup.cas.inv.warehouse.t4.parameter.model.Model_Branch_Cluster_Delivery;
import ph.com.guanzongroup.cas.inv.warehouse.t4.parameter.model.Model_Branch_Others;
import ph.com.guanzongroup.cas.inv.warehouse.t4.parameter.model.Model_Inventory_Supplier;

/**
 *
 * @author 12mnv
 */
public class CheckModels {

    private Model_Check_Transfer_Master poCheckTransferMaster;
    private Model_Check_Transfer_Detail poCheckTransferDetail;

    private final GRiderCAS poGRider;

    public CheckModels(GRiderCAS applicationDriver) {
        this.poGRider = applicationDriver;
    }

    public Model_Check_Transfer_Master CheckTransferMaster() {
        if (this.poGRider == null) {
            System.err.println("CheckModels.CheckTransferMaster: Application driver is not set.");
            return null;
        }
        if (this.poCheckTransferMaster == null) {
            this.poCheckTransferMaster = new Model_Check_Transfer_Master();
            this.poCheckTransferMaster.setApplicationDriver(this.poGRider);
            this.poCheckTransferMaster.setXML("Model_Check_Transfer_Master");
            this.poCheckTransferMaster.setTableName("Check_Transfer_Master");
            this.poCheckTransferMaster.initialize();
        }
        return this.poCheckTransferMaster;
    }

    public Model_Check_Transfer_Detail CheckTransferDeetail() {
        if (this.poGRider == null) {
            System.err.println("CheckModels.CheckTransferDeetail: Application driver is not set.");
            return null;
        }
        if (this.poCheckTransferDetail == null) {
            this.poCheckTransferDetail = new Model_Check_Transfer_Detail();
            this.poCheckTransferDetail.setApplicationDriver(this.poGRider);
            this.poCheckTransferDetail.setXML("Model_Check_Transfer_Detail");
            this.poCheckTransferDetail.setTableName("Check_Transfer_Detail");
            this.poCheckTransferDetail.initialize();
        }
        return this.poCheckTransferDetail;
    }
}
