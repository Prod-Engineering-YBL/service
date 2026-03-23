package ro.unibuc.prodeng.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import ro.unibuc.prodeng.model.CategoryEntity;

public interface CategoryRepository extends MongoRepository<CategoryEntity, String> {
	Optional<CategoryEntity> findByName(String name);
	Optional<CategoryEntity> findByAssignedUserId(String assignedUserId);
	long countByAssignedUserId(String assignedUserId);
}
