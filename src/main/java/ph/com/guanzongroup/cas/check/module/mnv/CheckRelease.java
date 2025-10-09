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
import org.guanzon.appdriver.constant.RecordStatus;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.model.Model_Check_Payments;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;
import ph.com.guanzongroup.cas.check.module.mnv.constant.CheckReleaseRecords;
import ph.com.guanzongroup.cas.check.module.mnv.constant.CheckReleaseStatus;
import ph.com.guanzongroup.cas.check.module.mnv.models.Model_Check_Release_Detail;
import ph.com.guanzongroup.cas.check.module.mnv.models.Model_Check_Release_Master;
import ph.com.guanzongroup.cas.check.module.mnv.services.CheckModels;

/**
 *
 * @author User
 */
public class CheckRelease extends Transaction{
    
    private String psIndustryCode = "";
    private List<Model> paCheckList;
    private List<Model> paDetail;
    private List<Model> paDetailCheck;
    
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
    
    @SuppressWarnings("unchecked")
    public List<Model_Check_Payments> GetDetailCheckList() {
        return (List<Model_Check_Payments>) (List<?>) paDetailCheck;
    }
    
    @SuppressWarnings("unchecked")
    public Model_Check_Payments GetCheckDetail(int fnRow) {
        return (Model_Check_Payments) paDetailCheck.get(fnRow);
    }
    
    public JSONObject initTransaction() throws GuanzonException, SQLException{
        SOURCE_CODE = "Dlvr";
        
        poMaster = new CheckModels(poGRider).CheckReleaseMaster();
        poDetail = new CheckModels(poGRider).CheckReleaseDetail();
        paDetail = new ArrayList<>();
        paCheckList = new ArrayList<>();
        paDetailCheck = new ArrayList<>();
        
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
                    
                    //check array stream if transaction number already exists
                    if (paDetailCheck.stream().anyMatch(loTrans -> loTrans.getValue("sTransNox").equals(transNo)) || 
                            paDetail.stream().anyMatch(loTrans -> loTrans.getValue("sTransNox").equals(transNo))) {
                        poJSON = new JSONObject();
                        poJSON.put("result", "error");
                        poJSON.put("message", "Check no " + loBrowseChecks.getCheckNo() + " already added!");
                        
                        return poJSON;
                    }
                    
                    //set max index count from list
                    if (fnEntryNo == paDetailCheck.size()) {
                        fnEntryNo = fnEntryNo - 1;
                    }
                    
                    //replace existing row with new value
                    Model_Check_Release_Detail loDetail = new CheckModels(poGRider).CheckReleaseDetail();
                    loDetail.setTransactionNo(GetMaster().getTransactionNo());
                    loDetail.setEntryNo(fnEntryNo);
                    loDetail.setSourceNo(loBrowseChecks.getTransactionNo());
                    loDetail.setSourceCD(loBrowseChecks.getSourceCode());
                    
                    paDetail.remove(fnEntryNo);
                    paDetail.add(fnEntryNo, loBrowseChecks);

                    paDetailCheck.remove(fnEntryNo);
                    paDetailCheck.add(fnEntryNo, loBrowseChecks);
                    
                    // Mark this transaction as processed
                    processedTrans.add(transNo);
                } else {
                    return poJSON;
                }
            }
            
            //remove last inserted row, which is a new row
            paDetail.remove(paDetail.size() - 1);
            paDetailCheck.remove(paDetailCheck.size() -1);
            
            //add a new row
            Model_Check_Release_Detail loDetail = new CheckModels(poGRider).CheckReleaseDetail();
            loDetail.newRecord();
            loDetail.setEntryNo(paDetail.size() + 1);
            
            paDetail.add(loDetail);
            
            Model_Check_Payments loCheck = new CashflowModels(poGRider).CheckPayments();
            loCheck.newRecord();
            
            paDetailCheck.add(loCheck);

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
        paDetailCheck.clear();
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
                paDetailCheck.add((Model) loBrowseChecks);

                // Mark this transaction as processed
                processedTrans.add(lsSourceno);
                
            } else {
                return poJSON;
            }
        }
        
        Model_Check_Release_Detail loDetail = new CheckModels(poGRider).CheckReleaseDetail();
        loDetail.newRecord();
        loDetail.setEntryNo(paDetail.size() + 1);

        paDetail.add(loDetail);

        Model_Check_Payments loCheck = new CashflowModels(poGRider).CheckPayments();
        loCheck.newRecord();
        paDetailCheck.add(loCheck);
        
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        return poJSON;
    }
   
}
