package com.vijay.User_Master.repository;

import com.vijay.User_Master.entity.CodeSnippet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CodeSnippetRepository extends JpaRepository<CodeSnippet, Long> {

    List<CodeSnippet> findByTutorialId(Long tutorialId);

    @Query("SELECT cs FROM CodeSnippet cs WHERE cs.tutorial.id = :tutorialId ORDER BY cs.displayOrder ASC")
    List<CodeSnippet> findByTutorialIdOrderByDisplayOrder(@Param("tutorialId") Long tutorialId);

    List<CodeSnippet> findByTutorialIdAndIsExecutableTrue(Long tutorialId);

    @Query("SELECT COUNT(cs) FROM CodeSnippet cs WHERE cs.tutorial.id = :tutorialId")
    Long countByTutorialId(@Param("tutorialId") Long tutorialId);
}
