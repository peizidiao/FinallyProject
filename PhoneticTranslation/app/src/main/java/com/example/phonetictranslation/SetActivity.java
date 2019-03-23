package com.example.phonetictranslation;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class SetActivity extends AppCompatActivity {

    private List<listview_item> list1=new ArrayList<>();
    private List<listview_item> list2=new ArrayList<>();
    private String Source_Language = null;
    private String Target_Language =null;
    private String Sound_Source = null;
    private MyAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setCustomView(R.layout.actionbar_set);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        Intent intent=getIntent();

        Source_Language=intent.getStringExtra("Source_Language");
        Target_Language=intent.getStringExtra("Target_Language");
        Sound_Source=intent.getStringExtra("Sound_Source");

        list1.add(new listview_item("Language",Source_Language+" To "+Target_Language));
        list1.add(new listview_item("Sound Source",Sound_Source));

        list2.add(new listview_item("About us",null));

        adapter=new MyAdapter(SetActivity.this,R.layout.listview_item,list1);
        MyAdapter adapter2=new MyAdapter(SetActivity.this,R.layout.listview_item,list2);

        ListView listView1=(ListView)findViewById(R.id.ListView1);
        listView1.setAdapter(adapter);
        listView1.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,long id){
                listview_item item=list1.get(position);
                if(item.getTitle().equals("Language")){
                    //跳转,这里应该要传值回来
                    Intent intent =new Intent(SetActivity.this,LanguageActivity.class);
                    intent.putExtra("Source_Language", Source_Language);
                    intent.putExtra("Target_Language", Target_Language);
                    startActivityForResult(intent,1);
                }
                else if(item.getTitle().equals("Sound Source")){
                    //跳转，这里应该要传值回来
                    Intent intent =new Intent(SetActivity.this,SoundActivity.class);
                    startActivityForResult(intent,2);
                }
            }
        });

        ListView listView2=(ListView)findViewById(R.id.ListView2);
        listView2.setAdapter(adapter2);
        listView2.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,long id){
                listview_item item=list2.get(position);
                if(item.getTitle().equals("About us")){
                    //跳转
                    Intent intent =new Intent(SetActivity.this,AboutActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    Source_Language = data.getStringExtra("Source_Language");
                    Target_Language = data.getStringExtra("Target_Language");
                    list1.clear();
                    list1.add(new listview_item("Language",Source_Language+" To "+Target_Language));
                    list1.add(new listview_item("Sound Source",Sound_Source));
                    adapter.notifyDataSetChanged();
                }
            case 2:
                if (resultCode == RESULT_OK) {
                    Sound_Source = data.getStringExtra("Sound_Source");
                }
        }
    }

    @Override
    public boolean onKeyDown(int KeyCode, KeyEvent event){
        if(KeyCode==KeyEvent.KEYCODE_BACK){
            Intent intent=new Intent();
            intent.putExtra("Source_Language",Source_Language);
            intent.putExtra("Target_Language",Target_Language);
            intent.putExtra("Sound_Source",Sound_Source);
            setResult(RESULT_OK,intent);
        }
        return super.onKeyDown(KeyCode,event);
    }

    public void Back_click(View view) {
        Intent intent=new Intent();
        intent.putExtra("Source_Language",Source_Language);
        intent.putExtra("Target_Language",Target_Language);
        intent.putExtra("Sound_Source",Sound_Source);
        setResult(RESULT_OK,intent);
        finish();
    }

    public class listview_item{
        private String title;
        private String selected;
        public listview_item(String title,String selected){
            this.title=title;
            this.selected=selected;
        }
        public String getTitle(){
            return title;
        }
        public String getSelected(){
            return selected;
        }
        public void setSelected(String selected){
            this.selected=selected;
        }
    }

    public class MyAdapter extends ArrayAdapter<listview_item>{
        private int resouceID;
        public MyAdapter(Context context,int resouceID,List<listview_item> objects){
            super(context,resouceID,objects);
            this.resouceID=resouceID;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            listview_item list_item =getItem(position);
            View view = LayoutInflater.from(getContext()).inflate(resouceID,parent,false);
            TextView textView_title=(TextView)view.findViewById(R.id.textView_item);
            textView_title.setText(list_item.getTitle());
            TextView textView_selected=(TextView)view.findViewById(R.id.textView_selected);
            textView_selected.setText(list_item.getSelected());
            return view;
        }
    }


}
