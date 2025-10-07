package ph.com.guanzongroup.cas.purchasing.module.mnv.models;

import java.sql.SQLException;
import java.util.Date;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.cas.parameter.model.Model_Branch;
import org.guanzon.cas.parameter.model.Model_Category;
import org.guanzon.cas.parameter.model.Model_Company;
import org.guanzon.cas.parameter.model.Model_Department;
import org.guanzon.cas.parameter.model.Model_Industry;
import org.guanzon.cas.parameter.services.ParamModels;
import org.guanzon.cas.purchasing.model.Model_PO_Master;
import org.guanzon.cas.purchasing.services.PurchaseOrderModels;
import org.json.simple.JSONObject;

/**
 *
 * @author maynevval 10-07-2025 3PM
 */
public class Model_PO_Cancellation_Master extends Model {

    //reference objects
    Model_Industry poIndustry;
    Model_Category poCategory;
    Model_Company poCompany;
    Model_Department poDepartment;
    Model_Branch poBranch;
    Model_PO_Master poPurchaseOrder;

    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            poEntity.updateString("sBranchCd", poGRider.getBranchCode());
            poEntity.updateNull("sDeptIDxx");
            poEntity.updateObject("dTransact", poGRider.getServerDate());
            poEntity.updateNull("sCompnyID");
            poEntity.updateNull("sSupplier");
            poEntity.updateNull("sAddrssID");
            poEntity.updateNull("sContctID");
            poEntity.updateNull("sContctID");
            poEntity.updateNull("dRefernce");
            poEntity.updateDouble("nTranTotl", 0.00d);
            poEntity.updateObject("nEntryNox", 0);
            poEntity.updateString("cPrintedx", "0");
            poEntity.updateString("cVATaxabl", "0");
            poEntity.updateDouble("nVATRatex", 0.00d);
            poEntity.updateDouble("nTWithHld", 0.00d);
            poEntity.updateDouble("nDiscount", 0.00d);
            poEntity.updateDouble("nAddDiscx", 0.00d);
            poEntity.updateDouble("nFreightx", 0.00d);
            poEntity.updateNull("sSourceNo");
            poEntity.updateNull("sSourceCd");
            poEntity.updateNull("sInvTypCd");
            poEntity.updateNull("sEntryByx");
            poEntity.updateNull("dEntryDte");
            poEntity.updateObject("dModified", poGRider.getServerDate());
            poEntity.updateString("cTranStat", "0");

            this.poBranch = (new ParamModels(this.poGRider)).Branch();
            this.poDepartment = (new ParamModels(this.poGRider)).Department();
            this.poIndustry = (new ParamModels(this.poGRider)).Industry();
            this.poCategory = (new ParamModels(this.poGRider)).Category();
            this.poCompany = (new ParamModels(this.poGRider)).Company();
            this.poPurchaseOrder = new PurchaseOrderModels(poGRider).PurchaseOrderMaster();

            poEntity.insertRow();
            poEntity.moveToCurrentRow();

            poEntity.absolute(1);

            ID = poEntity.getMetaData().getColumnLabel(1);

            //add model here
            pnEditMode = EditMode.UNKNOWN;

        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
    }
    //Getter & Setter 
    //sTransNox
    //sBranchCd
    //sDeptIDxx
    //sIndstCdx*
    //dTransact
    //sCompnyID*
    //sSupplier*
    //sAddrssID
    //sContctID
    //sReferNox
    //dRefernce
    //nTranTotl
    //cVATaxabl
    //nVATRatex
    //nTWithHld
    //nDiscount
    //nAddDiscx
    //nFreightx
    //sRemarksx
    //sSourceNo
    //sSourceCd
    //nEntryNox
    //sInvTypCd
    //cTranStat
    //sEntryByx
    //dEntryDte
    //sModified
    //dModified

    //sTransNox
    public JSONObject setTransactionNo(String transactionNo) {
        return setValue("sTransNox", transactionNo);
    }

    public String getTransactionNo() {
        return (String) getValue("sTransNox");
    }

    //sBranchCd
    public JSONObject setBranchCd(String branchCd) {
        return setValue("sBranchCd", branchCd);
    }

    public String getBranchCd() {
        return (String) getValue("sBranchCd");
    }

    //sDeptIDxx
    public JSONObject setDepartment(String destination) {
        return setValue("sDeptIDxx", destination);
    }

    public String getDepartment() {
        return (String) getValue("sDeptIDxx");
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

    //sCompnyID
    public JSONObject setCompanyID(String companyID) {
        return setValue("sCompnyID", companyID);
    }

    public String getCompanyID() {
        return (String) getValue("sCompnyID");
    }

    //sSupplier
    public JSONObject setSupplierID(String supplierID) {
        return setValue("sSupplier", supplierID);
    }

    public String getSupplierID() {
        return (String) getValue("sSupplier");
    }

    //sAddrssID
    public JSONObject setAddressID(String addressID) {
        return setValue("sAddrssID", addressID);
    }

    public String getAddressID() {
        return (String) getValue("sAddrssID");
    }

    //sContctID
    public JSONObject setContactID(String contactID) {
        return setValue("sContctID", contactID);
    }

    public String getContactID() {
        return (String) getValue("sContctID");
    }

    //sReferNox
    public JSONObject setReference(String reference) {
        return setValue("sReferNox", reference);
    }

    public String getReference() {
        return (String) getValue("sReferNox");
    }

    //dRefernce
    public JSONObject setPreparedDate(Date reference) {
        return setValue("dRefernce", reference);
    }

    public Date getReferenceDate() {
        return (Date) getValue("dRefernce");
    }

    //nTranTotl
    public JSONObject setTransactionTotal(Double transactionTotal) {
        return setValue("nTranTotl", transactionTotal);
    }

    public Double getTransactionTotal() {
        return Double.valueOf(getValue("nTranTotl").toString());
    }

    //cVATaxabl
    public JSONObject setTaxable(String taxable) {
        return setValue("cVATaxabl", taxable);
    }

    public String getTaxable() {
        return (String) getValue("cVATaxabl");
    }

    public boolean isTaxable() {
        return RecordStatus.ACTIVE.equals(getValue("cVATaxabl"));
    }

    //nVATRatex
    public JSONObject setVATRate(Double vatrate) {
        return setValue("nVATRatex", vatrate);
    }

    public Double getVATRate() {
        return Double.valueOf(getValue("nVATRatex").toString());
    }

    //nTWithHld
    public JSONObject setTaxWithHolding(Double taxWithHolding) {
        return setValue("nTWithHld", taxWithHolding);
    }

    public Double getTaxWithHolding() {
        return Double.valueOf(getValue("nTWithHld").toString());
    }

    //nDiscount
    public JSONObject setDiscount(Number discount) {
        return setValue("nDiscount", discount);
    }

    public Number getDiscount() {
        return (Number) getValue("nDiscount");
    }

    //nAddDiscx
    public JSONObject setAdditionalDiscount(Number additionalDiscount) {
        return setValue("nAddDiscx", additionalDiscount);
    }

    public Number getAdditionalDiscount() {
        return (Number) getValue("nAddDiscx");
    }

    //nFreightx
    public JSONObject setFreight(Number freight) {
        return setValue("nFreightx", freight);
    }

    public Number getFreight() {
        return (Number) getValue("nFreightx");
    }

    //sRemarksx
    public JSONObject setRemarks(String remarks) {
        return setValue("cVATaxabl", remarks);
    }

    public String getRemarks() {
        return (String) getValue("sRemarksx");
    }

    //sSourceNo
    public JSONObject setSourceNo(String source) {
        return setValue("sSourceNo", source);
    }

    public String getSourceNo() {
        return (String) getValue("sSourceNo");
    }

    //sSourceCd
    public JSONObject setSourceCode(String sourcecd) {
        return setValue("sSourceCd", sourcecd);
    }

    public String getSourceCode() {
        return (String) getValue("sSourceCd");
    }

    //nEntryNox
    public JSONObject setEntryNo(int entryNo) {
        return setValue("nEntryNox", entryNo);
    }

    public int getEntryNo() {
        return (int) getValue("nEntryNox");
    }

    //sInvTypCd
    public JSONObject setInventoryTypeCode(String invTypeCode) {
        return setValue("sInvTypCd", invTypeCode);
    }

    public String getInventoryTypeCode() {
        return (String) getValue("sInvTypCd");
    }

    //cTranStat
    public JSONObject setTransactionStatus(String transactionStatus) {
        return setValue("cTranStat", transactionStatus);
    }

    public String getTransactionStatus() {
        return (String) getValue("cTranStat");
    }

    //sEntryByx
    public JSONObject setEntryId(String entryId) {
        return setValue("sEntryByx", entryId);
    }

    public String getEntryId() {
        return (String) getValue("sEntryByx");
    }

    //dEntryDte
    public JSONObject setEntryDate(Date modifiedDate) {
        return setValue("sEntryByx", modifiedDate);
    }

    public Date getEntryDate() {
        return (Date) getValue("sEntryByx");
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

    public Model_Branch Branch() throws GuanzonException, SQLException {
        if (!"".equals((String) getValue("sDestinat"))) {
            if (poBranch.getEditMode() == EditMode.READY
                    && poBranch.getBranchCode().equals((String) getValue("sDestinat"))) {
                return poBranch;
            } else {
                poJSON = poBranch.openRecord((String) getValue("sDestinat"));
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

    public Model_Industry Industry() throws GuanzonException, SQLException {
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

    public Model_Category Category() throws GuanzonException, SQLException {
        if (!"".equals((String) getValue("sCategrCd"))) {
            if (poCategory.getEditMode() == EditMode.READY
                    && poCategory.getCategoryId().equals((String) getValue("sCategrCd"))) {
                return poCategory;
            } else {
                poJSON = poCategory.openRecord((String) getValue("sCategrCd"));
                if ("success".equals((String) poJSON.get("result"))) {
                    return poCategory;
                } else {
                    poCategory.initialize();
                    return poCategory;
                }
            }
        } else {
            poCategory.initialize();
            return poCategory;
        }
    }

    public Model_Company Company() throws GuanzonException, SQLException {
        if (!"".equals((String) getValue("sCompnyID"))) {
            if (poCompany.getEditMode() == EditMode.READY
                    && poCompany.getCompanyId().equals((String) getValue("sCompnyID"))) {
                return poCompany;
            } else {
                poJSON = poCompany.openRecord((String) getValue("sCompnyID"));
                if ("success".equals((String) poJSON.get("result"))) {
                    return poCompany;
                } else {
                    poCompany.initialize();
                    return poCompany;
                }
            }
        } else {
            poCompany.initialize();
            return poCompany;
        }
    }

    public Model_Department Department() throws SQLException, GuanzonException {
        if (!"".equals(getValue("sDeptIDxx"))) {
            if (this.poDepartment.getEditMode() == 1 && this.poDepartment
                    .getDepartmentId().equals(getValue("sDeptIDxx"))) {
                return this.poDepartment;
            }
            this.poJSON = this.poDepartment.openRecord((String) getValue("sDeptIDxx"));
            if ("success".equals(this.poJSON.get("result"))) {
                return this.poDepartment;
            }
            this.poDepartment.initialize();
            return this.poDepartment;
        }
        this.poDepartment.initialize();
        return this.poDepartment;
    }

    public Model_PO_Master CheckPayment() throws SQLException, GuanzonException {
        if (!"".equals(getValue("sOrderNox"))) {
            if (this.poPurchaseOrder.getEditMode() == 1 && this.poPurchaseOrder
                    .getTransactionNo().equals(getValue("sOrderNox"))) {
                return this.poPurchaseOrder;
            }
            this.poJSON = this.poPurchaseOrder.openRecord((String) getValue("sOrderNox"));
            if ("success".equals(this.poJSON.get("result"))) {
                return this.poPurchaseOrder;
            }
            this.poPurchaseOrder.initialize();
            return this.poPurchaseOrder;
        }
        poPurchaseOrder.initialize();
        return this.poPurchaseOrder;
    }

}
