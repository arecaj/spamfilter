package com.thesis.spamfilter.model;

import jakarta.persistence.*;
import org.springframework.data.relational.core.mapping.Table;

@Entity
@Table(name = "BLACKLIST")
public class Blacklist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private int idBl;
    private String word;

    public Blacklist() {
    }

    public Blacklist(String word){
        this.word = word;
    }

    public void setId(int idBl) {
        this.idBl = idBl;
    }

    public int getId() {
        return idBl;
    }

    @Column(name = "word", nullable = false)
    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }
}
