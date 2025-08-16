/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.inv.warehouse.t4.validators;

import org.guanzon.appdriver.iface.GValidator;

public class InventoryStockRequestApprovalValidatorFactory {

    public static GValidator make(String industryId) {
        switch (industryId) {
            case "01": //Mobile Phone
                return new InventoryStockRequestApproval_MP();
            case "02": //Motorcycle
                return new InventoryStockRequestApproval_MC();
            case "03": //Vehicle
                return new InventoryStockRequestApproval_Vehicle();
            case "04": //Monarch
                return new InventoryStockRequestApproval_Monarch();
            case "05": //Los Pedritos
                return new InventoryStockRequestApproval_LP();
            case "06": //General
                return new InventoryStockRequestApproval_General();
            case "07": //Appliances
                return new InventoryStockRequestApproval_Appliance();

            case "": //Main Office
                return new InventoryStockRequestApproval_General();
            default:
                return null;
        }
    }

}
