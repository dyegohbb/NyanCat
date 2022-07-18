package com.nyancat.controller;

import java.security.Principal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.nyancat.dao.AuthorityDAO;
import com.nyancat.dao.UserDAO;
import com.nyancat.model.Authority;
import com.nyancat.model.User;

@Controller
@RequestMapping("/")
public class NyanCatController {
	
	@Autowired
	UserDAO userDAO;
	
	@Autowired
	AuthorityDAO authorityDAO;
	
	@GetMapping
	public String home(Model model, Principal principal) {
		model.addAttribute("titulo", "NyanCat!");
		return "home";
	}
	
	@GetMapping
	@RequestMapping("/nyancat")
	public String nyancat(Model model, Principal principal) {
		User user = userDAO.findByUsername(principal.getName());
		model.addAttribute("user", user);
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
	public String cadastrar(Model model, User user) {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		user.setPassword(encoder.encode(user.getPassword()));
		user.setEnabled(true);
		userDAO.save(user);
		
		Authority authority = new Authority(user.getUsername(), "ROLE_ADM");
		authorityDAO.save(authority);
		return "home";
	}
}
