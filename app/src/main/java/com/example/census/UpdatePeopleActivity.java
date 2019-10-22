package com.example.census;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import static android.media.MediaRecorder.VideoSource.CAMERA;

public class UpdatePeopleActivity extends AppCompatActivity {

    private String email;
    private String relationshipStatus;
    private EditText firstname, lastname, birthdate, cin, adress, phone;
    private CircularImageView profile1;
    private RadioGroup radioGroup;
    private RadioButton radioButton, male, female;
    private ImageView selectPhoto;
    private Intent intent;
    int id=0;
    private String id1;

    private StringRequest stringRequest;
    private RequestQueue requestQueue;

    private StringRequest stringRequest1;
    private RequestQueue requestQueue1;

    private Spinner spinner;
    private ArrayAdapter<CharSequence> adapter;

    private SQLiteHandler db;
    private SessionManager session;

    private static final String IMAGE_DIRECTORY="/demonuts";
    private int GALLERY = 1, CAMERA = 2;

    private String url = "https://distyle-inclination.000webhostapp.com/android_login_api/index.php?action=selectInfo";
    private String url1 = "https://distyle-inclination.000webhostapp.com/android_login_api/index.php?action=update";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_people);

        intent = getIntent();
        id = intent.getIntExtra("id",0);
        id1 = String.valueOf(id);

        requestQueue = Volley.newRequestQueue(this);
        requestQueue1 = Volley.newRequestQueue(this);
        db = new SQLiteHandler(getApplicationContext());
        session = new SessionManager(getApplicationContext());
        if (!session.isLoggedIn()) {
            logoutUser();
        }

        firstname = (EditText)findViewById(R.id.textEditFirstname);
        lastname = (EditText)findViewById(R.id.textEditLastname);
        birthdate = (EditText)findViewById(R.id.dateEditPeople);
        cin = (EditText)findViewById(R.id.textEditCin);
        adress = (EditText)findViewById(R.id.textEditAdress);
        phone = (EditText)findViewById(R.id.textEditPhone);
        spinner = (Spinner)findViewById(R.id.spinnerEditStatus);
        radioGroup = (RadioGroup)findViewById(R.id.radioGroupEdit);
        male = (RadioButton)findViewById(R.id.radioButtonMale);
        female = (RadioButton)findViewById(R.id.radioButtonFemale);
        profile1 = (CircularImageView)findViewById(R.id.profilePict);
        selectPhoto = (ImageView)findViewById(R.id.selectImage1);

        selectPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPictureDialog();
            }
        });

        cin.addTextChangedListener(new TextWatcher() {
            int prevL = 0;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                prevL = cin.getText().toString().length();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                String value = s.toString();
//                if(s.length()==3||s.length()==7)
//                {
//                    value += "-";
//                    cin.setText(value);
//                    cin.setSelection(value.length());
//                }


            }

            @Override
            public void afterTextChanged(Editable s) {
                int length = s.length();
                if ((prevL < length) && (length == 3 || length == 7)) {
                    String data = cin.getText().toString();
                    cin.setText(data + "-");
                    cin.setSelection(length + 1);
                }
            }
        });

        adapter = ArrayAdapter.createFromResource(this,
                R.array.status_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                relationshipStatus = adapter.getItem(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

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
                        firstname.setText(jsonObject1.getString("firstname"));
                        lastname.setText(jsonObject1.getString("lastname"));
                        birthdate.setText(jsonObject1.getString("birthdate"));
                        cin.setText(jsonObject1.getString("cin"));
                        adress.setText(jsonObject1.getString("adress"));
                        phone.setText(jsonObject1.getString("phone"));
                        String imageb = jsonObject1.getString("profile_photo");

                        byte[] decodedString = Base64.decode(imageb,Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString,0,decodedString.length);
                        profile1.setImageBitmap(Bitmap.createScaledBitmap(decodedByte,130,130,true));

                        String situation = jsonObject1.getString("situation_marital");
                        int position = Arrays.asList(getResources().getStringArray(R.array.status_array)).indexOf(situation);
                        spinner.setSelection(position);
                        if(jsonObject1.getString("sex").matches("Male"))
                        {
                            male.setChecked(true);
                        }
                        else if(jsonObject1.getString("sex").matches("Female"))
                        {
                            female.setChecked(true);
                        }
                        else
                        {
                            System.out.println("LE PROB: "+jsonObject1.getString("sex"));
                        }
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

    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(UpdatePeopleActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater =getMenuInflater();
        inflater.inflate(R.menu.menu_logout,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.logout_menu)
        {
            session.setLogin(false);

            db.deleteUsers();

            // Launching the login activity
            Intent intent = new Intent(UpdatePeopleActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void saveEdit(View view)
    {
        if(TextUtils.isEmpty(firstname.getText().toString())||TextUtils.isEmpty(lastname.getText().toString())||TextUtils.isEmpty(birthdate.getText().toString())||TextUtils.isEmpty(cin.getText().toString())||TextUtils.isEmpty(adress.getText().toString())||TextUtils.isEmpty(phone.getText().toString()))
        {
            Toast.makeText(this, "All fields must be filled", Toast.LENGTH_SHORT).show();
            return;
        }
        else
        {
            stringRequest1 = new StringRequest(Request.Method.POST, url1, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
//                Intent intent = new Intent(UpdatePeopleActivity.this,PeopleListActivity.class);
//                startActivity(intent);
//                    finish();
                    onBackPressed();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            })
            {
                protected Map<String,String>getParams()throws AuthFailureError
                {
                    int selectedId = radioGroup.getCheckedRadioButtonId();
                    radioButton = (RadioButton)findViewById(selectedId);
//                radioString = radioButton.getText().toString();

                    BitmapDrawable bitmapDrawable = (BitmapDrawable)profile1.getDrawable();
                    Bitmap bitmap = bitmapDrawable.getBitmap();

                    ByteArrayOutputStream Stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG,100,Stream);
                    byte[] bytes = Stream.toByteArray();
                    String imagineb = Base64.encodeToString(bytes,0);

                    HashMap<String,String> hashMap = new HashMap<>();
                    hashMap.put("id",id1);
                    hashMap.put("firstname",firstname.getText().toString());
                    hashMap.put("lastname",lastname.getText().toString());
                    hashMap.put("birthdate",birthdate.getText().toString());
                    hashMap.put("cin",cin.getText().toString());
                    hashMap.put("sex",radioButton.getText().toString());
                    hashMap.put("adress",adress.getText().toString());
                    hashMap.put("phone",phone.getText().toString());
                    hashMap.put("situation_marital",relationshipStatus);
                    hashMap.put("profile_photo",imagineb);
                    return hashMap;
                }
            };
            requestQueue1.add(stringRequest1);
        }
    }

    public void showPictureDialog()
    {
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("Select Action");
        String[] pictureDialogItems = {"Select photos form Gallery", "Select photo from Camera"};
        pictureDialog.setItems(pictureDialogItems, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which)
                {
                    case 0:
                        choosePhotoFromGallery();
                        break;
                    case 1:
                        takePhotoFromCamera();
                        break;
                }
            }
        });
        pictureDialog.show();
    }

    public void choosePhotoFromGallery()
    {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,GALLERY);
    }

    public void takePhotoFromCamera()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,CAMERA);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode,resultCode,data);
        if(resultCode==this.RESULT_CANCELED)
        {
            return;
        }
        if (requestCode==GALLERY)
        {

            if (data!=null)
            {
                Uri contentUri = data.getData();
                try
                {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),contentUri);
                    String path = saveImage(bitmap);
                    Toast.makeText(UpdatePeopleActivity.this, "Image Saved!", Toast.LENGTH_SHORT).show();
                    profile1.setImageBitmap(bitmap);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    Toast.makeText(UpdatePeopleActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                }
            }

        }
        else if (requestCode==CAMERA)
        {
            Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
            profile1.setImageBitmap(thumbnail);
            saveImage(thumbnail);
            Toast.makeText(UpdatePeopleActivity.this, "Image Saved!", Toast.LENGTH_SHORT).show();
        }
    }

    public String saveImage(Bitmap myBitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        File wallpaperDirectory = new File(
                Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);
        // have the object build the directory structure, if needed.
        if (!wallpaperDirectory.exists()) {
            wallpaperDirectory.mkdirs();
        }

        try {
            File f = new File(wallpaperDirectory, Calendar.getInstance()
                    .getTimeInMillis() + ".jpg");
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            MediaScannerConnection.scanFile(this,
                    new String[]{f.getPath()},
                    new String[]{"image/jpeg"}, null);
            fo.close();
            Log.d("TAG", "File Saved::---&gt;" + f.getAbsolutePath());

            return f.getAbsolutePath();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return "";
    }

    private void requestMultiplePermissions()
    {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            Toast.makeText(getApplicationContext(), "All permissions are granted by user!", Toast.LENGTH_SHORT).show();
                        }

                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // show alert dialog navigating to Settings
                            //openSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).
                withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Toast.makeText(getApplicationContext(), "Some Error! ", Toast.LENGTH_SHORT).show();
                    }
                })
                .onSameThread()
                .check();
    }

    private String saveToInternalStorage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir."+firstname.getText().toString()+lastname.getText().toString(), Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,"profile.jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }
}
