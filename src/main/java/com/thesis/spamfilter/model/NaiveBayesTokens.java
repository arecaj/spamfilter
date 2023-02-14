package com.thesis.spamfilter.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name="NAIVE_BAYES_TOKENS")
public class NaiveBayesTokens {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private int idToken;
    private String token;
    private int spamCount;
    private int hamCount;
    private double probSpam;
    private double probHam;

    public NaiveBayesTokens(){

    }

    public NaiveBayesTokens(String token, int spamCount, int hamCount, double probSpam, double probHam){
        this.token = token;
        this.spamCount = spamCount;
        this.hamCount = hamCount;
        this.probSpam = probSpam;
        this.probHam = probHam;
    }

    public double getProbHam() {
        return probHam;
    }

    public void setProbHam(double probHam) {
        this.probHam = probHam;
    }

    public double getProbSpam() {
        return probSpam;
    }

    public void setProbSpam(double probSpam) {
        this.probSpam = probSpam;
    }

    public int getHamCount() {
        return hamCount;
    }

    public void setHamCount(int hamCount) {
        this.hamCount = hamCount;
    }

    public int increaseHamCount() { return this.hamCount++; }

    public int getSpamCount() {
        return spamCount;
    }

    public void setSpamCount(int spamCount) {
        this.spamCount = spamCount;
    }

    public int increaseSpamCount() { return this.spamCount++; }

    public String getToken() {
        return token;
    }

    public void setWord(String token) {
        this.token = token;
    }

    public int getIdToken() {
        return idToken;
    }

    public void setidToken(int idToken) {
        this.idToken = idToken;
    }
}
