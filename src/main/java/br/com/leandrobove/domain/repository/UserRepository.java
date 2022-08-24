package br.com.leandrobove.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import br.com.leandrobove.domain.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	@Query("from User u join fetch u.roles r where u.username = :username")
	Optional<User> findByUsername(String username);
	
	@Query("select distinct u from User u left join fetch u.roles")
	List<User> findAll();

	Optional<User> findByUuid(String uuid);
}
