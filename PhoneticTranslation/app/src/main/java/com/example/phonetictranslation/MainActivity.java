package com.example.phonetictranslation;

import android.Manifest;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Handler;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private String Source_Language = "简体中文";
    private String Target_Language = "English";
    private String Sound_Source = "common female voice";
    private String Access_token="24.52c02409824c18185bc010857406ea71.2592000.1555470322.282335-15779455";
    private String RecognizeResult =null;
    private String TranslateResult = null;

    private ImageView mIvPressToSay;
    private ImageView mIvPlay;
    private TextView mTvTxt;
    private TextView textView_loading;

    private static final int mAudioSource = MediaRecorder.AudioSource.MIC;
    //指定采样率 （MediaRecoder 的采样率通常是8000Hz AAC的通常是44100Hz。 设置采样率为44100，目前为常用的采样率，官方文档表示这个值可以兼容所有的设置）
    private static final int mSampleRateInHz=16000;
    //指定捕获音频的声道数目。在AudioFormat类中指定用于此的常量
    private static final int mChannelConfig= AudioFormat.CHANNEL_CONFIGURATION_MONO; //单声道
    //指定音频量化位数 ,在AudioFormaat类中指定了以下各种可能的常量。通常我们选择ENCODING_PCM_16BIT和ENCODING_PCM_8BIT PCM代表的是脉冲编码调制，它实际上是原始音频样本。
    //因此可以设置每个样本的分辨率为16位或者8位，16位将占用更多的空间和处理能力,表示的音频也更加接近真实。
    private static final int mAudioFormat=AudioFormat.ENCODING_PCM_16BIT;
    //指定缓冲区大小。调用AudioRecord类的getMinBufferSize方法可以获得。
    private int mBufferSizeInBytes= AudioRecord.getMinBufferSize(mSampleRateInHz,mChannelConfig, mAudioFormat);//计算最小缓冲区
    //创建AudioRecord。AudioRecord类实际上不会保存捕获的音频，因此需要手动创建文件并保存下载。
    private AudioRecord mAudioRecord = null;//创建AudioRecorder对象

    private MediaPlayer mPlayer = null;

    private File mAudioFile;
    private File mAudioResultFile;

    private Handler mMainThreadHandler;

    private boolean isRecording;
    private boolean displayFlag=false;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setCustomView(R.layout.actionbar_main);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        mIvPressToSay = (ImageView) findViewById(R.id.imageView);
        mIvPlay = (ImageView) findViewById(R.id.imageView2);
        textView_loading = (TextView)findViewById(R.id.textView_loading);
        mMainThreadHandler = new Handler(Looper.getMainLooper());

        requestAllPower();

        mIvPressToSay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                //根据不同touch action，执行不同逻辑
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.d(TAG, "按下: ");
                        startRecord();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        Log.d(TAG, "抬起: ");
                        stopRecord();
                        textView_loading.setVisibility(View.VISIBLE);
                        sendFileToBaidu(Source_Language);
                        break;
                    default:
                        break;
                }
                //处理了touch事件返回true
                return true;
            }
        });

        mIvPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_play();
            }
        });

    }

    public void requestAllPower() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED||ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED||ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO}, 1);
            }
        }
    }

    public void Set_click(View view) {
        Intent intent = new Intent(MainActivity.this, SetActivity.class);
        intent.putExtra("Source_Language", Source_Language);
        intent.putExtra("Target_Language", Target_Language);
        intent.putExtra("Sound_Source", Sound_Source);
        //这里也需要传值回来
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    Source_Language = data.getStringExtra("Source_Language");
                    Target_Language = data.getStringExtra("Target_Language");
                    Sound_Source = data.getStringExtra("Sound_Source");
                }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //activity销毁时，停止后台任务，避免内存泄漏
        stopRecord();
    }

    /** Start Record*/
    private void startRecord() {
        //改变UI状态
        mTvTxt = (TextView) findViewById(R.id.textView);
        mTvTxt.setText("Release and Stop");
        mIvPressToSay.setImageResource(R.drawable.microphone_onclick);
        //提交后台任务，执行录音逻辑
        new Thread(new Runnable() {
            @Override
            public void run() {
                doStart();
            }
        }).start();
    }

    /** Stop Record*/
    private void stopRecord() {
        //改变UI状态
        mTvTxt = (TextView) findViewById(R.id.textView);
        mTvTxt.setText("Hold and Talk");
        mIvPressToSay.setImageResource(R.drawable.microphone);
        //提交后台任务，执行停止逻辑
        new Thread(new Runnable() {
            @Override
            public void run() {
                doStop();
            }
        }).start();
    }

    /** Start Record logic*/
    private void doStart() {
        //创建AudioRecord
        if(mAudioResultFile!=null){
            mAudioResultFile.delete();
        }
        displayFlag=false;
        mAudioRecord = new AudioRecord(mAudioSource, mSampleRateInHz, mChannelConfig, mAudioFormat, mBufferSizeInBytes);
        if (AudioRecord.ERROR_BAD_VALUE == mBufferSizeInBytes || AudioRecord.ERROR == mBufferSizeInBytes) {
            throw new RuntimeException("Unable to getMinBufferSize");
        }
        if (mAudioRecord.getState() == AudioRecord.STATE_UNINITIALIZED) {
            throw new RuntimeException("The AudioRecord is not uninitialized");
        }
        //标记为开始采集状态
        isRecording = true;
        //创建一个流，存放从AudioRecord读取的数据
        mAudioFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), System.currentTimeMillis() + ".pcm");
        if (mAudioFile.exists()) {//音频文件保存过了删除
            mAudioFile.delete();
        }
        try {
            mAudioFile.createNewFile();//创建新文件
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "创建储存音频文件出错");
        }

        try {
            //获取到文件的数据流
            DataOutputStream mDataOutputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(mAudioFile)));
            byte[] buffer = new byte[mBufferSizeInBytes];
            mAudioRecord.startRecording();//开始录音
            //getRecordingState获取当前AudioReroding是否正在采集数据的状态
            while (isRecording && mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                int bufferReadResult = mAudioRecord.read(buffer, 0, mBufferSizeInBytes);
                for (int i = 0; i < bufferReadResult; i++) {
                    mDataOutputStream.write(buffer[i]);
                }
            }
            mDataOutputStream.close();
        } catch (Throwable t) {
            Log.e(TAG, "Recording Failed");
            stopRecord();
            recordFail();
        }

    }

    /** Stop Record logic*/
    private void doStop() {
        isRecording = false;
        //停止录音，回收AudioRecord对象，释放内存
        if (mAudioRecord != null) {
            if (mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                mAudioRecord.stop();
            }
            if (mAudioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
                mAudioRecord.release();
            }
        }
    }

    /** Record Error logic*/
    private void recordFail() {
        mAudioFile = null;
        //给用户toast提示失败，主要在主线程执行
        mMainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "Record failure", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** 调用识别api*/
    private void sendFileToBaidu(final String nowlanguage){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    FileInputStream inputFile=new FileInputStream(mAudioFile);
                    byte[] buffer =new byte[(int)mAudioFile.length()];
                    inputFile.read(buffer);
                    inputFile.close();
                    String byteString=Base64.encodeToString(buffer,Base64.NO_WRAP);
                    JSONObject object = new JSONObject();
                    object.put("format", "pcm");
                    object.put("rate", 16000);
                    if(nowlanguage.equals("简体中文")){
                        object.put("dev_pid", 1536);
                    }
                    else if(nowlanguage.equals("English")){
                        object.put("dev_pid", 1737);
                    }
                    object.put("channel", 1);
                    object.put("token", Access_token);
                    object.put("cuid", getLocalMacAddressFromIp(MainActivity.this));
                    object.put("len", buffer.length);
                    object.put("speech",byteString);
                    String stringUrl="https://vop.baidu.com/server_api";
                    URL url = new URL(stringUrl);
                    byte[] myData = object.toString().getBytes();
                    HttpURLConnection urlConnection = null;
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setDoInput(true);
                    urlConnection.setDoOutput(true);
                    urlConnection.setRequestProperty("Content-Type","application/json");
                    urlConnection.connect();
                    OutputStream outputStream = urlConnection.getOutputStream();
                    outputStream.write(myData, 0, myData.length);
                    outputStream.close();
                    int responseCode = urlConnection.getResponseCode();
                    if (responseCode == 200) {
                        // 取回响应的结果
                        InputStream inputStream = urlConnection.getInputStream();
                        StringBuffer Stringbuffer = new StringBuffer();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            Stringbuffer.append(line + "\n");
                        }
                        String resultJson=Stringbuffer.toString();
                        JSONObject Json=new JSONObject(resultJson);
                        int err_no=Json.getInt("err_no");
                        if(err_no==0){
                            JSONArray jA=Json.getJSONArray("result");
                            RecognizeResult=jA.getString(0);
                            Log.d(TAG, RecognizeResult);
                            mAudioFile.delete();
                            inputStream.close();
                            reader.close();
                            urlConnection.disconnect();
                            //调用翻译api
                            sentStringToBaidu();
                        }
                        else {
                            inputStream.close();
                            reader.close();
                            urlConnection.disconnect();
                            mAudioFile.delete();
                            mMainThreadHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this,"Error in recognize,Please speak again",Toast.LENGTH_LONG).show();
                                    textView_loading.setVisibility(View.INVISIBLE);
                                }
                            });
                        }
                    }
                    else{
                        mAudioFile.delete();
                        mMainThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this,"Http Error,Please check internet",Toast.LENGTH_LONG).show();
                                textView_loading.setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                }
                catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }
    /** 获取本地的MAC*/
    public static String getLocalMacAddressFromIp(Context context) {
        String mac_s = "";
        try {
            byte[] mac;
            NetworkInterface ne = NetworkInterface.getByInetAddress(InetAddress.getByName(getLocalIpAddress()));
            mac = ne.getHardwareAddress();
            mac_s = byte2hex(mac);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mac_s;
    }
    /** 获取MAC辅助函数*/
    public static  String byte2hex(byte[] b) {
        StringBuffer hs = new StringBuffer(b.length);
        String stmp = "";
        int len = b.length;
        for (int n = 0; n < len; n++) {
            stmp = Integer.toHexString(b[n] & 0xFF);
            if (stmp.length() == 1)
                hs = hs.append("0").append(stmp);
            else {
                hs = hs.append(stmp);
            }
        }
        return String.valueOf(hs);
    }
    /** 获取MAC辅助函数2*/
    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e(TAG, ex.toString());
        }
        return null;
    }

    /** 调用翻译api*/
    public void sentStringToBaidu(){
        String Baidu_URL = "https://fanyi-api.baidu.com/api/trans/vip/translate";
        String APP_ID = "20190320000279346";
        String APP_SECRET = "tXl9SvyjoGSevmV3H6vu";
        String from = "auto";
        String to=null;
        if(Target_Language.equals("简体中文")){
            to="zh";
        }
        else if(Target_Language.equals("English")){
            to="en";
        }
        try{
            String salt = String.valueOf(System.currentTimeMillis());
            String src = APP_ID + RecognizeResult + salt + APP_SECRET;
            String sign=md5(src);
            String query = URLEncoder.encode(RecognizeResult,"UTF-8");
            String realUrl=Baidu_URL+"?"+"q="+query+"&from="+from+"&to="+to+"&appid="+APP_ID+"&salt="+salt+"&sign="+sign;
            URL url = new URL(realUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                String temp = buffer.toString();
                JSONObject object =new JSONObject(temp);
                JSONArray trans_result=object.getJSONArray("trans_result");
                JSONObject temp1 = trans_result.getJSONObject(0);
                TranslateResult=temp1.getString("dst");
                Log.d(TAG, TranslateResult);
                inputStream.close();
                reader.close();
                urlConnection.disconnect();
                displayFlag=true;
                AudioDisplay();
            }
        }
        catch (MalformedURLException e){
            e.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        catch (JSONException e){
            e.printStackTrace();
        }

    }
    /** 加密函数*/
    public String md5(String input){
        if (input == null)
            return null;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] inputByteArray = input.getBytes("utf-8");
            messageDigest.update(inputByteArray);
            byte[] resultByteArray = messageDigest.digest();
            return byteArrayToHex(resultByteArray);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        catch (UnsupportedEncodingException e){
            return null;
        }
    }
    /** 辅助加密*/
    private static String byteArrayToHex(byte[] byteArray) {
        char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
        char[] resultCharArray = new char[byteArray.length * 2];
        int index = 0;
        for (byte b : byteArray) {
            resultCharArray[index++] = hexDigits[b >>> 4 & 0xf];
            resultCharArray[index++] = hexDigits[b & 0xf];
        }
        return new String(resultCharArray);
    }

    /** get mp3 logic*/
    public void AudioDisplay() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    String stringUrl="https://tsn.baidu.com/text2audio";
                    String text=URLEncoder.encode(TranslateResult,"UTF-8");
                    text=URLEncoder.encode(text,"UTF-8");
                    //0, common female voice
                    //1, common male voice
                    //3, emotional synthesis degree of freedom
                    //4, emotional synthesis ya-ya
                    String per="1";
                    if(Sound_Source.equals("common female voice")){
                        per="0";
                    }
                    else if(Sound_Source.equals("common male voice")){
                        per="1";
                    }
                    else if(Sound_Source.equals("emotional synthesis degree of freedom")){
                        per="3";
                    }
                    else if(Sound_Source.equals("emotional synthesis ya-ya")){
                        per="4";
                    }
                    String realUrl=stringUrl+"?tex="+text+"&lan=zh&cuid="+getLocalMacAddressFromIp(MainActivity.this)+"&ctp=1&"+"tok="+Access_token+"&per="+per;
                    URL url = new URL(realUrl);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();
                    String contentType = urlConnection.getContentType();
                    if (contentType.contains("audio/")) {
                        int responseCode = urlConnection.getResponseCode();
                        if (responseCode != 200) {
                            throw new Exception("http response code is" + responseCode);
                        }
                        InputStream inputStream = urlConnection.getInputStream();
                        byte[] b = new byte[1024];
                        // 定义一个输出流存储接收到的数据
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        // 开始接收数据
                        int len = 0;
                        while (true) {
                            len = inputStream.read(b);
                            if (len == -1) {
                                // 数据读完
                                break;
                            }
                            byteArrayOutputStream.write(b, 0, len);
                        }
                        byte[] result=byteArrayOutputStream.toByteArray();
                        mAudioResultFile=new File(Environment.getExternalStorageDirectory().getAbsolutePath(), System.currentTimeMillis() + ".mp3");
                        if (mAudioResultFile.exists()) {//音频文件保存过了删除
                            mAudioResultFile.delete();
                        }
                        mAudioResultFile.createNewFile();//创建新文件
                        FileOutputStream os = new FileOutputStream(mAudioResultFile);
                        os.write(result);
                        os.close();
                    } else {
                        InputStream inputStream = urlConnection.getInputStream();
                        StringBuffer Stringbuffer = new StringBuffer();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            Stringbuffer.append(line + "\n");
                        }
                        String resultJson=Stringbuffer.toString();
                        JSONObject Json=new JSONObject(resultJson);
                        //error
                    }
                    mMainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            textView_loading.setVisibility(View.INVISIBLE);
                        }
                    });
                    mPlayer=new MediaPlayer();
                    mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            mPlayer.release();
                            displayFlag=true;
                        }
                    });
                    mPlayer.setDataSource(mAudioResultFile.getPath());
                    mPlayer.prepare();
                    displayFlag=false;
                    mPlayer.start();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /** display logic*/
    void btn_play(){
        if(!displayFlag){
            Toast.makeText(MainActivity.this,"Please wait moment",Toast.LENGTH_LONG).show();
        }
        else {
            try{
                displayFlag=false;
                mPlayer=new MediaPlayer();
                mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mPlayer.release();
                        displayFlag=true;
                    }
                });
                mPlayer.setDataSource(mAudioResultFile.getPath());
                mPlayer.prepare();
                mPlayer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}




















