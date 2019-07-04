package com.ibm.isl.ta.vo;

public class TaskPayload {

	    String taskname;
	    int status;
	    String tenantId;
	    Preferences preferences;
	    String uploadKey;
	    String workspace;
	    String collection;
	    
		public String getTaskname() {
			return taskname;
		}
		public void setTaskname(String taskname) {
			this.taskname = taskname;
		}
		public int getStatus() {
			return status;
		}
		public void setStatus(int status) {
			this.status = status;
		}
		public String getTenantId() {
			return tenantId;
		}
		public void setTenantId(String tenantId) {
			this.tenantId = tenantId;
		}
		public Preferences getPreferences() {
			return preferences;
		}
		public void setPreferences(Preferences preferences) {
			this.preferences = preferences;
		}
		public String getUploadKey() {
			return uploadKey;
		}
		public void setUploadKey(String uploadKey) {
			this.uploadKey = uploadKey;
		}
		public String getWorkspace() {
			return workspace;
		}
		public void setWorkspace(String workspace) {
			this.workspace = workspace;
		}
		public String getCollection() {
			return collection;
		}
		public void setCollection(String collection) {
			this.collection = collection;
		}
}



