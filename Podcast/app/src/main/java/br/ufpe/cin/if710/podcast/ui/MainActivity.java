package br.ufpe.cin.if710.podcast.ui;

import android.app.Activity;
import android.arch.persistence.room.Room;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import br.ufpe.cin.if710.podcast.db.Episode;
import br.ufpe.cin.if710.podcast.db.EpisodeDatabase;
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
    private int posicaoClick;
    private List<Episode> episodes;
    private EpisodeDatabase episodioDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        episodioDatabase = Room.databaseBuilder(getApplicationContext(),
                EpisodeDatabase.class, "episode-db").build();

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

//    private class ViewHolder{ //usando padrão recomendado, contendo os textViews necessários na hora de alterar a view
//        final TextView textViewTitle;
//        final TextView textViewPubDate;
//        final Button btn_down;
//
//        private ViewHolder(View view){
//            this.textViewTitle = (TextView) view.findViewById(R.id.item_title);
//            this.textViewPubDate = (TextView) view.findViewById(R.id.item_date);
//            this.btn_down = (Button) view.findViewById(R.id.item_action);
//        }
//    }

    private class AdapterItemdb extends ArrayAdapter<Episode> { //adapter necessário para carregar os itens do db
        //passo 6
        ViewHolder vh;
        Context cont;
       // Cursor cursor_glo;
        int view;

        public AdapterItemdb(Context context, int i, List<Episode> episodes){
            super(context, i, episodes);
            view = i;
        }
        class ViewHolder {
            TextView item_title;
            TextView item_date;
            Button btn_down;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent){
            final ViewHolder viewHolder;
            if (convertView == null) {
                convertView = View.inflate(getContext(), view, null);
                viewHolder = new ViewHolder();
                viewHolder.item_title = (TextView) convertView.findViewById(R.id.item_title);
                viewHolder.item_date = (TextView) convertView.findViewById(R.id.item_date);
                viewHolder.btn_down = (Button)convertView.findViewById(R.id.item_action);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.item_title.setText(getItem(position).getTitle());
            viewHolder.item_date.setText(getItem(position).getPubDate());
            viewHolder.item_title.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) { //passo 5
                            Intent i = new Intent(cont,EpisodeDetailActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//essa flag é necessária pois o listener está sendo instanciado
                            //no próprio adapter e não numa activity
                            i.putExtra("Titulo",getItem(position).getTitle());
                            i.putExtra("Descricao",getItem(position).getDescription());
                            i.putExtra("PubDate",getItem(position).getPubDate());
                            cont.startActivity(i);//redireciona para activity EpisodeDetailActivity
                        }
                    });

            if(getItem(position).getDownloadUri()!=null){
                viewHolder.btn_down.setEnabled(true);
                viewHolder.btn_down.setText("Escutar");
            }else{
                //https://stackoverflow.com/questions/41256172/changing-one-item-only-in-a-listview-or-recycleview
                viewHolder.btn_down.setText("Download");
            }

            viewHolder.btn_down.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    if(getItem(position).getDownloadUri()!=null){
                        viewHolder.btn_down.setText(R.string.muda_btn);
                        viewHolder.btn_down.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent i = new Intent(cont,EscutarPodcast.class);
                                i.putExtra("URI_ARQ", getItem(position).getDownloadUri());
                                //repassa a string da URI do arquivo para a activity criada com os botões de play e pause do episódio
                                startActivity(i);
                            }
                        });
                    }else{
                        vh.btn_down.setText(R.string.action_download);
                        vh.btn_down.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                posicaoClick = position;
                                ServiceDownloadDB.startActionEpi(cont,getItem(position).getDownloadUri());
                                // Log.d("ID_ITEM_BTN",cursor.getString(cursor.getColumnIndexOrThrow(PodcastDBHelper._ID)));
                            }
                        });
                    }
                }


            });
            return convertView;
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        //fazendo registro do receiver que diz respeito ao download do feed
        broad = new BroadcastDownload();
        IntentFilter filter = new IntentFilter("br.ufpe.cin.if710.podcast.service.download_done");
        registerReceiver(broad,filter);
        Log.d(">>>>>>>>>>>>>>>>>", "Registei ao Service");

    }

    @Override
    protected void onResume(){
        super.onResume();

        try {//trocar para ver conexão com a internet

            if (episodes == null && ConexaoInternet.conectado(this)) { //o uso de getCount seria apenas para quando o aplicativo for executado pela primeira vez
               //chama método estático do service que é responsável pelo startService, que irá fazer o download do feed
                Toast.makeText(this,"Baixando o feed!",Toast.LENGTH_SHORT).show();
                ServiceDownloadDB.startActionFeed(this);
            }else{
                //se não estiver conectado, apenas seta o adapter para que seja exibido o que já foi baixado (e já está no banco)
                if(!ConexaoInternet.conectado(this)) {
                    AdapterItemdb adapt = new AdapterItemdb(this,R.layout.itemlista,episodes);
                    items.setAdapter(adapt);
                }
            }

        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
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
          (new FillEpisodeList()).execute();
      }
  }

    private class FillEpisodeList extends AsyncTask<String, Void, List<Episode>> {
        @Override
        protected void onPreExecute() {
            Toast.makeText(getApplicationContext(), "iniciando...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected List<Episode> doInBackground(String... params) {
            episodes = episodioDatabase.daoAccess().fetchAllData();
            return episodes;
        }

        @Override
        protected void onPostExecute(List<Episode> feed) {
            Toast.makeText(getApplicationContext(), "terminando...", Toast.LENGTH_SHORT).show();

            //Adapter Personalizado
            AdapterItemdb adapter = new AdapterItemdb(getApplicationContext(), R.layout.itemlista, episodes);

            //atualizar o list view
            items.setAdapter(adapter);
            items.setTextFilterEnabled(true);
            /*
            items.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    XmlFeedAdapter adapter = (XmlFeedAdapter) parent.getAdapter();
                    ItemFeed item = adapter.getItem(position);
                    String msg = item.getTitle() + " " + item.getLink();
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                }
            });
            /**/
        }
    }
}
