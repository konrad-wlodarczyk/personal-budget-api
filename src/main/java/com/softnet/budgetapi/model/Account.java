package com.softnet.budgetapi.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String accountName;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    protected Account(){}

    public Account(String accountName, BigDecimal balance){
        this.accountName = accountName;
        this.balance = balance;
    }

    public String getAccountName(){return this.accountName;}
    public void setAccountName(String accountName){this.accountName = accountName;}

    public BigDecimal getBalance(){return this.balance;}
}


