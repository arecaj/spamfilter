package com.thesis.spamfilter.repository;

import com.thesis.spamfilter.model.Messages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessagesRepository  extends JpaRepository<Messages, Long> {
    List<Messages> findAllByNbIsSpamNotNull();
    int countAllByNbIsSpamIs(Boolean nbIsSpam);
    int countAllByNbIsSpamIsNotNull();
    int countAllByMessageContainingAndNbIsSpam(String token, Boolean isSpam);
}
