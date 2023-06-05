package com.thesis.spamfilter.api;

import com.thesis.spamfilter.model.Messages;
import com.thesis.spamfilter.model.NaiveBayesTokens;
import com.thesis.spamfilter.repository.MessagesRepository;
import com.thesis.spamfilter.repository.NaiveBayesTokensRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/naive-bayes")
public class NaiveBayesTokensController {
    @Autowired
    private NaiveBayesTokensRepository naiveBayesTokensRepository;
    @Autowired
    private MessagesRepository messagesRepository;
    @Value("classpath:german_stopwords.txt")
    private Resource stopWords;
    public String readStopWords() throws IOException{
        String data = new String(stopWords.getInputStream().readAllBytes());
        return data;
    }
    private String[] regex = {"(?<=\\d)[.,](?=\\d)|(?<=[\\w-'])\\s+|\\s+(?=[\\w-'])|\\s+"};
    private double threshold = 0.9;

    @GetMapping("/init")
    public String initNaiveBayes() throws IOException {
        if ((int)naiveBayesTokensRepository.count() > 0) {
            return "Naive Bayes is already initialized. Use \"/api/v1/naive-bayes/reset\" to reset Naive Bayes.";
        }

        List<Messages> messages = messagesRepository.findAllByNbIsSpamNotNull();
        for (Messages message: messages){
            String[] msg = message.getMessage().split(regex[0]);
            saveTokens(msg, message.getNbIsSpam());
        }
        for (NaiveBayesTokens naiveBayesToken: naiveBayesTokensRepository.findAll()){
            calcProb(naiveBayesToken);
        }

        return "Naive Bayes has been initialized!";
    }

    @PostMapping("/create/{type}")
    public HashMap<String, String> createMessage(@PathVariable(value = "type") int type , @Valid @RequestBody Messages[] messages) throws IOException {
        //HashMap<String,String> output = new HashMap<>();
        HashMap<String, String> output = null;
        switch (type) {
            case 0:
                // multinomialer Naive Bayes
                output = multinomialNB(messages);
                break;
            case 1:
                //Bernoulli Naive Bayes
                output = bernoulliNB(messages);
                break;
            case 2:
                //Gaußscher Naive Bayes
                output = gaussianNB(messages);
        }

        return output;
    }

    private HashMap<String, String> gaussianNB(Messages[] messages) {
        HashMap<String,String> output = new HashMap<>();
        return output;
    }

    private HashMap<String, String> multinomialNB(Messages[] messages) throws IOException {
        HashMap<String,String> output = new HashMap<>();
        for (Messages message: messages){
            double countMsg = messagesRepository.countAllByNbIsSpamIsNotNull();
            double probIsSpamMsg = messagesRepository.countAllByNbIsSpamIs(true) / countMsg;
            double probIsHamMsg = messagesRepository.countAllByNbIsSpamIs(false) / countMsg;
            String[] tokens = message.getMessage().split(" ");
            double probSpam = probIsSpamMsg,probHam = probIsHamMsg;

            for (String token: tokens){
                NaiveBayesTokens naiveBayesToken = naiveBayesTokensRepository.findByToken(token.toLowerCase());
                if(naiveBayesToken == null) continue;
                probSpam *= naiveBayesToken.getProbSpam();
                probHam *= naiveBayesToken.getProbHam();
            }
            output.put(message.getMessage(),(probSpam >= probHam) ? "Spam" : "Ham");
            message.setNbIsSpam(probSpam > probHam);
            message = messagesRepository.save(message);
            saveTokens(message.getMessage().split(" "), message.getNbIsSpam());
        }

        for (NaiveBayesTokens naiveBayesToken: naiveBayesTokensRepository.findAll()){
            calcProb(naiveBayesToken);
        }
        return output;
    }

    private HashMap<String, String> bernoulliNB(Messages[] messages) throws IOException {
        HashMap<String,String> output = new HashMap<>();
        for (Messages message: messages){
            List<String> tokens = List.of(message.getMessage().split("regex[0]")); // nicht optimal
            HashMap<String,Integer> tokenIsInMessage = new HashMap<>();
            List<NaiveBayesTokens> naiveBayesTokens = naiveBayesTokensRepository.findAll();
            for (NaiveBayesTokens naiveBayesToken: naiveBayesTokens){
                String token = naiveBayesToken.getToken();
                tokenIsInMessage.put(token,tokens.contains(token) ? 1 : 0);
            }
            double probMsgIsSpam = 1,probMsgIsHam = 1;
            for (Map.Entry<String, Integer> entry : tokenIsInMessage.entrySet()) {
                String key = entry.getKey();
                Integer value = entry.getValue();
                double probTokenSpam = (1d +messagesRepository.countAllByMessageContainingAndNbIsSpam(key,true)) / 2d+messagesRepository.countAllByNbIsSpamIs(true);
                double probTokenHam = (1d +messagesRepository.countAllByMessageContainingAndNbIsSpam(key,false)) / 2d+messagesRepository.countAllByNbIsSpamIs(false);
                probMsgIsSpam *= Math.pow(probTokenSpam,value) * Math.pow(1-probTokenSpam, 1-value);
                probMsgIsHam *= Math.pow(probTokenHam,value) * Math.pow(1-probTokenHam, 1-value);
            }
            if (probMsgIsSpam > probMsgIsHam){
                output.put(message.getMessage(),"Is Spam");
            } else output.put(message.getMessage(),"Is Ham");
            //generateTokens(message.getMessage(), message.getNbIsSpam());
        }
        return output;
    }

    private void calcProb(NaiveBayesTokens naiveBayesToken) {
        Long spamWordCount = naiveBayesTokensRepository.sumSpam();
        Long hamWordCount = naiveBayesTokensRepository.sumHam();
        double spamCount = naiveBayesToken.getSpamCount();
        double hamCount = naiveBayesToken.getHamCount();

        naiveBayesToken.setProbSpam( spamCount / spamWordCount );
        naiveBayesToken.setProbHam( hamCount / hamWordCount );
        naiveBayesTokensRepository.save(naiveBayesToken);
    }

    private void saveTokens(String[] tokens, Boolean isSpam) throws IOException {
        for (String token: tokens){
            if(readStopWords().contains(token)) continue;
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

    private void generateTokens(String message, Boolean isSpam) throws IOException {
        String[] tokens = message.split(regex[0]); // nicht optimal
        NaiveBayesTokens newToken;
        for(String token: tokens){
            if(token.isEmpty() || readStopWords().contains(token)) continue;
            NaiveBayesTokens naiveBayesToken = naiveBayesTokensRepository.findByToken(token);
            if(naiveBayesToken == null){
            newToken = new NaiveBayesTokens(token,0,0,0,0);
            naiveBayesTokensRepository.save(newToken);
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
