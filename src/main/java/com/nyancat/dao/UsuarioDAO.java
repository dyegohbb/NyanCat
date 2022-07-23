package com.nyancat.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.nyancat.model.Usuario;

@Repository
public interface UsuarioDAO extends CrudRepository<Usuario, Integer>, JpaRepository<Usuario, Integer>{
	
	public Optional<Usuario> findByEmail(String email);
	
	public Optional<Usuario> findById(Long id);
	
	public void deleteById(Long id);

}

