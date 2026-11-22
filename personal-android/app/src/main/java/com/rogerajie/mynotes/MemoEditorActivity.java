package com.rogerajie.mynotes;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Simple full-screen memo editor. Blank titles display as the first sentence. */
public final class MemoEditorActivity extends Activity {
    private static final int ORANGE=Color.rgb(242,140,71);
    private NotesDb db;
    private EditText title,body;
    private long memoId;
    private boolean deleted=false;

    private int d(int x){return Math.round(x*getResources().getDisplayMetrics().density);}
    private TextView text(String s,int size,int color,boolean bold){
        TextView t=new TextView(this);
        t.setText(s);t.setTextSize(size);t.setTextColor(color);
        if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        return t;
    }
    private GradientDrawable rounded(int bg) {
        GradientDrawable g=new GradientDrawable();
        g.setColor(bg);g.setCornerRadius(d(10));return g;
    }

    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);
        db=new NotesDb(this);
        memoId=saved!=null?saved.getLong("memo_id",-1):getIntent().getLongExtra("memo_id",-1);
        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().setNavigationBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);

        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);
        setContentView(root);
        root.setOnApplyWindowInsetsListener((v,insets)->{
            int top,bottom;
            if(Build.VERSION.SDK_INT>=30) {
                android.graphics.Insets bars=insets.getInsets(WindowInsets.Type.systemBars());
                android.graphics.Insets ime=insets.getInsets(WindowInsets.Type.ime());
                top=bars.top;bottom=Math.max(bars.bottom,ime.bottom);
            } else {
                top=insets.getSystemWindowInsetTop();
                bottom=insets.getSystemWindowInsetBottom();
            }
            v.setPadding(0,top,0,bottom);
            return insets;
        });
        root.requestApplyInsets();

        LinearLayout actions=new LinearLayout(this);
        actions.setGravity(Gravity.CENTER_VERTICAL);
        actions.setPadding(d(20),0,d(20),0);
        root.addView(actions,new LinearLayout.LayoutParams(-1,d(65)));
        TextView back=text("‹ 返回",16,Color.rgb(85,85,85),false);
        actions.addView(back,new LinearLayout.LayoutParams(0,-2,1));
        back.setOnClickListener(v->finish());

        if(memoId>0) {
            TextView delete=text("删除",14,Color.rgb(170,170,170),false);
            delete.setPadding(d(12),d(10),d(12),d(10));
            actions.addView(delete,new LinearLayout.LayoutParams(-2,-2));
            delete.setOnClickListener(v->new AlertDialog.Builder(this)
                .setMessage("确定删除这条记事吗？")
                .setNegativeButton("取消",null)
                .setPositiveButton("删除",(dlg,w)->{
                    db.deleteMemo(memoId);
                    deleted=true;
                    finish();
                }).show());
        }

        TextView save=text("保存",15,Color.rgb(207,106,42),true);
        save.setPadding(d(16),d(10),d(16),d(10));
        save.setGravity(Gravity.CENTER);
        save.setBackground(rounded(Color.rgb(255,239,228)));
        actions.addView(save,new LinearLayout.LayoutParams(-2,-2));
        save.setOnClickListener(v->{persist();finish();});

        View bar=new View(this);bar.setBackgroundColor(Color.rgb(241,241,241));
        root.addView(bar,new LinearLayout.LayoutParams(-1,d(1)));

        LinearLayout content=new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(d(22),d(24),d(22),0);
        root.addView(content,new LinearLayout.LayoutParams(-1,0,1));

        title=new EditText(this);
        title.setTextSize(22);
        title.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        title.setSingleLine(true);
        title.setBackgroundColor(Color.TRANSPARENT);
        title.setTextColor(Color.rgb(34,34,34));
        title.setHintTextColor(Color.rgb(181,181,181));
        title.setHint("标题（选填）");
        title.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        content.addView(title,new LinearLayout.LayoutParams(-1,d(49)));

        body=new EditText(this);
        body.setTextSize(16);
        body.setGravity(Gravity.TOP|Gravity.LEFT);
        body.setBackgroundColor(Color.TRANSPARENT);
        body.setTextColor(Color.rgb(37,37,37));
        body.setHintTextColor(Color.rgb(181,181,181));
        body.setHint("在这里输入内容……");
        body.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE|
            InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,0,1);
        bp.topMargin=d(15);
        content.addView(body,bp);

        if(memoId>0){
            NotesDb.Memo note=db.memo(memoId);
            if(note!=null){
                title.setText(note.title);
                body.setText(note.body);
            }
        }
    }
    private void persist(){
        if(deleted||title==null||body==null)return;
        String t=title.getText().toString();
        String b=body.getText().toString();
        if(memoId<=0&&t.trim().isEmpty()&&b.trim().isEmpty())return;
        memoId=db.saveMemo(memoId,t,b);
    }
    @Override protected void onPause(){
        persist();
        super.onPause();
    }
    @Override public void onSaveInstanceState(Bundle saved){
        persist();
        saved.putLong("memo_id",memoId);
        super.onSaveInstanceState(saved);
    }
    @Override protected void onDestroy(){
        if(db!=null)db.close();
        super.onDestroy();
    }
}
