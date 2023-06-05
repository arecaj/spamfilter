package com.thesis.spamfilter.repository;

import com.thesis.spamfilter.model.NaiveBayesTokens;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NaiveBayesTokensRepository extends JpaRepository<NaiveBayesTokens, Long> {
    NaiveBayesTokens findByToken(String token);
    Optional<NaiveBayesTokens> findByX(String x);

    default NaiveBayesTokens findOrCreateByToken(String token) {
        return findByToken(token).orElseGet(() -> {
            NaiveBayesTokens naiveBayesToken = new NaiveBayesTokens();
            naiveBayesToken.setWord(token);
            return save(naiveBayesToken);
        });
    }
    @Query("SELECT sum(spamCount) FROM NaiveBayesTokens")
    Long sumSpam();
    @Query("SELECT sum(hamCount) FROM NaiveBayesTokens")
    Long sumHam();
}
