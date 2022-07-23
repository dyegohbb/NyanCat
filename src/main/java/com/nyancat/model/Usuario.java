package com.nyancat.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Transient;

import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.springframework.data.history.RevisionMetadata;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Audited
public class Usuario implements UserDetails {

	private static final long serialVersionUID = -9033808678351022373L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(unique = true)
	private String email;
	private String nome;
	private String senha;
	private Integer tentatives = 3;
	
	@Column(columnDefinition="tinyint(1) default 0")
	private boolean bloqueado = false;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd hh mm ss")
	private LocalDateTime dataDesbloqueio;

	@NotAudited
	@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
	private List<Perfil> perfis = new ArrayList<Perfil>(Arrays.asList(new Perfil("ROLE_USER")));
	
	@Transient
	private RevisionMetadata<Integer> editVersion;

	public Usuario() {
	}

	public Usuario(String nome) {
		this.nome = nome;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

	public List<Perfil> getPerfis() {
		return perfis;
	}

	public void setPerfis(List<Perfil> perfis) {
		this.perfis = perfis;
	}
	

	public Integer getTentativas() {
		return tentatives;
	}

	public void setTentativas(Integer tentatives) {
		this.tentatives = tentatives;
	}

	public boolean getBloqueado() {
		return bloqueado;
	}
	
	/** Método tentativa de desbloqueio
	 * @return Boolean - TRUE: desbloqueado; FALSE: ainda bloqueado*/
	public boolean solicitarDesbloqueio() {
		if(this.bloqueado && this.dataDesbloqueio != null) {
			if(LocalDateTime.now().isAfter(dataDesbloqueio)) {
				this.bloqueado = false;
				this.tentatives = 3;
			}
		}
		return !bloqueado;
	}

	public void setBloqueado(boolean bloqueado) {
		this.bloqueado = bloqueado;
	}

	public LocalDateTime getDataDesbloqueio() {
		return dataDesbloqueio;
	}

	public void setDataDesbloqueio(LocalDateTime dataDesbloqueio) {
		this.dataDesbloqueio = dataDesbloqueio;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// TODO Auto-generated method stub
		return this.perfis;
	}

	@Override
	public String getPassword() {
		// TODO Auto-generated method stub
		return this.senha;
	}

	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return this.email;
	}

	@Override
	public boolean isAccountNonExpired() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return !bloqueado;
	}

	public RevisionMetadata<Integer> getEditVersion() {
		return editVersion;
	}

	public void setEditVersion(RevisionMetadata<Integer> editVersion) {
		this.editVersion = editVersion;
	}
	
}
