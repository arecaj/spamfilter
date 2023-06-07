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
        return new String(stopWords.getInputStream().readAllBytes());
    }
    private final String[] regex = {"(?<=\\d)[.,](?=\\d)|(?<=[\\w-'])\\s+|\\s+(?=[\\w-'])|\\s+", " "};
    private final double threshold = 0.9;

    @GetMapping("/init/{type}")
    public String initNaiveBayes(@PathVariable(value = "type") int type) throws IOException {
        if ((int)naiveBayesTokensRepository.count() > 0) {
            return "Naive Bayes is already initialized. Use \"/api/v1/naive-bayes/reset\" to reset Naive Bayes.";
        }
        List<Messages> messages = messagesRepository.findAllByNbIsSpamNotNull();
        for (Messages message: messages){
            String[] msg = message.getMessage().split(regex[0]);
            saveTokens(msg, message.getNbIsSpam());
        }

        return switch (type) {
            case 0 ->
                // mnb
                    initMNB();
            case 1 ->
                // bnb
                    initBNB();
            case 2 ->
                // gnb
                    initGNB();
            default -> "Initialization failed!";
        };
    }

    private String initGNB() {

        return "Gaussian Naive Bayes has been initialized!";
    }

    private String initBNB() {
        for (NaiveBayesTokens naiveBayesToken: naiveBayesTokensRepository.findAll()){
            int totalMsgInSpamWithToken = messagesRepository.countAllByNbIsSpamIsAndMessageContaining(true, naiveBayesToken.getToken());
            int totalMsgInHamWithToken = messagesRepository.countAllByNbIsSpamIsAndMessageContaining(false, naiveBayesToken.getToken());
            int totalMsgInSpam = messagesRepository.countAllByNbIsSpamIs(true);
            int totalMsgInHam = messagesRepository.countAllByNbIsSpamIs(false);

            naiveBayesToken.setProbSpam((double) totalMsgInSpamWithToken / totalMsgInSpam);
            naiveBayesToken.setProbHam((double) totalMsgInHamWithToken / totalMsgInHam);
            naiveBayesTokensRepository.save(naiveBayesToken);
        }
        return "Bernoulli Naive Bayes has been initialized!";
    }

    private String initMNB() {
        for (NaiveBayesTokens naiveBayesToken: naiveBayesTokensRepository.findAll()){
//            Long totalTokenInSpam = naiveBayesTokensRepository.sumSpam();
//            Long totalTokenInHam = naiveBayesTokensRepository.sumHam();

            int totalMsgInSpam = messagesRepository.countAllByNbIsSpamIs(true);
            int totalMsgInHam = messagesRepository.countAllByNbIsSpamIs(false);
            double countTokenInSpam = naiveBayesToken.getSpamCount();
            double countTokenInHam = naiveBayesToken.getHamCount();

            naiveBayesToken.setProbSpam( countTokenInSpam / totalMsgInSpam );
            naiveBayesToken.setProbHam( countTokenInHam / totalMsgInHam );
            naiveBayesTokensRepository.save(naiveBayesToken);
        }
        return "Multinomial Naive Bayes has been initialized!";
    }

    @PostMapping("/create/{type}")
    public HashMap<String, String> createMessage(@PathVariable(value = "type") int type , @Valid @RequestBody Messages[] messages) throws IOException {
        //HashMap<String,String> output = new HashMap<>();
        HashMap<String, String> output = switch (type) {
            case 0 ->
                // multinomialer Naive Bayes
                    multinomialNB(messages);
            case 1 ->
                //Bernoulli Naive Bayes
                    bernoulliNB(messages);
            case 2 ->
                //Gaußscher Naive Bayes
                    gaussianNB(messages);
            default -> null;
        };

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
            double probSpamMsg = messagesRepository.countAllByNbIsSpamIs(true) / countMsg;
            double probHamMsg = messagesRepository.countAllByNbIsSpamIs(false) / countMsg;
            String[] tokens = message.getMessage().split(regex[0]);
            double probMsgInSpam = 1,probMsgInHam = 1;

            for (String token: tokens){
                NaiveBayesTokens naiveBayesToken = naiveBayesTokensRepository.findByToken(token.toLowerCase());
                if(naiveBayesToken == null) continue;
                probMsgInSpam *= naiveBayesToken.getProbSpam();
                probMsgInHam *= naiveBayesToken.getProbHam();
            }

            double probMsg = (probMsgInSpam * probSpamMsg) + (probMsgInHam * probHamMsg);
            double probSpam = (probMsgInSpam * probSpamMsg) / probMsg;
            double probHam = (probMsgInHam * probHamMsg) / probMsg;

            Boolean decision = probSpam >= threshold;
            output.put(message.getMessage(),decision ? "Spam" : "Ham");
            message.setNbIsSpam(decision);
            //message = messagesRepository.save(message);
            //saveTokens(message.getMessage().split(" "), message.getNbIsSpam());
        }

//        for (NaiveBayesTokens naiveBayesToken: naiveBayesTokensRepository.findAll()){
//            calcProbMNB(naiveBayesToken);
//        }
        return output;
    }

    private HashMap<String, String> bernoulliNB(Messages[] messages) throws IOException {
        HashMap<String,String> output = new HashMap<>();
        for (Messages message: messages){
            List<String> tokens = List.of(message.getMessage().split(regex[0])); // nicht optimal
            HashMap<String,Integer> tokenIsInMessage = new HashMap<>();
            List<NaiveBayesTokens> naiveBayesTokens = naiveBayesTokensRepository.findAll();
            for (NaiveBayesTokens naiveBayesToken: naiveBayesTokens){
                String token = naiveBayesToken.getToken();
                tokenIsInMessage.put(token,tokens.contains(token) ? 1 : 0);
            }
            double probMsgIsSpam = 0,probMsgIsHam = 0;
            for (Map.Entry<String, Integer> entry : tokenIsInMessage.entrySet()) {
                String key = entry.getKey();
                Integer value = entry.getValue();
                double thetaSpam = naiveBayesTokensRepository.findByToken(key).getProbSpam();
                double thetaHam = naiveBayesTokensRepository.findByToken(key).getProbHam();

//                double probTokenSpam = (1d +messagesRepository.countAllByNbIsSpamIsAndMessageContaining(true, key)) / 2d+messagesRepository.countAllByNbIsSpamIs(true);
//                double probTokenHam = (1d +messagesRepository.countAllByNbIsSpamIsAndMessageContaining(false, key)) / 2d+messagesRepository.countAllByNbIsSpamIs(false);
                probMsgIsSpam += Math.log(Math.pow(thetaSpam,value)) + Math.log(Math.pow(1-thetaSpam, 1-value));
                probMsgIsHam += Math.log(Math.pow(thetaHam,value)) + Math.log(Math.pow(1-thetaHam, 1-value));
            }
            if (probMsgIsSpam > probMsgIsHam){
                output.put(message.getMessage(),"Is Spam");
            } else output.put(message.getMessage(),"Is Ham");
            //generateTokens(message.getMessage(), message.getNbIsSpam());
        }
        return output;
    }

//    private void calcProbMNB(NaiveBayesTokens naiveBayesToken) {
//        Long totalTokenInSpam = naiveBayesTokensRepository.sumSpam();
//        Long totalTokenInHam = naiveBayesTokensRepository.sumHam();
//        double countTokenInSpam = naiveBayesToken.getSpamCount();
//        double countTokenInHam = naiveBayesToken.getHamCount();
//
//        naiveBayesToken.setProbSpam( countTokenInSpam / totalTokenInSpam );
//        naiveBayesToken.setProbHam( countTokenInHam / totalTokenInHam );
//        naiveBayesTokensRepository.save(naiveBayesToken);
//    }

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
