package ph.com.guanzongroup.cas.inv.warehouse.t4.model;

import java.sql.SQLException;
import java.util.Date;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.inv.warehouse.t4.model.services.DeliveryScheduleModels;
import ph.com.guanzongroup.cas.inv.warehouse.t4.parameter.model.Model_Branch_Cluster;
import ph.com.guanzongroup.cas.inv.warehouse.t4.parameter.services.DeliveryParamModels;

/**
 *
 * @author maynevval 07-24-2025
 */
public class Model_Delivery_Schedule_Detail extends Model {

    private Model_Branch_Cluster poBranchCluster;

    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            poEntity.updateString("cTrckSize", "0");

            poEntity.insertRow();
            poEntity.moveToCurrentRow();

            poEntity.absolute(1);

            ID = poEntity.getMetaData().getColumnLabel(1);
            ID2 = poEntity.getMetaData().getColumnLabel(2);
            
            poBranchCluster = new DeliveryParamModels(poGRider).BranchCluster();

            pnEditMode = EditMode.UNKNOWN;
        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
    }
    //Getter & Setter 
    //sTransNox
    //sClustrID*
    //cTrckSize*
    //sRemarksx*
    //cCancelld
    //dCancelld

    //sTransNox
    public JSONObject setTransactionNo(String transactionNo) {
        return setValue("sTransNox", transactionNo);
    }

    public String getTransactionNo() {
        return (String) getValue("sTransNox");
    }

    //sClustrID
    public JSONObject setClusterID(String clusterId) {
        return setValue("sClustrID", clusterId);
    }

    public String getClusterID() {
        return (String) getValue("sClustrID");
    }

    //cTrckSize
    public JSONObject setTruckSize(String truckSize) {
        return setValue("cTrckSize", truckSize);
    }

    public String getTruckSize() {
        return (String) getValue("cTrckSize");
    }

    //sRemarksx
    public JSONObject setRemarks(String remarks) {
        return setValue("sRemarksx", remarks);
    }

    public String getRemarks() {
        return (String) getValue("sRemarksx");
    }

    //cCancelld
    public JSONObject setCancelledStatus(String transactionStatus) {
        return setValue("cCancelld", transactionStatus);
    }

    public String getCancelledStatus() {
        return (String) getValue("cCancelld");
    }

    //dCancelld
    public JSONObject setCancelledDate(Date cancelledDate) {
        return setValue("dCancelld", cancelledDate);
    }

    public Date getCancelledDate() {
        return (Date) getValue("dCancelld");
    }

    //sModified
    public JSONObject setModifyingId(String modifyingId) {
        return setValue("sModified", modifyingId);
    }

    public String getModifyingId() {
        return (String) getValue("sModified");
    }

    //dModified
    public JSONObject setModifiedDate(Date modifiedDate) {
        return setValue("dModified", modifiedDate);
    }

    public Date getModifiedDate() {
        return (Date) getValue("dModified");
    }

    @Override
    public String getNextCode() {
        return MiscUtil.getNextCode(this.getTable(), ID, true, poGRider.getGConnection().getConnection(), poGRider.getBranchCode());
    }

    public Model_Branch_Cluster BranchCluster() throws SQLException, GuanzonException {
        if (!"".equals(getValue("sClustrID")) ) {
            if (this.poBranchCluster.getEditMode() == 1 && this.poBranchCluster
                    .getClusterID().equals(getValue("sClustrID"))) {
                return this.poBranchCluster;
            }
            this.poJSON = this.poBranchCluster.openRecord((String) getValue("sClustrID"));
            if ("success".equals(this.poJSON.get("result"))) {
                return this.poBranchCluster;
            }
            this.poBranchCluster.initialize();
            return this.poBranchCluster;
        }
        poBranchCluster.initialize();
        return this.poBranchCluster;
    }

}
