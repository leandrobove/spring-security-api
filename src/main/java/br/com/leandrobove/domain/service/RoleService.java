package br.com.leandrobove.domain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.leandrobove.domain.exception.RoleNotFoundException;
import br.com.leandrobove.domain.model.Role;
import br.com.leandrobove.domain.repository.RoleRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RoleService {

	@Autowired
	private RoleRepository roleRepository;

	public Role findByName(String name) {
		return roleRepository.findByName(name).orElseThrow(() -> new RoleNotFoundException(name));
	}

	@Transactional
	public Role saveRole(Role role) {
		log.info("Saving new role {} to the database", role.getName());

		return roleRepository.save(role);
	}
	
	@Transactional
	public void removeRole(Role role) {
		log.info("Removing role {} from the database", role.getName());
		
		roleRepository.delete(role);
	}
}
