package com.thesis.spamfilter.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name="NAIVE_BAYES_WORDS")
public class NaiveBayesWords {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private int idNbs;
    private String word;
    private int spamCount;
    private int hamCount;
    private double probSpam;
    private double probHam;

    public NaiveBayesWords(){

    }

    public NaiveBayesWords(String word, int spamCount, int hamCount, double probSpam, double probHam){
        this.word = word;
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

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public int getIdNbs() {
        return idNbs;
    }

    public void setIdNbs(int idNbs) {
        this.idNbs = idNbs;
    }
}
