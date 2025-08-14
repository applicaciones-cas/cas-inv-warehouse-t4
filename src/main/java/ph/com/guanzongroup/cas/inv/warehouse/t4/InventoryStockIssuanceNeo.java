package ph.com.guanzongroup.cas.inv.warehouse.t4;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.agent.services.Transaction;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.cas.inv.Inventory;
import org.guanzon.cas.inv.services.InvControllers;
import org.guanzon.cas.parameter.services.ParamModels;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.inv.warehouse.t4.constant.InventoryStockIssuanceStatus;
import ph.com.guanzongroup.cas.inv.warehouse.t4.model.Model_Inventory_Transfer_Detail;
import ph.com.guanzongroup.cas.inv.warehouse.t4.model.Model_Inventory_Transfer_Detail_Expiration;
import ph.com.guanzongroup.cas.inv.warehouse.t4.model.Model_Inventory_Transfer_Master;
import ph.com.guanzongroup.cas.inv.warehouse.t4.model.services.DeliveryIssuanceModels;

public class InventoryStockIssuanceNeo extends Transaction {

    private String psIndustryCode = "";
    private String psCompanyID = "";
    private String psCategorCD = "";
    private List<Model> paMaster;
    public Model poDetailExpiration;
    public List<Model> paDetailExpiration;

    public void setIndustryID(String industryId) {
        psIndustryCode = industryId;
    }

    public void setCompanyID(String companyId) {
        psCompanyID = companyId;
    }

    public void setCategoryID(String categoryId) {
        psCategorCD = categoryId;
    }

    public Model_Inventory_Transfer_Master getMaster() {
        return (Model_Inventory_Transfer_Master) poMaster;
    }

    @SuppressWarnings("unchecked")
    public List<Model_Inventory_Transfer_Master> getMasterList() {
        return (List<Model_Inventory_Transfer_Master>) (List<?>) paMaster;
    }

    public Model_Inventory_Transfer_Master getMaster(int masterRow) {
        return (Model_Inventory_Transfer_Master) paMaster.get(masterRow);

    }

    @SuppressWarnings("unchecked")
    public List<Model_Inventory_Transfer_Detail> getDetailList() {
        return (List<Model_Inventory_Transfer_Detail>) (List<?>) paDetail;
    }

    public Model_Inventory_Transfer_Detail getDetail(int entryNo) {
        if (getMaster().getTransactionNo().isEmpty()
                || getMaster().getIndustryId().isEmpty()) {
            return null;
        }

        if (entryNo <= 0 || entryNo > paDetail.size()) {
            return null;
        }

        Model_Inventory_Transfer_Detail loDetail;

        //find the detail record
        for (int lnCtr = 0; lnCtr <= paDetail.size() - 1; lnCtr++) {
            loDetail = (Model_Inventory_Transfer_Detail) paDetail.get(lnCtr);

            if (loDetail.getEntryNo() == entryNo) {
                return loDetail;
            }
        }

        loDetail = new DeliveryIssuanceModels(poGRider).InventoryTransferDetail();
        loDetail.newRecord();
        loDetail.setTransactionNo(getMaster().getTransactionNo());
        paDetail.add(loDetail);

        return loDetail;
    }

    public Model_Inventory_Transfer_Detail_Expiration getDetailOther(int entryNo) {
        if (getMaster().getTransactionNo().isEmpty()
                || getMaster().getIndustryId().isEmpty()) {
            return null;
        }

        if (entryNo <= 0 || entryNo > paDetailExpiration.size()) {
            return null;
        }

        Model_Inventory_Transfer_Detail_Expiration loDetailExpiration;

        //find the detail record
        for (int lnCtr = 0; lnCtr <= paDetail.size() - 1; lnCtr++) {
            loDetailExpiration = (Model_Inventory_Transfer_Detail_Expiration) paDetailExpiration.get(lnCtr);

            if (loDetailExpiration.getEntryNo() == entryNo) {
                return loDetailExpiration;
            }
        }

        loDetailExpiration = new DeliveryIssuanceModels(poGRider).InventoryTransferDetailExpiration();
        loDetailExpiration.newRecord();
        loDetailExpiration.setTransactionNo(getMaster().getTransactionNo());
        paDetailExpiration.add(loDetailExpiration);

        return loDetailExpiration;
    }

    @SuppressWarnings("unchecked")
    public List<Model_Inventory_Transfer_Detail_Expiration> getDetailListOther() {
        return (List<Model_Inventory_Transfer_Detail_Expiration>) (List<?>) paDetailExpiration;
    }

    public JSONObject initTransaction() throws GuanzonException, SQLException {
        SOURCE_CODE = "Dlvr";

        poMaster = new DeliveryIssuanceModels(poGRider).InventoryTransferMaster();
        poDetail = new DeliveryIssuanceModels(poGRider).InventoryTransferDetail();
        poDetailExpiration = new DeliveryIssuanceModels(poGRider).InventoryTransferDetailExpiration();
        paMaster = new ArrayList<Model>();
        initSQL();

        return super.initialize();
    }

    @Override
    public void initSQL() {
        SQL_BROWSE = " SELECT "
                + " c.sClustrID"
                + ", a.sBranchCd"
                + ", a.sIndstCdx"
                + ", a.sCategrCd"
                + ", a.sTransNox"
                + " FROM Inv_Stock_Request_Master a"
                + "     LEFT JOIN Branch_Others b ON a.sBranchCD = b.sBranchCd"
                + "     LEFT JOIN Branch_Cluster c ON b.sClustrID = c.sClustrID"
                + "         WHERE a.cTranStat = " + SQLUtil.toSQL(0);
    }

    public JSONObject OpenTransaction(String transactionNo) throws CloneNotSupportedException, SQLException, GuanzonException {
        return openTransaction(transactionNo);
    }

    public JSONObject SaveTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
        return saveTransaction();
    }

    public JSONObject UpdateTransaction() {
        return updateTransaction();
    }

    @Override
    protected JSONObject willSave() {
        poJSON = new JSONObject();

        poJSON = isEntryOkay(InventoryStockIssuanceStatus.OPEN);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        //assign values needed
        poJSON.put("result", "success");
        return poJSON;

    }

    @Override
    protected JSONObject isEntryOkay(String status) {
        poJSON = new JSONObject();
        GValidator loValidator = InventoryStockRequestApprovalValidatorFactory.make(getMaster().getIndustryId());

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
                }
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

    public JSONObject CancelTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid Edit Mode");
            return poJSON;
        }

        if (InventoryStockIssuanceStatus.CONFIRMED.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already confirmed.");
            return poJSON;
        }

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

            poJSON = ShowDialogFX.Search(poGRider,
                    lsSQL,
                    value,
                    "Transaction No»Date»Schedule Date",
                    "sTransNox»dTransact»dSchedule",
                    "sTransNox»dTransact»dSchedule",
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
            Logger.getLogger(InventoryStockIssuanceNeo.class
                    .getName()).log(Level.SEVERE, null, ex);
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }

    public JSONObject searchDetailByInventory(int row, String value, boolean byCode) throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        Inventory loSubClass = new InvControllers(poGRider, logwrapr).Inventory();

        if (getMaster().getIndustryId() == null || "".equals(getMaster().getIndustryId())) {
            poJSON.put("result", "error");
            poJSON.put("message", "Industry is not set.");
            return poJSON;
        }

        loSubClass.getModel().setIndustryCode(psIndustryCode);
        if (!psIndustryCode.isEmpty()) {
            poJSON = loSubClass.searchRecord(value, byCode, "", "", psIndustryCode);
        }
        System.out.println("result " + (String) poJSON.get("result"));
        if ("success".equals((String) poJSON.get("result"))) {
            for (int lnExisting = 0; lnExisting <= paDetail.size() - 1; lnExisting++) {
                if (((Model_Delivery_Schedule_Detail) paDetail.get(lnExisting)).getClusterID()
                        == loSubClass.getModel().getClusterID()
                        && loSubClass.getModel().getClusterID() != null
                        && !loSubClass.getModel().getClusterID().isEmpty()) {
                    poJSON = new JSONObject();
                    poJSON.put("result", "error");
                    poJSON.put("message", "Selected Cluster ID is already exist!");
                    return poJSON;

                }

            }
            getDetail(row).setClusterID(loSubClass.getModel().getClusterID());
        }
        return poJSON;

    }

    public JSONObject loadTransactionList()
            throws SQLException, GuanzonException, CloneNotSupportedException {

        if (psCategorCD.isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "Category is not set");
            return poJSON;

        }
        paMaster.clear();
        initSQL();
        String lsSQL = SQL_BROWSE;

        if (!psIndustryCode.isEmpty()) {
            lsSQL = MiscUtil.addCondition(lsSQL, "a.sIndstCdx = " + SQLUtil.toSQL(psIndustryCode));
        }
        if (!psCategorCD.isEmpty()) {
            lsSQL = MiscUtil.addCondition(lsSQL, "a.sCategrCd = " + SQLUtil.toSQL(psCategorCD));
        }
        ResultSet loRS = poGRider.executeQuery(lsSQL);

        if (MiscUtil.RecordCount(loRS)
                <= 0) {
            poJSON.put("result", "error");
            poJSON.put("message", "No record found.");
            return poJSON;
        }

        while (loRS.next()) {
            Model_Inv_Stock_Request_Master loInventoryStockRequest = new InvWarehouseModels(poGRider).InventoryStockRequestMaster();
            poJSON = loInventoryStockRequest.openRecord(loRS.getString("sTransNox"));

            if ("success".equals((String) poJSON.get("result"))) {
                paMaster.add((Model) loInventoryStockRequest);
            } else {
                return poJSON;
            }
        }

        poJSON = new JSONObject();
        poJSON.put(
                "result", "success");
        return poJSON;
    }

    public JSONObject getCategory() throws SQLException, GuanzonException {
        if (!"".equals(psIndustryCode)) {

            String lsSQL = "SELECT "
                    + " sCategrCd"
                    + ", sDescript"
                    + ", sIndstCdx"
                    + ", sInvTypCd"
                    + ", cRecdStat "
                    + " FROM Category "
                    + "  WHERE cRecdStat = " + SQLUtil.toSQL(RecordStatus.ACTIVE)
                    + " AND sIndstCdx = " + SQLUtil.toSQL(psIndustryCode);

            ResultSet loRS = poGRider.executeQuery(lsSQL);

            if (MiscUtil.RecordCount(loRS)
                    <= 0) {
                poJSON.put("result", "error");
                poJSON.put("message", "No record found.");
                return poJSON;
            }
            loRS.beforeFirst();
            if (loRS.next()) {
                Model_Category loCategory = new ParamModels(poGRider).Category();
                poJSON = loCategory.openRecord(loRS.getString("sCategrCd"));
                if ("success".equals((String) poJSON.get("result"))) {
                    psCategorCD = loRS.getString("sCategrCd");
                    getMaster().setCategoryId(psCategorCD);
                    return poJSON;
                } else {
                    poJSON.put("result", "error");
                    poJSON.put("message", "No record found.");
                    return poJSON;
                }
            }
        } else {
            //General
            psCategorCD = "0007";

        }
        poJSON.put("result", "success");
        poJSON.put("message", "Industry is General");
        return poJSON;

    }
}
