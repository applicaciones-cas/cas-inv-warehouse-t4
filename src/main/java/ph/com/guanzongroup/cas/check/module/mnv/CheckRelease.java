/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ph.com.guanzongroup.cas.check.module.mnv;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import org.guanzon.appdriver.iface.GValidator;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.model.Model_Check_Payments;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;
import ph.com.guanzongroup.cas.check.module.mnv.constant.CheckDepositStatus;
import ph.com.guanzongroup.cas.check.module.mnv.constant.CheckReleaseRecords;
import ph.com.guanzongroup.cas.check.module.mnv.constant.CheckReleaseStatus;
import ph.com.guanzongroup.cas.check.module.mnv.models.Model_Check_Deposit_Detail;
import ph.com.guanzongroup.cas.check.module.mnv.models.Model_Check_Release_Detail;
import ph.com.guanzongroup.cas.check.module.mnv.models.Model_Check_Release_Master;
import ph.com.guanzongroup.cas.check.module.mnv.services.CheckModels;
import ph.com.guanzongroup.cas.check.module.mnv.validator.CheckReleaseValidatorFactory;

/**
 *
 * @author User
 */
public class CheckRelease extends Transaction{
    
    private String psIndustryCode = "";
    private String psApprovalUser = "";
    private List<Model> paCheckList;
    private List<Model> paDetail;
    
    public void setIndustryID(String industryId) {
        psIndustryCode = industryId;
    }
    
    public Model_Check_Release_Master GetMaster(){
        return (Model_Check_Release_Master) poMaster;
    }
    
    public Model_Check_Release_Detail GetDetail(){
        return (Model_Check_Release_Detail) poDetail;
    }
    
    public List<Model_Check_Release_Detail> GetDetailList(){
        return (List<Model_Check_Release_Detail>) (List<?>) paDetail;
    }
    
    public Model_Check_Release_Detail GetDetail(int fnRow){
        return (Model_Check_Release_Detail) paDetail.get(fnRow);
    }
    
    @SuppressWarnings("unchecked")
    public List<Model_Check_Payments> GetCheckPaymentList() {
        return (List<Model_Check_Payments>) (List<?>) paCheckList;
    }
    
    @Override
    protected JSONObject willSave() throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        poJSON = isEntryOkay(CheckReleaseStatus.OPEN);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        int lnDetailCount = 0;
        double lnTotalAmount = 0;

        //assign values needed
        for (int lnCtr = 0; lnCtr < paDetail.size(); lnCtr++) {
            
            Model_Check_Release_Detail loDetail = (Model_Check_Release_Detail) paDetail.get(lnCtr);
            
            //null entries, disregard
            if (loDetail == null) {
                paDetail.remove(lnCtr);
            } else {
                
                //empty source no, disregard
                if (loDetail.getSourceNo() == null || loDetail.getSourceNo().isEmpty()) {
                    paDetail.remove(lnCtr);
                    continue;
                }
                
                //remove zero amounts on detail
                if (loDetail.CheckPayment().getAmount()<= 0.00) {
                    paDetail.remove(lnCtr);
                }
                lnDetailCount++;
                loDetail.setTransactionNo(GetMaster().getTransactionNo());
                loDetail.setEntryNo(lnDetailCount);
                lnTotalAmount += loDetail.CheckPayment().getAmount();
                System.out.println("DETAL : " + loDetail.getEntryNo());
            }
        }
        
        GetMaster().setEntryNo(lnDetailCount);
        System.out.println("GetMaster : " + GetMaster().getEntryNo());
        GetMaster().setTransactionTotal(lnTotalAmount);
        
        pdModified = poGRider.getServerDate();

        poJSON.put("result", "success");
        return poJSON;

    }
    
    @Override
    protected JSONObject isEntryOkay(String status) {
        psApprovalUser = "";

        poJSON = new JSONObject();
        GValidator loValidator = CheckReleaseValidatorFactory.make(GetMaster().getIndustryId());

        //initialize params for app validator
        loValidator.setApplicationDriver(poGRider);
        loValidator.setTransactionStatus(status);
        loValidator.setMaster(poMaster);
        
        ArrayList laDetailList = new ArrayList<>(GetDetailList());
        loValidator.setDetail(laDetailList);

        poJSON = loValidator.validate();
        
        //if validator requires approval
        if (poJSON.containsKey("isRequiredApproval") && Boolean.TRUE.equals(poJSON.get("isRequiredApproval"))) {
            
            //check user rights for approval
            if (poGRider.getUserLevel() <= UserRight.ENCODER) {
                
                //get approval from approving officer
                poJSON = ShowDialogFX.getUserApproval(poGRider);
                if ("error".equals((String) poJSON.get("result"))) {
                    return poJSON;
                } else {
                    
                    //if not authorized, show message
                    if (Integer.parseInt(poJSON.get("nUserLevl").toString()) <= UserRight.ENCODER) {
                        poJSON.put("result", "error");
                        poJSON.put("message", "User is not an authorized approving officer.");
                        return poJSON;
                    }
                    
                    //if success, return approving officer user id
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
    
    public JSONObject initTransaction() throws GuanzonException, SQLException{
        SOURCE_CODE = "Dlvr";
        
        poMaster = new CheckModels(poGRider).CheckReleaseMaster();
        poDetail = new CheckModels(poGRider).CheckReleaseDetail();
        paDetail = new ArrayList<>();
        paCheckList = new ArrayList<>();
        
        return super.initialize();
    }
    
    public JSONObject OpenTransaction(String transactionNo) throws CloneNotSupportedException, SQLException, GuanzonException {
        return poMaster.openRecord(transactionNo);
    }

    public JSONObject NewTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();
        poJSON = newTransaction();
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
 
        GetMaster().setIndustryId(psIndustryCode);
        return poJSON;
    }
    
    public JSONObject UpdateTransaction() {
        poJSON = new JSONObject();
        if (CheckReleaseStatus.CONFIRMED.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already released.");
            return poJSON;
        }

        if (CheckReleaseStatus.CANCELLED.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already cancelled.");
            return poJSON;
        }

        return updateTransaction();
    }
    
    public JSONObject SaveTransaction()  throws SQLException, GuanzonException, CloneNotSupportedException{
        poJSON = new JSONObject();
        
        
        
        poJSON = saveTransaction();
    
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        OpenTransaction(GetMaster().getTransactionNo());
        return poJSON;
    }
    
    public JSONObject SaveCheckPaymentTransaction(int EntryNo) throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        
        Model_Check_Release_Detail loDetail = (Model_Check_Release_Detail) paDetail.get(EntryNo);
        Model_Check_Payments loCheckPayment = (Model_Check_Payments) loDetail.CheckPayment();
        
        if (loCheckPayment.getEditMode() == EditMode.READY) {
            loCheckPayment.updateRecord();
            loCheckPayment.setLocation("3");
            loCheckPayment.setModifiedDate(poGRider.getServerDate());
            loCheckPayment.setModifyingId(poGRider.Encrypt(poGRider.getUserID()));
            poJSON = loCheckPayment.saveRecord();

            if (!"success".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
        }
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        return poJSON;
    }
    
    public JSONObject SearchTransactionMaster(String value, boolean byExact, boolean byCode){
        
        try{
            String lsSQL = CheckReleaseRecords.CheckReleaseMaster();
            if (!psIndustryCode.isEmpty()) {
                lsSQL = MiscUtil.addCondition(lsSQL, "sIndstCdx = " + SQLUtil.toSQL(psIndustryCode));
            }
            
            lsSQL = MiscUtil.addCondition(lsSQL, "cTranStat <> " + SQLUtil.toSQL(CheckReleaseStatus.CANCELLED));
            
            poJSON = new JSONObject();
            poJSON = ShowDialogFX.Search(poGRider, 
                    lsSQL, 
                    value, 
                    "Transaction No»Transaction Date»Received By", 
                    "sTransNox»dTransact»sReceived", 
                    "sTransNox»sReceived", 
                    byExact ? (byCode ? 0 : 1) : 1);
            
            if (poJSON != null) {
                return poMaster.openRecord((String) poJSON.get("sTransNox"));
            } else {
                poJSON = new JSONObject();
                poJSON.put("result", "error");
                poJSON.put("message", "No record loaded.");
                return poJSON;

            }
            
        }catch(SQLException | GuanzonException ex){
            Logger.getLogger(CheckRelease.class
                    .getName()).log(Level.SEVERE, null, ex);
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }
    
    public JSONObject SearchCheckTransaction(String fsValue, boolean byExact, boolean byCode){
        
        try{
            String lsSQL = CheckReleaseRecords.CheckPaymentRecord();

            if (!psIndustryCode.isEmpty()) {
                lsSQL = MiscUtil.addCondition(lsSQL, "a.sIndstCdx = " + SQLUtil.toSQL(psIndustryCode));
            }

            lsSQL = MiscUtil.addCondition(lsSQL, " a.cReleased = " + SQLUtil.toSQL(CheckReleaseStatus.OPEN));
            lsSQL = MiscUtil.addCondition(lsSQL, "a.cLocation = " + SQLUtil.toSQL(RecordStatus.ACTIVE));
            lsSQL = MiscUtil.addCondition(lsSQL, "a.cTranStat <> " + SQLUtil.toSQL(CheckReleaseStatus.CANCELLED));
            
            poJSON = new JSONObject();
            poJSON = ShowDialogFX.Search(poGRider, 
                    lsSQL, 
                    fsValue, 
                    "Transaction No»Transaction Date»Check No»Check Amt", 
                    "a.sTransNox»a.dTransact»a.sCheckNox»a.nAmountxx", 
                    "a.sTransNox»e.sPayeeNme»a.sCheckNox", 
                    byExact ? (byCode ? 0 : 1) : 2);
            
            if (poJSON != null) {
                
                paCheckList.clear();
                
                Model_Check_Payments loBrowseChecks
                    = new CashflowModels(poGRider).CheckPayments();
                
                poJSON = loBrowseChecks.openRecord((String) poJSON.get("sTransNox"));
                if ("success".equals((String) poJSON.get("result"))) {
                    paCheckList.add((Model) loBrowseChecks);
                }
                return poJSON;
            } else {
                poJSON = new JSONObject();
                poJSON.put("result", "error");
                poJSON.put("message", "No record loaded.");
                return poJSON;

            }
        
        }catch(SQLException | GuanzonException ex){
            Logger.getLogger(CheckRelease.class
                    .getName()).log(Level.SEVERE, null, ex);
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }
    
    public JSONObject LoadCheckListByDate(String fsDateFrom, String fsDateThru)
            throws SQLException, GuanzonException, CloneNotSupportedException {
        
        initSQL();
        String lsSQL = CheckReleaseRecords.CheckPaymentRecord();

        if (!psIndustryCode.isEmpty()) {
            lsSQL = MiscUtil.addCondition(lsSQL, "a.sIndstCdx = " + SQLUtil.toSQL(psIndustryCode));
        }
    
        lsSQL = MiscUtil.addCondition(lsSQL, " a.cReleased = " + SQLUtil.toSQL(CheckReleaseStatus.OPEN));
        lsSQL = MiscUtil.addCondition(lsSQL, "a.cLocation = " + SQLUtil.toSQL(RecordStatus.ACTIVE));
        lsSQL = MiscUtil.addCondition(lsSQL, "a.cTranStat <> " + SQLUtil.toSQL(CheckReleaseStatus.CANCELLED));
                
        if (!fsDateFrom.isEmpty() && !fsDateThru.isEmpty()) {
            lsSQL = MiscUtil.addCondition(lsSQL, " a.dCheckDte BETWEEN " + SQLUtil.toSQL(fsDateFrom) + "AND "
                    + SQLUtil.toSQL(fsDateThru));
        }else{
            
            if (!fsDateFrom.isEmpty()) {
                lsSQL = MiscUtil.addCondition(lsSQL, " a.dCheckDte = " + SQLUtil.toSQL(fsDateFrom));
            }
            
            if (!fsDateThru.isEmpty()) {
                lsSQL = MiscUtil.addCondition(lsSQL, " a.dCheckDte = " + SQLUtil.toSQL(fsDateThru));
            }
            
        }
        
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        System.out.println("Load Transaction list query is " + lsSQL);

        if (MiscUtil.RecordCount(loRS)
                <= 0) {
            poJSON.put("result", "error");
            poJSON.put("message", "No record found.");
            return poJSON;
        }
        Set<String> processedTrans = new HashSet<>();

        paCheckList.clear();
        while (loRS.next()) {
            String transNo = loRS.getString("sTransNox");

            // Skip if we already processed this transaction number
            if (processedTrans.contains(transNo)) {
                continue;
            }
            
            Model_Check_Payments loBrowseChecks
                    = new CashflowModels(poGRider).CheckPayments();

            poJSON = loBrowseChecks.openRecord(transNo);
            if ("success".equals((String) poJSON.get("result"))) {
                paCheckList.add((Model) loBrowseChecks);

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
    
    public JSONObject LoadCheckTransaction(String fsTransNox, int fnEntryNo){
        
        try{
            
            if(fsTransNox.isEmpty()){
                poJSON.put("result", "error");
                poJSON.put("message", "Transaction no is empty!");

                return poJSON;
            }
            
            if (fnEntryNo < 0) {
                poJSON.put("result", "error");
                poJSON.put("message", "Invalid row number!");

                return poJSON;
            }
            
            String lsSQL = CheckReleaseRecords.CheckPaymentRecord();

            if (!psIndustryCode.isEmpty()) {
                lsSQL = MiscUtil.addCondition(lsSQL, "a.sIndstCdx = " + SQLUtil.toSQL(psIndustryCode));
            }

            lsSQL = MiscUtil.addCondition(lsSQL, "a.sTransNox = " + SQLUtil.toSQL(fsTransNox));
            lsSQL = MiscUtil.addCondition(lsSQL, " a.cReleased = " + SQLUtil.toSQL(CheckReleaseStatus.OPEN));
            lsSQL = MiscUtil.addCondition(lsSQL, "a.cLocation = " + SQLUtil.toSQL(RecordStatus.ACTIVE));
            lsSQL = MiscUtil.addCondition(lsSQL, "a.cTranStat <> " + SQLUtil.toSQL(CheckReleaseStatus.CANCELLED));
            
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

                Model_Check_Payments loBrowseChecks
                        = new CashflowModels(poGRider).CheckPayments();

                poJSON = loBrowseChecks.openRecord(transNo);
                if ("success".equals((String) poJSON.get("result"))) {

                    //check array stream if check's transaction number already exists
                    if (paDetail.stream().anyMatch(loTrans -> loTrans.getValue("sSourceNo").equals(transNo))) {
                        poJSON = new JSONObject();
                        poJSON.put("result", "error");
                        poJSON.put("message", "Check no " + loBrowseChecks.getCheckNo() + " already added!");
                        
                        return poJSON;
                    }
                    
                    //initialize detail
                    Model_Check_Release_Detail loDetail = new CheckModels(poGRider).CheckReleaseDetail();
                    loDetail.newRecord();
                    loDetail.setTransactionNo(GetMaster().getTransactionNo());
                    loDetail.setEntryNo(fnEntryNo);
                    loDetail.setSourceNo(loBrowseChecks.getTransactionNo());
                    loDetail.setSourceCD(loBrowseChecks.getSourceCode());
                    
                    //if list is not empty and index is last row
                    if (fnEntryNo == paDetail.size() && paDetail.size() > 0) {
                        fnEntryNo = fnEntryNo - 1;
                    }
                    
                    //if list is empty, add as new row, else, replace selected item
                    if (paDetail.size() == 0) {
                        
                        //add as new row
                        paDetail.add(loDetail);
                    }else{
                        
                        //replace existing row with new value
                        paDetail.remove(fnEntryNo);
                        paDetail.add(fnEntryNo, loDetail);
                    }
                    
                    // Mark this transaction as processed
                    processedTrans.add(transNo);
                } else {
                    return poJSON;
                }
            }
            
            //if selected row item from detail is last, add new empty row
            if (fnEntryNo == (paDetail.size() - 1)) {
                
                //add a new row
                Model_Check_Release_Detail loNew = new CheckModels(poGRider).CheckReleaseDetail();
                loNew.newRecord();
                loNew.setTransactionNo("");
                loNew.setEntryNo(paDetail.size() + 1);

                paDetail.add(loNew);

            }

            poJSON = new JSONObject();
            poJSON.put("result", "success");
            return poJSON;
            
        }catch(SQLException | GuanzonException ex){
            Logger.getLogger(CheckRelease.class
                    .getName()).log(Level.SEVERE, null, ex);
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }
    
    public JSONObject LoadDetail(String fsTransNox) throws SQLException, GuanzonException, CloneNotSupportedException{
        
        if(fsTransNox.isEmpty()){
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction no is empty!");
            return poJSON;
        }
        
        String lsSQL = CheckReleaseRecords.CheckReleaseDetail();
        lsSQL = MiscUtil.addCondition(lsSQL, "sTransNox = " + SQLUtil.toSQL(fsTransNox));
        
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        
        if (MiscUtil.RecordCount(loRS)
                <= 0) {
            poJSON.put("result", "error");
            poJSON.put("message", "No record found.");
            return poJSON;
        }
        Set<String> processedTrans = new HashSet<>();
        
        paDetail.clear();
        while (loRS.next()) {
            
            String lsSourceno = loRS.getString("sSourceNo");
            
            // Skip if we already processed this transaction number
            if (processedTrans.contains(lsSourceno)) {
                continue;
            }
            
            Model_Check_Release_Detail loDetail = new CheckModels(poGRider).CheckReleaseDetail();
            loDetail.setTransactionNo(loRS.getString("sTransNox"));
            loDetail.setEntryNo(loRS.getInt("nEntryNox"));
            loDetail.setSourceCD(loRS.getString("sSourceCd"));
            loDetail.setSourceNo(loRS.getString("sSourceNo"));
            loDetail.setModifiedDate(loRS.getDate("dModified"));
            
            Model_Check_Payments loBrowseChecks = new CashflowModels(poGRider).CheckPayments();
            poJSON = loBrowseChecks.openRecord(lsSourceno);
            
            if ("success".equals((String) poJSON.get("result"))) {
                paDetail.add(loDetail);

                // Mark this transaction as processed
                processedTrans.add(lsSourceno);
                
            } else {
                return poJSON;
            }
        }
        
        //add a new row
        Model_Check_Release_Detail loNew = new CheckModels(poGRider).CheckReleaseDetail();
        loNew.newRecord();
        loNew.setTransactionNo("");
        loNew.setEntryNo(paDetail.size() + 1);

        paDetail.add(loNew);
        
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        return poJSON;
    }
   
}
