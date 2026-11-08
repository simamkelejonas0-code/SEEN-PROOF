package com.nino.seen19.real;
public class LogRunnable implements Runnable {
    MainActivity act; String msg; int type;
    public LogRunnable(MainActivity a,String m,int t){ act=a; msg=m; type=t; }
    public void run(){ try{ if(type==0) act.logBox.append("\n"+msg); else act.chatBox.append("\n"+msg); }catch(Exception e){} }
}
