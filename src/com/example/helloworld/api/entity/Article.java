package com.example.helloworld.api.entity;

import java.io.Serializable;
import java.util.Date;


public class Article implements Serializable {


	Date createDate;
	Date editDate;
	int id;
	String title;
	String text;
	String authorName;
	String authorAvatar;
	
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public Date getEditDate() {
		return editDate;
	}
	public void setEditDate(Date editDate) {
		this.editDate = editDate;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getAuthorName() {
		return authorName;
	}
	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}
	public String getAuthorAvatar() {
		return authorAvatar;
	}
	public void setAuthorAvatar(String authorAvatar) {
		this.authorAvatar = authorAvatar;
	}
	
	
}
