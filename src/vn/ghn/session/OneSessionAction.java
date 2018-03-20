/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ghn.session;

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
public class OneSessionAction extends APIResource{
    private static final Logger LOGGER = Logger.getLogger(OneSessionAction.class);
    private static final Gson GSON = new Gson();

    public OneSessionAction(String path) {
        super(path);
    }
    
    @APIMethod(name = "GET")
    public void onGet(APIRequest req, APIResponder resp) {
        try {            
            String session = req.getVar("session");
            if (session == null || session.isEmpty()) {
                resp.respond(new APIResponse(APIStatus.INVALID, "Invalid session."));
                return;
            }            
            resp.respond(SessionClient.getByValue(session));            
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            resp.respond(new APIResponse(APIStatus.ERROR, ex.getMessage()));
        }
    }   
}
