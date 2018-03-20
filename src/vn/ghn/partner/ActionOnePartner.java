/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ghn.partner;

import com.google.gson.Gson;
import common.api.APIRequest;
import common.api.APIResource;
import common.api.APIResponder;
import common.api.APIResponse;
import common.api.APIStatus;
import common.api.annotation.APIMethod;
import org.apache.log4j.Logger;

/**
 *
 * @author Bee
 */
public class ActionOnePartner extends APIResource{
    private static final Logger LOGGER = Logger.getLogger(ActionOnePartner.class);
    private static final Gson GSON = new Gson();

    public ActionOnePartner(String path) {
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

            APIResponse<Partner> apiResult = PartnerDBService.getInstance().get(id);            
            if(apiResult == null){
                resp.respond(new APIResponse(APIStatus.ERROR, "Management system is busy, please try again later."));
                return;
            }                      
            resp.respond(apiResult);
            
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            resp.respond(new APIResponse(APIStatus.ERROR, "An error occurred, please try again later." + ex.getMessage()));
        }
    }
    
    @APIMethod(name = "PUT")
    public void onPut(APIRequest req, APIResponder resp) {
        try {
            // parse information from request
            String id = req.getVar("id");
            if (id == null || id.isEmpty()) {
                resp.respond(new APIResponse(APIStatus.INVALID, "Invalid id."));
                return;
            }
            
            String content = req.getContent();
            Partner partner = null;
            if (content != null) {
                try {
                    partner = GSON.fromJson(content, Partner.class);
                } catch (Exception ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            } 
            
            if (partner == null){
                resp.respond(new APIResponse(APIStatus.INVALID, "Invalid data. Please put in JSON format."));
                return;
            }
            
            APIResponse<Partner> apiResult = PartnerClient.getInstance().update(id, partner);            
            if(apiResult == null){
                resp.respond(new APIResponse(APIStatus.ERROR, "Management system is busy, please try again later."));
                return;
            }                      
            resp.respond(apiResult);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            resp.respond(new APIResponse(APIStatus.ERROR, "An error occurred, please try again later." + ex.getMessage()));
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

            APIResponse<Partner> apiResult = PartnerDBService.getInstance().delete(id);
            if(apiResult == null){
                resp.respond(new APIResponse(APIStatus.ERROR, "Management system is busy, please try again later."));
                return;
            }                      
            resp.respond(apiResult);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            resp.respond(new APIResponse(APIStatus.ERROR, "An error occurred, please try again later." + ex.getMessage()));
        }
    }
}
