import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;

import com.todoapp.repository.TagRepository;
import com.todoapp.repository.TodoRepository;

class TodoServiceTest {
    private TodoRepository todoRepository;
    private TagRepository tagRepository;
    private TodoService todoService;

    @BeforeEach
    void setUp() {
        todoRepository = mock(TodoRepository.class);
        tagRepository = mock(TagRepository.class);
        todoService = new TodoService(todoRepository, tagRepository);
    }
}