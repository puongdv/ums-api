/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ghn.permission;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.model.MongoCounter;
import com.mongodb.model.MongoModel;
import org.apache.log4j.Logger;
import org.bson.Document;
import vn.common.lib.cache.NLocalCache;
import vn.ghn.user.UserDBService;

/**
 *
 * @author Bee
 */
public class PermissionDBService extends MongoModel<Permission>{
    
    private static final Logger LOGGER = Logger.getLogger(UserDBService.class);
    private static String collectionName = "permission";

    public PermissionDBService() {
        super("Permission", Permission.class);
    }

    @Override
    public void initDB(MongoDatabase db) {        
        // NLocalCache is a LRU cache that supports expired time
        // cache maximum 1000 object, for maximum 3600 seconds
        // this.enableCache(new NLocalCache(1000, 3600));
        
        try {
            db.createCollection(collectionName);
        } catch (Exception ex) {
            //LOGGER.error(ex.getMessage(), ex);
        }
        
        try {

            MongoCollection<Document> col = db.getCollection(collectionName);
            col.createIndex(new Document("key", 1));
            this.setCollection(col).setCounter(new MongoCounter(db.getCollection("counter")));
        } catch (Exception ex) {
            //LOGGER.error(ex.getMessage(), ex);
        }        
    }
    
    public static PermissionDBService getInstance() {
        return PermissionDBServiceHolder.INSTANCE;
    }
    
    private static class PermissionDBServiceHolder {
        private static final PermissionDBService INSTANCE = new PermissionDBService();
    }
    
}
