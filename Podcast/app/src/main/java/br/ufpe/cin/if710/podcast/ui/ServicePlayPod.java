package br.ufpe.cin.if710.podcast.ui;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.File;

public class ServicePlayPod extends Service {

    public final String TAG = "PlayerPodcast";
    private MediaPlayer media_player;
    private int mp_ID;
    private Uri arquivo;
    private final IBinder my_binder = new BinderPodcast();
    //private Intent i;


    @Override
    public void onCreate(){
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, final int startId){
        String arq = intent.getExtras().getString("URI_ARQUIVO");
        Log.d("ARQUIVO ON_START_CMD",arq);
        arquivo = Uri.parse(arq);

        boolean b = arquivo!=null;
        if(b){
            arq = "true";
        }else{
            arq = "false";
        }
        Log.d("ARQUI NULO ON_START_CMD",arq);
        //o media player só é criado aqui pois precisa do extra do intent para pegar a URI do arquivo
        if(arquivo!=null){
            media_player = MediaPlayer.create(this,arquivo);
            media_player.setLooping(false);
            //o OnCompletionListener é justamente para saber se a música que está sendo tocada acabou, e poder excluir o arquivo em seguida
            media_player.setOnCompletionListener( new MediaPlayer.OnCompletionListener(){
                public void onCompletion(MediaPlayer mp){
                    stopSelf(startId);
                    media_player.release();//dando release para que não fique em aberto
                    media_player = null;
                    File f = new File(arquivo.getPath());//pegando o arquivo para deletá-lo
                    boolean isDeleted = f.delete();
                }
            });
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        if(media_player!=null){
            media_player.stop();
            media_player.release();
        }
    }

    public void playEpi(){
        if(!media_player.isPlaying()){
            media_player.start();
        }
    }

    public void pauseEpi(){
        if(media_player.isPlaying()){
            media_player.pause();
        }
    }

    public class BinderPodcast extends Binder{
        ServicePlayPod getService(){
            return ServicePlayPod.this;
        }
    }


  //  private final IBinder binder_mp
    @Override
    public IBinder onBind(Intent intent) {
       return my_binder;
    }
}
