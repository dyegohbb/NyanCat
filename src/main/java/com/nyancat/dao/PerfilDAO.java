package com.nyancat.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nyancat.model.Perfil;

public interface PerfilDAO extends JpaRepository<Perfil, Long> {
	
	public Optional<Perfil> findById(Long id);
	
	public Optional<Perfil> findByNome(String nome);

}
