package com.nyancat.config.security;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.nyancat.dao.UsuarioDAO;
import com.nyancat.model.Usuario;

@Service
public class AuthService implements UserDetailsService{
	
	private static Logger logger = LoggerFactory.getLogger(AuthService.class);
	
	@Autowired
	UsuarioDAO usuarioDAO;

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		Optional<Usuario> usuario = usuarioDAO.findByEmail(email);
		
		if (usuario.isPresent()) {
			logger.info("Usuário logado com sucesso: " + email);
			return usuario.get();
		}
		UsernameNotFoundException ex = new UsernameNotFoundException("Falha ao logar: Usuário ou senha incorretos");
		logger.warn(ex.getMessage());
		throw ex;
	}

}
