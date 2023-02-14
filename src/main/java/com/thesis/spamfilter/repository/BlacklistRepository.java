package com.thesis.spamfilter.repository;

import com.thesis.spamfilter.model.Blacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlacklistRepository  extends JpaRepository<Blacklist, Long> {
    List<Blacklist> findByToken(String token);
}
