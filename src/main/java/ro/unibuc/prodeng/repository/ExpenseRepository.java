package ro.unibuc.prodeng.repository;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import ro.unibuc.prodeng.model.ExpenseEntity;

@Repository
public interface ExpenseRepository extends MongoRepository<ExpenseEntity, String> {
    //de adaugat dupa ce se adauga in service
    
}
