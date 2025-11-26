package com.hk.chart.command;


import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class LoginCommand {
	@NotBlank(message = "아이디를 입력해주세요")
	private String username;
	
	@NotBlank(message = "패스워드를 입력해주세요")
	@Length(min = 8 , max = 16, message = "8자리이상, 16자이하로 입력하세요")
	private String password;

	public LoginCommand(@NotBlank(message = "아이디를 입력해주세요") String username,
			@NotBlank(message = "패스워드를 입력해주세요") @Length(min = 8, max = 16, message = "8자리이상, 16자이하로 입력하세요") String password) {
		super();
		this.username = username;
		this.password = password;
	}

}