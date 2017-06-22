package net.martijnvandijk.weatherclient;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * An activity representing a list of Sensors. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link SensorDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class SensorListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private ArrayList<SensorNode> sensorNodes;
    private SensorListViewAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.sensorsSwipeRefresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshNodes();
            }
        });

        sensorNodes = new ArrayList<SensorNode>();
        mRecyclerView = (RecyclerView) findViewById(R.id.sensor_list);
        assert mRecyclerView != null;
        mAdapter = new SensorListViewAdapter(sensorNodes);
        mRecyclerView.setAdapter(mAdapter);
        refreshNodes();
        mSwipeRefreshLayout.setRefreshing(true);
        if (findViewById(R.id.sensor_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }


    }

    private void refreshNodes(){
        APIClient.get("/sensorNode", null, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                sensorNodes.clear();
                for( int i = 0; i < response.length(); i++){
                    try {
                        JSONObject s = response.getJSONObject(i);
                        SensorNode n = new SensorNode(
                            s.getString("name"),
                            s.getString("sensorNodeID")
                        );
                        sensorNodes.add(n);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                mSwipeRefreshLayout.setRefreshing(false);
                mAdapter.notifyDataSetChanged();

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Snackbar.make(findViewById(R.id.sensor_list), "Error while connecting to API", Snackbar.LENGTH_LONG).show();
            }
        });
    }


    public class SensorListViewAdapter
            extends RecyclerView.Adapter<SensorListViewAdapter.ViewHolder> {

        private final List<SensorNode> mValues;

        public SensorListViewAdapter(List<SensorNode> items) {
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.sensor_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);
            //holder.mIdView.setText("1");
            holder.mContentView.setText(mValues.get(position).name);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(SensorDetailFragment.ARG_SENSOR_NODE_ID, holder.mItem.sensorNodeID);
                        arguments.putString(SensorDetailFragment.ARG_SENSOR_NODE_NAME, holder.mItem.name);

                        SensorDetailFragment fragment = new SensorDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.sensor_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, SensorDetailActivity.class);
                        intent.putExtra(SensorDetailFragment.ARG_SENSOR_NODE_NAME, holder.mItem.name);
                        intent.putExtra(SensorDetailFragment.ARG_SENSOR_NODE_ID, holder.mItem.sensorNodeID);

                        context.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            //public final TextView mIdView;
            public final TextView mContentView;
            public SensorNode mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                //mIdView = (TextView) view.findViewById(R.id.id);
                mContentView = (TextView) view.findViewById(R.id.content);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }
}
