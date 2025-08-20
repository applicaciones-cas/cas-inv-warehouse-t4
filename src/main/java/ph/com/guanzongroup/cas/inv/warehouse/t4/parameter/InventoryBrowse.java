package ph.com.guanzongroup.cas.inv.warehouse.t4.parameter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
import org.guanzon.cas.client.services.ClientModels;
import org.guanzon.cas.inv.InvMaster;
import org.guanzon.cas.inv.InvSerial;
import org.guanzon.cas.inv.Inventory;
import org.guanzon.cas.inv.model.Model_Inv_Master;
import org.guanzon.cas.inv.model.Model_Inv_Serial;
import org.guanzon.cas.inv.model.Model_Inventory;
import org.guanzon.cas.inv.services.InvControllers;
import org.guanzon.cas.inv.services.InvModels;
import org.guanzon.cas.parameter.Branch;
import org.guanzon.cas.parameter.Brand;
import org.guanzon.cas.parameter.Category;
import org.guanzon.cas.parameter.CategoryLevel2;
import org.guanzon.cas.parameter.CategoryLevel3;
import org.guanzon.cas.parameter.CategoryLevel4;
import org.guanzon.cas.parameter.Color;
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
import org.guanzon.cas.parameter.services.ParamModels;
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
    private Model_Inventory poInventory;
    private Model_Inv_Master poInvMaster;
    private Model_Inv_Serial poInventorySerial;

    private Model_Branch poBranch;
    private Model_Industry poIndustry;
//    private Model_Inv_Supplier poInvSupplier;
    private Model_Client_Master poSupplierCompany;
    private Model_Category poCategory1;
    private Model_Category poCategory2;
    private Model_Category poCategory3;
    private Model_Category poCategory4;
    private Model_Category_Level2 poCategoryLevel2;
    private Model_Category_Level3 poCategoryLevel3;
    private Model_Category_Level4 poCategoryLevel4;
    private Model_Brand poBrand;
    private Model_Model poModel;
    private Model_Color poColor;
    private Model_Measure poMeasure;
    private Model_Inv_Type poInvType;
    private Model_Model_Variant poModelVariant;
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
        InvModels inventoryModel = new InvModels(poGRider);
        poSupplierCompany = new ClientModels(poGRider).ClientMaster();

        // Inventory-related
        this.poInventory = inventoryModel.Inventory();
        this.poInvMaster = inventoryModel.InventoryMaster();
        this.poInventorySerial = inventoryModel.InventorySerial();

        this.poInvMaster.setRecordStatus(psRecdStat);
        this.poInventory.setRecordStatus(psRecdStat);
//        this.poInventorySerial.setLocation(psRecdStat);

        // Parameter-related
        this.poBranch = new ParamModels(poGRider).Branch();
        this.poIndustry = new ParamModels(poGRider).Industry();
        this.poCategory1 = new ParamModels(poGRider).Category();
        this.poCategory2 = new ParamModels(poGRider).Category();
        this.poCategory3 = new ParamModels(poGRider).Category();
        this.poCategory4 = new ParamModels(poGRider).Category();
        this.poCategoryLevel2 = new ParamModels(poGRider).Category2();
        this.poCategoryLevel3 = new ParamModels(poGRider).Category3();
        this.poCategoryLevel4 = new ParamModels(poGRider).Category4();
        this.poBrand = new ParamModels(poGRider).Brand();
        this.poModel = new ParamModels(poGRider).Model();
        this.poColor = new ParamModels(poGRider).Color();
        this.poMeasure = new ParamModels(poGRider).Measurement();
        this.poInvType = new ParamModels(poGRider).InventoryType();
        this.poModelVariant = new ParamModels(poGRider).ModelVariant();

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        return poJSON;
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
        return poSupplierCompany.openRecord(supplierFilter);
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
        ParamControllers parameterModel = new ParamControllers(poGRider, poLogWrapper);
        Industry loParameter = parameterModel.Industry();
        loParameter.setRecordStatus(psRecdStat);

        poJSON = loParameter.searchRecord(searchIndustryFilter, false);
        if ("success".equals((String) poJSON.get("result"))) {
            poIndustry = loParameter.getModel();
        }
        return poJSON;
    }

    public JSONObject searchCategory1(String searchCategory1Filter) throws SQLException, GuanzonException {
        this.poJSON = new JSONObject();
        ParamControllers parameterModel = new ParamControllers(poGRider, poLogWrapper);
        Category loParameter = parameterModel.Category();
        loParameter.setRecordStatus(psRecdStat);

        poJSON = loParameter.searchRecord(searchCategory1Filter, false);
        if ("success".equals((String) poJSON.get("result"))) {
            poCategory1 = loParameter.getModel();
        }
        return poJSON;
    }

    public JSONObject searchCategory2(String searchCategory2Filter) throws SQLException, GuanzonException {
        this.poJSON = new JSONObject();
        ParamControllers parameterModel = new ParamControllers(poGRider, poLogWrapper);
        Category loParameter = parameterModel.Category();
        loParameter.setRecordStatus(psRecdStat);

        poJSON = loParameter.searchRecord(searchCategory2Filter, false);
        if ("success".equals((String) poJSON.get("result"))) {
            poCategory2 = loParameter.getModel();
        }
        return poJSON;
    }

    public JSONObject searchCategory3(String searchCategory3Filter) throws SQLException, GuanzonException {
        this.poJSON = new JSONObject();
        ParamControllers parameterModel = new ParamControllers(poGRider, poLogWrapper);
        Category loParameter = parameterModel.Category();
        loParameter.setRecordStatus(psRecdStat);

        poJSON = loParameter.searchRecord(searchCategory3Filter, false);
        if ("success".equals((String) poJSON.get("result"))) {
            poCategory3 = loParameter.getModel();
        }
        return poJSON;
    }

    public JSONObject searchCategory4(String searchCategory4Filter) throws SQLException, GuanzonException {
        this.poJSON = new JSONObject();
        ParamControllers parameterModel = new ParamControllers(poGRider, poLogWrapper);
        Category loParameter = parameterModel.Category();
        loParameter.setRecordStatus(psRecdStat);

        poJSON = loParameter.searchRecord(searchCategory4Filter, false);
        if ("success".equals((String) poJSON.get("result"))) {
            poCategory4 = loParameter.getModel();
        }
        return poJSON;
    }

    public JSONObject searchCategoryLevel2(String searchCategoryLevel2Filter) throws SQLException, GuanzonException {
        this.poJSON = new JSONObject();
        ParamControllers parameterModel = new ParamControllers(poGRider, poLogWrapper);
        CategoryLevel2 loParameter = parameterModel.CategoryLevel2();
        loParameter.setRecordStatus(psRecdStat);

        poJSON = loParameter.searchRecord(searchCategoryLevel2Filter, false);
        if ("success".equals((String) poJSON.get("result"))) {
            poCategoryLevel2 = loParameter.getModel();
        }
        return poJSON;
    }

    public JSONObject searchCategoryLevel3(String searchCategoryLevel3Filter) throws SQLException, GuanzonException {
        this.poJSON = new JSONObject();
        ParamControllers parameterModel = new ParamControllers(poGRider, poLogWrapper);
        CategoryLevel3 loParameter = parameterModel.CategoryLevel3();
        loParameter.setRecordStatus(psRecdStat);

        poJSON = loParameter.searchRecord(searchCategoryLevel3Filter, false);
        if ("success".equals((String) poJSON.get("result"))) {
            poCategoryLevel3 = loParameter.getModel();
        }
        return poJSON;
    }

    public JSONObject searchCategoryLevel4(String searchCategoryLevel4Filter) throws SQLException, GuanzonException {
        this.poJSON = new JSONObject();
        ParamControllers parameterModel = new ParamControllers(poGRider, poLogWrapper);
        CategoryLevel4 loParameter = parameterModel.CategoryLevel4();
        loParameter.setRecordStatus(psRecdStat);

        poJSON = loParameter.searchRecord(searchCategoryLevel4Filter, false);
        if ("success".equals((String) poJSON.get("result"))) {
            poCategoryLevel4 = loParameter.getModel();
        }
        return poJSON;
    }

    public JSONObject searchBrand(String searchBrandFilter) throws SQLException, GuanzonException {
        this.poJSON = new JSONObject();
        ParamControllers parameterModel = new ParamControllers(poGRider, poLogWrapper);
        Brand loParameter = parameterModel.Brand();
        loParameter.setRecordStatus(psRecdStat);

        poJSON = loParameter.searchRecord(searchBrandFilter, false);
        if ("success".equals((String) poJSON.get("result"))) {
            poBrand = loParameter.getModel();
        }
        return poJSON;
    }

    public JSONObject searchModel(String searchModelFilter) throws SQLException, GuanzonException {
        this.poJSON = new JSONObject();
        ParamControllers parameterModel = new ParamControllers(poGRider, poLogWrapper);
        Model loParameter = parameterModel.Model();
        loParameter.setRecordStatus(psRecdStat);

        poJSON = loParameter.searchRecord(searchModelFilter, false);
        if ("success".equals((String) poJSON.get("result"))) {
            poModel = loParameter.getModel();
        }
        return poJSON;
    }

    public JSONObject searchColor(String searchColorFilter) throws SQLException, GuanzonException {
        this.poJSON = new JSONObject();
        ParamControllers parameterModel = new ParamControllers(poGRider, poLogWrapper);
        Color loParameter = parameterModel.Color();
        loParameter.setRecordStatus(psRecdStat);

        poJSON = loParameter.searchRecord(searchColorFilter, false);
        if ("success".equals((String) poJSON.get("result"))) {
            poColor = loParameter.getModel();
        }
        return poJSON;
    }

    public JSONObject searchMeasure(String searchMeasureFilter) throws SQLException, GuanzonException {
        this.poJSON = new JSONObject();
        ParamControllers parameterModel = new ParamControllers(poGRider, poLogWrapper);
        Measure loParameter = parameterModel.Measurement();
        loParameter.setRecordStatus(psRecdStat);

        poJSON = loParameter.searchRecord(searchMeasureFilter, false);
        if ("success".equals((String) poJSON.get("result"))) {
            poMeasure = loParameter.getModel();
        }
        return poJSON;
    }

    public JSONObject searchInvType(String searchInvTypeFilter) throws SQLException, GuanzonException {
        this.poJSON = new JSONObject();
        ParamControllers parameterModel = new ParamControllers(poGRider, poLogWrapper);
        InvType loParameter = parameterModel.InventoryType();
        loParameter.setRecordStatus(psRecdStat);

        poJSON = loParameter.searchRecord(searchInvTypeFilter, false);
        if ("success".equals((String) poJSON.get("result"))) {
            poInvType = loParameter.getModel();
        }
        return poJSON;
    }

    public JSONObject searchModelVariant(String searchModelVariantFilter) throws SQLException, GuanzonException {
        this.poJSON = new JSONObject();
        ParamControllers parameterModel = new ParamControllers(poGRider, poLogWrapper);
        ModelVariant loParameter = parameterModel.ModelVariant();
        loParameter.setRecordStatus(psRecdStat);

        poJSON = loParameter.searchRecord(searchModelVariantFilter, false);
        if ("success".equals((String) poJSON.get("result"))) {
            poModelVariant = loParameter.getModel();
        }
        return poJSON;
    }

    // FOR UI PURPOSE'S
    public Model_Client_Master getModelSupplierMaster() {
        return poSupplierCompany;
    }
//
//    public Client getSupplier() {
//        return poSupplierCompany;
//    }

    public Model_Inventory getModelInventory() {
        return poInventory;
    }

    public Model_Inv_Serial getModelInventorySerial() {
        return poInventorySerial;
    }

    public Model_Inv_Master getModelInventoryMaster() {
        return poInvMaster;
    }

    public Model_Branch getModelBranch() {
        return poBranch;
    }

    public Model_Industry getModelIndustry() {
        return poIndustry;
    }

    public Model_Category getModelCategory() {
        return poCategory1;
    }

    public Model_Category getModelCategory2() {
        return poCategory2;
    }

    public Model_Category getModelCategory3() {
        return poCategory3;
    }

    public Model_Category getModelCategory4() {
        return poCategory4;
    }

    public Model_Category_Level2 getModelCategoryLevel2() {
        return poCategoryLevel2;
    }

    public Model_Category_Level3 getModelCategoryLevel3() {
        return poCategoryLevel3;
    }

    public Model_Category_Level4 getModelCategoryLevel4() {
        return poCategoryLevel4;
    }

    public Model_Brand getModelBrand() {
        return poBrand;
    }

    public Model_Model getModelModel() {
        return poModel;
    }

    public Model_Color getModelColor() {
        return poColor;
    }

    public Model_Measure getModelMeasure() {
        return poMeasure;
    }

    public Model_Inv_Type getModelInvType() {
        return poInvType;
    }

    public Model_Model_Variant getModelModel_Variant() {
        return poModelVariant;
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

        System.out.println("Search Dialog Query : " + lsSQL);
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

    public JSONObject searchInventorySerialWithStock(String value, boolean byCode, boolean byExact) throws SQLException, GuanzonException {
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

        System.out.println("Search Dialog Query : " + lsSQL);
        this.poJSON = ShowDialogFX.Search(
                poGRider,
                lsSQL,
                value,
                lscolHeader,
                lscolName,
                lscolCriteria,
                byExact ? 0 : byCode ? 2 : 1
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
            lsSQL = MiscUtil.addCondition(lsSQL, "b.cSoldStat = '0' ");
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

    public JSONObject searchInventoryIssaunce(String value, boolean byCode, boolean byExact) throws SQLException, GuanzonException {
        String lsSQL = getSQ_BrowseInventoryIssuance();

        String lsCondition = generateConditionInventory(true);
        if (!lsCondition.isEmpty()) {
            lsSQL = MiscUtil.addCondition(lsSQL, lsCondition);
        }

        if (pbisWithQty) {
            lsSQL = MiscUtil.addCondition(lsSQL, "bb.nQtyOnHnd > 0");
            lsSQL = MiscUtil.addCondition(lsSQL, "b.sSerial01 IS NULL OR b.cSoldStat = '0' ");
        }

        //default
        String lscolHeader = "Serial»Barcode»Description»Qty-On-Hand»Brand Name»Model Name»Color Name»UOM»Variant Name»Model Code";
        String lscolName = "xSerialNme»sBarcodex»xDescript»nQtyOnHnd»xBrandNme»xModelNme»xColorNme»xMeasurNm»xVrntName»xModelCde";
        String lscolCriteria = "TRIM(CONCAT (IFNULL (b.sSerial01, ''),IF (b.sSerial01 IS NOT NULL AND b.sSerial02 IS NOT NULL,'/ ',''),IFNULL (b.sSerial02, '')) )»"
                + "sBarcodex»a.sDescript»nQtyOnHnd»c.sDescript»d.sDescript»e.sDescript»f.sDescript»g.sDescript»d.sModelCde";

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
                byExact ? 0 : byCode ? 1 : 2
        );
        if (this.poJSON != null) {
            JSONObject result = new JSONObject();

            result = this.poInvMaster.openRecord((String) this.poJSON.get("sStockIDx"), (String) this.poJSON.get("sBranchCd"));
            if ("error".equals((String) result.get("result"))) {
                return result;
            }
            result = this.poInventory.openRecord((String) this.poJSON.get("sStockIDx"));
            if ("error".equals((String) result.get("result"))) {
                return result;
            }
            if ((String) this.poJSON.get("sSerialID") != null && !this.poJSON.get("sSerialID").toString().isEmpty()) {
                result = this.poInventorySerial.openRecord((String) this.poJSON.get("sSerialID"));
                if ("error".equals((String) result.get("result"))) {
                    return result;
                }
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
            poSupplierCompany = loClientSupplier.Master().getModel();
        }

        return poJSON;
    }

    public String generateConditionInventory(boolean isWithBranch) {
        String[][] filters = {
            {"bb.sBranchCd", this.getModelBranch() != null ? this.getModelBranch().getBranchCode() : null},
            //supplier
            {"ba.sSupplier", this.getModelSupplierMaster() != null ? this.getModelSupplierMaster().getClientId() : null},
            // inventory
            {"a.sIndstCdx", this.getModelIndustry() != null ? this.getModelIndustry().getIndustryId() : null},
            {"a.sCategCd1", this.getModelCategory() != null ? this.getModelCategory().getCategoryId() : null},
            {"a.sCategCd1", this.getModelCategory2() != null ? this.getModelCategory2().getCategoryId() : null},
            {"a.sCategCd1", this.getModelCategory3() != null ? this.getModelCategory3().getCategoryId() : null},
            {"a.sCategCd1", this.getModelCategory4() != null ? this.getModelCategory4().getCategoryId() : null},
            {"a.sCategCd2", this.getModelCategoryLevel2() != null ? this.getModelCategoryLevel2().getCategoryId() : null},
            {"a.sCategCd3", this.getModelCategoryLevel3() != null ? this.getModelCategoryLevel3().getCategoryId() : null},
            {"a.sCategCd4", this.getModelCategoryLevel4() != null ? this.getModelCategoryLevel4().getCategoryId() : null},
            {"a.sBrandIDx", this.getModelBrand() != null ? this.getModelBrand().getBrandId() : null},
            {"a.sModelIDx", this.getModelModel() != null ? this.getModelModel().getModelId() : null},
            {"a.sColorIDx", this.getModelColor() != null ? this.getModelColor().getColorId() : null},
            {"a.sVrntIDxx", this.getModelMeasure() != null ? this.getModelMeasure().getMeasureId() : null},
            {"a.sMeasurID", this.getModelInvType() != null ? this.getModelInvType().getInventoryTypeId() : null},
            {"a.sInvTypCd", this.getModelModel_Variant() != null ? this.getModelModel_Variant().getModelId() : null}
        };

        StringBuilder lsCondition = new StringBuilder();

        // Collect all CategCd1 values for IN clause
        List<String> categCd1Values = new ArrayList<>();

        for (int lnFilter = 0; lnFilter < filters.length; lnFilter++) {
            if (!isWithBranch && lnFilter == 0) {
                continue;
            }

            String column = filters[lnFilter][0];
            String value = filters[lnFilter][1];

            if (value != null) {
                if ("a.sCategCd1".equals(column)) {
                    categCd1Values.add(SQLUtil.toSQL(value));
                } else {
                    if (lsCondition.length() > 0) {
                        lsCondition.append(" AND ");
                    }
                    lsCondition.append(column).append(" = ").append(SQLUtil.toSQL(value));
                }
            }
        }

        // Add IN clause for sCategCd1 if there are multiple values
        if (!categCd1Values.isEmpty()) {
            if (lsCondition.length() > 0) {
                lsCondition.append(" AND ");
            }
            lsCondition.append("a.sCategCd1 IN (")
                    .append(String.join(", ", categCd1Values))
                    .append(")");
        }

        return lsCondition.toString();
    }

    public String generateConditionSerial() {
        String[][] filters = {
            {"a.sBranchCd", this.getModelBranch() != null ? this.getModelBranch().getBranchCode() : null},
            //supplier
            {"ba.sSupplier", this.getModelSupplierMaster() != null ? this.getModelSupplierMaster().getClientId() : null},
            // inventory
            {"b.sIndstCdx", this.getModelIndustry() != null ? this.getModelIndustry().getIndustryId() : null},
            {"b.sCategCd1", this.getModelCategory() != null ? this.getModelCategory().getCategoryId() : null},
            {"b.sCategCd1", this.getModelCategory2() != null ? this.getModelCategory2().getCategoryId() : null},
            {"b.sCategCd1", this.getModelCategory3() != null ? this.getModelCategory3().getCategoryId() : null},
            {"b.sCategCd1", this.getModelCategory4() != null ? this.getModelCategory4().getCategoryId() : null},
            {"b.sCategCd2", this.getModelCategoryLevel2() != null ? this.getModelCategoryLevel2().getCategoryId() : null},
            {"b.sCategCd3", this.getModelCategoryLevel3() != null ? this.getModelCategoryLevel3().getCategoryId() : null},
            {"b.sCategCd4", this.getModelCategoryLevel4() != null ? this.getModelCategoryLevel4().getCategoryId() : null},
            {"b.sBrandIDx", this.getModelBrand() != null ? this.getModelBrand().getBrandId() : null},
            {"b.sModelIDx", this.getModelModel() != null ? this.getModelModel().getModelId() : null},
            {"b.sColorIDx", this.getModelColor() != null ? this.getModelColor().getColorId() : null},
            {"b.sVrntIDxx", this.getModelMeasure() != null ? this.getModelMeasure().getMeasureId() : null},
            {"b.sMeasurID", this.getModelInvType() != null ? this.getModelInvType().getInventoryTypeId() : null},
            {"b.sInvTypCd", this.getModelModel_Variant() != null ? this.getModelModel_Variant().getModelId() : null}
        };

        StringBuilder lsCondition = new StringBuilder();

        // Collect all CategCd1 values for IN clause
        List<String> categCd1Values = new ArrayList<>();

        for (int lnFilter = 0; lnFilter < filters.length; lnFilter++) {

            String column = filters[lnFilter][0];
            String value = filters[lnFilter][1];

            if (value != null) {
                if ("a.sCategCd1".equals(column)) {
                    categCd1Values.add(SQLUtil.toSQL(value));
                } else {
                    if (lsCondition.length() > 0) {
                        lsCondition.append(" AND ");
                    }
                    lsCondition.append(column).append(" = ").append(SQLUtil.toSQL(value));
                }
            }
        }

        // Add IN clause for sCategCd1 if there are multiple values
        if (!categCd1Values.isEmpty()) {
            if (lsCondition.length() > 0) {
                lsCondition.append(" AND ");
            }
            lsCondition.append("a.sCategCd1 IN (")
                    .append(String.join(", ", categCd1Values))
                    .append(")");
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
                + "  TRIM(CONCAT(IFNULL (b.sSerial01, ''),IF(b.sSerial01 IS NOT NULL AND b.sSerial02 IS NOT NULL,'/ ',''),IFNULL (b.sSerial02, ''))) AS xSerialNme"
                + ",  IFNULL(a.sBarCodex, '') sBarcodex"
                + ",  IFNULL(a.sDescript, '') xDescript"
                + ",  IFNULL(bb.nQtyOnHnd, 0.00) nQtyOnHnd"
                + ",  IFNULL(c.sDescript, '') xBrandNme"
                + ",  IFNULL(d.sDescript, '') xModelNme"
                + ",  IFNULL(e.sDescript, '') xColorNme"
                + ",  IFNULL(f.sDescript, '') xMeasurNm"
                + ",  TRIM(CONCAT(IFNULL (g.sDescript, ''),' ',IFNULL (g.nYearMdlx, ''))) xVrntName"
                + ",  IFNULL(d.sModelCde, '') xModelCde"
                + ",  IFNULL(bc.sBranchNm, '') xBranchNm"
                + ",  IFNULL(bc.sBranchCd, '') sBranchCd"
                + ",  IFNULL(b.sSerialID, '') sSerialID"
                + ",  a.sStockIDx"
                + " FROM Inventory a"
                + "  LEFT JOIN Inv_Serial b ON a.sStockIDx = b.sStockIDx"
                + "  LEFT JOIN Brand c ON a.sBrandIDx = c.sBrandIDx"
                + "  LEFT JOIN Model d ON a.sModelIDx = d.sModelIDx"
                + "  LEFT JOIN Color e ON a.sColorIDx = e.sColorIDx"
                + "  LEFT JOIN Measure f ON a.sMeasurID = f.sMeasurID"
                + "  LEFT JOIN Model_Variant g ON a.sVrntIDxx = g.sVrntIDxx"
                + "  LEFT JOIN Inv_Supplier ba ON a.sStockIDx = ba.sStockIDx"
                + "  LEFT JOIN Inv_Master bb ON a.sStockIDx = bb.sStockIDx"
                + "  LEFT JOIN Branch bc ON bb.sBranchCd = bc.sBranchCd";
//                + "         ORDER BY xSerialNme DESC, sBarcodex ASC, nQtyOnHnd DESC";
        return lsSQL;
    }
}
