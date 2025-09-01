package ph.com.guanzongroup.cas.inv.warehouse.t4.model;

import java.sql.SQLException;
import java.util.Date;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.inv.model.Model_Inv_Serial;
import org.guanzon.cas.inv.model.Model_Inventory;
import org.guanzon.cas.inv.services.InvModels;
import org.json.simple.JSONObject;

/**
 *
 * @author maynevval 08-09-2025
 */
public class Model_Cluster_Delivery_Detail extends Model {

    private Model_Inventory poInventorySupersede;
    private Model_Inventory poInventory;
    private Model_Inv_Serial poInventorySerial;

    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            poEntity.insertRow();
            poEntity.moveToCurrentRow();

            poEntity.absolute(1);

            poEntity.updateObject("nEntryNox", 1);
            poEntity.updateObject("nNoItemsx", 0);
            poEntity.updateNull("sReferNox");
            poEntity.updateNull("sSourceCd");
            poEntity.updateNull("sBranchCd");
            poEntity.updateString("cCancelld", "0");
            poEntity.updateNull("dCancelld");
            ID = poEntity.getMetaData().getColumnLabel(1);
            ID2 = poEntity.getMetaData().getColumnLabel(2);

            poInventory = new InvModels(poGRider).Inventory();
            poInventorySupersede = new InvModels(poGRider).Inventory();
            poInventorySerial = new InvModels(poGRider).InventorySerial();

            pnEditMode = EditMode.UNKNOWN;
        } catch (SQLException e) {
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

    public Model_Inventory Inventory() throws SQLException, GuanzonException {
        if (!"".equals(getValue("sStockIDx"))) {
            if (this.poInventory.getEditMode() == 1 && this.poInventory
                    .getStockId().equals(getValue("sStockIDx"))) {
                return this.poInventory;
            }
            this.poJSON = this.poInventory.openRecord((String) getValue("sStockIDx"));
            if ("success".equals(this.poJSON.get("result"))) {
                return this.poInventory;
            }
            this.poInventory.initialize();
            return this.poInventory;
        }
        poInventory.initialize();
        return this.poInventory;
    }

    public Model_Inventory InventorySupersede() throws SQLException, GuanzonException {
        if (!"".equals(getValue("sOrigIDxx"))) {
            if (this.poInventorySupersede.getEditMode() == 1 && this.poInventorySupersede
                    .getStockId().equals(getValue("sOrigIDxx"))) {
                return this.poInventorySupersede;
            }
            this.poJSON = this.poInventorySupersede.openRecord((String) getValue("sOrigIDxx"));
            if ("success".equals(this.poJSON.get("result"))) {
                return this.poInventorySupersede;
            }
            this.poInventorySupersede.initialize();
            return this.poInventory;
        }
        poInventorySupersede.initialize();
        return this.poInventorySupersede;
    }

    public Model_Inv_Serial InventorySerial() throws SQLException, GuanzonException {
        if (!"".equals(getValue("sSerialID"))) {
            if (this.poInventorySerial.getEditMode() == 1 && this.poInventorySerial
                    .getStockId().equals(getValue("sSerialID"))) {
                return this.poInventorySerial;
            }
            this.poJSON = this.poInventorySerial.openRecord((String) getValue("sSerialID"));
            if ("success".equals(this.poJSON.get("result"))) {
                return this.poInventorySerial;
            }
            this.poInventorySerial.initialize();
            return this.poInventorySerial;
        }
        poInventorySerial.initialize();
        return this.poInventorySerial;
    }

}
