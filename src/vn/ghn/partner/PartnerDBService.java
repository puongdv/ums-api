/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ghn.partner;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.model.MongoModel;
import org.bson.Document;
import vn.common.lib.cache.NLocalCache;

/**
 *
 * @author Bee
 */
public class PartnerDBService extends MongoModel<Partner>{
    
    private static String collName = "partner";    

    private PartnerDBService() {
        super("Partner", Partner.class);
    }
    
    private static class ReceiptGroupDBServiceHolder {
        private static final PartnerDBService INSTANCE = new PartnerDBService();
    }
    
    public static PartnerDBService getInstance() {
        return ReceiptGroupDBServiceHolder.INSTANCE;
    }
    
    @Override
    public void initDB(MongoDatabase db) {
 
        //this.enableCache(new NLocalCache(1000, 3600));

        try {
            db.createCollection(collName);
        } catch (Exception ex) {
            //LOGGER.error(ex.getMessage(), ex);
        }

        try {
            MongoCollection<Document> col = db.getCollection(collName);
            this.setCollection(col);
            col.createIndex(new Document("partner_code", 1));
            col.createIndex(new Document("partner_name", 1));
        } catch (Exception ex) {
            //LOGGER.error(ex.getMessage(), ex);
        }
    }           
}
