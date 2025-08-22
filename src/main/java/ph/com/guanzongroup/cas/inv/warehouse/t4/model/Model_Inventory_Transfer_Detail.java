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
public class Model_Inventory_Transfer_Detail extends Model {

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
            poEntity.updateNull("sStockIDx");
            poEntity.updateNull("sOrigIDxx");
            poEntity.updateNull("sOrderNox");
            poEntity.updateDouble("nQuantity", 0.00d);
            poEntity.updateDouble("nInvCostx", 0.00d);
            poEntity.updateDouble("nReceived", 0.00d);
            poEntity.updateNull("sOrderNox");
            poEntity.updateNull("sRecvIDxx");
            poEntity.updateNull("sRecvIDxx");
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
    //sStockIDx*
    //sOrigIDxx*
    //sOrderNox
    //nQuantity
    //nInvCostx
    //nReceived
    //sRecvIDxx
    //sSerialID
    //sNotesxxx

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

    //sStockIDx
    public JSONObject setStockId(String stockid) {
        return setValue("sStockIDx", stockid);
    }

    public String getStockId() {
        return (String) getValue("sStockIDx");
    }

    //sOrigIDxx
    public JSONObject setOriginalId(String originalid) {
        return setValue("sOrigIDxx", originalid);
    }

    public String getOriginalId() {
        return (String) getValue("sOrigIDxx");
    }

    //sOrigIDxx
    public JSONObject setOrderNo(String orderno) {
        return setValue("sOrderNox", orderno);
    }

    public String getOrderNo() {
        return (String) getValue("sOrderNox");
    }

    //nQuantity
    public JSONObject setQuantity(Double quantity) {
        return setValue("nQuantity", quantity);
    }

    public Double getQuantity() {
        return (Double) getValue("nQuantity");
    }

    //nInvCostx
    public JSONObject setInventoryCost(Double inventoryCost) {
        return setValue("nInvCostx", inventoryCost);
    }

    public Double getInventoryCost() {
        return (Double) getValue("nInvCostx");
    }

    //nReceived
    public JSONObject setReceivedQuantity(Double receivedQuantity) {
        return setValue("nReceived", receivedQuantity);
    }

    public Double getReceivedQuantity() {
        return (Double) getValue("nReceived");
    }

    //sRecvIDxx
    public JSONObject setReceivedId(String receivedQuantity) {
        return setValue("sRecvIDxx", receivedQuantity);
    }

    public String getReceivedId() {
        return (String) getValue("sRecvIDxx");
    }

    //sSerialID
    public JSONObject setSerialID(String serialID) {
        return setValue("sSerialID", serialID);
    }

    public String getSerialID() {
        return (String) getValue("sSerialID");
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
            if (this.poInventorySerial.getEditMode() == 1 && this.poInventory
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
