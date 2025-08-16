package ph.com.guanzongroup.cas.inv.warehouse.t4.parameter.model;

import java.sql.SQLException;
import java.util.Date;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.cas.client.model.Model_Client_Master;
import org.guanzon.cas.client.services.ClientModels;
import org.guanzon.cas.inv.model.Model_Inventory;
import org.guanzon.cas.inv.services.InvModels;
import org.json.simple.JSONObject;

/**
 *
 * @author maynevval 07-26-2025
 */
public class Model_Inventory_Supplier extends Model {

    Model_Inventory poInventory;
    Model_Client_Master poClientMaster;

    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            poEntity.updateString("cRecdStat", "1");

            poEntity.insertRow();
            poEntity.moveToCurrentRow();

            poEntity.absolute(1);

            ID = poEntity.getMetaData().getColumnLabel(1);
            ID2 = poEntity.getMetaData().getColumnLabel(2);
            poInventory = new InvModels(poGRider).Inventory();
            poClientMaster = new ClientModels(poGRider).ClientMaster();

            pnEditMode = EditMode.UNKNOWN;
        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
    }
    //Getter & Setter 
    //sStockIDx
    //sSupplier*
    //cRecdStat*

    //sStockIDx
    public JSONObject setStockID(String stockID) {
        return setValue("sStockIDx", stockID);
    }

    public String getStockID() {
        return (String) getValue("sStockIDx");
    }

    //sSupplier
    public JSONObject setSupplier(String supplier) {
        return setValue("sSupplier", supplier);
    }

    public String getSupplier() {
        return (String) getValue("sSupplier");
    }

    //cRecdStat
    public JSONObject setRecordStatus(String recordStatus) {
        return setValue("cRecdStat", recordStatus);
    }

    public String getRecordStatus() {
        return (String) getValue("cRecdStat");
    }

    //cRecdStat
    public JSONObject isRecordActive(boolean isRecordActive) {
        return setValue("cRecdStat", (isRecordActive == true) ? "1" : "0");
    }

    public boolean isRecordActive() {
        return RecordStatus.ACTIVE.equals(getValue("cRecdStat"));
    }

    @Override
    public String getNextCode() {
        return "";
    }

    public Model_Client_Master Supplier() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sSupplier"))) {
            if (poClientMaster.getEditMode() == EditMode.READY
                    && poClientMaster.getClientId().equals((String) getValue("sSupplier"))) {
                return poClientMaster;
            } else {
                poJSON = poClientMaster.openRecord((String) getValue("sSupplier"));

                if ("success".equals((String) poJSON.get("result"))) {
                    return poClientMaster;
                } else {
                    poClientMaster.initialize();
                    return poClientMaster;
                }
            }
        } else {
            poClientMaster.initialize();
            return poClientMaster;
        }
    }

    public Model_Inventory Inventory() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sSupplier"))) {
            if (poInventory.getEditMode() == EditMode.READY
                    && poInventory.getStockId().equals((String) getValue("sStockIDx"))) {
                return poInventory;
            } else {
                poJSON = poInventory.openRecord((String) getValue("sStockIDx"));

                if ("success".equals((String) poJSON.get("result"))) {
                    return poInventory;
                } else {
                    poInventory.initialize();
                    return poInventory;
                }
            }
        } else {
            poInventory.initialize();
            return poInventory;
        }
    }
}
