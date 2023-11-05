package com.hhl.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "exness")
public class Exness {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	private String exness;
	private String server;
	private String password;
	private String passview;
	private double balance;
	private double prevBalance;
	private double totalProfit;
	private boolean isActive;
	
	@Column(columnDefinition="TEXT")
	private String message;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "user_id")
	public User user;
}