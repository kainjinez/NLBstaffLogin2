package victory1908.nlbstafflogin2.ManageBeaconFragment;


import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import victory1908.nlbstafflogin2.Config;
import victory1908.nlbstafflogin2.R;
import victory1908.nlbstafflogin2.beaconstac.Beacon;
import victory1908.nlbstafflogin2.beaconstac.BeaconAdapterDetail;

/**
 * A simple {@link Fragment} subclass.
 */
public class EditRegisteredBeacon extends Fragment implements SwipeRefreshLayout.OnRefreshListener {


    public EditRegisteredBeacon() {
        // Required empty public constructor
    }

    RequestQueue requestQueue;

    //JSON Array
    private JSONArray beaconArray;

    private List<Beacon> listBeacons,temListBeacon;

    //Creating Views
    private RecyclerView beaconRecyclerView;
    private RecyclerView.LayoutManager layoutMangerBeacon;
    private RecyclerView.Adapter beaconAdapter;

    SwipeRefreshLayout swipeRefreshLayout;


    ProgressBar progressBar;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View viewFragment = inflater.inflate(R.layout.fragment_edit_registered_beacon, container, false);

        progressBar = (ProgressBar)viewFragment.findViewById(R.id.progressBar);
        beaconRecyclerView = (RecyclerView)viewFragment.findViewById(R.id.beaconList);
        swipeRefreshLayout = (SwipeRefreshLayout)viewFragment.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);


        listBeacons = new ArrayList<>();

        beaconRecyclerView.setHasFixedSize(true);
        layoutMangerBeacon = new LinearLayoutManager(getContext());
        beaconRecyclerView.setLayoutManager(layoutMangerBeacon);

        requestQueue = Volley.newRequestQueue(getContext());

        getBeaconRespond(requestQueue);

        beaconAdapter = new BeaconAdapterDetail(getContext(),listBeacons);
        beaconRecyclerView.setAdapter(beaconAdapter);

        temListBeacon = listBeacons;


        //refresh when scroll till bottom
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            beaconRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    //IfScrolled at last then
                    if (isLastItemDisplaying(beaconRecyclerView)) {

                        getBeaconRespond(requestQueue);
                        if (!(listBeacons.equals(temListBeacon))) {
                            beaconAdapter.notifyDataSetChanged();
                            temListBeacon = listBeacons;
                        }
                    }
                }
            });
        } else {
            beaconRecyclerView.setOnScrollChangeListener(new RecyclerView.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    //IfScrolled at last then
                    if (isLastItemDisplaying(beaconRecyclerView)) {

                        getBeaconRespond(requestQueue);
                        if (!(listBeacons.equals(temListBeacon))) {
                            beaconAdapter.notifyDataSetChanged();
                            temListBeacon = listBeacons;
                        }
                    }
                }
            });
        }

        // Inflate the layout for this fragment
        return viewFragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    private void getBeaconRespond(RequestQueue requestQueue) {

        progressBar.setVisibility(View.VISIBLE);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.GET_ALL_BEACON_URL,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject respond) {
                        try {
                            beaconArray = new JSONArray();
                            beaconArray = respond.getJSONArray("result");

                            listBeacons.clear();
                            getBeaconDetail(beaconArray);
                            progressBar.setVisibility(View.GONE);
//                            Toast.makeText(getContext(),listBeacons.get(0).getBeaconSN(),Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getContext(), "Unable to fetch data event Detail: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });
        //Adding request to the queue
        requestQueue.add(jsonObjectRequest);
    }

    private void getBeaconDetail(JSONArray j) {
        listBeacons.clear();
        //Traversing through all the items in the json array
        for (int i = 0; i < j.length(); i++) {
            try {
                JSONObject json = j.getJSONObject(i);
                Beacon beacon = new Beacon();
                beacon.setBeaconName(json.getString(Config.BEACON_NAME));
                beacon.setBeaconID(json.getString(Config.BEACON_ID));
                beacon.setBeaconSN(json.getString(Config.BEACON_SN));
                beacon.setBeaconUUID(json.getString(Config.BEACON_UUID));
                beacon.setMajor(Integer.parseInt(json.getString(Config.BEACON_MAJOR)));
                beacon.setMinor(Integer.parseInt(json.getString(Config.BEACON_MINOR)));
                //Adding the event object to the list
                listBeacons.add(beacon);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        //Notifying the adapter that data has been added or changed
        beaconAdapter.notifyDataSetChanged();
    }

    private boolean isLastItemDisplaying(RecyclerView recyclerView) {
        if (recyclerView.getAdapter().getItemCount() != 0) {
            int lastVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
            if (lastVisibleItemPosition != RecyclerView.NO_POSITION && lastVisibleItemPosition == recyclerView.getAdapter().getItemCount() - 1)
                return true;
        }
        return false;
    }

    @Override
    public void onRefresh() {
        getBeaconRespond(requestQueue);
        if (!(listBeacons.equals(temListBeacon))) {
            beaconAdapter.notifyDataSetChanged();
            temListBeacon = listBeacons;
        }
        if (swipeRefreshLayout.isRefreshing())swipeRefreshLayout.setRefreshing(false);
    }
}
