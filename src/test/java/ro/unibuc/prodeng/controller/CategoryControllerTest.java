package ro.unibuc.prodeng.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.request.CreateCategoryRequest;
import ro.unibuc.prodeng.response.CategoryResponse;
import ro.unibuc.prodeng.service.CategoryService;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
public class CategoryControllerTest {

	@Mock
	private CategoryService categoryService;

	@InjectMocks
	private CategoryController categoryController;

	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	private final CategoryResponse testCategory1 = new CategoryResponse("1", "Work", "user-1");
	private final CategoryResponse testCategory2 = new CategoryResponse("2", "Personal", "user-2");
	private final CreateCategoryRequest createCategoryRequest = new CreateCategoryRequest("Work", "user-1");

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(categoryController).build();
	}

	@Test
	void testGetAllCategories_withMultipleCategories_returnsListOfCategories() throws Exception {
		// Arrange
		List<CategoryResponse> categories = Arrays.asList(testCategory1, testCategory2);
		when(categoryService.getAllCategories()).thenReturn(categories);

		// Act & Assert
		mockMvc.perform(get("/api/categories")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].id", is("1")))
				.andExpect(jsonPath("$[0].name", is("Work")))
				.andExpect(jsonPath("$[0].assignedUserId", is("user-1")))
				.andExpect(jsonPath("$[1].id", is("2")))
				.andExpect(jsonPath("$[1].name", is("Personal")))
				.andExpect(jsonPath("$[1].assignedUserId", is("user-2")));

		verify(categoryService, times(1)).getAllCategories();
	}

	@Test
	void testGetAllCategories_withNoCategories_returnsEmptyList() throws Exception {
		// Arrange
		when(categoryService.getAllCategories()).thenReturn(Arrays.asList());

		// Act & Assert
		mockMvc.perform(get("/api/categories")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(0)));

		verify(categoryService, times(1)).getAllCategories();
	}

	@Test
	void testGetCategoryById_existingCategoryRequested_returnsCategory() throws Exception {
		// Arrange
		String categoryId = "1";
		when(categoryService.getCategoryById(categoryId)).thenReturn(testCategory1);

		// Act & Assert
		mockMvc.perform(get("/api/categories/{id}", categoryId)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is("1")))
				.andExpect(jsonPath("$.name", is("Work")))
				.andExpect(jsonPath("$.assignedUserId", is("user-1")));

		verify(categoryService, times(1)).getCategoryById(categoryId);
	}

	@Test
	void testGetCategoryById_nonExistingCategoryRequested_returnsNotFound() throws Exception {
		// Arrange
		String categoryId = "999";
		when(categoryService.getCategoryById(categoryId)).thenThrow(new EntityNotFoundException("Category"));

		// Act & Assert
		mockMvc.perform(get("/api/categories/{id}", categoryId)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());

		verify(categoryService, times(1)).getCategoryById(categoryId);
	}

	@Test
	void testCreateCategory_validRequestProvided_createsAndReturnsCategory() throws Exception {
		// Arrange
		when(categoryService.createCategory(any(CreateCategoryRequest.class))).thenReturn(testCategory1);

		// Act & Assert
		mockMvc.perform(post("/api/categories")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createCategoryRequest)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is("1")))
				.andExpect(jsonPath("$.name", is("Work")))
				.andExpect(jsonPath("$.assignedUserId", is("user-1")));

		verify(categoryService, times(1)).createCategory(any(CreateCategoryRequest.class));
	}

	@Test
	void testDeleteCategory_existingCategoryRequested_returnsNoContent() throws Exception {
		// Arrange
		String categoryId = "1";

		// Act & Assert
		mockMvc.perform(delete("/api/categories/{id}", categoryId)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNoContent())
				.andExpect(content().string(""));

		verify(categoryService, times(1)).deleteCategory(categoryId);
	}

	@Test
	void testDeleteCategory_nonExistingCategoryRequested_returnsNotFound() throws Exception {
		// Arrange
		String categoryId = "999";
		doThrow(new EntityNotFoundException("Category")).when(categoryService).deleteCategory(categoryId);

		// Act & Assert
		mockMvc.perform(delete("/api/categories/{id}", categoryId)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());

		verify(categoryService, times(1)).deleteCategory(categoryId);
	}

	@Test
	void testGetTotalCategoriesCount_returnsCount() throws Exception {
		// Arrange
		when(categoryService.getTotalCategoriesCount()).thenReturn(5L);

		// Act & Assert
		mockMvc.perform(get("/api/categories/count")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().string("5"));

		verify(categoryService, times(1)).getTotalCategoriesCount();
	}

	@Test
	void testGetCategoriesCountByUserId_existingUserRequested_returnsCount() throws Exception {
		// Arrange
		String userId = "user-1";
		when(categoryService.getCategoriesCountByUserId(userId)).thenReturn(2L);

		// Act & Assert
		mockMvc.perform(get("/api/categories/user/{userId}/count", userId)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().string("2"));

		verify(categoryService, times(1)).getCategoriesCountByUserId(userId);
	}

	@Test
	void testHasAnyCategoryForUserId_userHasCategory_returnsTrue() throws Exception {
		// Arrange
		String userId = "user-1";
		when(categoryService.hasAnyCategoryForUserId(userId)).thenReturn(true);

		// Act & Assert
		mockMvc.perform(get("/api/categories/user/{userId}/has-any", userId)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().string("true"));

		verify(categoryService, times(1)).hasAnyCategoryForUserId(userId);
	}

	@Test
	void testHasAnyCategoryForUserId_userHasNoCategory_returnsFalse() throws Exception {
		// Arrange
		String userId = "user-2";
		when(categoryService.hasAnyCategoryForUserId(userId)).thenReturn(false);

		// Act & Assert
		mockMvc.perform(get("/api/categories/user/{userId}/has-any", userId)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().string("false"));

		verify(categoryService, times(1)).hasAnyCategoryForUserId(userId);
	}
}
