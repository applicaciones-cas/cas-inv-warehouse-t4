package ph.com.guanzongroup.cas.inv.warehouse.t4.validators;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.appdriver.iface.GValidator;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.inv.warehouse.t4.constant.InventoryStockIssuanceStatus;
import ph.com.guanzongroup.cas.inv.warehouse.t4.model.Model_Cluster_Delivery_Detail;
import ph.com.guanzongroup.cas.inv.warehouse.t4.model.Model_Cluster_Delivery_Master;

/**
 *
 * @author MNV t4
 */
public class InventoryStockClusterIssuance_Vehicle implements GValidator {

    GRiderCAS poGRider;
    String psTranStat;
    JSONObject poJSON;

    Model_Cluster_Delivery_Master poMaster;
    ArrayList<Model_Cluster_Delivery_Detail> paDetail;

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
        poMaster = (Model_Cluster_Delivery_Master) value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setDetail(ArrayList<Object> value) {
        paDetail = (ArrayList<Model_Cluster_Delivery_Detail>) (ArrayList<?>) value;
    }

    @Override
    public void setOthers(ArrayList<Object> value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public JSONObject validate() {
        try {
            switch (psTranStat) {
                case InventoryStockIssuanceStatus.OPEN:
                    return validateNew();
                case InventoryStockIssuanceStatus.CONFIRMED:
                    return validateConfirmed();
                case InventoryStockIssuanceStatus.POSTED:
                    return validatePosted();
                case InventoryStockIssuanceStatus.CANCELLED:
                    return validateCancelled();
                case InventoryStockIssuanceStatus.VOID:
                    return validateVoid();
                default:
                    poJSON = new JSONObject();
                    poJSON.put("result", "error");
                    poJSON.put("message", "unsupported function");
            }
        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
            Logger.getLogger(InventoryStockClusterIssuance_MC.class.getName()).log(Level.SEVERE, null, ex);
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", ex.getMessage());
        }

        return poJSON;
    }

    private JSONObject validateNew() throws SQLException {
        poJSON = new JSONObject();
        boolean isRequiredApproval = false;

        if (poMaster.getTransactionDate() == null) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid Transaction Date.");
            return poJSON;
        }

        //change transaction date 
        if (poMaster.getTransactionDate().after((Date) poGRider.getServerDate())
                && poMaster.getTransactionDate().before((Date) poGRider.getServerDate())) {
            poJSON.put("message", "Change of transaction date are not allowed.! Approval is Required");
            isRequiredApproval = true;
        }

        if (poMaster.getDepartreDate() != null) {
            isRequiredApproval = true;
        }

//        if (poMaster.getIndustryId() == null) {
//            poJSON.put("result", "error");
//            poJSON.put("message", "Industry is not set.");
//            return poJSON;
//        }
        if (poMaster.getCompanyID() == null || poMaster.getCompanyID().isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "Company is not set.");
            return poJSON;
        }
        if (poMaster.getCategoryId()
                == null || poMaster.getCategoryId().isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "Category is not set.");
            return poJSON;
        }
        if (poMaster.getBranchCode() == null || poMaster.getBranchCode().isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "Branch is not set.");
            return poJSON;
        }
        if (poMaster.getSerialId() == null || poMaster.getSerialId().isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "Serial is not set.");
            return poJSON;
        }
        if (poMaster.getDriverID() == null || poMaster.getDriverID().isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "Driver is not set.");
            return poJSON;
        }
        if (poMaster.getEmploy01() == null || poMaster.getEmploy01().isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "Employee 01 is not set.");
            return poJSON;
        }

        int lnDetailCount = 0;
        for (int lnCtr = 0; lnCtr < paDetail.size(); lnCtr++) {
            if (paDetail.get(lnCtr).getReferNo() != null
                    && !paDetail.get(lnCtr).getReferNo().isEmpty()) {

                lnDetailCount++;

            }
        }

        if (lnDetailCount <= 0) {
            poJSON.put("result", "error");
            poJSON.put("message", "Detail is not set.");
            return poJSON;
        }

        poJSON.put("result", "success");
        poJSON.put("isRequiredApproval", isRequiredApproval);

        return poJSON;
    }

    private JSONObject validateConfirmed() throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();
        boolean isRequiredApproval = false;

        if (poMaster.getTransactionDate() == null) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid Transaction Date.");
            return poJSON;
        }

        if (poMaster.getIndustryId() == null) {
            poJSON.put("result", "error");
            poJSON.put("message", "Industry is not set.");
            return poJSON;
        }
        if (poMaster.getCompanyID() == null || poMaster.getCompanyID().isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "Company is not set.");
            return poJSON;
        }

        int lnDetailCount = 0;
        for (int lnCtr = 0; lnCtr < paDetail.size(); lnCtr++) {
            if (paDetail.get(lnCtr).getReferNo() != null
                    && !paDetail.get(lnCtr).getReferNo().isEmpty()) {

                if (paDetail.get(lnCtr).InventoryTransfer().getMaster()
                        .getTransactionStatus().equals(InventoryStockIssuanceStatus.OPEN)) {
                    poJSON.put("result", "error");
                    poJSON.put("message", "Unprinted delivery Detected. Row =" + lnCtr);
                    return poJSON;
                }
                lnDetailCount++;

            }
        }

        if (lnDetailCount <= 0) {
            poJSON.put("result", "error");
            poJSON.put("message", "Detail is not set.");
            return poJSON;
        }

        poJSON.put("result", "success");
        poJSON.put("isRequiredApproval", isRequiredApproval);

        return poJSON;
    }

    private JSONObject validatePosted() {
        poJSON = new JSONObject();
        boolean isRequiredApproval = false;
        if (poGRider.getUserLevel() <= UserRight.ENCODER) {
            isRequiredApproval = true;
        }

        if (poMaster.getArrivalDate() == null) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid Arrival Transaction Date.");
            return poJSON;
        }

        int lnDetailCount = 0;
        for (int lnCtr = 0; lnCtr < paDetail.size(); lnCtr++) {
            if (paDetail.get(lnCtr).getReferNo() != null
                    && !paDetail.get(lnCtr).getReferNo().isEmpty()) {

                lnDetailCount++;

            }
        }

        if (lnDetailCount <= 0) {
            poJSON.put("result", "error");
            poJSON.put("message", "Detail is not set.");
            return poJSON;
        }

        poJSON.put("result", "success");
        poJSON.put("isRequiredApproval", isRequiredApproval);
        return poJSON;
    }

    private JSONObject validateCancelled() throws SQLException, GuanzonException, CloneNotSupportedException {
        boolean isRequiredApproval = false;
        poJSON = new JSONObject();

        if (poGRider.getUserLevel() <= UserRight.ENCODER) {
            isRequiredApproval = true;
        }

        for (int lnCtr = 0; lnCtr < paDetail.size(); lnCtr++) {
            if (paDetail.get(lnCtr).getReferNo() != null
                    && !paDetail.get(lnCtr).getReferNo().isEmpty()) {

                if (!paDetail.get(lnCtr).InventoryTransfer().getMaster()
                        .getTransactionStatus().equals(InventoryStockIssuanceStatus.OPEN)) {
                    poJSON.put("result", "error");
                    poJSON.put("message", "Cofirmed delivery Detected. Row =" + lnCtr);
                    return poJSON;
                }

            }
        }
        poJSON.put("result", "success");
        poJSON.put("isRequiredApproval", isRequiredApproval);
        return poJSON;
    }

    private JSONObject validateVoid() throws SQLException, GuanzonException, CloneNotSupportedException {
        boolean isRequiredApproval = false;
        poJSON = new JSONObject();

        if (poGRider.getUserLevel() <= UserRight.ENCODER) {
            isRequiredApproval = true;
        }

        for (int lnCtr = 0; lnCtr < paDetail.size(); lnCtr++) {
            if (paDetail.get(lnCtr).getReferNo() != null
                    && !paDetail.get(lnCtr).getReferNo().isEmpty()) {

                if (!paDetail.get(lnCtr).InventoryTransfer().getMaster()
                        .getTransactionStatus().equals(InventoryStockIssuanceStatus.OPEN)) {
                    poJSON.put("result", "error");
                    poJSON.put("message", "Cofirmed delivery Detected. Row =" + lnCtr);
                    return poJSON;
                }

            }
        }
        poJSON.put("result", "success");
        poJSON.put("isRequiredApproval", isRequiredApproval);
        return poJSON;
    }

}
