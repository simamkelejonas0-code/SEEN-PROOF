package com.nino.seen19.real;
import java.io.FileOutputStream;
import java.util.ArrayList;
public class FusionManager {
    public static ArrayList<String> observations = new ArrayList<String>();
    public static int expected=12;
    public static synchronized void addObs(String modelName,String text){
        try{
            if(text==null) text="OK";
            if(text.length()>200) text=text.substring(0,200);
            observations.add(modelName+": "+text);
            if(observations.size()>=expected){
                // FUSE - call wonder to give FINAL
                StringBuilder all=new StringBuilder();
                for(int i=0;i<observations.size();i++) all.append(observations.get(i)).append(" | ");
                String fusePrompt="FUSE 12 eyes into ONE final answer and suggest action. Observations: "+all.toString()+" Task: What is in image? Is safe? What can user do? Give FINAL and SUGGEST.";
                MainActivity.inst.log("--- FUSING 12 EYES INTO FINAL ---",0);
                Thread th=new Thread(new ModelCaller(99,null,fusePrompt,MainActivity.inst,MainActivity.inst.apiKey));
                th.start();
                observations.clear();
            }
        }catch(Exception e){}
    }
}
