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
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.appdriver.iface.GValidator;
import org.guanzon.cas.inv.warehouse.status.StockRequestStatus;
import org.guanzon.cas.parameter.Branch;
import org.guanzon.cas.parameter.model.Model_Branch;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.guanzon.cas.parameter.services.ParamModels;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.inv.warehouse.t4.constant.InventoryStockIssuancePrint;
import ph.com.guanzongroup.cas.inv.warehouse.t4.constant.InventoryStockIssuanceStatus;
import ph.com.guanzongroup.cas.inv.warehouse.t4.model.Model_Inventory_Transfer_Detail;
import ph.com.guanzongroup.cas.inv.warehouse.t4.model.Model_Inventory_Transfer_Detail_Expiration;
import ph.com.guanzongroup.cas.inv.warehouse.t4.model.Model_Inventory_Transfer_Master;
import ph.com.guanzongroup.cas.inv.warehouse.t4.model.services.DeliveryIssuanceModels;
import ph.com.guanzongroup.cas.inv.warehouse.t4.parameter.InventoryBrowse;
import ph.com.guanzongroup.cas.inv.warehouse.t4.report.ReportUtil;
import ph.com.guanzongroup.cas.inv.warehouse.t4.report.ReportUtilListener;
import ph.com.guanzongroup.cas.inv.warehouse.t4.validators.InventoryIssuanceValidatorFactory;

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
        if (getMaster().getTransactionNo().isEmpty()) {
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
        paDetail = new ArrayList<Model>();
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

    public JSONObject NewTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
        return newTransaction();
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

    public JSONObject searchDetailByIssuance(int row, String value, boolean byCode) throws SQLException, GuanzonException {
        InventoryBrowse loBrowse = new InventoryBrowse(poGRider, logwrapr);
        loBrowse.initTransaction();
        if (!psIndustryCode.isEmpty()) {
            loBrowse.setIndustry(psIndustryCode);
        }
        loBrowse.setCategoryFilters(getCategory());
        loBrowse.setBranch(poGRider.getBranchCode());

        poJSON = new JSONObject();

        poJSON = loBrowse.searchInventoryIssaunce(value, byCode);
        System.out.println("result " + (String) poJSON.get("result"));
        if ("success".equals((String) poJSON.get("result"))) {
            for (int lnExisting = 0; lnExisting <= paDetail.size() - 1; lnExisting++) {
                Model_Inventory_Transfer_Detail loExisting = (Model_Inventory_Transfer_Detail) paDetail.get(lnExisting);
                if (loExisting.getStockId() == loBrowse.getModelInventory().getStockId()) {
                    if (!loExisting.getSerialID().isEmpty()) {
                        if (loBrowse.getModelInventorySerial().getSerialId() != null) {
                            if (loExisting.getSerialID() != loBrowse.getModelInventorySerial().getSerialId()) {
                                continue;
                            }
                        }
                    }
                    poJSON = new JSONObject();
                    poJSON.put("result", "error");
                    poJSON.put("message", "Selected Inventory is already exist!");
                    return poJSON;

                }
            }
            getDetail(row).setStockId(loBrowse.getModelInventory().getStockId());
            if (loBrowse.getModelInventorySerial().getSerialId() != null) {
                getDetail(row).setSerialID(loBrowse.getModelInventorySerial().getSerialId());
            }

            getDetail(row).setQuantity(1.00);
        }

        return poJSON;

    }

    public JSONObject searchDetailByIssuance(int row, String value, boolean byCode, boolean byExact) throws SQLException, GuanzonException {
        InventoryBrowse loBrowse = new InventoryBrowse(poGRider, logwrapr);
        loBrowse.initTransaction();
        if (!psIndustryCode.isEmpty()) {
            loBrowse.setIndustry(psIndustryCode);
        }
        loBrowse.setCategoryFilters(getCategory());
        loBrowse.setBranch(poGRider.getBranchCode());

        poJSON = new JSONObject();

        poJSON = loBrowse.searchInventoryIssaunce(value, byCode, byExact);
        System.out.println("result " + (String) poJSON.get("result"));
        if ("success".equals((String) poJSON.get("result"))) {
            for (int lnExisting = 0; lnExisting <= paDetail.size() - 1; lnExisting++) {
                Model_Inventory_Transfer_Detail loExisting = (Model_Inventory_Transfer_Detail) paDetail.get(lnExisting);
                if (loExisting.getStockId() == loBrowse.getModelInventory().getStockId()) {
                    if (!loExisting.getSerialID().isEmpty()) {
                        if (loBrowse.getModelInventorySerial().getSerialId() != null) {
                            if (loExisting.getSerialID() != loBrowse.getModelInventorySerial().getSerialId()) {
                                continue;
                            }
                        }
                    }
                    poJSON = new JSONObject();
                    poJSON.put("result", "error");
                    poJSON.put("message", "Selected Inventory is already exist!");
                    return poJSON;

                }
            }
            getDetail(row).setStockId(loBrowse.getModelInventory().getStockId());
            if (loBrowse.getModelInventorySerial().getSerialId() != null) {
                getDetail(row).setSerialID(loBrowse.getModelInventorySerial().getSerialId());
            }

            getDetail(row).setQuantity(1.00);
        }

        return poJSON;

    }

    public JSONObject searchDetailBySerial(int row, String value, boolean byCode) throws SQLException, GuanzonException {
        InventoryBrowse loBrowse = new InventoryBrowse(poGRider, logwrapr);
        loBrowse.initTransaction();
        if (!psIndustryCode.isEmpty()) {
            loBrowse.setIndustry(psIndustryCode);
        }
        loBrowse.setCategoryFilters(getCategory());
        loBrowse.setBranch(poGRider.getBranchCode());

        poJSON = new JSONObject();

        poJSON = loBrowse.searchInventorySerialWithStock(value, byCode);
        System.out.println("result " + (String) poJSON.get("result"));
        if ("success".equals((String) poJSON.get("result"))) {
            for (int lnExisting = 0; lnExisting <= paDetail.size() - 1; lnExisting++) {
                Model_Inventory_Transfer_Detail loExisting = (Model_Inventory_Transfer_Detail) paDetail.get(lnExisting);
                if (loExisting.getStockId() == loBrowse.getModelInventory().getStockId()) {
                    if (!loExisting.getSerialID().isEmpty()) {
                        if (loBrowse.getModelInventorySerial().getSerialId() != null) {
                            if (loExisting.getSerialID() != loBrowse.getModelInventorySerial().getSerialId()) {
                                continue;
                            }
                        }
                    }
                    poJSON = new JSONObject();
                    poJSON.put("result", "error");
                    poJSON.put("message", "Selected Inventory is already exist!");
                    return poJSON;

                }
            }
        }

        getDetail(row).setStockId(loBrowse.getModelInventory().getStockId());
        if (loBrowse.getModelInventorySerial().getSerialId() != null) {
            getDetail(row).setSerialID(loBrowse.getModelInventorySerial().getSerialId());
        }

        getDetail(row).setQuantity(1.00);

        return poJSON;

    }

    public JSONObject searchDetailByBarcode(int row, String value, boolean byCode) throws SQLException, GuanzonException {
        InventoryBrowse loBrowse = new InventoryBrowse(poGRider, logwrapr);
        loBrowse.initTransaction();
        if (!psIndustryCode.isEmpty()) {
            loBrowse.setIndustry(psIndustryCode);
        }
        loBrowse.setCategoryFilters(getCategory());

        poJSON = new JSONObject();

        poJSON = loBrowse.searchInventory(value, byCode);
        System.out.println("result " + (String) poJSON.get("result"));

        if ("success".equals((String) poJSON.get("result"))) {
            getDetail(row).setOriginalId(loBrowse.getModelInventory().getStockId());
            return poJSON;
        }
        return poJSON;

    }

    public JSONObject searchTransactionDestination(String value, boolean byCode) throws SQLException, GuanzonException {
        Model_Branch loBrowse = new ParamModels(poGRider).Branch();

        String lsSQL = "SELECT sBranchCd, sBranchNm FROM Branch";
        lsSQL = MiscUtil.addCondition(lsSQL, "sIndstCdx = " + SQLUtil.toSQL(psIndustryCode));

        poJSON = ShowDialogFX.Search(poGRider,
                lsSQL,
                value,
                "Code»Branch Name",
                "sBranchCd»sBranchNm",
                "sBranchCd»sBranchNm",
                byCode ? 0 : 1);

        if (poJSON != null) {
            poJSON = loBrowse.openRecord((String) this.poJSON.get("sBranchCd"));
            System.out.println("result " + (String) poJSON.get("result"));

            if ("success".equals((String) poJSON.get("result"))) {
                getMaster().setDestination(loBrowse.getBranchCode());
                return poJSON;
            }

        }
        this.poJSON = new JSONObject();
        this.poJSON.put("result", "error");
        this.poJSON.put("message", "No record loaded.");
        return this.poJSON;

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
            Model_Inventory_Transfer_Master loInventoryIssuance = new DeliveryIssuanceModels(poGRider).InventoryTransferMaster();
            poJSON = loInventoryIssuance.openRecord(loRS.getString("sTransNox"));

            if ("success".equals((String) poJSON.get("result"))) {
                paMaster.add((Model) loInventoryIssuance);
            } else {
                return poJSON;
            }
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        return poJSON;
    }

    private String getCategory() {
        String lsCategory;
        switch (psIndustryCode) {
            case "01"://CP
                lsCategory = "0001";
                break;
            case "02"://MC
                lsCategory = "0003»0004";
                break;
            case "03"://Car
                lsCategory = "0005»0006";
                break;
            case "04"://Monarch
                lsCategory = "0009";
                break;
            case "05"://LP
                lsCategory = "0008";
                break;
            case "07"://Appliance
                lsCategory = "0002";
                break;
            default://Appliance
                lsCategory = "0007";
                break;

        }

        return lsCategory;
    }

    public JSONObject printRecord() throws SQLException, JRException, CloneNotSupportedException, GuanzonException {

        poJSON = new JSONObject();

        poJSON = isEntryOkay(StockRequestStatus.CONFIRMED);
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
                    if (!isJSONSuccess(PrintTransaction(), "Print Record",
                            "Initialize Record Print! ")) {
                        return;

                    }

                    if (!isJSONSuccess(ProcessTransaction(), "Print Record",
                            "Initialize Record Print! ")) {
                    }

                    poReportJasper.CloseReportUtil();

                } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                    Logger.getLogger(InventoryRequestApproval.class.getName()).log(Level.SEVERE, null, ex);
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
        poReportJasper.addParameter("DatePrinted", SQLUtil.dateFormat(poGRider.getServerDate(), SQLUtil.FORMAT_TIMESTAMP));

        poReportJasper.addParameter("watermarkImagePath", poGRider.getReportPath() + "images\\approved.png");

        poReportJasper.setReportName("InventoryRequestApproval");
        poReportJasper.setJasperPath(InventoryStockIssuancePrint.getJasperReport(psIndustryCode));

        //process by ResultSet
        String lsSQL = InventoryStockIssuancePrint.PrintRecordQuery();
        lsSQL = MiscUtil.addCondition(lsSQL, "InventoryStockRequestMaster.sTransNox = " + SQLUtil.toSQL(getMaster().getTransactionNo()));

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

    private JSONObject PrintTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        poJSON = OpenTransaction(getMaster().getTransactionNo());
        if ("error".equals((String) poJSON.get("result"))) {
            System.out.println("Print Record open transaction : " + (String) poJSON.get("message"));
            return poJSON;
        }

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (StockRequestStatus.PROCESSED.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already Processed.");
            return poJSON;
        }

        if (StockRequestStatus.CONFIRMED.equals((String) poMaster.getValue("cProcessd"))) {
            poJSON.put("result", "success");
            poJSON.put("message", "Transaction Printed successfully.");
            return poJSON;
        }
        //validator
        poJSON = isEntryOkay(StockRequestStatus.PROCESSED);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poGRider.beginTrans("UPDATE STATUS", "Process Transaction Print Tag", SOURCE_CODE, getMaster().getTransactionNo());

        String lsSQL = "UPDATE "
                + poMaster.getTable()
                + " SET   cProcessd = " + SQLUtil.toSQL(StockRequestStatus.CONFIRMED)
                + " WHERE sTransNox = " + SQLUtil.toSQL(getMaster().getTransactionNo());

        Long lnResult = poGRider.executeQuery(lsSQL,
                poMaster.getTable(),
                poGRider.getBranchCode(), "", "");
        if (lnResult <= 0L) {
            poGRider.rollbackTrans();

            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "Error updating the transaction status.");
            return poJSON;
        }

        poGRider.commitTrans();

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "Transaction Printed successfully.");

        return poJSON;
    }

    public JSONObject ProcessTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid Edit Mode");
            return poJSON;
        }

        if (StockRequestStatus.PROCESSED.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
//            poJSON.put("message", "Transaction was already processed.");
            return poJSON;
        }

        if (StockRequestStatus.CANCELLED.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
//            poJSON.put("message", "Transaction was already cancelled.");
            return poJSON;
        }

        if (StockRequestStatus.VOID.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
//            poJSON.put("message", "Transaction was already voided.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(StockRequestStatus.PROCESSED);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poGRider.beginTrans("UPDATE STATUS", "ProcessTransaction", SOURCE_CODE, getMaster().getTransactionNo());

        poJSON = statusChange(poMaster.getTable(),
                (String) poMaster.getValue("sTransNox"),
                "ProcessTransaction",
                StockRequestStatus.PROCESSED,
                false, true);
        if ("error".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }

        poGRider.commitTrans();

        poJSON = new JSONObject();
        poJSON.put("result", "success");
//        poJSON.put("message", "Transaction processed successfully.");

        return poJSON;
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
