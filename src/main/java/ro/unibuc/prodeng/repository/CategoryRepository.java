package ro.unibuc.prodeng.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import ro.unibuc.prodeng.model.CategoryEntity;

public interface CategoryRepository extends MongoRepository<CategoryEntity, String> {
    
}
