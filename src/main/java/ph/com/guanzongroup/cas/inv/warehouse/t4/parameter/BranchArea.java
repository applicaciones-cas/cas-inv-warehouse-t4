package ph.com.guanzongroup.cas.inv.warehouse.t4.parameter;

import java.sql.SQLException;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.services.Parameter;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.inv.warehouse.t4.parameter.model.Model_Branch_Area;
import ph.com.guanzongroup.cas.inv.warehouse.t4.parameter.services.DeliveryParamModels;

/**
 *
 * @author 12mnv
 */
public class BranchArea extends Parameter {

    Model_Branch_Area poModel;

    public void initialize() throws SQLException, GuanzonException {
        this.poModel = (new DeliveryParamModels(this.poGRider)).BranchArea();
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
        if (this.poModel.getAreaCode().isEmpty()) {
            this.poJSON.put("result", "error");
            this.poJSON.put("message", "Area Code must not be empty.");
            return this.poJSON;
        }
        if (this.poModel.getAreaDescription().isEmpty()) {
            this.poJSON.put("result", "error");
            this.poJSON.put("message", "Area Description must not be empty.");
            return this.poJSON;
        }
        this.poModel.setModifyingId(this.poGRider.Encrypt(this.poGRider.getUserID()));
        this.poModel.setModifiedDate(this.poGRider.getServerDate());
        this.poJSON.put("result", "success");
        return this.poJSON;
    }

    public Model_Branch_Area getModel() {
        return this.poModel;
    }

    public JSONObject searchRecord(String value, boolean byCode) throws SQLException, GuanzonException {
        String lsCondition = "";
        String lsSQL = MiscUtil.addCondition(getSQ_Browse(), lsCondition);
        this.poJSON = ShowDialogFX.Search(this.poGRider,
                lsSQL,
                value,
                "Code»Area Name",
                "sAreaCode»sAreaDesc",
                "sAreaCode»sAreaDesc",
                byCode ? 0 : 1);
        if (this.poJSON != null) {
            return this.poModel.openRecord((String) this.poJSON.get("sBrgyIDxx"));
        }
        this.poJSON = new JSONObject();
        this.poJSON.put("result", "error");
        this.poJSON.put("message", "No record loaded.");
        return this.poJSON;
    }
}
