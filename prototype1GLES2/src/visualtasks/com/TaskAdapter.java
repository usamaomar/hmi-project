package visualtasks.com;

import java.util.HashMap;
import java.util.List;

public class TaskAdapter {
	private HashMap<Integer, Task> mTaskList;
	private int mNewTaskID = 0;
	
	private ITasksListener mITaskListener;
	
	public TaskAdapter(ITasksListener pITaskListener) {
		this.mITaskListener = pITaskListener;
		init();
	}
	
	private void init(){
		mTaskList = new HashMap<Integer,Task>();
	}
	
	
	public int addTask(String pDescription, float pX, float pY){
		final int taskID = mNewTaskID++;
		final Task task = new Task(taskID,  pDescription, pX, pY);
		this.putTask(task);
		
		return taskID;
	}
	
		
	private void putTask(Task pTask){
		this.mTaskList.put(pTask.getID(), pTask);
		
		mITaskListener.onTaskAdded(this, pTask);
	}
	public void updateTaskDescription(int pTaskID, String pDescription) throws UnknownTaskExeption{
		final Task task = getTask(pTaskID);
		final String oldDescription = task.getDescription();
		task.setDescription(pDescription);
		mITaskListener.onTaskDescriptionUpdated(this, task, oldDescription, pDescription);
		
	}
	
	public void updateTaskPosition(int pTaskID, float pX, float pY) throws UnknownTaskExeption{
		final Task task = getTask(pTaskID);
		final float oldX = task.getX();
		final float oldY = task.getY();
		task.setX(pX);
		task.setY(pY);
		mITaskListener.onTaskPositionUpdated(this, task, oldX, oldY, pX, pY);
		
	}
	
	public void deleteTask(int pTaskID) throws UnknownTaskExeption{
		final Task task = getTask(pTaskID);
		this.mTaskList.remove(pTaskID);
		mITaskListener.onTaskDeleted(this, task);
		
	}
	
	
	
	public Task getTask(int pTaskID) throws UnknownTaskExeption{
		if(!mTaskList.containsKey(pTaskID)){
			throw new UnknownTaskExeption("Unknown task id: "+ pTaskID);
		}
		return mTaskList.get(pTaskID);
	}
	
	public void setTasks(List<Task> tasks){
		this.mTaskList.clear();
		
		for (Task task : tasks){
			this.putTask(task);
		}
	}
	
	public List<Task> getTasks(){
		return (List<Task>) this.mTaskList.values();
	}
	
	class UnknownTaskExeption extends Exception{
	
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public UnknownTaskExeption(String message) {
			super(message);
			// TODO Auto-generated constructor stub
		}
	}
}
