package com.rogerajie.mynotes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

/** All user information stays on the device. No accounts, network or deadlines. */
public final class NotesDb extends SQLiteOpenHelper {
    public static final class Memo {
        public long id;
        public String title;
        public String body;
        Memo(long id, String title, String body) {
            this.id=id;this.title=title;this.body=body;
        }
    }
    public static final class Task {
        public long id;
        public String text;
        public boolean done;
        Task(long id, String text, boolean done) {
            this.id=id;this.text=text;this.done=done;
        }
    }

    public NotesDb(Context c) { super(c.getApplicationContext(), "mynotes.db", null, 1); }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE memos (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, body TEXT NOT NULL)");
        db.execSQL("CREATE TABLE tasks (id INTEGER PRIMARY KEY AUTOINCREMENT, body TEXT NOT NULL, done INTEGER NOT NULL DEFAULT 0)");
    }
    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) throw new IllegalStateException("Missing migration");
    }

    public List<Memo> memos() {
        List<Memo> result=new ArrayList<>();
        try(Cursor c=getReadableDatabase().rawQuery("SELECT id,title,body FROM memos ORDER BY id DESC",null)){
            while(c.moveToNext())result.add(new Memo(c.getLong(0),c.getString(1),c.getString(2)));
        }
        return result;
    }
    public Memo memo(long id) {
        try(Cursor c=getReadableDatabase().rawQuery("SELECT id,title,body FROM memos WHERE id=?",new String[]{Long.toString(id)})){
            if(c.moveToFirst())return new Memo(c.getLong(0),c.getString(1),c.getString(2));
        }
        return null;
    }
    public long saveMemo(long id,String title,String body) {
        ContentValues cv=new ContentValues();
        cv.put("title",title.trim());
        cv.put("body",body);
        if(id>0) {
            getWritableDatabase().update("memos",cv,"id=?",new String[]{Long.toString(id)});
            return id;
        }
        return getWritableDatabase().insertOrThrow("memos",null,cv);
    }
    public void deleteMemo(long id) {
        getWritableDatabase().delete("memos","id=?",new String[]{Long.toString(id)});
    }
    public List<Task> tasks() {
        List<Task> result=new ArrayList<>();
        try(Cursor c=getReadableDatabase().rawQuery("SELECT id,body,done FROM tasks ORDER BY done ASC,id DESC",null)){
            while(c.moveToNext())result.add(new Task(c.getLong(0),c.getString(1),c.getInt(2)!=0));
        }
        return result;
    }
    public void saveTask(long id,String text) {
        text=text.trim();
        if(text.isEmpty())return;
        ContentValues cv=new ContentValues();
        cv.put("body",text);
        if(id>0)getWritableDatabase().update("tasks",cv,"id=?",new String[]{Long.toString(id)});
        else {cv.put("done",0);getWritableDatabase().insertOrThrow("tasks",null,cv);}
    }
    public void toggleTask(long id,boolean done){
        ContentValues cv=new ContentValues();
        cv.put("done",done?1:0);
        getWritableDatabase().update("tasks",cv,"id=?",new String[]{Long.toString(id)});
    }
    public void deleteTask(long id){
        getWritableDatabase().delete("tasks","id=?",new String[]{Long.toString(id)});
    }
    public static String displayTitle(String title,String body) {
        if(title!=null&&!title.trim().isEmpty())return title.trim();
        if(body==null||body.trim().isEmpty())return "无标题";
        String line="";
        for(String s:body.split("\\r?\\n")){
            if(!s.trim().isEmpty()){line=s.trim();break;}
        }
        if(line.isEmpty())return "无标题";
        int last=line.length();
        for(char ch:new char[]{'。','！','？','.','!','?'}) {
            int i=line.indexOf(ch);
            if(i>=0)last=Math.min(last,i+1);
        }
        return line.substring(0,Math.min(80,last));
    }
}
