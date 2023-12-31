package com.laptopshop.entities;

import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class VaiTro {

	@Id
	@GeneratedValue
	private long id;
	
	private String tenVaiTro;

	@JsonIgnore
	@ManyToMany(mappedBy = "vaiTro")
	private Set<NguoiDung> nguoiDung;

	public String getTenVaiTro() {
		return tenVaiTro;
	}

	public void setTenVaiTro(String tenVaiTro) {
		this.tenVaiTro = tenVaiTro;
	}

	public Set<NguoiDung> getNguoiDung() {
		return nguoiDung;
	}

	public void setNguoiDung(Set<NguoiDung> nguoiDung) {
		this.nguoiDung = nguoiDung;
	}
	
	public VaiTro(String tenVaiTro) {
		this.tenVaiTro = tenVaiTro;
	}
	
	public VaiTro() {
		// TODO Auto-generated constructor stub
	}
}
