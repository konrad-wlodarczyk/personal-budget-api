package com.softnet.budgetapi.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TransactionType type;
    @Column(nullable = false, length = 50)
    private String category;
    @Column(length = 255)
    private String description;
    @Column(nullable = false)
    private LocalDateTime transactionDate;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    protected Transaction(){}

    public Transaction(BigDecimal amount, TransactionType type, String category, String description, Account account){
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.description = description;
        this.account = account;
        this.transactionDate = LocalDateTime.now();
    }

    public Transaction(BigDecimal amount, TransactionType type, String category, Account account){
        this(amount, type, category, null, account);
    }

    public Long getId(){return this.id;}
    public BigDecimal getAmount(){return this.amount;}
    public TransactionType getType(){return this.type;}
    public String getCategory(){return this.category;}
    public String getDescription(){return this.description;}
    public LocalDateTime getTransactionDate(){return this.transactionDate;}
    public Account getAccount(){return this.account;}

}
