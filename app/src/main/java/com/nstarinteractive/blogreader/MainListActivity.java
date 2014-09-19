package com.nstarinteractive.blogreader;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;


public class MainListActivity extends ListActivity {

    protected JSONObject mBlogPosts;
    public static final int NUMBER_OF_POSTS = 20;
    public static final String KEY_TITLE = "title";
    public static final String KEY_AUTHOR = "author";
    public static final String TAG = MainListActivity.class.getSimpleName();
    protected final String mBlogURL = "http://blog.teamtreehouse.com/api/get_recent_summary/?count=" + NUMBER_OF_POSTS;
    protected ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_list);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);


        //Toast.makeText(this, getString(R.string.empty_list), Toast.LENGTH_LONG);
        /*
        Resources resources = getResources();
        mDataArray = resources.getStringArray(R.array.listItems);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mDataArray);
        setListAdapter(arrayAdapter);
        */

        if (isNetworkAvailable()) {
            DownloadBlogPostTask downloadBlogPostTask = new DownloadBlogPostTask();
            downloadBlogPostTask.execute();
            mProgressBar.setVisibility(View.VISIBLE);

        }else
            Toast.makeText(this, "Network connetion is unavailable!", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        try {
            JSONArray jsonArray = mBlogPosts.getJSONArray("posts");
            JSONObject jsonObject = jsonArray.getJSONObject(position);
            String url = jsonObject.getString("url");
            Intent intent = new Intent(this, BlogDetailActivity.class); //Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private boolean isNetworkAvailable() {
        boolean networkAvailable = false;
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context .CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected())
            networkAvailable = true;

        return networkAvailable;
    }

    public void updateBlogList() {
        mProgressBar.setVisibility(View.INVISIBLE);
        if (mBlogPosts == null)
        {
            //TODO Handle Error
            updateDisplayForErrorHandling();
        }
        else
        {
            try {
                JSONArray jsonArray = mBlogPosts.getJSONArray("posts");
                ArrayList<HashMap<String, String>> blogData = new ArrayList<HashMap<String, String>>();
                for (int i = 0; i < jsonArray.length(); i++)
                {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String title = jsonObject.getString(KEY_TITLE);
                    title = Html.fromHtml(title).toString();

                    String author = jsonObject.getString(KEY_AUTHOR);
                    author = Html.fromHtml(author).toString();

                    HashMap<String, String> hMap = new HashMap<String, String>();
                    hMap.put(KEY_TITLE, title);
                    hMap.put(KEY_AUTHOR, author);
                    blogData.add(hMap);
                }

                String[] keys = { KEY_TITLE, KEY_AUTHOR };
                int[] ids = { android.R.id.text1, android.R.id.text2 };
                //ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, blogStringArray);
                SimpleAdapter arrayAdapter = new SimpleAdapter(this, blogData, android.R.layout.simple_list_item_2, keys, ids);
                setListAdapter(arrayAdapter);
            } catch (JSONException e) {
                Log.e(TAG, "JSON Exception caught: ", e);
            }
        }
    }

    private void updateDisplayForErrorHandling() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.error_dialog_title));
        builder.setMessage(getString(R.string.error_dialog_message));
        builder.setPositiveButton(android.R.string.ok, null);
        AlertDialog dialog = builder.create();
        dialog.show();
        TextView textView = (TextView) getListView().getEmptyView();
        textView.setText(getString(R.string.empty_list));
    }

    private class DownloadBlogPostTask extends AsyncTask<Object, Void, JSONObject>
    {
        @Override
        protected JSONObject doInBackground(Object[] objects) {
            int responseCode = -1;
            JSONObject jsonObject = null;
            try {
                URL url = new URL(mBlogURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.connect();
                responseCode = httpURLConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK)
                {
                    InputStream inputStream = httpURLConnection.getInputStream();
                    Reader reader = new InputStreamReader(inputStream);
                    char []charArray = new char[httpURLConnection.getContentLength()];
                    reader.read(charArray);
                    String responseData = new String(charArray);
                    Log.v(TAG, responseData);
                    jsonObject = new JSONObject(responseData);

                }
                //Log.i(TAG, "Response Code: " + responseCode);
            }
            catch (MalformedURLException e) {
                Log.e(TAG, "Malformed exception caught: ", e);
            }
            catch (IOException e) {
                Log.e(TAG, "IOException caught: ", e);
            }
            catch (Exception e) {
                Log.e(TAG, "Exception caught: ", e);
            }
            return jsonObject;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            mBlogPosts = jsonObject;
            updateBlogList();
        }
    }


}
