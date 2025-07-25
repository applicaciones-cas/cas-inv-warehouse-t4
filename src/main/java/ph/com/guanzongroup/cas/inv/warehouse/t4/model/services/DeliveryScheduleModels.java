package ph.com.guanzongroup.cas.inv.warehouse.t4.model.services;

import java.sql.SQLException;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.cas.parameter.model.Model_Branch;
import org.guanzon.cas.parameter.model.Model_Category;
import org.guanzon.cas.parameter.model.Model_Company;
import org.guanzon.cas.parameter.model.Model_Industry;
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
    private Model_Industry poIndustry;
    private Model_Company poCompany;
    private Model_Branch poBranch;
    private Model_Category poCategory;
//   private Model_Cluster poCluster;
//   private Model_Branch_Other poBranchOther;
//   private Model_Truck_Size poTruckSize;

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

    //Reference Model 
    public Model_Industry Industry() throws SQLException, GuanzonException {
        if (poGRider == null) {
            System.err.println("ParamTabulate.Performing: Application driver is not set.");
            return null;
        }

        if (poIndustry == null) {
            poIndustry = new Model_Industry();
            poIndustry.setApplicationDriver(poGRider);
            poIndustry.setXML("Model_Industry");
            poIndustry.setTableName("Industry");
            poIndustry.initialize();
        }

        return poIndustry;
    }

    public Model_Company Company() throws GuanzonException, SQLException {
        if (poGRider == null) {
            System.err.println("ParamTabulate.Performing: Application driver is not set.");
            return null;
        }

        if (poCompany == null) {
            poCompany = new Model_Company();
            poCompany.setApplicationDriver(poGRider);
            poCompany.setXML("Model_Company");
            poCompany.setTableName("Company");
            poCompany.initialize();
        }

        return poCompany;
    }

    public Model_Branch Branch() throws SQLException, GuanzonException {
        if (poGRider == null) {
            System.err.println("ParamTabulate.Performing: Application driver is not set.");
            return null;
        }

        if (poBranch == null) {
            poBranch = new Model_Branch();
            poBranch.setApplicationDriver(poGRider);
            poBranch.setXML("Model_Branch");
            poBranch.setTableName("Branch");
            poBranch.initialize();
        }

        return poBranch;
    }

    public Model_Category Category() throws SQLException, GuanzonException {
        if (poGRider == null) {
            System.err.println("ParamTabulate.Performing: Application driver is not set.");
            return null;
        }

        if (poCategory == null) {
            poCategory = new Model_Category();
            poCategory.setApplicationDriver(poGRider);
            poCategory.setXML("Model_Category");
            poCategory.setTableName("Category");
            poCategory.initialize();
        }

        return poCategory;
    }

//    public Model_Cluster Cluster() throws SQLException, GuanzonException {
//        if (poGRider == null) {
//            System.err.println("ParamTabulate.Performing: Application driver is not set.");
//            return null;
//        }
//
//        if (poCluster == null) {
//            poCluster = new Model_Cluster();
//            poCluster.setApplicationDriver(poGRider);
//            poCluster.setXML("Model_Cluster");
//            poCluster.setTableName("Cluster");
//            poCluster.initialize();
//        }
//
//        return poCluster;
//    }
    
//    public Model_Branch_Other BranchOther() throws SQLException, GuanzonException {
//        if (poGRider == null) {
//            System.err.println("ParamTabulate.Performing: Application driver is not set.");
//            return null;
//        }
//
//        if (poBranchOther == null) {
//            poBranchOther = new Model_Cluster();
//            poBranchOther.setApplicationDriver(poGRider);
//            poBranchOther.setXML("Model_Branch_Other");
//            poBranchOther.setTableName("Branch_Other");
//            poBranchOther.initialize();
//        }
//
//        return poBranchOther;
//    }
    
//    public Model_Truck_Size TruckSize() throws SQLException, GuanzonException {
//        if (poGRider == null) {
//            System.err.println("ParamTabulate.Performing: Application driver is not set.");
//            return null;
//        }
//
//        if (poTruckSize == null) {
//            poTruckSize = new Model_Cluster();
//            poTruckSize.setApplicationDriver(poGRider);
//            poTruckSize.setXML("Model_Truck_Size");
//            poTruckSize.setTableName("Truck_Size ");
//            poTruckSize.initialize();
//        }
//
//        return poTruckSize;
//    }
}
