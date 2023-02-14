package com.thesis.spamfilter.api;

import com.thesis.spamfilter.model.Messages;
import com.thesis.spamfilter.model.NaiveBayesWords;
import com.thesis.spamfilter.repository.MessagesRepository;
import com.thesis.spamfilter.repository.NaiveBayesWordsRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/v1/naive-bayes")
public class NaiveBayesTokensController {
    @Autowired
    private NaiveBayesTokensRepository naiveBayesTokensRepository;
    @Autowired
    private MessagesRepository messagesRepository;
    private final List<String> stopWords;
    {
        try {
            stopWords = Files.readAllLines(Path.of("german_stopwords_plain.txt"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/init")
    public String initNaiveBayes(){
        if ((int)naiveBayesTokensRepository.count() > 0) {
            return "Naive Bayes is already initialized. Use \"/reset\" to reset Naive Bayes";
        }

        List<Messages> messages = messagesRepository.findAllByNbIsSpamNotNull();
        for (Messages message: messages){
            String[] msg = message.getMessage().split(" ");
            saveTokens(msg, message.getNbIsSpam());
        }

        for (NaiveBayesWords naiveBayesWord: naiveBayesTokensRepository.findAll()){
            calcProb(naiveBayesWord);
        }

        return "Naive Bayes has been initialized!";
    }

    @PostMapping("/create")
    public HashMap<String, String> createMessage(@Valid @RequestBody Messages[] messages){
        HashMap<String,String> output = new HashMap<>();
        for (Messages message: messages){
            double countMsg = messagesRepository.countAllByNbIsSpamIsNotNull();
            double probIsSpamMsg = messagesRepository.countAllByNbIsSpamIs(true) / countMsg;
            double probIsHamMsg = messagesRepository.countAllByNbIsSpamIs(false) / countMsg;
            String[] words = message.getMessage().split(" ");
            double probSpam = probIsSpamMsg,probHam = probIsHamMsg;

            for (String word: words){
                NaiveBayesWords naiveBayesWord = naiveBayesTokensRepository.findByWord(word);
                if(naiveBayesWord == null) continue;
                probSpam *= naiveBayesWord.getProbSpam();
                probHam *= naiveBayesWord.getProbHam();
            }
            output.put(message.getMessage(),(probSpam > probHam) ? "Spam" : "Ham");
            message.setNbIsSpam(probSpam > probHam);
            message = messagesRepository.save(message);
            saveTokens(message.getMessage().split(" "), message.getNbIsSpam());
        }

        for (NaiveBayesWords naiveBayesWord: naiveBayesTokensRepository.findAll()){
            calcProb(naiveBayesWord);
        }
        return output;
    }

    private void calcProb(NaiveBayesWords naiveBayesWord) {
        Long spamWordCount = naiveBayesTokensRepository.sumSpam();
        Long hamWordCount = naiveBayesTokensRepository.sumHam();
        double spamCount = naiveBayesWord.getSpamCount();
        double hamCount = naiveBayesWord.getHamCount();
        naiveBayesWord.setProbSpam( spamCount / spamWordCount );
        naiveBayesWord.setProbHam( hamCount / hamWordCount );
        naiveBayesTokensRepository.save(naiveBayesWord);
    }

    private void saveTokens(String[] tokens, Boolean isSpam){
        for (String token: tokens){
            if(stopWords.contains(token)) continue;
            token = token.toLowerCase();
            NaiveBayesTokens naiveBayesToken = naiveBayesTokensRepository.findByToken(token);
            if(naiveBayesToken == null){
                NaiveBayesTokens newNaiveBayesToken = new NaiveBayesTokens(
                    token,
                    isSpam ? 2 : 1,
                    isSpam ? 1 : 2,
                    0,
                    0
                );
                naiveBayesTokensRepository.save(newNaiveBayesToken);
            } else {
                if (isSpam) {
                    naiveBayesToken.increaseSpamCount();
                } else {
                    naiveBayesToken.increaseHamCount();
                }
                naiveBayesTokensRepository.save(naiveBayesToken);
            }
        }
    }

    @GetMapping("/reset")
    public String resetNaiveBayes(){
        naiveBayesTokensRepository.deleteAll();
        return "Naive Bayes successfully resetted";
    }
}
