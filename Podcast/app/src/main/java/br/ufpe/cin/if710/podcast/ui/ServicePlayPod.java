package br.ufpe.cin.if710.podcast.ui;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;

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
    public int onStartCommand(Intent intent, int flags, int startId){
        String arq = intent.getExtras().getString("URI_ARQUIVO");
        arquivo = Uri.parse(arq);
        if(arquivo!=null){
            media_player = MediaPlayer.create(this,arquivo);
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
