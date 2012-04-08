package visualtasks.com;

public interface ITasksListener {

	void afterTaskAdded(TasksController pTaskController, Task pTask);
	void afterTaskDeleted(TasksController pTaskController, Task pTask );
	void afterTaskPositionUpdated(TasksController pTaskController, Task pTask, float pOldX, float pOldY, float pNewX, float pNewY);
	void afterTaskDescriptionUpdated(TasksController pTaskController, Task pTask, String pOldDescription, String pNewDescription);
}
