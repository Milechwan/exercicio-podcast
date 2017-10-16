package br.ufpe.cin.if710.podcast.ui;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import br.ufpe.cin.if710.podcast.R;
import br.ufpe.cin.if710.podcast.db.PodcastDBHelper;
import br.ufpe.cin.if710.podcast.db.PodcastProviderContract;
import br.ufpe.cin.if710.podcast.domain.ItemFeed;
import br.ufpe.cin.if710.podcast.domain.XmlFeedParser;
import br.ufpe.cin.if710.podcast.ui.adapter.XmlFeedAdapter;


public class ServiceDownloadDB extends IntentService {

    public static final String ACTION_Download = "br.ufpe.cin.if710.podcast.ui.action.download";
    private static final String ACTION_Epi = "br.ufpe.cin.if710.podcast.ui.action.download_episode";

    //private Intent i;

    private static final String TAG_FEED = "Feed";
    private static final String TAG_EPISODE="Episode";
    private static final String RSS_FEED_URL = "http://leopoldomt.com/if710/fronteirasdaciencia.xml";//isso aqui é pra colcoar o link do feed
    //private static final String EXTRA_PARAM2 = "br.ufpe.cin.if710.podcast.ui.extra.PARAM2";
    List<ItemFeed> feed_itens;
    public ServiceDownloadDB() {
        super("ServiceDownloadDB");
    }

    public static void startActionFeed(Context context) {
        Intent intent = new Intent(context, ServiceDownloadDB.class);
        intent.setAction(ACTION_Download);
        intent.putExtra(TAG_FEED, RSS_FEED_URL);
        //intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    public static void startActionEpi(Context context, String param1) {
        Intent intent = new Intent(context, ServiceDownloadDB.class);
        intent.setAction(ACTION_Epi);
        intent.putExtra(TAG_EPISODE, param1);
        //intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_Download.equals(action)) {//nesta action, faz-se o download do feed
                final String param1 = intent.getStringExtra(TAG_FEED);
                handleActionDownload(param1);
                //daqui em diante, faz-se a inserção no banco de dados
                ContentResolver cr = getContentResolver(); //responsável por lidar com o PodcastProvider
                ContentValues cv = new ContentValues();

                for(int i=0;i<feed_itens.size();i++){
                    ItemFeed if_ = feed_itens.get(i);
                    cv.put(PodcastDBHelper.EPISODE_LINK,if_.getLink());
                    cv.put(PodcastDBHelper.EPISODE_DATE,if_.getPubDate());
                    cv.put(PodcastDBHelper.EPISODE_TITLE,if_.getTitle());
                    cv.put(PodcastDBHelper.EPISODE_DOWNLOAD_LINK,if_.getDownloadLink());
                    cv.put(PodcastDBHelper.EPISODE_DESC,if_.getDescription());

                    int upd = cr.update(PodcastProviderContract.EPISODE_LIST_URI,
                        cv,
                        PodcastDBHelper.EPISODE_DOWNLOAD_LINK+"=?",
                        new String[]{if_.getDownloadLink()});
                    if(upd==0){//se ele não encontrar na tabela nenhum item com tal link que seja atualizado, pode inserir
                        //a checagem é feita para evitar inserir itens já existentes no bd
                        cr.insert(PodcastProviderContract.EPISODE_LIST_URI,cv);
                    }
                    cv.clear();

                }
                //assim que terminar, manda brodcast para setar o adapter
                Intent in = new Intent("br.ufpe.cin.if710.podcast.service.download_done");
                sendBroadcast(in);


            } else if (ACTION_Epi.equals(action)) {
                final String param1 = intent.getStringExtra(TAG_EPISODE);
                File epi = baixaEpisodio(param1);
                ContentResolver cr = getContentResolver();
                ContentValues cv = new ContentValues();
                //como antes não era possível pegar a URI do arquivo, agora usa-se o método .toURI() e finalmente o banco pode possuir esse valor
                cv.put(PodcastDBHelper.EPISODE_FILE_URI,epi.toURI().toString());
                cr.update(PodcastProviderContract.EPISODE_LIST_URI,
                        cv,
                        PodcastDBHelper.EPISODE_DOWNLOAD_LINK+"=?",
                        new String[]{param1});
                sendBroadcast(new Intent("br.ufpe.cin.if710.podcast.service.download_done"));

            }
        }
    }

    private byte[] getDataFromURL(String feed) throws IOException { //método usado para download de qualquer tipo de arquivo

        InputStream in = null;
        byte[] response;
        try {

            URL url = new URL(feed);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            in = conn.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            for (int count; (count = in.read(buffer)) != -1; ) {
                out.write(buffer, 0, count);
            }

            response = out.toByteArray();

        } finally {
            if (in != null) {
                in.close();
            }
        }
       return response;
    }

    private File baixaEpisodio(String param){//método exclusivo para baixar episódio
        String[] splitar=param.split("/");
        String caminho = "podcasts_"+splitar[splitar.length-1];//como o download link pega o endereço completo, foi necessário usar split para ter o nome específico do arquivo
        InputStream in = null;
        FileOutputStream fos = null;
        HttpURLConnection conection = null;
        File f= null;

        try{
            URL url_ = new URL(param);
            conection = (HttpURLConnection) url_.openConnection();
            conection.connect();

            if(conection.getResponseCode()!= HttpURLConnection.HTTP_OK){
                Log.d("DEU_RUIM","Código HTTP "+conection.getResponseCode()+" "+conection.getResponseMessage());
            }

            f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),caminho);
            //uso de diretório de downloads pois em alguns aparelhos, não consegui encontrar a pasta de podcasts, ou dava falha de que tal diretório era inexistente
            fos = new FileOutputStream(f.getPath());
            BufferedOutputStream bos =  new BufferedOutputStream(fos);

            try{
                in = conection.getInputStream();
                byte[] buffer = new byte[8192];
                int tam = 0;

                while((tam = in.read(buffer))>0){
                    bos.write(buffer,0,tam);
                }

                bos.flush();
            }finally {
                fos.getFD().sync();
                bos.close();
                conection.disconnect();
                Log.d("DOWNLOAD_FILE","Acabei :D");
            }
        }catch (IOException e){
            e.printStackTrace();
        }

        return f;
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionDownload(String param1) {

        try{
            String dados = new String(getDataFromURL(param1),"UTF-8");
            feed_itens = XmlFeedParser.parse(dados);
        }catch (IOException e){
            e.printStackTrace();
        } catch (XmlPullParserException e1) {
            e1.printStackTrace();
        }

        //throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(BufferedOutputStream bos) {
        // TODO: Handle action Baz

        //throw new UnsupportedOperationException("Not yet implemented");
    }
}
