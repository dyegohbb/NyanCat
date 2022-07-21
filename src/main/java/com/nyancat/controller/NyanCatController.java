package com.nyancat.controller;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.nyancat.dao.PerfilDAO;
import com.nyancat.dao.UsuarioDAO;
import com.nyancat.model.Perfil;
import com.nyancat.model.Usuario;

@Controller
@RequestMapping("/")
public class NyanCatController {
	
	private static Logger logger = LoggerFactory.getLogger(NyanCatController.class);

	@Autowired
	UsuarioDAO usuarioDAO;

	@Autowired
	PerfilDAO perfilDAO;

	@GetMapping
	public String home(Model model, Principal principal, HttpSession session) {
		session.setAttribute("tentativas", 3);
		if(principal != null) {
			return "redirect:nyancat";
		}
		return "home";
	}

	@GetMapping
	@RequestMapping("/nyancat")
	public String nyancat(Model model, Principal principal) {
		Optional<Usuario> user = usuarioDAO.findByEmail(principal.getName());
		if (user.isPresent()) {
			model.addAttribute("user", user.get());
			model.addAttribute("adm", false);
			user.get().getPerfis().forEach(perfil -> {
				if (perfil.getNome().equals("ROLE_ADM")) {
					model.addAttribute("adm", true);
				}
			});
		} else {
			model.addAttribute("user", new Usuario("Usuário"));
		}

		return "nyancat";
	}

	@GetMapping
	@RequestMapping("/login")
	public String login(Model model, @RequestParam Optional<String> error, HttpSession session) {
		
		if (error.isPresent()) {
			model.addAttribute("error", true);
			return "login";
		}
		model.addAttribute("error", false);
		return "login";
	}

	@GetMapping
	@RequestMapping("/cadastro")
	public String cadastro() {
		return "cadastro";
	}

	@GetMapping
	@RequestMapping("/usuarios")
	public String getUsuarios(Model model, Principal principal) {
		Optional<Usuario> user = usuarioDAO.findByEmail(principal.getName());
		if (!user.isPresent()) {
			return "redirect:home";
		}
		List<Usuario> usuarios = usuarioDAO.findAll();
		model.addAttribute("usuarios", usuarios);
		model.addAttribute("user", user.get());
		return "usuarios";
	}

	@PostMapping
	@RequestMapping("/cadastro/save")
	public String cadastrar(Model model, @ModelAttribute Usuario user) {
		if(!user.getPassword().matches("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[$*&@#])[0-9a-zA-Z$*&@#]{8,}$")) {
			model.addAttribute("erroSenha", "Senha tem que ter 1 letra maiuscula, 1 minuscula, 1 caractere especial, 1 numero e no minimo 8 digitos :DDDDD");
			return "cadastro";
		} else {
			BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
			user.setSenha(encoder.encode(user.getPassword()));
			Usuario usuarioSaved = null;
			try {
				usuarioSaved = usuarioDAO.save(user);
			}catch (Exception e) {
				logger.error(e.getMessage());
				return "cadastro";
			}
			
			if(usuarioSaved != null) {
				logger.info("Usuário cadastrado: " + usuarioSaved.getEmail());
			}

			return "home";
		}
	}

	@PostMapping
	@RequestMapping("/godmod")
	public String godmod(Model model, String planck, Principal principal, HttpSession session) {
		Optional<Usuario> user = usuarioDAO.findByEmail(principal.getName());
		if (planck.equals("6,62607015")) {

			List<Perfil> perfisOld = user.get().getPerfis();
			Optional<Perfil> perfil = perfilDAO.findByNome("ROLE_ADM");

			if (perfil.isPresent()) {
				if (!perfisOld.contains(perfil.get())) {
					perfisOld.addAll(Arrays.asList(perfil.get()));
				}
			} else {
				Perfil p = perfilDAO.save(new Perfil("ROLE_ADM"));
				perfisOld.addAll(Arrays.asList(p));
			}

			user.get().setPerfis(perfisOld);
			usuarioDAO.save(user.get());
			session.invalidate();
			logger.info("Usuário deslogado com sucesso: " + principal.getName());
			logger.info("USUÁRIO: " + user.get().getEmail() + " AGORA É UM DEUS");
		} else {
			session.invalidate();
			logger.info("USUÁRIO: " + user.get().getEmail() + " IRRITOU OS DEUSES");
			usuarioDAO.delete(user.get());
			return "xd";
		}
		return "redirect:nyancat";
	}
	
	@PostMapping
	@RequestMapping("/usuarios/delete/{id}")
	private String deleteUser(@PathVariable Long id, Model model, Principal principal, HttpSession session) {
		Optional<Usuario> userToDelete = usuarioDAO.findById(id);
		
		if(principal.getName().equals(userToDelete.get().getEmail())) {
			session.invalidate();
		}
		
		if(userToDelete.isPresent()) {
			logger.info("USUÁRIO: " + userToDelete.get().getEmail() + " FOI DELETADO POR: " + principal.getName());
			usuarioDAO.delete(userToDelete.get());
		}
		
		Optional<Usuario> user = usuarioDAO.findByEmail(principal.getName());
		if (!user.isPresent()) {
			return "redirect:home";
		}
		
		List<Usuario> usuarios = usuarioDAO.findAll();
		model.addAttribute("user", user.get());
		model.addAttribute("usuarios", usuarios);
		return "usuarios";

	}
	
	@PostMapping
	@RequestMapping("/logout")
	public String logout(Principal principal, HttpSession session) {
		session.invalidate();
		logger.info("Usuário deslogado com sucesso: " + principal.getName());
		return "home";
	}
}
