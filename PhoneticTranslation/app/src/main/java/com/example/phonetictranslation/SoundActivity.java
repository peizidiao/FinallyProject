package com.example.phonetictranslation;

import android.app.ActionBar;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class SoundActivity extends AppCompatActivity {
    private String Source_language = null;
    private String Target_Language = null;
    private String Sound_Source = null;
    private String data[]=new String[4];
    private String selection1="common female voice";
    private String selection2="common male voice";
    private String selection3="emotional synthesis degree of freedom";
    private String selection4="emotional synthesis ya-ya";
    private String selected =null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setCustomView(R.layout.actionbar_sound);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        Intent intent=getIntent();
        Source_language=intent.getStringExtra("Source_Language");
        Target_Language=intent.getStringExtra("Target_Language");
        Sound_Source=intent.getStringExtra("Sound_Source");

        data[0]=selection1;
        data[1]=selection2;
        data[2]=selection3;
        data[3]=selection4;
        ArrayAdapter<String> adapter=new ArrayAdapter<String> (SoundActivity.this,android.R.layout.simple_list_item_1,data);
        ListView listView=(ListView)findViewById(R.id.ListView4);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(data[position].equals(selection1)){
                    selected=selection1;
                }
                else if(data[position].equals(selection2)){
                    selected=selection2;
                }
                else if(data[position].equals(selection3)){
                    selected=selection3;
                }
                else if(data[position].equals(selection4)){
                    selected=selection4;
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int KeyCode, KeyEvent event){
        if(KeyCode==KeyEvent.KEYCODE_BACK){
            Intent intent=new Intent();
            intent.putExtra("Source_Language",Source_language);
            intent.putExtra("Target_Language",Target_Language);
            intent.putExtra("Sound_Source",Sound_Source);
            setResult(RESULT_OK,intent);
        }
        return super.onKeyDown(KeyCode,event);
    }

    public void Back_click(View view) {
        Intent intent=new Intent();
        intent.putExtra("Source_Language",Source_language);
        intent.putExtra("Target_Language",Target_Language);
        intent.putExtra("Sound_Source",Sound_Source);
        setResult(RESULT_OK,intent);
        finish();
    }

    public void btn_confirm(View view) {
        if(selected==selection1){
            Sound_Source=selection1;
            Intent intent=new Intent();
            intent.putExtra("Source_Language",Source_language);
            intent.putExtra("Target_Language",Target_Language);
            intent.putExtra("Sound_Source",Sound_Source);
            setResult(RESULT_OK,intent);
            finish();
        }
        else if(selected==selection2){
            Sound_Source=selection2;
            Intent intent=new Intent();
            intent.putExtra("Source_Language",Source_language);
            intent.putExtra("Target_Language",Target_Language);
            intent.putExtra("Sound_Source",Sound_Source);
            setResult(RESULT_OK,intent);
            finish();
        }
        else if(selected==selection3){
            Sound_Source=selection3;
            Intent intent=new Intent();
            intent.putExtra("Source_Language",Source_language);
            intent.putExtra("Target_Language",Target_Language);
            intent.putExtra("Sound_Source",Sound_Source);
            setResult(RESULT_OK,intent);
            finish();
        }
        else if(selected==selection4){
            Sound_Source=selection4;
            Intent intent=new Intent();
            intent.putExtra("Source_Language",Source_language);
            intent.putExtra("Target_Language",Target_Language);
            intent.putExtra("Sound_Source",Sound_Source);
            setResult(RESULT_OK,intent);
            finish();
        }
        else {
            Toast.makeText(SoundActivity.this,"Please select one!",Toast.LENGTH_LONG).show();
        }
    }
}
