package com.nyancat.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nyancat.model.Authority;

public interface AuthorityDAO extends JpaRepository<Authority, String>{
	
	@Query("select a from Authority a where a.username = :username")
	public Authority findByUsername(@Param("username") String username);

}
