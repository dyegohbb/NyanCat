package com.nyancat.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
	
	@Autowired
	UsuarioDAO usuarioDAO;
	
	@Autowired
	PerfilDAO perfilDAO;
	
	@GetMapping
	public String home(Model model, Principal principal) {
		model.addAttribute("titulo", "NyanCat!");
		return "home";
	}
	
	@GetMapping
	@RequestMapping("/nyancat")
	public String nyancat(Model model, Principal principal) {
		Optional<Usuario> user = usuarioDAO.findByEmail(principal.getName());
		if (user.isPresent()) {
			model.addAttribute("user", user.get());
			List<String> perfisList = new ArrayList<String>();
			model.addAttribute("adm", false);
			user.get().getPerfis().forEach(perfil ->{
				if(perfil.getNome().equals("ADM")) {
					model.addAttribute("adm", true);
				}
			});
		}else {
			model.addAttribute("user", new Usuario("Usuário"));
		}
		
		return "nyancat";
	}
	
	@GetMapping
	@RequestMapping("/login")
	public String login(Model model, @RequestParam Optional<String> error) {
		if(error.isPresent()) {
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
	
	@PostMapping
	@RequestMapping("/cadastro/save")
	public String cadastrar(Model model, @ModelAttribute  Usuario user) {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		user.setSenha(encoder.encode(user.getPassword()));
		usuarioDAO.save(user);

		return "home";
	}
	
	@PostMapping
	@RequestMapping("/godmod")
	public String godmod(Model model, String planck, Principal principal, HttpSession session) {
		Optional<Usuario> user = usuarioDAO.findByEmail(principal.getName());
		if(planck.equals("6,62607015")) {
			List<Perfil> perfisOld = user.get().getPerfis();
			
			Optional<Perfil> perfil = perfilDAO.findByNome("ROLE_ADM");
			if(perfil.isPresent()) {
				perfisOld.addAll(Arrays.asList(perfil.get()));
			}else{
				Perfil p = perfilDAO.save(new Perfil("ROLE_ADM"));
				perfisOld.addAll(Arrays.asList(p));
			}
			
			user.get().setPerfis(perfisOld);
			usuarioDAO.save(user.get());
		}else {
			session.invalidate();
			usuarioDAO.delete(user.get());
			return "xd";
		}
		return "redirect:nyancat";
	}
}
