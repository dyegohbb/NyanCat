package com.nyancat.config.security;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SuppressWarnings("deprecation")
@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Autowired
	DataSource dataSource;
	
	@Autowired
	private AuthService authService;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests().antMatchers("/image/**").permitAll().antMatchers("/").permitAll().antMatchers("/css/**").permitAll()
				.antMatchers("/cadastro/**").permitAll()
				.antMatchers("/usuarios/**").hasRole("ADM")
				.anyRequest().authenticated().and()
				.formLogin(form -> form.loginPage("/login").permitAll().defaultSuccessUrl("/nyancat", true))
				.logout(logout -> {
					logout.logoutUrl("/logout").logoutSuccessUrl("/");
				}).csrf().disable();
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		auth.userDetailsService(authService).passwordEncoder(encoder);
	}

}
