package com.thesis.spamfilter.repository;

import com.thesis.spamfilter.model.NaiveBayesWords;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NaiveBayesTokensRepository extends JpaRepository<NaiveBayesTokens, Long> {
    NaiveBayesTokens findByToken(String token);
    @Query("SELECT sum(spamCount) FROM NaiveBayesTokens")
    Long sumSpam();
    @Query("SELECT sum(hamCount) FROM NaiveBayesTokens")
    Long sumHam();
}
