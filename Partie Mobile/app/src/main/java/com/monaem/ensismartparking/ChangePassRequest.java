package com.monaem.ensismartparking;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;



public class ChangePassRequest extends StringRequest{
    private static final String CHANGE_PASS_REQUEST_URL = "https://ensismartpark.000webhostapp.com/login_register/ChangePass.php";
    private Map<String, String> params;

    public ChangePassRequest(String email, String password, String newpassword, Response.Listener<String> listener){
        super(Method.POST,CHANGE_PASS_REQUEST_URL,listener,null);
        params = new HashMap<>();
        params.put("email",email);
        params.put("password",password);
        params.put("newpassword",newpassword);
    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }
}
