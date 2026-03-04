package ro.unibuc.prodeng.service;

import org.springframework.stereotype.Service;

import ro.unibuc.prodeng.repository.ExpenseRepository;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    
    public ExpenseService(ExpenseRepository expenseRepository){
        this.expenseRepository= expenseRepository;
    }

    
}
