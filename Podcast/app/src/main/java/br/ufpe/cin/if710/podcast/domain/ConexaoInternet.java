package br.ufpe.cin.if710.podcast.domain;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Milena on 12/10/2017.
 */

public class ConexaoInternet {

//classe com método para checar se o usuário está de fato conectado à internet

    public static boolean conectado(Context context){
        final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        //pega manager que trata de conectividade em geral do aparelho, como wifi, 3g, bluetooth etc
        NetworkInfo net_info = cm.getActiveNetworkInfo();//pega qual rede está sendo usada no momento
        if(net_info!=null){//caso haja rede ativa, verifica se é wifi ou 3g
            if(net_info.getType()== ConnectivityManager.TYPE_WIFI){
                return true;
            }else if(net_info.getType()==ConnectivityManager.TYPE_MOBILE){//diz respeito à plano de dados da operadora, como 3g
                return true;
            }
        }
        return false;
    }
}
