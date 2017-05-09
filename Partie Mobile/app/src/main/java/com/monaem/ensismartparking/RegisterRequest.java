package com.monaem.ensismartparking;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by karugo on 1/6/17.
 */

public class RegisterRequest extends StringRequest{
    private static final String REGISTER_REQUEST_URL = "https://ensismartpark.000webhostapp.com/login_register/Register.php";
    private Map<String, String> params;

    public RegisterRequest (String name,String lastname, String email, String password, Response.Listener<String> listener){
        super(Method.POST,REGISTER_REQUEST_URL,listener,null);
        params = new HashMap<>();
        params.put("name",name);
        params.put("lastname",lastname);
        params.put("email",email);
        params.put("password",password);
    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }
}
