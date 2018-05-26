package com.hnweb.clawpal.lostorfound.pet.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.hnweb.clawpal.AppController;
import com.hnweb.clawpal.DashboardActivity;
import com.hnweb.clawpal.GPSTracker;
import com.hnweb.clawpal.R;
import com.hnweb.clawpal.Utils.Constants;
import com.hnweb.clawpal.Utils.RowItem;
import com.hnweb.clawpal.WebUrl.WebURL;
import com.hnweb.clawpal.location.LocationSet;
import com.hnweb.clawpal.location.MyLocationListener;
import com.hnweb.clawpal.login.LoginActivity;
import com.hnweb.clawpal.lostorfound.pet.adaptor.lostFoundPetsAdaptor;
import com.hnweb.clawpal.lostorfound.pet.model.lostFoundPetListModel;
import com.hnweb.clawpal.myfavpet.ViewPagerActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by HNWeb-11 on 8/3/2016.
 */

public class LostFoundPeDemotList extends LocationSet implements TextWatcher {
    private static final int PERMISSION_REQUEST_CODE_LOCATION = 1;
    ImageView mIvBuySale, mIvAdoption, mIvLostFound;
    Toolbar toolbar;
    int counttoatal = 0;
    int counttotalType = 0;
    int counttotalTypeFound = 0;
    int counttotalAnimal = 0;
    int counttotalAnimalAll = 0;
    int counttotalAnimalCat = 0;
    int counttotalAnimalCatAll = 0;
    int counttotalKeyword = 0;
    TextView mTvTitle;
    ImageView iv_search;
    ImageView iv_dog, iv_cat;
    String city;
    ProgressDialog progress;
    Button mBtnBuyOrSale;
    MyLocationListener myLocationListener;
    double latitude = 0.0d;
    double longitude = 0.0d;
    EditText mTvSearch;
    ArrayList<lostFoundPetListModel> lostFoundPetList;
    ArrayList<lostFoundPetListModel> myList;
    ArrayList<lostFoundPetListModel> myList1;


    lostFoundPetsAdaptor FoundPetsAdaptor;
    boolean isconnected = false;
    String locationAddress;
    GPSTracker gpsTracker;
    SharedPreferences pref;
    String search_type = "";
    // ListView listView;
    List<RowItem> rowItems;
    String title;
    LinearLayout ll_top2;
    ListView eLView;
    int flag = 1;
    Button loadmore;
    RadioButton rb;
    ImageView iv_setting;
    String userID;
    int position_to_scroll;
    int position_to_scroll1;
    int position_to_scroll1All;
    int position_to_scrollCat;
    int position_to_scrollCatAll;
    int position_to_scrollLost;
    int position_to_scrollFound;
    int position_to_scrollText;
    private int mult = 1;
    private RadioGroup radioGroup2;
    // GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    public void onCreate(Bundle savedInstaceState) {
        super.onCreate(savedInstaceState);
        setContentView(R.layout.activity_lost_found_pet_list);
        try {
            getInit();
            lostFoundPetList = new ArrayList<>();
            radioGroup2 = (RadioGroup) findViewById(R.id.radioGroup2);
            pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
            userID = pref.getString("userid", null);
            progress = new ProgressDialog(LostFoundPeDemotList.this);
            progress.setMessage("Please wait...");
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            eLView = (ListView) findViewById(R.id.myListView);
            View footerView = ((LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.loading_view, null, false);
            eLView.addFooterView(footerView);
            loadmore = (Button) footerView.findViewById(R.id.button);
            loadmore.setVisibility(View.GONE);
            iv_setting = (ImageView) findViewById(R.id.iv_setting);
           /* if (userID != null) {
                iv_setting.setVisibility(View.VISIBLE);

            } else {
                iv_setting.setVisibility(View.GONE);
            }
            iv_setting.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupwindow1(v);
                }
            });*/
            Constants.pets_animalLost1 = new ArrayList<>();
            Constants.pets_animalFound1 = new ArrayList<>();
            Constants.pets_list1 = new ArrayList<>();
            Constants.pets_animal1 = new ArrayList<>();
            Constants.pets_animalCat1 = new ArrayList<>();
            Constants.pets_animalText1 = new ArrayList<>();
            Constants.pets_animalText1All = new ArrayList<>();
            Constants.pets_animalCatAll = new ArrayList<>();

            iv_setting.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (userID != null) {
                        popupwindow1(view);

                    } else {
                        popupwindow2(view);
                    }

                }
            });
            loadmore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {

                    //   Toast.makeText(PetListActivity.this, ""+ eLView.getLastVisiblePosition(), Toast.LENGTH_SHORT).show();
                    // getAll_Pets_service(eLView.getLastVisiblePosition() + 10);
                    next_record_service(flag);
                }
            });
            toolbar = (Toolbar) findViewById(R.id.toolbar);
            toolbar.setNavigationIcon(R.drawable.back_btn_img);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            mTvSearch = (EditText) findViewById(R.id.searchtxt);
            iv_search = (ImageView) findViewById(R.id.iv_search);
            radioGroup2.clearCheck();
            radioGroup2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    rb = (RadioButton) group.findViewById(checkedId);
                    if (null != rb && checkedId > -1) {
                        //  mTvSearch.setText("");
                        loadmore.setVisibility(View.GONE);
                        // Toast.makeText(PetListActivity.this, rb.getText(), Toast.LENGTH_SHORT).show();
                        search_type = rb.getText().toString();
                        progress = new ProgressDialog(LostFoundPeDemotList.this);
                        progress.setMessage("Please wait...");
                        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progress.setIndeterminate(true);
                        progress.setCancelable(true);
                        progress.show();
                        // getLost_found_List(rb.getText().toString());
                        // getlostFoundPetsListFromCurrentLocationType(city,rb.getText().toString(),1);

                        if (rb.getText().toString().equalsIgnoreCase("My Lost Pet")) {
                            if (mTvSearch.getText().toString().length() != 0) {
                                search_type = "Lost";
                                getlostType(mTvSearch.getText().toString(), search_type, 1);
                            } else {
                                search_type = "Lost";
                                getlostType(city, search_type, 1);
                            }

                        } else if (rb.getText().toString().equalsIgnoreCase("I Found A Pet")) {
                            if (mTvSearch.getText().toString().length() != 0) {
                                search_type = "Found";
                                getFoundType(mTvSearch.getText().toString(), search_type, 1);
                            } else {
                                search_type = "Found";
                                getFoundType(city, search_type, 1);
                            }

                        } else if (rb.getText().toString().equalsIgnoreCase("All")) {
                            Constants.pets_list1 = new ArrayList<>();
                            getDataByCityLocation(city, 1);
                        }

                    }

                }
            });

            toolbar.setNavigationOnClickListener(

                    new View.OnClickListener() {
                        @Override

                        public void onClick(View v) {

                            Intent intent = new Intent(LostFoundPeDemotList.this, DashboardActivity.class);
                            startActivity(intent);
                            finish();
                        }

                    }

            );
            iv_dog = (ImageView) findViewById(R.id.iv_dog);
            iv_cat = (ImageView) findViewById(R.id.iv_cat);
          /*  tv_sort.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    popupwindow(view);
                }
            });*/

            eLView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    lostFoundPetListModel list_model = FoundPetsAdaptor.getItem(position);
                    Intent intent2 = new Intent(LostFoundPeDemotList.this, LostFoundPetDetails.class);
                   /* Bundle args = new Bundle();
                    args.putSerializable("ARRAYLIST",(Serializable) list_model);
                    intent2.putExtra("BUNDLE",args);
                    startActivity(intent);*/
                    // Create a Bundle and Put Bundle in to it
                    Bundle bundleObject = new Bundle();
                    bundleObject.putSerializable("key", list_model);
// Put Bundle in to Intent and call start Activity
                    intent2.putExtras(bundleObject);
                    startActivity(intent2);
                }
            });

            iv_search.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTvSearch.getText().toString().length() != 0) {
                        if (getCurrentFocus() != null) {
                            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                        }
                        progress = new ProgressDialog(LostFoundPeDemotList.this);
                        progress.setMessage("Please wait...");
                        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progress.setIndeterminate(true);
                        // progress.setCancelable(false);
                        progress.show();

                        //getlostFoundPetsListFromCurrentLocation(mTvSearch.getText().toString(), search_type);

                        getDataByCityLocationEnter(mTvSearch.getText().toString(), 1);
                    } else {
                        Toast.makeText(LostFoundPeDemotList.this, "Please enter search text.", Toast.LENGTH_SHORT).show();
                    }


                }
            });
            iv_dog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTvSearch.getText().toString().length() != 0) {

                        if (radioGroup2.getCheckedRadioButtonId() == -1) {
                            getDogList(mTvSearch.getText().toString(), "Dog", 1);
                        } else {
                            if (rb.getText().toString().equalsIgnoreCase("All")) {
                                getDogList(mTvSearch.getText().toString(), "Dog", 1);
                            } else {
                                getDogListAllCriteria(mTvSearch.getText().toString(), search_type, "Dog", 1);
                            }
                        }
                    } else {
                        getDogList(city, "Dog", 1);
                    }
                }
            });
            iv_cat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // popupwindow(view);
                    if (mTvSearch.getText().toString().length() != 0) {
                        if (radioGroup2.getCheckedRadioButtonId() == -1) {
                            getCatList(mTvSearch.getText().toString(), "Cat", 1);
                        } else {
                            if (rb.getText().toString().equalsIgnoreCase("All")) {
                                getCatList(mTvSearch.getText().toString(), "Cat", 1);
                            }else
                            {
                                getCatListALL(mTvSearch.getText().toString(), search_type, "Cat", 1);
                            }

                        }
                    } else {
                        getCatList(city, "Cat", 1);

                    }
                }
            });
            mBtnBuyOrSale.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                   /* if (userID != null) {*/
                    Intent intent = new Intent(LostFoundPeDemotList.this, ReportAPetActivity.class);
                    startActivity(intent);
                    //   }
                    /* else {
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE:

                                        Intent intent = new Intent(LostFoundPeDemotList.this, LoginActivity.class);
                                        intent.putExtra("key", "LostFoundPetList");
                                        startActivity(intent);


                                    case DialogInterface.BUTTON_NEGATIVE:

                                        // No button clicked // do nothing Toast.makeText(AlertDialogActivity.this, "No Clicked", Toast.LENGTH_LONG).show();
                                        break;
                                }
                            }
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(LostFoundPeDemotList.this);
                        builder.setTitle("Message");
                        builder.setCancelable(false);
                        builder.setMessage("Please Login ").setPositiveButton("Login", dialogClickListener).setNegativeButton("No", dialogClickListener).show();

                    }*/

                }
            });
            //getAllPetsLostAndFound(flag);


            // getAllPetsLostAndFoundNew();

         /*   GPSTracker gpsTracker = new GPSTracker(this);
            if (gpsTracker.getIsGPSTrackingEnabled()) {
                Double stringLatitude = gpsTracker.latitude;
                Double stringgong = gpsTracker.longitude;
                // LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);


                LocationAddress locationAddress = new LocationAddress();
                locationAddress.getAddressFromLocation(stringLatitude, stringgong,
                        getApplicationContext(), new GeocoderHandler());


            } else {

                gpsTracker.showSettingsAlert();
            }*/
            myLocationListener = new MyLocationListener(LostFoundPeDemotList.this);
            if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, getApplicationContext(), LostFoundPeDemotList.this)) {
                fetchLocationData();
            } else {
                requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, PERMISSION_REQUEST_CODE_LOCATION, getApplicationContext(), LostFoundPeDemotList.this);
            }

            mTvSearch.addTextChangedListener(this);

            mTvSearch.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
            mTvSearch.setImeActionLabel("Search", EditorInfo.IME_ACTION_SEARCH);

            mTvSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                        getDataByCityLocationEnter(mTvSearch.getText().toString(), 1);

                        return true;
                    }
                    return false;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getInit() {

        ll_top2 = (LinearLayout) findViewById(R.id.ll_top2);

        mBtnBuyOrSale = (Button) findViewById(R.id.activity_login_btn_buysale);
    }

    public void getDogList(final String location, final String s_type, final int count) {
        counttotalAnimal = count;
        position_to_scroll1 = Constants.pets_animal1.size();
        progress.show();
        try {
            String url = WebURL.SEARCH_LOSTFOUND_CITYNANIMAL_PAGE + "keyword=" + location + "&type=" + s_type + "&currentpage=" + counttotalAnimal;
            ;

            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, url,
                    null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {

                        if (progress.isShowing()) {
                            progress.dismiss();
                        }
                        int flag = response.getInt("flag");
                        int totalcount = response.getInt("totalpages");
                        if (totalcount == counttotalAnimal) {
                            loadmore.setVisibility(View.GONE);
                        } else {
                            loadmore.setVisibility(View.VISIBLE);
                        }

                        if (flag == 1) {

                            JSONArray jsonArray = response.getJSONArray("response");
                            myList1 = new ArrayList<>();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                Constants.pets_animal1.add(new lostFoundPetListModel(jsonObject.getString("lost_found_id"), jsonObject.getString("title"), jsonObject.getString("name"),
                                        jsonObject.getString("age_range"), jsonObject.getString("gender"), jsonObject.getString("type_of_animal"),
                                        jsonObject.getString("breed_type"), jsonObject.getString("report_type"), jsonObject.getString("description"),
                                        jsonObject.getString("location"), jsonObject.getString("state"), jsonObject.getString("image"),
                                        jsonObject.getString("reporter_name"), jsonObject.getString("reporter_email"), jsonObject.getString("reporter_contact"),
                                        jsonObject.getString("report_date_time"), jsonObject.getString("address"), jsonObject.getString("latitude"), jsonObject.getString("longitude"), jsonObject.getString("country"), jsonObject.getString("zip")));

                            }
                            FoundPetsAdaptor = new lostFoundPetsAdaptor(LostFoundPeDemotList.this, Constants.pets_animal1);
                            eLView.setAdapter(FoundPetsAdaptor);
                            System.out.println("position_to_scroll" + position_to_scroll1);

                            eLView.setSelection(position_to_scroll1 - 1);
                            eLView.smoothScrollToPosition(position_to_scroll1 - 1);

                            counttotalAnimal = counttotalAnimal + 1;

                            loadmore.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View arg0) {

                                    getDogList(location, s_type, counttotalAnimal);
                                }
                            });

                            // ((RadioButton) radioGroup.findViewById(R.id.radioButton2)).setChecked(true);

                        } else {
                            Toast.makeText(LostFoundPeDemotList.this, "Record not found.", Toast.LENGTH_SHORT).show();
                            loadmore.setVisibility(View.GONE);
                            Constants.pets_animal1 = new ArrayList<>();
                            FoundPetsAdaptor = new lostFoundPetsAdaptor(LostFoundPeDemotList.this, Constants.pets_animal1);
                            eLView.setAdapter(FoundPetsAdaptor);
                            Toast.makeText(LostFoundPeDemotList.this, "No record found.", Toast.LENGTH_SHORT).show();
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(jsonObjReq, "jreq");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getDogListAllCriteria(final String location, final String s_type, final String animal, final int count) {
        counttotalAnimalAll = count;
        position_to_scroll1All = Constants.pets_animalText1All.size();
        progress.show();
        String url = WebURL.SERACH_LOSTFOUND_ALLCRITERIA + "keyword=" + location + "&type=" + s_type + "&animal=" + animal + "&currentpage=" + counttotalAnimalAll;

        try {
            ;

            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, url,
                    null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {

                        if (progress.isShowing()) {
                            progress.dismiss();
                        }
                        int flag = response.getInt("flag");
                        int totalcount = response.getInt("totalpages");
                        if (totalcount == counttotalAnimalAll) {
                            loadmore.setVisibility(View.GONE);
                        } else {
                            loadmore.setVisibility(View.VISIBLE);
                        }

                        if (flag == 1) {

                            JSONArray jsonArray = response.getJSONArray("response");
                            myList1 = new ArrayList<>();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                Constants.pets_animalText1All.add(new lostFoundPetListModel(jsonObject.getString("lost_found_id"), jsonObject.getString("title"), jsonObject.getString("name"),
                                        jsonObject.getString("age_range"), jsonObject.getString("gender"), jsonObject.getString("type_of_animal"),
                                        jsonObject.getString("breed_type"), jsonObject.getString("report_type"), jsonObject.getString("description"),
                                        jsonObject.getString("location"), jsonObject.getString("state"), jsonObject.getString("image"),
                                        jsonObject.getString("reporter_name"), jsonObject.getString("reporter_email"), jsonObject.getString("reporter_contact"),
                                        jsonObject.getString("report_date_time"), jsonObject.getString("address"), jsonObject.getString("latitude"), jsonObject.getString("longitude"), jsonObject.getString("country"), jsonObject.getString("zip")));

                            }
                            FoundPetsAdaptor = new lostFoundPetsAdaptor(LostFoundPeDemotList.this, Constants.pets_animalText1All);
                            eLView.setAdapter(FoundPetsAdaptor);
                            System.out.println("position_to_scroll" + position_to_scroll1All);

                            eLView.setSelection(position_to_scroll1All - 1);
                            eLView.smoothScrollToPosition(position_to_scroll1All - 1);

                            counttotalAnimalAll = counttotalAnimalAll + 1;

                            loadmore.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View arg0) {

                                    getDogListAllCriteria(location, s_type, animal, counttotalAnimalAll);
                                }
                            });

                            // ((RadioButton) radioGroup.findViewById(R.id.radioButton2)).setChecked(true);

                        } else {
                            Toast.makeText(LostFoundPeDemotList.this, "Record not found.", Toast.LENGTH_SHORT).show();
                            loadmore.setVisibility(View.GONE);
                            Constants.pets_animalText1All = new ArrayList<>();
                            FoundPetsAdaptor = new lostFoundPetsAdaptor(LostFoundPeDemotList.this, Constants.pets_animalText1All);
                            eLView.setAdapter(FoundPetsAdaptor);
                            Toast.makeText(LostFoundPeDemotList.this, "No record found.", Toast.LENGTH_SHORT).show();
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(jsonObjReq, "jreq");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getCatList(final String location, final String s_type, int count) {
        counttotalAnimalCat = count;
        position_to_scrollCat = Constants.pets_animalCat1.size();
        progress.show();
        try {
            String url = WebURL.SEARCH_LOSTFOUND_CITYNANIMAL_PAGE + "keyword=" + location + "&type=" + s_type + "&currentpage=" + counttotalAnimalCat;
            ;

            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, url,
                    null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {

                        if (progress.isShowing()) {
                            progress.dismiss();
                        }
                        int flag = response.getInt("flag");
                        int totalcount = response.getInt("totalpages");
                        if (totalcount == counttotalAnimalCat) {
                            loadmore.setVisibility(View.GONE);
                        } else {
                            loadmore.setVisibility(View.VISIBLE);
                        }

                        if (flag == 1) {

                            JSONArray jsonArray = response.getJSONArray("response");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                Constants.pets_animalCat1.add(new lostFoundPetListModel(jsonObject.getString("lost_found_id"), jsonObject.getString("title"), jsonObject.getString("name"),
                                        jsonObject.getString("age_range"), jsonObject.getString("gender"), jsonObject.getString("type_of_animal"),
                                        jsonObject.getString("breed_type"), jsonObject.getString("report_type"), jsonObject.getString("description"),
                                        jsonObject.getString("location"), jsonObject.getString("state"), jsonObject.getString("image"),
                                        jsonObject.getString("reporter_name"), jsonObject.getString("reporter_email"), jsonObject.getString("reporter_contact"),
                                        jsonObject.getString("report_date_time"), jsonObject.getString("address"), jsonObject.getString("latitude"), jsonObject.getString("longitude"), jsonObject.getString("country"), jsonObject.getString("zip")));

                            }
                            FoundPetsAdaptor = new lostFoundPetsAdaptor(LostFoundPeDemotList.this, Constants.pets_animalCat1);
                            eLView.setAdapter(FoundPetsAdaptor);
                            System.out.println("position_to_scroll" + position_to_scrollCat);

                            eLView.setSelection(position_to_scrollCat - 1);
                            eLView.smoothScrollToPosition(position_to_scrollCat - 1);

                            counttotalAnimalCat = counttotalAnimalCat + 1;

                            loadmore.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View arg0) {

                                    getCatList(location, s_type, counttotalAnimalCat);
                                }
                            });

                            // ((RadioButton) radioGroup.findViewById(R.id.radioButton2)).setChecked(true);

                        } else {
                            Toast.makeText(LostFoundPeDemotList.this, "Record not found.", Toast.LENGTH_SHORT).show();
                            loadmore.setVisibility(View.GONE);
                            Constants.pets_animalCat1 = new ArrayList<>();
                            FoundPetsAdaptor = new lostFoundPetsAdaptor(LostFoundPeDemotList.this, Constants.pets_animalCat1);
                            eLView.setAdapter(FoundPetsAdaptor);
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(jsonObjReq, "jreq");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getCatListALL(final String location, final String s_type, final String animal, int count) {
        counttotalAnimalCatAll = count;
        position_to_scrollCatAll = Constants.pets_animalCatAll.size();
        progress.show();
        try {
            String url = WebURL.SERACH_LOSTFOUND_ALLCRITERIA + "keyword=" + location + "&type=" + s_type + "&animal=" + animal + "&currentpage=" + counttotalAnimalCatAll;
            ;

            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, url,
                    null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {

                        if (progress.isShowing()) {
                            progress.dismiss();
                        }
                        int flag = response.getInt("flag");
                        int totalcount = response.getInt("totalpages");
                        if (totalcount == counttotalAnimalCatAll) {
                            loadmore.setVisibility(View.GONE);
                        } else {
                            loadmore.setVisibility(View.VISIBLE);
                        }

                        if (flag == 1) {

                            JSONArray jsonArray = response.getJSONArray("response");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                Constants.pets_animalCatAll.add(new lostFoundPetListModel(jsonObject.getString("lost_found_id"), jsonObject.getString("title"), jsonObject.getString("name"),
                                        jsonObject.getString("age_range"), jsonObject.getString("gender"), jsonObject.getString("type_of_animal"),
                                        jsonObject.getString("breed_type"), jsonObject.getString("report_type"), jsonObject.getString("description"),
                                        jsonObject.getString("location"), jsonObject.getString("state"), jsonObject.getString("image"),
                                        jsonObject.getString("reporter_name"), jsonObject.getString("reporter_email"), jsonObject.getString("reporter_contact"),
                                        jsonObject.getString("report_date_time"), jsonObject.getString("address"), jsonObject.getString("latitude"), jsonObject.getString("longitude"), jsonObject.getString("country"), jsonObject.getString("zip")));

                            }
                            FoundPetsAdaptor = new lostFoundPetsAdaptor(LostFoundPeDemotList.this, Constants.pets_animalCatAll);
                            eLView.setAdapter(FoundPetsAdaptor);
                            System.out.println("position_to_scroll" + position_to_scrollCatAll);

                            eLView.setSelection(position_to_scrollCatAll - 1);
                            eLView.smoothScrollToPosition(position_to_scrollCatAll - 1);

                            counttotalAnimalCatAll = counttotalAnimalCatAll + 1;

                            loadmore.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View arg0) {

                                    getCatListALL(location, s_type, animal, counttotalAnimalCatAll);
                                }
                            });

                            // ((RadioButton) radioGroup.findViewById(R.id.radioButton2)).setChecked(true);

                        } else {
                            Toast.makeText(LostFoundPeDemotList.this, "Record not found.", Toast.LENGTH_SHORT).show();
                            loadmore.setVisibility(View.GONE);
                            Constants.pets_animalCatAll = new ArrayList<>();
                            FoundPetsAdaptor = new lostFoundPetsAdaptor(LostFoundPeDemotList.this, Constants.pets_animalCatAll);
                            eLView.setAdapter(FoundPetsAdaptor);
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(jsonObjReq, "jreq");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getlostType(final String location, final String s_type, int count) {
        counttotalType = count;
        position_to_scrollLost = Constants.pets_animalLost1.size();
        progress.show();
        try {
            String url = WebURL.SEARCH_LOSTFOUND_CITYNTYPE_PAGE + "keyword=" + location + "&type=" + s_type + "&currentpage=" + counttotalType;
            ;

            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, url,
                    null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {

                        if (progress.isShowing()) {
                            progress.dismiss();
                        }
                        int flag = response.getInt("flag");
                        int totalcount = response.getInt("totalpages");


                        if (totalcount == counttotalType) {
                            loadmore.setVisibility(View.GONE);
                        } else {
                            loadmore.setVisibility(View.VISIBLE);
                        }


                        if (flag == 1) {

                            JSONArray jsonArray = response.getJSONArray("response");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                Constants.pets_animalLost1.add(new lostFoundPetListModel(jsonObject.getString("lost_found_id"), jsonObject.getString("title"), jsonObject.getString("name"),
                                        jsonObject.getString("age_range"), jsonObject.getString("gender"), jsonObject.getString("type_of_animal"),
                                        jsonObject.getString("breed_type"), jsonObject.getString("report_type"), jsonObject.getString("description"),
                                        jsonObject.getString("location"), jsonObject.getString("state"), jsonObject.getString("image"),
                                        jsonObject.getString("reporter_name"), jsonObject.getString("reporter_email"), jsonObject.getString("reporter_contact"),
                                        jsonObject.getString("report_date_time"), jsonObject.getString("address"), jsonObject.getString("latitude"), jsonObject.getString("longitude"), jsonObject.getString("country"), jsonObject.getString("zip")));

                            }

                            FoundPetsAdaptor = new lostFoundPetsAdaptor(LostFoundPeDemotList.this, Constants.pets_animalLost1);
                            eLView.setAdapter(FoundPetsAdaptor);
                            System.out.println("position_to_scroll" + position_to_scrollLost);

                            eLView.setSelection(position_to_scrollLost - 1);
                            eLView.smoothScrollToPosition(position_to_scrollLost - 1);

                            counttotalType = counttotalType + 1;

                            loadmore.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View arg0) {

                                    //   Toast.makeText(PetListActivity.this, ""+ eLView.getLastVisiblePosition(), Toast.LENGTH_SHORT).show();
                                    // getAll_Pets_service(eLView.getLastVisiblePosition() + 10);
                                    getlostType(location, s_type, counttotalType);
                                }
                            });

                            // ((RadioButton) radioGroup.findViewById(R.id.radioButton2)).setChecked(true);

                        } else {
                            Toast.makeText(LostFoundPeDemotList.this, "Record not found.", Toast.LENGTH_SHORT).show();
                            loadmore.setVisibility(View.GONE);
                            Constants.pets_animalLost1 = new ArrayList<>();
                            FoundPetsAdaptor = new lostFoundPetsAdaptor(LostFoundPeDemotList.this, Constants.pets_animalLost1);
                            eLView.setAdapter(FoundPetsAdaptor);
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(jsonObjReq, "jreq");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getFoundType(final String location, final String s_type, int count) {
        counttotalTypeFound = count;
        position_to_scrollFound = Constants.pets_animalFound1.size();
        progress.show();
        try {
            String url = WebURL.SEARCH_LOSTFOUND_CITYNTYPE_PAGE + "keyword=" + location + "&type=" + s_type + "&currentpage=" + counttotalTypeFound;
            ;

            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, url,
                    null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {

                        if (progress.isShowing()) {
                            progress.dismiss();
                        }
                        int flag = response.getInt("flag");
                        int totalcount = response.getInt("totalpages");


                        if (totalcount == counttotalTypeFound) {
                            loadmore.setVisibility(View.GONE);
                        } else {
                            loadmore.setVisibility(View.VISIBLE);
                        }


                        if (flag == 1) {

                            JSONArray jsonArray = response.getJSONArray("response");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                Constants.pets_animalFound1.add(new lostFoundPetListModel(jsonObject.getString("lost_found_id"), jsonObject.getString("title"), jsonObject.getString("name"),
                                        jsonObject.getString("age_range"), jsonObject.getString("gender"), jsonObject.getString("type_of_animal"),
                                        jsonObject.getString("breed_type"), jsonObject.getString("report_type"), jsonObject.getString("description"),
                                        jsonObject.getString("location"), jsonObject.getString("state"), jsonObject.getString("image"),
                                        jsonObject.getString("reporter_name"), jsonObject.getString("reporter_email"), jsonObject.getString("reporter_contact"),
                                        jsonObject.getString("report_date_time"), jsonObject.getString("address"), jsonObject.getString("latitude"), jsonObject.getString("longitude"), jsonObject.getString("country"), jsonObject.getString("zip")));

                            }
                            FoundPetsAdaptor = new lostFoundPetsAdaptor(LostFoundPeDemotList.this, Constants.pets_animalFound1);
                            eLView.setAdapter(FoundPetsAdaptor);
                            System.out.println("position_to_scroll" + position_to_scrollFound);

                            eLView.setSelection(position_to_scrollFound - 1);
                            eLView.smoothScrollToPosition(position_to_scrollFound - 1);
                            counttotalTypeFound = counttotalTypeFound + 1;

                            loadmore.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View arg0) {

                                    //   Toast.makeText(PetListActivity.this, ""+ eLView.getLastVisiblePosition(), Toast.LENGTH_SHORT).show();
                                    // getAll_Pets_service(eLView.getLastVisiblePosition() + 10);
                                    getFoundType(location, s_type, counttotalTypeFound);
                                }
                            });

                            // ((RadioButton) radioGroup.findViewById(R.id.radioButton2)).setChecked(true);

                        } else {
                            Constants.pets_animalFound1 = new ArrayList<>();
                            FoundPetsAdaptor = new lostFoundPetsAdaptor(LostFoundPeDemotList.this, Constants.pets_animalFound1);
                            eLView.setAdapter(FoundPetsAdaptor);
                            Toast.makeText(LostFoundPeDemotList.this, "Record not found.", Toast.LENGTH_SHORT).show();
                            loadmore.setVisibility(View.GONE);
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(jsonObjReq, "jreq");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //   get list of pets according to buy or sale
    public void getLost_found_List(String lostorfound) {
        try {
            String url = WebURL.GET_LOSTFOUND_PAYMENTDONE_BYTYPE;

            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, url + lostorfound,
                    null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        if (progress.isShowing()) {
                            progress.dismiss();
                        }
                        if (response.has("flag")) {
                            lostFoundPetList = new ArrayList<>();
                            JSONArray jsonArray = response.getJSONArray("response");

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                lostFoundPetList.add(new lostFoundPetListModel(jsonObject.getString("lost_found_id"), jsonObject.getString("title"), jsonObject.getString("name"),
                                        jsonObject.getString("age_range"), jsonObject.getString("gender"), jsonObject.getString("type_of_animal"),
                                        jsonObject.getString("breed_type"), jsonObject.getString("report_type"), jsonObject.getString("description"),
                                        jsonObject.getString("location"), jsonObject.getString("state"), jsonObject.getString("image"),
                                        jsonObject.getString("reporter_name"), jsonObject.getString("reporter_email"), jsonObject.getString("reporter_contact"),
                                        jsonObject.getString("report_date_time"), jsonObject.getString("address"), jsonObject.getString("latitude"), jsonObject.getString("longitude"), jsonObject.getString("country"), jsonObject.getString("zip")));
                            }
                            FoundPetsAdaptor = new lostFoundPetsAdaptor(LostFoundPeDemotList.this, lostFoundPetList);
                            eLView.setAdapter(FoundPetsAdaptor);
                            // FoundPetsAdaptor = new lostFoundPetsAdaptor(LostFoundPetList.this, lostFoundPetList);
                            //listView.setAdapter(FoundPetsAdaptor);

                            // ((RadioButton) radioGroup.findViewById(R.id.radioButton2)).setChecked(true);

                        } else {
                            Toast.makeText(LostFoundPeDemotList.this, "No Record found", Toast.LENGTH_SHORT).show();
                            loadmore.setVisibility(View.GONE);
                            lostFoundPetList = new ArrayList<>();
                            FoundPetsAdaptor = new lostFoundPetsAdaptor(LostFoundPeDemotList.this, lostFoundPetList);
                            eLView.setAdapter(FoundPetsAdaptor);
                            Toast.makeText(LostFoundPeDemotList.this, "No record found.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(jsonObjReq, "jreq");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //   get list of pets according to buy or sale
    public void getAllPetsLostAndFound(int page_no) {
        progress.show();
        try {
            String url = "http://www.designer321.com/johnpride/pawpal/mobile/get-pet-lost-found.php?page=" + page_no;

            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, url,
                    null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        progress.dismiss();
                      /*  if (progress.isShowing()) {
                            progress.dismiss();
                        }*/
                        Log.i("reponce", response.toString());

                        if (response.has("flag")) {
                            System.out.println("response123" + response);

                            lostFoundPetList = new ArrayList<>();
                            JSONArray jsonArray = response.getJSONArray("response");

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                lostFoundPetList.add(new lostFoundPetListModel(jsonObject.getString("lost_found_id"), jsonObject.getString("title"), jsonObject.getString("name"),
                                        jsonObject.getString("age_range"), jsonObject.getString("gender"), jsonObject.getString("type_of_animal"),
                                        jsonObject.getString("breed_type"), jsonObject.getString("report_type"), jsonObject.getString("description"),
                                        jsonObject.getString("location"), jsonObject.getString("state"), jsonObject.getString("image"),
                                        jsonObject.getString("reporter_name"), jsonObject.getString("reporter_email"), jsonObject.getString("reporter_contact"),
                                        jsonObject.getString("report_date_time"), jsonObject.getString("address"), jsonObject.getString("latitude"), jsonObject.getString("longitude"), jsonObject.getString("country"), jsonObject.getString("zip")));
                                System.out.println("Email..." + jsonObject.getString("reporter_email"));
                            }
                            FoundPetsAdaptor = new lostFoundPetsAdaptor(LostFoundPeDemotList.this, creatingItems(mult));
                            eLView.setAdapter(FoundPetsAdaptor);
                            // listView.setAdapter(FoundPetsAdaptor);
                            flag = flag + 1;
                            // ((RadioButton) radioGroup.findViewById(R.id.radioButton2)).setChecked(true);

                        } else {
                            Toast.makeText(LostFoundPeDemotList.this, "No Record found", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(jsonObjReq, "jreq");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getAllPetsLostAndFoundNew() {
        progress.show();
        try {
            String url = WebURL.GET_LOSTFOUND_PAYMENTDONE_LIST;

            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, url,
                    null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        progress.dismiss();
                      /*  if (progress.isShowing()) {
                            progress.dismiss();
                        }*/
                        Log.i("reponce", response.toString());

                        if (response.has("flag")) {
                            System.out.println("response123" + response);

                            lostFoundPetList = new ArrayList<>();
                            JSONArray jsonArray = response.getJSONArray("response");

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                lostFoundPetList.add(new lostFoundPetListModel(jsonObject.getString("lost_found_id"), jsonObject.getString("title"), jsonObject.getString("name"),
                                        jsonObject.getString("age_range"), jsonObject.getString("gender"), jsonObject.getString("type_of_animal"),
                                        jsonObject.getString("breed_type"), jsonObject.getString("report_type"), jsonObject.getString("description"),
                                        jsonObject.getString("location"), jsonObject.getString("state"), jsonObject.getString("image"),
                                        jsonObject.getString("reporter_name"), jsonObject.getString("reporter_email"), jsonObject.getString("reporter_contact"),
                                        jsonObject.getString("report_date_time"), jsonObject.getString("address"), jsonObject.getString("latitude"), jsonObject.getString("longitude"), jsonObject.getString("country"), jsonObject.getString("zip")));
                                System.out.println("Email..." + jsonObject.getString("reporter_email"));
                            }
                            FoundPetsAdaptor = new lostFoundPetsAdaptor(LostFoundPeDemotList.this, creatingItems(mult));
                            eLView.setAdapter(FoundPetsAdaptor);
                            // listView.setAdapter(FoundPetsAdaptor);
                            flag = flag + 1;
                            // ((RadioButton) radioGroup.findViewById(R.id.radioButton2)).setChecked(true);

                        } else {
                            Toast.makeText(LostFoundPeDemotList.this, "No Record found", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(jsonObjReq, "jreq");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void next_record_service(int page_no) {
        progress.show();
        try {
            String url = "http://www.designer321.com/johnpride/pawpal/mobile/get-pet-lost-found.php?page=" + page_no;
            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, url,
                    null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        progress.dismiss();
                      /*  if (progress.isShowing()) {
                            progress.dismiss();
                        }*/
                        int flag = response.getInt("flag");

                        if (flag == 1) {
                            System.out.println("response123" + response);

                            myList = new ArrayList<>();
                            JSONArray jsonArray = response.getJSONArray("response");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                myList.add(new lostFoundPetListModel(jsonObject.getString("lost_found_id"), jsonObject.getString("title"), jsonObject.getString("name"),
                                        jsonObject.getString("age_range"), jsonObject.getString("gender"), jsonObject.getString("type_of_animal"),
                                        jsonObject.getString("breed_type"), jsonObject.getString("report_type"), jsonObject.getString("description"),
                                        jsonObject.getString("location"), jsonObject.getString("state"), jsonObject.getString("image"),
                                        jsonObject.getString("reporter_name"), jsonObject.getString("reporter_email"), jsonObject.getString("reporter_contact"),
                                        jsonObject.getString("report_date_time"), jsonObject.getString("address"), jsonObject.getString("latitude"), jsonObject.getString("longitude"), jsonObject.getString("country"), jsonObject.getString("zip")));
                                lostFoundPetList.add(myList.get(i));
                            }
                          /*  pet_list_adaptor = new Pet_list_Adaptor(PetListActivity.this, pets_list);
                            //	note : this should come next to loading view
                            eLView.setAdapter(pet_list_adaptor);*/
                            FoundPetsAdaptor.notifyDataSetChanged();
                            flag = flag + 1;
                            Log.i("page_n0=", "" + flag);
                          /*  pet_list_adaptor = new Pet_list_Adaptor(PetListActivity.this, creatingItems(mult));
                            //	note : this should come next to loading view
                            eLView.setAdapter(pet_list_adaptor);
                            //  listView2.setAdapter(pet_list_adaptor);
                            //  listView.setVisibility(View.VISIBLE);*/

                        } else {
                            Toast.makeText(LostFoundPeDemotList.this, "No more Record found", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(jsonObjReq, "jreq");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //	method to supply fake items to adapter
    private ArrayList<lostFoundPetListModel> creatingItems(int mult) {

        myList = new ArrayList<>();
        int cnt = (lostFoundPetList.size() < 5) ? lostFoundPetList.size() : 5;
        for (int i = 0; i < cnt; i++) {
            int total = mult + i;
            if (lostFoundPetList.size() < total) {

            } else {
                myList.add(lostFoundPetList.get(total - 1));
            }

        }

        return myList;
    }

    public void popupwindow(View view) {


        PopupMenu popup = new PopupMenu(LostFoundPeDemotList.this, view);
        //Inflating the Popup using xml file
        popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                //Toast.makeText(LostFoundPetList.this, "You Clicked : " + item.getTitle(), Toast.LENGTH_SHORT).show();
                System.out.println("Clicked " + item.getTitle().toString());


                if (search_type.equals("")) {
                    search_type = "Lost";
                }

                if (item.getTitle().toString().equals("Sort date by Ascending")) {

                    getAllPetsAscendingorDescending("1", search_type);
                } else {

                    getAllPetsAscendingorDescending("2", search_type);

                }
                return true;
            }
        });


        popup.show();//showing popup menu
    }

   /* @Override
    public void loadData() {
        mult += 5;

        //	new data loader
        new FakeLoader().execute();

    }*/

    public void getAllPetsAscendingorDescending(String sortvalue, String type) {
        progress.show();
        try {
            String url = WebURL.GETLOSTFOUND_ASCDESC + sortvalue + "&type=";

            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, url,
                    null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    progress.dismiss();
                    try {
                      /*  if (progress.isShowing()) {
                            progress.dismiss();
                        }*/
                        Log.i("reponce", response.toString());

                        if (response.has("flag")) {
                            System.out.println("response123" + response);

                            lostFoundPetList = new ArrayList<>();
                            JSONArray jsonArray = response.getJSONArray("response");

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                lostFoundPetList.add(new lostFoundPetListModel(jsonObject.getString("lost_found_id"), jsonObject.getString("title"), jsonObject.getString("name"),
                                        jsonObject.getString("age_range"), jsonObject.getString("gender"), jsonObject.getString("type_of_animal"),
                                        jsonObject.getString("breed_type"), jsonObject.getString("report_type"), jsonObject.getString("description"),
                                        jsonObject.getString("location"), jsonObject.getString("state"), jsonObject.getString("image"),
                                        jsonObject.getString("reporter_name"), jsonObject.getString("reporter_email"), jsonObject.getString("reporter_contact"),
                                        jsonObject.getString("report_date_time"), jsonObject.getString("address"), jsonObject.getString("latitude"), jsonObject.getString("longitude"), jsonObject.getString("country"), jsonObject.getString("zip")));
                            }
                            FoundPetsAdaptor = new lostFoundPetsAdaptor(LostFoundPeDemotList.this, creatingItems(mult));
                            eLView.setAdapter(FoundPetsAdaptor);
                            // listView.setAdapter(FoundPetsAdaptor);
                            flag = flag + 1;
                            // ((RadioButton) radioGroup.findViewById(R.id.radioButton2)).setChecked(true);

                        } else {
                            Toast.makeText(LostFoundPeDemotList.this, "No Record found", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(jsonObjReq, "jreq");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {

        Intent intent = new Intent(LostFoundPeDemotList.this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }

    public void popupwindow1(View view) {


        PopupMenu popup = new PopupMenu(LostFoundPeDemotList.this, view);

        popup.getMenuInflater().inflate(R.menu.popup_mymenu, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getTitle().toString().equals("Logout")) {
                    SharedPreferences pref;
                    SharedPreferences.Editor editor;
                    pref = getApplication().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
                    editor = pref.edit();
                    editor.clear();
                    editor.commit();
                    editor.apply();
                    Intent intent1 = new Intent(getApplicationContext(), LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent1.putExtra("key", "ReportAPet");
                    finish();
                    startActivity(intent1);

                } else if (item.getTitle().toString().equals("My Listing")) {
                    Intent intent = new Intent(LostFoundPeDemotList.this, MyPetListLostFoundActivity.class);
                    startActivity(intent);
                } else if (item.getTitle().toString().equals("Favourite")) {
                    Intent intent = new Intent(LostFoundPeDemotList.this, ViewPagerActivity.class);
                    startActivity(intent);
                }
                return true;
            }
        });


        popup.show();//showing popup menu
    }

    public void fetchLocationData() {
        // check if myLocationListener enabled
        if (myLocationListener.canGetLocation()) {
            latitude = myLocationListener.getLatitude();
            longitude = myLocationListener.getLongitude();

            // Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
        } else {
            myLocationListener.showSettingsAlert();
            latitude = myLocationListener.getLatitude();
            longitude = myLocationListener.getLongitude();

            // Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();

        }

        Geocoder geocoder = new Geocoder(LostFoundPeDemotList.this, Locale.getDefault());
        List<Address> addresses = null;
        StringBuilder result = new StringBuilder();

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                //  result.append(address.getAddressLine(0)).append(" ");
                result.append(address.getAddressLine(1)).append(" ");
                // result.append(address.getAddressLine(2)).append("");
                //result.append(address.getAddressLine(3)).append(" ");
                //   Toast.makeText(getApplicationContext(), address.toString(), Toast.LENGTH_LONG).show();
                System.out.println("Address" + address.toString());
                System.out.println("Address...cit" + addresses.get(0).getLocality());
                city = addresses.get(0).getLocality();
                getDataByCityLocation(addresses.get(0).getLocality(), 1);


            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {

            case PERMISSION_REQUEST_CODE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    fetchLocationData();

                } else {

                    Toast.makeText(getApplicationContext(), "Permission Denied, You cannot access location data.", Toast.LENGTH_LONG).show();

                }
                break;

        }
    }

    public void getDataByCityLocation(final String city1, int count) {

        counttoatal = count;
        System.out.println("loctionCity.." + city1);
        progress.show();
        try {
            String url = WebURL.SEARCH_LOSTFOUND_CITYNEAR_PAGE + "keyword=" + city1 + "&currentpage=" + counttoatal;
            System.out.println("URL...." + url);
            position_to_scroll = Constants.pets_list1.size();

            System.out.println("CityUrl.." + url);


            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, url,
                    null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {

                        System.out.println("CityLoctionlist" + response);
                        if (progress.isShowing()) {
                            progress.dismiss();
                        }
                        int flag = response.getInt("flag");
                        int totalcount = response.getInt("totalpages");
                        if (totalcount == counttoatal) {
                            loadmore.setVisibility(View.GONE);
                        } else {
                            loadmore.setVisibility(View.VISIBLE);
                        }
                        if (flag == 1) {

                            JSONArray jsonArray = response.getJSONArray("response");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                Constants.pets_list1.add(new lostFoundPetListModel(jsonObject.getString("lost_found_id"), jsonObject.getString("title"), jsonObject.getString("name"),
                                        jsonObject.getString("age_range"), jsonObject.getString("gender"), jsonObject.getString("type_of_animal"),
                                        jsonObject.getString("breed_type"), jsonObject.getString("report_type"), jsonObject.getString("description"),
                                        jsonObject.getString("location"), jsonObject.getString("state"), jsonObject.getString("image"),
                                        jsonObject.getString("reporter_name"), jsonObject.getString("reporter_email"), jsonObject.getString("reporter_contact"),
                                        jsonObject.getString("report_date_time"), jsonObject.getString("address"), jsonObject.getString("latitude"), jsonObject.getString("longitude"), jsonObject.getString("country"), jsonObject.getString("zip")));

                            }

                            FoundPetsAdaptor = new lostFoundPetsAdaptor(LostFoundPeDemotList.this, Constants.pets_list1);
                            eLView.setAdapter(FoundPetsAdaptor);
                            System.out.println("position_to_scroll" + position_to_scroll);
                            eLView.setAdapter(FoundPetsAdaptor);
                            eLView.setSelection(position_to_scroll - 1);
                            eLView.smoothScrollToPosition(position_to_scroll - 1);
//                                eLView.smoothScrollToPosition(10);
                            counttoatal = counttoatal + 1;


                            loadmore.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View arg0) {

                                    //   Toast.makeText(PetListActivity.this, ""+ eLView.getLastVisiblePosition(), Toast.LENGTH_SHORT).show();
                                    // getAll_Pets_service(eLView.getLastVisiblePosition() + 10);
                                    getDataByCityLocation(city1, counttoatal);
                                }
                            });
                            // ((RadioButton) radioGroup.findViewById(R.id.radioButton2)).setChecked(true);

                        } else {
                            Toast.makeText(LostFoundPeDemotList.this, "Record not found.", Toast.LENGTH_SHORT).show();
                            loadmore.setVisibility(View.GONE);
                            Constants.pets_list1 = new ArrayList<>();
                            FoundPetsAdaptor = new lostFoundPetsAdaptor(LostFoundPeDemotList.this, Constants.pets_list1);
                            eLView.setAdapter(FoundPetsAdaptor);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(jsonObjReq, "jreq");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void getDataByCityLocationEnter(final String city, int count) {
        counttotalKeyword = count;
        position_to_scrollText = Constants.pets_animalText1.size();
        System.out.println("loctionCity.." + city);
        progress.show();
        try {
            String url = WebURL.SEARCH_LOSTFOUND_CITYONLY_PAGE + "keyword=" + city + "&currentpage=" + counttotalKeyword;
            ;

            System.out.println("CityUrl.." + url);


            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, url,
                    null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {

                        System.out.println("CityLoctionlist" + response);
                        if (progress.isShowing()) {
                            progress.dismiss();
                        }
                        int flag = response.getInt("flag");

                        if (flag == 1) {
                            int totalcount = response.getInt("totalpages");
                            if (totalcount == counttotalKeyword) {
                                loadmore.setVisibility(View.GONE);
                            } else {
                                loadmore.setVisibility(View.VISIBLE);
                            }
                            JSONArray jsonArray = response.getJSONArray("response");
                            myList1 = new ArrayList<>();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                Constants.pets_animalText1.add(new lostFoundPetListModel(jsonObject.getString("lost_found_id"), jsonObject.getString("title"), jsonObject.getString("name"),
                                        jsonObject.getString("age_range"), jsonObject.getString("gender"), jsonObject.getString("type_of_animal"),
                                        jsonObject.getString("breed_type"), jsonObject.getString("report_type"), jsonObject.getString("description"),
                                        jsonObject.getString("location"), jsonObject.getString("state"), jsonObject.getString("image"),
                                        jsonObject.getString("reporter_name"), jsonObject.getString("reporter_email"), jsonObject.getString("reporter_contact"),
                                        jsonObject.getString("report_date_time"), jsonObject.getString("address"), jsonObject.getString("latitude"), jsonObject.getString("longitude"), jsonObject.getString("country"), jsonObject.getString("zip")));
                                // pets_listText.add(myList1.get(i));
                            }
                            FoundPetsAdaptor = new lostFoundPetsAdaptor(LostFoundPeDemotList.this, Constants.pets_animalText1);
                            eLView.setAdapter(FoundPetsAdaptor);
                            System.out.println("position_to_scroll" + position_to_scroll);
                            eLView.setAdapter(FoundPetsAdaptor);
                            eLView.setSelection(position_to_scroll - 1);
                            eLView.smoothScrollToPosition(position_to_scroll - 1);

                            counttotalKeyword = counttotalKeyword + 1;

                            loadmore.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View arg0) {

                                    getDataByCityLocationEnter(city, counttotalKeyword);
                                }
                            });

                            // ((RadioButton) radioGroup.findViewById(R.id.radioButton2)).setChecked(true);

                        } else {
                            Toast.makeText(LostFoundPeDemotList.this, "Record not found.", Toast.LENGTH_SHORT).show();
                            loadmore.setVisibility(View.GONE);
                            Constants.pets_animalText1 = new ArrayList<>();
                            FoundPetsAdaptor = new lostFoundPetsAdaptor(LostFoundPeDemotList.this, Constants.pets_animalText1);
                            eLView.setAdapter(FoundPetsAdaptor);
                            Toast.makeText(LostFoundPeDemotList.this, "No record found.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(jsonObjReq, "jreq");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    public void popupwindow2(View view) {


        PopupMenu popup = new PopupMenu(LostFoundPeDemotList.this, view);

        popup.getMenuInflater().inflate(R.menu.popup_mymenuone, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getTitle().toString().equals("Login")) {
                    Intent intent = new Intent(LostFoundPeDemotList.this, LoginActivity.class);
                    intent.putExtra("key", "ReportAPet");
                    startActivity(intent);

                }
                return true;
            }
        });


        popup.show();//showing popup menu
    }

    private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {

            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    //    Toast.makeText(PetListActivity.this, "" + locationAddress.toString(), Toast.LENGTH_SHORT).show();
                    //  getlostFoundPetsListFromCurrentLocation(locationAddress.toString());
                    break;
                default:
                    locationAddress = null;
            }

        }
    }

    //	background thread - use this class inside fragment to avoid any orientation changes
    private class FakeLoader extends AsyncTask<Void, Void, ArrayList<lostFoundPetListModel>> {

        @Override
        protected ArrayList<lostFoundPetListModel> doInBackground(Void... params) {

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return creatingItems(mult);
        }

       /* @Override
        protected void onPostExecute(ArrayList<lostFoundPetListModel> result) {
            super.onPostExecute(result);

            eLView.addNewData(result);
        }*/
    }
}
