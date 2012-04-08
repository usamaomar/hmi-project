package visualtasks.com;

import java.util.HashMap;

public class TasksController {
	private HashMap<Integer, Task> mTaskList;
	private int mNewTaskID = 0;
	
	private ITasksListener mITaskListener;
	
	public TasksController(ITasksListener pITaskListener) {
		this.mITaskListener = pITaskListener;
		init();
	}
	
	private void init(){
		mTaskList = new HashMap<Integer,Task>();
	}
	
	
	public int addTask(String pDescription, float pX, float pY){
		final int taskID = mNewTaskID++;
		final Task task = new Task(taskID,  pDescription, pX, pY);
		
		this.mTaskList.put(taskID, task);
		
		mITaskListener.afterTaskAdded(this, task);
		return taskID;
	}
	
		
	public void updateTaskDescription(int pTaskID, String pDescription) throws UnknownTaskExeption{
		final Task task = getTask(pTaskID);
		final String oldDescription = task.getDescription();
		task.setDescription(pDescription);
		mITaskListener.afterTaskDescriptionUpdated(this, task, oldDescription, pDescription);
		
	}
	
	public void updateTaskPosition(int pTaskID, float pX, float pY) throws UnknownTaskExeption{
		final Task task = getTask(pTaskID);
		final float oldX = task.getX();
		final float oldY = task.getY();
		task.setX(pX);
		task.setY(pY);
		mITaskListener.afterTaskPositionUpdated(this, task, oldX, oldY, pX, pY);
		
	}
	
	public void deleteTask(int pTaskID) throws UnknownTaskExeption{
		final Task task = getTask(pTaskID);
		this.mTaskList.remove(pTaskID);
		mITaskListener.afterTaskDeleted(this, task);
		
	}
	
	public void invalidateTask(int pTaskID) throws UnknownTaskExeption{
		final Task task = getTask(pTaskID);
		mITaskListener.afterTaskAdded(this, task);
		
	}
	
	public Task getTask(int pTaskID) throws UnknownTaskExeption{
		if(!mTaskList.containsKey(pTaskID)){
			throw new UnknownTaskExeption("Unknown task id: "+ pTaskID);
		}
		return mTaskList.get(pTaskID);
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
