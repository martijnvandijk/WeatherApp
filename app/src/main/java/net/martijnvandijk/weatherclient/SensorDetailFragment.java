package net.martijnvandijk.weatherclient;

import android.app.Activity;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import cz.msebera.android.httpclient.Header;

/**
 * A fragment representing a single Sensor detail screen.
 * This fragment is either contained in a {@link SensorListActivity}
 * in two-pane mode (on tablets) or a {@link SensorDetailActivity}
 * on handsets.
 */
public class SensorDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_SENSOR_NODE_ID = "sensorNodeID";
    public static final String ARG_SENSOR_NODE_NAME = "name";

    /**
     * The dummy content this fragment is presenting.
     */
    private String sensorNodeId;
    private String sensorName;


    private GraphView temperatureGraph;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SensorDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_SENSOR_NODE_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.

            sensorName = getArguments().getString(ARG_SENSOR_NODE_NAME);
            sensorNodeId = getArguments().getString(ARG_SENSOR_NODE_ID);
            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(sensorName);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.sensor_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (sensorNodeId != null) {
            ((TextView) rootView.findViewById(R.id.sensor_detail)).setText(sensorNodeId);
        }
        if (sensorName != null){

        }

        temperatureGraph = (GraphView) rootView.findViewById(R.id.sensor_detail_temperature);
        temperatureGraph.getViewport().setScrollable(true);
        temperatureGraph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getActivity()));
        temperatureGraph.getGridLabelRenderer().setNumHorizontalLabels(3); // only 4 because of the space
        refreshData();
        return rootView;
    }

    private void refreshData(){
        APIClient.get("/measurement/sensorNode/" + sensorNodeId, null, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                ArrayList<DataPoint> dataPoints = new ArrayList<>();

                for( int i = 0; i < response.length(); i++){
                    try {
                        JSONObject s = response.getJSONObject(i);
                        if(s.getString("measurementType").equals("temperature")) {

                            Date date = ISO8601.toCalendar(s.getString("timestamp")).getTime();
                            Double temperature = s.getDouble("value");
                            DataPoint p = new DataPoint(date, temperature);
                            dataPoints.add(p);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                DataPoint[] dps = dataPoints.toArray(new DataPoint[dataPoints.size()]);
                LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(dps);
                temperatureGraph.addSeries(series);


            }

//            @Override
//            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
////                Snackbar.make(findViewById(R.id.sensor_list), "Error while connecting to API", Snackbar.LENGTH_LONG).show();
//            }
        });
    }
}
