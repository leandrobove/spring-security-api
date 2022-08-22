package br.com.leandrobove;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import br.com.leandrobove.domain.model.Role;
import br.com.leandrobove.domain.model.User;
import br.com.leandrobove.domain.service.RoleService;
import br.com.leandrobove.domain.service.UserService;

@SpringBootApplication
public class SpringsecuritytestApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringsecuritytestApplication.class, args);
	}

	@Bean
	CommandLineRunner run(RoleService roleService, UserService userService) {
		return args -> {
			Role admin = new Role(null, "ROLE_ADMIN");
			Role user = new Role(null, "ROLE_USER");
			
			roleService.saveRole(admin);
			roleService.saveRole(user);
			
			User joao = new User(null, "joao", "senha123");
			User maria = new User(null, "maria", "senha123");
			
			userService.saveUser(joao);
			userService.saveUser(maria);
			
			userService.addRoleToUser("joao", "ROLE_USER");
			userService.addRoleToUser("maria", "ROLE_ADMIN");
			
		};
	}
}
