package app.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import app.entity.Task;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

}
