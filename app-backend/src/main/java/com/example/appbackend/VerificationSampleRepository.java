package com.example.appbackend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface VerificationSampleRepository extends JpaRepository<VerificationSample, Long> {

}
