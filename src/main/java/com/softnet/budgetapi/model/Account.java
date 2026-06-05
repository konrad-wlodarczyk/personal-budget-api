package com.softnet.budgetapi.model;

import com.softnet.budgetapi.exception.BusinessException;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    protected Account(){}

    public Account(String name){
        this.name = name;
    }

    public Long getId(){return this.id;}

    public String getName(){return this.name;}
    public void setName(String name){this.name = name;}

    public BigDecimal getBalance(){return this.balance;}

    public void deposit(BigDecimal amount){
        if(amount.compareTo(BigDecimal.ZERO)<=0){
            throw new BusinessException("The deposit amount has to be a positive number");
        } else {
            this.balance = this.balance.add(amount);
        }
    }

    public void withdraw(BigDecimal amount){
        if(amount.compareTo(BigDecimal.ZERO)<=0){
            throw new BusinessException("The withdrawal amount has to be a positive number");
        } else if(amount.compareTo(this.balance)>0){
            throw new BusinessException("Insufficient balance for the withdrawal operation");
        } else {
            this.balance = this.balance.subtract(amount);
        }
    }
}


