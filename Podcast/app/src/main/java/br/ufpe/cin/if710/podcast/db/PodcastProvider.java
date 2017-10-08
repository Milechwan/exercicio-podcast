package br.ufpe.cin.if710.podcast.db;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class PodcastProvider extends ContentProvider {
    PodcastDBHelper db_help;
    Cursor c;
    Uri u;
    int index_del;

    public PodcastProvider() {

    }
    //métodos implementados para passo 4
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) throws NullPointerException{
        // Implement this to handle requests to delete one or more rows.
        if(uri.equals(PodcastProviderContract.EPISODE_LIST_URI)){
            index_del = db_help.getWritableDatabase().delete(PodcastDBHelper.DATABASE_TABLE,selection,selectionArgs);
        }else{
            index_del = -1;//caso a deleção falhe, retornar um número negativo para identificar
        }
        //throw new UnsupportedOperationException("Not yet implemented");
        return index_del;
    }

    /*public void setDb_help(Context c){
        db_help = PodcastDBHelper.getInstance(c);
    }*/

/*    public PodcastDBHelper getDBhelper(){
        return db_help;
    }*/

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) throws NullPointerException{
        //filtrando a Uri apenas por garantia
        if(uri.equals(PodcastProviderContract.EPISODE_LIST_URI)){
            long id = db_help.getWritableDatabase().insert(PodcastDBHelper.DATABASE_TABLE,null,values);
            u = ContentUris.withAppendedId(PodcastProviderContract.EPISODE_LIST_URI,id);
        }

        return u;
    }

    @Override
    public boolean onCreate() {
        //instanciar o database helper para manipular os dados
        db_help = PodcastDBHelper.getInstance(getContext());
        Log.d("ON_CREATE", "executou onCreate do PodcastProvider");
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) throws NullPointerException {
        // TODO: Implement this to handle query requests from clients.
        //ainda precisa alterar essa query!!
        c = getContext().getContentResolver().query(uri,projection,selection,selectionArgs,sortOrder);
        //throw new UnsupportedOperationException("Not yet implemented");
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        if(uri.equals(PodcastProviderContract.EPISODE_LIST_URI)){
            return db_help.getWritableDatabase().update(PodcastDBHelper.DATABASE_TABLE,values,selection,selectionArgs);
        }
        return -1;
    }
}
