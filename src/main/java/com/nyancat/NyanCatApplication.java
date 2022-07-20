package com.nyancat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.nyancat.dao.PerfilDAO;
import com.nyancat.model.Perfil;

@SpringBootApplication
public class NyanCatApplication {

	public static void main(String[] args) {
		SpringApplication.run(NyanCatApplication.class, args);
	}

}
