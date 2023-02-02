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
public class NaiveBayesWordsController {
    @Autowired
    private NaiveBayesWordsRepository naiveBayesWordsRepository;
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
        if ((int)naiveBayesWordsRepository.count() > 0) {
            return "Naive Bayes is already initialized. Use \"/reset\" to reset Naive Bayes";
        }

        List<Messages> messages = messagesRepository.findAllByNbIsSpamNotNull();
        for (Messages message: messages){
            String[] msg = message.getMessage().split(" ");
            saveWords(msg, message.getNbIsSpam());
        }

        for (NaiveBayesWords naiveBayesWord: naiveBayesWordsRepository.findAll()){
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
                NaiveBayesWords naiveBayesWord = naiveBayesWordsRepository.findByWord(word);
                if(naiveBayesWord == null) continue;
                probSpam *= naiveBayesWord.getProbSpam();
                probHam *= naiveBayesWord.getProbHam();
            }
            output.put(message.getMessage(),(probSpam > probHam) ? "Spam" : "Ham");
            message.setNbIsSpam(probSpam > probHam);
            message = messagesRepository.save(message);
            saveWords(message.getMessage().split(" "), message.getNbIsSpam());
        }

        for (NaiveBayesWords naiveBayesWord: naiveBayesWordsRepository.findAll()){
            calcProb(naiveBayesWord);
        }
        return output;
    }

    private void calcProb(NaiveBayesWords naiveBayesWord) {
        Long spamWordCount = naiveBayesWordsRepository.sumSpam();
        Long hamWordCount = naiveBayesWordsRepository.sumHam();
        double spamCount = naiveBayesWord.getSpamCount();
        double hamCount = naiveBayesWord.getHamCount();
        naiveBayesWord.setProbSpam( spamCount / spamWordCount );
        naiveBayesWord.setProbHam( hamCount / hamWordCount );
        naiveBayesWordsRepository.save(naiveBayesWord);
    }

    private void saveWords(String[] words, Boolean isSpam){
        for (String word: words){
            if(stopWords.contains(word)) continue;
            word = word.toLowerCase();
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
