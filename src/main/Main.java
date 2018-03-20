package main;

import com.google.gson.Gson;
import com.mongodb.client.MongoDatabase;
import com.mongodb.inst.DBInitHandler;
import com.mongodb.inst.MongoInstance;
import common.api.APIActionHandler;
import common.api.APIBlueprint;
import common.api.APIContainer;
import common.api.APIFactory;
import common.api.APIProtocol;
import common.api.APIRequest;
import common.api.APIResponder;
import common.api.APIResponse;
import common.api.APIServer;
import common.api.APIStatus;
import common.api.AutobuildHelper;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import org.apache.log4j.Logger;
import vn.common.lib.config.NConfig;
import vn.ghn.warehouse.ActionOneWarehouse;
import vn.ghn.warehouse.ActionWarehouse;
import vn.ghn.action.LoginAction;
import vn.ghn.action.LoginApiAcion;
import vn.ghn.action.LoginFbAction;
import vn.ghn.action.LoginT62Action;
import vn.ghn.gateway.APIGatewayClient;
import vn.ghn.gateway.APIRegistrationInformation;
import vn.ghn.partner.ActionOnePartner;
import vn.ghn.partner.ActionPartner;
import vn.ghn.partner.PartnerDBService;
import vn.ghn.permission.OnePermissionAction;
import vn.ghn.permission.PermissionAction;
import vn.ghn.permission.PermissionDBService;
import vn.ghn.role.OneRoleAction;
import vn.ghn.role.RoleAction;
import vn.ghn.role.RoleDBService;
import vn.ghn.session.OneSessionAction;
import vn.ghn.session.SessionDBService;
import vn.ghn.user.MultiUserAction;
import vn.ghn.user.OneUserAction;
import vn.ghn.user.UserAction;
import vn.ghn.user.UserDBService;
import vn.ghn.user.UserSearchAction;
import vn.ghn.warehouse.ActionSyncWarehouse;
import vn.ghn.warehouse.WarehouseDBService;

/**
 *
 * @author Bee
 */
public class Main {
   
    public static void main(String[] args) throws Exception {
        
        String env = System.getProperty("appenv");
        if (env == null) {
            env = "localhost";
        }
        NConfig.init("./conf/" + env + ".ini");                
        
        Logger.getLogger("profiler");
        
        AutobuildHelper.writePidFile();

        initDB(
                NConfig.getString("db.host"), 
                NConfig.getInt("db.port"), 
                NConfig.getString("db.user"),
                NConfig.getString("db.pass"),
                NConfig.getString("db.name"), 
                env
        );        
        //TODO push to restart service
        //TODO push to restart service
    }   
    
    private static void initDB(String host, int port, String user, String pass, String dbName, String env) throws Exception {
        
        try ( 
            Socket sock = new Socket()) {
            sock.connect(new InetSocketAddress(host, port), 500);
        }                                      
        
        MongoInstance mg = new MongoInstance(host, port, dbName,
            new DBInitHandler() {

                @Override
                public void onInitSuccess(MongoDatabase database) {
                    
                    try {
                                                
                        UserDBService.getInstance().initDB(database);        
                        RoleDBService.getInstance().initDB(database); 
                        PermissionDBService.getInstance().initDB(database);
                        WarehouseDBService.getInstance().initDB(database);
                        PartnerDBService.getInstance().initDB(database);
                        SessionDBService.getInstance().initDB(database);

                        if("localhost".equals(env)) {
                            ServerInit();
                        }else{
                            APIGatewayInit();
                            AutobuildHelper.commitStarted();
                        }          

                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }

                @Override
                public void onInitError(APIResponse apir) {
                    System.out.println(apir);
                }
            }, user, pass, "admin");
        mg.connect();                
    }
    
    private static void ServerInit() throws IOException{
        APIProtocol protocol = APIProtocol.HTTP;

        APIContainer container = new APIContainer();
        
        setupRouting(container);

        APIServer server = APIFactory.createAPIServer(protocol);
        server.setContainer(container);

        server.setConfiguration(NConfig.getString("service.host"), NConfig.getInt("service.port"));
        server.setDefaultHandler(new APIActionHandler() {
            @Override
            public void handle(APIRequest req, APIResponder resp) {  
                if ("OPTIONS".equals(req.getMethod())) {
                    APIResponse r = new APIResponse(APIStatus.OK, "Accept call");
//                    r.setHeader("Access-Control-Allow-Origin", "*");
//                    r.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
//                    r.setHeader("Access-Control-Allow-Headers", "X-Auth,X-WarehouseId");
                    resp.respond(r);
                }             
            }
        });
        server.start();
    }
    
    private static void APIGatewayInit(){
        
        APIServer server = APIFactory.createAPIServer(APIProtocol.THRIFT);
        
        APIContainer container = new APIContainer();
        
        setupRouting(container);
     
        APIRegistrationInformation regInfo = new APIRegistrationInformation();
        regInfo.codeVersion = System.getProperty("codeVersion");
        regInfo.protocol = APIProtocol.THRIFT;
        regInfo.resource = Arrays.asList(
            NConfig.getString("gateway.root")
        );
        
        regInfo.instanceId = System.getProperty("instanceId");
        
        APIGatewayClient.getInstance().init(NConfig.getString("gateway.host"), NConfig.getInt("gateway.port"));
        server.setContainer(container);
        
        int port = server.startRandomPort();
        
        regInfo.host = System.getProperty("host");
        regInfo.port = port;
        
        long timeOut = NConfig.getLong("gateway.timeOut");
        
        server.setTimeout(timeOut);
        
        System.out.println(new Gson().toJson(regInfo));
        APIGatewayClient.getInstance().register(regInfo); 
    }
    
    private static void setupRouting(APIContainer container) {

        APIBlueprint pb = new APIBlueprint(NConfig.getString("api-url.user"));        
        pb.addResource(new UserAction(""));
        container.addBlueprint(pb);
        
        pb = new APIBlueprint(NConfig.getString("api-url.userMulti"));        
        pb.addResource(new MultiUserAction(""));
        container.addBlueprint(pb);
        
        pb = new APIBlueprint(NConfig.getString("api-url.userMulti"));        
        pb.addResource(new MultiUserAction(""));
        container.addBlueprint(pb);
        
        pb = new APIBlueprint(NConfig.getString("api-url.user-search"));        
        pb.addResource(new UserSearchAction(""));
        container.addBlueprint(pb);
        
        pb = new APIBlueprint(NConfig.getString("api-url.user"));        
        pb.addResource(new OneUserAction("{id}"));
        container.addBlueprint(pb);
        
        pb = new APIBlueprint(NConfig.getString("api-url.permission"));        
        pb.addResource(new PermissionAction(""));
        container.addBlueprint(pb);
        
        // catch "/permission/{id}" path
        pb = new APIBlueprint(NConfig.getString("api-url.permission"));        
        pb.addResource(new OnePermissionAction("/{id}"));
        container.addBlueprint(pb);
        
        // catch "/warehouse/*" path
        pb = new APIBlueprint(NConfig.getString("api-url.warehouse"));        
        pb.addResource(new ActionWarehouse(""));
        container.addBlueprint(pb);
        
        // catch "/warehouse/sync/*" path
        pb = new APIBlueprint(NConfig.getString("api-url.warehouse-sync"));        
        pb.addResource(new ActionSyncWarehouse(""));
        container.addBlueprint(pb);
        
        // catch "/warehouse/{id}" path
        pb = new APIBlueprint(NConfig.getString("api-url.warehouse"));        
        pb.addResource(new ActionOneWarehouse("/{id}"));
        container.addBlueprint(pb);
             
        // catch "/role/*" path
        pb = new APIBlueprint(NConfig.getString("api-url.role"));        
        pb.addResource(new RoleAction(""));
        container.addBlueprint(pb);
        
        // catch "/region/{id}" path
        pb = new APIBlueprint(NConfig.getString("api-url.role"));        
        pb.addResource(new OneRoleAction("{id}"));
        container.addBlueprint(pb);
        
        // catch "/login/*" path
        pb = new APIBlueprint(NConfig.getString("api-url.login"));        
        pb.addResource(new LoginAction(""));
        container.addBlueprint(pb);
        
        // catch "/login-t62/*" path
        pb = new APIBlueprint(NConfig.getString("api-url.login-t62"));        
        pb.addResource(new LoginT62Action(""));
        container.addBlueprint(pb);
                
        pb = new APIBlueprint(NConfig.getString("api-url.login-fb"));        
        pb.addResource(new LoginFbAction(""));
        container.addBlueprint(pb);
                
        pb = new APIBlueprint(NConfig.getString("api-url.login-in"));        
        pb.addResource(new LoginApiAcion(""));
        container.addBlueprint(pb);
                
        pb = new APIBlueprint(NConfig.getString("api-url.session"));        
        pb.addResource(new OneSessionAction("/{session}"));
        container.addBlueprint(pb); 
        
        pb = new APIBlueprint(NConfig.getString("api-url.partner"));        
        pb.addResource(new ActionPartner(""));
        container.addBlueprint(pb);
        
        pb = new APIBlueprint(NConfig.getString("api-url.partner"));        
        pb.addResource(new ActionOnePartner("/{id}"));
        container.addBlueprint(pb);
        
        //===================== v1
        pb = new APIBlueprint(NConfig.getString("api-path.user"));        
        pb.addResource(new UserAction(""));
        container.addBlueprint(pb);
        
        pb = new APIBlueprint(NConfig.getString("api-path.userMulti"));        
        pb.addResource(new MultiUserAction(""));
        container.addBlueprint(pb);
        
        pb = new APIBlueprint(NConfig.getString("api-path.userMulti"));        
        pb.addResource(new MultiUserAction(""));
        container.addBlueprint(pb);
        
        pb = new APIBlueprint(NConfig.getString("api-path.user-search"));        
        pb.addResource(new UserSearchAction(""));
        container.addBlueprint(pb);
        
        pb = new APIBlueprint(NConfig.getString("api-path.user"));        
        pb.addResource(new OneUserAction("{id}"));
        container.addBlueprint(pb);
        
        pb = new APIBlueprint(NConfig.getString("api-path.permission"));        
        pb.addResource(new PermissionAction(""));
        container.addBlueprint(pb);
        
        pb = new APIBlueprint(NConfig.getString("api-path.permission"));        
        pb.addResource(new OnePermissionAction("/{id}"));
        container.addBlueprint(pb);
        
        pb = new APIBlueprint(NConfig.getString("api-path.warehouse"));        
        pb.addResource(new ActionWarehouse(""));
        container.addBlueprint(pb);
        
        pb = new APIBlueprint(NConfig.getString("api-path.warehouse-sync"));        
        pb.addResource(new ActionSyncWarehouse(""));
        container.addBlueprint(pb);
        
        pb = new APIBlueprint(NConfig.getString("api-path.warehouse"));        
        pb.addResource(new ActionOneWarehouse("/{id}"));
        container.addBlueprint(pb);

        pb = new APIBlueprint(NConfig.getString("api-path.role"));        
        pb.addResource(new RoleAction(""));
        container.addBlueprint(pb);
        
        pb = new APIBlueprint(NConfig.getString("api-path.role"));        
        pb.addResource(new OneRoleAction("{id}"));
        container.addBlueprint(pb);
        
        pb = new APIBlueprint(NConfig.getString("api-path.login"));        
        pb.addResource(new LoginAction(""));
        container.addBlueprint(pb);
        
        pb = new APIBlueprint(NConfig.getString("api-path.login-t62"));        
        pb.addResource(new LoginT62Action(""));
        container.addBlueprint(pb);
                
        pb = new APIBlueprint(NConfig.getString("api-path.login-fb"));        
        pb.addResource(new LoginFbAction(""));
        container.addBlueprint(pb);
                
        pb = new APIBlueprint(NConfig.getString("api-path.login-in"));        
        pb.addResource(new LoginApiAcion(""));
        container.addBlueprint(pb);
                
        pb = new APIBlueprint(NConfig.getString("api-path.session"));        
        pb.addResource(new OneSessionAction("/{session}"));
        container.addBlueprint(pb); 
        
        pb = new APIBlueprint(NConfig.getString("api-path.partner"));        
        pb.addResource(new ActionPartner(""));
        container.addBlueprint(pb);
        
        pb = new APIBlueprint(NConfig.getString("api-path.partner"));        
        pb.addResource(new ActionOnePartner("/{id}"));
        container.addBlueprint(pb);

    }    
}