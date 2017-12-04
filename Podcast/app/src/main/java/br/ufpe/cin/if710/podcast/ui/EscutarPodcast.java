package br.ufpe.cin.if710.podcast.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import br.ufpe.cin.if710.podcast.R;

public class EscutarPodcast extends Activity {

    ServicePlayPod spp;
    Button btn_play;
    Button btn_pause;
    String ext;
    boolean isBound = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_episode);
        btn_pause = (Button) findViewById(R.id.botaoPause);
        //iniciar service que trata o media player
        Intent i = getIntent();
        ext = i.getExtras().getString("URI_ARQ");
        final Intent musicIntent = new Intent(this, ServicePlayPod.class);
        musicIntent.putExtra("URI_ARQUIVO",ext);
        stopService(musicIntent);
        startService(musicIntent);

        btn_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String auxBtn = btn_pause.getText().toString();
                if(isBound){
                    if(auxBtn.equals("Pausar")){
                        spp.pauseEpi();
                        btn_pause.setText("Tocar");
                    }else if(auxBtn.equals("Tocar")) {
                        spp.playEpi();
                        btn_pause.setText("Pausar");
                    }
                }else{
                    Toast.makeText(getApplicationContext(),"Não tem bind ainda :<", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private ServiceConnection serv_conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            spp = ((ServicePlayPod.BinderPodcast)iBinder).getService();
            isBound=true;
            spp.playEpi();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            spp = null;
            isBound=false;
        }
    };

    @Override
    protected void onResume(){
        super.onResume();
        if(!isBound){
            Toast.makeText(this,"Binding...",Toast.LENGTH_LONG).show();
            Intent bindIntent = new Intent(this,ServicePlayPod.class);
            bindIntent.putExtra("URI_ARQUIVO",ext);
            isBound = bindService(bindIntent,serv_conn, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onStop(){
        Toast.makeText(this,"Unbinding...",Toast.LENGTH_LONG).show();
        unbindService(serv_conn);
        isBound=false;
        super.onStop();
    }
}
