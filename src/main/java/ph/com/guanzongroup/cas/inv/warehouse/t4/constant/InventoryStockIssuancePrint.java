/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ph.com.guanzongroup.cas.inv.warehouse.t4.constant;

import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;

/**
 *
 * @author Maynard
 */
public class InventoryStockIssuancePrint {

    public static final String MOBILE_PHONE_REPORT = "InventoryIssuanceMP";
    public static final String MOTORCYCLE_REPORT = "InventoryIssuanceMC";
    public static final String CAR_REPORT = "InventoryIssuanceCar";
    public static final String HOSPITALITY_REPORT = "InventoryIssuanceMonarch";
    public static final String LOS_PEDRITOS_REPORT = "InventoryIssuanceLP";
    public static final String GENERAL_REPORT = "InventoryIssuance";
    public static final String APPLIANCE_REPORT = "InventoryIssuanceAppliance";

    public static final String PrintRecordQuery() {
        String lsSQL = "SELECT "
                + "   InventoryTransferMaster.sTransNox sTransNox"
                + ",  IFNULL(InventoryTransferDetail.sStockIDx,'') sStockIDx"
                + ",  TRIM(CONCAT(IFNULL (InventorySerial.sSerial01, ''),IF(InventorySerial.sSerial01 IS NOT NULL AND InventorySerial.sSerial02 IS NOT NULL,'/ ',''),IFNULL (InventorySerial.sSerial02, '')))  xSerialNme"
                + ",  IFNULL(Inventory.sBarCodex,'') Barcode"
                + ",  IFNULL(Inventory.sDescript,'') InventoryName"
                + ",  IFNULL(Brand.sDescript,'') BrandName"
                + ",  IFNULL(Model.sDescript,'') ModelName"
                + ",  IFNULL(Color.sDescript,'') ColorName"
                + ",  IFNULL(Measure.sDescript,'') MeasureName"
                + ",  IFNULL(InventoryType.sDescript,'') InventoryTypeName"
                + ",  IFNULL(Variant.sDescript,'') VariantName"
                + ",  InventoryStockRequestDetail.nQtyOnHnd nQtyOnHnd"
                + ",  InventoryStockRequestDetail.nQuantity nQuantity"
                + ",  InventoryStockRequestDetail.nApproved nApproved"
                + ",  InventoryStockRequestDetail.nCancelld nCancelld"
                + "   FROM Inv_Transfer_Master InventoryTransferMaster"
                + "     LEFT JOIN Inv_Transfer_Detail InventoryTransferDetail"
                + "         ON InventoryTransferMaster.sTransNox = InventoryTransferDetail.sTransNox"
                + "     LEFT JOIN Inventory Inventory"
                + "         ON InventoryStockRequestDetail.sStockIDx = Inventory.sStockIDx"
                + "     LEFT JOIN Inv_Serial InventorySerial"
                + "         ON Inventory.sStockIDx = InventorySerial.sStockIDx "
                + "             AND InventoryStockRequestDetail.sSerialID = InventorySerial.sSerialID "
                + "     LEFT JOIN Category Category"
                + "         ON Inventory.sCategCd1 = Category.sCategrCd"
                + "     LEFT JOIN Category_Level2 Category_Level2"
                + "         ON Inventory.sCategCd2 = Category_Level2.sCategrCd"
                + "     LEFT JOIN Category_Level3 Category_Level3"
                + "         ON Inventory.sCategCd3 = Category_Level3.sCategrCd"
                + "     LEFT JOIN Category_Level4 Category_Level4"
                + "         ON Inventory.sCategCd4 = Category_Level4.sCategrCd"
                + "     LEFT JOIN Brand Brand"
                + "         ON Inventory.sBrandIDx = Brand.sBrandIDx"
                + "     LEFT JOIN Model Model"
                + "         ON Inventory.sModelIDx = Model.sModelIDx"
                + "     LEFT JOIN Color Color"
                + "         ON Inventory.sColorIDx = Color.sColorIDx"
                + "     LEFT JOIN Measure Measure"
                + "         ON Inventory.sMeasurID = Measure.sMeasurID"
                + "     LEFT JOIN Inv_Type InventoryType"
                + "         ON Inventory.sInvTypCd = InventoryType.sInvTypCd"
                + "     LEFT JOIN Model_Variant Variant"
                + "         ON Inventory.sVrntIDxx = Variant.sVrntIDxx"
                + "             ORDER BY InventoryStockRequestDetail.nEntryNox ASC";

        return lsSQL;
    }

    public static String getJasperReport(String psIndustryCode) {
        switch (psIndustryCode) {
            case "01":
                return MOBILE_PHONE_REPORT;
            case "02":
                return MOTORCYCLE_REPORT;
            case "03":
                return CAR_REPORT;
            case "04":
                return HOSPITALITY_REPORT;
            case "05":
                return LOS_PEDRITOS_REPORT;
            case "06":
                return GENERAL_REPORT;
            case "07":
                return APPLIANCE_REPORT;
            default:
                return GENERAL_REPORT;
        }
    }

}
