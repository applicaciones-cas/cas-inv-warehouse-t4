package ph.com.guanzongroup.cas.inv.warehouse.t4.validators;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.appdriver.iface.GValidator;
import org.guanzon.cas.inv.warehouse.model.Model_Inv_Stock_Request_Detail;
import org.guanzon.cas.inv.warehouse.model.Model_Inv_Stock_Request_Master;
import org.guanzon.cas.inv.warehouse.status.StockRequestStatus;
import org.json.simple.JSONObject;

/**
 *
 * @author Arsiela 03-12-2025
 */
public class InventoryStockRequestApproval_Vehicle implements GValidator {

    GRiderCAS poGRider;
    String psTranStat;
    JSONObject poJSON;

    Model_Inv_Stock_Request_Master poMaster;
    ArrayList<Model_Inv_Stock_Request_Detail> paDetail;

    @Override
    public void setApplicationDriver(Object applicationDriver) {
        poGRider = (GRiderCAS) applicationDriver;
    }

    @Override
    public void setTransactionStatus(String transactionStatus) {
        psTranStat = transactionStatus;
    }

    @Override
    public void setMaster(Object value) {
        poMaster = (Model_Inv_Stock_Request_Master) value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setDetail(ArrayList<Object> value) {
        paDetail = (ArrayList<Model_Inv_Stock_Request_Detail>) (ArrayList<?>) value;
    }

    @Override
    public void setOthers(ArrayList<Object> value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public JSONObject validate() {
        try {
            switch (psTranStat) {
                case StockRequestStatus.CONFIRMED:
                    return validateConfirmed();
                case StockRequestStatus.PROCESSED:
                    return validateProcess();
                default:
                    poJSON = new JSONObject();
                    poJSON.put("result", "error");
                    poJSON.put("message", "unsupported function");
            }
        } catch (SQLException ex) {
            Logger.getLogger(InventoryStockRequestApproval_Vehicle.class.getName()).log(Level.SEVERE, null, ex);
        }

        return poJSON;
    }

    private JSONObject validateConfirmed() throws SQLException {
        poJSON = new JSONObject();
        boolean isRequiredApproval = false;

        if (poMaster.getTransactionDate() == null) {
            poJSON.put("message", "Invalid Transaction Date.");
            return poJSON;
        }
        if (poMaster.getIndustryId() == null) {
            poJSON.put("message", "Industry is not set.");
            return poJSON;
        }
        if (poMaster.getCompanyID() == null || poMaster.getCompanyID().isEmpty()) {
            poJSON.put("message", "Company is not set.");
            return poJSON;
        }
        if (poMaster.getCategoryId()
                == null || poMaster.getCategoryId().isEmpty()) {
            poJSON.put("message", "Category is not set.");
            return poJSON;
        }
        if (poMaster.getBranchCode() == null || poMaster.getBranchCode().isEmpty()) {
            poJSON.put("message", "Branch is not set.");
            return poJSON;
        }

        int lnDetailCount = 0;
        for (int lnCtr = 0; lnCtr < paDetail.size(); lnCtr++) {
            if (paDetail.get(lnCtr).getApproved() > 0
                    || paDetail.get(lnCtr).getCancelled() > 0) {
                lnDetailCount++;
            }

        }

        if (lnDetailCount <= 0) {
            poJSON.put("result", "error");
            poJSON.put("message", "Unmodified Transaction");
            return poJSON;
        }

        
        if (poMaster.getProcessed()) {
            if (poGRider.getUserLevel() <= UserRight.ENCODER) {
                isRequiredApproval = true;
            }
        }
        poJSON.put("result", "success");
        poJSON.put("isRequiredApproval", isRequiredApproval);

        return poJSON;
    }

    private JSONObject validateProcess() {
        poJSON = new JSONObject();

        int lnDetailCount = 0;
        for (int lnCtr = 0; lnCtr < paDetail.size(); lnCtr++) {
            if ((paDetail.get(lnCtr).getApproved() + paDetail.get(lnCtr).getCancelled())
                    >= paDetail.get(lnCtr).getQuantity()) {
                lnDetailCount++;
            }

        }

        if (lnDetailCount != paDetail.size()) {
            poJSON.put("result", "error");
//            poJSON.put("message", "Detail is not fully processed.");
            return poJSON;
        }

        poJSON.put("result", "success");

        return poJSON;
    }

}
