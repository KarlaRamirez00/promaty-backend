package com.promaty.rrhh.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.promaty.rrhh.entity.Request;
import com.promaty.rrhh.entity.RequestEntityType;
import com.promaty.rrhh.repository.projection.RequesterOptionProjection;

public interface RequestRepository extends JpaRepository<Request, Long>, JpaSpecificationExecutor<Request> {

	Optional<Request> findByEntityTypeAndEntityId(RequestEntityType entityType, Long entityId);

	List<Request> findByEntityTypeAndEntityIdIn(RequestEntityType entityType, List<Long> entityIds);

	@Query("""
		select distinct r.requesterUserId as id, r.createdBy as name
		from Request r
		where r.entityType in :entityTypes
		""")
	List<RequesterOptionProjection> findRequesters(@Param("entityTypes") List<RequestEntityType> entityTypes);

	@Query("""
		select distinct r.requesterUserId as id, r.createdBy as name
		from Request r
		where r.entityType in :entityTypes and r.project.id in :projectIds
		""")
	List<RequesterOptionProjection> findRequestersInProjects(
		@Param("entityTypes") List<RequestEntityType> entityTypes,
		@Param("projectIds") List<Long> projectIds
	);
}
