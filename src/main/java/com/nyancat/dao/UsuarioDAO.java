package com.nyancat.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nyancat.model.Usuario;

public interface UsuarioDAO extends JpaRepository<Usuario, String>{
	
	public Optional<Usuario> findByEmail(String email);
	
	public Optional<Usuario> findById(Long id);
	
	public void deleteById(Long id);

}
