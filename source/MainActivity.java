package com.nino.seen19.real;
import android.app.Activity; import android.content.pm.PackageManager; import android.graphics.Bitmap; import android.graphics.SurfaceTexture; import android.hardware.Camera; import android.media.MediaRecorder; import android.os.Bundle; import android.view.TextureView; import android.view.View; import android.widget.Button; import android.widget.EditText; import android.widget.LinearLayout; import android.widget.ScrollView; import android.widget.TextView; import java.io.File; import java.io.FileInputStream;
public class MainActivity extends Activity implements TextureView.SurfaceTextureListener, View.OnClickListener {
    public static final String[] MODELS={"muse-spark-1.3","muse-spark-1.3-contributor","muse-spark-1.2","muse-spark-1.2-contributor","muse-spark-1.1","muse-wonder-1.0","muse-image-1.0","muse-voice-transcribe-1.0","sam-3.1","muse-glimmer-30b","muse-code","muse-spark-open"};
    public static final String[] NAMES={"SPARK-1.3-EYE","CONTRIB-LABEL","SPARK-1.2-MEASURE","SPARK-1.2C-CALC","SPARK-1.1-SCENE","WONDER-REASON","IMAGE-GEN","VOICE-TRANSCRIBE","SAM-3.1-DOT","GLIMMER-30B","MUSE-CODE","SPARK-OPEN"};
    public static final String[] TASKS={"EYE spark-1.3 see live","LABEL contrib label","MEASURE spark-1.2 measure","CALC contrib calc","SCENE spark-1.1 scene","REASON wonder reasoning","IMAGE image gen AR","VOICE voice-transcribe AUDIO RECORD_AUDIO /v1/audio/transcriptions","SAM sam-3.1 DOT requires image we send image","GLIMMER glimmer-30b open-weight Apache 2.0","CODE muse-code terminal Wonder Full product not API","OPEN spark-open 816B MoE"};
    public TextureView preview; public Camera cam; public Bitmap lastBmp; public TextView logBox, chatBox, sigBox; public EditText chatInput; public String apiKey="KEY_REMOVED_USE_mtm_key_txt"; public boolean torchOn=false, videoEyesOn=false, recording=false; public Button bTorch,bSnap,bSend,bVideo,bFix,bVoice; public MediaRecorder recorder; public String audioPath; public static MainActivity inst; public File knowFile;
    protected void onCreate(Bundle b){
        super.onCreate(b); inst=this; audioPath=getFilesDir()+"/voice.3gp";
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(0xFF0A0A0A);
        preview=new TextureView(this); root.addView(preview,new LinearLayout.LayoutParams(-1,0,3));
        LinearLayout split=new LinearLayout(this); split.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout logPanel=new LinearLayout(this); logPanel.setOrientation(LinearLayout.VERTICAL); logPanel.setBackgroundColor(0xFF111111);
        logBox=new TextView(this); logBox.setText("SEEN 19 FIXED NO BUG\n"); logBox.setTextColor(0xFF00FF88); logBox.setBackgroundColor(0xFF111111); logBox.setTextSize(7);
        ScrollView logSv=new ScrollView(this); logSv.addView(logBox); logPanel.addView(logSv,new LinearLayout.LayoutParams(-1,-1,1));
        LinearLayout chatPanel=new LinearLayout(this); chatPanel.setOrientation(LinearLayout.VERTICAL); chatPanel.setBackgroundColor(0xFF1A1A1A);
        chatBox=new TextView(this); chatBox.setText("AI: Hello Siimamkele\n"); chatBox.setTextColor(0xFFFFFFFF); chatBox.setBackgroundColor(0xFF1A1A1A); chatBox.setTextSize(9);
        ScrollView chatSv=new ScrollView(this); chatSv.addView(chatBox); chatPanel.addView(chatSv,new LinearLayout.LayoutParams(-1,-1,1));
        split.addView(logPanel,new LinearLayout.LayoutParams(0,-1,1)); split.addView(chatPanel,new LinearLayout.LayoutParams(0,-1,1));
        root.addView(split,new LinearLayout.LayoutParams(-1,0,3));
        LinearLayout chatRow=new LinearLayout(this); chatRow.setOrientation(LinearLayout.HORIZONTAL); chatInput=new EditText(this); chatInput.setHint("Ask eye..."); chatInput.setBackgroundColor(0xFF2A2A2A); chatInput.setTextColor(0xFFFFFFFF); bSend=new Button(this); bSend.setText("SEND"); bSend.setBackgroundColor(0xFF0066FF); bSend.setTextColor(0xFFFFFFFF); bSend.setOnClickListener(this); chatRow.addView(chatInput,new LinearLayout.LayoutParams(0,-2,3)); chatRow.addView(bSend,new LinearLayout.LayoutParams(0,-2,1)); root.addView(chatRow,new LinearLayout.LayoutParams(-1,-2));
        LinearLayout r1=new LinearLayout(this); r1.setOrientation(LinearLayout.HORIZONTAL); bTorch=new Button(this); bTorch.setText("TORCH"); bTorch.setBackgroundColor(0xFF333333); bTorch.setTextColor(0xFFFFFFFF); bTorch.setOnClickListener(this); bSnap=new Button(this); bSnap.setText("SNAP 12"); bSnap.setBackgroundColor(0xFF333333); bSnap.setTextColor(0xFFFFFFFF); bSnap.setOnClickListener(this); bVideo=new Button(this); bVideo.setText("VIDEO OFF"); bVideo.setBackgroundColor(0xFF0044AA); bVideo.setTextColor(0xFFFFFFFF); bVideo.setOnClickListener(this); bVoice=new Button(this); bVoice.setText("VOICE OFF"); bVoice.setBackgroundColor(0xFFAA3300); bVoice.setTextColor(0xFFFFFFFF); bVoice.setOnClickListener(this); r1.addView(bTorch,new LinearLayout.LayoutParams(0,-2,1)); r1.addView(bSnap,new LinearLayout.LayoutParams(0,-2,1)); r1.addView(bVideo,new LinearLayout.LayoutParams(0,-2,1)); r1.addView(bVoice,new LinearLayout.LayoutParams(0,-2,1)); root.addView(r1,new LinearLayout.LayoutParams(-1,-2));
        bFix=new Button(this); bFix.setText("FIX CAM"); bFix.setBackgroundColor(0xFF333333); bFix.setTextColor(0xFFFFFFFF); bFix.setOnClickListener(this); root.addView(bFix,new LinearLayout.LayoutParams(-1,-2));
        sigBox=new TextView(this); sigBox.setText("created by SIIMAMKELE NGXONONO from KIRKWOOD"); sigBox.setTextColor(0xFF00DDFF); sigBox.setTextSize(9); sigBox.setTextAlignment(View.TEXT_ALIGNMENT_CENTER); sigBox.setBackgroundColor(0xFF111133); root.addView(sigBox,new LinearLayout.LayoutParams(-1,-2));
        setContentView(root); preview.setSurfaceTextureListener(this); knowFile=new File(getFilesDir(),"SEEN-knowledge.json"); loadKey(); checkPerm(); initKnowledge();
    }
    void checkPerm(){ try{ if(checkSelfPermission(android.Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED || checkSelfPermission(android.Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED) requestPermissions(new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO},1); }catch(Exception e){} }
    void loadKey(){
        try{
            File f=new File("/sdcard/Download/mtm_key.txt");
            if(f.exists()){
                FileInputStream fis=new FileInputStream(f);
                int sz=(int)f.length();
                byte[] buf=(byte[])java.lang.reflect.Array.newInstance(byte.class, sz);
                int r=fis.read(buf);
                fis.close();
                String k=new String(buf,0,r).trim();
                if(k.length()>20) apiKey=k;
            }
        }catch(Exception e){}
        log("KEY "+apiKey.substring(0,12),0);
    }
    void initKnowledge(){ try{ if(!knowFile.exists()){ java.io.FileOutputStream fos=new java.io.FileOutputStream(knowFile); fos.write("{\"created\":\"by SIIMAMKELE NGXONONO\"}\n".getBytes()); fos.close(); } log("knowledge.json PRIVATE OK",0); }catch(Exception e){} }
    public void log(String s,int t){ try{ runOnUiThread(new LogRunnable(this,s,t)); }catch(Exception e){} }
    public void onClick(View v){ if(v==bTorch) torch(); else if(v==bSnap) snap(); else if(v==bSend) omni(); else if(v==bFix) fix(); else if(v==bVideo) toggleVideo(); else if(v==bVoice) toggleVoice(); }
    void toggleVoice(){ try{ if(!recording){ recorder=new MediaRecorder(); recorder.setAudioSource(MediaRecorder.AudioSource.MIC); recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP); recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB); recorder.setOutputFile(audioPath); recorder.prepare(); recorder.start(); recording=true; bVoice.setText("VOICE ON"); bVoice.setBackgroundColor(0xFFFF0000); log("VOICE RECORDING",0); } else { try{ recorder.stop(); }catch(Exception e){} try{ recorder.release(); }catch(Exception e){} recorder=null; recording=false; bVoice.setText("VOICE OFF"); bVoice.setBackgroundColor(0xFFAA3300); log("VOICE SAVED "+audioPath,0); Thread th=new Thread(new ModelCaller(7,lastBmp,"voice",this,apiKey)); th.start(); } }catch(Exception e){ log("VOICE ERR "+e,0); } }
    void toggleVideo(){ videoEyesOn=!videoEyesOn; bVideo.setText(videoEyesOn?"VIDEO ON":"VIDEO OFF"); if(videoEyesOn){ Thread t=new Thread(new VideoLoop(this)); t.start(); } }
    void fix(){ try{ if(cam!=null){ cam.stopPreview(); cam.release(); } cam=null; Thread.sleep(200); }catch(Exception e){} startCam(); }
    void startCam(){ try{ if(cam!=null){ cam.stopPreview(); cam.release(); cam=null; } try{ cam=Camera.open(); }catch(Exception e){ cam=Camera.open(0); } cam.setPreviewTexture(preview.getSurfaceTexture()); Camera.Parameters p=cam.getParameters(); try{ p.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO); }catch(Exception e){} try{ cam.setParameters(p); }catch(Exception e){} cam.setDisplayOrientation(90); cam.startPreview(); log("CAM READY",0); }catch(Exception e){ log("CAM ERR "+e,0); } }
    void torch(){ try{ Camera.Parameters p=cam.getParameters(); p.setFlashMode(!torchOn?Camera.Parameters.FLASH_MODE_TORCH:Camera.Parameters.FLASH_MODE_OFF); torchOn=!torchOn; cam.setParameters(p); }catch(Exception e){} }
    void snap(){ if(lastBmp==null){ log("NO FRAME",0); return; } for(int i=0;i<12;i++){ Thread th=new Thread(new ModelCaller(i,lastBmp,TASKS[i],this,apiKey)); th.start(); } }
    void omni(){ String pr=""; try{ pr=chatInput.getText().toString(); }catch(Exception e){} if(pr==null || pr.length()<1) pr="test"; log("User: "+pr,1); for(int i=0;i<12;i++){ Thread th=new Thread(new ModelCaller(i,lastBmp,TASKS[i]+" "+pr,this,apiKey)); th.start(); } chatInput.setText(""); }
    public void onSurfaceTextureAvailable(SurfaceTexture s,int w,int h){ startCam(); }
    public void onSurfaceTextureSizeChanged(SurfaceTexture s,int w,int h){}
    public boolean onSurfaceTextureDestroyed(SurfaceTexture s){ videoEyesOn=false; if(cam!=null){ try{ cam.stopPreview(); cam.release(); }catch(Exception e){} cam=null; } return true; }
    public void onSurfaceTextureUpdated(SurfaceTexture s){ try{ if(preview.isAvailable()) lastBmp=preview.getBitmap(640,480); }catch(Exception e){} }
}
