package ph.com.guanzongroup.cas.inv.warehouse.t4;

import java.sql.ResultSet;
import java.sql.SQLException;
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
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.inv.warehouse.t4.model.Model_Delivery_Schedule_Detail;
import ph.com.guanzongroup.cas.inv.warehouse.t4.model.Model_Delivery_Schedule_Master;
import ph.com.guanzongroup.cas.inv.warehouse.t4.model.services.DeliveryScheduleModels;

public class DeliverySchedule extends Transaction {

    private String psIndustryID = "";
    private String psCompanyID = "";
    private String psCategorCD = "";

    public void setIndustryID(String industryId) {
        psIndustryID = industryId;
    }

    public void setCompanyID(String companyId) {
        psCompanyID = companyId;
    }

    public void setCategoryID(String categoryId) {
        psCategorCD = categoryId;
    }

    public Model_Delivery_Schedule_Master getMaster() {
        return (Model_Delivery_Schedule_Master) poMaster;
    }

    @SuppressWarnings("unchecked")
    public List<Model_Delivery_Schedule_Detail> getDetailList() {
        return (List<Model_Delivery_Schedule_Detail>) (List<?>) paDetail;
    }

    public Model_Delivery_Schedule_Detail getDetail(int clusterRow) {
        if (getMaster().getTransactionNo().isEmpty())
//                || getMaster().getIndustryId().isEmpty()) 
                {
            return null;
        }

        Model_Delivery_Schedule_Detail loDetail;

        // If index is invalid or out of range, add a new detail
        if (clusterRow < 0 || clusterRow >= paDetail.size()) {
            loDetail = new DeliveryScheduleModels(poGRider).DeliveryScheduleDetail();
            loDetail.newRecord();
            loDetail.setTransactionNo(getMaster().getTransactionNo());

            paDetail.add(loDetail);
            return loDetail;
        }

        // Safe to get the selected detail
        Model_Delivery_Schedule_Detail loDetailSelected = (Model_Delivery_Schedule_Detail) paDetail.get(clusterRow);

        // Find a match by ClusterID
        for (int lnCtr = 0; lnCtr <= paDetail.size() - 1; lnCtr++) {
            loDetail = (Model_Delivery_Schedule_Detail) paDetail.get(lnCtr);

            if (loDetail.getClusterID() == loDetailSelected.getClusterID()) {
                return loDetail;
            }
        }

        // No match found — create new
        loDetail = new DeliveryScheduleModels(poGRider).DeliveryScheduleDetail();
        loDetail.newRecord();
        loDetail.setTransactionNo(getMaster().getTransactionNo());

        paDetail.add(loDetail);
        return loDetail;
    }

    public JSONObject initTransaction() throws GuanzonException, SQLException {
        SOURCE_CODE = "Tbln";

        poMaster = new DeliveryScheduleModels(poGRider).DeliverySchedule();
        poDetail = new DeliveryScheduleModels(poGRider).DeliveryScheduleDetail();
        return super.initialize();
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
                    byExact ? (byCode ? 0 : 1) : 3);

            if (poJSON != null) {
                return openTransaction((String) poJSON.get("sTransNox"));

            } else {
                poJSON = new JSONObject();
                poJSON.put("result", "error");
                poJSON.put("message", "No record loaded.");
                return poJSON;
            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            Logger.getLogger(DeliverySchedule.class.getName()).log(Level.SEVERE, null, ex);
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }

    public JSONObject openTransaction(String transactionNo) throws CloneNotSupportedException, SQLException, GuanzonException {
        if (transactionNo.isEmpty()) {

            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction must not empty.");
            return poJSON;
        }

        poJSON = poMaster.openRecord(transactionNo);

        if (!"success".equals((String) poJSON.get("result"))) {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "Unable to Open Transaction Record.");
            return poJSON;
        }
        paDetail.clear();

        String lsSQL = "SELECT * FROM " + poDetail.getTable()
                + " WHERE sTransNox = " + SQLUtil.toSQL(transactionNo);

        ResultSet loRS = poGRider.executeQuery(lsSQL);

        if (MiscUtil.RecordCount(loRS) > 0) {
            while (loRS.next()) {
                Model loDetail = (Model) poDetail.clone();
                loDetail.newRecord();
                poJSON = loDetail.openRecord(transactionNo, loRS.getInt("sClustrID"));

                if (!"success".equals((String) poJSON.get("result"))) {
                    poJSON.put("message", "Unable to open transaction detail record.");
                    clear();
                    return poJSON;
                }
                loDetail.updateRecord();

                paDetail.add(loDetail);
            }
        }
        poEvent = new JSONObject();
        poEvent.put("event", "UPDATE");

        pnEditMode = EditMode.READY;
        pbRecordExist = true;

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    public JSONObject newTransaction() throws CloneNotSupportedException {
        if (!pbInitTran) {
            poJSON.put("result", "error");
            poJSON.put("message", "Object is not initialized.");
            return poJSON;
        }

        poMaster.initialize();
        poMaster.newRecord();

        poDetail.initialize();
        poDetail.newRecord();

        paDetail.clear();
        paDetail.add(poDetail);

        poJSON = initFields();
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        pnEditMode = EditMode.ADDNEW;

        poEvent = new JSONObject();
        poEvent.put("event", "ADD NEW");

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    public JSONObject saveTransaction() throws CloneNotSupportedException, SQLException, GuanzonException {
        poJSON = new JSONObject();

        if (!pbInitTran) {
            poJSON.put("result", "error");
            poJSON.put("message", "Object is not initialized.");
            return poJSON;
        }

        if (pnEditMode == EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "Saving of unmodified transaction is not allowed.");
            return poJSON;
        }

        poJSON = willSave();
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        if (getEditMode() == EditMode.ADDNEW) {
            pdModified = poGRider.getServerDate();
            //poMaster.setValue("sModified", poGRider.Encrypt(poGRider.getUserID()));
        }

        poJSON = save();

        if (!pbWthParent) {
            poGRider.beginTrans((String) poEvent.get("event"),
                    poMaster.getTable(),
                    SOURCE_CODE,
                    String.valueOf(poMaster.getValue(1)));
        }

        if ("success".equals((String) poJSON.get("result"))) {
            //save master and detail
            if (pbVerifyEntryNo) {
                poMaster.setValue("nEntryNox", paDetail.size());
            }

            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                poMaster.setValue("dModified", pdModified);
                poJSON = poMaster.saveRecord();

                if ("error".equals((String) poJSON.get("result"))) {
                    if (!pbWthParent) {
                        poGRider.rollbackTrans();
                    }
                    return poJSON;
                }

                for (int lnCtr = 0; lnCtr <= paDetail.size() - 1; lnCtr++) {
                    //paDetail.get(lnCtr).setValue("dModified", pdModified);
                    poJSON = paDetail.get(lnCtr).saveRecord();

                    if ("error".equals((String) poJSON.get("result"))) {
                        if (!pbWthParent) {
                            poGRider.rollbackTrans();
                        }
                        return poJSON;
                    }
                }
            } else {
                poJSON.put("result", "error");
                poJSON.put("message", "Edit mode is not allowed to save transaction.");
                return poJSON;
            }
        } else {
            if (!pbWthParent) {
                poGRider.rollbackTrans();
            }

            return poJSON;
        }

        if (!pbWthParent) {
            poGRider.commitTrans();
        }

        pnEditMode = EditMode.UNKNOWN;
        pbRecordExist = true;

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "Transaction saved successfully.");
        return poJSON;
    }

    public JSONObject UpdateTransaction() {
        return updateTransaction();
    }

}
