package com.iusupov.myvpn.server;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Ernest Iusupov
 */
public interface UserRepository extends JpaRepository<User, Long> {
}
