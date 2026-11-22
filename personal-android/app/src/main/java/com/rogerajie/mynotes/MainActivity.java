package com.rogerajie.mynotes;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.List;

public final class MainActivity extends Activity {
    private static final int ORANGE=Color.rgb(242,140,71);
    private static final int INK=Color.rgb(36,36,36);
    private static final int GREY=Color.rgb(150,150,150);
    private NotesDb db;
    private LinearLayout root, rows;
    private ScrollView scroll;
    private TextView tabMemos,tabTasks;
    private View lineMemos,lineTasks;
    private int currentTab=0;

    private int d(float dp){return Math.round(dp*getResources().getDisplayMetrics().density);}
    private GradientDrawable round(int bg,int radius){
        GradientDrawable gd=new GradientDrawable();
        gd.setColor(bg);gd.setCornerRadius(d(radius));return gd;
    }
    private GradientDrawable circle(boolean checked) {
        GradientDrawable gd=new GradientDrawable();
        gd.setShape(GradientDrawable.OVAL);
        gd.setColor(checked?ORANGE:Color.WHITE);
        gd.setStroke(d(2),checked?ORANGE:Color.rgb(214,214,214));
        return gd;
    }
    private TextView text(String s,int sp,int color,boolean bold) {
        TextView t=new TextView(this);
        t.setText(s);t.setTextSize(sp);t.setTextColor(color);
        if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        return t;
    }
    private LinearLayout column() {
        LinearLayout l=new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }
    private LinearLayout.LayoutParams lp(int w,int h) {
        return new LinearLayout.LayoutParams(w<0?w:d(w),h<0?h:d(h));
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        if(state!=null)currentTab=state.getInt("tab",0);
        db=new NotesDb(this);
        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().setNavigationBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);

        root=column();
        root.setBackgroundColor(Color.WHITE);
        setContentView(root);
        root.setOnApplyWindowInsetsListener((v,insets)->{
            int top,bottom;
            if(Build.VERSION.SDK_INT>=30) {
                android.graphics.Insets bars=insets.getInsets(WindowInsets.Type.systemBars());
                top=bars.top;bottom=bars.bottom;
            } else {
                top=insets.getSystemWindowInsetTop();
                bottom=insets.getSystemWindowInsetBottom();
            }
            v.setPadding(0,top,0,bottom);
            return insets;
        });
        root.requestApplyInsets();

        LinearLayout header=column();
        header.setPadding(d(21),d(23),d(21),d(14));
        TextView subtitle=text("MY NOTES",12,Color.rgb(147,147,147),false);
        header.addView(subtitle,lp(-1,-2));
        TextView title=text("我的记事本",25,INK,true);
        LinearLayout.LayoutParams titleP=lp(-1,-2);titleP.topMargin=d(4);
        header.addView(title,titleP);
        root.addView(header,lp(-1,-2));

        LinearLayout tabs=new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabs.setBackgroundColor(Color.WHITE);
        LinearLayout left=tab(0,"记事本");
        LinearLayout right=tab(1,"To-do List");
        tabs.addView(left,new LinearLayout.LayoutParams(0,d(54),1));
        tabs.addView(right,new LinearLayout.LayoutParams(0,d(54),1));
        root.addView(tabs,lp(-1,54));
        View divider=new View(this);divider.setBackgroundColor(Color.rgb(235,235,235));
        root.addView(divider,lp(-1,1));

        FrameLayout body=new FrameLayout(this);
        root.addView(body,new LinearLayout.LayoutParams(-1,0,1));
        scroll=new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setVerticalScrollBarEnabled(false);
        rows=column();
        rows.setPadding(d(20),d(12),d(20),d(100));
        scroll.addView(rows,new ScrollView.LayoutParams(-1,-2));
        body.addView(scroll,new FrameLayout.LayoutParams(-1,-1));

        TextView add=text("+",33,Color.WHITE,false);
        add.setGravity(Gravity.CENTER);
        add.setBackground(round(ORANGE,100));
        add.setElevation(d(5));
        add.setContentDescription("新增");
        FrameLayout.LayoutParams ap=new FrameLayout.LayoutParams(d(59),d(59),Gravity.BOTTOM|Gravity.RIGHT);
        ap.setMargins(0,0,d(23),d(23));
        body.addView(add,ap);
        add.setOnClickListener(v->{
            if(currentTab==0)startActivity(new Intent(this,MemoEditorActivity.class));
            else taskDialog(null);
        });
        setTab(currentTab);
    }

    private LinearLayout tab(int index,String label) {
        LinearLayout cell=column();
        TextView name=text(label,16,INK,true);
        name.setGravity(Gravity.CENTER);
        cell.addView(name,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout indicatorHolder=new LinearLayout(this);
        indicatorHolder.setPadding(d(18),0,d(18),0);
        View indicator=new View(this);
        indicator.setBackground(round(ORANGE,4));
        indicatorHolder.addView(indicator,lp(-1,3));
        cell.addView(indicatorHolder,lp(-1,3));
        if(index==0){tabMemos=name;lineMemos=indicator;}
        else {tabTasks=name;lineTasks=indicator;}
        cell.setOnClickListener(v->setTab(index));
        return cell;
    }

    private void setTab(int index){
        currentTab=index;
        tabMemos.setTextColor(index==0?INK:Color.rgb(119,119,119));
        tabTasks.setTextColor(index==1?INK:Color.rgb(119,119,119));
        lineMemos.setVisibility(index==0?View.VISIBLE:View.INVISIBLE);
        lineTasks.setVisibility(index==1?View.VISIBLE:View.INVISIBLE);
        refresh();
    }

    @Override protected void onResume(){
        super.onResume();
        if(rows!=null)refresh();
    }
    @Override public void onSaveInstanceState(Bundle b){
        b.putInt("tab",currentTab);
        super.onSaveInstanceState(b);
    }
    @Override protected void onDestroy(){
        if(db!=null)db.close();
        super.onDestroy();
    }

    private void refresh(){
        rows.removeAllViews();
        scroll.scrollTo(0,0);
        if(currentTab==0)showMemos();else showTasks();
    }

    private void showMemos(){
        TextView hint=text("一条条记录想法，标题可以留空",12,Color.rgb(170,170,170),false);
        hint.setPadding(d(3),d(12),0,d(15));
        rows.addView(hint,lp(-1,-2));
        List<NotesDb.Memo> all=db.memos();
        if(all.isEmpty()) {
            empty("还没有记事\n点击右下角 + 新建");
            return;
        }
        for(NotesDb.Memo memo:all){
            LinearLayout card=column();
            card.setPadding(d(3),d(18),d(3),d(17));
            TextView title=text(NotesDb.displayTitle(memo.title,memo.body),17,INK,true);
            card.addView(title,lp(-1,-2));
            TextView preview=text(memo.body.isEmpty()?"（暂无正文）":memo.body,14,
                Color.rgb(138,138,138),false);
            preview.setMaxLines(2);preview.setEllipsize(TextUtils.TruncateAt.END);
            LinearLayout.LayoutParams pp=lp(-1,-2);pp.topMargin=d(6);
            card.addView(preview,pp);
            View divider=new View(this);divider.setBackgroundColor(Color.rgb(239,239,239));
            LinearLayout.LayoutParams dp=lp(-1,1);dp.topMargin=d(16);
            card.addView(divider,dp);
            rows.addView(card,lp(-1,-2));
            card.setOnClickListener(v->startActivity(
                new Intent(this,MemoEditorActivity.class).putExtra("memo_id",memo.id)));
            card.setOnLongClickListener(v->{deleteMemo(memo.id);return true;});
        }
    }
    private void empty(String s){
        TextView v=text(s,14,Color.rgb(170,170,170),false);
        v.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL);
        v.setPadding(d(12),d(92),d(12),0);
        rows.addView(v,lp(-1,-2));
    }

    private void section(String s,boolean divider){
        if(divider){
            View line=new View(this);
            line.setBackgroundColor(Color.rgb(237,237,237));
            LinearLayout.LayoutParams p=lp(-1,1);p.topMargin=d(17);p.bottomMargin=d(15);
            rows.addView(line,p);
        }
        TextView t=text(s,12,GREY,true);
        t.setPadding(d(4),divider?0:d(18),0,d(8));
        rows.addView(t,lp(-1,-2));
    }
    private void showTasks(){
        List<NotesDb.Task> tasks=db.tasks();
        int active=0,done=0;
        for(NotesDb.Task t:tasks){if(t.done)done++;else active++;}
        section("未完成 · "+active,false);
        for(NotesDb.Task t:tasks)if(!t.done)showTaskRow(t);
        section("已完成 · "+done,true);
        for(NotesDb.Task t:tasks)if(t.done)showTaskRow(t);
    }
    private void showTaskRow(NotesDb.Task task){
        LinearLayout row=new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(d(2),d(12),d(2),d(12));
        TextView circle=text(task.done?"✓":"",15,Color.WHITE,false);
        circle.setGravity(Gravity.CENTER);
        circle.setBackground(circle(task.done));
        circle.setContentDescription(task.done?"取消完成":"标记完成");
        LinearLayout.LayoutParams cp=lp(25,25);cp.rightMargin=d(12);
        row.addView(circle,cp);
        circle.setOnClickListener(v->{db.toggleTask(task.id,!task.done);refresh();});
        TextView body=text(task.text,16,
            task.done?Color.rgb(177,177,177):Color.rgb(41,41,41),false);
        if(task.done)body.setPaintFlags(body.getPaintFlags()|Paint.STRIKE_THRU_TEXT_FLAG);
        body.setGravity(Gravity.CENTER_VERTICAL);
        row.addView(body,new LinearLayout.LayoutParams(0,-2,1));
        body.setOnClickListener(v->taskDialog(task));
        row.setOnLongClickListener(v->{deleteTask(task.id);return true;});
        rows.addView(row,lp(-1,-2));
        View divider=new View(this);divider.setBackgroundColor(Color.rgb(246,246,246));
        rows.addView(divider,lp(-1,1));
    }
    private void taskDialog(NotesDb.Task task){
        EditText field=new EditText(this);
        field.setText(task==null?"":task.text);
        field.setHint("输入待办内容……");
        field.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE|
            InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        field.setMinLines(2);field.setMaxLines(5);
        LinearLayout wrapper=column();
        wrapper.setPadding(d(20),d(3),d(20),0);
        wrapper.addView(field,lp(-1,-2));
        AlertDialog.Builder b=new AlertDialog.Builder(this)
            .setTitle(task==null?"新增待办事项":"编辑待办事项")
            .setView(wrapper)
            .setNegativeButton("取消",(dlg,which)->{})
            .setPositiveButton("保存",(dlg,which)->{
                if(!field.getText().toString().trim().isEmpty()){
                    db.saveTask(task==null?0:task.id,field.getText().toString());
                    refresh();
                }
            });
        if(task!=null)b.setNeutralButton("删除",(dlg,which)->deleteTask(task.id));
        b.show();
    }
    private void deleteTask(long id){
        new AlertDialog.Builder(this).setMessage("确定删除这条待办事项吗？")
            .setNegativeButton("取消",null)
            .setPositiveButton("删除",(dlg,w)->{db.deleteTask(id);refresh();}).show();
    }
    private void deleteMemo(long id){
        new AlertDialog.Builder(this).setMessage("确定删除这条记事吗？")
            .setNegativeButton("取消",null)
            .setPositiveButton("删除",(dlg,w)->{db.deleteMemo(id);refresh();}).show();
    }
}
