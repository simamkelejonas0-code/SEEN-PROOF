package com.nino.seen19.real;
import android.graphics.Bitmap;
import android.util.Base64;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
public class ModelCaller implements Runnable {
    int id; Bitmap bmp; String prompt; MainActivity act; String apiKey;
    public ModelCaller(int i,Bitmap b,String p,MainActivity a,String k){id=i;bmp=b;prompt=p;act=a;apiKey=k;}
    String callJson(String endpoint,String body) throws Exception{
        URL url=new URL(endpoint); HttpURLConnection c=(HttpURLConnection)url.openConnection();
        c.setRequestMethod("POST"); c.setRequestProperty("Authorization","Bearer "+apiKey); c.setRequestProperty("Content-Type","application/json"); c.setDoOutput(true); c.setConnectTimeout(15000); c.setReadTimeout(60000);
        OutputStream os=c.getOutputStream(); os.write(body.getBytes("UTF-8")); os.close();
        int code=c.getResponseCode(); InputStream is=code>=200&&code<300?c.getInputStream():c.getErrorStream();
        Scanner sc=new Scanner(is).useDelimiter("\\A"); String r=sc.hasNext()?sc.next():""; sc.close(); return code+"|"+r;
    }
    String callAudio(String endpoint,File audioFile) throws Exception{
        String boundary="----SEEN20"; URL url=new URL(endpoint); HttpURLConnection c=(HttpURLConnection)url.openConnection();
        c.setRequestMethod("POST"); c.setRequestProperty("Authorization","Bearer "+apiKey); c.setRequestProperty("Content-Type","multipart/form-data; boundary="+boundary); c.setDoOutput(true); c.setConnectTimeout(20000); c.setReadTimeout(60000);
        OutputStream os=c.getOutputStream(); String head="--"+boundary+"\r\nContent-Disposition: form-data; name=\"model\"\r\n\r\nmuse-voice-transcribe-1.0\r\n--"+boundary+"\r\nContent-Disposition: form-data; name=\"file\"; filename=\"voice.3gp\"\r\nContent-Type: audio/3gpp\r\n\r\n"; os.write(head.getBytes());
        FileInputStream fis=new FileInputStream(audioFile); byte[] buf=(byte[])java.lang.reflect.Array.newInstance(byte.class, 4096); int len; while((len=fis.read(buf))!=-1){ os.write(buf,0,len); } fis.close();
        os.write(("\r\n--"+boundary+"--\r\n").getBytes()); os.close(); int code=c.getResponseCode(); InputStream is=code>=200&&code<300?c.getInputStream():c.getErrorStream(); Scanner sc=new Scanner(is).useDelimiter("\\A"); String r=sc.hasNext()?sc.next():""; sc.close(); return code+"|"+r;
    }
    String extractText(String json){
        try{ int idx=json.indexOf("output_text"); if(idx==-1) idx=json.indexOf("\"text\":"); if(idx!=-1){ int colon=json.indexOf(":",idx); int q1=json.indexOf("\"",colon+1); int q2=json.indexOf("\"",q1+1); if(q2!=-1) return json.substring(q1+1,q2).replace("\\n"," ").replace("\\\"","\""); } return ""; }catch(Exception e){ return ""; }
    }
    public void run(){
        try{
            boolean isFusion = (id==99);
            String model; String name; String safe=prompt.replace("\"","'").replace("\n"," "); if(safe.length()>800) safe=safe.substring(0,800);
            String endpoint="https://api.meta.ai/v1/responses"; String body="";
            if(isFusion){
                model="muse-wonder-1.0"; name="FINAL-FUSION";
                act.log(">> FINAL FUSION WONDER FUSING",0);
                body="{\"model\":\""+model+"\",\"input\":\""+safe+"\"}";
                String resp=callJson(endpoint,body); String[] parts=resp.split("\\|",2); int code=Integer.parseInt(parts[0]); String js=parts.length>1?parts[1]:"";
                if(code==200){ String clean=extractText(js); if(clean.length()<10) clean=js.substring(0,Math.min(400,js.length())); act.log(name+" 200 FINAL OK",0); act.log("FINAL: "+clean+"\nSUGGEST: Measure wall? Torch? Warn vehicles? Calculate area? Voice?",1); try{ FileOutputStream fos=new FileOutputStream(act.knowFile,true); fos.write(("{\"model\":\"FINAL\",\"learn\":\""+clean.replace("\"","'").substring(0,Math.min(180,clean.length()))+"\"}\n").getBytes()); fos.close(); }catch(Exception e){} } else { act.log(name+" "+code+" "+js.substring(0,80),0); }
                return;
            }
            model=MainActivity.MODELS[id]; name=MainActivity.NAMES[id];
            if(model.equals("muse-image-1.0")){
                endpoint="https://api.meta.ai/v1/images/generations"; body="{\"model\":\""+model+"\",\"prompt\":\""+safe+"\"}";
                String resp=callJson(endpoint,body); String[] parts=resp.split("\\|",2); int code=Integer.parseInt(parts[0]); if(code==200){ act.log(name+" 200 IMAGE OK",0); FusionManager.addObs(name,"AR image generated"); } return;
            } else if(model.equals("muse-voice-transcribe-1.0")){
                try{ File af=new File(act.audioPath); if(af.exists() && af.length()>1000){ String resp=callAudio("https://api.meta.ai/v1/audio/transcriptions",af); String[] parts=resp.split("\\|",2); int code=Integer.parseInt(parts[0]); if(code==200){ String js=parts.length>1?parts[1]:""; act.log(name+" 200 AUDIO OK",0); FusionManager.addObs(name,extractText(js)); return; } } }catch(Exception e){}
                body="{\"model\":\"muse-spark-1.3\",\"input\":\"VOICE PROXY "+safe+"\"}";
            } else if(model.equals("sam-3.1")){
                if(bmp!=null){ ByteArrayOutputStream baos=new ByteArrayOutputStream(); bmp.compress(Bitmap.CompressFormat.JPEG,65,baos); byte[] b=baos.toByteArray(); String b64=Base64.encodeToString(b,Base64.NO_WRAP); act.log(">> "+name+" FRAME "+b.length/1024+"KB SAM DOT",0); body="{\"model\":\""+model+"\",\"input\":[{\"role\":\"user\",\"content\":[{\"type\":\"input_text\",\"text\":\"SAM "+safe+"\"},{\"type\":\"input_image\",\"image_url\":\"data:image/jpeg;base64,"+b64+"\"}]}]}"; } else body="{\"model\":\""+model+"\",\"input\":\"SAM needs image\"}";
            } else if(model.equals("muse-glimmer-30b") || model.equals("muse-code") || model.equals("muse-spark-open")){
                String proxy="muse-spark-1.3"; if(bmp!=null){ ByteArrayOutputStream baos=new ByteArrayOutputStream(); bmp.compress(Bitmap.CompressFormat.JPEG,60,baos); byte[] bb=baos.toByteArray(); String b64=Base64.encodeToString(bb,Base64.NO_WRAP); body="{\"model\":\""+proxy+"\",\"input\":[{\"role\":\"user\",\"content\":[{\"type\":\"input_text\",\"text\":\""+name+" "+safe+"\"},{\"type\":\"input_image\",\"image_url\":\"data:image/jpeg;base64,"+b64+"\"}]}]}"; } else body="{\"model\":\""+proxy+"\",\"input\":\""+name+" "+safe+"\"}";
            } else {
                if(bmp!=null){ ByteArrayOutputStream baos=new ByteArrayOutputStream(); bmp.compress(Bitmap.CompressFormat.JPEG,60,baos); byte[] b=baos.toByteArray(); String b64=Base64.encodeToString(b,Base64.NO_WRAP); body="{\"model\":\""+model+"\",\"input\":[{\"role\":\"user\",\"content\":[{\"type\":\"input_text\",\"text\":\""+safe+"\"},{\"type\":\"input_image\",\"image_url\":\"data:image/jpeg;base64,"+b64+"\"}]}]}"; } else body="{\"model\":\""+model+"\",\"input\":\""+safe+"\"}";
            }
            String resp=callJson(endpoint,body); String[] parts=resp.split("\\|",2); int code=Integer.parseInt(parts[0]); String js=parts.length>1?parts[1]:"";
            if(code==200){ String clean=extractText(js); if(clean.length()<5) clean="OK"; act.log(name+" 200 OK",0); FusionManager.addObs(name,clean); } else { act.log(name+" "+code,0); FusionManager.addObs(name,"no see"); }
        }catch(Exception e){ act.log("ERR "+id+" "+e,0); FusionManager.addObs("ERR"+id,"err"); }
    }
}
