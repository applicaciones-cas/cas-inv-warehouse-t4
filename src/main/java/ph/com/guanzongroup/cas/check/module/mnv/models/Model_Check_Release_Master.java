/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ph.com.guanzongroup.cas.check.module.mnv.models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.client.model.Model_Client_Master;
import org.guanzon.cas.client.services.ClientModels;
import org.guanzon.cas.parameter.model.Model_Banks;
import org.guanzon.cas.parameter.model.Model_Branch;
import org.guanzon.cas.parameter.model.Model_Department;
import org.guanzon.cas.parameter.model.Model_Industry;
import org.guanzon.cas.parameter.services.ParamModels;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.model.Model_Bank_Account_Master;
import ph.com.guanzongroup.cas.cashflow.model.Model_Check_Payments;
import ph.com.guanzongroup.cas.cashflow.model.Model_Payee;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;
import ph.com.guanzongroup.cas.check.module.mnv.constant.CheckReleaseStatus;

/**
 *
 * @author User
 */
public class Model_Check_Release_Master extends Model{
    
    Model_Industry poIndustry;
    Model_Branch poBranch;
    Model_Client_Master poSupplier;
    Model_Payee poPayee;
    Model_Bank_Account_Master poBankAccountMaster;
    Model_Banks poBanks;
    Model_Check_Payments poCheckPayment;

    @Override
    public void initialize() {
        
        try{
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());
            poEntity.last();
            poEntity.moveToInsertRow();
            
            poEntity.updateObject("dTransact", poGRider.getServerDate());
            poEntity.updateObject("nEntryNox", 0);
            poEntity.updateDouble("nTranTotl", 0.00d);
            poEntity.updateString("cPrintedx", "0");
            poEntity.updateString("cTranStat", CheckReleaseStatus.OPEN);
            poEntity.updateNull("sReceived");
            poEntity.updateNull("sModified");
            poEntity.updateObject("dModified", poGRider.getServerDate());

            MiscUtil.initRowSet(poEntity);
            
            ParamModels model = new ParamModels(poGRider);
            poBranch = model.Branch();
            poBanks = model.Banks();
            poIndustry = model.Industry();
            
            CashflowModels cashFlow = new CashflowModels(poGRider);
            poPayee = cashFlow.Payee();
            poBankAccountMaster = cashFlow.Bank_Account_Master();
            poCheckPayment = cashFlow.CheckPayments();
            
            ClientModels clientModel = new ClientModels(poGRider);
            poSupplier = clientModel.ClientMaster();
            
            poEntity.insertRow();
            poEntity.moveToCurrentRow();

            poEntity.absolute(1);

            ID = poEntity.getMetaData().getColumnLabel(1);

            //add model here
            pnEditMode = EditMode.UNKNOWN;
            
        }catch(Exception e){
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
    }
    
    //sTransNox
    public JSONObject setTransactionNo(String transactionNo) {
        return setValue("sTransNox", transactionNo);
    }

    public String getTransactionNo() {
        return (String) getValue("sTransNox");
    }

    //sIndstCdx
    public JSONObject setIndustryId(String industryId) {
        return setValue("sIndstCdx", industryId);
    }

    public String getIndustryId() {
        return (String) getValue("sIndstCdx");
    }
    
    //dTransact
    public JSONObject setTransactionDate(Date transactionDate) {
        return setValue("dTransact", transactionDate);
    }

    public Date getTransactionDate() {
        return (Date) getValue("dTransact");
    }
    
    //nEntryNox
    public JSONObject setEntryNo(int entryNo) {
        return setValue("nEntryNox", entryNo);
    }

    public int getEntryNo() {
        return (int) getValue("nEntryNox");
    }
    
    //sRemarksx
    public JSONObject setRemarks(String remarks) {
        return setValue("sRemarksx", remarks);
    }

    public String getRemarks() {
        return (String) getValue("sRemarksx");
    }
    
    //nTranTotl
    public JSONObject setTransactionTotal(Double transactionTotal) {
        return setValue("nTranTotl", transactionTotal);
    }

    public Double getTransactionTotal() {
        return Double.valueOf(getValue("nTranTotl").toString());
    }
    
    //cPrintedx
    public JSONObject setPrintStatus(String printStatus) {
        return setValue("cPrintedx", printStatus);
    }

    public String getPrintStatus() {
        return (String) getValue("cPrintedx");
    }
    
    //cTranStat
    public JSONObject setTransactionStatus(String transactionStatus) {
        return setValue("cTranStat", transactionStatus);
    }

    public String getTransactionStatus() {
        return (String) getValue("cTranStat");
    }
    
    //sReceived
    public JSONObject setReceivedBy(String receivedBy) {
        return setValue("sReceived", receivedBy);
    }

    public String getReceivedBy() {
        return (String) getValue("sReceived");
    }
    
    //sModified
    public JSONObject setModifyingId(String modifyingId) {
        return setValue("sModified", modifyingId);
    }

    public String getModifyingId() {
        return (String) getValue("sModified");
    }

    //dModified
    public JSONObject setModifiedDate(Date modifiedDate) {
        return setValue("dModified", modifiedDate);
    }

    public Date getModifiedDate() {
        return (Date) getValue("dModified");
    }
    
    @Override
    public String getNextCode() {
        return MiscUtil.getNextCode(this.getTable(), ID, true, poGRider.getGConnection().getConnection(), poGRider.getBranchCode());
    }
    
    public Model_Payee Payee() throws GuanzonException, SQLException {
        if (!"".equals((String) getValue("sPayeeIDx"))) {
            if (poPayee.getEditMode() == EditMode.READY
                    && poPayee.getPayeeID().equals((String) getValue("sPayeeIDx"))) {
                return poPayee;
            } else {
                poJSON = poPayee.openRecord((String) getValue("sPayeeIDx"));
                if ("success".equals((String) poJSON.get("result"))) {
                    return poPayee;
                } else {
                    poPayee.initialize();
                    return poPayee;
                }
            }
        } else {
            poPayee.initialize();
            return poPayee;
        }
    }

    public Model_Client_Master Supplier() throws GuanzonException, SQLException {
        if (!"".equals((String) getValue("sSupplier"))) {
            if (poSupplier.getEditMode() == EditMode.READY
                    && poSupplier.getClientId().equals((String) getValue("sSupplier"))) {
                return poSupplier;
            } else {
                poJSON = poSupplier.openRecord((String) getValue("sSupplier"));
                if ("success".equals((String) poJSON.get("result"))) {
                    return poSupplier;
                } else {
                    poSupplier.initialize();
                    return poSupplier;
                }
            }
        } else {
            poSupplier.initialize();
            return poSupplier;
        }
    }

    public Model_Branch Branch() throws GuanzonException, SQLException {
        if (!"".equals((String) getValue("sBranchCd"))) {
            if (poBranch.getEditMode() == EditMode.READY
                    && poBranch.getBranchCode().equals((String) getValue("sBranchCd"))) {
                return poBranch;
            } else {
                poJSON = poBranch.openRecord((String) getValue("sBranchCd"));
                if ("success".equals((String) poJSON.get("result"))) {
                    return poBranch;
                } else {
                    poBranch.initialize();
                    return poBranch;
                }
            }
        } else {
            poBranch.initialize();
            return poBranch;
        }
    }

    public Model_Banks Banks() throws GuanzonException, SQLException {
        if (!"".equals((String) getValue("sBankIDxx"))) {
            if (poBanks.getEditMode() == EditMode.READY
                    && poBanks.getBankID().equals((String) getValue("sBankIDxx"))) {
                return poBanks;
            } else {
                poJSON = poBanks.openRecord((String) getValue("sBankIDxx"));
                if ("success".equals((String) poJSON.get("result"))) {
                    return poBanks;
                } else {
                    poBanks.initialize();
                    return poBanks;
                }
            }
        } else {
            poBanks.initialize();
            return poBanks;
        }
    }

    public Model_Bank_Account_Master Bank_Account_Master() throws GuanzonException, SQLException {
        if (!"".equals((String) getValue("sBnkActID"))) {
            if (poBankAccountMaster.getEditMode() == EditMode.READY
                    && poBankAccountMaster.getBankAccountId().equals((String) getValue("sBnkActID"))) {
                return poBankAccountMaster;
            } else {
                poJSON = poBankAccountMaster.openRecord((String) getValue("sBnkActID"));
                if ("success".equals((String) poJSON.get("result"))) {
                    return poBankAccountMaster;
                } else {
                    poBankAccountMaster.initialize();
                    return poBankAccountMaster;
                }
            }
        } else {
            poBankAccountMaster.initialize();
            return poBankAccountMaster;
        }
    }

    public Model_Industry Industry() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sIndstCdx"))) {
            if (poIndustry.getEditMode() == EditMode.READY
                    && poIndustry.getIndustryId().equals((String) getValue("sIndstCdx"))) {
                return poIndustry;
            } else {
                poJSON = poIndustry.openRecord((String) getValue("sIndstCdx"));

                if ("success".equals((String) poJSON.get("result"))) {
                    return poIndustry;
                } else {
                    poIndustry.initialize();
                    return poIndustry;
                }
            }
        } else {
            poIndustry.initialize();
            return poIndustry;
        }
    }
    
    public Model_Check_Payments CheckPayment() throws SQLException, GuanzonException {
        if (!"".equals(getValue("sSourceNo"))) {
            if (this.poCheckPayment.getEditMode() == 1 && this.poCheckPayment
                    .getTransactionNo().equals(getValue("sSourceNo"))) {
                return this.poCheckPayment;
            }
            this.poJSON = this.poCheckPayment.openRecord((String) getValue("sSourceNo"));
            if ("success".equals(this.poJSON.get("result"))) {
                return this.poCheckPayment;
            }
            this.poCheckPayment.initialize();
            return this.poCheckPayment;
        }
        poCheckPayment.initialize();
        return this.poCheckPayment;
    }
}
