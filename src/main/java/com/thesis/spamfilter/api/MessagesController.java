package com.thesis.spamfilter.api;

import com.thesis.spamfilter.model.Blacklist;
import com.thesis.spamfilter.model.BlacklistMsgEval;
import com.thesis.spamfilter.model.Messages;
import com.thesis.spamfilter.repository.BlacklistMsgEvalRepository;
import com.thesis.spamfilter.repository.MessagesRepository;
import com.thesis.spamfilter.repository.BlacklistRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1")
public class MessagesController {
    @Autowired
    private MessagesRepository messagesRepository;
    @Autowired
    private BlacklistRepository blacklistRepository;
    @Autowired
    private BlacklistMsgEvalRepository blacklistMsgEvalRepository;

   @GetMapping("/messages")
    public List<Messages> getAllMessages(){
       return messagesRepository.findAll();
   }

   @PostMapping("/messages/{type}")
    public String createMessage(@PathVariable(value = "type") int type, @Valid @RequestBody Messages message){
       messagesRepository.save(message);

       boolean is_spam = false;
       switch (type){
           case 0:
            is_spam = blacklistFilter(message);
           break;
       }
       return is_spam ? "The omitted message has been evaluated as spam."
               : "The omitted message has been evaluated as ham.";
   }

   private boolean blacklistFilter(Messages message){
       String[] msg = message.getMessage().split(" ");
       for(String m : msg) {
           List<Blacklist> word = blacklistRepository.findByToken(m);
           if (!word.isEmpty()) {
               BlacklistMsgEval blacklistMsgEval = new BlacklistMsgEval(
                       word.get(0).getId(),
                       message.getId(),
                       true
               );
               blacklistMsgEvalRepository.save(blacklistMsgEval);
               return true;
           }
       }
       return false;
   }
}
