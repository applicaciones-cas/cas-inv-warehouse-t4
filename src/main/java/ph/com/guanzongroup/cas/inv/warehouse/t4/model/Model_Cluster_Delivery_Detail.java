package ph.com.guanzongroup.cas.inv.warehouse.t4.model;

import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.inv.model.Model_Inv_Serial;
import org.guanzon.cas.inv.model.Model_Inventory;
import org.guanzon.cas.inv.services.InvModels;
import org.guanzon.cas.parameter.model.Model_Branch;
import org.guanzon.cas.parameter.services.ParamModels;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.inv.warehouse.t4.InventoryStockIssuanceNeo;
import ph.com.guanzongroup.cas.inv.warehouse.t4.model.services.DeliveryIssuanceControllers;
import ph.com.guanzongroup.cas.inv.warehouse.t4.model.services.DeliveryIssuanceModels;

/**
 *
 * @author maynevval 08-09-2025
 */
public class Model_Cluster_Delivery_Detail extends Model {

    private Model_Branch poBranch;
    private Model_Inventory_Transfer_Master poInventoryMaster;
    private InventoryStockIssuanceNeo poIssuance;


    @Override
    public void initialize() {
        try {
            this.poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            this.poEntity.last();
            this.poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            this.poEntity.insertRow();
            this.poEntity.moveToCurrentRow();

            this.poEntity.absolute(1);

            this.poEntity.updateObject("nEntryNox", 1);
            this.poEntity.updateObject("nNoItemsx", 0);
            this.poEntity.updateNull("sReferNox");
            this.poEntity.updateNull("sSourceCd");
            this.poEntity.updateNull("sBranchCd");
            this.poEntity.updateString("cCancelld", "0");
            this.poEntity.updateNull("dCancelld");
            this.ID = poEntity.getMetaData().getColumnLabel(1);
            this.ID2 = poEntity.getMetaData().getColumnLabel(2);

            this.poBranch = (new ParamModels(this.poGRider)).Branch();
            this.poInventoryMaster = new DeliveryIssuanceModels(poGRider).InventoryTransferMaster();
            this.poIssuance = new DeliveryIssuanceControllers(poGRider, null).InventoryStockIssuanceNeo();
            poIssuance.initTransaction();
            pnEditMode = EditMode.UNKNOWN;
        } catch (SQLException | GuanzonException e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
    }
    //Getter & Setter 
    //sTransNox
    //nEntryNox*
    //sReferNox*
    //sSourceCd*
    //sBranchCd
    //nNoItemsx
    //cCancelld
    //dCancelld
    //dModified

    //sTransNox
    public JSONObject setTransactionNo(String transactionNo) {
        return setValue("sTransNox", transactionNo);
    }

    public String getTransactionNo() {
        return (String) getValue("sTransNox");
    }

    //nEntryNox
    public JSONObject setEntryNo(int entryNo) {
        return setValue("nEntryNox", entryNo);
    }

    public int getEntryNo() {
        return (int) getValue("nEntryNox");
    }

    //sReferNox
    public JSONObject setReferNo(String referno) {
        return setValue("sReferNox", referno);
    }

    public String getReferNo() {
        return (String) getValue("sReferNox");
    }

    //sSourceCd
    public JSONObject setSourceCode(String sourceCode) {
        return setValue("sSourceCd", sourceCode);
    }

    public String getSourceCode() {
        return (String) getValue("sSourceCd");
    }

    //sBranchCd
    public JSONObject setBranchCode(String branchCode) {
        return setValue("sBranchCd", branchCode);
    }

    public String getBranchCode() {
        return (String) getValue("sBranchCd");
    }

    //nNoItemsx
    public JSONObject setNoOfItem(Double quantity) {
        return setValue("nNoItemsx", quantity);
    }

    public Double getNoOfItem() {
        return Double.valueOf(getValue("nNoItemsx").toString());
    }

    //cCancelld
    public JSONObject setCancelled(String isCancelled) {
        return setValue("cCancelld", isCancelled);
    }

    public String getCancelled() {
        return (String) getValue("cCancelld");
    }

    //dCancelld
    public JSONObject setCancelledDate(Date modifiedDate) {
        return setValue("dCancelld", modifiedDate);
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
        return "";
    }

    public Model_Branch Branch() throws SQLException, GuanzonException {
        if (!"".equals(getValue("sBranchCd"))) {
            if (this.poBranch.getEditMode() == 1 && this.poBranch
                    .getBranchCode().equals(getValue("sBranchCd"))) {
                return this.poBranch;
            }
            this.poJSON = this.poBranch.openRecord((String) getValue("sBranchCd"));
            if ("success".equals(this.poJSON.get("result"))) {
                return this.poBranch;
            }
            this.poBranch.initialize();
            return this.poBranch;
        }
        this.poBranch.initialize();
        return this.poBranch;
    }


    public InventoryStockIssuanceNeo InventoryTransfer() throws SQLException, GuanzonException, CloneNotSupportedException {
        if (!"".equals(getValue("sReferNox")) && getValue("sReferNox") != null) {
            if (this.poIssuance
                    .getMaster().getTransactionNo() != null) {
                if (this.poIssuance.getEditMode() == 0 && this.poIssuance
                        .getMaster().getTransactionNo().equals(getValue("sReferNox"))) {
                    return this.poIssuance;
                }
                if (this.poIssuance.getEditMode() == 1 && this.poIssuance
                        .getMaster().getTransactionNo().equals(getValue("sReferNox"))) {
                    return this.poIssuance;
                }
                if (this.poIssuance.getEditMode() == 2 && this.poIssuance
                        .getMaster().getTransactionNo().equals(getValue("sReferNox"))) {
                    return this.poIssuance;
                }
            }
            this.poJSON = this.poIssuance.OpenTransaction((String) getValue("sReferNox"));
            if ("success".equals(this.poJSON.get("result"))) {
//                if (poIssuance.getEditMode() != EditMode.ADDNEW) {
//                    //auto update mode
////                    poIssuance.UpdateTransaction();
//                }
                 return this.poIssuance;
            }
            this.poIssuance.initTransaction();
            poIssuance.NewTransaction();
            setReferNo(poIssuance.getMaster().getTransactionNo());
            return this.poIssuance;
        }
        this.poIssuance.initTransaction();
        poIssuance.NewTransaction();
        setReferNo(poIssuance.getMaster().getTransactionNo());
        return this.poIssuance;
    }
}
