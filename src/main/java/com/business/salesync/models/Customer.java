package com.business.salesync.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "customers")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Customer {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@NotBlank
	@Column(name = "name", nullable = false)
	private String name;

	@NotBlank
	@Column(name = "phone_number", nullable = false)
	private String phoneNumber;
	
	@NotBlank
	@Column(name = "email", nullable = false)
	private String email;

	@NotBlank
	@Column(name = "address", nullable = false)
	private String address;
}
