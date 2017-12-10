package br.ufpe.cin.if710.podcast.application;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * Created by Milena on 10/12/2017.
 */

public class Aplicacao extends Application {
    @Override
    public void onCreate(){
        super.onCreate();
        /*if(LeakCanary.isInAnalyzerProcess(this)){
            return;
        }*/
        LeakCanary.install(this);
    }

}
