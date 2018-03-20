/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ghn.partner;

import com.google.gson.Gson;
import common.api.APIResponse;
import common.api.APIStatus;
import org.apache.log4j.Logger;

/**
 *
 * @author Bee
 */
public class PartnerClient {
    
    private static final Gson GSON = new Gson();
    private static final Logger LOGGER = Logger.getLogger(PartnerClient.class);
    
    private PartnerClient(){
        
    }    
    
    public static PartnerClient getInstance() {
        return PartnerClient.WarehouseGroupClientHolder.INSTANCE;
    }
     
    private static class WarehouseGroupClientHolder {
        private static final PartnerClient INSTANCE = new PartnerClient();
    }
    
    public APIResponse<Partner> create(Partner partner){
        try {
            
            if(partner.partnerCode == null || partner.partnerCode.isEmpty() || "".equals(partner.partnerCode)){
                return new APIResponse<>(APIStatus.INVALID, "Invalid data, require [partnerCode].");
            }
            
            if(partner.partnerName == null || partner.partnerName.isEmpty() || "".equals(partner.partnerName)){
                return new APIResponse<>(APIStatus.INVALID, "Invalid data, require [partnerCode].");
            }
            
            partner.partnerCode = partner.partnerCode.toUpperCase();
            Partner query = new Partner();
            query.partnerCode = partner.partnerCode;
            APIResponse<Partner> apiResult = PartnerDBService.getInstance().queryOne(query);
            if(apiResult == null || (apiResult.status != APIStatus.OK && apiResult.status != APIStatus.NOT_FOUND)){
                return new APIResponse<>(APIStatus.ERROR, "Management system is busy, please try again later.");
            }
            if(apiResult.status == APIStatus.OK){
                return new APIResponse<>(APIStatus.EXISTED, String.format("[%s] already exist", partner.partnerCode));
            }
            return PartnerDBService.getInstance().create(partner);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return new APIResponse<>(APIStatus.ERROR, "An error occurred, please try again later.");
        }
    }
    
    public APIResponse<Partner> update(String id, Partner partner){
        try {
            
            partner.partnerCode = partner.partnerCode.toUpperCase();           
            Partner query = new Partner();
            query.partnerCode = partner.partnerCode;
            APIResponse<Partner> apiResult = PartnerDBService.getInstance().queryOne(query);
            if(apiResult == null || (apiResult.status != APIStatus.OK && apiResult.status != APIStatus.NOT_FOUND)){
                return new APIResponse<>(APIStatus.ERROR, "Management system is busy, please try again later.");
            }
            
            if(apiResult.status == APIStatus.OK){
                Partner receiptGroupDb = apiResult.getFirst();
                if(!id.equals(receiptGroupDb.id)){
                    return new APIResponse<>(APIStatus.EXISTED, String.format("[%s] already exist", partner.partnerCode));
                }
            }
            return PartnerDBService.getInstance().update(id, partner);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return new APIResponse<>(APIStatus.ERROR, "An error occurred, please try again later.");
        }
    }
    
    public APIResponse<Partner> get(String partnerCode){
        try {
            Partner query = new Partner();
            query.partnerCode = partnerCode;
            return PartnerDBService.getInstance().queryOne(query);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return new APIResponse<>(APIStatus.ERROR, "An error occurred, please try again later.");
        }
    }
}
