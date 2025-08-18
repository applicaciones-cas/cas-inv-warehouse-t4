package ph.com.guanzongroup.cas.inv.warehouse.t4.parameter;

import java.sql.SQLException;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.cas.client.Client;
import org.guanzon.cas.client.model.Model_Client_Master;
import org.guanzon.cas.client.services.ClientControllers;
import org.guanzon.cas.inv.InvMaster;
import org.guanzon.cas.inv.InvSerial;
import org.guanzon.cas.inv.Inventory;
import org.guanzon.cas.inv.model.Model_Inv_Master;
import org.guanzon.cas.inv.model.Model_Inv_Serial;
import org.guanzon.cas.inv.model.Model_Inventory;
import org.guanzon.cas.inv.services.InvControllers;
import org.guanzon.cas.parameter.Branch;
import org.guanzon.cas.parameter.Brand;
import org.guanzon.cas.parameter.Category;
import org.guanzon.cas.parameter.CategoryLevel2;
import org.guanzon.cas.parameter.CategoryLevel3;
import org.guanzon.cas.parameter.CategoryLevel4;
import org.guanzon.cas.parameter.Industry;
import org.guanzon.cas.parameter.InvType;
import org.guanzon.cas.parameter.Measure;
import org.guanzon.cas.parameter.Model;
import org.guanzon.cas.parameter.ModelVariant;
import org.guanzon.cas.parameter.model.Model_Branch;
import org.guanzon.cas.parameter.model.Model_Brand;
import org.guanzon.cas.parameter.model.Model_Category;
import org.guanzon.cas.parameter.model.Model_Category_Level2;
import org.guanzon.cas.parameter.model.Model_Category_Level3;
import org.guanzon.cas.parameter.model.Model_Category_Level4;
import org.guanzon.cas.parameter.model.Model_Color;
import org.guanzon.cas.parameter.model.Model_Industry;
import org.guanzon.cas.parameter.model.Model_Inv_Type;
import org.guanzon.cas.parameter.model.Model_Measure;
import org.guanzon.cas.parameter.model.Model_Model;
import org.guanzon.cas.parameter.model.Model_Model_Variant;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.json.simple.JSONObject;

/**
 *
 * @author Maynard 2025-08-15
 *
 * Sample Implementation @
 *
 * InventoryBrowse loBrowse = new InventoryBrowse(poGRider,poLogWrapper)
 * loBrowse.initTransaction();
 *
 * //set parameter Filter for queries
 *
 * loBrowse.setIndustry(getMaster().getIndustry); // or passIndustry *
 * loBrowse.setCategoryFilters("0001»0002»0003") ; addmore filter if neccessary
 *
 * @usage of withStockSearch
 *
 * //set a Branch for (POS,TRANSFER,BRANCH ADJUSTMENT(ONLINE TRANSACTION))
 *
 * loBrowse.setBranch(poGRider.getBranchCd);
 *
 * @usage of withStockSearch incase not validating stock Qty on Hand
 * loBrowse.setisWithQuantityStock (false)//default true
 *
 * loBrowse if (psSupplier != null){
 * loBrowse.setInventorySuppplier("yoursupplier") ; }
 *
 * setCustomHeader (Optional) if modified set all 3 else default before calling
 * function
 *
 * loBrowse.setCustomColHeader = "yourcolheader"
 *
 * loBrowse.setCustomColName = "yourcolumnname"
 *
 * loBrowse.setCustomColCriteria = "yourcolumncriteria"
 *
 *
 * poJSON = searchInventory(value,bycode,true); // Inventory only (FOR
 * MAINTENANCE)STOCKID
 *
 * poJSON = searchInventory(value,bycode); // Inventory only (FOR MAINTENANCE)
 *
 * poJSON = searchInventoryWithStock(value,bycode); // Inventory with Master
 * (BranchFilter) (POS SP/OTHER None Serialize)
 *
 * poJSON = searchInventorySerial(value,bycode);//Inventory with Inventory
 * Serial (FOR JO AND REPAIR DIFFERENT BRANCH AFTER SALES)
 *
 * poJSON = searchInventorySerialWithStock(value,bycode);//Inventory with
 * Inventory Master (BranchFilter) & Inventory Serial (FOR MC/CAR/Serialize
 * Product needed)
 *
 * poJSON = searchInventoryIssuance(value,bycode);// Inventory with Inventory
 * Serial(POS,Transfer,Return usage)
 *
 *
 * if ("!error".equals((String) poJSON.get("result"))) { set required or need
 *
 * function getDetail(row).setStockIDx =
 * loBrowse.getModelInventory().getStockIDx; }
 */
public class InventoryBrowse {

    private final GRiderCAS poGRider;
    private LogWrapper poLogWrapper;
    private JSONObject poJSON;
    private Inventory poInventory;
    private InvMaster poInvMaster;
    private InvSerial poInventorySerial;

    private Branch poBranch;
    private Industry poIndustry;
    private InventorySupplier poInvSupplier;
    private Client poSupplierCompany;
    private Category poCategory1;
    private Category poCategory2;
    private Category poCategory3;
    private Category poCategory4;
    private CategoryLevel2 poCategoryLevel2;
    private CategoryLevel3 poCategoryLevel3;
    private CategoryLevel4 poCategoryLevel4;
    private Brand poBrand;
    private Model poModel;
    private org.guanzon.cas.parameter.Color poColor;
    private Measure poMeasure;
    private InvType poInvType;
    private ModelVariant poModelVariant;
    private String psRecdStat;
    private String psCustomHeader = "";
    private String psCustomName = "";
    private String psCustomCriteria = "";
    private boolean pbisWithQty = true;
    private boolean pbisSerialize = true;

    public InventoryBrowse(GRiderCAS applicationDriver, LogWrapper logWrapper) {
        this.poGRider = applicationDriver;
        this.poLogWrapper = logWrapper;
    }

    public JSONObject initTransaction() throws GuanzonException, SQLException {
        InvControllers inventoryObject = new InvControllers(poGRider, poLogWrapper);
        ParamControllers parameterObject = new ParamControllers(poGRider, poLogWrapper);
        poSupplierCompany = new ClientControllers(poGRider, poLogWrapper).Client();
        poSupplierCompany.Master().setRecordStatus(RecordStatus.ACTIVE);
        poSupplierCompany.Master().setClientType("1");

        // Inventory-related
        this.poInventory = inventoryObject.Inventory();
        this.poInvMaster = inventoryObject.InventoryMaster();
        this.poInventorySerial = inventoryObject.InventorySerial();

        this.poInvMaster.setRecordStatus(psRecdStat);
        this.poInventory.setRecordStatus(psRecdStat);
        this.poInventorySerial.setRecordStatus(psRecdStat);

        // Parameter-related
        this.poBranch = parameterObject.Branch();
        this.poIndustry = parameterObject.Industry();
        this.poCategory1 = parameterObject.Category();
        this.poCategory2 = parameterObject.Category();
        this.poCategory3 = parameterObject.Category();
        this.poCategory4 = parameterObject.Category();
        this.poCategoryLevel2 = parameterObject.CategoryLevel2();
        this.poCategoryLevel3 = parameterObject.CategoryLevel3();
        this.poCategoryLevel4 = parameterObject.CategoryLevel4();
        this.poBrand = parameterObject.Brand();
        this.poModel = parameterObject.Model();
        this.poColor = parameterObject.Color();
        this.poMeasure = parameterObject.Measurement();
        this.poInvType = parameterObject.InventoryType();
        this.poModelVariant = parameterObject.ModelVariant();

        // Set status for all parameter objects
        setRecordStatusForParams(psRecdStat);

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        return poJSON;
    }

    private void setRecordStatusForParams(String status) {
        poBranch.setRecordStatus(status);
        poIndustry.setRecordStatus(status);
        poCategory1.setRecordStatus(status);
        poCategory2.setRecordStatus(status);
        poCategory3.setRecordStatus(status);
        poCategory4.setRecordStatus(status);
        poCategoryLevel2.setRecordStatus(status);
        poCategoryLevel3.setRecordStatus(status);
        poCategoryLevel4.setRecordStatus(status);
        poBrand.setRecordStatus(status);
        poModel.setRecordStatus(status);
        poColor.setRecordStatus(status);
        poMeasure.setRecordStatus(status);
        poInvType.setRecordStatus(status);
        poModelVariant.setRecordStatus(status);
    }

    public void setRecordStatus(String recordStatus) {
        psRecdStat = recordStatus;
    }

    public void isWithQuantityStock(boolean isWithStockQuantity) {
        pbisWithQty = isWithStockQuantity;
    }

    public void isSerializeInventory(boolean isSerializeInveotry) {
        pbisSerialize = isSerializeInveotry;
    }

    public void setCustomColHeader(String columnheaderNameSearch) {
        psCustomHeader = columnheaderNameSearch;
    }

    public void setCustomColName(String columnNameSearch) {
        psCustomName = columnNameSearch;
    }

    public void setCustomColCriteria(String columnCriteriaSearch) {
        psCustomCriteria = columnCriteriaSearch;
    }

    public JSONObject setBranch(String brancdCd) throws SQLException, GuanzonException {
        this.poJSON = new JSONObject();
        return poBranch.openRecord(brancdCd);
    }

    public JSONObject setIndustry(String indusrty) throws SQLException, GuanzonException {
        this.poJSON = new JSONObject();
        return poIndustry.openRecord(indusrty);
    }

    public JSONObject setCategoryFilters(String categoryFilter) throws SQLException, GuanzonException {
        String[] filters = categoryFilter.split("»"); // Split by the » symbol
        poJSON = new JSONObject();

        if (filters.length > 0) {
            poCategory1.openRecord(filters[0]);
        }
        if (filters.length > 1) {
            poCategory2.openRecord(filters[1]);
        }
        if (filters.length > 2) {
            poCategory3.openRecord(filters[2]);
        }
        if (filters.length > 3) {
            poCategory4.openRecord(filters[3]);
        }

        poJSON.put("result", "success");
        return poJSON;

    }

    //Set Filter Purpose
    public JSONObject setSupplier(String supplierFilter) throws SQLException, GuanzonException {
        this.poJSON = new JSONObject();
        return poSupplierCompany.Master().openRecord(supplierFilter);
    }

    public JSONObject setCategory1(String categoryfilter) throws SQLException, GuanzonException {
        this.poJSON = new JSONObject();
        return poCategory1.openRecord(categoryfilter);
    }

    public JSONObject setCategory2(String categoryfilter) throws SQLException, GuanzonException {
        this.poJSON = new JSONObject();
        return poCategory2.openRecord(categoryfilter);
    }

    public JSONObject setCategory3(String categoryfilter) throws SQLException, GuanzonException {
        this.poJSON = new JSONObject();
        return poCategory3.openRecord(categoryfilter);
    }

    public JSONObject setCategory4(String categoryfilter) throws SQLException, GuanzonException {
        this.poJSON = new JSONObject();
        return poCategory4.openRecord(categoryfilter);
    }

    public JSONObject setCategoryLevel2(String categorylevel2filter) throws SQLException, GuanzonException {
        this.poJSON = new JSONObject();
        return poCategoryLevel2.openRecord(categorylevel2filter);
    }

    public JSONObject setCategoryLevel3(String categorylevel3filter) throws SQLException, GuanzonException {
        this.poJSON = new JSONObject();
        return poCategoryLevel3.openRecord(categorylevel3filter);
    }

    public JSONObject setCategoryLevel4(String categorylevel4filter) throws SQLException, GuanzonException {
        this.poJSON = new JSONObject();
        return poCategoryLevel4.openRecord(categorylevel4filter);
    }

    public JSONObject setBrand(String brandIDFilter) throws SQLException, GuanzonException {
        this.poJSON = new JSONObject();
        return poBrand.openRecord(brandIDFilter);
    }

    public JSONObject setModel(String modelIDFilter) throws SQLException, GuanzonException {
        this.poJSON = new JSONObject();
        return poModel.openRecord(modelIDFilter);
    }

    public JSONObject setColor(String colorIDFilter) throws SQLException, GuanzonException {
        this.poJSON = new JSONObject();
        return poColor.openRecord(colorIDFilter);
    }

    public JSONObject setMeasure(String measureIDFilter) throws SQLException, GuanzonException {
        this.poJSON = new JSONObject();
        return poMeasure.openRecord(measureIDFilter);
    }

    public JSONObject setInvType(String invTypeIDFilter) throws SQLException, GuanzonException {
        this.poJSON = new JSONObject();
        return poInvType.openRecord(invTypeIDFilter);
    }

    public JSONObject setModelVariant(String modelVariantIDFilter) throws SQLException, GuanzonException {
        this.poJSON = new JSONObject();
        return poModelVariant.openRecord(modelVariantIDFilter);
    }

    public JSONObject searchIndustry(String searchIndustryFilter) throws SQLException, GuanzonException {
        this.poJSON = new JSONObject();
        return poIndustry.searchRecord(searchIndustryFilter, false);
    }

    public JSONObject searchCategory1(String searchCategory1Filter) throws SQLException, GuanzonException {
        this.poJSON = new JSONObject();
        return poCategory1.searchRecord(searchCategory1Filter, false);
    }

    public JSONObject searchCategory2(String searchCategory2Filter) throws SQLException, GuanzonException {
        this.poJSON = new JSONObject();
        return poCategory2.searchRecord(searchCategory2Filter, false);
    }

    public JSONObject searchCategory3(String searchCategory3Filter) throws SQLException, GuanzonException {
        this.poJSON = new JSONObject();
        return poCategory3.searchRecord(searchCategory3Filter, false);
    }

    public JSONObject searchCategory4(String searchCategory4Filter) throws SQLException, GuanzonException {
        this.poJSON = new JSONObject();
        return poCategory3.searchRecord(searchCategory4Filter, false);
    }

    public JSONObject searchCategoryLevel2(String searchCategoryLevel2Filter) throws SQLException, GuanzonException {
        this.poJSON = new JSONObject();
        return poCategoryLevel2.searchRecord(searchCategoryLevel2Filter, false);
    }

    public JSONObject searchCategoryLevel3(String searchCategoryLevel3Filter) throws SQLException, GuanzonException {
        this.poJSON = new JSONObject();
        return poCategoryLevel3.searchRecord(searchCategoryLevel3Filter, false);
    }

    public JSONObject searchCategoryLevel4(String searchCategoryLevel4Filter) throws SQLException, GuanzonException {
        this.poJSON = new JSONObject();
        return poCategoryLevel4.searchRecord(searchCategoryLevel4Filter, false);
    }

    public JSONObject searchBrand(String searchBrandFilter) throws SQLException, GuanzonException {
        this.poJSON = new JSONObject();
        return poBrand.searchRecord(searchBrandFilter, false);
    }

    public JSONObject searchModel(String searchBrandFilter) throws SQLException, GuanzonException {
        this.poJSON = new JSONObject();
        return poModel.searchRecord(searchBrandFilter, false);
    }

    public JSONObject searchColor(String searchBrandFilter) throws SQLException, GuanzonException {
        this.poJSON = new JSONObject();
        return poColor.searchRecord(searchBrandFilter, false);
    }

    public JSONObject searchMeasure(String searchMeasureFilter) throws SQLException, GuanzonException {
        this.poJSON = new JSONObject();
        return poMeasure.searchRecord(searchMeasureFilter, false);
    }

    public JSONObject searchInvType(String searchInvTypeFilter) throws SQLException, GuanzonException {
        this.poJSON = new JSONObject();
        return poInvType.searchRecord(searchInvTypeFilter, false);
    }

    public JSONObject searchModelVariant(String searchModelVariantFilter) throws SQLException, GuanzonException {
        this.poJSON = new JSONObject();
        return poModelVariant.searchRecord(searchModelVariantFilter, false);
    }

    // FOR UI PURPOSE'S
    public Model_Client_Master getModelSupplierMaster() {
        return poSupplierCompany.Master().getModel();
    }

    public Client getSupplier() {
        return poSupplierCompany;
    }

    public Model_Inventory getModelInventory() {
        return poInventory.getModel();
    }

    public Model_Inv_Serial getModelInventorySerial() {
        return poInventorySerial.getModel();
    }

    public Model_Inv_Master getModelInventoryMaster() {
        return poInvMaster.getModel();
    }

    public Model_Branch getModelBranch() {
        return poBranch.getModel();
    }

    public Model_Industry getModelIndustry() {
        return poIndustry.getModel();
    }

    public Model_Category getModelCategory() {
        return poCategory1.getModel();
    }

    public Model_Category getModelCategory2() {
        return poCategory2.getModel();
    }

    public Model_Category getModelCategory3() {
        return poCategory3.getModel();
    }

    public Model_Category getModelCategory4() {
        return poCategory4.getModel();
    }

    public Model_Category_Level2 getModelCategoryLevel2() {
        return poCategoryLevel2.getModel();
    }

    public Model_Category_Level3 getModelCategoryLevel3() {
        return poCategoryLevel3.getModel();
    }

    public Model_Category_Level4 getModelCategoryLevel4() {
        return poCategoryLevel4.getModel();
    }

    public Model_Brand getModelBrand() {
        return poBrand.getModel();
    }

    public Model_Model getModelModel() {
        return poModel.getModel();
    }

    public Model_Color getModelColor() {
        return poColor.getModel();
    }

    public Model_Measure getModelMeasure() {
        return poMeasure.getModel();
    }

    public Model_Inv_Type getModelInvType() {
        return poInvType.getModel();
    }

    public Model_Model_Variant getModelModel_Variant() {
        return poModelVariant.getModel();
    }

    public JSONObject searchInventory(String value, boolean byCode) throws SQLException, GuanzonException {
        String lsSQL = getSQ_BrowseInventory();
        String lsCondition = generateConditionInventory(false);
        if (!lsCondition.isEmpty()) {
            lsSQL = MiscUtil.addCondition(lsSQL, lsCondition);
        }

        //default
        String lscolHeader = "Barcode»Description»Brand»Model»UOM";
        String lscolName = "sBarCodex»sDescript»xBrandNme»xModelNme»xMeasurNm";
        String lscolCriteria = "a.sBarCodex»a.sDescript»IFNULL(b.sDescript, '')»IFNULL(c.sDescript, '')»IFNULL(e.sDescript, '')";

        if (!psCustomHeader.isEmpty() && !psCustomName.isEmpty() && !psCustomCriteria.isEmpty()) {
            lscolHeader = psCustomHeader;
            lscolName = psCustomName;
            lscolCriteria = psCustomCriteria;
        }

        System.out.println("Search Dialog Query : " + lsSQL);
        this.poJSON = ShowDialogFX.Search(
                poGRider,
                lsSQL,
                value,
                lscolHeader,
                lscolName,
                lscolCriteria,
                byCode ? 0 : 1
        );
        if (this.poJSON != null) {
            return this.poInventory.openRecord((String) this.poJSON.get("sStockIDx"));

        }
        this.poJSON = new JSONObject();
        this.poJSON.put("result", "error");
        this.poJSON.put("message", "No record loaded.");
        return this.poJSON;
    }

    public JSONObject searchInventory(String value, boolean byCode, boolean byExactStockID) throws SQLException, GuanzonException {
        String lsSQL = getSQ_BrowseInventory();
        String lsCondition = generateConditionInventory(false);
        if (!lsCondition.isEmpty()) {
            lsSQL = MiscUtil.addCondition(lsSQL, lsCondition);
        }
        //default
        String lscolHeader = "ID»Barcode»Description»Brand»Model»UOM";
        String lscolName = "sStockIDx»sBarCodex»sDescript»xBrandNme»xModelNme»xMeasurNm";
        String lscolCriteria = "a.sStockIDx»a.sBarCodex»a.sDescript»IFNULL(b.sDescript, '')»IFNULL(c.sDescript, '')»IFNULL(e.sDescript, '')";

        if (!psCustomHeader.isEmpty() && !psCustomName.isEmpty() && !psCustomCriteria.isEmpty()) {
            lscolHeader = psCustomHeader;
            lscolName = psCustomName;
            lscolCriteria = psCustomCriteria;
        }

        System.out.println("Search Dialog Query : " + lsSQL);
        this.poJSON = ShowDialogFX.Search(
                poGRider,
                lsSQL,
                value,
                lscolHeader,
                lscolName,
                lscolCriteria,
                byExactStockID ? 0 : byCode ? 1 : 2
        );

        if (this.poJSON != null) {
            return this.poInventory.openRecord((String) this.poJSON.get("sStockIDx"));

        }
        this.poJSON = new JSONObject();
        this.poJSON.put("result", "error");
        this.poJSON.put("message", "No record loaded.");
        return this.poJSON;
    }

    public JSONObject searchInventorySerial(String value, boolean byCode) throws SQLException, GuanzonException {
        String lsSQL = getSQ_BrowseInventorySerial();

        String lsCondition = generateConditionSerial();
        if (!lsCondition.isEmpty()) {
            lsSQL = MiscUtil.addCondition(lsSQL, lsCondition);
        }

        //default
        String lscolHeader = "ID»Description»Serial 01»Serial 02";
        String lscolName = "sSerialID»xDescript»sSerial01»sSerial02";
        String lscolCriteria = "a.sSerialID»b.sDescript»a.sSerial01»a.sSerial02";

        if (!psCustomHeader.isEmpty() && !psCustomName.isEmpty() && !psCustomCriteria.isEmpty()) {
            lscolHeader = psCustomHeader;
            lscolName = psCustomName;
            lscolCriteria = psCustomCriteria;
        }

        System.out.println("Search Dialog Query : " + lsSQL);
        this.poJSON = ShowDialogFX.Search(
                poGRider,
                lsSQL,
                value,
                lscolHeader,
                lscolName,
                lscolCriteria,
                byCode ? 0 : 1
        );
        if (this.poJSON != null) {
            JSONObject result = new JSONObject();
            result = this.poInventorySerial.openRecord((String) this.poJSON.get("sSerialID"));
            if ("error".equals((String) result.get("result"))) {
                return poJSON;
            }
            result = this.poInventory.openRecord((String) this.poJSON.get("sStockIDx"));
            if ("error".equals((String) result.get("result"))) {
                return poJSON;
            }

            this.poJSON.put("result", "success");
            return poJSON;
        }
        this.poJSON = new JSONObject();
        this.poJSON.put("result", "error");
        this.poJSON.put("message", "No record loaded.");
        return this.poJSON;

    }

    public JSONObject searchInventoryWithStock(String value, boolean byCode) throws SQLException, GuanzonException {
        String lsSQL = getSQ_BrowseInventorywithStock();

        String lsCondition = generateConditionInventory(true);
        if (!lsCondition.isEmpty()) {
            lsSQL = MiscUtil.addCondition(lsSQL, lsCondition);
        }

        if (pbisWithQty) {
            lsSQL = MiscUtil.addCondition(lsSQL, "bb.nQtyOnHnd > 0");
        }
        if (!pbisSerialize) {
            lsSQL = MiscUtil.addCondition(lsSQL, "a.cSerialze = 0");
        } else {
            lsSQL = MiscUtil.addCondition(lsSQL, "a.cSerialze = 1");
        }
        //default
        String lscolHeader = "Barcode»Description»Brand»Model»UOM»Branch Name";
        String lscolName = "sBarCodex»sDescript»xBrandNme»xModelNme»xMeasurNm»xBranchNm";
        String lscolCriteria = "a.sBarCodex»a.sDescript»IFNULL(b.sDescript, '')»IFNULL(c.sDescript, '')»IFNULL(e.sDescript, '')»IFNULL(bb.sBranchNm, '')";

        if (!psCustomHeader.isEmpty() && !psCustomName.isEmpty() && !psCustomCriteria.isEmpty()) {
            lscolHeader = psCustomHeader;
            lscolName = psCustomName;
            lscolCriteria = psCustomCriteria;
        }

        System.out.println("Search Dialog Query : " + lsSQL);
        this.poJSON = ShowDialogFX.Search(
                poGRider,
                lsSQL,
                value,
                lscolHeader,
                lscolName,
                lscolCriteria,
                byCode ? 0 : 1
        );
        if (this.poJSON != null) {
            JSONObject result = new JSONObject();
            result = this.poInvMaster.openRecord((String) this.poJSON.get("sStockIDx"), (String) this.poJSON.get("sBranchCd"));
            if ("error".equals((String) result.get("result"))) {
                return poJSON;
            }
            result = this.poInventory.openRecord((String) this.poJSON.get("sStockIDx"));
            if ("error".equals((String) result.get("result"))) {
                return poJSON;
            }

            this.poJSON.put("result", "success");
            return poJSON;

        }
        this.poJSON = new JSONObject();
        this.poJSON.put("result", "error");
        this.poJSON.put("message", "No record loaded.");
        return this.poJSON;
    }

    public JSONObject searchInventorySerialWithStock(String value, boolean byCode) throws SQLException, GuanzonException {
        String lsSQL = getSQ_BrowseInventorySerialwithStock();

        String lsCondition = generateConditionSerial();
        if (!lsCondition.isEmpty()) {
            lsSQL = MiscUtil.addCondition(lsSQL, lsCondition);
        }

        if (pbisWithQty) {
            lsSQL = MiscUtil.addCondition(lsSQL, "bb.nQtyOnHnd > 0");
            lsSQL = MiscUtil.addCondition(lsSQL, "a.cSoldStat = '0' ");
        }

        //default
        String lscolHeader = "ID»Description»Serial 01»Serial 02»Branch Name";
        String lscolName = "sSerialID»xDescript»sSerial01»sSerial02»xBranchNm";
        String lscolCriteria = "a.sSerialID»b.sDescript»a.sSerial01»a.sSerial02»IFNULL(bb.sBranchNm, '')";

        if (!psCustomHeader.isEmpty() && !psCustomName.isEmpty() && !psCustomCriteria.isEmpty()) {
            lscolHeader = psCustomHeader;
            lscolName = psCustomName;
            lscolCriteria = psCustomCriteria;
        }

        this.poJSON = ShowDialogFX.Search(
                poGRider,
                lsSQL,
                value,
                lscolHeader,
                lscolName,
                lscolCriteria,
                byCode ? 2 : 1
        );
        if (this.poJSON != null) {
            JSONObject result = new JSONObject();

            result = this.poInvMaster.openRecord((String) this.poJSON.get("sStockIDx"), (String) this.poJSON.get("sBranchCd"));
            if ("error".equals((String) result.get("result"))) {
                return poJSON;
            }

            result = this.poInventorySerial.openRecord((String) this.poJSON.get("sSerialID"));
            if ("error".equals((String) result.get("result"))) {
                return poJSON;
            }
            result = this.poInventory.openRecord((String) this.poJSON.get("sStockIDx"));
            if ("error".equals((String) result.get("result"))) {
                return poJSON;
            }

            this.poJSON.put("result", "success");
            return poJSON;

        }
        this.poJSON = new JSONObject();
        this.poJSON.put("result", "error");
        this.poJSON.put("message", "No record loaded.");
        return this.poJSON;
    }

    public JSONObject searchInventoryIssaunce(String value, boolean byCode) throws SQLException, GuanzonException {
        String lsSQL = getSQ_BrowseInventoryIssuance();

        String lsCondition = generateConditionInventory(true);
        if (!lsCondition.isEmpty()) {
            lsSQL = MiscUtil.addCondition(lsSQL, lsCondition);
        }

        if (pbisWithQty) {
            lsSQL = MiscUtil.addCondition(lsSQL, "bb.nQtyOnHnd > 0");
            lsSQL = MiscUtil.addCondition(lsSQL, "a.cSoldStat = '0' ");
        }

        //default
        String lscolHeader = "Serial»Barcode»Description»Qty-On-Hand»Brand Name»Model Name»Color Name»UOM»Variant Name»Model Code";
        String lscolName = "xSerialNme»sBarcodex»xDescript»nQtyOnHnd»xBrandNme»xModelNme»xColorNme»xMeasurNm»xVrntName»xModelCde";
        String lscolCriteria = "xSerialNme»sBarcodex»xDescript»nQtyOnHnd»xBrandNme»xModelNme»xColorNme»xMeasurNm»xVrntName»xModelCde";

        if (!psCustomHeader.isEmpty() && !psCustomName.isEmpty() && !psCustomCriteria.isEmpty()) {
            lscolHeader = psCustomHeader;
            lscolName = psCustomName;
            lscolCriteria = psCustomCriteria;
        }

        this.poJSON = ShowDialogFX.Search(
                poGRider,
                lsSQL,
                value,
                lscolHeader,
                lscolName,
                lscolCriteria,
                byCode ? 0 : 1
        );
        if (this.poJSON != null) {
            JSONObject result = new JSONObject();

            result = this.poInvMaster.openRecord((String) this.poJSON.get("sStockIDx"), (String) this.poJSON.get("sBranchCd"));
            if ("error".equals((String) result.get("result"))) {
                return poJSON;
            }

            result = this.poInventorySerial.openRecord((String) this.poJSON.get("sSerialID"));
            if ("error".equals((String) result.get("result"))) {
                return poJSON;
            }
            result = this.poInventory.openRecord((String) this.poJSON.get("sStockIDx"));
            if ("error".equals((String) result.get("result"))) {
                return poJSON;
            }

            this.poJSON.put("result", "success");
            return poJSON;

        }
        this.poJSON = new JSONObject();
        this.poJSON.put("result", "error");
        this.poJSON.put("message", "No record loaded.");
        return this.poJSON;
    }

    public JSONObject SearchInventorySupplier(String value, boolean byCode) throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();

        Client loClientSupplier = new ClientControllers(poGRider, poLogWrapper).Client();
        loClientSupplier.Master().setRecordStatus(RecordStatus.ACTIVE);
        loClientSupplier.Master().setClientType("1");
        poJSON = loClientSupplier.Master().searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            poSupplierCompany = loClientSupplier;
        }

        return poJSON;
    }

    public String generateConditionInventory(boolean isWithBranch) {
        String[][] filters = {
            {"bb.sBranchCd", this.getModelBranch() != null ? this.getModelBranch().getBranchCode() : null},
            //supplier
            {"ba.sSupplier", this.getModelSupplierMaster() != null ? this.getModelSupplierMaster().getClientId() : null},
            // inventory
            {"a.IndustryId", this.getModelIndustry() != null ? this.getModelIndustry().getIndustryId() : null},
            {"a.IndustryId", this.getModelIndustry() != null ? this.getModelIndustry().getIndustryId() : null},
            {"a.sCategCd1", this.getModelCategory() != null ? this.getModelCategory().getCategoryId() : null},
            {"a.sCategCd2", this.getModelCategory2() != null ? this.getModelCategory2().getCategoryId() : null},
            {"a.sCategCd3", this.getModelCategory3() != null ? this.getModelCategory3().getCategoryId() : null},
            {"a.sCategCd4", this.getModelCategory4() != null ? this.getModelCategory4().getCategoryId() : null},
            {"a.sCategLvl2", this.getModelCategoryLevel2() != null ? this.getModelCategoryLevel2().getCategoryId() : null},
            {"a.sCategLvl3", this.getModelCategoryLevel3() != null ? this.getModelCategoryLevel3().getCategoryId() : null},
            {"a.sCategLvl4", this.getModelCategoryLevel4() != null ? this.getModelCategoryLevel4().getCategoryId() : null},
            {"a.sBrandId", this.getModelBrand() != null ? this.getModelBrand().getBrandId() : null},
            {"a.sModelId", this.getModelModel() != null ? this.getModelModel().getModelId() : null},
            {"a.sColorId", this.getModelColor() != null ? this.getModelColor().getColorId() : null},
            {"a.sMeasureId", this.getModelMeasure() != null ? this.getModelMeasure().getMeasureId() : null},
            {"a.sInvTypeId", this.getModelInvType() != null ? this.getModelInvType().getInventoryTypeId() : null},
            {"a.sVariantId", this.getModelModel_Variant() != null ? this.getModelModel_Variant().getModelId() : null}
        };

        StringBuilder lsCondition = new StringBuilder();

        for (int lnFilter = 0; lnFilter < filters.length; lnFilter++) {
            if (!isWithBranch && lnFilter == 0) {
                continue;
            }

            if (filters[lnFilter][1] != null) {
                if (lsCondition.length() > 0) {
                    lsCondition.append(" AND ");
                }
                lsCondition.append(filters[lnFilter][0]).append(" = ").append(SQLUtil.toSQL(filters[lnFilter][1]));
            }
        }

        return lsCondition.toString();
    }

    public String generateConditionSerial() {
        String[][] filters = {
            {"a.sBranchCd", this.getModelBranch() != null ? this.getModelBranch().getBranchCode() : null},
            //supplier
            {"ba.sSupplier", this.getModelSupplierMaster() != null ? this.getModelSupplierMaster().getClientId() : null},
            // inventory
            {"b.IndustryId", this.getModelIndustry() != null ? this.getModelIndustry().getIndustryId() : null},
            {"b.IndustryId", this.getModelIndustry() != null ? this.getModelIndustry().getIndustryId() : null},
            {"b.sCategCd1", this.getModelCategory() != null ? this.getModelCategory().getCategoryId() : null},
            {"b.sCategCd2", this.getModelCategory2() != null ? this.getModelCategory2().getCategoryId() : null},
            {"b.sCategCd3", this.getModelCategory3() != null ? this.getModelCategory3().getCategoryId() : null},
            {"b.sCategCd4", this.getModelCategory4() != null ? this.getModelCategory4().getCategoryId() : null},
            {"b.sCategLvl2", this.getModelCategoryLevel2() != null ? this.getModelCategoryLevel2().getCategoryId() : null},
            {"b.sCategLvl3", this.getModelCategoryLevel3() != null ? this.getModelCategoryLevel3().getCategoryId() : null},
            {"b.sCategLvl4", this.getModelCategoryLevel4() != null ? this.getModelCategoryLevel4().getCategoryId() : null},
            {"b.sBrandId", this.getModelBrand() != null ? this.getModelBrand().getBrandId() : null},
            {"b.sModelId", this.getModelModel() != null ? this.getModelModel().getModelId() : null},
            {"b.sColorId", this.getModelColor() != null ? this.getModelColor().getColorId() : null},
            {"b.sMeasureId", this.getModelMeasure() != null ? this.getModelMeasure().getMeasureId() : null},
            {"b.sInvTypeId", this.getModelInvType() != null ? this.getModelInvType().getInventoryTypeId() : null},
            {"b.sVariantId", this.getModelModel_Variant() != null ? this.getModelModel_Variant().getModelId() : null}
        };

        StringBuilder lsCondition = new StringBuilder();

        for (int lnFilter = 0; lnFilter < filters.length; lnFilter++) {

            if (filters[lnFilter][1] != null) {
                if (lsCondition.length() > 0) {
                    lsCondition.append(" AND ");
                }
                lsCondition.append(filters[lnFilter][0]).append(" = ").append(SQLUtil.toSQL(filters[lnFilter][1]));
            }
        }

        return lsCondition.toString();
    }

    public String getSQ_BrowseInventory() {

        String lsSQL = "SELECT "
                + " a.sStockIDx"
                + ", a.sBarCodex"
                + ", a.sDescript"
                + ", a.sBriefDsc"
                + ", a.sAltBarCd"
                + ", a.sCategCd1"
                + ", a.sCategCd2"
                + ", a.sCategCd3"
                + ", a.sCategCd4"
                + ", a.sBrandIDx"
                + ", a.sModelIDx"
                + ", a.sColorIDx"
                + ", a.sVrntIDxx"
                + ", a.sMeasurID"
                + ", a.sInvTypCd"
                + ", a.sIndstCdx"
                + ", a.nUnitPrce"
                + ", a.nSelPrice"
                + ", a.nDiscLev1"
                + ", a.nDiscLev2"
                + ", a.nDiscLev3"
                + ", a.nDealrDsc"
                + ", a.nMinLevel"
                + ", a.nMaxLevel"
                + ", a.cComboInv"
                + ", a.cWthPromo"
                + ", a.cSerialze"
                + ", a.cUnitType"
                + ", a.cInvStatx"
                + ", a.nShlfLife"
                + ", a.sSupersed"
                + ", a.cRecdStat"
                + ", a.sModified"
                + ", a.dModified"
                + ", IFNULL (b.sDescript, '') xBrandNme"
                + ", IFNULL (c.sDescript, '') xModelNme"
                + ", IFNULL (d.sDescript, '') xColorNme"
                + ", IFNULL (e.sDescript, '') xMeasurNm"
                + ", TRIM(CONCAT(IFNULL (f.sDescript, ''),' ',IFNULL (f.nYearMdlx, ''))) xVrntName"
                + ", IFNULL (c.sModelCde, '') xModelCde"
                + " FROM Inventory a"
                + "  LEFT JOIN Brand b ON a.sBrandIDx = b.sBrandIDx"
                + "  LEFT JOIN Model c ON a.sModelIDx = c.sModelIDx"
                + "  LEFT JOIN Color d ON a.sColorIDx = d.sColorIDx"
                + "  LEFT JOIN Measure e ON a.sMeasurID = e.sMeasurID"
                + "  LEFT JOIN Model_Variant f ON a.sVrntIDxx = f.sVrntIDxx"
                + "  LEFT JOIN Inv_Supplier ba ON a.sStockIDx = ba.sStockIDx ";
        return lsSQL;
    }

    public String getSQ_BrowseInventorySerial() {
        String lsSQL = "SELECT "
                + "  a.sSerialID"
                + ", a.sBranchCd"
                + ", a.sClientID"
                + ", a.sSerial01"
                + ", a.sSerial02"
                + ", a.nUnitPrce"
                + ", a.sStockIDx"
                + ", a.cLocation"
                + ", a.cSoldStat"
                + ", a.cUnitType"
                + ", a.sCompnyID"
                + ", a.sWarranty"
                + ", a.dModified"
                + ", b.sStockIDx"
                + ", b.sBarCodex"
                + ", b.sDescript"
                + ", b.sBriefDsc"
                + ", b.sAltBarCd"
                + ", b.sCategCd1"
                + ", b.sCategCd2"
                + ", b.sCategCd3"
                + ", b.sCategCd4"
                + ", b.sBrandIDx"
                + ", b.sModelIDx"
                + ", b.sColorIDx"
                + ", b.sVrntIDxx"
                + ", b.sMeasurID"
                + ", b.sInvTypCd"
                + ", b.sIndstCdx"
                + ", b.nUnitPrce"
                + ", b.nSelPrice"
                + ", b.nDiscLev1"
                + ", b.nDiscLev2"
                + ", b.nDiscLev3"
                + ", b.nDealrDsc"
                + ", b.nMinLevel"
                + ", b.nMaxLevel"
                + ", b.cComboInv"
                + ", b.cWthPromo"
                + ", b.cSerialze"
                + ", b.cUnitType"
                + ", b.cInvStatx"
                + ", b.nShlfLife"
                + ", b.sSupersed"
                + ", b.cRecdStat"
                + ", IFNULL (c.sDescript, '') xBrandNme"
                + ", IFNULL (d.sDescript, '') xModelNme"
                + ", IFNULL (e.sDescript, '') xColorNme"
                + ", IFNULL (f.sDescript, '') xMeasurNm"
                + ", TRIM(CONCAT(IFNULL (g.sDescript, ''),' ',IFNULL (g.nYearMdlx, ''))) xVrntName"
                + ", IFNULL (d.sModelCde, '') xModelCde"
                + " FROM Inv_Serial a"
                + "  LEFT JOIN Inventory b ON a.sStockIDx = b.sStockIDx"
                + "  LEFT JOIN Brand c ON b.sBrandIDx = c.sBrandIDx"
                + "  LEFT JOIN Model d ON b.sModelIDx = d.sModelIDx"
                + "  LEFT JOIN Color e ON b.sColorIDx = e.sColorIDx"
                + "  LEFT JOIN Measure f ON b.sMeasurID = f.sMeasurID"
                + "  LEFT JOIN Model_Variant g ON b.sVrntIDxx = g.sVrntIDxx"
                + "  LEFT JOIN Inv_Supplier ba ON b.sStockIDx = ba.sStockIDx ";
        return lsSQL;
    }

    public String getSQ_BrowseInventorywithStock() {
        //include inv_master
        String lsSQL = " SELECT "
                + " a.sStockIDx"
                + ", a.sBarCodex"
                + ", a.sDescript"
                + ", a.sBriefDsc"
                + ", a.sAltBarCd"
                + ", a.sCategCd1"
                + ", a.sCategCd2"
                + ", a.sCategCd3"
                + ", a.sCategCd4"
                + ", a.sBrandIDx"
                + ", a.sModelIDx"
                + ", a.sColorIDx"
                + ", a.sVrntIDxx"
                + ", a.sMeasurID"
                + ", a.sInvTypCd"
                + ", a.sIndstCdx"
                + ", a.nUnitPrce"
                + ", a.nSelPrice"
                + ", a.nDiscLev1"
                + ", a.nDiscLev2"
                + ", a.nDiscLev3"
                + ", a.nDealrDsc"
                + ", a.nMinLevel"
                + ", a.nMaxLevel"
                + ", a.cComboInv"
                + ", a.cWthPromo"
                + ", a.cSerialze"
                + ", a.cUnitType"
                + ", a.cInvStatx"
                + ", a.nShlfLife"
                + ", a.sSupersed"
                + ", a.cRecdStat"
                + ", a.sModified"
                + ", a.dModified"
                + ", IFNULL (b.sDescript, '') xBrandNme"
                + ", IFNULL (c.sDescript, '') xModelNme"
                + ", IFNULL (d.sDescript, '') xColorNme"
                + ", IFNULL (e.sDescript, '') xMeasurNm"
                + ", TRIM(CONCAT(IFNULL (f.sDescript, ''),' ',IFNULL (f.nYearMdlx, ''))) xVrntName"
                + ", IFNULL (c.sModelCde, '') xModelCde"
                + ", IFNULL (bc.sBranchNm, '') xBranchNm"
                + ", IFNULL (bc.sBranchCd, '') sBranchCd"
                + " FROM Inventory a"
                + "  LEFT JOIN Brand b ON a.sBrandIDx = b.sBrandIDx"
                + "  LEFT JOIN Model c ON a.sModelIDx = c.sModelIDx"
                + "  LEFT JOIN Color d ON a.sColorIDx = d.sColorIDx"
                + "  LEFT JOIN Measure e ON a.sMeasurID = e.sMeasurID"
                + "  LEFT JOIN Model_Variant f ON a.sVrntIDxx = f.sVrntIDxx"
                + "  LEFT JOIN Inv_Supplier ba ON a.sStockIDx = ba.sStockIDx "
                + "  LEFT JOIN Inv_Master bb ON a.sStockIDx = bb.sStockIDx "
                + "  LEFT JOIN Branch bc ON bb.sBranchCd = bc.sBranchCd";
        return lsSQL;
    }

    public String getSQ_BrowseInventorySerialwithStock() {
        String lsSQL = "SELECT "
                + "  a.sSerialID"
                + ", a.sBranchCd"
                + ", a.sClientID"
                + ", a.sSerial01"
                + ", a.sSerial02"
                + ", a.nUnitPrce"
                + ", a.sStockIDx"
                + ", a.cLocation"
                + ", a.cSoldStat"
                + ", a.cUnitType"
                + ", a.sCompnyID"
                + ", a.sWarranty"
                + ", a.dModified"
                + ", b.sStockIDx"
                + ", b.sBarCodex"
                + ", b.sDescript"
                + ", b.sBriefDsc"
                + ", b.sAltBarCd"
                + ", b.sCategCd1"
                + ", b.sCategCd2"
                + ", b.sCategCd3"
                + ", b.sCategCd4"
                + ", b.sBrandIDx"
                + ", b.sModelIDx"
                + ", b.sColorIDx"
                + ", b.sVrntIDxx"
                + ", b.sMeasurID"
                + ", b.sInvTypCd"
                + ", b.sIndstCdx"
                + ", b.nUnitPrce"
                + ", b.nSelPrice"
                + ", b.nDiscLev1"
                + ", b.nDiscLev2"
                + ", b.nDiscLev3"
                + ", b.nDealrDsc"
                + ", b.nMinLevel"
                + ", b.nMaxLevel"
                + ", b.cComboInv"
                + ", b.cWthPromo"
                + ", b.cSerialze"
                + ", b.cUnitType"
                + ", b.cInvStatx"
                + ", b.nShlfLife"
                + ", b.sSupersed"
                + ", b.cRecdStat"
                + ", IFNULL (c.sDescript, '') xBrandNme"
                + ", IFNULL (d.sDescript, '') xModelNme"
                + ", IFNULL (e.sDescript, '') xColorNme"
                + ", IFNULL (f.sDescript, '') xMeasurNm"
                + ", TRIM(CONCAT(IFNULL (g.sDescript, ''),' ',IFNULL (g.nYearMdlx, ''))) xVrntName"
                + ", IFNULL (d.sModelCde, '') xModelCde"
                + ", IFNULL (bc.sBranchNm, '') xBranchNm"
                + ", IFNULL (bc.sBranchCd, '') sBranchCd"
                + " FROM Inv_Serial a"
                + "  LEFT JOIN Inventory b ON a.sStockIDx = b.sStockIDx"
                + "  LEFT JOIN Brand c ON b.sBrandIDx = c.sBrandIDx"
                + "  LEFT JOIN Model d ON b.sModelIDx = d.sModelIDx"
                + "  LEFT JOIN Color e ON b.sColorIDx = e.sColorIDx"
                + "  LEFT JOIN Measure f ON b.sMeasurID = f.sMeasurID"
                + "  LEFT JOIN Model_Variant g ON b.sVrntIDxx = g.sVrntIDxx"
                + "  LEFT JOIN Inv_Supplier ba ON b.sStockIDx = ba.sStockIDx "
                + "  LEFT JOIN Inv_Master bb ON b.sStockIDx = bb.sStockIDx AND a.sBranchCd = bb.sBranchCd "
                + "  LEFT JOIN Branch bc ON bb.sBranchCd = bc.sBranchCd";
        return lsSQL;
    }

    public String getSQ_BrowseInventoryIssuance() {
        String lsSQL = " SELECT "
                + "  TRIM(CONCAT(IFNULL (b.sSerial01, ''),IF(b.sSerial01 IS NOT NULL AND b.sSerial02 IS NOT NULL,'/ ',''),IFNULL (b.sSerial02, ''))) AS xSerialNme,"
                + "  IFNULL(a.sBarCodex, '') sBarcodex,"
                + "  IFNULL(a.sDescript, '') xDescript,"
                + "  IFNULL(bb.nQtyOnHnd, 0.00) nQtyOnHnd,"
                + "  IFNULL(c.sDescript, '') xBrandNme,"
                + "  IFNULL(d.sDescript, '') xModelNme,"
                + "  IFNULL(e.sDescript, '') xColorNme,"
                + "  IFNULL(f.sDescript, '') xMeasurNm,"
                + "  TRIM(CONCAT(IFNULL (g.sDescript, ''),' ',IFNULL (g.nYearMdlx, ''))) xVrntName,"
                + "  IFNULL(d.sModelCde, '') xModelCde,"
                + "  IFNULL(bc.sBranchNm, '') xBranchNm,"
                + "  IFNULL(bc.sBranchCd, '') sBranchCd,"
                + "  IFNULL(b.sSerialID, '') sSerialID"
                + " FROM Inventory a"
                + "  LEFT JOIN Inv_Serial bON a.sStockIDx = b.sStockIDx"
                + "  LEFT JOIN Brand c ON a.sBrandIDx = c.sBrandIDx"
                + "  LEFT JOIN Model d ON a.sModelIDx = d.sModelIDx"
                + "  LEFT JOIN Color e ON a.sColorIDx = e.sColorIDx"
                + "  LEFT JOIN Measure f ON a.sMeasurID = f.sMeasurID"
                + "  LEFT JOIN Model_Variant g ON a.sVrntIDxx = g.sVrntIDxx"
                + "  LEFT JOIN Inv_Supplier ba ON a.sStockIDx = ba.sStockIDx"
                + "  LEFT JOIN Inv_Master bb ON a.sStockIDx = bb.sStockIDx AND bb.sBranchCd = b.sBranchCd"
                + "  LEFT JOIN Branch bc ON bb.sBranchCd = bc.sBranchCd"
                + "         ORDER BY xSerialNme DESC, sBarcodex ASC, nQtyOnHnd DESC";
        return lsSQL;
    }
}
