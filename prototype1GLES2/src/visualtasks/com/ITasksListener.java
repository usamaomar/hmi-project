package visualtasks.com;

public interface ITasksListener {

	void onTaskAdded(TaskAdapter pTaskController, Task pTask);
	void onTaskDeleted(TaskAdapter pTaskController, Task pTask );
	void onTaskPositionUpdated(TaskAdapter pTaskController, Task pTask, float pOldX, float pOldY, float pNewX, float pNewY);
	void onTaskDescriptionUpdated(TaskAdapter pTaskController, Task pTask, String pOldDescription, String pNewDescription);
}
