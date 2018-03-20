package vn.ghn.session;

import com.google.gson.Gson;
import vn.ghn.user.User;
import vn.ghn.user.UserInfo;

public class SessionInfo{
    public String session;
    public String t62;
    public long expired;
    public UserInfo userInfo;
    
    public static SessionInfo getInstance(){
        return new SessionInfo();
    }
    
    public SessionInfo getBySessionUser(Session session, UserInfo userInfo){
        this.t62 = session.t62;
        this.session = session.value;
        this.expired = session.expired;
        this.userInfo = userInfo;
        return this;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }      
}