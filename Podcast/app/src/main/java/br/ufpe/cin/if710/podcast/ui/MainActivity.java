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

        private ViewHolder(View view){
            this.textViewTitle = (TextView) view.findViewById(R.id.item_title);
            this.textViewPubDate = (TextView) view.findViewById(R.id.item_date);
        }
    }

    private class AdapterItemdb extends CursorAdapter { //adapter customizado necessário para carregar os itens do db
        //passo 6
        ViewHolder vh;
        Context c;
        public AdapterItemdb(Context context, Cursor cursor){
            super(context, cursor, 0);
            c = context;
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

            vh.textViewTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) { //passo 5
                    Intent i = new Intent(c,EpisodeDetailActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//essa flag é necessária pois o listener está sendo instanciado
                    //no próprio adapter e não numa activity
                    i.putExtra("Titulo",cursor.getString(cursor.getColumnIndexOrThrow(PodcastDBHelper.EPISODE_TITLE)));
                    i.putExtra("Descricao",cursor.getString(cursor.getColumnIndexOrThrow(PodcastDBHelper.EPISODE_DESC)));
                    i.putExtra("PubDate",cursor.getString(cursor.getColumnIndexOrThrow(PodcastDBHelper.EPISODE_DATE)));
                    c.startActivity(i);
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        broad = new BroadcastDownload();
        IntentFilter filter = new IntentFilter("br.ufpe.cin.if710.podcast.service.download_done");
        registerReceiver(broad,filter);

    }

    @Override
    protected void onResume(){
        super.onResume();
        //chama método estático do service que é responsável pelo startService, que irá fazer o download do feed
        ServiceDownloadDB.startActionFeed(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(broad);

    }

  /*  private class DownloadXmlTask extends AsyncTask<String, Void, List<ItemFeed>> {
        @Override
        protected void onPreExecute() {
            Toast.makeText(getApplicationContext(), "iniciando...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected List<ItemFeed> doInBackground(String... params) {
            List<ItemFeed> itemList = new ArrayList<>();
            try {
                itemList = XmlFeedParser.parse(getRssFeed(params[0]));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }
            return itemList;
        }

        @Override
        protected void onPostExecute(List<ItemFeed> feed) {
            Toast.makeText(getApplicationContext(), "terminando...", Toast.LENGTH_SHORT).show();

            //Adapter Personalizado
            XmlFeedAdapter adapter = new XmlFeedAdapter(getApplicationContext(), R.layout.itemlista, feed);

            //atualizar o list view
            items.setAdapter(adapter);
            items.setTextFilterEnabled(true);

            //passo 3 e 4
            ContentResolver cr = getContentResolver();
            ContentValues cv = new ContentValues();

            for(int i=0;i<feed.size();i++){
                ItemFeed if_ = feed.get(i);
                cv.put(PodcastDBHelper.EPISODE_LINK,if_.getLink());
                cv.put(PodcastDBHelper.EPISODE_DATE,if_.getPubDate());
                cv.put(PodcastDBHelper.EPISODE_TITLE,if_.getTitle());
                cv.put(PodcastDBHelper.EPISODE_DOWNLOAD_LINK,if_.getDownloadLink());
                cv.put(PodcastDBHelper.EPISODE_DESC,if_.getDescription());
                //Log.d("CV",cv.toString());
                int x = helper.getWritableDatabase().update(PodcastDBHelper.DATABASE_TABLE,
                        cv,
                        PodcastDBHelper.EPISODE_DOWNLOAD_LINK+"=?",
                        new String[]{if_.getDownloadLink()});
                if(x==0) helper.getWritableDatabase().insert(PodcastDBHelper.DATABASE_TABLE,null,cv);
           {//se ele não encontrar na tabela nenhum item com tal link, pode inserir
            PodcastProvider pp = new PodcastProvider();            pp.setDb_help(getApplicationContext());
            PodcastDBHelper helper = pp.getDBhelper();
            ContentValues cv = new ContentValues();
            for(int i=0;i<feed.size();i++){
                ItemFeed if_ = feed.get(i);
                cv.put(PodcastDBHelper.EPISODE_LINK,if_.getLink());
                cv.put(PodcastDBHelper.EPISODE_DATE,if_.getPubDate());
                cv.put(PodcastDBHelper.EPISODE_TITLE,if_.getTitle());
                cv.put(PodcastDBHelper.EPISODE_DOWNLOAD_LINK,if_.getDownloadLink());
                cv.put(PodcastDBHelper.EPISODE_DESC,if_.getDescription());
                int x = helper.getWritableDatabase().update(PodcastDBHelper.DATABASE_TABLE,
                        cv,
                        PodcastDBHelper.EPISODE_DOWNLOAD_LINK+"=?",
                        new String[]{if_.getDownloadLink()});
                if(x==0) helper.getWritableDatabase().insert(PodcastDBHelper.DATABASE_TABLE,null,cv); //se x for 0, significa que nenhuma linha da tabela foi modificada
                cv.clear();
                //estou devendo pegar a URI

            }
            items.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    XmlFeedAdapter adapter = (XmlFeedAdapter) parent.getAdapter();
                    ItemFeed item = adapter.getItem(position);
                    String msg = item.getTitle() + " " + item.getLink();
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                }
            });

        }
    }
*/
  public class BroadcastDownload extends BroadcastReceiver{
        //passo 10
      //o broadcast é dinâmico, por ser instanciado na própria main activity
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
                //ao finalizar o download dos itens do feed, o broadcast receiver simplesmente seta o cursor adapter
                AdapterItemdb aidb = new AdapterItemdb(getApplicationContext(),c);
                items.setAdapter(aidb);

            }
        }));
  }

    //TODO Opcional - pesquise outros meios de obter arquivos da internet - não está sendo utilizado
    private String getRssFeed(String feed) throws IOException {
        InputStream in = null;
        String rssFeed = "";
        try {
            URL url = new URL(feed);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            in = conn.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            for (int count; (count = in.read(buffer)) != -1; ) {
                out.write(buffer, 0, count);
            }
            byte[] response = out.toByteArray();
            rssFeed = new String(response, "UTF-8");
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return rssFeed;
    }
}
