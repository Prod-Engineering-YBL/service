package ro.unibuc.prodeng.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import ro.unibuc.prodeng.model.CategoryEntity;
import ro.unibuc.prodeng.repository.CategoryRepository;
import ro.unibuc.prodeng.request.CreateCategoryRequest;
import ro.unibuc.prodeng.response.CategoryResponse;
import ro.unibuc.prodeng.exception.EntityNotFoundException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class CategoryServiceTest {

	@Mock
	private CategoryRepository categoryRepository;

	@InjectMocks
	private CategoryService categoryService;

	@Test
	void testGetAllCategories_withMultipleCategories_returnsAllCategories() {
		// Arrange
		List<CategoryEntity> categories = Arrays.asList(
				new CategoryEntity("1", "Work", "user-1"),
				new CategoryEntity("2", "Personal", "user-2")
		);
		when(categoryRepository.findAll()).thenReturn(categories);

		// Act
		List<CategoryResponse> result = categoryService.getAllCategories();

		// Assert
		assertEquals(2, result.size());
		assertEquals("Work", result.get(0).name());
		assertEquals("Personal", result.get(1).name());
	}

	@Test
	void testGetCategoryById_existingCategoryRequested_returnsCategory() {
		// Arrange
		CategoryEntity category = new CategoryEntity("1", "Work", "user-1");
		when(categoryRepository.findById("1")).thenReturn(Optional.of(category));

		// Act
		CategoryResponse result = categoryService.getCategoryById("1");

		// Assert
		assertNotNull(result);
		assertEquals("1", result.id());
		assertEquals("Work", result.name());
		assertEquals("user-1", result.assignedUserId());
	}

	@Test
	void testGetCategoryById_nonExistingCategoryRequested_throwsEntityNotFoundException() {
		// Arrange
		when(categoryRepository.findById("999")).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(EntityNotFoundException.class, () -> categoryService.getCategoryById("999"));
	}

	@Test
	void testCreateCategory_newCategoryWithValidData_createsAndReturnsCategory() {
		// Arrange
		CreateCategoryRequest request = new CreateCategoryRequest("Work", "user-1");
		when(categoryRepository.findByName("Work")).thenReturn(Optional.empty());
		when(categoryRepository.save(any(CategoryEntity.class))).thenAnswer(invocation -> {
			CategoryEntity entity = invocation.getArgument(0);
			String id = "generated-category-id";
			return new CategoryEntity(id, entity.name(), entity.assignedUserId());
		});

		// Act
		CategoryResponse result = categoryService.createCategory(request);

		// Assert
		assertNotNull(result);
		assertNotNull(result.id());
		assertEquals("Work", result.name());
		assertEquals("user-1", result.assignedUserId());
		verify(categoryRepository, times(1)).save(any(CategoryEntity.class));
	}

	@Test
	void testCreateCategory_existingCategoryName_throwsIllegalArgumentException() {
		// Arrange
		CreateCategoryRequest request = new CreateCategoryRequest("Work", "user-1");
		when(categoryRepository.findByName("Work"))
				.thenReturn(Optional.of(new CategoryEntity("1", "Work", "user-2")));

		// Act & Assert
		assertThrows(IllegalArgumentException.class, () -> categoryService.createCategory(request));
		verify(categoryRepository, never()).save(any(CategoryEntity.class));
	}

	@Test
	void testChangeName_existingCategoryRequested_changesNameSuccessfully() {
		// Arrange
		CategoryEntity existing = new CategoryEntity("1", "Work", "user-1");
		when(categoryRepository.findById("1")).thenReturn(Optional.of(existing));
		when(categoryRepository.save(any(CategoryEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// Act
		CategoryResponse result = categoryService.changeName("1", "Updated Work");

		// Assert
		assertNotNull(result);
		assertEquals("1", result.id());
		assertEquals("Updated Work", result.name());
		assertEquals("user-1", result.assignedUserId());
	}

	@Test
	void testChangeName_nonExistingCategoryRequested_throwsEntityNotFoundException() {
		// Arrange
		when(categoryRepository.findById("999")).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(EntityNotFoundException.class, () -> categoryService.changeName("999", "Updated"));
	}

	@Test
	void testDeleteCategory_existingCategoryRequested_deletesSuccessfully() {
		// Arrange
		when(categoryRepository.existsById("1")).thenReturn(true);

		// Act
		categoryService.deleteCategory("1");

		// Assert
		verify(categoryRepository, times(1)).deleteById("1");
	}

	@Test
	void testDeleteCategory_nonExistingCategoryRequested_throwsEntityNotFoundException() {
		// Arrange
		when(categoryRepository.existsById("999")).thenReturn(false);

		// Act & Assert
		assertThrows(EntityNotFoundException.class, () -> categoryService.deleteCategory("999"));
	}

	@Test
	void testAssign_existingCategoryRequested_assignsUserSuccessfully() {
		// Arrange
		CategoryEntity existing = new CategoryEntity("1", "Work", "user-1");
		when(categoryRepository.findById("1")).thenReturn(Optional.of(existing));
		when(categoryRepository.save(any(CategoryEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// Act
		CategoryResponse result = categoryService.assign("1", "user-2");

		// Assert
		assertNotNull(result);
		assertEquals("1", result.id());
		assertEquals("Work", result.name());
		assertEquals("user-2", result.assignedUserId());
	}

	@Test
	void testAssign_nonExistingCategoryRequested_throwsEntityNotFoundException() {
		// Arrange
		when(categoryRepository.findById("999")).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(EntityNotFoundException.class, () -> categoryService.assign("999", "user-2"));
	}

	@Test
	void testGetCategoryByAssignedUserId_existingUserAssignment_returnsCategory() {
		// Arrange
		CategoryEntity category = new CategoryEntity("1", "Work", "user-1");
		when(categoryRepository.findByAssignedUserId("user-1")).thenReturn(Optional.of(category));

		// Act
		CategoryResponse result = categoryService.getCategoryByAssignedUserId("user-1");

		// Assert
		assertNotNull(result);
		assertEquals("1", result.id());
		assertEquals("Work", result.name());
		assertEquals("user-1", result.assignedUserId());
	}

	@Test
	void testGetCategoryByAssignedUserId_nonExistingUserAssignment_throwsEntityNotFoundException() {
		// Arrange
		when(categoryRepository.findByAssignedUserId("user-999")).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(EntityNotFoundException.class, () -> categoryService.getCategoryByAssignedUserId("user-999"));
	}

	@Test
	void testGetTotalCategoriesCount_returnsCountFromRepository() {
		// Arrange
		when(categoryRepository.count()).thenReturn(5L);

		// Act
		long result = categoryService.getTotalCategoriesCount();

		// Assert
		assertEquals(5L, result);
	}

	@Test
	void testGetCategoriesCountByUserId_existingUser_returnsCountFromRepository() {
		// Arrange
		when(categoryRepository.countByAssignedUserId("user-1")).thenReturn(2L);

		// Act
		long result = categoryService.getCategoriesCountByUserId("user-1");

		// Assert
		assertEquals(2L, result);
	}

	@Test
	void testHasAnyCategoryForUserId_userHasCategory_returnsTrue() {
		// Arrange
		when(categoryRepository.findByAssignedUserId("user-1"))
				.thenReturn(Optional.of(new CategoryEntity("1", "Work", "user-1")));

		// Act
		boolean result = categoryService.hasAnyCategoryForUserId("user-1");

		// Assert
		assertTrue(result);
	}

	@Test
	void testHasAnyCategoryForUserId_userHasNoCategory_returnsFalse() {
		// Arrange
		when(categoryRepository.findByAssignedUserId("user-2")).thenReturn(Optional.empty());

		// Act
		boolean result = categoryService.hasAnyCategoryForUserId("user-2");

		// Assert
		assertFalse(result);
	}
}
