package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Link;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Link entity.
 */
@SuppressWarnings("unused")
public interface LinkRepository extends JpaRepository<Link,Long> {

}
