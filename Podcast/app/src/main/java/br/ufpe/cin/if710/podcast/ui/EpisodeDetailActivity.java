package br.ufpe.cin.if710.podcast.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import br.ufpe.cin.if710.podcast.R;

public class EpisodeDetailActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episode_detail);

        TextView tv_titulo = (TextView)findViewById(R.id.titulo_epi);
        TextView tv_desc = (TextView)findViewById(R.id.desc_epi);
        TextView tv_pubdate = (TextView)findViewById(R.id.pubdate_epi);

        Intent i = getIntent();
        tv_titulo.setText(i.getStringExtra("Titulo"));
        tv_desc.setText(i.getStringExtra("Descricao"));
        tv_pubdate.setText(i.getStringExtra("PubDate"));
        //TODO preencher com informações do episódio clicado na lista...
    }
}
