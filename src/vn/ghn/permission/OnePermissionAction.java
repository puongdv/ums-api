/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ghn.permission;

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
public class OnePermissionAction extends APIResource{
    private static final Logger LOGGER = Logger.getLogger(OnePermissionAction.class);
    private static final Gson GSON = new Gson();

    public OnePermissionAction(String path) {
        super(path);
    }
    
    @APIMethod(name = "GET")
    public void onGet(APIRequest req, APIResponder resp) {
        try {
            // parse information from request
            String id = req.getVar("id");
            if (id == null || id.isEmpty()) {
                resp.respond(new APIResponse(APIStatus.INVALID, "Invalid id."));
                return;
            }
            
            APIResponse<Permission> result = PermissionDBService.getInstance().queryOne("code", id);
            if(result == null){
                resp.respond(new APIResponse(APIStatus.ERROR, "Management system is busy, please try again later."));
                return;
            } 
                     
            if(result.status != APIStatus.OK){
                result = PermissionDBService.getInstance().get(id);                        
                if(result == null){
                    resp.respond(new APIResponse(APIStatus.ERROR, "Management system is busy, please try again later."));
                    return;
                }
            }
            resp.respond(result);
            
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            resp.respond(new APIResponse(APIStatus.ERROR, "An error occurred, please try again later."));
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
            Permission permission = null;
            if (content != null) {
                try {
                    permission = GSON.fromJson(content, Permission.class);
                } catch (Exception ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            } 
            
            if (permission == null){
                resp.respond(new APIResponse(APIStatus.INVALID, "Invalid data. Please put in JSON format."));
                return;
            }
            
            APIResponse<Permission> result = PermissionDBService.getInstance().update(id, permission);            
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
            // parse information from request
            String id = req.getVar("id");
            if (id == null || id.isEmpty()) {
                resp.respond(new APIResponse(APIStatus.INVALID, "Invalid id."));
                return;
            }

            // call DB
            APIResponse<Permission> result = PermissionDBService.getInstance().delete(id);
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
