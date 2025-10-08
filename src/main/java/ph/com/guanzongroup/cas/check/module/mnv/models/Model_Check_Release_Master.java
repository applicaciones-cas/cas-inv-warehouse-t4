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
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.cas.parameter.model.Model_Industry;
import org.guanzon.cas.parameter.services.ParamModels;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.check.module.mnv.constant.CheckReleaseStatus;

/**
 *
 * @author User
 */
public class Model_Check_Release_Master extends Model{
    
    private Model_Industry poIndustry;

    @Override
    public void initialize() {
        
        try{
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());
            
            poEntity.last();
            poEntity.moveToInsertRow();
            
            MiscUtil.initRowSet(poEntity);
            
            poEntity.updateObject("dTransact", poGRider.getServerDate());
            poEntity.updateObject("nEntryNox", 0);
            poEntity.updateDouble("nTranTotl", 0.00);
            poEntity.updateString("cPrintedx", "0");
            poEntity.updateString("cTranStat", CheckReleaseStatus.OPEN);
            poEntity.updateNull("sReceived");
            poEntity.updateNull("sModified");
            poEntity.updateObject("dModified", poGRider.getServerDate());

            
            
            ParamModels model = new ParamModels(poGRider);
            poIndustry = model.Industry();
            
            poEntity.insertRow();
            poEntity.moveToCurrentRow();
            
            poEntity.absolute(1);

            ID = poEntity.getMetaData().getColumnLabel(1);

            //add model here
            pnEditMode = EditMode.UNKNOWN;
            
        }catch(SQLException e){
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
    
    public boolean isPrintedStatus() {
        return RecordStatus.ACTIVE.equals(getValue("cPrintedx"));
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
}
