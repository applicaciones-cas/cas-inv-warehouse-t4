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
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.appdriver.iface.GValidator;
import org.guanzon.cas.inv.warehouse.model.Model_Inv_Stock_Request_Detail;
import org.guanzon.cas.inv.warehouse.model.Model_Inv_Stock_Request_Master;
import org.guanzon.cas.inv.warehouse.services.InvWarehouseModels;
import org.guanzon.cas.inv.warehouse.status.StockRequestStatus;
import org.guanzon.cas.parameter.model.Model_Category;
import org.guanzon.cas.parameter.services.ParamModels;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.inv.warehouse.t4.model.services.DeliveryScheduleModels;
import ph.com.guanzongroup.cas.inv.warehouse.t4.parameter.BranchCluster;
import ph.com.guanzongroup.cas.inv.warehouse.t4.parameter.model.Model_Branch_Cluster;
import ph.com.guanzongroup.cas.inv.warehouse.t4.parameter.services.DeliveryParamController;
import ph.com.guanzongroup.cas.inv.warehouse.t4.parameter.services.DeliveryParamModels;
import ph.com.guanzongroup.cas.inv.warehouse.t4.validators.DeliveryScheduleValidatorFactory;

public class InventoryRequestApproval extends Transaction {

    private String psIndustryCode = "";
    private String psCompanyID = "";
    private String psCategorCD = "";
    private List<Model> paMaster;
    public Model poCluster;

    public void setIndustryID(String industryId) {
        psIndustryCode = industryId;
    }

    public void setCompanyID(String companyId) {
        psCompanyID = companyId;
    }

    public void setCategoryID(String categoryId) {
        psCategorCD = categoryId;
    }

    public Model_Branch_Cluster getBranchCluster() {
        return (Model_Branch_Cluster) poCluster;
    }

    public Model_Inv_Stock_Request_Master getMaster() {
        return (Model_Inv_Stock_Request_Master) poMaster;
    }

    @SuppressWarnings("unchecked")
    public List<Model_Inv_Stock_Request_Master> getMasterList() {
        return (List<Model_Inv_Stock_Request_Master>) (List<?>) paMaster;
    }

    public Model_Inv_Stock_Request_Master getMaster(int masterRow) {
        return (Model_Inv_Stock_Request_Master) paMaster.get(masterRow);

    }

    @SuppressWarnings("unchecked")
    public List<Model_Inv_Stock_Request_Detail> getDetailList() {
        return (List<Model_Inv_Stock_Request_Detail>) (List<?>) paDetail;
    }

    public Model_Inv_Stock_Request_Detail getDetail(int entryNo) {
        if (getMaster().getTransactionNo().isEmpty()
                || getMaster().getIndustryId().isEmpty()) {
            return null;
        }

        if (entryNo < 0 || entryNo > paDetail.size() - 1) {
            return null;
        }

        Model_Inv_Stock_Request_Detail loDetail;

        //find the detail record
        for (int lnCtr = 0; lnCtr <= paDetail.size() - 1; lnCtr++) {
            loDetail = (Model_Inv_Stock_Request_Detail) paDetail.get(lnCtr);

            if (loDetail.getEntryNumber() == entryNo) {
                return loDetail;
            }
        }
        return null;
    }

    public JSONObject initTransaction() throws GuanzonException, SQLException {
        SOURCE_CODE = "";

        poMaster = new DeliveryScheduleModels(poGRider).DeliverySchedule();
        poDetail = new DeliveryScheduleModels(poGRider).DeliveryScheduleDetail();
        poCluster = new DeliveryParamModels(poGRider).BranchCluster();
        paMaster = new ArrayList<Model>();

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
                + "         WHERE a.cTranStat = " + SQLUtil.toSQL(StockRequestStatus.CONFIRMED);
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
    protected JSONObject isEntryOkay(String status) {
        GValidator loValidator = DeliveryScheduleValidatorFactory.make(getMaster().getIndustryId());

        loValidator.setApplicationDriver(poGRider);
        loValidator.setTransactionStatus(status);
        loValidator.setMaster(poMaster);
//        loValidator.setDetail(paDetail);

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

    public JSONObject searchClusterBranch(int row, String value, boolean byCode) throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        BranchCluster loSubClass = new DeliveryParamController(poGRider, logwrapr).BranchCluster();

        if (psIndustryCode == null && "".equals(psIndustryCode)) {
            poJSON.put("result", "error");
            poJSON.put("message", "Industry is not set.");
            return poJSON;
        }

        loSubClass.getModel().setIndustryCode(psIndustryCode);

        poJSON = loSubClass.searchRecordbyIndustry(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            poCluster = loSubClass.getModel();
            return poJSON;
        }
        return poJSON;

    }

    public JSONObject loadTransactionList()
            throws SQLException, GuanzonException, CloneNotSupportedException {
        if (poCluster == null) {
            poJSON.put("result", "error");
            poJSON.put("message", "Branch Cluster is not set");
            return poJSON;
        }
        if ("".equals(poCluster.getValue("sClustrID"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Branch Cluster is not set");
            return poJSON;
        }

        if (psIndustryCode.isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "Industry is not set");
            return poJSON;

        }
        paMaster.clear();
        initSQL();
        String lsSQL = SQL_BROWSE;

        if (poCluster != null && !"".equals(poCluster.getValue("sClustrID"))) {
            lsSQL = MiscUtil.addCondition(lsSQL, "c.sClustrID = " + SQLUtil.toSQL(poCluster.getValue("sClustrID")));
        }
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

}
