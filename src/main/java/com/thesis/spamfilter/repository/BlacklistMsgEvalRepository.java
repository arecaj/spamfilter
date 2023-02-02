package com.thesis.spamfilter.repository;

import com.thesis.spamfilter.model.BlacklistMsgEval;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BlacklistMsgEvalRepository extends JpaRepository<BlacklistMsgEval, Long> {
    List<BlacklistMsgEval> findByIdMsg(int idMsg);
}
