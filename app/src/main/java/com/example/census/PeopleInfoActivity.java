package com.example.census;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.util.HashMap;
import java.util.Map;

import android.content.Intent;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.mikhaellopez.circularimageview.CircularImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PeopleInfoActivity extends AppCompatActivity {

    private TextView firstname,  sex, birthdate, cin, adress, phone, status;
    private CircularImageView profile1;

    private Intent intent;
    private int id=0;
    int id2 = 0;
    private String id1;
    private StringRequest stringRequest;
    private RequestQueue requestQueue;

    private StringRequest stringRequest1;
    private RequestQueue requestQueue1;

    private String url = "https://distyle-inclination.000webhostapp.com/android_login_api/index.php?action=selectInfo";
    private String url1 = "https://distyle-inclination.000webhostapp.com/android_login_api/index.php?action=delete";

    private SQLiteHandler db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.people_info);

    }

    @Override
    protected void onResume() {
        super.onResume();

        intent = getIntent();
        id = intent.getIntExtra("id",0);
        id2 = id;
        id1 = String.valueOf(id);
        firstname = (TextView)findViewById(R.id.viewFirstName);
        sex = (TextView)findViewById(R.id.viewSex);
        birthdate = (TextView)findViewById(R.id.viewBirthdate);
        cin = (TextView)findViewById(R.id.viewCin);
        adress = (TextView)findViewById(R.id.viewAdress);
        phone = (TextView)findViewById(R.id.viewPhone);
        status = (TextView)findViewById(R.id.viewStatus);
        profile1 = (CircularImageView)findViewById(R.id.profPic);

        requestQueue = Volley.newRequestQueue(this);
        requestQueue1 = Volley.newRequestQueue(this);

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }


        stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("result");
                    for(int i=0;i<jsonArray.length();i++)
                    {
                        JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                        firstname.setText(jsonObject1.getString("firstname") +" " + jsonObject1.getString("lastname"));
//                        lastname.setText(jsonObject1.getString("lastname"));
                        sex.setText(jsonObject1.getString("sex"));
                        birthdate.setText(jsonObject1.getString("birthdate"));
                        cin.setText(jsonObject1.getString("cin"));
                        adress.setText(jsonObject1.getString("adress"));
                        phone.setText(jsonObject1.getString("phone"));
                        status.setText(jsonObject1.getString("situation_marital"));
                        String imageb = jsonObject1.getString("profile_photo");

                        System.out.println("Aloooo "+status);

                        byte[] decodedString = Base64.decode(imageb,Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString,0,decodedString.length);
                        profile1.setImageBitmap(Bitmap.createScaledBitmap(decodedByte,130,130,true));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> hashMap = new HashMap<>();
                hashMap.put("id",id1);
                return hashMap;
            }
        };

        requestQueue.add(stringRequest);

    }

    /**
     * Logging out the user. Will set isLoggedIn flag to false in shared
     * preferences Clears the user data from sqlite users table
     * */
    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(PeopleInfoActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater =getMenuInflater();
        inflater.inflate(R.menu.menu_edit,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.menu_edit_logout)
        {
            session.setLogin(false);

            db.deleteUsers();

            // Launching the login activity
            Intent intent = new Intent(PeopleInfoActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        else if(id==R.id.menu_edit)
        {
            Intent intent = new Intent(PeopleInfoActivity.this,UpdatePeopleActivity.class);
            intent.putExtra("id",id2);
            startActivity(intent);
        }
        else if(id==R.id.menu_edit_delete)
        {
            stringRequest1 =new StringRequest(Request.Method.POST, url1, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    onBackPressed();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            })
            {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    HashMap<String,String> hashMap = new HashMap<>();
                    hashMap.put("id",id1);
                    return hashMap;
                }
            };
            requestQueue1.add(stringRequest1);
        }
        return super.onOptionsItemSelected(item);
    }
}
