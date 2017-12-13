package br.ufpe.cin.if710.podcast.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

/**
 * Created by Walber on 12/12/2017.
 */
@Database(entities = {Episode.class}, version = 1)
public abstract class EpisodeDatabase extends RoomDatabase{
    public abstract DaoEpisode daoAccess();
}
