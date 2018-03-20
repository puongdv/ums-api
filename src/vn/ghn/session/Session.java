package vn.ghn.session;

import com.google.gson.Gson;
import common.api.APIModel;

public class Session extends APIModel<String>{
    
    public String userId;
    public String value;
    public String t62;
    public String userAgent;
    public Long expired;
    
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }    
}
