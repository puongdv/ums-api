/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ghn.warehouse;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.model.MongoCounter;
import com.mongodb.model.MongoModel;
import org.bson.Document;

/**
 *
 * @author Bee
 */
public class WarehouseDBService extends MongoModel<Warehouse>{
    
    private static String collectionName = "warehouse";

    public WarehouseDBService() {
        super("Warehouse",Warehouse.class);
    }

    @Override
    public void initDB(MongoDatabase db) {
       
        try {
            db.createCollection(collectionName);
        } catch (Exception ex) {
            //LOGGER.error(ex.getMessage(), ex);
        }

        try {
            MongoCollection<Document> col = db.getCollection(collectionName);
            col.createIndex(new Document("warehouse_code", 1));
            this.setCollection(col).setCounter(new MongoCounter(db.getCollection("counter")));
        } catch (Exception ex) {
            //LOGGER.error(ex.getMessage(), ex);
        }
    }
    
    public static WarehouseDBService getInstance() {
        return WarehouseDBServiceHolder.INSTANCE;
    }
    
    private static class WarehouseDBServiceHolder {
        private static final WarehouseDBService INSTANCE = new WarehouseDBService();
    }
    
}
