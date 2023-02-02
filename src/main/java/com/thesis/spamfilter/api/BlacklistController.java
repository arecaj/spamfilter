package com.thesis.spamfilter.api;

import com.thesis.spamfilter.model.Blacklist;
import com.thesis.spamfilter.model.BlacklistMsgEval;
import com.thesis.spamfilter.model.Messages;
import com.thesis.spamfilter.repository.BlacklistMsgEvalRepository;
import com.thesis.spamfilter.repository.BlacklistRepository;
import com.thesis.spamfilter.repository.MessagesRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/blacklist")
public class BlacklistController {
    @Autowired
    private BlacklistRepository blacklistRepository;
    @Autowired
    private MessagesRepository messagesRepository;
    @Autowired
    private BlacklistMsgEvalRepository blacklistMsgEvalRepository;
    private double count_spam = 0;

    @GetMapping("/words")
    public List<Blacklist> getAllWords(){
        return (List<Blacklist>) blacklistRepository.findAll();
    }

    //Berechnet den Anteil an (durch Blacklist-Filter erkanntem) Spam in allen Messages
    @GetMapping("/eval")
    public double evalBlacklist(){
        List<Messages> messages = messagesRepository.findAll();
        for (Messages msg: messages){
            List<BlacklistMsgEval> blacklistMsgEval = blacklistMsgEvalRepository.findByIdMsg(msg.getId());
            if(!blacklistMsgEval.isEmpty() && blacklistMsgEval.get(0).getIsSpam()) count_spam++;
        }
         return count_spam / messages.size() * 100;
    }

    @PostMapping("/words")
    public String createWord(@Valid @RequestBody Blacklist[] blacklist) {
        for (Blacklist bList: blacklist){
            //bList.setId(UUID.randomUUID());
            blacklistRepository.save(bList);
        }
        return "Words added to the Blacklist!";
    }

    @DeleteMapping("/words/{id}")
    public Map< String, Boolean > deleteWord(@PathVariable(value = "id") Long wordId)
            throws ResourceNotFoundException {
        Blacklist blacklist = blacklistRepository.findById(wordId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found for this id :: " + wordId));

        blacklistRepository.delete(blacklist);
        Map < String, Boolean > response = new HashMap< >();
        response.put("deleted", Boolean.TRUE);
        return response;
    }
}
