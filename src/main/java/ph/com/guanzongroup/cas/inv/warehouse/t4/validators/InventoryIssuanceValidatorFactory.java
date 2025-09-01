/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.inv.warehouse.t4.validators;

import org.guanzon.appdriver.iface.GValidator;

public class InventoryIssuanceValidatorFactory {

    public static GValidator make(String industryId) {
        switch (industryId) {
            case "01": //Mobile Phone
                return new InventoryStockIssuance_MP();
            case "02": //Motorcycle
                return new InventoryStockIssuance_MC();
            case "03": //Vehicle
                return new InventoryStockIssuance_Vehicle();
            case "04": //Monarch
                return new InventoryStockIssuance_Monarch();
            case "05": //Los Pedritos
                return new InventoryStockIssuance_LP();
            case "06": //General
                return new InventoryStockIssuance_General();
            case "07": //Appliances
                return new InventoryStockIssuance_Appliance();

            case "": //Main Office
                return new InventoryStockIssuance_General();
            default:
                return null;
        }
    }

}
