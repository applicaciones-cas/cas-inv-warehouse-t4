package ph.com.guanzongroup.cas.inv.warehouse.t4.parameter;

import java.sql.SQLException;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.services.Parameter;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.cas.client.Client;
import org.guanzon.cas.client.services.ClientControllers;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.inv.warehouse.t4.parameter.model.Model_Inventory_Supplier;
import ph.com.guanzongroup.cas.inv.warehouse.t4.parameter.services.DeliveryParamModels;

/**
 *
 * @author 12mnv
 */
public class InventorySupplier extends Parameter {

    Model_Inventory_Supplier poModel;

    public void initialize() throws SQLException, GuanzonException {
        this.poModel = (new DeliveryParamModels(this.poGRider)).InventorySupplier();
        super.initialize();
    }

    public JSONObject isEntryOkay() throws SQLException {
        this.poJSON = new JSONObject();
        if (this.poGRider.getUserLevel() < 16) {
            this.poJSON.put("result", "error");
            this.poJSON.put("message", "User is not allowed to save record.");
            return this.poJSON;
        }
        this.poJSON = new JSONObject();
        if (this.poModel.getStockID().isEmpty()) {
            this.poJSON.put("result", "error");
            this.poJSON.put("message", "Area Code must not be empty.");
            return this.poJSON;
        }
        if (this.poModel.getSupplier().isEmpty()) {
            this.poJSON.put("result", "error");
            this.poJSON.put("message", "Area Description must not be empty.");
            return this.poJSON;
        }
        this.poJSON.put("result", "success");
        return this.poJSON;
    }

    public Model_Inventory_Supplier getModel() {
        return this.poModel;
    }

    public JSONObject SearchSupplier(String value, boolean byCode)
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();

        Client loClientSupplier = new ClientControllers(poGRider, logwrapr).Client();
        loClientSupplier.Master().setRecordStatus(RecordStatus.ACTIVE);
        loClientSupplier.Master().setClientType("1");
        poJSON = loClientSupplier.Master().searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            getModel().setSupplier(loClientSupplier.Master().getModel().getClientId());
        }

        return poJSON;
    }

    public JSONObject SearchInventory(String value, boolean byCode)
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();

        InventoryBrowse loInventory = new InventoryBrowse(poGRider, logwrapr);
        loInventory.setRecordStatus(psRecdStat);
        poJSON = loInventory.searchInventory(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            getModel().setSupplier(loInventory.getModelInventory().getStockId());
        }

        return poJSON;
    }

    public JSONObject searchRecord(String value, boolean byCode) throws SQLException, GuanzonException {
        String lsCondition = "";
        if (this.psRecdStat.length() > 1) {
            for (int lnCtr = 0; lnCtr <= this.psRecdStat.length() - 1; lnCtr++) {
                lsCondition = lsCondition + ", " + SQLUtil.toSQL(Character.toString(this.psRecdStat.charAt(lnCtr)));
            }
            lsCondition = "cRecdStat IN (" + lsCondition.substring(2) + ")";
        } else {
            lsCondition = "cRecdStat = " + SQLUtil.toSQL(this.psRecdStat);
        }
        String lsSQL = MiscUtil.addCondition(getSQ_Browse(), lsCondition);
        this.poJSON = ShowDialogFX.Search(this.poGRider,
                lsSQL,
                value,
                "ID»Supplier ID",
                "sStockIDx»sSupplier",
                "sStockIDx»sSupplier",
                byCode ? 0 : 1);
        if (this.poJSON != null) {
            return this.poModel.openRecord((String) this.poJSON.get("sStockIDx"),(String) this.poJSON.get("sSupplier"));
        }
        this.poJSON = new JSONObject();
        this.poJSON.put("result", "error");
        this.poJSON.put("message", "No record loaded.");
        return this.poJSON;
    }
}
