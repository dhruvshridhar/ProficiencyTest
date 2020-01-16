package in.blogspot.tecnopandit.proficiencytest;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


//NOTE:
//I have not implemented lazy downloading.
//All code is written by me from scratch.
//I dont know much about caching!!
//Many image links were broken or not mentioned so I have replaced them with error images.
//I know code is cluttered.
//Its a simple app that just do the given task.
//You can see my other projects at my github: https://github.com/dhruvshridhar
//THANK YOU :)


public class MainActivity extends AppCompatActivity {
    ListView listView;
    String result;
    Downloader downloader;
    Imagedownload imagedownload;
    List<String> titles = new ArrayList<>();
    List<String> descriptions = new ArrayList<>();
    List<String> images = new ArrayList<>();
    List<Map<String, Object>> listcontent = new ArrayList<>();
    String link = "https://dl.dropboxusercontent.com/s/2iodh4vg0eortkl/facts.json";


    //Creating new AsyncTask for downloading images.

    public class Imagedownload extends AsyncTask<String, Void, List<Bitmap>> {

        @Override
        protected List<Bitmap> doInBackground(String... strings) {
            List<Bitmap> result = new ArrayList<>();

            for (int i = 0; i < strings.length; i++) {
                try {
                    URL url = new URL(strings[i]);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.connect();
                    InputStream inputStream = httpURLConnection.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    result.add(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                    Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.error);
                    result.add(bm);
                }
            }
            return result;
        }
    }


// Another AsyncTask for getting JSON feed.

    public class Downloader extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            try {
                result = "";
                URL url = new URL(strings[0]);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.connect();
                InputStream inputStream = httpURLConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);
                int data = reader.read();
                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }
                return result;
            } catch (Exception e) {
                return e.toString();

            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView=findViewById(R.id.listview);
        getdata(); //As name specifies for getting data
    }

    public void getdata() {
        downloader = new Downloader();
        imagedownload = new Imagedownload();

        try {
            String datagot = downloader.execute(link).get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // JSON Parsing starts here:

        try {
            JSONObject jsonObject = new JSONObject(result);
            setTitle(jsonObject.get("title").toString());
            JSONArray x = jsonObject.getJSONArray("rows");
            for (int i = 0; i < x.length(); i++) {
                titles.add(x.getJSONObject(i).getString("title"));
                descriptions.add(x.getJSONObject(i).getString("description"));
                images.add(x.getJSONObject(i).getString("imageHref"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Weird stuff going on

        String[] im = new String[images.size()];
        for (int i = 0; i < images.size(); i++) {
            im[i] = images.get(i);
        }
        List<Bitmap> bitary = new ArrayList<>();
        try {
            bitary = imagedownload.execute(im).get();

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Making maps to add in List

        for (int i = 0; i < bitary.size(); i++) {
            Map<String, Object> temp = new HashMap<>();
            temp.put("title", titles.get(i));
            temp.put("desc", descriptions.get(i));
            temp.put("img", bitary.get(i));
            listcontent.add(temp);
        }

        // Making simple adapter and custom view binder

        SimpleAdapter simpleAdapter = new SimpleAdapter(getApplicationContext(), listcontent, R.layout.customlist, new String[]{"title", "desc", "img"}, new int[]{R.id.txt, R.id.cur, R.id.flag});
        SimpleAdapter.ViewBinder viewBinder = new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                if (view.getId() == R.id.flag) {
                    ((ImageView) view).setImageBitmap((Bitmap) data);
                    return true;
                }
                return false;
            }
        };
        simpleAdapter.setViewBinder(viewBinder);
        listView.setAdapter(simpleAdapter);
    }
    // Bravo!!! You made it till here!!
}
