package io.transwarp.kundb.demo.entity;

import java.io.Serializable;
import java.sql.Timestamp;

public class Xuser implements Serializable{
	private Integer id;

	private String name;

	private Integer age;
	
	private Integer isDelete;
	private Timestamp updateTime;

	public Integer getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public Integer getAge()
	{
		return age;
	}

	public void setId(Integer id)
	{
		this.id = id;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setAge(Integer age)
	{
		this.age = age;
	}
	
	public Integer getIsDelete() {
		return isDelete;
	}
	
	public void setIsDelete(Integer isDelete) {
		this.isDelete = isDelete;
	}
	
	public Timestamp getUpdateTime() {
		return updateTime;
	}
	
	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}
}
