package com.nino.seen19.real;
public class VideoLoop implements Runnable {
    MainActivity act;
    public VideoLoop(MainActivity a){ act=a; }
    public void run(){
        while(act.videoEyesOn){
            try{
                Thread.sleep(3000);
                if(act.lastBmp!=null && act.videoEyesOn){
                    act.log("--- EYE NEVER BLINKS SAM DOT + VOICE ---",0);
                    String txt=""; try{ txt=act.chatInput.getText().toString(); }catch(Exception e){}
                    for(int i=0;i<12;i++){ Thread th=new Thread(new ModelCaller(i,act.lastBmp,MainActivity.TASKS[i]+" "+txt,act,act.apiKey)); th.start(); Thread.sleep(250); }
                }
            }catch(Exception e){}
        }
    }
}
