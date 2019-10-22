package com.example.census;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.renderscript.Sampler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.mikhaellopez.circularimageview.CircularImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class PeopleListActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    private ListView listView;
    private ArrayList<CustomModelList> arrayList;
    private CustomAdapter arrayAdapter;
    private FloatingActionButton floatingActionButton;
    private StringRequest stringRequest;
    private RequestQueue requestQueue;
    private Intent intentMail;
    private String emailUser;
    private NavigationView navigationView;

    private StringRequest stringRequest2;
    private RequestQueue requestQueue2;
    private StringRequest stringRequest1;
    private RequestQueue requestQueue1;

    private String url = "https://distyle-inclination.000webhostapp.com/android_login_api/index.php?action=select";
    private String url1 = "https://distyle-inclination.000webhostapp.com/android_login_api/index.php?action=delete";
    private String url2 = "https://distyle-inclination.000webhostapp.com/android_login_api/index.php?action=selectInfoUser";
    private SQLiteHandler db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people_list);
        setNavigationViewListener();

    }

    @Override
    protected void onResume() {
        super.onResume();

        setNavigationViewListener();

        intentMail = getIntent();
        emailUser = intentMail.getStringExtra("username");

        drawerLayout = (DrawerLayout)findViewById(R.id.drawerB);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,R.string.Open,R.string.Close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        navigationView = (NavigationView)findViewById(R.id.navipam);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        floatingActionButton = (FloatingActionButton)findViewById(R.id.floatingActionButton);
        listView = (ListView) findViewById(R.id.listviewPeople);
        arrayList = new ArrayList<>();

        requestQueue2 = Volley.newRequestQueue(this);
        requestQueue = Volley.newRequestQueue(this);
        requestQueue1 = Volley.newRequestQueue(this);

        db = new SQLiteHandler(getApplicationContext());
        session = new SessionManager(getApplicationContext());
        if (!session.isLoggedIn()) {
            logoutUser();
        }
        HashMap<String, String> user = db.getUserDetails();
        final String email = user.get("email");


        View header = navigationView.getHeaderView(0);
        final TextView textDraw = (TextView)header.findViewById(R.id.nameDraw);
        final CircularImageView circu = (CircularImageView)header.findViewById(R.id.circulatoire);


        stringRequest2 = new StringRequest(Request.Method.POST, url2, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                System.out.println("Just "+response);
                JSONObject jsonObject2 = null;
                try {
                    jsonObject2 = new JSONObject(response);
                    JSONArray jsonArray = jsonObject2.getJSONArray("result");
                    for(int i=0;i<jsonArray.length();i++)
                    {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        textDraw.setText(jsonObject.getString("name"));
                        loadImg(jsonObject.getString("profile_photo"),circu);
                        System.out.println("AHHHHHHH"+jsonObject.getString("name"));
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
            protected Map<String,String> getParams()throws AuthFailureError
            {
                HashMap<String,String> hashMap = new HashMap<>();
                hashMap.put("username",email);
                return hashMap;
            }
        };

        requestQueue2.add(stringRequest2);



        stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
//                Intent intent = new Intent(AddContact.this,PeopleInfoActivity.class);
//                startActivity(intent);
//                onBackPressed();
                JSONObject jsonObject2 = null;
                try {
                    jsonObject2 = new JSONObject(response);
                    JSONArray jsonArray = jsonObject2.getJSONArray("result");
                    for(int i=0;i<jsonArray.length();i++)
                    {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String title = jsonObject.getString("situation_marital");
                            int id = jsonObject.getInt("id");
                            String lastname = jsonObject.getString("lastname");
                            String firstname = jsonObject.getString("firstname");
                            String cin = jsonObject.getString("cin");
                            String imageb = jsonObject.getString("profile_photo");
//                            arrayList.add(new SectionItem(title));

                            arrayList.add(new CustomModelList(id,firstname+" "+lastname,cin,imageb));
                        }
                    Collections.sort(arrayList, new Comparator<CustomModelList>() {
                        @Override
                        public int compare(CustomModelList o1, CustomModelList o2) {
                            return (o1.name.toString()).compareTo(o2.name.toString());
                        }
                    });
                    arrayAdapter = new CustomAdapter(PeopleListActivity.this,R.layout.custom_list_people,arrayList);
                    arrayAdapter.notifyDataSetChanged();
                    listView.setAdapter(arrayAdapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent intent = new Intent(PeopleListActivity.this,PeopleInfoActivity.class);
                            intent.putExtra("id",arrayList.get(position).getId());
                            startActivity(intent);
                        }
                    });

                    listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                        @Override
                        public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                            final AlertDialog.Builder builder = new AlertDialog.Builder(PeopleListActivity.this);
                            builder.setTitle("Action");

                            builder.setItems(new CharSequence[]{"Edit", "Open", "Delete"}, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, int which) {
                                    if (which==0)
                                    {
                                        Intent intent = new Intent(PeopleListActivity.this, UpdatePeopleActivity.class);
                                        intent.putExtra("id", arrayList.get(position).getId());
                                        startActivity(intent);
                                    }
                                    if(which==1)
                                    {
                                        Intent intent = new Intent(PeopleListActivity.this, PeopleInfoActivity.class);
                                        intent.putExtra("id", arrayList.get(position).getId());
                                        startActivity(intent);
                                    }
                                    if(which==2)
                                    {
                                        Intent intent = new Intent(PeopleListActivity.this, PeopleInfoActivity.class);

                                        intent.putExtra("id", arrayList.get(position).getId());
                                        int id = intent.getIntExtra("id",0);
                                        final String id1 = String.valueOf(id);

                                        stringRequest1 = new StringRequest(Request.Method.POST, url1, new Response.Listener<String>() {
                                            @Override
                                            public void onResponse(String response) {
                                                Intent intent = new Intent(PeopleListActivity.this, PeopleListActivity.class);
                                                startActivity(intent);
                                                finish();
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

                                }
                            });



                            builder.show();
                            return true;
                        }
                    });
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
            protected Map<String,String> getParams()throws AuthFailureError
            {
//                db = new SQLiteHandler(getApplicationContext());
//                HashMap<String, String> user = db.getUserDetails();
//                final String email = user.get("email");
                HashMap<String,String> hashMap = new HashMap<>();
                hashMap.put("username",email);
                return hashMap;
            }
        };

        requestQueue.add(stringRequest);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PeopleListActivity.this,AddPeopleActivity.class);
                startActivity(intent);
            }
        });
    }

    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(PeopleListActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater =getMenuInflater();
        inflater.inflate(R.menu.menu_list,menu);
        MenuItem menuItem = menu.findItem(R.id.app_bar_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText)
            {
                if(TextUtils.isEmpty(newText))
                {
                    arrayAdapter.filter("");
                    listView.clearTextFilter();
                }
                else
                {
                    arrayAdapter.filter(newText);
                }

                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.logoutSeaarch)
        {
            session.setLogin(false);

            db.deleteUsers();

            // Launching the login activity
            Intent intent = new Intent(PeopleListActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        if(actionBarDrawerToggle.onOptionsItemSelected(item))
        {
           return true;
        }


        return super.onOptionsItemSelected(item);
    }



    public void loadImg(String image, ImageView view)
    {
        byte[] decodedString = Base64.decode(image,Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString,0,decodedString.length);
        view.setImageBitmap(Bitmap.createScaledBitmap(decodedByte,130,130,true));
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        switch (menuItem.getItemId()) {

            case R.id.dashboard: {
                System.out.println("I am clicked");
                break;
            }
        }
        //close navigation drawer
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setNavigationViewListener() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.navipam);
        navigationView.setNavigationItemSelectedListener(this);
    }
}

