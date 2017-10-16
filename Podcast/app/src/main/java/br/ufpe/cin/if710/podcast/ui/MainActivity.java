package br.ufpe.cin.if710.podcast.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import br.ufpe.cin.if710.podcast.R;
import br.ufpe.cin.if710.podcast.db.PodcastDBHelper;
import br.ufpe.cin.if710.podcast.db.PodcastProvider;
import br.ufpe.cin.if710.podcast.db.PodcastProviderContract;
import br.ufpe.cin.if710.podcast.domain.ConexaoInternet;
import br.ufpe.cin.if710.podcast.domain.ItemFeed;
import br.ufpe.cin.if710.podcast.domain.XmlFeedParser;
import br.ufpe.cin.if710.podcast.ui.adapter.XmlFeedAdapter;

public class MainActivity extends Activity {

    //ao fazer envio da resolucao, use este link no seu codigo!
    private final String RSS_FEED = "http://leopoldomt.com/if710/fronteirasdaciencia.xml";
    //TODO teste com outros links de podcast
   // private static ServiceDownloadDB service_d;
    private ListView items;
    private BroadcastDownload broad;
    private Cursor c;
    private int posicaoClick;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        items = (ListView) findViewById(R.id.items);

        //service_d = new ServiceDownloadDB();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this,SettingsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    private class ViewHolder{ //usando padrão recomendado, contendo os textViews necessários na hora de alterar a view
        final TextView textViewTitle;
        final TextView textViewPubDate;
        final Button btn_down;

        private ViewHolder(View view){
            this.textViewTitle = (TextView) view.findViewById(R.id.item_title);
            this.textViewPubDate = (TextView) view.findViewById(R.id.item_date);
            this.btn_down = (Button) view.findViewById(R.id.item_action);
        }
    }

    private class AdapterItemdb extends CursorAdapter { //adapter necessário para carregar os itens do db
        //passo 6
        ViewHolder vh;
        Context cont;
       // Cursor cursor_glo;

        public AdapterItemdb(Context context, Cursor cursor){
            super(context, cursor, 0);
            cont = context;
        }
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.itemlista,parent,false); //usando o xml dado
        }


        @Override
        public void bindView(View view, Context context, final Cursor cursor) {
            vh = new ViewHolder(view); //padrão recomendado para o scroll
            view.setTag(vh); //amarrando à view
            String pubdate = cursor.getString(cursor.getColumnIndexOrThrow(PodcastDBHelper.EPISODE_DATE));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(PodcastDBHelper.EPISODE_TITLE));

            vh.textViewPubDate.setText(pubdate);
            vh.textViewTitle.setText(title);

            final int position = cursor.getPosition();//usado pois há diferença entre o índice do item clicado com o o índice apontado pelo cursor
            vh.textViewTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) { //passo 5
                    Intent i = new Intent(cont,EpisodeDetailActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//essa flag é necessária pois o listener está sendo instanciado
                    //no próprio adapter e não numa activity
                    cursor.moveToPosition(position);//força mudança de posição do cursor para pegar informações o item clicado
                    i.putExtra("Titulo",cursor.getString(cursor.getColumnIndexOrThrow(PodcastDBHelper.EPISODE_TITLE)));
                    i.putExtra("Descricao",cursor.getString(cursor.getColumnIndexOrThrow(PodcastDBHelper.EPISODE_DESC)));
                    i.putExtra("PubDate",cursor.getString(cursor.getColumnIndexOrThrow(PodcastDBHelper.EPISODE_DATE)));
                    cont.startActivity(i);//redireciona para activity EpisodeDetailActivity
                }
            });

            //aqui verifica quais itens já foram baixados, para que o texto do botão seja 'escutar' e ao clicá-lo,
            //iniciar o service do media player
            if(cursor.getString(cursor.getColumnIndexOrThrow(PodcastDBHelper.EPISODE_FILE_URI))!=null){
                //Toast.makeText(getApplicationContext(),"Baixado",Toast.LENGTH_SHORT).show();
                vh.btn_down.setText(R.string.muda_btn);
                vh.btn_down.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        cursor.moveToPosition(position);
                        Intent i = new Intent(cont,EscutarPodcast.class);
                        i.putExtra("URI_ARQ",cursor.getString(cursor.getColumnIndexOrThrow(PodcastDBHelper.EPISODE_FILE_URI)));
                        //repassa a string da URI do arquivo para a activity criada com os botões de play e pause do episódio
                        startActivity(i);
                    }
                });
            }else{
                vh.btn_down.setText(R.string.action_download);
                vh.btn_down.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        cursor.moveToPosition(position);

                        //inicia service que fará o download do .mp3
                        posicaoClick = position;
                        ServiceDownloadDB.startActionEpi(cont,cursor.getString(cursor.getColumnIndexOrThrow(PodcastDBHelper.EPISODE_DOWNLOAD_LINK)));
                        // Log.d("ID_ITEM_BTN",cursor.getString(cursor.getColumnIndexOrThrow(PodcastDBHelper._ID)));
                    }
                });
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //fazendo registro do receiver que diz respeito ao download do feed
        broad = new BroadcastDownload();
        IntentFilter filter = new IntentFilter("br.ufpe.cin.if710.podcast.service.download_done");
        registerReceiver(broad,filter);

    }

    @Override
    protected void onResume(){
        super.onResume();

        try {//trocar para ver conexão com a internet
            ContentResolver cr = getContentResolver();
            c = cr.query(PodcastProviderContract.EPISODE_LIST_URI, new String[]{}, null, new String[]{}, null);
            if (c.getCount() == 0 || ConexaoInternet.conectado(this)) { //o uso de getCount seria apenas para quando o aplicativo for executado pela primeira vez
               //chama método estático do service que é responsável pelo startService, que irá fazer o download do feed
                Toast.makeText(this,"Baixando o feed!",Toast.LENGTH_SHORT).show();
                ServiceDownloadDB.startActionFeed(this);
            }else{
                //se não estiver conectado, apenas seta o adapter para que seja exibido o que já foi baixado (e já está no banco)
                AdapterItemdb adapt = new AdapterItemdb(this,c);
                items.setAdapter(adapt);
            }

        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        c.close();//fechando cursor usado no onResume aqui, pois se fosse feito no onPause, causaria problemas sobre usar cursor já fechado
        unregisterReceiver(broad);//tirando o registro do receiver

    }

  @Override
  public void onPause(){
      super.onPause();
  }

  public class BroadcastDownload extends BroadcastReceiver{
        //passo 10
      //o receiver é dinâmico, por ser instanciado na própria main activity
      public BroadcastDownload(){

      }
      @Override
      public void onReceive(Context context, Intent intent) {
          runThread();
      }
  }

  public void runThread(){
        runOnUiThread(new Thread(new Runnable() {
            @Override
            public void run() throws NullPointerException {
                ContentResolver cr = getContentResolver();
                Cursor c = cr.query(PodcastProviderContract.EPISODE_LIST_URI,
                        PodcastDBHelper.columns,
                        null,
                        new String[]{},
                        null);

                AdapterItemdb aidb = new AdapterItemdb(getApplicationContext(),c);
                items.setAdapter(aidb);
                //como as duas actions implementadas no ServiceDownloadDB enviam broadcast para um mesmo receiver e ele só executa uma função
                //a partir desse try, ele verifica se o episódio foi baixado para que um toast seja exibido
                try{
                    c.moveToPosition(posicaoClick);
                    String pos = posicaoClick+"";
                    c = cr.query(PodcastProviderContract.EPISODE_LIST_URI,
                            new String[]{PodcastDBHelper.EPISODE_FILE_URI},
                            PodcastDBHelper._ID+"=?",
                            new String[]{pos},
                            null);
                    if(c.moveToFirst()) {
                        Toast.makeText(getApplicationContext(),"Episódio "+posicaoClick+" baixado!",Toast.LENGTH_SHORT).show();
                    }
                }catch (NullPointerException e){
                    e.printStackTrace();
                }

            }
        }));
  }

}
