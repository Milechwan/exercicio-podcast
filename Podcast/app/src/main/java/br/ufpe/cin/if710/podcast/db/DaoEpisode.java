package br.ufpe.cin.if710.podcast.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

/**
 * Created by Walber on 12/12/2017.
 */
@Dao
public interface DaoEpisode {
    @Insert
    void insertMultipleRecord(Episode... episodes);

    @Insert
    void insertMultipleListRecord(List<Episode> episodeList);

    @Insert
    void insertOnlySingleRecord(Episode episode);

    @Query("SELECT * FROM Episode")
    List<Episode> fetchAllData();

    @Query("SELECT * FROM Episode WHERE title =:title")
    Episode getSingleRecord(String title);

    @Update
    void updateRecord(Episode episode);

    @Delete
    void deleteRecord(Episode episode);

}


