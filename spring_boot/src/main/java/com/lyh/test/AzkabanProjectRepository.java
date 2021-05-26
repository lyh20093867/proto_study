//package com.lyh.test;
//
//import com.lyh.pojo.AzkabanProject;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Slice;
//import org.springframework.data.jdbc.repository.query.Query;
//import org.springframework.data.repository.PagingAndSortingRepository;
//
//import java.util.List;
//
//interface AzkabanProjectRepository extends PagingAndSortingRepository<AzkabanProject,String> {
//
//    List<AzkabanProject> findByFirstnameOrderByLastname(String firstname, Pageable pageable);
//
//    Slice<AzkabanProject> findByProjectName(String name, Pageable pageable);
//
//    Page<AzkabanProject> findByName(String lastname, Pageable pageable);
//
//    AzkabanProject findByFirstnameAndLastname(String firstname, String lastname);
//
//    AzkabanProject findFirstByLastname(String lastname);
//
//    @Query("SELECT * FROM person WHERE lastname = :lastname")
//    List<AzkabanProject> findByLastname(String lastname);
//}
