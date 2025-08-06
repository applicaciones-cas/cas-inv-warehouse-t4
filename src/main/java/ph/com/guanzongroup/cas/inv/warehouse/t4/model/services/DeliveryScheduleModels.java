package ph.com.guanzongroup.cas.inv.warehouse.t4.model.services;

import org.guanzon.appdriver.base.GRiderCAS;
import ph.com.guanzongroup.cas.inv.warehouse.t4.model.Model_Delivery_Schedule_Detail;
import ph.com.guanzongroup.cas.inv.warehouse.t4.model.Model_Delivery_Schedule_Master;

/**
 *
 * @author 12mnv
 */
public class DeliveryScheduleModels {

    public DeliveryScheduleModels(GRiderCAS applicationDriver) {
        poGRider = applicationDriver;
    }

    private final GRiderCAS poGRider;

    //Useable Model List
    private Model_Delivery_Schedule_Master poDeliverySchedule;
    private Model_Delivery_Schedule_Detail poDeliveryScheduleDetail;

    //Delivery_Schedule_Master & Details
    public Model_Delivery_Schedule_Master DeliverySchedule() {
        if (poGRider == null) {
            System.err.println("ParamTabulate.Performing: Application driver is not set.");
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
            System.err.println("ParamTabulate.Performing: Application driver is not set.");
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

}
