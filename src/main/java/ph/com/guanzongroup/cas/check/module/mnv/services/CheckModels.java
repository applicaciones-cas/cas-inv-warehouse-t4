package ph.com.guanzongroup.cas.check.module.mnv.services;

import org.guanzon.appdriver.base.GRiderCAS;
import ph.com.guanzongroup.cas.check.module.mnv.models.Model_Check_Deposit_Detail;
import ph.com.guanzongroup.cas.check.module.mnv.models.Model_Check_Deposit_Master;
import ph.com.guanzongroup.cas.check.module.mnv.models.Model_Check_Transfer_Detail;
import ph.com.guanzongroup.cas.check.module.mnv.models.Model_Check_Transfer_Master;

/**
 *
 * @author 12mnv
 */
public class CheckModels {

    private Model_Check_Transfer_Master poCheckTransferMaster;
    private Model_Check_Transfer_Detail poCheckTransferDetail;

    private Model_Check_Deposit_Master poCheckDepositMaster;
    private Model_Check_Deposit_Detail poCheckDepositDetail;

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

    public Model_Check_Transfer_Detail CheckTransferDetail() {
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

    public Model_Check_Deposit_Master CheckDepositMaster() {
        if (this.poGRider == null) {
            System.err.println("CheckModels.CheckDepositMaster: Application driver is not set.");
            return null;
        }
        if (this.poCheckDepositMaster == null) {
            this.poCheckDepositMaster = new Model_Check_Deposit_Master();
            this.poCheckDepositMaster.setApplicationDriver(this.poGRider);
            this.poCheckDepositMaster.setXML("Model_Check_Deposit_Master");
            this.poCheckDepositMaster.setTableName("Check_Deposit_Master");
            this.poCheckDepositMaster.initialize();
        }
        return this.poCheckDepositMaster;
    }

    public Model_Check_Deposit_Detail CheckDepositDetail() {
        if (this.poGRider == null) {
            System.err.println("CheckModels.CheckDepositDetail: Application driver is not set.");
            return null;
        }
        if (this.poCheckDepositDetail == null) {
            this.poCheckDepositDetail = new Model_Check_Deposit_Detail();
            this.poCheckDepositDetail.setApplicationDriver(this.poGRider);
            this.poCheckDepositDetail.setXML("Model_Check_Deposit_Detail");
            this.poCheckDepositDetail.setTableName("Check_Deposit_Detail");
            this.poCheckDepositDetail.initialize();
        }
        return this.poCheckDepositDetail;
    }
}
