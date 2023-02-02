package com.thesis.spamfilter.repository;

import com.thesis.spamfilter.model.NaiveBayesWords;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NaiveBayesWordsRepository extends JpaRepository<NaiveBayesWords, Long> {
    NaiveBayesWords findByWord(String words);
    @Query("SELECT sum(spamCount) FROM NaiveBayesWords")
    Long sumSpam();
    @Query("SELECT sum(hamCount) FROM NaiveBayesWords")
    Long sumHam();
}
