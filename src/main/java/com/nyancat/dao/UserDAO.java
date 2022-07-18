package com.nyancat.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nyancat.model.User;

public interface UserDAO extends JpaRepository<User, String>{
	
	@Query("select u from User u where u.username = :username")
	public User findByUsername(@Param("username") String username);

}
