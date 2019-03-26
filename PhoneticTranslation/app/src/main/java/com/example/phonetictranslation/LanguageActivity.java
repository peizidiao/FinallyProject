package com.example.phonetictranslation;

import android.Manifest;
import android.app.ActionBar;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class LanguageActivity extends AppCompatActivity {
    private String Source_language = null;
    private String Target_Language = null;
    private String Sound_Source = null;
    private String data[]=new String[2];
    private String selection1="简体中文 To English";
    private String selection2="English To 简体中文";
    private String selected =null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setCustomView(R.layout.actionbar_language);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        Intent intent=getIntent();
        Source_language=intent.getStringExtra("Source_Language");
        Target_Language=intent.getStringExtra("Target_Language");
        Sound_Source=intent.getStringExtra("Sound_Source");
        data[0]=selection1;
        data[1]=selection2;
        ArrayAdapter<String> adapter=new ArrayAdapter<String> (LanguageActivity.this,android.R.layout.simple_list_item_1,data);
        ListView listView=(ListView)findViewById(R.id.ListView3);
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

            }
        });
    }

    public void Back_click(View view) {
        Intent intent=new Intent();
        intent.putExtra("Source_Language",Source_language);
        intent.putExtra("Target_Language",Target_Language);
        intent.putExtra("Sound_Source",Sound_Source);
        setResult(RESULT_OK,intent);
        finish();
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

    public void btn_confirm(View view) {
        if(selected==selection1){
            Source_language="简体中文";
            Target_Language="English";
            Intent intent=new Intent();
            intent.putExtra("Source_Language",Source_language);
            intent.putExtra("Target_Language",Target_Language);
            intent.putExtra("Sound_Source",Sound_Source);
            setResult(RESULT_OK,intent);
            finish();
        }
        else if(selected==selection2){
            Source_language="English";
            Target_Language="简体中文";
            Intent intent=new Intent();
            intent.putExtra("Source_Language",Source_language);
            intent.putExtra("Target_Language",Target_Language);
            intent.putExtra("Sound_Source",Sound_Source);
            setResult(RESULT_OK,intent);
            finish();
        }
        else {
            Toast.makeText(LanguageActivity.this,"Please select one!",Toast.LENGTH_LONG).show();
        }
    }
}
