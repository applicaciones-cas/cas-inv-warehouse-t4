package ph.com.guanzongroup.cas.inv.warehouse.t4;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import net.sf.jasperreports.engine.JRException;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.agent.services.Transaction;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.appdriver.iface.GValidator;
import org.guanzon.cas.client.model.Model_Client_Master;
import org.guanzon.cas.client.services.ClientModels;
import org.guanzon.cas.inv.warehouse.model.Model_Inv_Stock_Request_Master;
import org.guanzon.cas.inv.warehouse.services.InvWarehouseModels;
import org.guanzon.cas.parameter.TownCity;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.inv.warehouse.t4.constant.DeliveryStockIssuanceRecord;
import ph.com.guanzongroup.cas.inv.warehouse.t4.constant.InventoryStockIssuancePrint;
import ph.com.guanzongroup.cas.inv.warehouse.t4.constant.InventoryStockIssuanceStatus;
import ph.com.guanzongroup.cas.inv.warehouse.t4.model.Model_Cluster_Delivery_Detail;
import ph.com.guanzongroup.cas.inv.warehouse.t4.model.Model_Cluster_Delivery_Master;
import ph.com.guanzongroup.cas.inv.warehouse.t4.model.Model_Inventory_Transfer_Detail;
import ph.com.guanzongroup.cas.inv.warehouse.t4.model.services.DeliveryIssuanceControllers;
import ph.com.guanzongroup.cas.inv.warehouse.t4.model.services.DeliveryIssuanceModels;
import ph.com.guanzongroup.cas.inv.warehouse.t4.parameter.BranchCluster;
import ph.com.guanzongroup.cas.inv.warehouse.t4.parameter.services.DeliveryParamController;
import ph.com.guanzongroup.cas.inv.warehouse.t4.report.ReportUtil;
import ph.com.guanzongroup.cas.inv.warehouse.t4.report.ReportUtilListener;
import ph.com.guanzongroup.cas.inv.warehouse.t4.validators.InventoryClusterIssuanceValidatorFactory;

public class InventoryStockIssuance extends Transaction {

    private String psIndustryCode = "";
    private String psCompanyID = "";
    private String psCategoryCD = "";
    private String psApprovalUser = "";
    private List<Model> paMaster;
    private List<Model> paStockMaster;
    public Model poDetailExpiration;
//    public List<InventoryStockIssuanceNeo> paDetailOther;

    public void setIndustryID(String industryId) {
        psIndustryCode = industryId;
    }

    public void setCompanyID(String companyId) {
        psCompanyID = companyId;
    }

    public void setCategoryID(String categoryId) {
        psCategoryCD = categoryId;
    }

    public Model_Cluster_Delivery_Master getMaster() {
        return (Model_Cluster_Delivery_Master) poMaster;
    }

    @SuppressWarnings("unchecked")
    public List<Model_Cluster_Delivery_Master> getMasterList() {
        return (List<Model_Cluster_Delivery_Master>) (List<?>) paMaster;
    }

    @SuppressWarnings("unchecked")
    public List<Model_Inv_Stock_Request_Master> getStockMasterList() {
        return (List<Model_Inv_Stock_Request_Master>) (List<?>) paStockMaster;
    }

    public Model_Cluster_Delivery_Master getMaster(int masterRow) {
        return (Model_Cluster_Delivery_Master) paMaster.get(masterRow);

    }

    @SuppressWarnings("unchecked")
    public List<Model_Cluster_Delivery_Detail> getDetailList() {
        return (List<Model_Cluster_Delivery_Detail>) (List<?>) paDetail;
    }

    public Model_Cluster_Delivery_Detail getDetail(int entryNo) throws SQLException, GuanzonException, CloneNotSupportedException {
        if (getMaster().getTransactionNo().isEmpty() || entryNo <= 0) {
            return null;
        }

        Model_Cluster_Delivery_Detail loDetail;

        //auto add detail
        Model_Cluster_Delivery_Detail lastDetail = (Model_Cluster_Delivery_Detail) paDetail.get(paDetail.size() - 1);

        if (lastDetail.InventoryTransfer().getEditMode() == EditMode.ADDNEW) {
//            if (referNo == null || referNo.isEmpty()) {
            lastDetail.setTransactionNo(getMaster().getTransactionNo());
            lastDetail.setEntryNo(getDetailCount());
//            }
        }

        //find the detail record
        for (int lnCtr = 0; lnCtr < paDetail.size(); lnCtr++) {
            loDetail = (Model_Cluster_Delivery_Detail) paDetail.get(lnCtr);
            if (loDetail.getEntryNo() == entryNo) {                loDetail.InventoryTransfer();
                loDetail.InventoryTransfer().getMaster().setBranchCode(psBranchCode);
                loDetail.InventoryTransfer().getMaster().setCompanyID(psCompanyID);
                loDetail.InventoryTransfer().getMaster().setCategoryId(psCategoryCD);
                loDetail.InventoryTransfer().getMaster().setIndustryId(psIndustryCode);
                //keep getting nextcode avoid conflict to other
                if (loDetail.InventoryTransfer().getEditMode() == EditMode.ADDNEW) {
                    loDetail.InventoryTransfer().getMaster().getNextCode();
                    loDetail.setReferNo(loDetail.InventoryTransfer().getMaster().getTransactionNo());
                }
                return loDetail;
            }
        }

        Model_Cluster_Delivery_Detail loDetailNew = new DeliveryIssuanceModels(poGRider).InventoryClusterDeliveryDetail();
        loDetailNew.newRecord();
        loDetailNew.setTransactionNo(getMaster().getTransactionNo());
        loDetailNew.setEntryNo(entryNo);
        loDetailNew.InventoryTransfer().NewTransaction();
        loDetailNew.setReferNo(loDetailNew.InventoryTransfer().getMaster().getTransactionNo());
        System.out.println("Transaction no transfer = " + loDetailNew.InventoryTransfer().getMaster().getTransactionNo());
        paDetail.add(loDetailNew);

        return loDetailNew;
    }

    public JSONObject requestDetail(int stockRequest)
            throws GuanzonException, CloneNotSupportedException, SQLException {
        poJSON = new JSONObject();
        Model_Inv_Stock_Request_Master loStockMaster = (Model_Inv_Stock_Request_Master) paStockMaster.get(stockRequest);
        Model_Cluster_Delivery_Detail loDetail;

        //check if last is already Saved
        InventoryStockIssuanceNeo loDetailOther = getDetail(paDetail.size()).InventoryTransfer();
        if (loDetailOther != null) {
            if (loDetailOther.getEditMode() == EditMode.ADDNEW) {
                if (loDetailOther.getMaster().getOrderNo() != null
                        && !loDetailOther.getMaster().getOrderNo().isEmpty()) {
                    poJSON.put("result", "error");
//                    poJSON.put("message", "Unsaved Transaction Detected");
                    return poJSON;

                }
            }
        }

        //check if Stock Request already in Detail Other (Transfer Detail)
        for (int lnCtr = 1; lnCtr <= paDetail.size(); lnCtr++) {
            InventoryStockIssuanceNeo loExistingTransfer = getDetail(lnCtr).InventoryTransfer();
            if (loExistingTransfer.getMaster().getTransactionNo() != null) {
                if (loExistingTransfer.getMaster().getOrderNo() != null) {
                    if (loExistingTransfer.getMaster().getOrderNo().equals(loStockMaster.getTransactionNo())) {
                        if (loExistingTransfer.getEditMode() != EditMode.ADDNEW) {
                            poJSON.put("result", "success");
                            poJSON.put("message", "Stock Request is Already added! Delivery No." + loExistingTransfer.getMaster().getTransactionNo());
                            return poJSON;
                        }
                    }
                }
            }
        }

        //Add to Detail Not Existing
        if (loDetailOther.getEditMode() == EditMode.ADDNEW) {
            loDetail = getDetail(paDetail.size());
        } else {
            loDetail = getDetail(paDetail.size() + 1);
        }
        loDetail.setReferNo(loDetail.InventoryTransfer().getMaster().getTransactionNo());
        loDetail.setSourceCode(loDetail.InventoryTransfer().getSourceCode());
        loDetail.setBranchCode(loDetail.InventoryTransfer().getMaster().getBranchCode());

        //Inventory Transfer Master
        loDetail.InventoryTransfer().getMaster().setOrderNo(loStockMaster.getTransactionNo());
        loDetail.InventoryTransfer().getMaster().setDestination(loStockMaster.getBranchCode());
        loDetail.InventoryTransfer().getMaster().setDeliveryType("1");
        //Inventory Transfer Detail

        //clone detail to transfer
        InventoryRequestApproval loStockRequest = getRequestApproval(loStockMaster.getTransactionNo());
        if (loStockRequest != null) {
            int lnDetail = 0;
            for (int lnCtr = 1; lnCtr <= loStockRequest.getDetailCount(); lnCtr++) {
                if (loStockRequest.getDetail(lnCtr).getApproved() >= 1) {
                    lnDetail++;
                    loDetail.InventoryTransfer().getDetail(lnDetail).setOrderNo(loStockRequest.getMaster().getTransactionNo());
                    loDetail.InventoryTransfer().getDetail(lnDetail).setStockId(loStockRequest.getDetail(lnCtr).getStockId());
                    loDetail.InventoryTransfer().getDetail(lnDetail).setInventoryCost(((Number) loStockRequest.getDetail(lnCtr).Inventory().getCost()).doubleValue());
                }
            }
        } else {
            poJSON.put("result", "error");
            poJSON.put("message", "Unable to Retrieve Detail");
            return poJSON;
        }
        poJSON.put("result", "success");
//        poJSON.put("message", "Detail added successfully.");
        return poJSON;
    }

    public JSONObject replaceDetail(int entryNo, int stockRequest)
            throws GuanzonException, CloneNotSupportedException, SQLException {
        poJSON = new JSONObject();
        Model_Inv_Stock_Request_Master loStockMaster = (Model_Inv_Stock_Request_Master) paStockMaster.get(stockRequest);
        InventoryStockIssuanceNeo loDetailOther;
        Model_Cluster_Delivery_Detail loDetail;

        //check if Stock Request already in Detail (Delivery Cluster Detail)
        for (int lnDetail = 0; lnDetail <= paDetail.size() - 1; lnDetail++) {
            loDetail = (Model_Cluster_Delivery_Detail) paDetail.get(lnDetail);
            loDetailOther = loDetail.InventoryTransfer();
            if (loDetailOther.getMaster().getTransactionNo() != null) {
                if (loDetailOther.getMaster().getOrderNo() != null) {
                    if (loDetailOther.getMaster().getOrderNo().equals(loStockMaster.getTransactionNo())) {
                        poJSON.put("result", "success");
                        poJSON.put("message", "Stock Request is Already added! Delivery No." + loDetailOther.getMaster().getTransactionNo());
                        return poJSON;
                    }
                }
            }

        }

        //Add to Detail Not Existing
        loDetail = getDetail(entryNo);
        //clear record
        loDetailOther = loDetail.InventoryTransfer();
        loDetailOther.NewTransaction();

        loDetail.setReferNo(loDetailOther.getMaster().getTransactionNo());
        loDetail.setSourceCode(loDetailOther.getSourceCode());
        loDetail.setBranchCode(loDetailOther.getMaster().getBranchCode());

        //Inventory Transfer Master
        loDetail.InventoryTransfer().getMaster().setOrderNo(loStockMaster.getTransactionNo());
        loDetail.InventoryTransfer().getMaster().setDestination(loStockMaster.getBranchCode());
        loDetail.InventoryTransfer().getMaster().setDeliveryType("1");
        //Inventory Transfer Detail

        //clone detail to transfer
        InventoryRequestApproval loStockRequest = getRequestApproval(loStockMaster.getTransactionNo());

        if (loStockRequest != null) {
            for (int lnCtr = 1; lnCtr <= loStockRequest.getDetailCount(); lnCtr++) {
                if (loStockRequest.getDetail(lnCtr).getApproved() >= 1) {
                    loDetail.InventoryTransfer().getDetail(loDetail.InventoryTransfer().getDetailCount()).setOrderNo(loStockRequest.getMaster().getTransactionNo());
                    loDetail.InventoryTransfer().getDetail(loDetail.InventoryTransfer().getDetailCount()).setStockId(loStockRequest.getDetail(lnCtr).getStockId());
                    loDetail.InventoryTransfer().getDetail(loDetail.InventoryTransfer().getDetailCount()).setInventoryCost(((Number) loStockRequest.getDetail(lnCtr).Inventory().getCost()).doubleValue());
                }
            }
        } else {
            poJSON.put("result", "error");
            poJSON.put("message", "Unable to Retrieve Detail");
            return poJSON;
        }
        poJSON.put("result", "success");
//        poJSON.put("message", "Detail added successfully.");
        return poJSON;
    }

    private InventoryRequestApproval getRequestApproval(String transactionNo)
            throws GuanzonException, SQLException, CloneNotSupportedException {
        InventoryRequestApproval loSubClass = new DeliveryIssuanceControllers(poGRider, null).InventoryRequestApproval();
        loSubClass.initTransaction();
        loSubClass.OpenTransaction(transactionNo);

        if ("error".equals((String) poJSON.get("result"))) {
            return null;
        }

        return loSubClass;
    }

    public JSONObject initTransaction() throws GuanzonException, SQLException {
        SOURCE_CODE = "Dlvr";

        poMaster = new DeliveryIssuanceModels(poGRider).InventoryClusterDeliveryMaster();
        poDetail = new DeliveryIssuanceModels(poGRider).InventoryClusterDeliveryDetail();
//        poDetailExpiration = new DeliveryIssuanceModels(poGRider).InventoryTransferDetailExpiration();
        paMaster = new ArrayList<Model>();
        paDetail = new ArrayList<Model>();
//        paDetailOther = new ArrayList<InventoryStockIssuanceNeo>();
        paStockMaster = new ArrayList<Model>();
        initSQL();

        return super.initialize();
    }

    @Override
    public void initSQL() {
        SQL_BROWSE = "SELECT"
                + " a.sTransNox"
                + ", a.dTransact"
                + ", b.sClustrDs sClustrDs"
                + " FROM Cluster_Delivery_Master a "
                + "     LEFT JOIN Branch_Cluster b ON a.sClustrID = b.sClustrID";
    }

    public JSONObject OpenTransaction(String transactionNo) throws CloneNotSupportedException, SQLException, GuanzonException {
        return openTransaction(transactionNo);
    }

    public JSONObject NewTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();
        poJSON = newTransaction();
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        getMaster().setIndustryId(psIndustryCode);
        getMaster().setCompanyID(psCompanyID);
        getMaster().setCategoryId(psCategoryCD);
        getMaster().setBranchCode(poGRider.getBranchCode());

//        InventoryStockIssuanceNeo loDetailOther = new DeliveryIssuanceControllers(poGRider, null).InventoryStockIssuanceNeo();
//
//        loDetailOther.initTransaction();
//        loDetailOther.NewTransaction();
//        paDetailOther.add(loDetailOther);
////        getDetail(1).setReferNo(paDetailOther.get(0).getMaster().getTransactionNo());
        return poJSON;
    }

    public JSONObject SaveTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {

        return saveTransaction();
    }

    public JSONObject SaveTransactionDelivery(int deliveryNo) throws SQLException, GuanzonException, CloneNotSupportedException {

        poJSON = willSave();
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        System.out.println(getDetail(deliveryNo).InventoryTransfer().getMaster().getTransactionNo());
        poJSON = getDetail(deliveryNo).InventoryTransfer().SaveTransaction();
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        poJSON = SaveTransaction();
        if (!"error".equals((String) poJSON.get("result"))) {
            poJSON.put("result", "success");
            pnEditMode = EditMode.READY;
            UpdateTransaction();
            return poJSON;
        }

        return poJSON;
    }

    public JSONObject UpdateTransaction() {
        poJSON = new JSONObject();
        if (InventoryStockIssuanceStatus.CONFIRMED.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already confirmed.");
            return poJSON;
        }

        return updateTransaction();
    }

    @Override
    protected JSONObject willSave() throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        poJSON = isEntryOkay(InventoryStockIssuanceStatus.OPEN);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        if (!pbWthParent && getEditMode() == EditMode.ADDNEW) {
            getMaster().setTransactionNo(getMaster().getNextCode());
        }
        int lnDetailCount = 0;
        //assign values needed
        for (int lnCtr = 1; lnCtr <= paDetail.size(); lnCtr++) {
            Model_Cluster_Delivery_Detail loDetail = getDetail(lnCtr);
            if (loDetail.getReferNo() == null || loDetail.getReferNo().isEmpty()) {
                paDetail.remove(lnCtr - 1);
                continue;
            }

            InventoryStockIssuanceNeo loOtherDetail = getDetail(lnCtr).InventoryTransfer();
            double lnItemofDelivery = 0;

//            loOtherDetail.getMaster().setTransactionNo(loOtherDetail.getMaster().getNextCode());
            loDetail.setReferNo(loOtherDetail.getMaster().getTransactionNo());
            for (int lnCtrOther = loOtherDetail.getDetailCount(); lnCtrOther >= 1; lnCtrOther--) {
                Model_Inventory_Transfer_Detail subDetail = loOtherDetail.getDetail(lnCtrOther);
                if (subDetail.getQuantity() <= 0) {
                    loOtherDetail.paDetail.remove(lnCtrOther - 1);
                } else {
                    subDetail.setTransactionNo(loOtherDetail.getMaster().getTransactionNo());
                    lnItemofDelivery++;
                }
            }

            lnDetailCount++;
            loDetail.setTransactionNo(getMaster().getTransactionNo());
            loDetail.setNoOfItem(lnItemofDelivery);
            loDetail.setEntryNo(lnDetailCount);
        }

        getMaster().setEntryNo(lnDetailCount);
        pdModified = poGRider.getServerDate();

        poJSON.put("result", "success");
        return poJSON;

    }

    @Override
    protected JSONObject isEntryOkay(String status) {
        psApprovalUser = "";

        poJSON = new JSONObject();
        GValidator loValidator = InventoryClusterIssuanceValidatorFactory.make(getMaster().getIndustryId());

        loValidator.setApplicationDriver(poGRider);
        loValidator.setTransactionStatus(status);
        loValidator.setMaster(poMaster);
        ArrayList laDetailList = new ArrayList<>(getDetailList());
        loValidator.setDetail(laDetailList);

        poJSON = loValidator.validate();
        if (poJSON.containsKey("isRequiredApproval") && Boolean.TRUE.equals(poJSON.get("isRequiredApproval"))) {
            if (poGRider.getUserLevel() <= UserRight.ENCODER) {
                poJSON = ShowDialogFX.getUserApproval(poGRider);
                if ("error".equals((String) poJSON.get("result"))) {
                    return poJSON;
                } else {
                    if (Integer.parseInt(poJSON.get("nUserLevl").toString()) <= UserRight.ENCODER) {
                        poJSON.put("result", "error");
                        poJSON.put("message", "User is not an authorized approving officer.");
                        return poJSON;
                    }
                    psApprovalUser = poJSON.get("sUserIDxx") != null
                            ? poJSON.get("sUserIDxx").toString()
                            : poGRider.getUserID();
                }
            } else {
                psApprovalUser = poGRider.getUserID();
            }
        }
        return poJSON;
    }

    public JSONObject CloseTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (InventoryStockIssuanceStatus.CONFIRMED.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already confirmed.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(InventoryStockIssuanceStatus.CONFIRMED);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poGRider.beginTrans("UPDATE STATUS", "ConfirmTransaction", SOURCE_CODE, getMaster().getTransactionNo());

        poJSON = statusChange(poMaster.getTable(),
                (String) poMaster.getValue("sTransNox"),
                "ConfirmTransaction",
                InventoryStockIssuanceStatus.CONFIRMED,
                false, true);
        if ("error".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }

        poGRider.commitTrans();

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "Transaction confirmed successfully.");

        return poJSON;
    }

    public JSONObject PostTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        if (getEditMode() != EditMode.UPDATE
                && getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid Edit Mode.");
            return poJSON;
        }

        if (InventoryStockIssuanceStatus.POSTED.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already posted.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(InventoryStockIssuanceStatus.POSTED);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        poMaster.setValue("sReceived", poGRider.Encrypt(poGRider.getUserID()));
        if (!psApprovalUser.isEmpty()) {
            poMaster.setValue("sApproved", poGRider.Encrypt(psApprovalUser));
        }
        poJSON = SaveTransaction();
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poGRider.beginTrans("UPDATE STATUS", "PostTransaction", SOURCE_CODE, getMaster().getTransactionNo());

        poJSON = statusChange(poMaster.getTable(),
                (String) poMaster.getValue("sTransNox"),
                "PostTransaction",
                InventoryStockIssuanceStatus.POSTED,
                false, true);
        if ("error".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }

        poGRider.commitTrans();

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "Transaction posted successfully.");

        return poJSON;
    }

    public JSONObject CancelTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid Edit Mode");
            return poJSON;
        }
//
//        if (InventoryStockIssuanceStatus.CONFIRMED.equals((String) poMaster.getValue("cTranStat"))) {
//            poJSON.put("result", "error");
//            poJSON.put("message", "Transaction was already confirmed.");
//            return poJSON;
//        }

        if (InventoryStockIssuanceStatus.CANCELLED.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already cancelled.");
            return poJSON;
        }

        if (InventoryStockIssuanceStatus.VOID.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already voided.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(InventoryStockIssuanceStatus.CANCELLED);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poGRider.beginTrans("UPDATE STATUS", "CancelTransaction", SOURCE_CODE, getMaster().getTransactionNo());

        poJSON = statusChange(poMaster.getTable(),
                (String) poMaster.getValue("sTransNox"),
                "CancelTransaction",
                InventoryStockIssuanceStatus.CANCELLED,
                false, true);
        if ("error".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }

        poGRider.commitTrans();

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "Transaction cancelled successfully.");

        return poJSON;
    }

    public JSONObject VoidTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid Edit Mode.");
            return poJSON;
        }

//        if (DeliveryScheduleStatus.CONFIRMED.equals((String) poMaster.getValue("cTranStat"))) {
//            poJSON.put("result", "error");
//            poJSON.put("message", "Transaction was already confirmed.");
//            return poJSON;
//        }
        if (InventoryStockIssuanceStatus.CANCELLED.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already cancelled.");
            return poJSON;
        }

        if (InventoryStockIssuanceStatus.VOID.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already voided.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(InventoryStockIssuanceStatus.VOID);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poGRider.beginTrans("UPDATE STATUS", "VoidTransaction", SOURCE_CODE, getMaster().getTransactionNo());

        poJSON = statusChange(poMaster.getTable(),
                (String) poMaster.getValue("sTransNox"),
                "VoidTransaction",
                InventoryStockIssuanceStatus.VOID,
                false, true);
        if ("error".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }

        poGRider.commitTrans();

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "Transaction voided successfully.");

        return poJSON;
    }

    public JSONObject searchTransaction(String value, boolean byCode, boolean byExact) {
        try {
            String lsSQL = SQL_BROWSE;

            lsSQL = MiscUtil.addCondition(lsSQL, "a.sBranchCd = " + SQLUtil.toSQL(poGRider.getBranchCode()));

            String lsCondition = "";
            if (psTranStat != null) {
                if (this.psTranStat.length() > 1) {
                    for (int lnCtr = 0; lnCtr <= this.psTranStat.length() - 1; lnCtr++) {
                        lsCondition = lsCondition + ", " + SQLUtil.toSQL(Character.toString(this.psTranStat.charAt(lnCtr)));
                    }
                    lsCondition = "a.cTranStat IN (" + lsCondition.substring(2) + ")";
                } else {
                    lsCondition = "a.cTranStat = " + SQLUtil.toSQL(this.psTranStat);
                }
                lsSQL = MiscUtil.addCondition(lsSQL, lsCondition);
            }
            if (!psCategoryCD.isEmpty()) {
                lsSQL = MiscUtil.addCondition(lsSQL, " a.sCategrCd = " + SQLUtil.toSQL(psCategoryCD));
            }

            System.out.println("Search Query is = " + lsSQL);
            poJSON = ShowDialogFX.Search(poGRider,
                    lsSQL,
                    value,
                    "Transaction No»Date»Cluster Name",
                    "sTransNox»dTransact»sClustrDs",
                    "a.sTransNox»a.dTransact»b.sClustrDs",
                    byExact ? (byCode ? 0 : 1) : 2);

            if (poJSON != null) {
                return openTransaction((String) poJSON.get("sTransNox"));

//            } else if ("error".equals((String) poJSON.get("result"))) {
//                return poJSON;
            } else {
                poJSON = new JSONObject();
                poJSON.put("result", "error");
                poJSON.put("message", "No record loaded.");
                return poJSON;

            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            Logger.getLogger(InventoryStockIssuance.class
                    .getName()).log(Level.SEVERE, null, ex);
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }

    public JSONObject searchTransactionPosting(String value, boolean byCode, boolean byExact) {
        try {
            String lsSQL = SQL_BROWSE;
            String lsCondition = "";
            if (psTranStat != null) {
                if (this.psTranStat.length() > 1) {
                    for (int lnCtr = 0; lnCtr <= this.psTranStat.length() - 1; lnCtr++) {
                        lsCondition = lsCondition + ", " + SQLUtil.toSQL(Character.toString(this.psTranStat.charAt(lnCtr)));
                    }
                    lsCondition = "a.cTranStat IN (" + lsCondition.substring(2) + ")";
                } else {
                    lsCondition = "a.cTranStat = " + SQLUtil.toSQL(this.psTranStat);
                }
                lsSQL = MiscUtil.addCondition(lsSQL, lsCondition);
            }
            lsSQL = MiscUtil.addCondition(lsSQL, "a.sDestinat = " + SQLUtil.toSQL(poGRider.getBranchCode()));

            if (!psCategoryCD.isEmpty()) {
                lsSQL = MiscUtil.addCondition(lsSQL, " a.sCategrCd = " + SQLUtil.toSQL(psCategoryCD));
            }
            System.out.println("Search Query is = " + lsSQL);
            poJSON = ShowDialogFX.Search(poGRider,
                    lsSQL,
                    value,
                    "Transaction No»Date»Cluster Name",
                    "sTransNox»dTransact»sClustrDs",
                    "a.sTransNox»a.dTransact»b.sClustrDs",
                    byExact ? (byCode ? 0 : 1) : 2);

            if (poJSON != null) {
                poJSON = openTransaction((String) poJSON.get("sTransNox"));

                if (!"error".equals((String) poJSON.get("result"))) {
                    return UpdateTransaction();
                }
                return poJSON;
//            } else if ("error".equals((String) poJSON.get("result"))) {
//                return poJSON;
            } else {
                poJSON = new JSONObject();
                poJSON.put("result", "error");
                poJSON.put("message", "No record loaded.");
                return poJSON;

            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            Logger.getLogger(InventoryStockIssuance.class
                    .getName()).log(Level.SEVERE, null, ex);
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }

    public JSONObject searchTransactionCluster(String value, boolean byCode) throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        BranchCluster loSubClass = new DeliveryParamController(poGRider, logwrapr).BranchCluster();
        loSubClass.setRecordStatus(RecordStatus.ACTIVE);
        if (!psIndustryCode.isEmpty()) {
            loSubClass.getModel().setIndustryCode(psIndustryCode);
        }
        poJSON = loSubClass.searchRecordbyIndustry(value, byCode);

        System.out.println("result " + (String) poJSON.get("result"));
        if ("success".equals((String) poJSON.get("result"))) {

            getMaster().setClusterID(loSubClass.getModel().getClusterID());
        }
        return poJSON;
    }

    public JSONObject searchTransactionTown(String value, boolean byCode) throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        TownCity loSubClass = new ParamControllers(poGRider, logwrapr).TownCity();
        loSubClass.setRecordStatus(RecordStatus.ACTIVE);

        poJSON = loSubClass.searchRecord(value, byCode);

        System.out.println("result " + (String) poJSON.get("result"));
        if ("success".equals((String) poJSON.get("result"))) {

            getMaster().setTownId(loSubClass.getModel().getTownId());
        }
        return poJSON;
    }

    public JSONObject searchTransactionPlate(String value, boolean byCode) throws SQLException, GuanzonException {
//        poJSON = new JSONObject();
//        TownCity loSubClass = new ParamControllers(poGRider, logwrapr).TownCity();
//        loSubClass.setRecordStatus(RecordStatus.ACTIVE);
//
//        poJSON = loSubClass.searchRecord(value, byCode);
//
//        System.out.println("result " + (String) poJSON.get("result"));
//        if ("success".equals((String) poJSON.get("result"))) {
//
//            getMaster().setClusterID(loSubClass.getModel().getTownId());
//        }
//        return poJSON;

        poJSON.put("result", "error");
        poJSON.put("message", "Temporary disabled.");
        return poJSON;
    }

    public JSONObject searchTransactionDriver(String value, boolean byCode) throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        Model_Client_Master loSubClass = new ClientModels(poGRider).ClientMaster();
        loSubClass.setRecordStatus(RecordStatus.ACTIVE);

        String lsSQL = "SELECT"
                + " b.sClientID,"
                + " b.sCompnyNm,"
                + " b.sLastName,"
                + " b.sFrstName,"
                + " b.sMiddName,"
                + " b.sMaidenNm"
                + " FROM GGC_iSysDBF.Employee_Master001 a"
                + " LEFT JOIN Client_Master b ON a.sEmployID = b.sClientID"
                + "  WHERE a.cDriverxx = " + SQLUtil.toSQL(RecordStatus.ACTIVE)
                + "  AND a.cRecdStat = " + SQLUtil.toSQL(RecordStatus.ACTIVE)
                + "  AND b.sClientID <> ''";

//        lsSQL = MiscUtil.addCondition(lsSQL, "a.sBranchCd = " + SQLUtil.toSQL(poGRider.getBranchCode()));
        System.out.println("Search Query is = " + lsSQL);
        poJSON = ShowDialogFX.Search(poGRider,
                lsSQL,
                value,
                "Employee ID»Name",
                "sClientID»sCompnyNm",
                "b.sClientID»b.sCompnyNm",
                byCode ? 0 : 1);

        if (poJSON != null) {
            poJSON = loSubClass.openRecord((String) poJSON.get("sClientID"));

            if ("success".equals((String) poJSON.get("result"))) {
                if (getMaster().getEmploy01() != null
                        && getMaster().getEmploy01().equals(loSubClass.getClientId())) {
                    poJSON.put("result", "error");
                    poJSON.put("message", "Same Employee Detected. Please select different Employee");
                    return poJSON;
                } else if (getMaster().getEmploy02() != null
                        && getMaster().getEmploy02().equals(loSubClass.getClientId())) {
                    poJSON.put("result", "error");
                    poJSON.put("message", "Same Employee Detected. Please select different Employee");
                    return poJSON;
                }

                getMaster().setDriverID(loSubClass.getClientId());
            }
            return poJSON;
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;

        }
    }

    public JSONObject searchTransactionAssistant01(String value, boolean byCode) throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        Model_Client_Master loSubClass = new ClientModels(poGRider).ClientMaster();
        loSubClass.setRecordStatus(RecordStatus.ACTIVE);

        String lsSQL = "SELECT"
                + " b.sClientID,"
                + " b.sCompnyNm,"
                + " b.sLastName,"
                + " b.sFrstName,"
                + " b.sMiddName,"
                + " b.sMaidenNm"
                + " FROM GGC_iSysDBF.Employee_Master001 a"
                + " LEFT JOIN Client_Master b ON a.sEmployID = b.sClientID"
                + "  WHERE a.cDriverxx = " + SQLUtil.toSQL(RecordStatus.ACTIVE)
                + "  AND a.cRecdStat = " + SQLUtil.toSQL(RecordStatus.ACTIVE)
                + "  AND b.sClientID <> ''";

//        lsSQL = MiscUtil.addCondition(lsSQL, "a.sBranchCd = " + SQLUtil.toSQL(poGRider.getBranchCode()));
        System.out.println("Search Query is = " + lsSQL);
        poJSON = ShowDialogFX.Search(poGRider,
                lsSQL,
                value,
                "Employee ID»Name",
                "sClientID»sCompnyNm",
                "b.sClientID»b.sCompnyNm",
                byCode ? 0 : 1);

        if (poJSON != null) {
            poJSON = loSubClass.openRecord((String) poJSON.get("sClientID"));

            if ("success".equals((String) poJSON.get("result"))) {
                if (getMaster().getDriverID() != null) {
                    if (getMaster().getDriverID().equals(loSubClass.getClientId())) {
                        poJSON.put("result", "error");
                        poJSON.put("message", "Same Employee Detected to Driver. Please select different Employee");
                        return poJSON;
                    }
                } else if (getMaster().getEmploy02() != null) {
                    if (getMaster().getEmploy02().equals(loSubClass.getClientId())) {
                        poJSON.put("result", "error");
                        poJSON.put("message", "Same Employee Detected. Please select different Employee");
                        return poJSON;
                    }
                }
                getMaster().setEmploy01(loSubClass.getClientId());
            }
            return poJSON;
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;

        }
    }

    public JSONObject searchTransactionAssistant02(String value, boolean byCode) throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        Model_Client_Master loSubClass = new ClientModels(poGRider).ClientMaster();
        loSubClass.setRecordStatus(RecordStatus.ACTIVE);

        String lsSQL = "SELECT"
                + " b.sClientID,"
                + " b.sCompnyNm,"
                + " b.sLastName,"
                + " b.sFrstName,"
                + " b.sMiddName,"
                + " b.sMaidenNm"
                + " FROM GGC_iSysDBF.Employee_Master001 a"
                + " LEFT JOIN Client_Master b ON a.sEmployID = b.sClientID"
                + "  WHERE a.cDriverxx = " + SQLUtil.toSQL(RecordStatus.ACTIVE)
                + "  AND a.cRecdStat = " + SQLUtil.toSQL(RecordStatus.ACTIVE)
                + "  AND b.sClientID <> ''";

//        lsSQL = MiscUtil.addCondition(lsSQL, "a.sBranchCd = " + SQLUtil.toSQL(poGRider.getBranchCode()));
        System.out.println("Search Query is = " + lsSQL);
        poJSON = ShowDialogFX.Search(poGRider,
                lsSQL,
                value,
                "Employee ID»Name",
                "sClientID»sCompnyNm",
                "b.sClientID»b.sCompnyNm",
                byCode ? 0 : 1);

        if (poJSON != null) {
            poJSON = loSubClass.openRecord((String) poJSON.get("sClientID"));

            if ("success".equals((String) poJSON.get("result"))) {
                if (getMaster().getDriverID() != null) {
                    if (getMaster().getDriverID().equals(loSubClass.getClientId())) {
                        poJSON.put("result", "error");
                        poJSON.put("message", "Same Employee Detected to Driver. Please select different Employee");
                        return poJSON;
                    }
                } else if (getMaster().getEmploy01() != null) {
                    if (getMaster().getEmploy01().equals(loSubClass.getClientId())) {
                        poJSON.put("result", "error");
                        poJSON.put("message", "Same Employee Detected. Please select different Employee");
                        return poJSON;
                    }
                } else if (getMaster().getEmploy01() == null) {
                    getMaster().setEmploy01(loSubClass.getClientId());
                    return poJSON;
                }
                getMaster().setEmploy02(loSubClass.getClientId());

            }
            return poJSON;
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;

        }
    }

    public JSONObject searchDetailIssuanceSerial(int detailEntryNo, int detailTransferEntryNo, String fsValue, boolean fbByCode, boolean fbByExact)
            throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();
        InventoryStockIssuanceNeo loDetailOther = getDetail(detailEntryNo).InventoryTransfer();
        if (loDetailOther.getDetail(detailTransferEntryNo).getOrderNo() == null
                || loDetailOther.getDetail(detailTransferEntryNo).getOrderNo().isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "Unable to search add detail");
            return poJSON;
        }
        if (!loDetailOther.getDetail(detailTransferEntryNo).Inventory().isSerialized()) {
            poJSON.put("result", "error");
            poJSON.put("message", "This Inventory is none serialize. Detail Row" + detailTransferEntryNo);
            return poJSON;
        }

        poJSON = loDetailOther.searchDetailByClusterIssuance(detailTransferEntryNo, fsValue, fbByCode, fbByExact);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        // handle cloning if approved > 1
        if (loDetailOther.getDetail(detailTransferEntryNo).InventoryStockRequest().getApproved() > 1) {

            loDetailOther.getDetail(detailTransferEntryNo).InventoryStockRequest().setApproved(1d);

            //clone detail to transfer
            int lnStockRow = detailTransferEntryNo;
            double lnIssuedCount = 0;
            InventoryRequestApproval loStockRequest = getRequestApproval(loDetailOther.getMaster().getOrderNo());
            if (loStockRequest != null) {
                //check existing record for row and count of issued
                for (int lnCtr = 1; lnCtr <= loStockRequest.getDetailCount(); lnCtr++) {
                    if (loStockRequest.getDetail(lnCtr).getStockId() != null) {
                        if (loStockRequest.getDetail(lnCtr).getStockId()
                                .equals(loDetailOther.getDetail(detailTransferEntryNo).getStockId())) {
                            lnStockRow = lnCtr;
                            for (int lnRowDetail = 1; lnRowDetail <= loDetailOther.getDetailCount(); lnRowDetail++) {
                                if (loDetailOther.getDetail(lnRowDetail).getStockId() != null) {

                                    if (loStockRequest.getDetail(lnCtr).getStockId()
                                            .equals(loDetailOther.getDetail(lnRowDetail).getStockId())) {

                                        lnIssuedCount = lnIssuedCount + loDetailOther.getDetail(detailTransferEntryNo).getQuantity();
                                    }
                                }
                            }
                        }
                    }
                }
            }
            //add seperate detail
            Model_Inventory_Transfer_Detail loLastDetailOther = loDetailOther.getDetail(loDetailOther.getDetailCount());
            if (loLastDetailOther.getStockId() == null || loLastDetailOther.getStockId().isEmpty()) {
                //count if the detail is already has issued
                loLastDetailOther.setOrderNo(loStockRequest.getMaster().getTransactionNo());
                loLastDetailOther.setStockId(loStockRequest.getDetail(lnStockRow).getStockId());
                loLastDetailOther.setInventoryCost(((Number) loStockRequest.getDetail(lnStockRow).Inventory().getCost()).doubleValue());
                loLastDetailOther.InventoryStockRequest().setApproved(loStockRequest.getDetail(lnStockRow).getApproved() - lnIssuedCount);

            } else {
                Model_Inventory_Transfer_Detail loNewDetailOther = loDetailOther.getDetail(loDetailOther.getDetailCount() + 1);
                loNewDetailOther.setOrderNo(loStockRequest.getMaster().getTransactionNo());
                loNewDetailOther.setStockId(loStockRequest.getDetail(lnStockRow).getStockId());
                loNewDetailOther.setInventoryCost(((Number) loStockRequest.getDetail(lnStockRow).Inventory().getCost()).doubleValue());
                loNewDetailOther.InventoryStockRequest().setApproved(loStockRequest.getDetail(lnStockRow).getApproved() - lnIssuedCount);

            }
        }
        poJSON.put("result", "success");
        return poJSON;

    }

    public JSONObject loadStockTransactionList()
            throws SQLException, GuanzonException, CloneNotSupportedException {

        if (getMaster().getClusterID() == null
                || getMaster().getClusterID().isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "No cluster is set.");
            return poJSON;
        }
        paStockMaster.clear();
        initSQL();
        String lsSQL = DeliveryStockIssuanceRecord.StockRequestRecord();

        if (!psIndustryCode.isEmpty()) {
            lsSQL = MiscUtil.addCondition(lsSQL, "a.sIndstCdx = " + SQLUtil.toSQL(psIndustryCode));
        }
        if (!psCategoryCD.isEmpty()) {
            lsSQL = MiscUtil.addCondition(lsSQL, "a.sCategrCd = " + SQLUtil.toSQL(psCategoryCD));
        }

        if (getMaster().getTownId() != null
                && !getMaster().getTownId().isEmpty()) {
            lsSQL = MiscUtil.addCondition(lsSQL, " e.sTownIDxx = " + SQLUtil.toSQL(getMaster().getTownId()));
        }
        lsSQL = MiscUtil.addCondition(lsSQL, " b.nApproved > (b.nCancelld + b.nIssueQty + b.nOrderQty) ");
        lsSQL = MiscUtil.addCondition(lsSQL, "a.cProcessd = " + SQLUtil.toSQL(RecordStatus.ACTIVE));
        lsSQL = MiscUtil.addCondition(lsSQL, "d.sClustrID = " + SQLUtil.toSQL(getMaster().getClusterID()));

        ResultSet loRS = poGRider.executeQuery(lsSQL);
        System.out.println("Load Transaction list query is " + lsSQL);

        if (MiscUtil.RecordCount(loRS)
                <= 0) {
            poJSON.put("result", "error");
            poJSON.put("message", "No record found.");
            return poJSON;
        }
        Set<String> processedTrans = new HashSet<>();

        while (loRS.next()) {
            String transNo = loRS.getString("sTransNox");

            // Skip if we already processed this transaction number
            if (processedTrans.contains(transNo)) {
                continue;
            }

            Model_Inv_Stock_Request_Master loInventoryRequest
                    = new InvWarehouseModels(poGRider).InventoryStockRequestMaster();

            poJSON = loInventoryRequest.openRecord(transNo);

            if ("success".equals((String) poJSON.get("result"))) {
                paStockMaster.add((Model) loInventoryRequest);

                // Mark this transaction as processed
                processedTrans.add(transNo);
            } else {
                return poJSON;
            }
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        return poJSON;
    }

    public JSONObject setDetailItemCount() throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();
        double lnItemValidCount;
        for (int lnCtr = 0; lnCtr < paDetail.size(); lnCtr++) {
            lnItemValidCount = 0.0;
            Model_Cluster_Delivery_Detail loDetail = getDetail(lnCtr);
            if (loDetail == null) {
                poJSON.put("result", "success");
                return poJSON;
            }
            if (loDetail.getReferNo() != null && !loDetail.getReferNo().isEmpty()) {

                for (int lnDetailOther = 0; lnDetailOther < loDetail.InventoryTransfer().getDetailCount(); lnDetailOther++) {
                    if (loDetail.InventoryTransfer().getDetail(lnDetailOther).getStockId() != null
                            && !loDetail.InventoryTransfer().getDetail(lnDetailOther).getStockId().isEmpty()) {
                        if (loDetail.InventoryTransfer().getDetail(lnDetailOther).getQuantity() > 0) {

                            lnItemValidCount = lnItemValidCount + ((Number) loDetail.InventoryTransfer().getDetail(lnDetailOther).getQuantity()).doubleValue();
                        }
                    }

                }
            }

            loDetail.setNoOfItem(lnItemValidCount);
        }

        poJSON.put("result", "success");
        return poJSON;
    }

    public JSONObject printRecord() throws SQLException, JRException, CloneNotSupportedException, GuanzonException {

        poJSON = new JSONObject();

        if (InventoryStockIssuanceStatus.POSTED.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already Processed.");
            return poJSON;
        }
//
//        if (InventoryStockIssuanceStatus.CONFIRMED.equals((String) poMaster.getValue("cTranStat"))) {
//            poJSON.put("result", "error");
//            poJSON.put("message", "Transaction was already confirmed.");
//            return poJSON;
//        }

        if (InventoryStockIssuanceStatus.CANCELLED.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already cancelled.");
            return poJSON;
        }

        if (InventoryStockIssuanceStatus.VOID.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already voided.");
            return poJSON;
        }
        poJSON = isEntryOkay(InventoryStockIssuanceStatus.CONFIRMED);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        ReportUtil poReportJasper = new ReportUtil(poGRider);

        if (psCategoryCD == null && psCategoryCD.isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "Category is Required for this Transaction");
            return poJSON;
        }
        if (getMaster().getTransactionNo() == null && getMaster().getTransactionNo().isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "No record is Selected");
            return poJSON;

        }
        poJSON = OpenTransaction(getMaster().getTransactionNo());
        if ("error".equals((String) poJSON.get("result"))) {
            System.out.println("Print Record open transaction : " + (String) poJSON.get("message"));
            return poJSON;
        }

        // Attach listener
        poReportJasper.setReportListener(new ReportUtilListener() {
            @Override
            public void onReportOpen() {
                System.out.println("Report opened.");
            }

            @Override
            public void onReportClose() {
                //fetch/add if needed
                System.out.println("Report closed.");
            }

            @Override
            public void onReportPrint() {
                System.out.println("Report printing...");
                try {
//                    if (!isJSONSuccess(PrintTransaction(), "Print Record",
//                            "Initialize Record Print! ")) {
//                        return;
//
//                    }
                    if (getMaster().getTransactionStatus().equals(InventoryStockIssuanceStatus.OPEN)) {
                        if (!isJSONSuccess(CloseTransaction(), "Print Record",
                                "Initialize Close Transaction! ")) {
                        }
                    }

                    poReportJasper.CloseReportUtil();

                } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                    Logger.getLogger(InventoryRequestApproval.class
                            .getName()).log(Level.SEVERE, null, ex);
                    ShowMessageFX.Error("", "", ex.getMessage());
                }
            }

            @Override
            public void onReportExport() {
                System.out.println("Report exported.");
                if (!isJSONSuccess(poReportJasper.exportReportbyExcel(), "Export Record",
                        "Initialize Record Export! ")) {
                    return;
                }

//                poReportJasper.CloseReportUtil();
                //if used a model or array please create function 
            }

            @Override
            public void onReportExportPDF() {
                System.out.println("Report exported.");
//                poReportJasper.CloseReportUtil();
            }

        });
        //add Parameter
        poReportJasper.addParameter("BranchName", poGRider.getBranchName());
        poReportJasper.addParameter("Address", poGRider.getAddress());
        poReportJasper.addParameter("CompanyName", poGRider.getClientName());
        poReportJasper.addParameter("TransactionNo", getMaster().getTransactionNo());
        poReportJasper.addParameter("TransactionDate", SQLUtil.dateFormat(getMaster().getTransactionDate(), SQLUtil.FORMAT_LONG_DATE));
        poReportJasper.addParameter("Remarks", getMaster().getRemarks());
//        poReportJasper.addParameter("Destination", getMaster().BranchDestination().getBranchName());
//        poReportJasper.addParameter("Trucking", getMaster().TruckingCompany().getCompanyName());
//        poReportJasper.addParameter("DatePrinted", SQLUtil.dateFormat(poGRider.getServerDate(), SQLUtil.FORMAT_TIMESTAMP));

//        poReportJasper.addParameter("watermarkImagePath", poGRider.getReportPath() + "images\\approved.png");
        poReportJasper.setReportName("Inventory Issuance");
        poReportJasper.setJasperPath(InventoryStockIssuancePrint.getJasperReport(psIndustryCode));

        //process by ResultSet
        String lsSQL = InventoryStockIssuancePrint.PrintRecordQuery();
        lsSQL = MiscUtil.addCondition(lsSQL, "InventoryTransferMaster.sTransNox = " + SQLUtil.toSQL(getMaster().getTransactionNo()));

        poReportJasper.setSQLReport(lsSQL);
        System.out.println("Print Data Query :" + lsSQL);

        //process by JasperCollection parse ur List / ArrayList
        //JRBeanCollectionDataSource jrRS = new JRBeanCollectionDataSource(R1data);
        //poReportJasper.setJRBeanCollectionDataSource(jrRS);
        //direct pass JasperViewer
        //         reportPrint = JasperFillManager.fillReport(poGRider.getReportPath() + psJasperPath + ".jasper",
        //                    poParamater,
        //                    yourDATA);
        //        poReportJasper.setJasperPrint(report0Print);
        poReportJasper.isAlwaysTop(false);
        poReportJasper.isWithUI(true);
        poReportJasper.isWithExport(true);
        poReportJasper.isWithExportPDF(true);
        poReportJasper.willExport(true);
        return poReportJasper.generateReport();

    }

    private boolean isJSONSuccess(JSONObject loJSON, String module, String fsModule) {
        String result = (String) loJSON.get("result");
        if ("error".equals(result)) {
            String message = (String) loJSON.get("message");
            Platform.runLater(() -> {
                if (message != null) {
                    ShowMessageFX.Warning(null, module, fsModule + ": " + message);
                }
            });
            return false;
        }
        String message = (String) loJSON.get("message");

        Platform.runLater(() -> {
            if (message != null) {
                ShowMessageFX.Information(null, module, fsModule + ": " + message);
            }
        });
        return true;

    }

}
