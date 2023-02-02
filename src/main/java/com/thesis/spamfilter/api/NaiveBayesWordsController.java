package com.thesis.spamfilter.api;

import com.thesis.spamfilter.model.BlacklistMsgEval;
import com.thesis.spamfilter.model.Messages;
import com.thesis.spamfilter.model.NaiveBayesWords;
import com.thesis.spamfilter.repository.MessagesRepository;
import com.thesis.spamfilter.repository.NaiveBayesWordsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/naive-bayes")
public class NaiveBayesWordsController {
    @Autowired
    private NaiveBayesWordsRepository naiveBayesWordsRepository;
    @Autowired
    private MessagesRepository messagesRepository;

    @GetMapping("/init")
    public String initNaiveBayes(){
        if ((int)naiveBayesWordsRepository.count() > 0) {
            return "Naive Bayes is already initialized. Use \"/reset\" to reset Naive Bayes";
        }
        List<Messages> messages = messagesRepository.findAllByNbIsSpamNotNull();
        for (Messages message: messages){
            String[] msg = message.getMessage().split(" ");
            saveWords(msg, message.getNbIsSpam());
            initProbs();
        }

        return "Naive Bayes has been initialized!";
    }

    private void initProbs() {
        List<NaiveBayesWords> naiveBayesWords = naiveBayesWordsRepository.findAll();
        for (NaiveBayesWords naiveBayesWord: naiveBayesWords){
            Long spamWordCount = naiveBayesWordsRepository.sumSpam();
            Long hamWordCount = naiveBayesWordsRepository.sumHam();
            double spamCount = naiveBayesWord.getSpamCount();
            double hamCount = naiveBayesWord.getHamCount();
            naiveBayesWord.setProbSpam( spamCount / spamWordCount );
            naiveBayesWord.setProbHam( hamCount / hamWordCount );
            naiveBayesWordsRepository.save(naiveBayesWord);
        }
    }

    private void saveWords(String[] words, Boolean isSpam){
        for (String word: words){
            NaiveBayesWords naiveBayesWord = naiveBayesWordsRepository.findByWord(word);
            if(naiveBayesWord == null){
                NaiveBayesWords newNaiveBayesWord = new NaiveBayesWords(
                    word,
                    isSpam ? 2 : 1,
                    isSpam ? 1 : 2,
                    0,
                    0
                );
                naiveBayesWordsRepository.save(newNaiveBayesWord);
            } else {
                if (isSpam) {
                    naiveBayesWord.increaseSpamCount();
                } else {
                    naiveBayesWord.increaseHamCount();
                }
                naiveBayesWordsRepository.save(naiveBayesWord);
            }
        }
    }

    @GetMapping("/reset")
    public String resetNaiveBayes(){
        naiveBayesWordsRepository.deleteAll();
        return "Naive Bayes successfully resetted";
    }
}
