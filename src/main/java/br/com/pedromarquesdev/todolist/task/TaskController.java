package br.com.pedromarquesdev.todolist.task;

import br.com.pedromarquesdev.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
public class TaskController {

	@Autowired
	private ITaskRepository taskRepository;

	@PostMapping("/")
	public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
		var idUser =  request.getAttribute("idUser");
		taskModel.setIdUser((UUID) idUser);

		var currentDate = LocalDateTime.now();
		if (currentDate.isAfter(taskModel.getStartAt()) || currentDate.isAfter(taskModel.getEndAt())) {
			return ResponseEntity
					.status(HttpStatus.BAD_REQUEST)
					.body("A data de inicio / data de termino deve ser maior que a data atual");
		}

		if (taskModel.getStartAt().isAfter(taskModel.getEndAt())) {
			return ResponseEntity
					.status(HttpStatus.BAD_REQUEST)
					.body("A data de inicio deve ser menor que a data de término");
		}
		var task = this.taskRepository.save(taskModel);
		return ResponseEntity.status(HttpStatus.OK).body(task);
	}

	@GetMapping("/")
	public List<TaskModel> list(HttpServletRequest request) {
		var idUser =  request.getAttribute("idUser");
		var tasks = this.taskRepository.findByIdUser((UUID) idUser);
		for (TaskModel task: tasks) {
			System.out.println(task.getTitle());
		}
		return tasks;
	}

	@PutMapping("/{id}")
	public ResponseEntity update(@RequestBody TaskModel taskModel, @PathVariable UUID id, HttpServletRequest request) {

		var task = this.taskRepository.findById(id).orElse(null);

		if (task == null) {
			return ResponseEntity
					.status(HttpStatus.BAD_REQUEST)
					.body("Tarefa nao encontrada!");
		}

		var idUser =  request.getAttribute("idUser");

		if (!task.getIdUser().equals(idUser)) {
			return ResponseEntity
					.status(HttpStatus.BAD_REQUEST)
					.body("Usuario nao tem permissao para alterar esta tarefa");
		}

		Utils.copyNonNullProperties(taskModel, task);

		var taskUpdated = this.taskRepository.save(task);

		return  ResponseEntity.ok().body(taskUpdated);
	}

}
