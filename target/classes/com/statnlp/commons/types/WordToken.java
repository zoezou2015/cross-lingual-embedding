package com.statnlp.commons.types;


public class WordToken extends InputToken{
	
	private static final long serialVersionUID = -1296542134339296118L;
	
	private String tag;
	private String aTag;
	private int headIndex; 
	private String entity;
	private String depLabel;
	
	public WordToken(String name) {
		super(name);
		this.tag = "";
		this.headIndex = -1;
		this.entity = "O";
		this.aTag = "";
	}
	
	public WordToken(String name, String tag) {
		super(name);
		this.tag = tag;
		this.headIndex = -1;
		this.entity = "O";
		this.aTag = tag.substring(0, 1);
	}
	
	public WordToken(String name, String tag, int headIndex) {
		super(name);
		this.tag = tag;
		this.headIndex = headIndex;
		this.entity = "O";
		this.aTag = tag.substring(0, 1);
	}
	
	public WordToken(String name, String tag, int headIndex, String entity) {
		super(name);
		this.tag = tag;
		this.headIndex = headIndex;
		this.entity = entity;
		this.aTag = tag.substring(0, 1);
	}
	
	public WordToken(String name, String tag, int headIndex, String entity, String depLabel) {
		super(name);
		this.tag = tag;
		this.headIndex = headIndex;
		this.entity = entity;
		this.aTag = tag.substring(0, 1);
		this.depLabel = depLabel;
	}
	
	
	public String getTag(){
		return this.tag;
	}
	
	public String getATag(){
		return this.aTag;
	}
	
	public void setHead(int index){
		this.headIndex = index;
	}
	
	public int getHeadIndex(){
		return this.headIndex;
	}
	
	public void setEntity(String entity){
		this.entity = entity;
	}
	
	public String getEntity(){
		return this.entity;
	}
	
	
	
	public String getDepLabel() {
		return depLabel;
	}

	public void setDepLabel(String depLabel) {
		this.depLabel = depLabel;
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof WordToken){
			WordToken w = (WordToken)o;
			return w._name.equals(this._name) && w.tag.equals(this.tag) && (w.headIndex == this.headIndex) && w.entity.equals(this.entity);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return this._name.hashCode() +this.tag.hashCode() + this.headIndex + this.entity.hashCode() + 7;
	}
	
	@Override
	public String toString() {
		if(!tag.equals("")) return "Word:"+this._name+"/"+tag+","+headIndex+","+entity;
		return "WORD:"+this._name;
	}
	
	
}