package com.nyancat.config.security;

import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationFailureDisabledEvent;
import org.springframework.stereotype.Component;

import com.nyancat.dao.UsuarioDAO;
import com.nyancat.model.Usuario;

@Component
public class AuthenticationEventListener {

	private static Logger logger = LoggerFactory.getLogger(AuthenticationEventListener.class);
	
	@Autowired
	UsuarioDAO usuarioDAO;
	
    @EventListener
    public void authenticationFailed(AuthenticationFailureBadCredentialsEvent event) {

        String email = (String) event.getAuthentication().getPrincipal();
        Optional<Usuario> user = usuarioDAO.findByEmail(email);
        if(user.isPresent()) {
        	Usuario usuario = user.get();
        	usuario.setTentativas(usuario.getTentativas() - 1);
        	logger.info(usuario.getTentativas() + " Tentativas de login restantes para: " + usuario.getEmail());
        	if(usuario.getTentativas() <= 0) {
        		usuario.setBloqueado(true);
        		usuario.setDataDesbloqueio(LocalDateTime.now().plusSeconds(30L));
        		logger.info("USUARIO BLOQUEADO: " + usuario.getEmail() + " ATÉ : " + usuario.getDataDesbloqueio());
        	}
        	System.out.println(usuario.getDataDesbloqueio());
        	usuarioDAO.save(usuario);
        }
    }
    
    @EventListener
    private void AuthenticationFailedByBlocked(AuthenticationFailureDisabledEvent event) {
    	
    	String email = (String) event.getAuthentication().getPrincipal();
    	Optional<Usuario> user = usuarioDAO.findByEmail(email);
        if(user.isPresent()) {
        	Usuario usuario = user.get();
        	logger.info("Solicitando desbloqueio para: " + usuario.getEmail());
        	if(usuario.solicitarDesbloqueio()) {
        		usuarioDAO.save(usuario);
        		logger.info("USUARIO DESBLOQUEADO: " + usuario.getEmail());
        	}else {
        		logger.info("Falha no desbloqueio para: " + usuario.getEmail() + " data de desbloqueio não alcançada");
        	}
        }

	}

}
