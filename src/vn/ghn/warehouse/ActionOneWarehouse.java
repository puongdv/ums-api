/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ghn.warehouse;

import com.google.gson.Gson;
import common.api.APIRequest;
import common.api.APIResource;
import common.api.APIResponder;
import common.api.APIResponse;
import common.api.APIStatus;
import common.api.annotation.APIMethod;
import org.apache.log4j.Logger;
import vn.ghn.utils.Utility;

/**
 *
 * @author Bee
 */
public class ActionOneWarehouse extends APIResource{
    private static final Logger LOGGER = Logger.getLogger(ActionOneWarehouse.class);
    private static final Gson GSON = new Gson();

    public ActionOneWarehouse(String path) {
        super(path);
    }     
    
    @APIMethod(name = "GET")
    public void onGet(APIRequest req, APIResponder resp) {
        try {
            
            String id = req.getVar("id");
            if (id == null || id.isEmpty()) {
                resp.respond(new APIResponse(APIStatus.INVALID, "Invalid id."));
                return;
            }
            
            APIResponse<Warehouse> apiResult = WarehouseClient.getInstance()
                    .get(Utility.stringArrToIntArr(id.split(",")));           
            
            if(apiResult == null){
                resp.respond(
                    new APIResponse(
                        APIStatus.ERROR, 
                        "Management system is busy, please try again later."
                    )
                );
                return;
            }                        
            
            resp.respond(apiResult);
            
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            resp.respond(new APIResponse(APIStatus.ERROR, "An error occurred, please try again later."));
        }
    }
    
    @APIMethod(name = "PUT")
    public void onPut(APIRequest req, APIResponder resp) {
        try {
            
            String id = req.getVar("id");
            if (id == null || id.isEmpty()) {
                resp.respond(new APIResponse(APIStatus.INVALID, "Invalid id."));
                return;
            }
            
            String content = req.getContent();
            Warehouse warehouse = null;
            if (content != null) {
                try {
                    warehouse = GSON.fromJson(content, Warehouse.class);
                } catch (Exception ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            } 
            
            if (warehouse == null){
                resp.respond(new APIResponse(APIStatus.INVALID, "Invalid data. Please put in JSON format."));
                return;
            }
            
            APIResponse<Warehouse> result = WarehouseClient.getInstance().update(Utility.parseInt(id), warehouse);            
            if(result == null){
                resp.respond(new APIResponse(APIStatus.ERROR, "Management system is busy, please try again later."));
                return;
            }                      
            resp.respond(result);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            resp.respond(new APIResponse(APIStatus.ERROR, "An error occurred, please try again later."));
        }
    }
    
    @APIMethod(name = "DELETE")
    public void onDelete(APIRequest req, APIResponder resp) {
        try {
            
            String id = req.getVar("id");
            if (id == null || id.isEmpty()) {
                resp.respond(new APIResponse(APIStatus.INVALID, "Invalid id."));
                return;
            }

            APIResponse<Warehouse> result = WarehouseClient.getInstance().delete(Utility.parseInt(id));
            if(result == null){
                resp.respond(new APIResponse(APIStatus.ERROR, "Management system is busy, please try again later."));
                return;
            }
            
            resp.respond(result);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            resp.respond(new APIResponse(APIStatus.ERROR, "An error occurred, please try again later."));
        }
    }
}
