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
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import net.sf.jasperreports.engine.JRException;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.agent.services.Transaction;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.appdriver.iface.GValidator;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.DocumentMapping;
import ph.com.guanzongroup.cas.cashflow.model.Model_Check_Payments;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;
import ph.com.guanzongroup.cas.check.module.mnv.constant.CheckReleaseRecords;
import ph.com.guanzongroup.cas.check.module.mnv.constant.CheckReleaseStatus;
import ph.com.guanzongroup.cas.check.module.mnv.constant.CheckTransferRecords;
import ph.com.guanzongroup.cas.check.module.mnv.constant.CheckTransferStatus;
import ph.com.guanzongroup.cas.check.module.mnv.models.Model_Check_Deposit_Detail;
import ph.com.guanzongroup.cas.check.module.mnv.models.Model_Check_Release_Detail;
import ph.com.guanzongroup.cas.check.module.mnv.models.Model_Check_Release_Master;
import ph.com.guanzongroup.cas.check.module.mnv.models.Model_Check_Transfer_Master;
import ph.com.guanzongroup.cas.check.module.mnv.services.CheckModels;
import ph.com.guanzongroup.cas.check.module.mnv.validator.CheckReleaseValidatorFactory;
import ph.com.guanzongroup.cas.inv.warehouse.t4.report.ReportUtil;
import ph.com.guanzongroup.cas.inv.warehouse.t4.report.ReportUtilListener;

/**
 *
 * @author User
 */
public class CheckRelease extends Transaction{
    
    private String psIndustryCode = "";
    private String psApprovalUser = "";
    private List<Model> paCheckList;
    
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

        //assign values needed and remove from detail list with the ff conditions
        for (int lnCtr = 0; lnCtr < paDetail.size(); lnCtr++) {
            
            Model_Check_Release_Detail loDetail = (Model_Check_Release_Detail) paDetail.get(lnCtr);
            //if null, remove from saving
            if (loDetail == null) {
                paDetail.remove(lnCtr);
            } else {
                
                //if source no is empty, remove from saving
                if (loDetail.getSourceNo() == null || loDetail.getSourceNo().isEmpty()) {
                    paDetail.remove(lnCtr);
                    continue;
                }
                
                //if amount is less than zero, remove from saving
                if (loDetail.CheckPayment().getAmount() <= 0) {
                    paDetail.remove(lnCtr);
                    continue;
                }
                lnDetailCount++;
                
                //assign values to model
                loDetail.setTransactionNo(GetMaster().getTransactionNo());
                loDetail.setEntryNo(lnDetailCount);
                
                //recompute master total by amount per detail
                lnTotalAmount += loDetail.CheckPayment().getAmount();

            }
        }

        GetMaster().setEntryNo(lnDetailCount);
        GetMaster().setTransactionTotal(lnTotalAmount);
        
        //initialize date modified
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
        return openTransaction(transactionNo);
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
        
        //validate status if already released
        if (CheckReleaseStatus.CONFIRMED.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already released.");
            return poJSON;
        }

        //validate tranaction if already cancelled
        if (CheckReleaseStatus.CANCELLED.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already cancelled.");
            return poJSON;
        }

        return updateTransaction();
    }
    
    public JSONObject CloseTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();
        
        //edit mode should be in ready, for update of transaction
        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction is not ready for confirmation");
            return poJSON;
        }

        //check status if already confirmed
        if (CheckReleaseStatus.CONFIRMED.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already confirmed.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(CheckReleaseStatus.CONFIRMED);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        //begin transaction
        poGRider.beginTrans("UPDATE STATUS", "ConfirmTransaction", SOURCE_CODE, GetMaster().getTransactionNo());

        //update transaction status to confirm
        poJSON = statusChange(poMaster.getTable(),
                (String) poMaster.getValue("sTransNox"),
                "ConfirmTransaction",
                CheckReleaseStatus.CONFIRMED,
                false, true);
        
        //check result, return if error
        if ("error".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }
        
        //if success, get details
        for (int lnCtr = 0; lnCtr < paDetail.size(); lnCtr++) {
            Model_Check_Release_Detail loDetail = (Model_Check_Release_Detail) paDetail.get(lnCtr);

            //get source no, if not empty
            if (loDetail.getSourceNo() != null) {
                if (!loDetail.getSourceNo().isEmpty()) {
                    
                    //update detail's check payment
                    poJSON = new JSONObject();
                    poJSON = ReleaseCheckPaymentTransaction(lnCtr);

                    if (!"success".equals((String) poJSON.get("result"))) {
                        poGRider.rollbackTrans();
                        return poJSON;
                    }
                }
            }
        }

        poGRider.commitTrans();

        //reload transaction
        openTransaction(GetMaster().getTransactionNo());
        
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "Transaction confirmed successfully.");
        return poJSON;
    }
    
    public JSONObject ReleaseCheckPaymentTransaction(int EntryNo) throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        
        //initialize check payment detail properties
        Model_Check_Release_Detail loDetail = (Model_Check_Release_Detail) paDetail.get(EntryNo);
        Model_Check_Payments loCheckPayment = loDetail.CheckPayment();
        
        //allow to update properties, if transaction is ready for update
        if (loCheckPayment.getEditMode() == EditMode.READY) {
            
            //set transaction on update
            loCheckPayment.updateRecord();
            
            //set properties for record
            loCheckPayment.setLocation("3");
            loCheckPayment.setReleased("1");
            loCheckPayment.setModifiedDate(poGRider.getServerDate());
            loCheckPayment.setModifyingId(poGRider.Encrypt(poGRider.getUserID()));
            
            //save record
            poJSON = loCheckPayment.saveRecord();

            if (!"success".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
        }
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        return poJSON;
    }
    
    public JSONObject VoidTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        //edit mode should be in ready, for update of transaction
        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid Edit Mode.");
            return poJSON;
        }

        //do not allow void if already cancelled
        if (CheckReleaseStatus.CANCELLED.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already cancelled.");
            return poJSON;
        }

        //do not allow void if already voided
        if (CheckReleaseStatus.VOID.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already voided.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(CheckReleaseStatus.VOID);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        //initialize transaction
        poGRider.beginTrans("UPDATE STATUS", "VoidTransaction", SOURCE_CODE, GetMaster().getTransactionNo());

        //update transaction status
        poJSON = statusChange(poMaster.getTable(),
                (String) poMaster.getValue("sTransNox"),
                "VoidTransaction",
                CheckReleaseStatus.VOID,
                false, true);
        if ("error".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }
        
        //get detail
        for (int lnCtr = 0; lnCtr < paDetail.size(); lnCtr++) {
            Model_Check_Release_Detail loDetail = (Model_Check_Release_Detail) paDetail.get(lnCtr);

            //validate detail's source no
            if (loDetail.getSourceNo() != null) {
                if (!loDetail.getSourceNo().isEmpty()) {
                    
                    //update detail's check payment transaction
                    poJSON = new JSONObject();
                    poJSON = ReturnCheckPaymentTransaction(lnCtr);

                    if (!"success".equals((String) poJSON.get("result"))) {
                        poGRider.rollbackTrans();
                        return poJSON;
                    }
                }
            }
        }
        poGRider.commitTrans();

        //reload transaction
        openTransaction(GetMaster().getTransactionNo());
        
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "Transaction voided successfully.");

        return poJSON;
    }
    
    public JSONObject ReturnCheckPaymentTransaction(int EntryNo) throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        
        //get check detail
        Model_Check_Release_Detail loDetail = (Model_Check_Release_Detail) paDetail.get(EntryNo);
        Model_Check_Payments loCheckPayment = loDetail.CheckPayment();
        
        //update transaction, if in ready mode
        if (loCheckPayment.getEditMode() == EditMode.READY) {
            
            //initialize record update
            loCheckPayment.updateRecord();
            
            //set check payment properties
            loCheckPayment.setBranchCode(poGRider.getBranchCode());
            loCheckPayment.setLocation("1");
            
            //save transaction
            poJSON = loCheckPayment.saveRecord();

            if (!"success".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
        }
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        return poJSON;
    }

    public JSONObject CancelTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        //edit mode should be in ready, for update of transaction
        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid Edit Mode");
            return poJSON;
        }

        //do not allow void if already cancelled
        if (CheckReleaseStatus.CANCELLED.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already cancelled.");
            return poJSON;
        }

        //do not allow void if already voided
        if (CheckReleaseStatus.VOID.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already voided.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(CheckReleaseStatus.CANCELLED);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        //initialize transaction
        poGRider.beginTrans("UPDATE STATUS", "CancelTransaction", SOURCE_CODE, GetMaster().getTransactionNo());

        //update transaction status
        poJSON = statusChange(poMaster.getTable(),
                (String) poMaster.getValue("sTransNox"),
                "CancelTransaction",
                CheckReleaseStatus.CANCELLED,
                false, true);
        if ("error".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }

        //get detail
        for (int lnCtr = 0; lnCtr < paDetail.size(); lnCtr++) {
            Model_Check_Release_Detail loDetail = (Model_Check_Release_Detail) paDetail.get(lnCtr);

            //validate detail's source no
            if (loDetail.getSourceNo() != null) {
                if (!loDetail.getSourceNo().isEmpty()) {
                    
                    //update detail's check payment transaction
                    poJSON = new JSONObject();
                    poJSON = ReturnCheckPaymentTransaction(lnCtr);

                    if (!"success".equals((String) poJSON.get("result"))) {
                        poGRider.rollbackTrans();
                        return poJSON;
                    }
                }
            }
        }
        poGRider.commitTrans();

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "Transaction cancelled successfully.");

        return poJSON;
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
    
    public JSONObject SearchTransaction(String value, boolean byExact){
        
        try{
            String lsSQL = CheckReleaseRecords.CheckReleaseMaster();
            if (!psIndustryCode.isEmpty()) {
                lsSQL = MiscUtil.addCondition(lsSQL, "sIndstCdx = " + SQLUtil.toSQL(psIndustryCode));
            }
            lsSQL = MiscUtil.addCondition(lsSQL, "LEFT(sTransNox,4) = " + SQLUtil.toSQL(poGRider.getBranchCode()));
            lsSQL = MiscUtil.addCondition(lsSQL, "cTranStat <> " + SQLUtil.toSQL(CheckReleaseStatus.CANCELLED));
            
            System.out.print(lsSQL);
            
            poJSON = new JSONObject();
            poJSON = ShowDialogFX.Search(poGRider, 
                    lsSQL, 
                    value, 
                    "Transaction No»Transaction Date»Received By", 
                    "sTransNox»dTransact»sReceived", 
                    "sTransNox»sReceived", 
                    byExact ? 0 : 1);
            
            if (poJSON != null) {
                return openTransaction((String) poJSON.get("sTransNox"));
            } else {
                poJSON = new JSONObject();
                poJSON.put("result", "error");
                poJSON.put("message", "No record loaded.");
                return poJSON;

            }
            
        }catch(SQLException | GuanzonException | CloneNotSupportedException ex){
            Logger.getLogger(CheckRelease.class
                    .getName()).log(Level.SEVERE, null, ex);
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }
    
    public JSONObject SearchTransactionPosting(String value, boolean byExact){
        
        try{
            String lsSQL = CheckReleaseRecords.CheckReleaseMaster();
            if (!psIndustryCode.isEmpty()) {
                lsSQL = MiscUtil.addCondition(lsSQL, "sIndstCdx = " + SQLUtil.toSQL(psIndustryCode));
            }
            lsSQL = MiscUtil.addCondition(lsSQL, "LEFT(sTransNox,4) = " + SQLUtil.toSQL(poGRider.getBranchCode()));
            lsSQL = MiscUtil.addCondition(lsSQL, "cTranStat = " + SQLUtil.toSQL(CheckReleaseStatus.CONFIRMED));
            
            System.out.print(lsSQL);
            
            poJSON = new JSONObject();
            poJSON = ShowDialogFX.Search(poGRider, 
                    lsSQL, 
                    value, 
                    "Transaction No»Transaction Date»Received By", 
                    "sTransNox»dTransact»sReceived", 
                    "sTransNox»sReceived", 
                    byExact ? 0 : 1);
            
            if (poJSON != null) {
                return openTransaction((String) poJSON.get("sTransNox"));
            } else {
                poJSON = new JSONObject();
                poJSON.put("result", "error");
                poJSON.put("message", "No record loaded.");
                return poJSON;

            }
            
        }catch(SQLException | GuanzonException | CloneNotSupportedException ex){
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
            
            //if no entries from list, add new entry
            if (paDetail.size() <= 0 || fnEntryNo ==  paDetail.size()) {
                
                //add new record, as extra row
                Model_Check_Release_Detail newDetail = new CheckModels(poGRider).CheckReleaseDetail();
                newDetail.newRecord();

                //set property value for transaction no and entry no for new detail
                newDetail.setTransactionNo(GetMaster().getTransactionNo());
                newDetail.setEntryNo(paDetail.size() + 1);

                //add as new row
                paDetail.add(newDetail);

            }
            
            Model_Check_Payments loBrowseChecks
                        = new CashflowModels(poGRider).CheckPayments();

            poJSON = loBrowseChecks.openRecord(fsTransNox);

            //if record successfully loaded
            if ("success".equals((String) poJSON.get("result"))) {
                
                //check array stream if check's transaction number already exists
                for (int lnExist = 0; lnExist < paDetail.size(); lnExist++) {
                    
                    //get details record from list
                    Model_Check_Release_Detail loDetail = (Model_Check_Release_Detail) paDetail.get(lnExist);
                    if (loDetail.getSourceNo() != null) {
                        
                        //validate check added by source no, do not allow
                        if (loDetail.getSourceNo().equals(fsTransNox)) {
                            poJSON = new JSONObject();
                            poJSON.put("result", "error");
                            poJSON.put("message", "Check no " + loBrowseChecks.getCheckNo() + " already added!");

                            return poJSON;
                        }
                    }
                    
                    //if entry no of detail matches the selected row index
                    if (loDetail.getEntryNo() == fnEntryNo) {
                        
                        //update source no for detail
                        loDetail.setSourceNo(loBrowseChecks.getTransactionNo());

                        poJSON = new JSONObject();
                        poJSON.put("result", "success");
                        return poJSON;
                    }
                }
                
                poJSON = new JSONObject();
                        poJSON.put("result", "success");
                        return poJSON;

            } else {
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
            
            //set model properties
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
        
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        return poJSON;
    }
    
   public JSONObject PrintRecord() throws SQLException, JRException, CloneNotSupportedException, GuanzonException {

        poJSON = new JSONObject();

        //validate transaction status
        if (CheckReleaseStatus.POSTED.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already Processed.");
            return poJSON;
        }

        if (CheckReleaseStatus.CANCELLED.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already cancelled.");
            return poJSON;
        }

        if (CheckReleaseStatus.VOID.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already voided.");
            return poJSON;
        }

        //initialize report utility object
        ReportUtil poReportJasper = new ReportUtil(poGRider);

        //check transaction no
        if (GetMaster().getTransactionNo() == null && GetMaster().getTransactionNo().isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "No record is Selected");
            return poJSON;

        }
        
        //open transaction record
        poJSON = OpenTransaction(GetMaster().getTransactionNo());
        if ("error".equals((String) poJSON.get("result"))) {
            System.out.println("Print Record open transaction : " + (String) poJSON.get("message"));
            return poJSON;
        }

        //check print status if active
        if (((Model_Check_Release_Master) poMaster).isPrintedStatus()) {
            
            //check entry
            poJSON = isEntryOkay(CheckReleaseStatus.CONFIRMED);
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
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
                    
                    //open transaction record
                    poJSON = OpenTransaction(GetMaster().getTransactionNo());
                    if ("error".equals((String) poJSON.get("result"))) {
                        System.out.println("Print Record open transaction : " + (String) poJSON.get("message"));
                        return;
                    }
                    
                    //check print status
                    if (!GetMaster().isPrintedStatus()) {
                        
                        //if not active, print transaction
                        if (!isJSONSuccess(PrintTransaction(), "Print Record","Initialize Record Print! ")) {
                            return;
                        }
                    }
                    
                    //if status is open, close transaction
                    if (GetMaster().getTransactionStatus().equals(CheckTransferStatus.OPEN)) {
                        if (!isJSONSuccess(CloseTransaction(), "Print Record",
                                "Initialize Close Transaction! ")) {
                        }
                    }

                    //close object
                    poReportJasper.CloseReportUtil();

                } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
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
            }

            @Override
            public void onReportExportPDF() {
                System.out.println("Report exported.");
            }

        }
        );
        
        //add Jasper Parameter
        poReportJasper.addParameter("BranchName", poGRider.getBranchName());
        poReportJasper.addParameter("Address", poGRider.getAddress());
        poReportJasper.addParameter("CompanyName", poGRider.getClientName());
        poReportJasper.addParameter("TransactionNo", GetMaster().getTransactionNo());
        poReportJasper.addParameter("TransactionDate", SQLUtil.dateFormat(GetMaster().getTransactionDate(), SQLUtil.FORMAT_LONG_DATE));
        poReportJasper.addParameter("Remarks", GetMaster().getRemarks());
        poReportJasper.addParameter("ReceivedBy", "Sheryl Rabanal");
        poReportJasper.addParameter("DatePrinted", SQLUtil.dateFormat(poGRider.getServerDate(), SQLUtil.FORMAT_TIMESTAMP));
        
        //set watermark based on print status, "Print" or "Reprint"
        if (GetMaster().isPrintedStatus()) {
            poReportJasper.addParameter("watermarkImagePath", poGRider.getReportPath() + "images\\reprint.png");
        } else {
            poReportJasper.addParameter("watermarkImagePath", poGRider.getReportPath() + "images\\blank.png");
        }

        //set print name
        poReportJasper.setReportName("Check Release");
        poReportJasper.setJasperPath(CheckReleaseRecords.getJasperReport(psIndustryCode));

        //process by ResultSet
        String lsSQL = CheckReleaseRecords.PrintRecord();
        lsSQL = MiscUtil.addCondition(lsSQL, "a.sTransNox = " + SQLUtil.toSQL(GetMaster().getTransactionNo()));
        
        System.out.println(
                "Print Data Query :" + lsSQL);

        poReportJasper.setSQLReport(lsSQL);

        poReportJasper.isAlwaysTop(false);
        poReportJasper.isWithUI(true);
        poReportJasper.isWithExport(false);
        poReportJasper.isWithExportPDF(false);
        poReportJasper.willExport(true);
        
        return poReportJasper.generateReport();

    }
   
   private JSONObject PrintTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        //open transaction
        poJSON = OpenTransaction(GetMaster().getTransactionNo());
        if ("error".equals((String) poJSON.get("result"))) {
            System.out.println("Print Record open transaction : " + (String) poJSON.get("message"));
            return poJSON;
        }

        //do not allow printing if edit mode is not ready
        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        //begin transaction
        poGRider.beginTrans("UPDATE STATUS", "Process Transaction Print Tag", SOURCE_CODE, GetMaster().getTransactionNo());

        //initialize sql for update
        String lsSQL = "UPDATE "
                + poMaster.getTable()
                + " SET   cPrintedx = " + SQLUtil.toSQL(CheckReleaseStatus.CONFIRMED)
                + " WHERE sTransNox = " + SQLUtil.toSQL(GetMaster().getTransactionNo());

        //execute query update
        Long lnResult = poGRider.executeQuery(lsSQL,
                poMaster.getTable(),
                poGRider.getBranchCode(), "", "");
        
        //check result 
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
