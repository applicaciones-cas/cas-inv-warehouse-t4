package ph.com.guanzongroup.cas.inv.warehouse.t4;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
import org.guanzon.cas.parameter.model.Model_Branch;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.guanzon.cas.parameter.services.ParamModels;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.inv.warehouse.t4.constant.DeliveryStockIssuanceRecord;
import ph.com.guanzongroup.cas.inv.warehouse.t4.constant.InventoryStockIssuancePrint;
import ph.com.guanzongroup.cas.inv.warehouse.t4.constant.InventoryStockIssuanceStatus;
import ph.com.guanzongroup.cas.inv.warehouse.t4.model.Model_Cluster_Delivery_Detail;
import ph.com.guanzongroup.cas.inv.warehouse.t4.model.Model_Cluster_Delivery_Master;
import ph.com.guanzongroup.cas.inv.warehouse.t4.model.Model_Delivery_Schedule_Detail;
import ph.com.guanzongroup.cas.inv.warehouse.t4.model.services.DeliveryIssuanceControllers;
import ph.com.guanzongroup.cas.inv.warehouse.t4.model.services.DeliveryIssuanceModels;
import ph.com.guanzongroup.cas.inv.warehouse.t4.parameter.BranchCluster;
import ph.com.guanzongroup.cas.inv.warehouse.t4.parameter.InventoryBrowse;
import ph.com.guanzongroup.cas.inv.warehouse.t4.parameter.services.DeliveryParamController;
import ph.com.guanzongroup.cas.inv.warehouse.t4.report.ReportUtil;
import ph.com.guanzongroup.cas.inv.warehouse.t4.report.ReportUtilListener;
import ph.com.guanzongroup.cas.inv.warehouse.t4.validators.InventoryIssuanceValidatorFactory;

public class InventoryStockIssuance extends Transaction {

    private String psIndustryCode = "";
    private String psCompanyID = "";
    private String psCategorCD = "";
    private String psApprovalUser = "";
    private List<Model> paMaster;
    private List<Model> paStockMaster;
    public Model poDetailExpiration;
    public List<InventoryStockIssuanceNeo> paDetailOther;

    public void setIndustryID(String industryId) {
        psIndustryCode = industryId;
    }

    public void setCompanyID(String companyId) {
        psCompanyID = companyId;
    }

    public void setCategoryID(String categoryId) {
        psCategorCD = categoryId;
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

    public Model_Cluster_Delivery_Detail getDetail(int entryNo) {
        if (getMaster().getTransactionNo().isEmpty()) {
            return null;
        }

        if (entryNo <= 0 || entryNo > paDetail.size()) {
            return null;
        }

        Model_Cluster_Delivery_Detail loDetail;

        //find the detail record
        for (int lnCtr = 0; lnCtr <= paDetail.size() - 1; lnCtr++) {
            loDetail = (Model_Cluster_Delivery_Detail) paDetail.get(lnCtr);

            if (loDetail.getEntryNo() == entryNo) {
                return loDetail;
            }
        }

        loDetail = new DeliveryIssuanceModels(poGRider).InventoryClusterDeliveryDetail();
        loDetail.newRecord();
        loDetail.setTransactionNo(getMaster().getTransactionNo());
        loDetail.setEntryNo(entryNo);
        paDetail.add(loDetail);

        return loDetail;
    }

    public InventoryStockIssuanceNeo getDetailOther(int entryNo) throws SQLException, GuanzonException, CloneNotSupportedException {
        if (getMaster().getTransactionNo().isEmpty()
                || getMaster().getIndustryId().isEmpty()) {
            return null;
        }

        if (entryNo <= 0 || entryNo > paDetailOther.size()) {
            return null;
        }
        Model_Cluster_Delivery_Detail loDetail = getDetail(entryNo);
        InventoryStockIssuanceNeo loDetailOther;

        //find the detail record
        for (int lnCtr = 0; lnCtr <= paDetailOther.size() - 1; lnCtr++) {
            loDetailOther = (InventoryStockIssuanceNeo) paDetailOther.get(lnCtr);

            if (loDetailOther.getMaster().getTransactionNo() == loDetail.getReferNo()) {
                return loDetailOther;
            }
        }

        if (loDetail.getReferNo() != null && !loDetail.getReferNo().isEmpty()) {
            loDetailOther = new DeliveryIssuanceControllers(poGRider, null).InventoryStockIssuanceNeo();
            loDetailOther.initTransaction();
            loDetailOther.OpenTransaction(loDetail.getReferNo());
        }
        loDetailOther = new DeliveryIssuanceControllers(poGRider, null).InventoryStockIssuanceNeo();
        loDetailOther.initTransaction();
        loDetailOther.NewTransaction();
        getDetail(entryNo).setReferNo(loDetailOther.getMaster().getTransactionNo());

        return loDetailOther;
    }

    @SuppressWarnings("unchecked")
    public List<InventoryStockIssuanceNeo> getDetailOtherList() {
        return (List<InventoryStockIssuanceNeo>) (List<?>) paDetailOther;
    }

    public JSONObject initTransaction() throws GuanzonException, SQLException {
        SOURCE_CODE = "Dlvr";

        poMaster = new DeliveryIssuanceModels(poGRider).InventoryTransferMaster();
        poDetail = new DeliveryIssuanceModels(poGRider).InventoryTransferDetail();
        poDetailExpiration = new DeliveryIssuanceModels(poGRider).InventoryTransferDetailExpiration();
        paMaster = new ArrayList<Model>();
        paDetail = new ArrayList<Model>();
        paDetailOther = new ArrayList<InventoryStockIssuanceNeo>();
        initSQL();

        return super.initialize();
    }

    @Override
    public void initSQL() {
        SQL_BROWSE = "SELECT"
                + " a.sTransNox"
                + ", a.dTransact"
                + ", d.sBranchNm xBranchNm"
                + ", e.sBranchNm xDestinat"
                + ", c.sCompnyNm sCompnyNm"
                + ", a.sBranchCd"
                + ", a.sDestinat"
                + " FROM Inv_Transfer_Master a "
                + "     LEFT JOIN AP_Client_Master b ON a.sTruckIDx = b.sClientID"
                + "     LEFT JOIN Client_Master c ON b.sClientID = c.sClientID"
                + "     LEFT JOIN Branch d ON a.sBranchCd = d.sBranchCd"
                + "     LEFT JOIN Branch e ON a.sDestinat = e.sBranchCd";
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
        getMaster().setCategoryId(psCategorCD);
        getMaster().setBranchCode(poGRider.getBranchCode());
        return poJSON;
    }

    public JSONObject SaveTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
        return saveTransaction();
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
    protected JSONObject willSave() throws SQLException {
        poJSON = new JSONObject();

        poJSON = isEntryOkay(InventoryStockIssuanceStatus.OPEN);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        int lnDetailCount = 0;

        //assign values needed
        for (int lnCtr = 0; lnCtr < paDetail.size(); lnCtr++) {
            Model_Cluster_Delivery_Detail loDetail = (Model_Cluster_Delivery_Detail) paDetail.get(lnCtr);
            if (loDetail.getReferNo().isEmpty()) {

                lnDetailCount++;
                loDetail.setTransactionNo(getMaster().getTransactionNo());
                loDetail.setEntryNo(lnDetailCount);
            } else {
                paDetail.remove(lnCtr);

            }
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
        GValidator loValidator = InventoryIssuanceValidatorFactory.make(getMaster().getIndustryId());

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
            if (!psCategorCD.isEmpty()) {
                lsSQL = MiscUtil.addCondition(lsSQL, " a.sCategrCd = " + SQLUtil.toSQL(psCategorCD));
            }

            System.out.println("Search Query is = " + lsSQL);
            poJSON = ShowDialogFX.Search(poGRider,
                    lsSQL,
                    value,
                    "Transaction No»Destination»Date",
                    "sTransNox»xDestinat»dTransact",
                    "a.sTransNox»e.sBranchNm»a.dTransact",
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

            if (!psCategorCD.isEmpty()) {
                lsSQL = MiscUtil.addCondition(lsSQL, " a.sCategrCd = " + SQLUtil.toSQL(psCategorCD));
            }
            System.out.println("Search Query is = " + lsSQL);
            poJSON = ShowDialogFX.Search(poGRider,
                    lsSQL,
                    value,
                    "Transaction No»Branch Name»Date",
                    "sTransNox»xBranchNm»dTransact",
                    "a.sTransNox»d.sBranchNm»a.dTransact",
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

//    public JSONObject searchDetailByIssuance(int row, String value, boolean byCode) throws SQLException, GuanzonException {
//        InventoryBrowse loBrowse = new InventoryBrowse(poGRider, logwrapr);
//        loBrowse.initTransaction();
//        if (!psIndustryCode.isEmpty()) {
//            loBrowse.setIndustry(psIndustryCode);
//        }
//        loBrowse.setCategoryFilters(psCategorCD);
//        loBrowse.setBranch(poGRider.getBranchCode());
//
//        poJSON = new JSONObject();
//
//        poJSON = loBrowse.searchInventoryIssaunce(value, byCode);
//        System.out.println("result " + (String) poJSON.get("result"));
//        if ("success".equals((String) poJSON.get("result"))) {
//            for (int lnExisting = 0; lnExisting <= paDetail.size() - 1; lnExisting++) {
//                Model_Inventory_Transfer_Detail loExisting = (Model_Inventory_Transfer_Detail) paDetail.get(lnExisting);
//                if (loExisting.getStockId() != null) {
//                    if (loExisting.getStockId().equals(loBrowse.getModelInventory().getStockId())) {
//                        if (!loExisting.getSerialID().isEmpty()) {
//                            if (loBrowse.getModelInventorySerial().getSerialId() != null) {
//                                if (!loExisting.getSerialID().equals(loBrowse.getModelInventorySerial().getSerialId())) {
//                                    continue;
//                                }
//                            }
//                        }
//                        poJSON = new JSONObject();
//                        poJSON.put("result", "error");
//                        poJSON.put("message", "Selected Inventory is already exist!");
//                        return poJSON;
//
//                    }
//                }
//            }
//            getDetail(row).setStockId(loBrowse.getModelInventory().getStockId());
//            if (loBrowse.getModelInventorySerial().getSerialId() != null) {
//                getDetail(row).setSerialID(loBrowse.getModelInventorySerial().getSerialId());
//            }
//
//            getDetail(row).setQuantity(1.00);
//        }
//
//        return poJSON;
//
//    }
//    public JSONObject searchDetailByIssuance(int row, String value, boolean byCode, boolean byExact) throws SQLException, GuanzonException {
//        InventoryBrowse loBrowse = new InventoryBrowse(poGRider, logwrapr);
//        loBrowse.initTransaction();
//        if (!psIndustryCode.isEmpty()) {
//            loBrowse.setIndustry(psIndustryCode);
//        }
//        loBrowse.setCategoryFilters(psCategorCD);
//        loBrowse.setBranch(poGRider.getBranchCode());
//
//        poJSON = new JSONObject();
//
//        poJSON = loBrowse.searchInventoryIssaunce(value, byCode, byExact);
//        System.out.println("result " + (String) poJSON.get("result"));
//        if ("success".equals((String) poJSON.get("result"))) {
//            for (int lnExisting = 0; lnExisting <= paDetail.size() - 1; lnExisting++) {
//                Model_Inventory_Transfer_Detail loExisting = (Model_Inventory_Transfer_Detail) paDetail.get(lnExisting);
//                if (loExisting.getStockId() != null) {
//                    if (loExisting.getStockId().equals(loBrowse.getModelInventory().getStockId())) {
//                        if (!loExisting.getSerialID().isEmpty()) {
//                            if (loBrowse.getModelInventorySerial().getSerialId() != null) {
//                                if (!loExisting.getSerialID().equals(loBrowse.getModelInventorySerial().getSerialId())) {
//                                    continue;
//                                }
//                            }
//                        }
//                        poJSON = new JSONObject();
//                        poJSON.put("result", "error");
//                        poJSON.put("message", "Selected Inventory is already exist!");
//                        return poJSON;
//
//                    }
//                }
//            }
//            getDetail(row).setStockId(loBrowse.getModelInventory().getStockId());
//            if (loBrowse.getModelInventorySerial().getSerialId() != null) {
//                getDetail(row).setSerialID(loBrowse.getModelInventorySerial().getSerialId());
//            }
//
//            getDetail(row).setQuantity(1.00);
//        }
//
//        return poJSON;
//
//    }
//
//    public JSONObject searchDetailBySerial(int row, String value, boolean byCode) throws SQLException, GuanzonException {
//        InventoryBrowse loBrowse = new InventoryBrowse(poGRider, logwrapr);
//        loBrowse.initTransaction();
//        if (!psIndustryCode.isEmpty()) {
//            loBrowse.setIndustry(psIndustryCode);
//        }
//        loBrowse.setCategoryFilters(psCategorCD);
//        loBrowse.setBranch(poGRider.getBranchCode());
//
//        poJSON = new JSONObject();
//
//        poJSON = loBrowse.searchInventorySerialWithStock(value, byCode);
//        System.out.println("result " + (String) poJSON.get("result"));
//        if ("success".equals((String) poJSON.get("result"))) {
//            for (int lnExisting = 0; lnExisting <= paDetail.size() - 1; lnExisting++) {
//                Model_Inventory_Transfer_Detail loExisting = (Model_Inventory_Transfer_Detail) paDetail.get(lnExisting);
//                if (loExisting.getStockId() != null) {
//                    if (loExisting.getStockId().equals(loBrowse.getModelInventory().getStockId())) {
//                        if (!loExisting.getSerialID().isEmpty()) {
//                            if (loBrowse.getModelInventorySerial().getSerialId() != null) {
//                                if (!loExisting.getSerialID().equals(loBrowse.getModelInventorySerial().getSerialId())) {
//                                    continue;
//                                }
//                            }
//                        }
//                        poJSON = new JSONObject();
//                        poJSON.put("result", "error");
//                        poJSON.put("message", "Selected Inventory is already exist!");
//                        return poJSON;
//
//                    }
//                }
//            }
//        }
//
//        getDetail(row).setStockId(loBrowse.getModelInventory().getStockId());
//        if (loBrowse.getModelInventorySerial().getSerialId() != null) {
//            getDetail(row).setSerialID(loBrowse.getModelInventorySerial().getSerialId());
//        }
//
//        getDetail(row).setQuantity(1.00);
//
//        return poJSON;
//
//    }
//
//    public JSONObject searchDetailByBarcode(int row, String value, boolean byCode) throws SQLException, GuanzonException {
//        InventoryBrowse loBrowse = new InventoryBrowse(poGRider, logwrapr);
//        loBrowse.initTransaction();
//        if (!psIndustryCode.isEmpty()) {
//            loBrowse.setIndustry(psIndustryCode);
//        }
//        loBrowse.setCategoryFilters(psCategorCD);
//
//        poJSON = new JSONObject();
//
//        poJSON = loBrowse.searchInventory(value, byCode);
//        System.out.println("result " + (String) poJSON.get("result"));
//
//        if ("success".equals((String) poJSON.get("result"))) {
//            getDetail(row).setOriginalId(loBrowse.getModelInventory().getStockId());
//            return poJSON;
//        }
//        return poJSON;
//
//    }
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

            getMaster().setClusterID(loSubClass.getModel().getTownId());
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

    public JSONObject loadStockTransactionList()
            throws SQLException, GuanzonException, CloneNotSupportedException {

        if (getMaster().getClusterID() == null
                && getMaster().getClusterID().isEmpty()) {
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
        if (!psCategorCD.isEmpty()) {
            lsSQL = MiscUtil.addCondition(lsSQL, "a.sCategrCd = " + SQLUtil.toSQL(psCategorCD));
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

        while (loRS.next()) {
            Model_Inv_Stock_Request_Master loInventoryRequest = new InvWarehouseModels(poGRider).InventoryStockRequestMaster();
            poJSON = loInventoryRequest.openRecord(loRS.getString("sTransNox"));

            if ("success".equals((String) poJSON.get("result"))) {
                paStockMaster.add((Model) loInventoryRequest);
            } else {
                return poJSON;
            }
        }

        poJSON = new JSONObject();
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

        if (psCategorCD == null && psCategorCD.isEmpty()) {
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
