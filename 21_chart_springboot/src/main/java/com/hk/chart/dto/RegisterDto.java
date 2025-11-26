package com.hk.chart.dto;

import org.hibernate.validator.constraints.Length;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RegisterDto {

    @NotBlank(message = "아이디를 입력해주세요")
    private String username;

    @NotBlank(message = "패스워드를 입력해주세요")
    @Length(min = 8, max = 16, message = "8자리이상, 16자이하로 입력하세요")
    private String password;

    @NotBlank(message = "이름을 입력해주세요")
    private String name;

    @Email(message = "올바른 이메일을 입력해주세요")
    private String email;

}