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
    default NaiveBayesTokens findByTokenOrCreate(String token) {
        NaiveBayesTokens naiveBayesToken = findByToken(token);
        if (naiveBayesToken == null){
            naiveBayesToken = new NaiveBayesTokens();
            naiveBayesToken.setToken(token);
            save(naiveBayesToken);
        }
        return naiveBayesToken;

    }
    @Query("SELECT sum(spamCount) FROM NaiveBayesTokens")
    int sumSpamToken();
    @Query("SELECT sum(hamCount) FROM NaiveBayesTokens")
    int sumHamToken();
}
