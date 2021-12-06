package com.example.weatherq;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    String Key="cf70f1ffd0f3af2781497633430a5463";
    TextView CityText,WdText,SdText,FlText,AqiText;
    Button button;
    EditText SearchText;
    RecyclerView recyclerView;
    List<ItemDataList> dataList=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InitializeView();

    }

    private String PinUrl(String city)
    {

        String url="http://apis.juhe.cn/simpleWeather/query?city="+city+"&key="+Key;
        return url;
    }
    private void InitializeView()
    {
        CityText=findViewById(R.id.Title_Text);
        WdText=findViewById(R.id.WdText);
        SdText=findViewById(R.id.SdText);
        FlText=findViewById(R.id.FlText);
        AqiText=findViewById(R.id.AqiText);
        button=findViewById(R.id.ScButton);
        button.setOnClickListener(SearchOnClick);
        SearchText=findViewById(R.id.ScEditText);
        recyclerView=findViewById(R.id.RecyclerView);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(linearLayoutManager);
    }
    private View.OnClickListener SearchOnClick=new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            getDatasync(PinUrl( SearchText.getText().toString()));
        }
    };
    private Handler UiHandler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what)
            {
                case 100:

                    DataBan((JsonX) message.obj);
                    break;

            }

            return false;
        }
    });

    private void DataBan(JsonX json)
    {

        if (json.getError_code()!=0){ Toast.makeText(this,json.getReason(),Toast.LENGTH_LONG).show();  return;}
        JsonX.Result result=json.getResult();
        CityText.setText(result.getCity());
        JsonX.Realtime realtime=result.getRealtime();
        WdText.setText("温度:"+realtime.getTemperature()+"°");
        SdText.setText("湿度:"+realtime.getHumidity()+"%");
        FlText.setText("风力:"+realtime.getPower());
        AqiText.setText("AQI:"+realtime.getAqi());
        List<JsonX.Future> futures=result.getFuture();
        for (int i=0; i<futures.size();i++)
        {
            JsonX.Future future=futures.get(i);
            int image=R.drawable.weather_s;
            JsonX.Wid wid= futures.get(i).getWid();
            if (wid.getDay().equals("00")){image=R.drawable.weather_t;}
            if (wid.getDay().equals("01")){image=R.drawable.weather_un;}
            if (wid.getDay().equals("02")){image=R.drawable.weather_un;}
            if (wid.getDay().equals("03")||wid.getDay().equals("04")){image=R.drawable.weather_y;}
            if (wid.getDay().equals("07")||wid.getDay().equals("05")){image=R.drawable.weather_y;}
            if (wid.getDay().equals("08")||wid.getDay().equals("09")){image=R.drawable.weather_y;}
            if (wid.getDay().equals("10")||wid.getDay().equals("11")){image=R.drawable.weather_y;}
            if (wid.getDay().equals("12")||wid.getDay().equals("13")){image=R.drawable.weather_y;}

            dataList.add(new ItemDataList(image,future.getTemperature(),future.getDate(),future.getWeather()));

        }
        recyclerView.setAdapter(new DiRecycleApadpter(this,dataList));

    }


    public void getDatasync(final String url){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();//创建OkHttpClient对象
                    Request request = new Request.Builder()
                            .url(url)//请求接口。如果需要传参拼接到接口后面。
                            .build();//创建Request 对象
                    Response response = null;
                    response = client.newCall(request).execute();//得到Response 对象
                    if (response.isSuccessful()) {
                        Gson gson = new Gson();
                        Log.d("kwwl","response.code()=="+response.code());

                        String str= response.body().string();
                        Message message=new Message();
                        message.what=100;

                        JsonX data = gson.fromJson(str, JsonX.class);


                        message.obj=data;
                        UiHandler.sendMessage(message);
                        Log.d("kwwl","我的=="+str);
                        //此时的代码执行在子线程，修改UI的操作请使用handler跳转到UI线程。
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


}


class ItemDataList
{
    public int ImageId;
    public String Temperature;
    public String Data;
    public String QkText;

    public ItemDataList(int im,String te,String data ,String qkText){
        ImageId=im;
        Temperature=te;
        Data=data;
        QkText=qkText;

    }

}
class DiRecycleApadpter extends RecyclerView.Adapter<DiRecycleApadpter.MyViewHolder>{
    private Context context;//适配器布局
    private List<ItemDataList> lists;
    private View infater;
    public DiRecycleApadpter(Context context,List<ItemDataList> datalists){
        this.context=context;
        this.lists=datalists;

    }  
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        infater= LayoutInflater.from(context).inflate(R.layout.recycler_item,parent,false);
        MyViewHolder myViewHolder=new MyViewHolder(infater);


        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.imageView.setImageResource(lists.get(position).ImageId);
        holder.WdText.setText(holder.WdText.getText()+ lists.get(position).Temperature);
        holder.DataText.setText(lists.get(position).Data);
        holder.QkText.setText(lists.get(position).QkText);
    }

    @Override
    public int getItemCount() {
        return lists.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView WdText;
        TextView DataText;
        TextView QkText;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView=itemView.findViewById(R.id.Title_image);
            WdText=itemView.findViewById(R.id.WdText);
            DataText=itemView.findViewById(R.id.DataText);
            QkText=itemView.findViewById(R.id.qktext);
        }
    }
}
class JsonX {
    private String reason;
    private Result result;
    private int error_code;
    public void setReason(String reason) {
        this.reason = reason;
    }
    public String getReason() {
        return reason;
    }

    public void setResult(Result result) {
        this.result = result;
    }
    public Result getResult() {
        return result;
    }

    public void setError_code(int error_code) {
        this.error_code = error_code;
    }
    public int getError_code() {
        return error_code;
    }
    public class Wid {

        private String day;
        private String night;
        public void setDay(String day) {
            this.day = day;
        }
        public String getDay() {
            return day;
        }

        public void setNight(String night) {
            this.night = night;
        }
        public String getNight() {
            return night;
        }

    }
    public class Result {

        private String city;
        private Realtime realtime;
        private List<Future> future;
        public void setCity(String city) {
            this.city = city;
        }
        public String getCity() {
            return city;
        }

        public void setRealtime(Realtime realtime) {
            this.realtime = realtime;
        }
        public Realtime getRealtime() {
            return realtime;
        }

        public void setFuture(List<Future> future) {
            this.future = future;
        }
        public List<Future> getFuture() {
            return future;
        }

    }
    public class Future {

        private String date;
        private String temperature;
        private String weather;
        private Wid wid;
        private String direct;
        public void setDate(String date) {
            this.date = date;
        }
        public String getDate() {
            return date;
        }

        public void setTemperature(String temperature) {
            this.temperature = temperature;
        }
        public String getTemperature() {
            return temperature;
        }

        public void setWeather(String weather) {
            this.weather = weather;
        }
        public String getWeather() {
            return weather;
        }

        public void setWid(Wid wid) {
            this.wid = wid;
        }
        public Wid getWid() {
            return wid;
        }

        public void setDirect(String direct) {
            this.direct = direct;
        }
        public String getDirect() {
            return direct;
        }

    }
    public class Realtime {

        private String temperature;
        private String humidity;
        private String info;
        private String wid;
        private String direct;
        private String power;
        private String aqi;
        public void setTemperature(String temperature) {
            this.temperature = temperature;
        }
        public String getTemperature() {
            return temperature;
        }

        public void setHumidity(String humidity) {
            this.humidity = humidity;
        }
        public String getHumidity() {
            return humidity;
        }

        public void setInfo(String info) {
            this.info = info;
        }
        public String getInfo() {
            return info;
        }

        public void setWid(String wid) {
            this.wid = wid;
        }
        public String getWid() {
            return wid;
        }

        public void setDirect(String direct) {
            this.direct = direct;
        }
        public String getDirect() {
            return direct;
        }

        public void setPower(String power) {
            this.power = power;
        }
        public String getPower() {
            return power;
        }

        public void setAqi(String aqi) {
            this.aqi = aqi;
        }
        public String getAqi() {
            return aqi;
        }

    }
}
