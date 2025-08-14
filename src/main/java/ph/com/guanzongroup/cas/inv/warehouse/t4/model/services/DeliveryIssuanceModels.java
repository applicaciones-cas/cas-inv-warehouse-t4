package ph.com.guanzongroup.cas.inv.warehouse.t4.model.services;

import org.guanzon.appdriver.base.GRiderCAS;
import ph.com.guanzongroup.cas.inv.warehouse.t4.model.Model_Delivery_Schedule_Detail;
import ph.com.guanzongroup.cas.inv.warehouse.t4.model.Model_Delivery_Schedule_Master;
import ph.com.guanzongroup.cas.inv.warehouse.t4.model.Model_Inventory_Transfer_Detail;
import ph.com.guanzongroup.cas.inv.warehouse.t4.model.Model_Inventory_Transfer_Detail_Expiration;
import ph.com.guanzongroup.cas.inv.warehouse.t4.model.Model_Inventory_Transfer_Master;

/**
 *
 * @author 12mnv
 */
public class DeliveryIssuanceModels {

    public DeliveryIssuanceModels(GRiderCAS applicationDriver) {
        poGRider = applicationDriver;
    }

    private final GRiderCAS poGRider;

    //Useable Model List
    private Model_Delivery_Schedule_Master poDeliverySchedule;
    private Model_Delivery_Schedule_Detail poDeliveryScheduleDetail;
    private Model_Inventory_Transfer_Master poInventoryTransferMaster;
    private Model_Inventory_Transfer_Detail poInventoryTransferDetail;
    private Model_Inventory_Transfer_Detail_Expiration poInventoryTransferDetailExpiration;

    //Delivery_Schedule_Master & Details
    public Model_Delivery_Schedule_Master DeliverySchedule() {
        if (poGRider == null) {
            System.err.println("DeliveryIssuanceModels.Performing: Application driver is not set.");
            return null;
        }

        if (poDeliverySchedule == null) {
            poDeliverySchedule = new Model_Delivery_Schedule_Master();
            poDeliverySchedule.setApplicationDriver(poGRider);
            poDeliverySchedule.setXML("Model_Delivery_Schedule_Master");
            poDeliverySchedule.setTableName("Delivery_Schedule_Master");
            poDeliverySchedule.initialize();
        }

        return poDeliverySchedule;
    }

    public Model_Delivery_Schedule_Detail DeliveryScheduleDetail() {
        if (poGRider == null) {
            System.err.println("DeliveryIssuanceModels.Performing: Application driver is not set.");
            return null;
        }

        if (poDeliveryScheduleDetail == null) {
            poDeliveryScheduleDetail = new Model_Delivery_Schedule_Detail();
            poDeliveryScheduleDetail.setApplicationDriver(poGRider);
            poDeliveryScheduleDetail.setXML("Model_Delivery_Schedule_Detail");
            poDeliveryScheduleDetail.setTableName("Delivery_Schedule_Detail");
            poDeliveryScheduleDetail.initialize();
        }

        return poDeliveryScheduleDetail;
    }

    //Inventory_Transfer_Master & Details & Other 
    public Model_Inventory_Transfer_Master InventoryTransferMaster() {
        if (poGRider == null) {
            System.err.println("DeliveryIssuanceModels.Performing: Application driver is not set.");
            return null;
        }

        if (poInventoryTransferMaster == null) {
            poInventoryTransferMaster = new Model_Inventory_Transfer_Master();
            poInventoryTransferMaster.setApplicationDriver(poGRider);
            poInventoryTransferMaster.setXML("Model_Inventory_Transfer_Master");
            poInventoryTransferMaster.setTableName("Inventory_Transfer_Master");
            poInventoryTransferMaster.initialize();
        }

        return poInventoryTransferMaster;
    }

    public Model_Inventory_Transfer_Detail InventoryTransferDetail() {
        if (poGRider == null) {
            System.err.println("DeliveryIssuanceModels.Performing: Application driver is not set.");
            return null;
        }

        if (poInventoryTransferDetail == null) {
            poInventoryTransferDetail = new Model_Inventory_Transfer_Detail();
            poInventoryTransferDetail.setApplicationDriver(poGRider);
            poInventoryTransferDetail.setXML("Model_Inventory_Transfer_Detail");
            poInventoryTransferDetail.setTableName("Inventory_Transfer_Detail");
            poInventoryTransferDetail.initialize();
        }

        return poInventoryTransferDetail;
    }

    public Model_Inventory_Transfer_Detail_Expiration InventoryTransferDetailExpiration() {
        if (poGRider == null) {
            System.err.println("DeliveryIssuanceModels.Performing: Application driver is not set.");
            return null;
        }

        if (poInventoryTransferDetailExpiration == null) {
            poInventoryTransferDetailExpiration = new Model_Inventory_Transfer_Detail_Expiration();
            poInventoryTransferDetailExpiration.setApplicationDriver(poGRider);
            poInventoryTransferDetailExpiration.setXML("Model_Inventory_Transfer_Detail_Expiration");
            poInventoryTransferDetailExpiration.setTableName("Inventory_Transfer_Detail_Expiration");
            poInventoryTransferDetailExpiration.initialize();
        }

        return poInventoryTransferDetailExpiration;
    }

}
